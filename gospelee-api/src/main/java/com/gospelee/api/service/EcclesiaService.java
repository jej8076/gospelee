package com.gospelee.api.service;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;

public interface EcclesiaService {

  List<EcclesiaResponseDTO> getEcclesiaList();

  List<EcclesiaResponseDTO> searchEcclesia(String text);

  Ecclesia getEcclesia(Long ecclesiaUid);

  Ecclesia getEcclesiaByAccountUid(Long accountUid);

  Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO);

  EcclesiaResponseDTO updateEcclesia(EcclesiaUpdateDTO ecclesiaUpdateDTO);

  AccountEcclesiaHistory joinRequestEcclesia(Long ecclesiaUid);

  List<AccountEcclesiaHistoryDTO> getJoinRequestList();
}
