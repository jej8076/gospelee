package com.gospelee.api.service.gallery;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.gallery.GalleryFolderRequestDTO;
import com.gospelee.api.dto.gallery.GalleryFolderResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.entity.GalleryFolder;
import com.gospelee.api.entity.GalleryImage;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.exception.GalleryException;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import com.gospelee.api.repository.jpa.gallery.GalleryFolderRepository;
import com.gospelee.api.repository.jpa.gallery.GalleryImageRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryFolderServiceImpl implements GalleryFolderService {

  private final GalleryFolderRepository galleryFolderRepository;
  private final GalleryImageRepository galleryImageRepository;
  private final EcclesiaJpaRepository ecclesiaJpaRepository;

  @Override
  @Transactional
  public GalleryFolderResponseDTO createFolder(GalleryFolderRequestDTO request) {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      throw new GalleryException(ErrorResponseType.ECCL_101);
    }

    int depth = 1;
    String targetRolesStr = rolesToString(request.getTargetRoles());

    if (request.getParentId() != null) {
      GalleryFolder parent = galleryFolderRepository.findByIdAndEcclesiaUidAndDelYn(
          request.getParentId(), ecclesiaUid, Yn.N.name())
          .orElseThrow(() -> new GalleryException(ErrorResponseType.GALL_106, "상위 폴더를 찾을 수 없습니다."));

      if (parent.getDepth() >= 3) {
        throw new GalleryException(ErrorResponseType.GALL_104, "폴더는 최대 3단계(depth)까지만 생성할 수 있습니다.");
      }

      depth = parent.getDepth() + 1;

      // 상위 폴더의 권한을 상속받거나 설정
      if (targetRolesStr == null || targetRolesStr.isEmpty()) {
        targetRolesStr = parent.getTargetRoles();
      }
    }

    GalleryFolder folder = GalleryFolder.builder()
        .ecclesiaUid(ecclesiaUid)
        .parentId(request.getParentId())
        .depth(depth)
        .name(request.getName().trim())
        .targetRoles(targetRolesStr)
        .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
        .delYn(Yn.N.name())
        .build();

    GalleryFolder saved = galleryFolderRepository.save(folder);
    return toResponseDTO(saved, 0L, 0L);
  }

  @Override
  @Transactional
  public GalleryFolderResponseDTO updateFolder(GalleryFolderRequestDTO request) {
    if (request.getId() == null) {
      throw new GalleryException(ErrorResponseType.COMM_101, "폴더 ID가 필요합니다.");
    }

    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();

    GalleryFolder folder = galleryFolderRepository.findByIdAndEcclesiaUidAndDelYn(
        request.getId(), ecclesiaUid, Yn.N.name())
        .orElseThrow(() -> new GalleryException(ErrorResponseType.GALL_106, "폴더를 찾을 수 없습니다."));

    if (request.getName() != null && !request.getName().trim().isEmpty()) {
      folder.changeName(request.getName().trim());
    }

    if (request.getTargetRoles() != null) {
      folder.changeTargetRoles(rolesToString(request.getTargetRoles()));
    }

    if (request.getSortOrder() != null) {
      folder.changeSortOrder(request.getSortOrder());
    }

    long imageCount = galleryImageRepository.countByFolderIdAndDelYn(folder.getId(), Yn.N.name());
    long subFolderCount = galleryFolderRepository.countByParentIdAndDelYn(folder.getId(), Yn.N.name());

    return toResponseDTO(folder, imageCount, subFolderCount);
  }

  @Override
  @Transactional
  public void deleteFolder(Long folderId) {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();

    GalleryFolder folder = galleryFolderRepository.findByIdAndEcclesiaUidAndDelYn(
        folderId, ecclesiaUid, Yn.N.name())
        .orElseThrow(() -> new GalleryException(ErrorResponseType.GALL_106, "삭제할 폴더를 찾을 수 없습니다."));

    // 재귀적으로 하위 폴더 및 이미지 삭제 처리 및 용량 반환
    long freedBytes = deleteFolderRecursively(folder, ecclesiaUid);

    if (freedBytes > 0) {
      final long totalFreed = freedBytes;
      ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid).ifPresent(ecclesia -> {
        ecclesia.subtractStorageUsedBytes(totalFreed);
      });
    }
  }

  private long deleteFolderRecursively(GalleryFolder folder, Long ecclesiaUid) {
    folder.markAsDeleted();
    long freedBytes = 0L;

    // 해당 폴더의 이미지 삭제
    List<GalleryImage> images = galleryImageRepository.findByFolderIdAndDelYn(folder.getId(), Yn.N.name());
    for (GalleryImage image : images) {
      image.markAsDeleted();
      freedBytes += (image.getFileSize() != null ? image.getFileSize() : 0L);
    }

    // 하위 폴더 삭제
    List<GalleryFolder> subFolders = galleryFolderRepository.findByParentIdAndDelYn(folder.getId(), Yn.N.name());
    for (GalleryFolder subFolder : subFolders) {
      freedBytes += deleteFolderRecursively(subFolder, ecclesiaUid);
    }

    return freedBytes;
  }

  @Override
  @Transactional(readOnly = true)
  public List<GalleryFolderResponseDTO> getFolders(Long parentId, String roleFilter) {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      return Collections.emptyList();
    }

    List<GalleryFolder> folders = galleryFolderRepository.findFoldersByEcclesiaAndParent(
        ecclesiaUid, parentId, roleFilter);

    return folders.stream().map(folder -> {
      long imageCount = galleryImageRepository.countByFolderIdAndDelYn(folder.getId(), Yn.N.name());
      long subFolderCount = galleryFolderRepository.countByParentIdAndDelYn(folder.getId(), Yn.N.name());
      return toResponseDTO(folder, imageCount, subFolderCount);
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<GalleryFolderResponseDTO> getAllFolders() {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      return Collections.emptyList();
    }

    List<GalleryFolder> folders = galleryFolderRepository.findAllActiveFoldersByEcclesia(ecclesiaUid);

    return folders.stream().map(folder -> {
      long imageCount = galleryImageRepository.countByFolderIdAndDelYn(folder.getId(), Yn.N.name());
      long subFolderCount = galleryFolderRepository.countByParentIdAndDelYn(folder.getId(), Yn.N.name());
      return toResponseDTO(folder, imageCount, subFolderCount);
    }).collect(Collectors.toList());
  }

  private String rolesToString(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return null;
    }
    return String.join(",", roles);
  }

  private List<String> stringToRoles(String rolesStr) {
    if (rolesStr == null || rolesStr.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.stream(rolesStr.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private GalleryFolderResponseDTO toResponseDTO(GalleryFolder folder, Long imageCount, Long subFolderCount) {
    return GalleryFolderResponseDTO.builder()
        .id(folder.getId())
        .ecclesiaUid(folder.getEcclesiaUid())
        .parentId(folder.getParentId())
        .depth(folder.getDepth())
        .name(folder.getName())
        .targetRoles(stringToRoles(folder.getTargetRoles()))
        .sortOrder(folder.getSortOrder())
        .imageCount(imageCount)
        .subFolderCount(subFolderCount)
        .insertTime(folder.getInsertTime())
        .updateTime(folder.getUpdateTime())
        .build();
  }
}
