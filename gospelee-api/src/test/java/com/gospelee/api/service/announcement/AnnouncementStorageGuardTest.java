package com.gospelee.api.service.announcement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AnnouncementStorageGuardTest {

  private static final long MB = 1024L * 1024;

  private EcclesiaJpaRepository repository;
  private AnnouncementStorageGuard guard;
  private Ecclesia ecclesia;

  @BeforeEach
  void setUp() {
    repository = mock(EcclesiaJpaRepository.class);
    guard = new AnnouncementStorageGuard(repository);
    ecclesia = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    ReflectionTestUtils.setField(ecclesia, "insertTime", LocalDateTime.now());
    when(repository.findEcclesiasByUid(5L)).thenReturn(Optional.of(ecclesia));
  }

  @Test
  void reserve_addsUsage_untilUnverifiedLimit() {
    guard.reserve(5L, 60 * MB);
    guard.reserve(5L, 40 * MB);

    assertEquals(100 * MB, ecclesia.getStorageUsedBytesOrDefault());
    EcclesiaException e = assertThrows(EcclesiaException.class, () -> guard.reserve(5L, 1));
    assertEquals(true, e.getMessage().contains("100MB"));
    assertEquals(100 * MB, ecclesia.getStorageUsedBytesOrDefault());
  }

  @Test
  void reserve_verifiedChurch_hasLargerLimit() {
    ecclesia.changeVerified(true);

    guard.reserve(5L, 500 * MB);

    assertEquals(500 * MB, ecclesia.getStorageUsedBytesOrDefault());
  }

  @Test
  void release_returnsUsage_andNeverGoesNegative() {
    guard.reserve(5L, 30 * MB);

    guard.release(5L, 10 * MB);
    assertEquals(20 * MB, ecclesia.getStorageUsedBytesOrDefault());

    guard.release(5L, 999 * MB);
    assertEquals(0L, ecclesia.getStorageUsedBytesOrDefault());
  }

  @Test
  void assertWritable_blocksExpiredUnverifiedChurch() {
    ReflectionTestUtils.setField(ecclesia, "insertTime", LocalDateTime.now().minusDays(15));

    assertThrows(EcclesiaException.class, () -> guard.assertWritable(5L));

    ecclesia.changeVerified(true);
    guard.assertWritable(5L);
  }

  @Test
  void nullEcclesia_isIgnored() {
    guard.assertWritable(null);
    guard.reserve(null, 10 * MB);
    guard.release(null, 10 * MB);
  }
}
