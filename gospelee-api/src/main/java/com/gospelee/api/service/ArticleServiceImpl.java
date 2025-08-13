package com.gospelee.api.service;

import com.gospelee.api.dto.article.CrawlArticleDTO;
import com.gospelee.api.enums.ArticleCategoryType;
import com.gospelee.api.repository.jpa.article.ArticleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;

  public List<CrawlArticleDTO> getAll() {
    return articleRepository.findAll().stream()
        .map(CrawlArticleDTO::fromEntity)
        .toList();
  }

  @Override
  public List<CrawlArticleDTO> getArticleList(String category) {
    ArticleCategoryType articleCategoryType = ArticleCategoryType.fromName(category);
    return articleRepository.findByCategoryAndOrderNumIsNotNullOrderByOrderNumAsc(
            articleCategoryType.name()).stream()
        .map(CrawlArticleDTO::fromEntity)
        .toList();
  }


}


