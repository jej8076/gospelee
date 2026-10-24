package com.gospelee.api.enums;

/**
 * 반응을 남길 수 있는 대상 종류. 새 화면에서 반응을 쓰려면 값을 추가하고
 * {@link com.gospelee.api.service.reaction.ReactionTargetPolicy} 구현체를 하나 등록한다.
 */
public enum ReactionTargetType {
  /** 교회 공지사항 */
  ANNOUNCEMENT,
}
