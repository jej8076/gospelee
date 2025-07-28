package com.gospelee.api.repository.jpa.file;

import com.gospelee.api.entity.FileDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailsRepository extends JpaRepository<FileDetails, Long> {

  List<FileDetails> findAllByFileId(Long fileId);
}
