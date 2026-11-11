package com.gospelee.api.controller;

import com.gospelee.api.dto.ministry.MinistryDtos.DecisionRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.InviteSettingsRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.JoinRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.LeaveRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.ReportRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.RoleRequest;
import com.gospelee.api.dto.ministry.MinistryDtos.SaveRequest;
import com.gospelee.api.service.ministry.MinistryReportService;
import com.gospelee.api.service.ministry.MinistryService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 모임 API. 성공 응답은 앱이 빈 본문을 실패로 보므로 항상 본문을 가진다 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ministry")
public class MinistryController {

  private final MinistryService ministryService;
  private final MinistryReportService reportService;

  private static ResponseEntity<Object> ok(Object body) {
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestBody @Valid SaveRequest body) {
    return ok(ministryService.create(body));
  }

  @GetMapping("/mine")
  public ResponseEntity<Object> mine() {
    return ok(ministryService.getMine());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable("id") Long id) {
    return ok(ministryService.get(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Object> update(@PathVariable("id") Long id,
      @RequestBody @Valid SaveRequest body) {
    return ok(ministryService.update(id, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
    return ok(ministryService.delete(id));
  }

  @PostMapping("/{id}/leave")
  public ResponseEntity<Object> leave(@PathVariable("id") Long id,
      @RequestBody(required = false) LeaveRequest body) {
    return ok(ministryService.leave(id, body == null ? null : body.successorUid()));
  }

  @GetMapping("/{id}/members")
  public ResponseEntity<Object> members(@PathVariable("id") Long id) {
    return ok(ministryService.getMembers(id));
  }

  @PatchMapping("/{id}/members/{uid}/role")
  public ResponseEntity<Object> changeRole(@PathVariable("id") Long id,
      @PathVariable("uid") Long uid, @RequestBody RoleRequest body) {
    return ok(ministryService.changeRole(id, uid, body.role()));
  }

  @DeleteMapping("/{id}/members/{uid}")
  public ResponseEntity<Object> removeMember(@PathVariable("id") Long id,
      @PathVariable("uid") Long uid) {
    return ok(ministryService.removeMember(id, uid));
  }

  @GetMapping("/{id}/invite")
  public ResponseEntity<Object> invite(@PathVariable("id") Long id) {
    return ok(ministryService.getInvite(id));
  }

  @PostMapping("/{id}/invite/regenerate")
  public ResponseEntity<Object> regenerateInvite(@PathVariable("id") Long id) {
    return ok(ministryService.regenerateInvite(id));
  }

  @PatchMapping("/{id}/invite/settings")
  public ResponseEntity<Object> inviteSettings(@PathVariable("id") Long id,
      @RequestBody InviteSettingsRequest body) {
    return ok(ministryService.updateInviteSettings(id, body.autoApprove()));
  }

  @GetMapping("/invite/info/{code}")
  public ResponseEntity<Object> inviteInfo(@PathVariable("code") String code) {
    return ok(ministryService.getInviteInfo(code));
  }

  @PostMapping("/join/invite")
  public ResponseEntity<Object> join(@RequestBody JoinRequest body) {
    return ok(ministryService.join(body.code()));
  }

  @GetMapping("/{id}/requests")
  public ResponseEntity<Object> requests(@PathVariable("id") Long id) {
    return ok(ministryService.getRequests(id));
  }

  @PatchMapping("/{id}/requests/{uid}")
  public ResponseEntity<Object> decide(@PathVariable("id") Long id, @PathVariable("uid") Long uid,
      @RequestBody DecisionRequest body) {
    return ok(ministryService.decideRequest(id, uid, body.approve()));
  }

  /** 모임 공지 신고. 멤버만 할 수 있고 관리자 신고 관리에서 처리한다 */
  @PostMapping("/{id}/announcements/{announcementId}/report")
  public ResponseEntity<Object> reportAnnouncement(@PathVariable("id") Long id,
      @PathVariable("announcementId") Long announcementId, @RequestBody ReportRequest body) {
    reportService.report(AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow(), id,
        announcementId, body.reason(), body.detail());
    return ok(Map.of("reported", true));
  }
}
