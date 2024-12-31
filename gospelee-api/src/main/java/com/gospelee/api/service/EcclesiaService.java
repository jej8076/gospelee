package com.gospelee.api.service;

import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;

public interface EcclesiaService {

  List<Ecclesia> getEcclesia();

  Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO);

}
