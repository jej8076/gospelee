package com.gospelee.api.controller;

import com.gospelee.api.dto.gallery.GalleryFolderRequestDTO;
import com.gospelee.api.dto.gallery.GalleryFolderResponseDTO;
import com.gospelee.api.service.gallery.GalleryFolderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gallery/folder")
public class GalleryFolderController {

  private final GalleryFolderService galleryFolderService;

  @GetMapping("/list")
  public ResponseEntity<List<GalleryFolderResponseDTO>> getFolders(
      @RequestParam(value = "parentId", required = false) Long parentId,
      @RequestParam(value = "roleFilter", required = false) String roleFilter) {
    List<GalleryFolderResponseDTO> folders = galleryFolderService.getFolders(parentId, roleFilter);
    return new ResponseEntity<>(folders, HttpStatus.OK);
  }

  @GetMapping("/all")
  public ResponseEntity<List<GalleryFolderResponseDTO>> getAllFolders() {
    List<GalleryFolderResponseDTO> folders = galleryFolderService.getAllFolders();
    return new ResponseEntity<>(folders, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<GalleryFolderResponseDTO> createFolder(
      @RequestBody @Valid GalleryFolderRequestDTO request) {
    GalleryFolderResponseDTO response = galleryFolderService.createFolder(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping
  public ResponseEntity<GalleryFolderResponseDTO> updateFolder(
      @RequestBody @Valid GalleryFolderRequestDTO request) {
    GalleryFolderResponseDTO response = galleryFolderService.updateFolder(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable("id") Long id) {
    galleryFolderService.deleteFolder(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
