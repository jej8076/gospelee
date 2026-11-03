package com.gospelee.api.service.push;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushdirect.PushDirectRequestDTO;
import com.gospelee.api.dto.pushdirect.PushDirectResponseDTO;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.service.announcement.AnnouncementAccessPolicy;
import com.gospelee.api.service.push.PushDispatchService.PushRequest;
import com.gospelee.api.service.push.PushDispatchService.SendResult;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지·주보를 등록하지 않고 교회 성도(전체 또는 선택한 그룹)에게 직접 푸시를 보낸다.
 * 같은 교회의 담임목사/교역자/관리자만 보낼 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDirectService {

  private final PushDispatchService pushDispatchService;

  @Transactional
  public PushDirectResponseDTO send(PushDirectRequestDTO dto) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!AnnouncementAccessPolicy.canWriteEcclesia(account)) {
      throw new AccessDeniedException("푸시 알림을 보낼 권한이 없습니다.");
    }
    String title = oneLine(dto.getTitle());
    String message = dto.getMessage() == null ? "" : dto.getMessage().trim();
    if (title.isEmpty() || message.isEmpty()) {
      throw new IllegalArgumentException("제목과 내용을 입력해 주세요.");
    }

    SendResult result = pushDispatchService.dispatch(PushRequest.builder()
        .sendAccountUid(account.getUid())
        .ecclesiaUid(account.getEcclesiaUid())
        .groupIds(dto.getGroupIds())
        .organization(OrganizationType.ECCLESIA.name())
        .category(CategoryType.DIRECT.name())
        .title(title)
        .message(message)
        .data(Map.of())
        .build());
    if (result == null) {
      throw new IllegalArgumentException("알림을 받을 성도가 없습니다.");
    }
    log.info("직접 푸시 - pushId:{}, accountUid:{}, 대상:{}명, 전달:{}명, 실패:{}명, 토큰없음:{}명",
        result.pushId(), account.getUid(), result.targetCount(), result.sentCount(),
        result.failedCount(), result.noTokenCount());
    return PushDirectResponseDTO.builder()
        .pushId(result.pushId())
        .targetCount(result.targetCount())
        .sentCount(result.sentCount())
        .failedCount(result.failedCount())
        .noTokenCount(result.noTokenCount())
        .build();
  }

  /** 제목은 한 줄로 만든다 */
  private static String oneLine(String value) {
    return value == null ? "" : value.replaceAll("\\s+", " ").trim();
  }
}
