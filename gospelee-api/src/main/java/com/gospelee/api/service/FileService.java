package com.gospelee.api.service;

import com.gospelee.api.dto.file.FileUploadWrapperDTO;

public interface FileService {

  boolean uploadFile(FileUploadWrapperDTO fileUploadWrapperDTO);
}