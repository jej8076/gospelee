package com.gospelee.api.controller;

import com.gospelee.api.dto.prayer.PrayerDTOs.CardCreateRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.ReportRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.SettingRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeEventRequest;
import com.gospelee.api.service.prayer.PrayerAdminService;
import com.gospelee.api.service.prayer.PrayerCardService;
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
 * 권한과 교회 범위는 서비스에서 로그인 계정으로 판단한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/prayer")
public class PrayerController {

  private final PrayerSettingService settingService;
  private final PrayerCardService cardService;
  private final PrayerAdminService adminService;

  // 앱 ApiClient 는 빈 본문을 실패로 처리하므로 본문 없는 응답에는 id 를 담는다
  private static ResponseEntity<Object> ok(Object body) {
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private static ResponseEntity<Object> done(Long id) {
    return ok(Map.of("id", id));
  }

  // --- 성도용 -----------------------------------------------------------------

  @GetMapping("/setting")
  public ResponseEntity<Object> getSetting() {
    return ok(settingService.appSetting(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @PostMapping("/cards")
  public ResponseEntity<Object> create(@RequestBody @Valid CardCreateRequest body) {
    return ok(cardService.create(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), body));
  }

  @GetMapping("/cards/mine")
  public ResponseEntity<Object> mine() {
    return ok(cardService.mine(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @PostMapping("/cards/draw")
  public ResponseEntity<Object> draw() {
    return ok(cardService.draw(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @GetMapping("/cards/{id}")
  public ResponseEntity<Object> get(@PathVariable("id") Long id) {
    return ok(cardService.get(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id));
  }

  @PostMapping("/cards/{id}/respond")
  public ResponseEntity<Object> respond(@PathVariable("id") Long id,
      @RequestBody(required = false) RespondRequest body) {
    return ok(cardService.respond(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id,
        body));
  }

  @PutMapping("/cards/{id}/answered")
  public ResponseEntity<Object> answered(@PathVariable("id") Long id) {
    cardService.markAnswered(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @DeleteMapping("/cards/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
    cardService.delete(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @PostMapping("/cards/{id}/report")
  public ResponseEntity<Object> report(@PathVariable("id") Long id,
      @RequestBody @Valid ReportRequest body) {
    cardService.report(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id, body);
    return done(id);
  }

  @GetMapping("/summary")
  public ResponseEntity<Object> summary(
      @RequestParam(name = "since", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
    return ok(cardService.summary(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), since));
  }

  @GetMapping("/tree")
  public ResponseEntity<Object> tree() {
    return ok(cardService.tree(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  // --- 교회 관리자용 ----------------------------------------------------------

  @GetMapping("/admin/setting")
  public ResponseEntity<Object> adminSetting() {
    return ok(settingService.adminSetting(
        AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @PutMapping("/admin/setting")
  public ResponseEntity<Object> updateSetting(@RequestBody @Valid SettingRequest body) {
    return ok(settingService.updateSetting(
        AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), body));
  }

  @PutMapping("/admin/tree-event")
  public ResponseEntity<Object> updateTreeEvent(@RequestBody @Valid TreeEventRequest body) {
    return ok(settingService.updateTreeEvent(
        AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), body));
  }

  @GetMapping("/admin/reports")
  public ResponseEntity<Object> reports() {
    return ok(adminService.reportedCards(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @GetMapping("/admin/pending")
  public ResponseEntity<Object> pending() {
    return ok(adminService.pendingCards(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }

  @PutMapping("/admin/cards/{id}/hide")
  public ResponseEntity<Object> hide(@PathVariable("id") Long id) {
    adminService.hide(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/restore")
  public ResponseEntity<Object> restore(@PathVariable("id") Long id) {
    adminService.restore(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/approve")
  public ResponseEntity<Object> approve(@PathVariable("id") Long id) {
    adminService.approve(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @PutMapping("/admin/cards/{id}/reject")
  public ResponseEntity<Object> reject(@PathVariable("id") Long id) {
    adminService.reject(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id);
    return done(id);
  }

  @GetMapping("/admin/stats")
  public ResponseEntity<Object> stats() {
    return ok(adminService.stats(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()));
  }
}
