package com.gospelee.api.dto.announcement.projection;

import com.gospelee.api.dto.announcement.QAnnouncementResponseDTO;
import com.gospelee.api.entity.QAnnouncement;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;

public class AnnouncementProjections {

  private static final QAnnouncement announcement = QAnnouncement.announcement;

  public static final Expression<String> pushNotificationSendYnExpression =
      new CaseBuilder()
          .when(announcement.pushNotificationIds.isNotNull())
          .then("Y")
          .otherwise("N");

  // DTO 생성용 프로젝션 (StringExpression으로 변경하여 더 유연하게)
  public static QAnnouncementResponseDTO toColumn(StringExpression orgName) {
    return new QAnnouncementResponseDTO(
        announcement.id,
        announcement.organizationType,
        announcement.organizationId,
        orgName,
        announcement.subject,
        announcement.text,
        announcement.fileUid,
        pushNotificationSendYnExpression,
        announcement.pushNotificationIds,
        announcement.insertTime,
        announcement.updateTime
    );
  }
}