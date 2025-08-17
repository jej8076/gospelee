package com.gospelee.api.controller;

import com.gospelee.api.dto.article.CrawlArticleDTO;
import com.gospelee.api.dto.article.CrawlArticleRequestDTO;
import com.gospelee.api.service.ArticleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/article")
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping
  public ResponseEntity<Object> getJournalByAccountUid(
      @RequestBody CrawlArticleRequestDTO crawlArticleRequestDTO) {
    List<CrawlArticleDTO> getJournalByAccountUid = articleService.getArticleList(
        crawlArticleRequestDTO.getCategory());
    return new ResponseEntity<>(getJournalByAccountUid, HttpStatus.OK);
  }

}
