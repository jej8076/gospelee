package com.gospelee.api.service.announcement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.RoleType;
import org.junit.jupiter.api.Test;

class AnnouncementAccessPolicyTest {

  private AccountAuthDTO account(RoleType role, Long ecclesiaUid) {
    return AccountAuthDTO.builder().uid(1L).role(role).ecclesiaUid(ecclesiaUid).build();
  }

  private Announcement announcement(String type, Long organizationId) {
    return Announcement.builder()
        .organizationType(type)
        .organizationId(organizationId)
        .subject("s")
        .text("t")
        .build();
  }

  @Test
  void managersOfAChurch_canWrite() {
    assertTrue(AnnouncementAccessPolicy.canWriteEcclesia(account(RoleType.SENIOR_PASTOR, 10L)));
    assertTrue(AnnouncementAccessPolicy.canWriteEcclesia(account(RoleType.PASTOR, 10L)));
    assertTrue(AnnouncementAccessPolicy.canWriteEcclesia(account(RoleType.ADMIN, 10L)));
  }

  @Test
  void layman_orAccountWithoutChurch_cannotWrite() {
    assertFalse(AnnouncementAccessPolicy.canWriteEcclesia(account(RoleType.LAYMAN, 10L)));
    assertFalse(AnnouncementAccessPolicy.canWriteEcclesia(account(RoleType.SENIOR_PASTOR, null)));
    assertFalse(AnnouncementAccessPolicy.canWriteEcclesia(null));
  }

  @Test
  void modify_onlyOwnChurchAnnouncement() {
    AccountAuthDTO pastor = account(RoleType.PASTOR, 10L);

    assertTrue(AnnouncementAccessPolicy.canModifyEcclesia(pastor, announcement("ECCLESIA", 10L)));
    assertFalse(AnnouncementAccessPolicy.canModifyEcclesia(pastor, announcement("ECCLESIA", 99L)));
    assertFalse(
        AnnouncementAccessPolicy.canModifyEcclesia(pastor, announcement("BRAND_STORY", 10L)));
    assertFalse(AnnouncementAccessPolicy.canModifyEcclesia(
        account(RoleType.LAYMAN, 10L), announcement("ECCLESIA", 10L)));
  }
}
