package com.gospelee.api.service;

import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import org.springframework.core.io.Resource;

public interface FileService {

  FileUploadResponseDTO uploadFileWithResponse(FileUploadWrapperDTO fileUploadWrapperDTO);

  Resource getFile(Long fileId, Long fileDetailId);

  Resource getFileByToken(String accessToken, Long fileDetailId);
}