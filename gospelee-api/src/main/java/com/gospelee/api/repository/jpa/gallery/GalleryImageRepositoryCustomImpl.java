package com.gospelee.api.repository.jpa.gallery;

import com.gospelee.api.entity.GalleryImage;
import com.gospelee.api.entity.QGalleryFolder;
import com.gospelee.api.entity.QGalleryImage;
import com.gospelee.api.enums.Yn;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class GalleryImageRepositoryCustomImpl implements GalleryImageRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<GalleryImage> findImagesByCriteria(Long ecclesiaUid, Long folderId,
      Boolean includeUncategorized, LocalDate startDate, LocalDate endDate,
      String targetRole, int page, int size) {
    QGalleryImage image = QGalleryImage.galleryImage;
    QGalleryFolder folder = QGalleryFolder.galleryFolder;

    BooleanExpression condition = buildCondition(image, folder, ecclesiaUid, folderId,
        includeUncategorized, startDate, endDate, targetRole);

    return queryFactory
        .selectFrom(image)
        .leftJoin(folder).on(image.folderId.eq(folder.id))
        .where(condition)
        .orderBy(image.insertTime.desc(), image.id.desc())
        .offset((long) page * size)
        .limit(size)
        .fetch();
  }

  @Override
  public long countImagesByCriteria(Long ecclesiaUid, Long folderId, Boolean includeUncategorized,
      LocalDate startDate, LocalDate endDate, String targetRole) {
    QGalleryImage image = QGalleryImage.galleryImage;
    QGalleryFolder folder = QGalleryFolder.galleryFolder;

    BooleanExpression condition = buildCondition(image, folder, ecclesiaUid, folderId,
        includeUncategorized, startDate, endDate, targetRole);

    Long count = queryFactory
        .select(image.count())
        .from(image)
        .leftJoin(folder).on(image.folderId.eq(folder.id))
        .where(condition)
        .fetchOne();

    return count != null ? count : 0L;
  }

  @Override
  public long sumFileSizeByEcclesiaUid(Long ecclesiaUid) {
    QGalleryImage image = QGalleryImage.galleryImage;

    Long sum = queryFactory
        .select(image.fileSize.sum())
        .from(image)
        .where(image.ecclesiaUid.eq(ecclesiaUid).and(image.delYn.eq(Yn.N.name())))
        .fetchOne();

    return sum != null ? sum : 0L;
  }

  private BooleanExpression buildCondition(QGalleryImage image, QGalleryFolder folder,
      Long ecclesiaUid, Long folderId, Boolean includeUncategorized, LocalDate startDate,
      LocalDate endDate, String targetRole) {
    BooleanExpression condition = image.ecclesiaUid.eq(ecclesiaUid)
        .and(image.delYn.eq(Yn.N.name()));

    if (folderId != null) {
      condition = condition.and(image.folderId.eq(folderId));
    } else if (Boolean.TRUE.equals(includeUncategorized)) {
      condition = condition.and(image.folderId.isNull());
    }

    if (startDate != null) {
      condition = condition.and(image.insertTime.goe(startDate.atStartOfDay()));
    }

    if (endDate != null) {
      condition = condition.and(image.insertTime.loe(endDate.atTime(LocalTime.MAX)));
    }

    if (targetRole != null && !targetRole.trim().isEmpty() && !"ALL".equalsIgnoreCase(targetRole)) {
      BooleanExpression folderRoleMatch = image.folderId.isNotNull().and(
          folder.targetRoles.isNull()
              .or(folder.targetRoles.isEmpty())
              .or(folder.targetRoles.containsIgnoreCase(targetRole))
      );

      BooleanExpression rootRoleMatch = image.folderId.isNull().and(
          image.targetRoles.isNull()
              .or(image.targetRoles.isEmpty())
              .or(image.targetRoles.containsIgnoreCase(targetRole))
      );

      condition = condition.and(folderRoleMatch.or(rootRoleMatch));
    }

    return condition;
  }
}
