package com.gospelee.api.service.bulletin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gospelee.api.service.bulletin.BulletinFilePolicy.Kind;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class BulletinFilePolicyTest {

  private static final byte[] PDF = "%PDF-1.7 body".getBytes();
  private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0};
  private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
  private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

  private MockMultipartFile file(String name, String contentType, byte[] bytes) {
    return new MockMultipartFile("files", name, contentType, bytes);
  }

  @Test
  void detect_pdfAndImages_byExtensionAndMagicBytes() {
    assertEquals(Kind.PDF, BulletinFilePolicy.detect(file("a.pdf", "application/pdf", PDF)));
    assertEquals(Kind.PDF, BulletinFilePolicy.detect(file("A.PDF", "text/html", PDF)));
    assertEquals(Kind.IMAGE, BulletinFilePolicy.detect(file("a.jpg", "image/jpeg", JPEG)));
    assertEquals(Kind.IMAGE, BulletinFilePolicy.detect(file("a.jpeg", "image/jpeg", JPEG)));
    assertEquals(Kind.IMAGE, BulletinFilePolicy.detect(file("a.png", "image/png", PNG)));
    assertEquals(Kind.IMAGE, BulletinFilePolicy.detect(file("a.webp", "image/webp", WEBP)));
  }

  @Test
  void detect_rejectsMismatchedOrUnsupportedFiles() {
    // 확장자만 pdf 이고 내용이 html 인 경우
    assertThrows(IllegalArgumentException.class, () -> BulletinFilePolicy.detect(
        file("a.pdf", "application/pdf", "<html>".getBytes())));
    // 이미지 확장자인데 내용이 PDF 인 경우
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.detect(file("a.png", "image/png", PDF)));
    // 허용하지 않는 형식
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.detect(file("a.gif", "image/gif", "GIF89a".getBytes())));
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.detect(file("noext", "image/png", PNG)));
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.detect(file(null, "image/png", PNG)));
  }

  @Test
  void validateSet_allowsOnePdfOrManyImages() {
    assertDoesNotThrow(() -> BulletinFilePolicy.validateSet(List.of(Kind.PDF)));
    assertDoesNotThrow(() -> BulletinFilePolicy.validateSet(List.of(Kind.IMAGE, Kind.IMAGE)));
    assertDoesNotThrow(() -> BulletinFilePolicy.validateSet(
        Collections.nCopies(BulletinFilePolicy.MAX_IMAGE_COUNT, Kind.IMAGE)));
  }

  @Test
  void validateSet_rejectsEmptyMixedAndTooMany() {
    assertThrows(IllegalArgumentException.class, () -> BulletinFilePolicy.validateSet(List.of()));
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.validateSet(List.of(Kind.PDF, Kind.IMAGE)));
    assertThrows(IllegalArgumentException.class,
        () -> BulletinFilePolicy.validateSet(List.of(Kind.PDF, Kind.PDF)));
    assertThrows(IllegalArgumentException.class, () -> BulletinFilePolicy.validateSet(
        Collections.nCopies(BulletinFilePolicy.MAX_IMAGE_COUNT + 1, Kind.IMAGE)));
  }

  @Test
  void kindOfStored_mapsExtension() {
    assertEquals(Kind.PDF, BulletinFilePolicy.kindOfStored("PDF"));
    assertEquals(Kind.IMAGE, BulletinFilePolicy.kindOfStored("jpg"));
    assertNull(BulletinFilePolicy.kindOfStored("exe"));
    assertNull(BulletinFilePolicy.kindOfStored(null));
  }
}
