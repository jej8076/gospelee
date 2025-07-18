package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.repository.EcclesiaRepository;
import java.util.List;

public interface EcclesiaJdbcRepository {

  List<EcclesiaResponseDTO> findAllWithMasterName();

  List<EcclesiaResponseDTO> searchEcclesia(String text);
}
