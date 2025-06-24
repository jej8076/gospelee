package com.gospelee.api.controller;

import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.repository.FileDetailsRepository;
import com.gospelee.api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/file")
public class FileController {

  private final FileService fileService;
  private final FileDetailsRepository fileDetailsRepository;

  /**
   * 파일 조회 API (기존 - 보안 취약)
   * file 테이블의 ID와 file_details 테이블의 ID를 조합하여 파일을 조회합니다.
   *
   * @param fileId file 테이블의 ID
   * @param fileDetailId file_details 테이블의 ID
   * @return 파일 리소스
   */
  @GetMapping("/{fileId}/{fileDetailId}")
  public ResponseEntity<Resource> getFile(
      @PathVariable Long fileId,
      @PathVariable Long fileDetailId) {

    try {
      Resource resource = fileService.getFile(fileId, fileDetailId);

      // 파일 상세 정보 조회하여 원본 파일명과 Content-Type 설정
      FileDetails fileDetails = fileDetailsRepository.findById(fileDetailId)
          .orElseThrow(() -> new IllegalArgumentException("파일 상세 정보를 찾을 수 없습니다."));

      // Content-Type 설정 (저장된 fileType 사용, 없으면 기본값)
      String contentType = fileDetails.getFileType();
      if (contentType == null || contentType.isEmpty()) {
        contentType = "application/octet-stream";
      }

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "inline; filename=\"" + fileDetails.getFileOriginalName() + "\"")
          .body(resource);

    } catch (IllegalArgumentException e) {
      log.error("파일 조회 실패: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("파일 조회 중 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 보안 파일 조회 API (accessToken 기반)
   * accessToken과 file_details ID를 조합하여 안전하게 파일을 조회합니다.
   *
   * @param accessToken 파일 접근 토큰 (UUID)
   * @param fileDetailId file_details 테이블의 ID
   * @return 파일 리소스
   */
  @GetMapping("/secure/{accessToken}/{fileDetailId}")
  public ResponseEntity<Resource> getFileByToken(
      @PathVariable String accessToken,
      @PathVariable Long fileDetailId) {

    try {
      Resource resource = fileService.getFileByToken(accessToken, fileDetailId);

      // 파일 상세 정보 조회하여 원본 파일명과 Content-Type 설정
      FileDetails fileDetails = fileDetailsRepository.findById(fileDetailId)
          .orElseThrow(() -> new IllegalArgumentException("파일 상세 정보를 찾을 수 없습니다."));

      // Content-Type 설정 (저장된 fileType 사용, 없으면 기본값)
      String contentType = fileDetails.getFileType();
      if (contentType == null || contentType.isEmpty()) {
        contentType = "application/octet-stream";
      }

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "inline; filename=\"" + fileDetails.getFileOriginalName() + "\"")
          .body(resource);

    } catch (IllegalArgumentException e) {
      log.error("파일 조회 실패: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("파일 조회 중 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 보안 파일 다운로드 API (accessToken 기반)
   * accessToken과 file_details ID를 조합하여 안전하게 파일을 다운로드합니다.
   *
   * @param accessToken 파일 접근 토큰 (UUID)
   * @param fileDetailId file_details 테이블의 ID
   * @return 파일 리소스 (다운로드)
   */
  @GetMapping("/secure/download/{accessToken}/{fileDetailId}")
  public ResponseEntity<Resource> downloadFileByToken(
      @PathVariable String accessToken,
      @PathVariable Long fileDetailId) {

    try {
      Resource resource = fileService.getFileByToken(accessToken, fileDetailId);

      // 파일 상세 정보 조회하여 원본 파일명 설정
      FileDetails fileDetails = fileDetailsRepository.findById(fileDetailId)
          .orElseThrow(() -> new IllegalArgumentException("파일 상세 정보를 찾을 수 없습니다."));

      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + fileDetails.getFileOriginalName() + "\"")
          .body(resource);

    } catch (IllegalArgumentException e) {
      log.error("파일 다운로드 실패: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("파일 다운로드 중 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
