package com.gospelee.api.service.ministry;

import org.springframework.stereotype.Component;

/**
 * 모임이 삭제될 때 모임에 딸린 콘텐츠를 지운다. 콘텐츠 종류가 늘어날 때(공지 → 갤러리 → 기도 쪽지)
 * 이곳에 삭제를 추가한다.
 */
@Component
public class MinistryDataCleaner {

  public void deleteAll(Long ministryId) {
    // 1a 의 공지 삭제는 공지 연동 단계에서 채운다
  }
}
