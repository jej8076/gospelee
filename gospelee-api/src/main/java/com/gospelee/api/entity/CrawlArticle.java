package com.gospelee.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlArticle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(length = 500, nullable = false, unique = true)
  private String url;

  @Lob
  @Column(nullable = false)
  private String title;

  @Lob
  @Column(nullable = false)
  private String content;

  @Lob
  private String sourceInfo;

  private LocalDate date;

  @Lob
  private String imageUrl;

  /**
   * JSON 형식의 images
   */
  @Lob
  @Column(columnDefinition = "LONGTEXT COLLATE utf8mb4_bin")
  private String images;

  @Column(length = 20)
  private String category;

  @Column(name = "order_num", columnDefinition = "TINYINT UNSIGNED")
  private Short orderNum;

  private LocalDateTime crawledAt;

  /**
   * JSON 형식의 selectors_used
   */
  @Lob
  @Column(columnDefinition = "LONGTEXT COLLATE utf8mb4_bin")
  private String selectorsUsed;

  @Column(nullable = false, updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp createdAt;

  @Column(nullable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private Timestamp updatedAt;
  
  @Builder
  public CrawlArticle(Integer id, String url, String title, String content, String sourceInfo,
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
}
