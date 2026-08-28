package com.gospelee.api.service.gallery;

import com.gospelee.api.dto.gallery.GalleryFolderRequestDTO;
import com.gospelee.api.dto.gallery.GalleryFolderResponseDTO;
import java.util.List;

public interface GalleryFolderService {

  GalleryFolderResponseDTO createFolder(GalleryFolderRequestDTO request);

  GalleryFolderResponseDTO updateFolder(GalleryFolderRequestDTO request);

  void deleteFolder(Long folderId);

  List<GalleryFolderResponseDTO> getFolders(Long parentId, String roleFilter);

  List<GalleryFolderResponseDTO> getAllFolders();
}
