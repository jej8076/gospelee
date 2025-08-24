package com.gospelee.api.service.announcement;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.enums.AppType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
public class AnnouncementServiceDispatcher implements AnnouncementService {

  private static final String HEADER_KEY = "X-App-Identifier";
  private static final String ADMIN = "AnnouncementAdmin";
  private static final String CLIENT = "AnnouncementClient";

  private final Map<String, AnnouncementService> strategyMap;
  private final HttpServletRequest request;

  public AnnouncementServiceDispatcher(
      Map<String, AnnouncementService> strategyMap, // 키 = 빈 이름
      HttpServletRequest request
  ) {
    // 복사해 불변 맵으로 보관(선택)
    this.strategyMap = new HashMap<>(strategyMap);
    this.request = request;
  }

  private AnnouncementService resolve() {

    // TODO request를 사용해 header를 가져와서 enum과 비교하는 것까지 수행하는 메서드 만들어 재사용할 것
    String headerValue = request.getHeader(HEADER_KEY);
    String beanName = (headerValue != null && !headerValue.isBlank() && AppType.OOG_WEB.name()
        .equals(headerValue)) ? ADMIN : CLIENT;

    AnnouncementService svc = strategyMap.get(beanName);
    if (svc == null) {
      throw new IllegalArgumentException("지원되지 않는 provider 헤더 값: " + headerValue);
    }
    return svc;
  }

  @Override
  public List<AnnouncementResponseDTO> getAnnouncementList(HttpServletRequest req, String type) {
    return resolve().getAnnouncementList(req, type);
  }

  @Override
  public AnnouncementDTO getAnnouncement(String type, Long id) {
    return resolve().getAnnouncement(type, id);
  }

  @Override
  public AnnouncementDTO insertAnnouncement(List<MultipartFile> files, AnnouncementDTO dto) {
    return resolve().insertAnnouncement(files, dto);
  }

  @Override
  public AnnouncementDTO updateAnnouncement(List<MultipartFile> files, AnnouncementDTO dto) {
    return resolve().updateAnnouncement(files, dto);
  }
}