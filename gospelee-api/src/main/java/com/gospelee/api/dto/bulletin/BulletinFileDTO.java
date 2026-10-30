package com.gospelee.api.dto.bulletin;

import lombok.Getter;

/** 주보 첨부 파일 한 개 (서버 저장 경로는 노출하지 않는다) */
@Getter
public class BulletinFileDTO {

  private final Long fileDetailId;
  private final String fileOriginalName;
  private final Long fileSize;
  private final String extension;

  public BulletinFileDTO(Long fileDetailId, String fileOriginalName, Long fileSize,
      String extension) {
    this.fileDetailId = fileDetailId;
    this.fileOriginalName = fileOriginalName;
    this.fileSize = fileSize;
    this.extension = extension;
  }
}
