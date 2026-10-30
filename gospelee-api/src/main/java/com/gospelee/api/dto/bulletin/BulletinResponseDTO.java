package com.gospelee.api.dto.bulletin;

import com.gospelee.api.entity.Bulletin;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** 주보 응답. 파일은 서버 경로 없이 id/이름/크기/종류만 내려준다 */
@Getter
public class BulletinResponseDTO {

  private final Long id;
  private final Long ecclesiaUid;
  private final LocalDate worshipDate;
  private final String title;
  private final String openYn;
  /** PDF 또는 IMAGE. 파일이 없으면 null */
  private final String fileKind;
  /** /file/{accessToken}/{fileDetailId} 로 파일을 조회한다 */
  private final String accessToken;
  private final List<BulletinFileDTO> fileList;
  private final LocalDateTime insertTime;
  private final LocalDateTime updateTime;

  @Builder
  public BulletinResponseDTO(Long id, Long ecclesiaUid, LocalDate worshipDate, String title,
      String openYn, String fileKind, String accessToken, List<BulletinFileDTO> fileList,
      LocalDateTime insertTime, LocalDateTime updateTime) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.worshipDate = worshipDate;
    this.title = title;
    this.openYn = openYn;
    this.fileKind = fileKind;
    this.accessToken = accessToken;
    this.fileList = fileList;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }

  public static BulletinResponseDTO of(Bulletin bulletin, String fileKind, String accessToken,
      List<BulletinFileDTO> fileList) {
    return BulletinResponseDTO.builder()
        .id(bulletin.getId())
        .ecclesiaUid(bulletin.getEcclesiaUid())
        .worshipDate(bulletin.getWorshipDate())
        .title(bulletin.getTitle())
        .openYn(bulletin.getOpenYn())
        .fileKind(fileKind)
        .accessToken(accessToken)
        .fileList(fileList)
        .insertTime(bulletin.getInsertTime())
        .updateTime(bulletin.getUpdateTime())
        .build();
  }
}
