package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.repository.EcclesiaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private EcclesiaRepository ecclesiaRepository;

  @InjectMocks
  private OrganizationService organizationService;

  @Test
  @DisplayName("Entity가 없는 조직 타입의 경우 기본 표시명을 반환한다")
  void getOrganizationName_WithoutEntity_ReturnsDisplayName() {
    // given
    OrganizationType organizationType = OrganizationType.MINISTRY;
    Long organizationId = 1L;

    // when
    String result = organizationService.getOrganizationName(organizationType, organizationId);

    // then
    assertEquals("사역단체", result);
    verifyNoInteractions(ecclesiaRepository);
  }

  @Test
  @DisplayName("Entity가 있는 조직 타입의 경우 DB에서 조회한 이름을 반환한다")
  void getOrganizationName_WithEntity_ReturnsEntityName() {
    // given
    OrganizationType organizationType = OrganizationType.ECCLESIA;
    Long organizationId = 1L;
    Ecclesia ecclesia = Ecclesia.builder()
        .name("테스트교회")
        .build();

    when(ecclesiaRepository.findById(organizationId)).thenReturn(Optional.of(ecclesia));

    // when
    String result = organizationService.getOrganizationName(organizationType, organizationId);

    // then
    assertEquals("테스트교회", result);
    verify(ecclesiaRepository).findById(organizationId);
  }

  @Test
  @DisplayName("Entity가 있지만 DB에서 찾을 수 없는 경우 기본 표시명을 반환한다")
  void getOrganizationName_EntityNotFound_ReturnsDisplayName() {
    // given
    OrganizationType organizationType = OrganizationType.ECCLESIA;
    Long organizationId = 999L;

    when(ecclesiaRepository.findById(organizationId)).thenReturn(Optional.empty());

    // when
    String result = organizationService.getOrganizationName(organizationType, organizationId);

    // then
    assertEquals("교회", result);
    verify(ecclesiaRepository).findById(organizationId);
  }
}
