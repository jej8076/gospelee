package com.gospelee.api.controller;

import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.service.EcclesiaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/all")
  public ResponseEntity<Object> getEcclesia() {
    List<Ecclesia> getAccountAll = ecclesiaService.getEcclesia();
    return new ResponseEntity<>(getAccountAll, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insertEcclesia(@RequestBody EcclesiaInsertDTO ecclesiaInsertDTO) {
    Ecclesia ecclesia = ecclesiaService.saveEcclesia(ecclesiaInsertDTO);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

}
