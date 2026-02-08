package com.gospelee.api.service;

import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import com.gospelee.api.entity.YoutubeVideo;
import com.gospelee.api.repository.jpa.youtube.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeVideoServiceImpl implements YoutubeVideoService {

    private final YoutubeVideoRepository youtubeVideoRepository;

    @Override
    public List<YoutubeVideoResponseDTO> getActiveVideos() {
        List<YoutubeVideo> videos = youtubeVideoRepository.findActiveVideosOrderBySortOrder();
        
        return videos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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
                .sortOrder(video.getSortOrder())
                .build();
    }
}
