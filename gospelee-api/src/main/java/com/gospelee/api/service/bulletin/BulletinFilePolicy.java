package com.gospelee.api.service.bulletin;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;

/**
 * 주보 첨부 파일 규칙. PDF 한 개, 또는 이미지 여러 장만 허용하고 둘을 섞을 수 없다.
 * 클라이언트가 보낸 contentType 은 믿지 않고 확장자와 파일 앞부분(매직 바이트)으로 종류를 판별한다.
 */
final class BulletinFilePolicy {

  static final int MAX_IMAGE_COUNT = 20;

  enum Kind {PDF, IMAGE}

  private BulletinFilePolicy() {
  }

  /**
   * 업로드 파일의 종류를 판별한다
   *
   * @throws IllegalArgumentException PDF/이미지가 아니거나, 확장자와 실제 내용이 다른 경우
   */
  static Kind detect(MultipartFile file) {
    String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
    int dot = name.lastIndexOf('.');
    String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    byte[] head = readHead(file);

    if (ext.equals("pdf") && isPdf(head)) {
      return Kind.PDF;
    }
    if ((ext.equals("jpg") || ext.equals("jpeg")) && isJpeg(head)
        || ext.equals("png") && isPng(head)
        || ext.equals("webp") && isWebp(head)) {
      return Kind.IMAGE;
    }
    throw new IllegalArgumentException(
        "주보는 PDF 또는 이미지(jpg, png, webp) 파일만 올릴 수 있습니다: " + name);
  }

  /**
   * 최종적으로 남는 파일들의 구성이 규칙에 맞는지 확인한다
   *
   * @param kinds 남는 파일(기존 + 신규 - 삭제)의 종류 목록
   */
  static void validateSet(List<Kind> kinds) {
    if (kinds.isEmpty()) {
      throw new IllegalArgumentException("주보 파일을 한 개 이상 올려 주세요.");
    }
    long pdfCount = kinds.stream().filter(k -> k == Kind.PDF).count();
    long imageCount = kinds.size() - pdfCount;
    if (pdfCount > 0 && imageCount > 0) {
      throw new IllegalArgumentException("PDF 와 이미지를 함께 올릴 수 없습니다.");
    }
    if (pdfCount > 1) {
      throw new IllegalArgumentException("PDF 는 한 개만 올릴 수 있습니다.");
    }
    if (imageCount > MAX_IMAGE_COUNT) {
      throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 올릴 수 있습니다.");
    }
  }

  /** 저장된 파일 정보(확장자)로 종류를 판별한다. 알 수 없으면 IMAGE 가 아닌 PDF 로 오인하지 않도록 null */
  static Kind kindOfStored(String extension) {
    if (extension == null) {
      return null;
    }
    String ext = extension.toLowerCase(Locale.ROOT);
    if (ext.equals("pdf")) {
      return Kind.PDF;
    }
    if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp")) {
      return Kind.IMAGE;
    }
    return null;
  }

  private static byte[] readHead(MultipartFile file) {
    try (InputStream in = file.getInputStream()) {
      return in.readNBytes(12);
    } catch (IOException e) {
      throw new IllegalArgumentException("파일을 읽을 수 없습니다: " + file.getOriginalFilename());
    }
  }

  private static boolean isPdf(byte[] h) {
    return h.length >= 5 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F' && h[4] == '-';
  }

  private static boolean isJpeg(byte[] h) {
    return h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
  }

  private static boolean isPng(byte[] h) {
    return h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
        && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A;
  }

  private static boolean isWebp(byte[] h) {
    return h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
        && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
  }
}
