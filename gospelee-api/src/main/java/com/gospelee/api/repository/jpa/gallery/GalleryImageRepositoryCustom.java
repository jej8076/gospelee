package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryImage;
import java.time.LocalDate;
import java.util.List;

public interface GalleryImageRepositoryCustom {

  List<GalleryImage> findImagesByCriteria(Long ecclesiaUid, Long folderId,
      Boolean includeUncategorized, LocalDate startDate, LocalDate endDate,
      String targetRole, int page, int size);

  long countImagesByCriteria(Long ecclesiaUid, Long folderId, Boolean includeUncategorized,
      LocalDate startDate, LocalDate endDate, String targetRole);

  long sumFileSizeByEcclesiaUid(Long ecclesiaUid);
}
