package com.gospelee.api.service.prayer;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기도 쪽지를 교회가 아닌 모임 범위로 쓰게 해 주는 변환기.
 *
 * <p>기도 쪽지 서비스는 모두 로그인 계정의 {@code ecclesiaUid} 로 범위를 정한다. 모임은 같은 테이블을
 * 쓰되 {@code ecclesia_uid} 자리에 {@link #ministryKey(Long)} (모임 id 의 음수) 를 넣어 교회와 겹치지
 * 않게 한다. 이 클래스가 모임 멤버·관리자인지 확인한 뒤, 범위 키를 담은 계정 사본을 돌려준다.
 * 교회 id 는 항상 양수라 키가 충돌하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class PrayerScopeResolver {

  private final MinistryRepository ministryRepository;
  private final MinistryMemberRepository memberRepository;

  /** 모임의 기도 쪽지 범위 키. 교회 id(양수)와 겹치지 않도록 음수로 쓴다 */
  public static Long ministryKey(Long ministryId) {
    return -ministryId;
  }

  /** 범위 키가 모임이면 모임 id, 교회면 null */
  public static Long ministryIdOf(Long scopeKey) {
    return scopeKey != null && scopeKey < 0 ? -scopeKey : null;
  }

  /** 성도용. ministryId 가 없으면 계정 그대로(교회), 있으면 활성 멤버만 그 모임 범위로 바꾼다 */
  @Transactional(readOnly = true)
  public AccountAuthDTO member(AccountAuthDTO account, Long ministryId) {
    if (ministryId == null) {
      return account;
    }
    requireMember(account, ministryId);
    return scoped(account, ministryId, account.getRole());
  }

  /** 관리자용. 모임 대표·운영진만 그 모임 범위로 바꾼다 */
  @Transactional(readOnly = true)
  public AccountAuthDTO manager(AccountAuthDTO account, Long ministryId) {
    if (ministryId == null) {
      return account;
    }
    MinistryMember me = requireMember(account, ministryId);
    if (!me.getRole().canManage()) {
      throw new AccessDeniedException("모임 대표 또는 운영진만 관리할 수 있습니다.");
    }
    // 서비스의 관리자 확인(교회 직분)을 통과하도록 관리 역할을 담는다. 모임 권한은 위에서 확인했다
    return scoped(account, ministryId, RoleType.ADMIN);
  }

  private MinistryMember requireMember(AccountAuthDTO account, Long ministryId) {
    // 활성 멤버가 아니면 모임이 있는지도 알 수 없게 같은 응답을 준다
    MinistryMember me = memberRepository.findByMinistryIdAndAccountUid(ministryId, account.getUid())
        .filter(MinistryMember::isActive)
        .orElseThrow(() -> new AccessDeniedException("모임 멤버만 이용할 수 있습니다."));
    Ministry ministry = ministryRepository.findById(ministryId)
        .orElseThrow(() -> new AccessDeniedException("모임 멤버만 이용할 수 있습니다."));
    if (ministry.isSuspended()) {
      throw new AccessDeniedException("정지된 모임에서는 기도 쪽지를 이용할 수 없습니다.");
    }
    return me;
  }

  private static AccountAuthDTO scoped(AccountAuthDTO account, Long ministryId, RoleType role) {
    return AccountAuthDTO.builder()
        .uid(account.getUid())
        .name(account.getName())
        .nickname(account.getNickname())
        .ecclesiaUid(ministryKey(ministryId))
        .rrn(account.getRrn())
        .phone(account.getPhone())
        .email(account.getEmail())
        .role(role)
        .idToken(account.getIdToken())
        .accessToken(account.getAccessToken())
        .refreshToken(account.getRefreshToken())
        .pushToken(account.getPushToken())
        .ecclesiaStatus(EcclesiaStatusType.APPROVAL.getName())
        .socialLoginPlatform(account.getSocialLoginPlatform())
        .leaveYn(account.getLeaveYn())
        .build();
  }
}
