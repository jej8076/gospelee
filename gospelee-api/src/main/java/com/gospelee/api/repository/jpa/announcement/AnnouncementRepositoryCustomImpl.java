package com.gospelee.api.repository.jpa.announcement;

import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.dto.announcement.projection.AnnouncementProjections;
import com.gospelee.api.entity.QAnnouncement;
import com.gospelee.api.entity.QEcclesia;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.Yn;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AnnouncementRepositoryCustomImpl implements AnnouncementRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<AnnouncementResponseDTO> findByOrganizationTypeAndOrganizationIdAndOpenY(
      String organizationType, Long organizationId, boolean isOpen) {

    OrganizationType type = OrganizationType.fromName(organizationType);

    JPAQuery<AnnouncementResponseDTO> query = queryFactory
        .select(AnnouncementProjections.toColumn(getOrganizationNameExpression(type)))
        .from(QAnnouncement.announcement);

    // Entity가 있는 경우에만 JOIN 수행
    if (type.isHasEntity()) {
      addJoinForEntityType(query, type);
    }

    return query
        .where(isOpen
            ? buildWhereConditionWithOpen(organizationId, type.name())
            : buildWhereCondition(organizationId, type.name()))
        .orderBy(QAnnouncement.announcement.insertTime.desc())
        .fetch();
  }

  /**
   * 조직 타입에 따른 조직명 표현식을 반환합니다.
   */
  private StringExpression getOrganizationNameExpression(OrganizationType type) {
    if (!type.isHasEntity()) {
      // Entity가 없는 경우 고정 문자열 반환
      return Expressions.stringTemplate("{0}", type.getDisplayName());
    }

    // Entity가 있는 경우 타입별 분기
    switch (type) {
      case ECCLESIA:
        return QEcclesia.ecclesia.name;
      default:
        return Expressions.stringTemplate("{0}", type.getDisplayName());
    }
  }

  /**
   * Entity가 있는 조직 타입에 대해 JOIN을 추가합니다.
   */
  private void addJoinForEntityType(JPAQuery<AnnouncementResponseDTO> query,
      OrganizationType type) {
    switch (type) {
      case ECCLESIA:
        query.leftJoin(QEcclesia.ecclesia)
            .on(QAnnouncement.announcement.organizationId.eq(QEcclesia.ecclesia.uid));
        break;
      // 향후 다른 Entity 타입들 추가
      default:
        // Entity가 있다고 표시되었지만 처리되지 않은 경우
        break;
    }
  }

  /**
   * 공통 WHERE 조건을 생성합니다.
   */
  private BooleanExpression buildWhereCondition(Long organizationId, String organizationType) {
    return QAnnouncement.announcement.organizationId.eq(organizationId)
        .and(QAnnouncement.announcement.organizationType.eq(organizationType));
  }

  private BooleanExpression buildWhereConditionWithOpen(Long organizationId,
      String organizationType) {
    return QAnnouncement.announcement.organizationId.eq(organizationId)
        .and(QAnnouncement.announcement.organizationType.eq(organizationType)
            .and(QAnnouncement.announcement.openYn.eq(Yn.Y.name())));
  }
}
