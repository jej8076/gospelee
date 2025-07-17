package com.gospelee.api.repository.jpa.announcement;

import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import java.util.List;

public interface AnnouncementRepositoryCustom {

  List<AnnouncementResponseDTO> findByOrganizationTypeAndOrganizationId(String organizationType,
      Long organizationId);
}
