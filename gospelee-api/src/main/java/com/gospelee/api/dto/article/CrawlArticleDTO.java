package com.gospelee.api.dto.article;

import com.gospelee.api.entity.CrawlArticle;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CrawlArticleDTO {

  private Integer id;
  private String url;
  private String title;
  private String content;
  private String sourceInfo;
  private LocalDate date;
  private String imageUrl;
  private String images;       // JSON 형식
  private String category;
  private Short orderNum;
  private LocalDateTime crawledAt;
  private String selectorsUsed; // JSON 형식
  private Timestamp createdAt;
  private Timestamp updatedAt;

  @Builder
  public CrawlArticleDTO(Integer id, String url, String title, String content, String sourceInfo,
      LocalDate date, String imageUrl, String images, String category, Short orderNum,
      LocalDateTime crawledAt, String selectorsUsed, Timestamp createdAt, Timestamp updatedAt) {
    this.id = id;
    this.url = url;
    this.title = title;
    this.content = content;
    this.sourceInfo = sourceInfo;
    this.date = date;
    this.imageUrl = imageUrl;
    this.images = images;
    this.category = category;
    this.orderNum = orderNum;
    this.crawledAt = crawledAt;
    this.selectorsUsed = selectorsUsed;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static CrawlArticleDTO fromEntity(CrawlArticle entity) {
    return CrawlArticleDTO.builder()
        .id(entity.getId())
        .url(entity.getUrl())
        .title(entity.getTitle())
        .content(entity.getContent())
        .sourceInfo(entity.getSourceInfo())
        .date(entity.getDate())
        .imageUrl(entity.getImageUrl())
        .images(entity.getImages())
        .category(entity.getCategory())
        .orderNum(entity.getOrderNum())
        .crawledAt(entity.getCrawledAt())
        .selectorsUsed(entity.getSelectorsUsed())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public CrawlArticle toEntity() {
    return CrawlArticle.builder()
        .id(this.id)
        .url(this.url)
        .title(this.title)
        .content(this.content)
        .sourceInfo(this.sourceInfo)
        .date(this.date)
        .imageUrl(this.imageUrl)
        .images(this.images)
        .category(this.category)
        .orderNum(this.orderNum)
        .crawledAt(this.crawledAt)
        .selectorsUsed(this.selectorsUsed)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .build();
  }
}

