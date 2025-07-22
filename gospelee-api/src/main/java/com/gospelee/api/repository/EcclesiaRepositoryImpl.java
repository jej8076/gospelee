package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.repository.jdbc.ecclesia.EcclesiaJdbcRepository;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EcclesiaRepositoryImpl implements EcclesiaRepository {

  private final EcclesiaJpaRepository ecclesiaJpaRepository;
  private final EcclesiaJdbcRepository ecclesiaJdbcRepository;

  @Override
  public Optional<Ecclesia> findEcclesiasByUid(long uid) {
    return ecclesiaJpaRepository.findEcclesiasByUid(uid);
  }

  @Override
  public Optional<Ecclesia> findEcclesiasByMasterAccountUid(long accountUid) {
    return ecclesiaJpaRepository.findEcclesiasByMasterAccountUid(accountUid);
  }

  @Override
  public Ecclesia save(Ecclesia ecclesia) {
    return ecclesiaJpaRepository.save(ecclesia);
  }

  @Override
  public Optional<Ecclesia> findById(Long id) {
    return ecclesiaJpaRepository.findById(id);
  }

  @Override
  public List<EcclesiaResponseDTO> findAllWithMasterName() {
    return ecclesiaJdbcRepository.findAllWithMasterName();
  }

  @Override
  public List<EcclesiaResponseDTO> searchEcclesia(String text) {
    return ecclesiaJdbcRepository.searchEcclesia(text);
  }
}
