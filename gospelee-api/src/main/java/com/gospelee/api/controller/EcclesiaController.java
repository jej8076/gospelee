package com.gospelee.api.controller;

import com.gospelee.api.dto.common.SearchDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.service.EcclesiaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ecclesia")
public class EcclesiaController {

  private final EcclesiaService ecclesiaService;

  @PostMapping("/list")
  public ResponseEntity<Object> getEcclesias() {
    List<EcclesiaResponseDTO> ecclesiaList = ecclesiaService.getEcclesiaList();
    return new ResponseEntity<>(ecclesiaList, HttpStatus.OK);
  }

  @PostMapping("/search")
  public ResponseEntity<Object> searchEcclesia(@RequestBody SearchDTO searchDTO) {
    List<EcclesiaResponseDTO> ecclesiaList = ecclesiaService.searchEcclesia(searchDTO.getText());
    return new ResponseEntity<>(ecclesiaList, HttpStatus.OK);
  }

  @PostMapping("/{ecclesiaUid}")
  public ResponseEntity<Object> getEcclesia(@PathVariable("ecclesiaUid") Long ecclesiaUid) {
    Ecclesia ecclesia = ecclesiaService.getEcclesia(ecclesiaUid);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  @PostMapping("/account/{accountUid}")
  public ResponseEntity<Object> getEcclesiaByAccount(@PathVariable("accountUid") Long accountUid) {
    Ecclesia ecclesia = ecclesiaService.getEcclesiaByAccountUid(accountUid);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insertEcclesia(@RequestBody EcclesiaInsertDTO ecclesiaInsertDTO) {
    Ecclesia ecclesia = ecclesiaService.saveEcclesia(ecclesiaInsertDTO);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  @PatchMapping("/status")
  public ResponseEntity<Object> updateEcclesia(@RequestBody EcclesiaUpdateDTO ecclesiaUpdateDTO) {
    EcclesiaResponseDTO responseDTO = ecclesiaService.updateEcclesia(ecclesiaUpdateDTO);
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  /**
   * 교회 참여 요청
   *
   * @param ecclesiaUid
   * @return
   */
  @PostMapping("/join/request/{ecclesiaUid}")
  public ResponseEntity<Object> joinRequestEcclesia(@PathVariable("ecclesiaUid") Long ecclesiaUid) {
    AccountEcclesiaHistory result = ecclesiaService.joinRequestEcclesia(ecclesiaUid);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

}
