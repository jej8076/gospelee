package com.gospelee.api.dto.article;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrawlArticleRequestDTO {

  private String category;

}

