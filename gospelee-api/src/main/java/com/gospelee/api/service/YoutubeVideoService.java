package com.gospelee.api.service;

import com.gospelee.api.dto.youtube.YoutubeVideoRequestDTO;
import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import java.util.List;

public interface YoutubeVideoService {

  List<YoutubeVideoResponseDTO> getActiveVideos();

  List<YoutubeVideoResponseDTO> getAllVideos();

  YoutubeVideoResponseDTO getVideoById(Long id);

  YoutubeVideoResponseDTO createVideo(YoutubeVideoRequestDTO requestDTO);

  YoutubeVideoResponseDTO updateVideo(YoutubeVideoRequestDTO requestDTO);
}
