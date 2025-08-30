package com.gospelee.api.service;

import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import java.util.List;

public interface YoutubeVideoService {
    List<YoutubeVideoResponseDTO> getActiveVideos();
}
