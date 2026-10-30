package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.Yn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 교회 주보. 교회마다 예배 날짜(주일) 하나에 주보 한 건을 둔다.
 * 첨부 파일(PDF 한 개 또는 이미지 여러 장)은 fileUid 로 연결한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "bulletin", uniqueConstraints = @UniqueConstraint(
    name = "uk_bulletin_ecclesia_date", columnNames = {"ecclesia_uid", "worship_date"}))
public class Bulletin extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "worship_date", nullable = false)
  private LocalDate worshipDate;

  @Column(name = "title")
  private String title;

  @Column(name = "file_uid")
  private Long fileUid;

  @Column(name = "open_yn", nullable = false)
  private String openYn;

  @Builder
  public Bulletin(Long id, Long ecclesiaUid, LocalDate worshipDate, String title, Long fileUid,
      String openYn) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.worshipDate = worshipDate;
    this.title = title;
    this.fileUid = fileUid;
    this.openYn = openYn;
  }

  @PrePersist
  public void prePersist() {
    if (this.openYn == null) {
      this.openYn = Yn.Y.name();
    }
  }

  public void changeWorshipDate(LocalDate worshipDate) {
    this.worshipDate = worshipDate;
  }

  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeFileUid(Long fileUid) {
    this.fileUid = fileUid;
  }

  public void changeOpenYn(Yn openYn) {
    this.openYn = openYn.name();
  }
}
