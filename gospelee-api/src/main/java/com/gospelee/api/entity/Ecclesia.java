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

  @Column(name = "senior_paster_name")
  private String seniorPastorName;

  @Column(name = "church_address")
  private String churchAddress;

  @Column(name = "storage_limit_bytes")
  private Long storageLimitBytes;

  @Column(name = "storage_used_bytes")
  private Long storageUsedBytes;

  // 초대 링크용 코드
  @Column(name = "invite_code", length = 32, unique = true)
  private String inviteCode;

  // 초대 링크로 가입 시 관리자 승인 없이 바로 가입 여부 (기본: 승인 필요)
  @Column(name = "invite_auto_approve")
  private Boolean inviteAutoApprove;

  @Builder
  public Ecclesia(long uid, String name, String status, Long masterAccountUid,
      String churchIdentificationNumber, String telephone, String seniorPastorName,
      String churchAddress, Long storageLimitBytes, Long storageUsedBytes) {
    this.uid = uid;
    this.name = name;
    this.status = status;
    this.masterAccountUid = masterAccountUid;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.telephone = telephone;
    this.seniorPastorName = seniorPastorName;
    this.churchAddress = churchAddress;
    this.storageLimitBytes = storageLimitBytes != null ? storageLimitBytes : 10737418240L;
    this.storageUsedBytes = storageUsedBytes != null ? storageUsedBytes : 0L;
  }

  public void changeInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public void changeInviteAutoApprove(boolean autoApprove) {
    this.inviteAutoApprove = autoApprove;
  }

  public boolean isInviteAutoApprove() {
    return Boolean.TRUE.equals(this.inviteAutoApprove);
  }

  public void changeStatus(EcclesiaStatusType status) {
    this.status = status.getName();
  }

  public void changeSeniorPastorName(String name) {
    this.seniorPastorName = name;
  }

  public void changeChurchAddress(String address) {
    this.churchAddress = address;
  }

  public long getStorageLimitBytesOrDefault() {
    return this.storageLimitBytes != null ? this.storageLimitBytes : 10737418240L;
  }

  public long getStorageUsedBytesOrDefault() {
    return this.storageUsedBytes != null ? this.storageUsedBytes : 0L;
  }

  public boolean hasEnoughStorage(long incomingBytes) {
    return getStorageUsedBytesOrDefault() + incomingBytes <= getStorageLimitBytesOrDefault();
  }

  public void addStorageUsedBytes(long bytes) {
    this.storageUsedBytes = getStorageUsedBytesOrDefault() + bytes;
  }

  public void subtractStorageUsedBytes(long bytes) {
    long current = getStorageUsedBytesOrDefault();
    this.storageUsedBytes = Math.max(0L, current - bytes);
  }

  public void changeStorageLimitBytes(long bytes) {
    this.storageLimitBytes = bytes;
  }
}
