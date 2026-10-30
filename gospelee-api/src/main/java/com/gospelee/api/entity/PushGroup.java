package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 푸시 알림 그룹. 교회 관리자가 성도를 직접 골라 묶어 두고, 공지·주보를 등록할 때 그룹에게만 알림을 보낸다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "push_group", uniqueConstraints = @UniqueConstraint(
    name = "uk_push_group_ecclesia_name", columnNames = {"ecclesia_uid", "name"}))
public class PushGroup extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Column(name = "description", length = 100)
  private String description;

  @Builder
  public PushGroup(Long id, Long ecclesiaUid, String name, String description) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.name = name;
    this.description = description;
  }

  public void change(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
