package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.prayer.PrayerDTOs.CardCreateRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.ReportRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.SettingRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeEventRequest;
import com.gospelee.api.service.prayer.PrayerAdminService;
import com.gospelee.api.service.prayer.PrayerCardService;
import com.gospelee.api.service.prayer.PrayerScopeResolver;
import com.gospelee.api.service.prayer.PrayerSettingService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기도 쪽지 API. 성도용(/prayer/...)과 교회 관리자용(/prayer/admin/...)이 함께 있고,
 * 권한과 범위는 서비스에서 로그인 계정으로 판단한다. 모든 요청에 ministryId 를 붙이면 교회 대신 그 모임의
 * 기도 쪽지로 동작한다 (모임 멤버·대표/운영진 확인은 {@link PrayerScopeResolver}).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/prayer")
public class PrayerController {

  private final PrayerSettingService settingService;
  private final PrayerCardService cardService;
  private final PrayerAdminService adminService;
  private final PrayerScopeResolver scopeResolver;

  // 앱 ApiClient 는 빈 본문을 실패로 처리하므로 본문 없는 응답에는 id 를 담는다
  private static ResponseEntity<Object> ok(Object body) {
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private static ResponseEntity<Object> done(Long id) {
    return ok(Map.of("id", id));
  }

  /** 성도용: ministryId 가 있으면 그 모임의 기도 쪽지, 없으면 내 교회의 기도 쪽지 */
  private AccountAuthDTO member(Long ministryId) {
    return scopeResolver.member(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(),
        ministryId);
  }

  /** 관리자용: ministryId 가 있으면 모임 대표·운영진, 없으면 교회 관리자 */
  private AccountAuthDTO manager(Long ministryId) {
    return scopeResolver.manager(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(),
        ministryId);
  }

  // --- 성도용 -----------------------------------------------------------------

  @GetMapping("/setting")
  public ResponseEntity<Object> getSetting(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(settingService.appSetting(member(ministryId)));
  }

  @PostMapping("/cards")
  public ResponseEntity<Object> create(@RequestParam(name = "ministryId", required = false) Long ministryId, @RequestBody @Valid CardCreateRequest body) {
    return ok(cardService.create(member(ministryId), body));
  }

  @GetMapping("/cards/mine")
  public ResponseEntity<Object> mine(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(cardService.mine(member(ministryId)));
  }

  @PostMapping("/cards/draw")
  public ResponseEntity<Object> draw(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(cardService.draw(member(ministryId)));
  }

  @GetMapping("/cards/{id}")
  public ResponseEntity<Object> get(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    return ok(cardService.get(member(ministryId), id));
  }

  @PostMapping("/cards/{id}/respond")
  public ResponseEntity<Object> respond(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id,
      @RequestBody(required = false) RespondRequest body) {
    return ok(cardService.respond(member(ministryId), id,
        body));
  }

  @PutMapping("/cards/{id}/answered")
  public ResponseEntity<Object> answered(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    cardService.markAnswered(member(ministryId), id);
    return done(id);
  }

  @DeleteMapping("/cards/{id}")
  public ResponseEntity<Object> delete(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    cardService.delete(member(ministryId), id);
    return done(id);
  }

  @PostMapping("/cards/{id}/report")
  public ResponseEntity<Object> report(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id,
      @RequestBody @Valid ReportRequest body) {
    cardService.report(member(ministryId), id, body);
    return done(id);
  }

  @GetMapping("/summary")
  public ResponseEntity<Object> summary(@RequestParam(name = "ministryId", required = false) Long ministryId, 
      @RequestParam(name = "since", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
    return ok(cardService.summary(member(ministryId), since));
  }

  @GetMapping("/tree")
  public ResponseEntity<Object> tree(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(cardService.tree(member(ministryId)));
  }

  // --- 교회 관리자용 ----------------------------------------------------------

  @GetMapping("/admin/setting")
  public ResponseEntity<Object> adminSetting(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(settingService.adminSetting(
        manager(ministryId)));
  }

  @PutMapping("/admin/setting")
  public ResponseEntity<Object> updateSetting(@RequestParam(name = "ministryId", required = false) Long ministryId, @RequestBody @Valid SettingRequest body) {
    return ok(settingService.updateSetting(
        manager(ministryId), body));
  }

  @PutMapping("/admin/tree-event")
  public ResponseEntity<Object> updateTreeEvent(@RequestParam(name = "ministryId", required = false) Long ministryId, @RequestBody @Valid TreeEventRequest body) {
    return ok(settingService.updateTreeEvent(
        manager(ministryId), body));
  }

  @GetMapping("/admin/reports")
  public ResponseEntity<Object> reports(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(adminService.reportedCards(manager(ministryId)));
  }

  @GetMapping("/admin/pending")
  public ResponseEntity<Object> pending(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(adminService.pendingCards(manager(ministryId)));
  }

  @PutMapping("/admin/cards/{id}/hide")
  public ResponseEntity<Object> hide(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    adminService.hide(manager(ministryId), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/restore")
  public ResponseEntity<Object> restore(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    adminService.restore(manager(ministryId), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/approve")
  public ResponseEntity<Object> approve(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    adminService.approve(manager(ministryId), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/reject")
  public ResponseEntity<Object> reject(@RequestParam(name = "ministryId", required = false) Long ministryId, @PathVariable("id") Long id) {
    adminService.reject(manager(ministryId), id);
    return done(id);
  }

  @GetMapping("/admin/stats")
  public ResponseEntity<Object> stats(@RequestParam(name = "ministryId", required = false) Long ministryId) {
    return ok(adminService.stats(manager(ministryId)));
  }
}
