package com.gospelee.api.service;

import com.gospelee.api.dto.youtube.YoutubeVideoRequestDTO;
import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import com.gospelee.api.entity.YoutubeVideo;
import com.gospelee.api.repository.jpa.youtube.YoutubeVideoRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YoutubeVideoServiceImpl implements YoutubeVideoService {

  private final YoutubeVideoRepository youtubeVideoRepository;

  @Override
  public List<YoutubeVideoResponseDTO> getActiveVideos() {
    return youtubeVideoRepository.findActiveVideosOrderBySortOrder().stream()
        .map(this::convertToResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<YoutubeVideoResponseDTO> getAllVideos() {
    return youtubeVideoRepository.findAllOrderBySortOrder().stream()
        .map(this::convertToResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public YoutubeVideoResponseDTO getVideoById(Long id) {
    YoutubeVideo video = youtubeVideoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다. id: " + id));
    return convertToResponseDTO(video);
  }

  @Override
  @Transactional
  public YoutubeVideoResponseDTO createVideo(YoutubeVideoRequestDTO requestDTO) {
    YoutubeVideo video = YoutubeVideo.builder()
        .videoId(requestDTO.getVideoId())
        .title(requestDTO.getTitle())
        .description(requestDTO.getDescription())
        .thumbnailUrl(requestDTO.getThumbnailUrl())
        .channelTitle(requestDTO.getChannelTitle())
        .publishedAt(requestDTO.getPublishedAt())
        .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
        .sortOrder(requestDTO.getSortOrder() != null ? requestDTO.getSortOrder() : 0)
        .build();

    YoutubeVideo saved = youtubeVideoRepository.save(video);
    return convertToResponseDTO(saved);
  }

  @Override
  @Transactional
  public YoutubeVideoResponseDTO updateVideo(YoutubeVideoRequestDTO requestDTO) {
    YoutubeVideo video = youtubeVideoRepository.findById(requestDTO.getId())
        .orElseThrow(
            () -> new RuntimeException("영상을 찾을 수 없습니다. id: " + requestDTO.getId()));

    video.changeTitle(requestDTO.getTitle());
    video.changeDescription(requestDTO.getDescription());
    video.changeThumbnailUrl(requestDTO.getThumbnailUrl());
    video.changeChannelTitle(requestDTO.getChannelTitle());
    video.changePublishedAt(requestDTO.getPublishedAt());
    video.changeIsActive(requestDTO.getIsActive());
    video.changeSortOrder(requestDTO.getSortOrder());

    return convertToResponseDTO(video);
  }

  private YoutubeVideoResponseDTO convertToResponseDTO(YoutubeVideo video) {
    return YoutubeVideoResponseDTO.builder()
        .id(video.getId())
        .videoId(video.getVideoId())
        .title(video.getTitle())
        .description(video.getDescription())
        .thumbnailUrl(video.getThumbnailUrl())
        .channelTitle(video.getChannelTitle())
        .publishedAt(video.getPublishedAt())
        .isActive(video.getIsActive())
        .sortOrder(video.getSortOrder())
        .insertTime(video.getInsertTime() != null ? video.getInsertTime().toString() : null)
        .build();
  }
}
