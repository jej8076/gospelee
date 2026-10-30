package com.gospelee.api.controller;

import com.gospelee.api.dto.bulletin.BulletinRequestDTO;
import com.gospelee.api.dto.bulletin.BulletinResponseDTO;
import com.gospelee.api.service.bulletin.BulletinService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 교회 주보 API. 조회는 교인, 등록/수정/삭제는 교회 관리자 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bulletin")
public class BulletinController {

  private final BulletinService bulletinService;

  @GetMapping
  public ResponseEntity<Object> getList(HttpServletRequest request,
      @RequestParam(value = "limit", required = false) Integer limit) {
    List<BulletinResponseDTO> list = bulletinService.getList(request, limit);
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable("id") Long id) {
    return new ResponseEntity<>(bulletinService.get(id), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insert(
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @RequestPart("body") @Valid BulletinRequestDTO body) {
    log.info("주보 등록 요청 - 파일 개수: {}, worshipDate: {}", files == null ? 0 : files.size(),
        body.getWorshipDate());
    return new ResponseEntity<>(bulletinService.insert(files, body), HttpStatus.OK);
  }

  @PutMapping
  public ResponseEntity<Object> update(
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @RequestPart("body") @Valid BulletinRequestDTO body) {
    log.info("주보 수정 요청 - id: {}, 파일 개수: {}", body.getId(), files == null ? 0 : files.size());
    return new ResponseEntity<>(bulletinService.update(files, body), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
    bulletinService.delete(id);
    // 앱 ApiClient 는 빈 본문을 실패로 처리하므로 삭제한 id 를 본문으로 반환
    return new ResponseEntity<>(Map.of("id", id), HttpStatus.OK);
  }
}
