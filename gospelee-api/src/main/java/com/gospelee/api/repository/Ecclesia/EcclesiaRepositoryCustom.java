package com.gospelee.api.repository.Ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import java.util.List;

public interface EcclesiaRepositoryCustom {

  List<EcclesiaResponseDTO> findAllWithMasterName();
}
