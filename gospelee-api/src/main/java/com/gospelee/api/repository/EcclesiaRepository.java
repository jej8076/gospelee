package com.gospelee.api.repository;

import com.gospelee.api.entity.Ecclesia;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EcclesiaRepository extends JpaRepository<Ecclesia, String> {

  Optional<Ecclesia> findEcclesiasByUid(long uid);
}
