package com.gospelee.api.service;

import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;

public interface EcclesiaService {

  List<EcclesiaResponseDTO> getEcclesiaAll();

  Ecclesia getEcclesia(Long ecclesiaUid);

  Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO);

}
