package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryFolder;
import java.util.List;

public interface GalleryFolderRepositoryCustom {

  List<GalleryFolder> findFoldersByEcclesiaAndParent(Long ecclesiaUid, Long parentId, String roleFilter);

  List<GalleryFolder> findAllActiveFoldersByEcclesia(Long ecclesiaUid);
}
