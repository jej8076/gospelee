package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.file.FileUploadRequestDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.enums.FileCategoryType;
import com.gospelee.api.repository.FileDetailsRepository;
import com.gospelee.api.repository.FileRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
  public boolean uploadFile(FileUploadWrapperDTO fileUploadWrapperDTO) {

    // upload를 호출하는 곳에서 계정정보를 안넣어줄 수 있음
    if (fileUploadWrapperDTO.getAccountAuth() == null) {
      fileUploadWrapperDTO.changeAccountAuth(
          AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow());
    }

    FileEntity fileEntity = FileEntity.builder()
        .accountUid(fileUploadWrapperDTO.getAccountAuth().getUid())
        .category(fileUploadWrapperDTO.getFileCategoryType().name())
        .parentId(String.valueOf(fileUploadWrapperDTO.getParentId()))
        .delYn("N")
        .build();

    fileEntity = fileRepository.save(fileEntity);

    FileUploadRequestDTO request = fileToDTO(fileUploadWrapperDTO.getFileCategoryType(),
        fileUploadWrapperDTO.getFile(), fileUploadWrapperDTO.getAccountAuth());

    FileDetails fileDetails = FileDetails.builder()
        .fileId(fileEntity.getId())
        .fileSize(request.getFileSize())
        .fileType(request.getFileType())
        .fileOriginalName(request.getFileOriginalName())
        .filePath(request.getFilePath() + File.separator + request.getFileSaveName())
        .extension(request.getExtension())
        .build();

    fileDetailsRepository.save(fileDetails);

    saveFile(request, fileUploadWrapperDTO.getFile());

    return true;
  }

  private FileUploadRequestDTO fileToDTO(FileCategoryType fileCategoryType, MultipartFile file,
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
        .filePath(makeFilePath(fileCategoryType, account.getUid()))
        .fileOriginalName(originalFilename)
        .fileSaveName(UUID.randomUUID() + DOT + extension)
        .fileSize(file.getSize())
        .fileType(file.getContentType())
        .extension(extension)
        .build();
  }

  private String makeFilePath(FileCategoryType fileCategoryType, Long userUid) {
    if (userUid == null) {
      throw new IllegalArgumentException("파일 경로에 userUid가 존재하지 않음");
    }

    String path = userUid + File.separator + fileCategoryType.lowerCaseName();
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


