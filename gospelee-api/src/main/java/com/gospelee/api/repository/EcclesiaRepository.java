package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.projection.EcclesiaResponseProjection;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EcclesiaRepository extends JpaRepository<Ecclesia, String> {

  @Query("SELECT "
      + "e.uid AS ecclesiaUid "
      + ", e.churchIdentificationNumber AS churchIdentificationNumber "
      + ", e.status AS status "
      + ", e.name AS ecclesiaName "
      + ", a.name AS masterAccountName "
      + ", e.insertTime AS insertTime "
      + "FROM Ecclesia e "
      + "LEFT JOIN Account a "
      + "ON e.masterAccountUid = a.uid")
  List<EcclesiaResponseProjection> findAllWithMasterName();

  Optional<Ecclesia> findEcclesiasByUid(long uid);
}
