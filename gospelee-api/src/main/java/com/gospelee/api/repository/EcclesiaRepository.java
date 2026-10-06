package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;
import java.util.Optional;

public interface EcclesiaRepository {

  Optional<Ecclesia> findEcclesiasByUid(long uid);

  Optional<Ecclesia> findEcclesiasByMasterAccountUid(long accountUid);

  List<EcclesiaResponseDTO> findAllWithMasterName();

  List<EcclesiaResponseDTO> searchEcclesia(String text);

  Optional<Ecclesia> findByInviteCode(String inviteCode);

  boolean existsByInviteCode(String inviteCode);

  Ecclesia save(Ecclesia ecclesia);

  Optional<Ecclesia> findById(Long id);
}
