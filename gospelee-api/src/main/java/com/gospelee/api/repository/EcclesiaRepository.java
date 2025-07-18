package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.repository.jdbc.ecclesia.EcclesiaJdbcRepository;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcclesiaRepository {

  Optional<Ecclesia> findEcclesiasByUid(long uid);

  Optional<Ecclesia> findEcclesiasByMasterAccountUid(long accountUid);

  List<EcclesiaResponseDTO> findAllWithMasterName();

  List<EcclesiaResponseDTO> searchEcclesia(String text);

  Ecclesia save(Ecclesia ecclesia);

  Optional<Ecclesia> findById(Long id);
}
