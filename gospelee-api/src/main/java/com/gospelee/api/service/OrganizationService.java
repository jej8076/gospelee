package com.gospelee.api.service;

import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.repository.EcclesiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrganizationService {

  private final EcclesiaRepository ecclesiaRepository;

  /**
   * 조직 타입과 ID로 조직명을 조회합니다.
   * Entity가 있는 경우 DB에서 조회하고, 없는 경우 기본값을 반환합니다.
   */
  public String getOrganizationName(OrganizationType organizationType, Long organizationId) {
    if (!organizationType.isHasEntity()) {
      // Entity가 없는 경우 기본 표시명 반환
      return organizationType.getDisplayName();
    }

    // Entity가 있는 경우 타입별로 분기 처리
    switch (organizationType) {
      case ECCLESIA:
        return ecclesiaRepository.findById(organizationId)
            .map(ecclesia -> ecclesia.getName())
            .orElse(organizationType.getDisplayName());
      
      // 향후 다른 Entity가 있는 타입들 추가
      default:
        return organizationType.getDisplayName();
    }
  }
}
