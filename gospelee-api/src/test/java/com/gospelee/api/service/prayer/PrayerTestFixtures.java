package com.gospelee.api.service.prayer;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.Yn;
import org.springframework.test.util.ReflectionTestUtils;

/** 기도 쪽지 테스트가 함께 쓰는 만들기 도우미 */
final class PrayerTestFixtures {

  static final Long ECCLESIA = 7L;

  private PrayerTestFixtures() {
  }

  static AccountAuthDTO member(long uid) {
    return AccountAuthDTO.builder().uid(uid).ecclesiaUid(ECCLESIA).role(RoleType.LAYMAN)
        .ecclesiaStatus("APL").leaveYn(Yn.N).build();
  }

  static AccountAuthDTO manager(long uid) {
    return AccountAuthDTO.builder().uid(uid).ecclesiaUid(ECCLESIA).role(RoleType.PASTOR)
        .ecclesiaStatus("APL").leaveYn(Yn.N).build();
  }

  /** 켜 둔 설정 (그 밖의 값은 기본값) */
  static PrayerSetting enabledSetting() {
    PrayerSetting s = PrayerSetting.defaults(ECCLESIA);
    s.apply(true, s.getAuthorMode(), s.getMaxLength(), s.getAllowedExpiryDays(),
        s.getWhoCanWrite(), s.getScopeMode(), s.getResponderVisibility(), s.getAdminSeesAnon(),
        s.getModerationMode(), s.getAutoHideThreshold(), s.isCheerEnabled(),
        s.getDailyDrawLimit(), s.isSummaryPush());
    return s;
  }

  static PrayerSetting setting(AuthorMode authorMode, String expiryDays, WhoCanWrite who,
      ScopeMode scope, ResponderVisibility responder, AdminSeesAnon adminSees,
      ModerationMode moderation, int threshold, boolean cheer, int drawLimit) {
    PrayerSetting s = PrayerSetting.defaults(ECCLESIA);
    s.apply(true, authorMode, 200, expiryDays, who, scope, responder, adminSees, moderation,
        threshold, cheer, drawLimit, true);
    return s;
  }

  static PrayerCard card(long id, long authorUid, DisplayMode mode, ResponderVisibility visible,
      AdminSeesAnon adminSees, int responseCount) {
    PrayerCard card = PrayerCard.builder().id(id).ecclesiaUid(ECCLESIA).authorUid(authorUid)
        .content("기도해 주세요").displayMode(mode).scope(CardScope.CHURCH)
        .status(CardStatus.ACTIVE).responderVisible(visible).adminSeesAnon(adminSees).build();
    ReflectionTestUtils.setField(card, "responseCount", responseCount);
    return card;
  }

  static PrayerCard card(long id, long authorUid, int responseCount) {
    return card(id, authorUid, DisplayMode.ANON, ResponderVisibility.HIDDEN,
        AdminSeesAnon.NEVER, responseCount);
  }
}
