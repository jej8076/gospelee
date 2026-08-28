package com.gospelee.api.controller;

import com.gospelee.api.dto.gallery.GalleryImageListRequestDTO;
import com.gospelee.api.dto.gallery.GalleryImageListResponseDTO;
import com.gospelee.api.dto.gallery.GalleryImageResponseDTO;
import com.gospelee.api.dto.gallery.StorageStatusResponseDTO;
import com.gospelee.api.service.gallery.GalleryImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gallery")
public class GalleryImageController {

  private final GalleryImageService galleryImageService;

  @PostMapping("/images/upload")
  public ResponseEntity<List<GalleryImageResponseDTO>> uploadImages(
      @RequestPart("files") List<MultipartFile> files,
      @RequestParam(value = "folderId", required = false) Long folderId,
      @RequestParam(value = "targetRoles", required = false) List<String> targetRoles) {
    List<GalleryImageResponseDTO> response = galleryImageService.uploadImages(folderId, targetRoles, files);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/images/list")
  public ResponseEntity<GalleryImageListResponseDTO> getImages(
      @RequestBody GalleryImageListRequestDTO request) {
    GalleryImageListResponseDTO response = galleryImageService.getImages(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/images")
  public ResponseEntity<Void> deleteImages(@RequestBody List<Long> imageIds) {
    galleryImageService.deleteImages(imageIds);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/storage/status")
  public ResponseEntity<StorageStatusResponseDTO> getStorageStatus() {
    StorageStatusResponseDTO response = galleryImageService.getStorageStatus();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
