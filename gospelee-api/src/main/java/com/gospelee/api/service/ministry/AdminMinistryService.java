package com.gospelee.api.service.ministry;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.MinistryStatus;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 운영자(ADMIN)용 모임 목록과 정지·해제. 모임 내용(공지 본문 등)은 내려 주지 않는다 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMinistryService {

  private final MinistryRepository ministryRepository;
  private final MinistryMemberRepository memberRepository;
  private final AccountRepository accountRepository;

  public record Row(Long id, String name, String status, long memberCount, String ownerName,
                    long storageUsedBytes, long storageLimitBytes, LocalDateTime createdAt) {

  }

  /** 이름이나 대표 이름에 keyword 가 들어 있는 모임, 최근 만든 순 */
  @Transactional(readOnly = true)
  public List<Row> list(String keyword) {
    Map<Long, Long> counts = new HashMap<>();
    for (Object[] row : memberRepository.countActiveByMinistry()) {
      counts.put((Long) row[0], (Long) row[1]);
    }
    Map<Long, MinistryMember> owners = memberRepository
        .findByRoleAndStatus(MinistryRole.OWNER, MinistryMemberStatus.ACTIVE).stream()
        .collect(Collectors.toMap(MinistryMember::getMinistryId, Function.identity(),
            (a, b) -> a));
    Map<Long, Account> accounts = accountRepository
        .findAllById(owners.values().stream().map(MinistryMember::getAccountUid).toList())
        .stream().collect(Collectors.toMap(Account::getUid, Function.identity()));

    String needle = keyword == null ? "" : keyword.trim().toLowerCase();
    return ministryRepository.findAll().stream()
        .map(m -> {
          MinistryMember owner = owners.get(m.getId());
          Account ownerAccount = owner == null ? null : accounts.get(owner.getAccountUid());
          return new Row(m.getId(), m.getName(), m.getStatus().name(),
              counts.getOrDefault(m.getId(), 0L), ownerName(ownerAccount),
              m.getStorageUsedBytes(), m.getStorageLimitBytes(), m.getInsertTime());
        })
        .filter(r -> needle.isEmpty() || r.name().toLowerCase().contains(needle)
            || r.ownerName().toLowerCase().contains(needle))
        .sorted(Comparator.comparing(Row::createdAt,
            Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
  }

  @Transactional
  public Row changeStatus(Long id, boolean suspend, Long adminUid) {
    Ministry ministry = ministryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("모임을 찾을 수 없습니다."));
    ministry.changeStatus(suspend ? MinistryStatus.SUSPENDED
        : MinistryStatus.ACTIVE);
    log.info("모임 {} - id:{}, adminUid:{}", suspend ? "정지" : "정지 해제", id, adminUid);
    return list(null).stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
  }

  private static String ownerName(Account account) {
    if (account == null) {
      return "";
    }
    if (account.getNickname() != null && !account.getNickname().isBlank()) {
      return account.getNickname();
    }
    return account.getName() == null ? "" : account.getName();
  }
}
