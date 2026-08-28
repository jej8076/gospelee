package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryFolder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalleryFolderRepository extends JpaRepository<GalleryFolder, Long>,
    GalleryFolderRepositoryCustom {

  Optional<GalleryFolder> findByIdAndEcclesiaUidAndDelYn(Long id, Long ecclesiaUid, String delYn);

  List<GalleryFolder> findByParentIdAndDelYn(Long parentId, String delYn);

  long countByParentIdAndDelYn(Long parentId, String delYn);
}
