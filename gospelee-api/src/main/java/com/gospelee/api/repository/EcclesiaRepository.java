package com.gospelee.api.repository;

import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.repository.Ecclesia.EcclesiaRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcclesiaRepository extends JpaRepository<Ecclesia, String>,
    EcclesiaRepositoryCustom {

  Optional<Ecclesia> findEcclesiasByUid(long uid);

  Optional<Ecclesia> findEcclesiasByMasterAccountUid(long accountUid);
}
