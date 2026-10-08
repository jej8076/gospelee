package com.gospelee.api.service.announcement;

import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 교회 공지의 검증 기한과 첨부 사진 저장 용량을 확인하고 사용량을 반영한다.
 * 호출하는 쪽(공지 서비스)의 트랜잭션 안에서 사용해야 변경된 사용량이 함께 저장된다.
 */
@Component
@RequiredArgsConstructor
class AnnouncementStorageGuard {

  private final EcclesiaJpaRepository ecclesiaJpaRepository;

  /**
   * 검증 기한이 지난 미검증 교회는 공지를 작성/수정할 수 없다
   */
  void assertWritable(Long ecclesiaUid) {
    if (ecclesiaUid == null) {
      return;
    }
    ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid).ifPresent(ecclesia -> {
      if (ecclesia.isVerificationExpired()) {
        throw new EcclesiaException("교회 검증 기한이 지나 공지를 작성할 수 없습니다. 운영자 검증이 필요합니다.");
      }
    });
  }

  /**
   * 업로드할 사진 용량이 한도를 넘지 않는지 확인하고 사용량에 더한다
   */
  void reserve(Long ecclesiaUid, long incomingBytes) {
    if (ecclesiaUid == null || incomingBytes <= 0) {
      return;
    }
    ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid).ifPresent(ecclesia -> {
      if (!ecclesia.hasEnoughStorage(incomingBytes)) {
        throw new EcclesiaException("교회 저장 공간 용량(" + formatLimit(ecclesia)
            + ")을 초과하여 사진을 올릴 수 없습니다.");
      }
      ecclesia.addStorageUsedBytes(incomingBytes);
    });
  }

  /**
   * 삭제된 사진 용량을 사용량에서 뺀다
   */
  void release(Long ecclesiaUid, long freedBytes) {
    if (ecclesiaUid == null || freedBytes <= 0) {
      return;
    }
    ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid)
        .ifPresent(ecclesia -> ecclesia.subtractStorageUsedBytes(freedBytes));
  }

  private String formatLimit(Ecclesia ecclesia) {
    long mb = ecclesia.getStorageLimitBytesOrDefault() / (1024 * 1024);
    return mb >= 1024 ? (mb / 1024) + "GB" : mb + "MB";
  }
}
