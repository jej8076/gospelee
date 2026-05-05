package com.gospelee.api.controller;

import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.dto.youtube.YoutubeVideoRequestDTO;
import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import com.gospelee.api.service.YoutubeVideoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

  private final YoutubeVideoService youtubeVideoService;

  /**
   * 활성 영상 목록 조회 (앱용)
   */
  @PostMapping("/videos")
  public ResponseEntity<List<YoutubeVideoResponseDTO>> getActiveVideos() {
    List<YoutubeVideoResponseDTO> videos = youtubeVideoService.getActiveVideos();
    return new ResponseEntity<>(videos, HttpStatus.OK);
  }

  /**
   * 전체 영상 목록 조회 (관리자용)
   */
  @PostMapping("/videos/all")
  public ResponseEntity<Object> getAllVideos() {
    List<YoutubeVideoResponseDTO> videos = youtubeVideoService.getAllVideos();
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", videos));
  }

  /**
   * 영상 단건 조회
   */
  @PostMapping("/videos/{id}")
  public ResponseEntity<Object> getVideoById(@PathVariable Long id) {
    YoutubeVideoResponseDTO video = youtubeVideoService.getVideoById(id);
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", video));
  }

  /**
   * 영상 등록
   */
  @PostMapping("/videos/create")
  public ResponseEntity<Object> createVideo(@RequestBody YoutubeVideoRequestDTO requestDTO) {
    YoutubeVideoResponseDTO video = youtubeVideoService.createVideo(requestDTO);
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", video));
  }

  /**
   * 영상 수정
   */
  @PutMapping("/videos")
  public ResponseEntity<Object> updateVideo(@RequestBody YoutubeVideoRequestDTO requestDTO) {
    YoutubeVideoResponseDTO video = youtubeVideoService.updateVideo(requestDTO);
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", video));
  }
}
