package com.gospelee.api.service.gallery;

import com.gospelee.api.dto.gallery.GalleryImageListRequestDTO;
import com.gospelee.api.dto.gallery.GalleryImageListResponseDTO;
import com.gospelee.api.dto.gallery.GalleryImageResponseDTO;
import com.gospelee.api.dto.gallery.StorageStatusResponseDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface GalleryImageService {

  List<GalleryImageResponseDTO> uploadImages(Long folderId, List<String> targetRoles,
      List<MultipartFile> files);

  GalleryImageListResponseDTO getImages(GalleryImageListRequestDTO request);

  void deleteImages(List<Long> imageIds);

  StorageStatusResponseDTO getStorageStatus();
}
