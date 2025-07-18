package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  @Column
  private String name;

  @Column
  private Long ecclesiaUid;

  @Column
  private String rrn;

  @Column
  private String phone;

  @Column
  private String email;

  @Column
  @Enumerated(EnumType.STRING)
  private RoleType role;

  @Column(length = 1000)
  private String id_token;

  @Column(length = 1000)
  private String pushToken;

  @Builder
  public Account(long uid, String name, Long ecclesiaUid, String rrn, String phone, String email,
      RoleType role, String id_token, String pushToken) {
    this.uid = uid;
    this.name = name;
    this.ecclesiaUid = ecclesiaUid;
    this.rrn = rrn;
    this.phone = phone;
    this.email = email;
    this.role = role;
    this.id_token = id_token;
    this.pushToken = pushToken;
  }

  public void changeRole(RoleType role) {
    this.role = role;
  }

  public void changeEcclesiaUid(long ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
  }


}
