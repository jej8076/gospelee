package com.gospelee.api.service;

import com.gospelee.api.dto.article.CrawlArticleDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import java.util.List;

public interface ArticleService {

  List<CrawlArticleDTO> getAll();

  List<CrawlArticleDTO> getArticleList(String catetory);

}
