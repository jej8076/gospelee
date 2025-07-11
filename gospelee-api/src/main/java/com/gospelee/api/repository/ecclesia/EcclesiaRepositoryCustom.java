package com.gospelee.api.repository.ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import java.util.List;

public interface EcclesiaRepositoryCustom {

  List<EcclesiaResponseDTO> findAllWithMasterName();

  List<EcclesiaResponseDTO> searchEcclesia(String text);
}
