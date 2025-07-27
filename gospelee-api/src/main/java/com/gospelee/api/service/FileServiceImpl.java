package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.file.FileUploadDetailResponseDTO;
import com.gospelee.api.dto.file.FileUploadRequestDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

  private final String DOT = ".";
  private final FileRepository fileRepository;
  private final FileDetailsRepository fileDetailsRepository;

  @Value("${file.base-path}")
  private String fileBasePath;

  @Override
  @Transactional
  public FileUploadResponseDTO uploadFileWithResponse(FileUploadWrapperDTO fileUploadWrapperDTO) {

    // upload를 호출하는 곳에서 계정정보를 안넣어줄 수 있음
    if (fileUploadWrapperDTO.getAccountAuth() == null) {
      fileUploadWrapperDTO.changeAccountAuth(
          AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow());
    }

    FileEntity fileEntity = FileEntity.builder()
        .accountUid(fileUploadWrapperDTO.getAccountAuth().getUid())
        .category(fileUploadWrapperDTO.getCategoryType().name())
        .parentId(String.valueOf(fileUploadWrapperDTO.getParentId()))
        .delYn("N")
        .accessToken(UUID.randomUUID().toString())
        .build();

    fileEntity = fileRepository.save(fileEntity);

    List<FileUploadDetailResponseDTO> fileDetailList = new ArrayList<>();
    for (MultipartFile file : fileUploadWrapperDTO.getFiles()) {
      FileUploadRequestDTO request = fileToDTO(fileUploadWrapperDTO.getCategoryType(), file,
          fileUploadWrapperDTO.getAccountAuth());

      FileDetails fileDetails = FileDetails.builder()
          .fileId(fileEntity.getId())
          .fileSize(request.getFileSize())
          .fileType(request.getFileType())
          .fileOriginalName(request.getFileOriginalName())
          .filePath(request.getFilePath() + File.separator + request.getFileSaveName())
          .extension(request.getExtension())
          .build();

      // 파일 물리 저장
      saveFile(request, file);

      // detail 영속성 데이터 저장
      fileDetails = fileDetailsRepository.save(fileDetails);
      fileDetailList.add(FileUploadDetailResponseDTO.builder()
          .fileDetailId(fileDetails.getId())
          .fileOriginalName(file.getOriginalFilename())
          .build());
    }

    return FileUploadResponseDTO.builder()
        .fileId(fileEntity.getId())
        .fileDetailList(fileDetailList)
        .accessToken(fileEntity.getAccessToken())
        .build();
  }

  @Override
  public Resource getFile(Long fileId, Long fileDetailId) {
    // file 테이블에서 파일 정보 조회
    FileEntity fileEntity = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. fileId: " + fileId));

    // 삭제된 파일인지 확인
    if ("Y".equals(fileEntity.getDelYn())) {
      throw new IllegalArgumentException("삭제된 파일입니다.");
    }

    // file_details 테이블에서 파일 상세 정보 조회
    FileDetails fileDetails = fileDetailsRepository.findById(fileDetailId)
        .orElseThrow(() -> new IllegalArgumentException(
            "파일 상세 정보를 찾을 수 없습니다. fileDetailId: " + fileDetailId));

    // file_details의 fileId가 요청한 fileId와 일치하는지 확인
    if (!fileEntity.getId().equals(fileDetails.getFileId())) {
      throw new IllegalArgumentException("파일 ID와 파일 상세 ID가 일치하지 않습니다.");
    }

    // 실제 파일 경로 생성
    String fullPath = fileBasePath + fileDetails.getFilePath();
    File file = new File(fullPath);

    // 파일 존재 여부 확인
    if (!file.exists()) {
      throw new IllegalArgumentException("실제 파일이 존재하지 않습니다. path: " + fullPath);
    }

    return new FileSystemResource(file);
  }

  @Override
  public Resource getFileByToken(String accessToken, Long fileDetailId) {
    // accessToken으로 파일 정보 조회 (삭제되지 않은 파일만)
    FileEntity fileEntity = fileRepository.findByAccessTokenAndDelYn(accessToken, "N")
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 접근 토큰입니다."));

    // file_details 테이블에서 파일 상세 정보 조회
    FileDetails fileDetails = fileDetailsRepository.findById(fileDetailId)
        .orElseThrow(() -> new IllegalArgumentException(
            "파일 상세 정보를 찾을 수 없습니다. fileDetailId: " + fileDetailId));

    // file_details의 fileId가 조회된 fileId와 일치하는지 확인
    if (!fileEntity.getId().equals(fileDetails.getFileId())) {
      throw new IllegalArgumentException("파일 ID와 파일 상세 ID가 일치하지 않습니다.");
    }

    // 실제 파일 경로 생성
    String fullPath = fileBasePath + fileDetails.getFilePath();
    File file = new File(fullPath);

    // 파일 존재 여부 확인
    if (!file.exists()) {
      throw new IllegalArgumentException("실제 파일이 존재하지 않습니다. path: " + fullPath);
    }

    return new FileSystemResource(file);
  }

  private FileUploadRequestDTO fileToDTO(CategoryType categoryType, MultipartFile file,
      AccountAuthDTO account) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일이 비어있습니다.");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = "";

    if (originalFilename != null && originalFilename.contains(DOT)) {
      extension = originalFilename.substring(originalFilename.lastIndexOf(DOT) + 1);
    }

    return FileUploadRequestDTO.builder()
        .filePath(makeFilePath(categoryType, account.getUid()))
        .fileOriginalName(originalFilename)
        .fileSaveName(UUID.randomUUID() + DOT + extension)
        .fileSize(file.getSize())
        .fileType(file.getContentType())
        .extension(extension)
        .build();
  }

  private String makeFilePath(CategoryType categoryType, Long userUid) {
    if (userUid == null) {
      throw new IllegalArgumentException("파일 경로에 userUid가 존재하지 않음");
    }

    String path = File.separator + userUid + File.separator + categoryType.lowerCaseName();
    String fullPath = fileBasePath + File.separator + path;

    if (!createDirectoryIfNotExists(fullPath)) {
      throw new IllegalArgumentException("파일 경로 조회(생성)에 실패함");
    }
    // basePath는 환경에 따라 변경될 수 있으므로 상대경로를 기준으로 기록함
    return path;
  }

  private boolean createDirectoryIfNotExists(String pathStr) {
    Path path = Paths.get(pathStr);

    if (!Files.exists(path)) {
      try {
        Files.createDirectories(path);
        return true;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    } else {
      return true;
    }
  }

  /**
   * 파일명을 중복없도록 생성 후 해당 파일명으로 파일을 실제로 저장하고 생성된 파일명을 return한다
   *
   * @param fileUploadRequestDTO
   * @param file
   * @return
   */
  private void saveFile(FileUploadRequestDTO fileUploadRequestDTO, MultipartFile file) {
    File fileDestination = new File(
        fileBasePath + File.separator + fileUploadRequestDTO.getFilePath(),
        fileUploadRequestDTO.getFileSaveName());
    try {
      file.transferTo(fileDestination);  // 실제 파일 저장
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}


