package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.EcclesiaStatusType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Columns;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "ANNOUNCEMENT_GENERATOR",
    sequenceName = "ANNOUNCEMENT_SEQ01",
    allocationSize = 1
)
@ToString
public class Announcement extends EditInfomation {

  @Id
  @GeneratedValue(
      strategy = GenerationType.AUTO,
      generator = "ANNOUNCEMENT_GENERATOR"
  )
  private Long id;

  @Column
  private String subject;

  @Column
  private String text;

  @Column
  private Long fileUid;

  @Column(name = "push_notification_ids")
  private String pushNotificationIds;
}
