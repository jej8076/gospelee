package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "youtube_video")
@Getter
@NoArgsConstructor
public class YoutubeVideo extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "video_id", nullable = false, unique = true)
  private String videoId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Column(name = "channel_title")
  private String channelTitle;

  @Column(name = "published_at")
  private String publishedAt;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "sort_order")
  private Integer sortOrder;

  @Builder
  public YoutubeVideo(Long id, String videoId, String title, String description,
      String thumbnailUrl,
      String channelTitle, String publishedAt, Boolean isActive, Integer sortOrder) {
    this.id = id;
    this.videoId = videoId;
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.channelTitle = channelTitle;
    this.publishedAt = publishedAt;
    this.isActive = isActive;
    this.sortOrder = sortOrder;
  }
}
