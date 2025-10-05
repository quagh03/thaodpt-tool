package com.huylq.thaotool.controller;

import com.huylq.thaotool.model.Article;
import com.huylq.thaotool.service.ScraperService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class ScraperController {

  ScraperService scraperService;

  @PostMapping("/scrape")
  public List<Article> getHeadlines() {
    return scraperService.scrapeArticle();
  }
}
