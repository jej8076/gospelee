package com.gospelee.api.service.bulletin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 클라이언트가 보낸 contentType 대신 서버가 판별한 값을 쓰는 래퍼.
 * 파일 조회 API 는 저장된 contentType 을 그대로 응답하므로, 업로드 시점에 안전한 값으로 고정한다.
 */
final class NormalizedMultipartFile implements MultipartFile {

  private final MultipartFile delegate;
  private final String contentType;

  NormalizedMultipartFile(MultipartFile delegate, String contentType) {
    this.delegate = delegate;
    this.contentType = contentType;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getOriginalFilename() {
    return delegate.getOriginalFilename();
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public long getSize() {
    return delegate.getSize();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return delegate.getBytes();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override
  public Resource getResource() {
    return delegate.getResource();
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    delegate.transferTo(dest);
  }

  @Override
  public void transferTo(Path dest) throws IOException, IllegalStateException {
    delegate.transferTo(dest);
  }
}
