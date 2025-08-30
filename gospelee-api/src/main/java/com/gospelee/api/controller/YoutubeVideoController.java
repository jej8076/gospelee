package com.gospelee.api.controller;

import com.gospelee.api.dto.youtube.YoutubeVideoResponseDTO;
import com.gospelee.api.service.YoutubeVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeVideoService youtubeVideoService;

    @PostMapping("/videos")
    public ResponseEntity<List<YoutubeVideoResponseDTO>> getActiveVideos() {
        List<YoutubeVideoResponseDTO> videos = youtubeVideoService.getActiveVideos();
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }
}
