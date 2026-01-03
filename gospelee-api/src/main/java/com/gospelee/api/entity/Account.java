package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.Yn;
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
  private String idToken;

  @Column(length = 1000)
  private String pushToken;

  @Column
  @Enumerated(EnumType.STRING)
  private Yn leaveYn;

  @Builder
  public Account(long uid, String name, Long ecclesiaUid, String rrn, String phone, String email,
      RoleType role, String idToken, String pushToken, Yn leaveYn) {
    this.uid = uid;
    this.name = name;
    this.ecclesiaUid = ecclesiaUid;
    this.rrn = rrn;
    this.phone = phone;
    this.email = email;
    this.role = role;
    this.idToken = idToken;
    this.pushToken = pushToken;
    this.leaveYn = leaveYn;
  }

  public void changeRole(RoleType role) {
    this.role = role;
  }

  public void changeEcclesiaUid(long ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
  }

  public void changeLeaveYn(Yn yn) {
    this.leaveYn = yn;
  }

}
