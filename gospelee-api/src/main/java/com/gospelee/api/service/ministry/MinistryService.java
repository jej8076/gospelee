package com.gospelee.api.service.ministry;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ministry.MinistryDtos.Detail;
import com.gospelee.api.dto.ministry.MinistryDtos.Invite;
import com.gospelee.api.dto.ministry.MinistryDtos.InviteInfo;
import com.gospelee.api.dto.ministry.MinistryDtos.JoinResult;
import com.gospelee.api.dto.ministry.MinistryDtos.LeaveResult;
import com.gospelee.api.dto.ministry.MinistryDtos.Member;
import com.gospelee.api.dto.ministry.MinistryDtos.Request;
import com.gospelee.api.dto.ministry.MinistryDtos.SaveRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.Summary;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.MinistryStatus;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모임 생성·멤버십·초대·대표 승계. 권한은 모임별 역할로만 판정하고, 계정의 교회나 전역 역할은 쓰지 않는다.
 * 한도(대표 3개, 가입 10개)와 대표 승계는 계정·모임 행을 잠가 동시 요청에도 지켜진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryService {

  public static final int MAX_OWNED = 3;
  public static final int MAX_JOINED = 10;

  private static final String INVITE_CODE_CHARS = "abcdefghjkmnpqrstuvwxyz23456789";
  private static final int INVITE_CODE_LENGTH = 12;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String NOT_FOUND = "모임을 찾을 수 없습니다.";

  private final MinistryRepository ministryRepository;
  private final MinistryMemberRepository memberRepository;
  private final AccountRepository accountRepository;
  private final MinistryPushNotifier pushNotifier;
  private final MinistryDataCleaner dataCleaner;

  // ---------------------------------------------------------------- 조회

  @Transactional(readOnly = true)
  public List<Summary> getMine() {
    Long uid = currentUid();
    List<MinistryMember> memberships = memberRepository.findByAccountUid(uid).stream()
        .filter(m -> m.getStatus() != MinistryMemberStatus.REMOVED).toList();
    Map<Long, Ministry> ministries = ministryRepository
        .findAllById(memberships.stream().map(MinistryMember::getMinistryId).toList()).stream()
        .collect(Collectors.toMap(Ministry::getId, Function.identity()));
    return memberships.stream()
        .filter(m -> ministries.containsKey(m.getMinistryId()))
        .sorted(Comparator.comparing(MinistryMember::getInsertTime,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .map(m -> {
          Ministry ministry = ministries.get(m.getMinistryId());
          return new Summary(ministry.getId(), ministry.getName(), ministry.getStatus(),
              m.getRole(), m.getStatus());
        }).toList();
  }

  @Transactional(readOnly = true)
  public Detail get(Long id) {
    MinistryMember me = requireActive(id, currentUid());
    Ministry ministry = findMinistry(id);
    boolean manager = me.getRole().canManage();
    return new Detail(ministry.getId(), ministry.getName(), ministry.getDescription(),
        ministry.getStatus(), me.getRole(), me.getStatus(),
        memberRepository.countByMinistryIdAndStatus(id, MinistryMemberStatus.ACTIVE),
        manager ? ministry.getInviteCode() : null,
        manager ? ministry.isInviteAutoApprove() : null);
  }

  @Transactional(readOnly = true)
  public List<Member> getMembers(Long id) {
    requireActive(id, currentUid());
    List<MinistryMember> members = memberRepository
        .findByMinistryIdAndStatus(id, MinistryMemberStatus.ACTIVE);
    Map<Long, Account> accounts = accountsOf(members);
    return members.stream()
        .sorted(Comparator.comparing((MinistryMember m) -> m.getRole().ordinal())
            .thenComparing(MinistryMember::getJoinedAt,
                Comparator.nullsLast(Comparator.naturalOrder())))
        .map(m -> new Member(m.getAccountUid(), displayName(accounts.get(m.getAccountUid())),
            m.getRole(), m.getJoinedAt()))
        .toList();
  }

  // ---------------------------------------------------------------- 생성·수정·삭제

  @Transactional
  public Detail create(SaveRequest request) {
    Long uid = currentUid();
    lockAccount(uid);
    if (memberRepository.countByAccountUidAndRole(uid, MinistryRole.OWNER) >= MAX_OWNED) {
      throw new IllegalArgumentException(
          "대표로 있는 모임은 최대 " + MAX_OWNED + "개까지 만들 수 있습니다.");
    }
    assertJoinRoom(uid);

    Ministry ministry = ministryRepository.save(Ministry.builder()
        .name(normalizeName(request.name()))
        .description(normalizeDescription(request.description()))
        .inviteCode(generateInviteCode())
        .build());
    memberRepository.save(MinistryMember.builder()
        .ministryId(ministry.getId()).accountUid(uid)
        .role(MinistryRole.OWNER).status(MinistryMemberStatus.ACTIVE)
        .joinedAt(LocalDateTime.now()).build());
    log.info("모임 생성 - id:{}, ownerUid:{}", ministry.getId(), uid);
    return get(ministry.getId());
  }

  @Transactional
  public Detail update(Long id, SaveRequest request) {
    requireManager(id);
    Ministry ministry = findWritableMinistry(id);
    ministry.change(normalizeName(request.name()), normalizeDescription(request.description()));
    return get(id);
  }

  /** 대표의 의도적 삭제. 멤버가 남아 있어도 삭제하며, 확인 절차는 앱에서 받는다 */
  @Transactional
  public Map<String, Object> delete(Long id) {
    Long uid = currentUid();
    Ministry ministry = lockMinistry(id);
    MinistryMember me = requireActive(id, uid);
    if (me.getRole() != MinistryRole.OWNER) {
      throw new AccessDeniedException("모임 대표만 모임을 삭제할 수 있습니다.");
    }
    deleteMinistry(ministry);
    return Map.of("id", id);
  }

  // ---------------------------------------------------------------- 탈퇴·승계

  @Transactional
  public LeaveResult leave(Long id, Long successorUid) {
    Long uid = currentUid();
    lockAccount(uid);
    Ministry ministry = lockMinistry(id);
    MinistryMember me = memberRepository.findByMinistryIdAndAccountUid(id, uid)
        .filter(m -> m.getStatus() != MinistryMemberStatus.REMOVED)
        .orElseThrow(() -> new NoSuchElementException(NOT_FOUND));
    if (me.getRole() != MinistryRole.OWNER) {
      memberRepository.delete(me);
      return new LeaveResult("LEFT", null);
    }
    return handOverOrDelete(ministry, me, successorUid);
  }

  /**
   * 계정 탈퇴 시 모든 모임에서 나간다. 대표인 모임은 자동 승계(또는 삭제)로 처리한다.
   * 호출하는 쪽의 트랜잭션에 참여한다.
   */
  @Transactional
  public void leaveAll(Long accountUid) {
    lockAccount(accountUid);
    for (MinistryMember membership : memberRepository.findByAccountUid(accountUid)) {
      Optional<Ministry> found = ministryRepository.findByIdForUpdate(membership.getMinistryId());
      if (found.isEmpty()) {
        memberRepository.delete(membership);
        continue;
      }
      MinistryMember current = memberRepository.findById(membership.getId()).orElse(null);
      if (current == null) {
        continue;
      }
      if (current.getRole() == MinistryRole.OWNER && current.isActive()) {
        handOverOrDelete(found.get(), current, null);
      } else {
        memberRepository.delete(current);
      }
    }
  }

  private LeaveResult handOverOrDelete(Ministry ministry, MinistryMember owner,
      Long successorUid) {
    List<MinistryMember> candidates = memberRepository
        .findByMinistryIdAndStatus(ministry.getId(), MinistryMemberStatus.ACTIVE).stream()
        .filter(m -> !m.getAccountUid().equals(owner.getAccountUid()))
        .filter(m -> isLiveAccount(m.getAccountUid()))
        .toList();

    MinistryMember successor;
    if (successorUid != null) {
      successor = candidates.stream().filter(m -> m.getAccountUid().equals(successorUid))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("양도할 멤버를 찾을 수 없습니다."));
      lockAccount(successor.getAccountUid());
      if (ownedCount(successor.getAccountUid()) >= MAX_OWNED) {
        throw new IllegalArgumentException(
            "선택한 멤버는 이미 대표로 있는 모임이 " + MAX_OWNED + "개라 넘겨받을 수 없습니다.");
      }
    } else {
      successor = pickSuccessor(candidates);
    }

    // 대표는 한 명이어야 하므로 기존 대표 행을 먼저 지우고 반영한 뒤 승계한다
    memberRepository.delete(owner);
    memberRepository.flush();
    if (successor == null) {
      deleteMinistry(ministry);
      return new LeaveResult("DELETED", null);
    }
    successor.changeRole(MinistryRole.OWNER);
    memberRepository.save(successor);
    log.info("모임 대표 승계 - ministryId:{}, from:{}, to:{}", ministry.getId(),
        owner.getAccountUid(), successor.getAccountUid());
    return new LeaveResult("TRANSFERRED", successor.getAccountUid());
  }

  /** 운영진 우선, 같은 역할 안에서는 가입일이 이른 순. 대표 한도에 걸린 사람은 건너뛴다 */
  private MinistryMember pickSuccessor(List<MinistryMember> candidates) {
    List<MinistryMember> ordered = candidates.stream()
        .sorted(Comparator.comparing((MinistryMember m) -> m.getRole() == MinistryRole.MANAGER ? 0 : 1)
            .thenComparing(MinistryMember::getJoinedAt,
                Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
    for (MinistryMember candidate : ordered) {
      lockAccount(candidate.getAccountUid());
      if (ownedCount(candidate.getAccountUid()) < MAX_OWNED) {
        return candidate;
      }
    }
    return null;
  }

  private void deleteMinistry(Ministry ministry) {
    dataCleaner.deleteAll(ministry.getId());
    memberRepository.deleteByMinistryId(ministry.getId());
    ministryRepository.delete(ministry);
    log.info("모임 삭제 - id:{}", ministry.getId());
  }

  // ---------------------------------------------------------------- 멤버 관리

  @Transactional
  public Detail changeRole(Long id, Long targetUid, MinistryRole role) {
    Long uid = currentUid();
    Ministry ministry = lockMinistry(id);
    MinistryMember me = requireActive(id, uid);
    if (me.getRole() != MinistryRole.OWNER) {
      throw new AccessDeniedException("모임 대표만 역할을 바꿀 수 있습니다.");
    }
    assertWritable(ministry);
    if (role == null || targetUid.equals(uid)) {
      throw new IllegalArgumentException("바꿀 수 없는 요청입니다.");
    }
    MinistryMember target = memberRepository.findByMinistryIdAndAccountUid(id, targetUid)
        .filter(MinistryMember::isActive)
        .orElseThrow(() -> new NoSuchElementException("멤버를 찾을 수 없습니다."));

    if (role == MinistryRole.OWNER) {
      lockAccount(targetUid);
      if (ownedCount(targetUid) >= MAX_OWNED) {
        throw new IllegalArgumentException(
            "선택한 멤버는 이미 대표로 있는 모임이 " + MAX_OWNED + "개라 넘겨받을 수 없습니다.");
      }
      // 기존 대표는 운영진으로 남는다. 대표 한 명 제약 때문에 먼저 내려서 반영한다
      me.changeRole(MinistryRole.MANAGER);
      memberRepository.saveAndFlush(me);
      target.changeRole(MinistryRole.OWNER);
    } else {
      target.changeRole(role);
    }
    memberRepository.save(target);
    return get(id);
  }

  @Transactional
  public Map<String, Object> removeMember(Long id, Long targetUid) {
    Long uid = currentUid();
    Ministry ministry = findMinistry(id);
    MinistryMember me = requireActive(id, uid);
    if (!me.getRole().canManage()) {
      throw new AccessDeniedException("멤버를 내보낼 권한이 없습니다.");
    }
    assertWritable(ministry);
    MinistryMember target = memberRepository.findByMinistryIdAndAccountUid(id, targetUid)
        .filter(MinistryMember::isActive)
        .orElseThrow(() -> new NoSuchElementException("멤버를 찾을 수 없습니다."));
    if (targetUid.equals(uid) || target.getRole() == MinistryRole.OWNER) {
      throw new IllegalArgumentException("내보낼 수 없는 멤버입니다.");
    }
    if (me.getRole() == MinistryRole.MANAGER && target.getRole() != MinistryRole.MEMBER) {
      throw new AccessDeniedException("운영진은 일반 멤버만 내보낼 수 있습니다.");
    }
    target.remove();
    memberRepository.save(target);
    return Map.of("accountUid", targetUid);
  }

  // ---------------------------------------------------------------- 초대·가입

  @Transactional
  public Invite getInvite(Long id) {
    requireManager(id);
    Ministry ministry = findMinistry(id);
    if (ministry.getInviteCode() == null || ministry.getInviteCode().isBlank()) {
      ministry.changeInviteCode(generateInviteCode());
    }
    return new Invite(ministry.getInviteCode(), ministry.isInviteAutoApprove());
  }

  @Transactional
  public Invite regenerateInvite(Long id) {
    requireManager(id);
    Ministry ministry = findWritableMinistry(id);
    ministry.changeInviteCode(generateInviteCode());
    return new Invite(ministry.getInviteCode(), ministry.isInviteAutoApprove());
  }

  @Transactional
  public Invite updateInviteSettings(Long id, boolean autoApprove) {
    requireManager(id);
    Ministry ministry = findWritableMinistry(id);
    ministry.changeInviteAutoApprove(autoApprove);
    return new Invite(ministry.getInviteCode(), ministry.isInviteAutoApprove());
  }

  /** 초대 코드로 보는 미리보기. 모임 이름과 멤버 수만 내려 주고 멤버 목록은 주지 않는다 */
  @Transactional(readOnly = true)
  public InviteInfo getInviteInfo(String code) {
    Ministry ministry = findInvitable(code);
    return new InviteInfo(ministry.getName(), ministry.getDescription(),
        memberRepository.countByMinistryIdAndStatus(ministry.getId(),
            MinistryMemberStatus.ACTIVE));
  }

  @Transactional
  public JoinResult join(String code) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long uid = account.getUid();
    lockAccount(uid);
    Ministry ministry = findInvitable(code);

    Optional<MinistryMember> existing = memberRepository
        .findByMinistryIdAndAccountUid(ministry.getId(), uid);
    if (existing.isPresent() && existing.get().isActive()) {
      return new JoinResult(ministry.getId(), ministry.getName(), "ALREADY_JOINED");
    }
    if (existing.isPresent() && existing.get().getStatus() == MinistryMemberStatus.PENDING) {
      return new JoinResult(ministry.getId(), ministry.getName(), "PENDING");
    }
    assertJoinRoom(uid);

    // 내보낸 이력이 있으면 자동 승인 모임이어도 대표·운영진의 승인을 거친다
    boolean wasRemoved = existing.isPresent();
    boolean immediate = ministry.isInviteAutoApprove() && !wasRemoved;
    if (existing.isPresent()) {
      existing.get().requestAgain();
      memberRepository.save(existing.get());
    } else {
      memberRepository.save(MinistryMember.builder()
          .ministryId(ministry.getId()).accountUid(uid).role(MinistryRole.MEMBER)
          .status(immediate ? MinistryMemberStatus.ACTIVE : MinistryMemberStatus.PENDING)
          .joinedAt(immediate ? LocalDateTime.now() : null).build());
    }
    if (immediate) {
      return new JoinResult(ministry.getId(), ministry.getName(), "JOINED");
    }
    notifyManagers(ministry, account.getName());
    return new JoinResult(ministry.getId(), ministry.getName(), "PENDING");
  }

  @Transactional(readOnly = true)
  public List<Request> getRequests(Long id) {
    requireManager(id);
    List<MinistryMember> pending = memberRepository
        .findByMinistryIdAndStatus(id, MinistryMemberStatus.PENDING);
    Map<Long, Account> accounts = accountsOf(pending);
    return pending.stream()
        .sorted(Comparator.comparing(MinistryMember::getInsertTime,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .map(m -> new Request(m.getAccountUid(), displayName(accounts.get(m.getAccountUid())),
            m.getInsertTime()))
        .toList();
  }

  @Transactional
  public Map<String, Object> decideRequest(Long id, Long targetUid, boolean approve) {
    requireManager(id);
    Ministry ministry = findWritableMinistry(id);
    lockAccount(targetUid);
    MinistryMember target = memberRepository.findByMinistryIdAndAccountUid(id, targetUid)
        .filter(m -> m.getStatus() == MinistryMemberStatus.PENDING)
        .orElseThrow(() -> new NoSuchElementException("가입 신청을 찾을 수 없습니다."));
    if (approve) {
      target.approve();
      memberRepository.save(target);
    } else {
      // 거절은 이력을 남기지 않는다 (내보낸 경우와 달리 다시 신청할 수 있다)
      memberRepository.delete(target);
    }
    accountRepository.findById(targetUid)
        .ifPresent(a -> pushNotifier.notifyJoinDecided(a, ministry, approve));
    return Map.of("accountUid", targetUid, "approved", approve);
  }

  private void notifyManagers(Ministry ministry, String requesterName) {
    List<MinistryMember> managers = memberRepository
        .findByMinistryIdAndStatus(ministry.getId(), MinistryMemberStatus.ACTIVE).stream()
        .filter(m -> m.getRole().canManage()).toList();
    accountsOf(managers).values()
        .forEach(a -> pushNotifier.notifyJoinRequested(a, ministry, requesterName));
  }

  // ---------------------------------------------------------------- 운영자(관리자 웹)

  @Transactional
  public void changeStatus(Long id, boolean suspend) {
    Ministry ministry = findMinistry(id);
    ministry.changeStatus(suspend
        ? MinistryStatus.SUSPENDED
        : MinistryStatus.ACTIVE);
  }

  // ---------------------------------------------------------------- 공용 검사

  /** 공지 등 다른 기능이 쓰는 권한 검사: 활성 멤버의 모임 내 역할 */
  @Transactional(readOnly = true)
  public Optional<MinistryRole> findRole(Long ministryId, Long accountUid) {
    return memberRepository.findByMinistryIdAndAccountUid(ministryId, accountUid)
        .filter(MinistryMember::isActive).map(MinistryMember::getRole);
  }

  private MinistryMember requireActive(Long ministryId, Long accountUid) {
    // 멤버가 아니면 모임이 있는지도 알 수 없도록 같은 응답을 준다
    return memberRepository.findByMinistryIdAndAccountUid(ministryId, accountUid)
        .filter(MinistryMember::isActive)
        .orElseThrow(() -> new NoSuchElementException(NOT_FOUND));
  }

  private MinistryMember requireManager(Long ministryId) {
    MinistryMember me = requireActive(ministryId, currentUid());
    if (!me.getRole().canManage()) {
      throw new AccessDeniedException("모임 대표 또는 운영진만 할 수 있습니다.");
    }
    return me;
  }

  private void assertWritable(Ministry ministry) {
    if (ministry.isSuspended()) {
      throw new IllegalStateException("정지된 모임입니다. 운영자에게 문의해주세요.");
    }
  }

  private Ministry findMinistry(Long id) {
    return ministryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException(NOT_FOUND));
  }

  private Ministry findWritableMinistry(Long id) {
    Ministry ministry = findMinistry(id);
    assertWritable(ministry);
    return ministry;
  }

  private Ministry lockMinistry(Long id) {
    return ministryRepository.findByIdForUpdate(id)
        .orElseThrow(() -> new NoSuchElementException(NOT_FOUND));
  }

  private Ministry findInvitable(String code) {
    String clean = code == null ? "" : code.trim();
    if (clean.isEmpty()) {
      throw new IllegalArgumentException("유효하지 않은 초대 링크입니다.");
    }
    Ministry ministry = ministryRepository.findByInviteCode(clean)
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 링크입니다."));
    if (ministry.isSuspended()) {
      throw new IllegalStateException("정지된 모임은 가입할 수 없습니다.");
    }
    return ministry;
  }

  private void assertJoinRoom(Long uid) {
    long joined = memberRepository.countByAccountUidAndStatusIn(uid,
        List.of(MinistryMemberStatus.ACTIVE, MinistryMemberStatus.PENDING));
    if (joined >= MAX_JOINED) {
      throw new IllegalArgumentException(
          "가입할 수 있는 모임은 최대 " + MAX_JOINED + "개입니다. (승인 대기 포함)");
    }
  }

  private long ownedCount(Long uid) {
    return memberRepository.countByAccountUidAndRole(uid, MinistryRole.OWNER);
  }

  private void lockAccount(Long uid) {
    accountRepository.findByUidForUpdate(uid)
        .orElseThrow(() -> new NoSuchElementException("계정을 찾을 수 없습니다."));
  }

  private boolean isLiveAccount(Long uid) {
    return accountRepository.findById(uid).map(a -> a.getLeaveYn() != Yn.Y).orElse(false);
  }

  private Map<Long, Account> accountsOf(List<MinistryMember> members) {
    return accountRepository.findAllById(
            members.stream().map(MinistryMember::getAccountUid).toList()).stream()
        .collect(Collectors.toMap(Account::getUid, Function.identity()));
  }

  private static String displayName(Account account) {
    if (account == null) {
      return "탈퇴한 사용자";
    }
    if (account.getNickname() != null && !account.getNickname().isBlank()) {
      return account.getNickname();
    }
    return account.getName() == null ? "이름 없음" : account.getName();
  }

  private static Long currentUid() {
    return AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow().getUid();
  }

  private static String normalizeName(String name) {
    String trimmed = name == null ? "" : name.trim();
    if (trimmed.isEmpty() || trimmed.length() > 30) {
      throw new IllegalArgumentException("모임 이름은 1~30자로 입력해 주세요.");
    }
    return trimmed;
  }

  private static String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    String trimmed = description.trim();
    if (trimmed.length() > 200) {
      throw new IllegalArgumentException("소개는 200자까지 입력할 수 있습니다.");
    }
    return trimmed;
  }

  private String generateInviteCode() {
    for (int attempt = 0; attempt < 10; attempt++) {
      StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
      for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
        sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
      }
      if (!ministryRepository.existsByInviteCode(sb.toString())) {
        return sb.toString();
      }
    }
    throw new IllegalStateException("초대 코드 생성에 실패했습니다. 다시 시도해주세요.");
  }
}
