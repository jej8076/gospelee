package com.gospelee.api.service.reaction;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.enums.ReactionTargetType;

/**
 * 반응 대상 종류별 규칙. 대상이 존재하는지, 이 사용자가 반응할 수 있는지를 확인한다.
 * 새 대상 종류를 지원하려면 {@link ReactionTargetType} 값을 추가하고 이 인터페이스의 구현체를
 * 빈으로 등록하면 된다.
 */
public interface ReactionTargetPolicy {

  ReactionTargetType targetType();

  /** 반응할 수 없으면 예외를 던진다 */
  void assertReactable(AccountAuthDTO account, Long targetId);
}
