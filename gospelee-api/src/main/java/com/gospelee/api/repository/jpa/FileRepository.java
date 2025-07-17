package com.gospelee.api.repository.jpa;

import com.gospelee.api.entity.FileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

  Optional<FileEntity> findByAccessTokenAndDelYn(String accessToken, String delYn);
}
