package com.gospelee.api.repository.jpa.ecclesia;

import com.gospelee.api.entity.Ecclesia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcclesiaRepository extends JpaRepository<Ecclesia, Long> {

  Optional<Ecclesia> findEcclesiasByUid(long uid);

  Optional<Ecclesia> findEcclesiasByMasterAccountUid(long accountUid);
}
