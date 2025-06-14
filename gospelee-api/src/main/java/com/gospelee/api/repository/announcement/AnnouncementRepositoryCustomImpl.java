package com.gospelee.api.repository.announcement;

import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.dto.announcement.projection.AnnouncementProjections;
import com.gospelee.api.entity.QAnnouncement;
import com.gospelee.api.enums.OrganizationType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AnnouncementRepositoryCustomImpl implements AnnouncementRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<AnnouncementResponseDTO> findByOrganizationTypeAndOrganizationId(
      String organizationType, String organizationId) {

    OrganizationType type = OrganizationType.fromName(organizationType);

    return queryFactory
        .select(AnnouncementProjections.toColumn(type.getNameField()))
        .from(QAnnouncement.announcement)
        .join(type.getEntity())
        .on(QAnnouncement.announcement.organizationId.eq(type.getIdField()))
        .fetch();
  }
}
