package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.EcclesiaStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Ecclesia extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  @Column
  private String name;

  @Column
  private String status;

  @Column(name = "master_account_uid")
  private Long masterAccountUid;

  @Column(name = "church_identification_number")
  private String churchIdentificationNumber;

  @Column(name = "telephone")
  private String telephone;

  @Builder
  public Ecclesia(long uid, String name, String status, Long masterAccountUid,
      String churchIdentificationNumber, String telephone) {
    this.uid = uid;
    this.name = name;
    this.status = status;
    this.masterAccountUid = masterAccountUid;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.telephone = telephone;
  }

  public void changeStatus(EcclesiaStatusType status) {
    this.status = status.getName();
  }
}
