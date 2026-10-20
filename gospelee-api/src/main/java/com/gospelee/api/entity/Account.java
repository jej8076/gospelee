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

  // 공유/댓글 등 다른 사용자에게 노출되는 이름. 비어 있으면 입력을 유도한다
  @Column(length = 20)
  private String nickname;

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
  public Account(long uid, String name, String nickname, Long ecclesiaUid, String rrn, String phone, String email,
      RoleType role, String idToken, String pushToken, Yn leaveYn) {
    this.uid = uid;
    this.name = name;
    this.nickname = nickname;
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

  /**
   * 회원 탈퇴: 개인정보를 모두 비우고 탈퇴 처리한다.
   * uid 만 남겨 두어, 작성한 나눔 묵상/댓글이 "탈퇴한 사용자"로 익명 표시되게 한다.
   */
  public void withdraw() {
    this.name = null;
    this.nickname = null;
    this.ecclesiaUid = null;
    this.rrn = null;
    this.phone = null;
    this.email = null;
    this.idToken = null;
    this.pushToken = null;
    this.role = RoleType.LAYMAN;
    this.leaveYn = Yn.Y;
  }

}
