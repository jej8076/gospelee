package com.gospelee.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.EcclesiaStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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

  // 초대 링크용 코드 (다른 사용자에게 노출되지 않도록 JSON 응답에서 제외)
  @JsonIgnore
  @Column(name = "invite_code", length = 32, unique = true)
  private String inviteCode;

  // 초대 링크로 가입 시 관리자 승인 없이 바로 가입 여부 (기본: 승인 필요)
  @JsonIgnore
  @Column(name = "invite_auto_approve")
  private Boolean inviteAutoApprove;

  // 운영자 검증 여부 (Y: 검증 완료). 검증 전에는 성도 수, 저장 용량, 검색 노출이 제한된다.
  @Column(name = "verified_yn", length = 1)
  private String verifiedYn;

  // 검증 전(미검증) 교회 제한
  public static final int UNVERIFIED_MAX_MEMBERS = 30;
  public static final long UNVERIFIED_STORAGE_LIMIT_BYTES = 100L * 1024 * 1024;
  public static final int VERIFICATION_DEADLINE_DAYS = 14;

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
    this.verifiedYn = "N";
  }

  public boolean isVerified() {
    return "Y".equals(this.verifiedYn);
  }

  public void changeVerified(boolean verified) {
    this.verifiedYn = verified ? "Y" : "N";
  }

  /**
   * 검증 기한(신청일 + 14일). 검증 완료된 교회도 값은 계산되지만 제한에는 쓰이지 않는다.
   */
  public LocalDateTime getVerificationDeadline() {
    LocalDateTime insertTime = getInsertTime();
    return insertTime == null ? null : insertTime.plusDays(VERIFICATION_DEADLINE_DAYS);
  }

  public boolean isVerificationExpired() {
    return isVerificationExpiredAt(LocalDateTime.now());
  }

  public boolean isVerificationExpiredAt(LocalDateTime now) {
    LocalDateTime deadline = getVerificationDeadline();
    return !isVerified() && deadline != null && now.isAfter(deadline);
  }

  /**
   * 새 성도를 받을 수 있는지 (검증 전에는 {@link #UNVERIFIED_MAX_MEMBERS}명까지)
   */
  public boolean hasRoomForMember(long currentMemberCount) {
    return isVerified() || currentMemberCount < UNVERIFIED_MAX_MEMBERS;
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
    long limit = this.storageLimitBytes != null ? this.storageLimitBytes : 10737418240L;
    // 검증 전 교회는 설정된 한도와 상관없이 더 작은 값으로 제한
    return isVerified() ? limit : Math.min(limit, UNVERIFIED_STORAGE_LIMIT_BYTES);
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
