package com.gospelee.api.service.push;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushgroup.PushGroupCandidateDTO;
import com.gospelee.api.dto.pushgroup.PushGroupRequestDTO;
import com.gospelee.api.dto.pushgroup.PushGroupResponseDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushGroup;
import com.gospelee.api.entity.PushGroupMember;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.announcement.AnnouncementAccessPolicy;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 푸시 알림 그룹 관리. 같은 교회의 담임목사/교역자/관리자만 만들고 고칠 수 있고,
 * 멤버는 같은 교회 성도 중에서만 고를 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushGroupService {

  static final int MAX_GROUPS_PER_ECCLESIA = 30;

  private final PushGroupRepository pushGroupRepository;
  private final PushGroupMemberRepository pushGroupMemberRepository;
  private final AccountService accountService;

  @Transactional(readOnly = true)
  public List<PushGroupResponseDTO> getList() {
    AccountAuthDTO account = requireManager();
    List<PushGroup> groups = pushGroupRepository.findByEcclesiaUidOrderByNameAsc(
        account.getEcclesiaUid());
    if (groups.isEmpty()) {
      return List.of();
    }
    Map<Long, List<Long>> membersByGroup = pushGroupMemberRepository
        .findByGroupIdIn(groups.stream().map(PushGroup::getId).toList()).stream()
        .collect(Collectors.groupingBy(PushGroupMember::getGroupId,
            Collectors.mapping(PushGroupMember::getAccountUid, Collectors.toList())));
    return groups.stream()
        .map(g -> toResponse(g, membersByGroup.getOrDefault(g.getId(), List.of())))
        .toList();
  }

  /** 그룹 멤버로 고를 수 있는 우리 교회 성도 목록 (이름순) */
  @Transactional(readOnly = true)
  public List<PushGroupCandidateDTO> getCandidates() {
    AccountAuthDTO account = requireManager();
    return churchMembers(account.getEcclesiaUid()).stream()
        .sorted(Comparator.comparing(a -> a.getName() == null ? "" : a.getName()))
        .map(a -> new PushGroupCandidateDTO(a.getUid(), a.getName(), a.getNickname(),
            a.getRole(), a.getPushToken() != null && !a.getPushToken().isEmpty()))
        .toList();
  }

  @Transactional
  public PushGroupResponseDTO create(PushGroupRequestDTO dto) {
    AccountAuthDTO account = requireManager();
    Long ecclesiaUid = account.getEcclesiaUid();
    String name = normalizeName(dto.getName());

    if (pushGroupRepository.countByEcclesiaUid(ecclesiaUid) >= MAX_GROUPS_PER_ECCLESIA) {
      throw new IllegalArgumentException(
          "푸시 그룹은 교회당 최대 " + MAX_GROUPS_PER_ECCLESIA + "개까지 만들 수 있습니다.");
    }
    if (pushGroupRepository.existsByEcclesiaUidAndName(ecclesiaUid, name)) {
      throw new IllegalArgumentException("같은 이름의 그룹이 이미 있습니다.");
    }
    Set<Long> memberUids = validMembers(ecclesiaUid, dto.getMemberAccountUids());

    PushGroup group = pushGroupRepository.save(PushGroup.builder()
        .ecclesiaUid(ecclesiaUid)
        .name(name)
        .description(normalizeDescription(dto.getDescription()))
        .build());
    saveMembers(group.getId(), memberUids);
    log.info("푸시 그룹 생성 - id:{}, ecclesiaUid:{}, 멤버:{}명", group.getId(), ecclesiaUid,
        memberUids.size());
    return toResponse(group, List.copyOf(memberUids));
  }

  @Transactional
  public PushGroupResponseDTO update(PushGroupRequestDTO dto) {
    AccountAuthDTO account = requireManager();
    Long ecclesiaUid = account.getEcclesiaUid();
    if (dto.getId() == null) {
      throw new IllegalArgumentException("수정할 그룹 id 가 필요합니다.");
    }
    PushGroup group = pushGroupRepository.findByIdAndEcclesiaUid(dto.getId(), ecclesiaUid)
        .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다."));
    String name = normalizeName(dto.getName());

    if (!group.getName().equals(name)
        && pushGroupRepository.existsByEcclesiaUidAndName(ecclesiaUid, name)) {
      throw new IllegalArgumentException("같은 이름의 그룹이 이미 있습니다.");
    }
    Set<Long> memberUids = validMembers(ecclesiaUid, dto.getMemberAccountUids());

    group.change(name, normalizeDescription(dto.getDescription()));
    // 멤버는 보낸 목록으로 통째로 바꾼다
    pushGroupMemberRepository.deleteByGroupId(group.getId());
    pushGroupMemberRepository.flush();
    saveMembers(group.getId(), memberUids);
    return toResponse(group, List.copyOf(memberUids));
  }

  @Transactional
  public void delete(Long id) {
    AccountAuthDTO account = requireManager();
    PushGroup group = pushGroupRepository.findByIdAndEcclesiaUid(id, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다."));
    pushGroupMemberRepository.deleteByGroupId(group.getId());
    pushGroupRepository.delete(group);
    log.info("푸시 그룹 삭제 - id:{}, accountUid:{}", id, account.getUid());
  }

  private AccountAuthDTO requireManager() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!AnnouncementAccessPolicy.canWriteEcclesia(account)) {
      throw new AccessDeniedException("푸시 알림 그룹을 관리할 권한이 없습니다.");
    }
    return account;
  }

  /** 탈퇴하지 않은 우리 교회 성도 */
  private List<Account> churchMembers(Long ecclesiaUid) {
    return accountService.getAccountByEcclesiaUid(ecclesiaUid).orElse(List.of()).stream()
        .filter(a -> a.getLeaveYn() != Yn.Y)
        .toList();
  }

  /** 멤버 목록이 모두 우리 교회 성도인지 확인하고 중복을 뺀 uid 집합을 돌려준다 */
  private Set<Long> validMembers(Long ecclesiaUid, List<Long> requested) {
    if (requested == null || requested.isEmpty()) {
      return Set.of();
    }
    Set<Long> allowed = churchMembers(ecclesiaUid).stream()
        .map(Account::getUid).collect(Collectors.toSet());
    Set<Long> unique = new LinkedHashSet<>(requested);
    if (!allowed.containsAll(unique)) {
      throw new IllegalArgumentException("우리 교회 성도가 아닌 계정이 포함되어 있습니다.");
    }
    return unique;
  }

  private void saveMembers(Long groupId, Set<Long> memberUids) {
    pushGroupMemberRepository.saveAll(memberUids.stream()
        .map(uid -> PushGroupMember.builder().groupId(groupId).accountUid(uid).build())
        .toList());
  }

  private static String normalizeName(String name) {
    String trimmed = name == null ? "" : name.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("그룹 이름을 입력해 주세요.");
    }
    return trimmed;
  }

  private static String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }

  private PushGroupResponseDTO toResponse(PushGroup group, List<Long> memberUids) {
    return PushGroupResponseDTO.builder()
        .id(group.getId())
        .name(group.getName())
        .description(group.getDescription())
        .memberAccountUids(memberUids)
        .insertTime(group.getInsertTime())
        .build();
  }
}
