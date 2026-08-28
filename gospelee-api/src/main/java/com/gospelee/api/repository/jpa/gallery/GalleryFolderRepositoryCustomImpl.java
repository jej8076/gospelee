package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryFolder;
import com.gospelee.api.entity.QGalleryFolder;
import com.gospelee.api.enums.Yn;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class GalleryFolderRepositoryCustomImpl implements GalleryFolderRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<GalleryFolder> findFoldersByEcclesiaAndParent(Long ecclesiaUid, Long parentId,
      String roleFilter) {
    QGalleryFolder folder = QGalleryFolder.galleryFolder;

    BooleanExpression condition = folder.ecclesiaUid.eq(ecclesiaUid)
        .and(folder.delYn.eq(Yn.N.name()));

    if (parentId == null) {
      condition = condition.and(folder.parentId.isNull());
    } else {
      condition = condition.and(folder.parentId.eq(parentId));
    }

    if (roleFilter != null && !roleFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
      condition = condition.and(
          folder.targetRoles.isNull()
              .or(folder.targetRoles.isEmpty())
              .or(folder.targetRoles.containsIgnoreCase(roleFilter))
      );
    }

    return queryFactory
        .selectFrom(folder)
        .where(condition)
        .orderBy(folder.sortOrder.asc(), folder.insertTime.asc())
        .fetch();
  }

  @Override
  public List<GalleryFolder> findAllActiveFoldersByEcclesia(Long ecclesiaUid) {
    QGalleryFolder folder = QGalleryFolder.galleryFolder;

    return queryFactory
        .selectFrom(folder)
        .where(folder.ecclesiaUid.eq(ecclesiaUid).and(folder.delYn.eq(Yn.N.name())))
        .orderBy(folder.depth.asc(), folder.sortOrder.asc())
        .fetch();
  }
}
