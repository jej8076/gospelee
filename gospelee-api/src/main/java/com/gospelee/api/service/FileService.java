package com.gospelee.api.service;

import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.FileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

  FileUploadResponseDTO uploadFileWithResponse(FileUploadWrapperDTO fileUploadWrapperDTO);

  Resource getFile(Long fileId, Long fileDetailId);

  Resource getFileByToken(String accessToken, Long fileDetailId);
}
