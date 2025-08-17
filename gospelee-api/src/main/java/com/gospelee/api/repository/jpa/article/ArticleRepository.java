package com.gospelee.api.repository.jpa.article;

import com.gospelee.api.entity.CrawlArticle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<CrawlArticle, Long> {

  List<CrawlArticle> findByCategoryAndOrderNumIsNotNullOrderByOrderNumAsc(String category);
}
