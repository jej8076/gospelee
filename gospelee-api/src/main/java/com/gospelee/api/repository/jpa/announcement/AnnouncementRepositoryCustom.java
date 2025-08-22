package com.gospelee.api.repository.jpa.announcement;

import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.enums.Yn;
import java.util.List;

public interface AnnouncementRepositoryCustom {

  List<AnnouncementResponseDTO> findByOrganizationTypeAndOrganizationIdAndOpenY(
      String organizationType,
      Long organizationId,
      boolean isOpen);

}
