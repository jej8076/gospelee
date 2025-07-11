package com.gospelee.api.repository.ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

// TODO jej8076 JPQL 사용한 부분은 추후 제거할 예정
@Repository("jpqlEcclesiaRepository")
public class JpqlEcclesiaRepositoryCustomImpl implements EcclesiaRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<EcclesiaResponseDTO> findAllWithMasterName() {
    String jpql = """
        SELECT e.uid AS ecclesiaUid,
               e.churchIdentificationNumber AS churchIdentificationNumber,
               e.status AS status,
               e.name AS ecclesiaName,
               a.name AS masterAccountName,
               e.insertTime AS insertTime
        FROM Ecclesia e
        LEFT JOIN Account a ON e.masterAccountUid = a.uid
        """;

    TypedQuery<EcclesiaResponseDTO> query = entityManager.createQuery(jpql,
        EcclesiaResponseDTO.class);
    return query.getResultList();
  }

  @Override
  public List<EcclesiaResponseDTO> searchEcclesia(String text) {
    return List.of();
  }
}
