package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long>,
    GalleryImageRepositoryCustom {

  Optional<GalleryImage> findByIdAndEcclesiaUidAndDelYn(Long id, Long ecclesiaUid, String delYn);

  List<GalleryImage> findByFolderIdAndDelYn(Long folderId, String delYn);

  List<GalleryImage> findByIdInAndEcclesiaUidAndDelYn(List<Long> ids, Long ecclesiaUid, String delYn);

  long countByFolderIdAndDelYn(Long folderId, String delYn);
}
