package com.huylq.thaotool.service;

import com.huylq.thaotool.model.Article;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ScraperService {

  String VNEXPRESS_HOME_URL = "https://vnexpress.net/suc-khoe";
  int NUMBER_OF_ARTICLES = 10;

  FileService fileService;
  AIService AIService;
  EmailService emailService;

  private final ExecutorService executorService = Executors.newFixedThreadPool(4);

  public List<Article> scrapeArticle() {
    Set<String> urls = new HashSet<>();
    List<Article> articles = new ArrayList<>();
    String htmlFilePath = "";

    try {
      log.info("Starting scrape from homepage: {}", VNEXPRESS_HOME_URL);
      Document doc = Jsoup.connect(VNEXPRESS_HOME_URL).get();
      Elements titleElements = doc.select("h3.title-news");

      List<String> hrefs = titleElements.stream()
          .map(e -> e.select("a").attr("href"))
          .distinct()
          .collect(Collectors.toList());

      Random random = new Random();
      log.info("Total {} articles found on homepage.", hrefs.size());

      Collections.shuffle(hrefs, random);
      while (urls.size() < NUMBER_OF_ARTICLES && !hrefs.isEmpty()) {
        urls.add(hrefs.remove(0));
      }
      log.info("Selected {} unique article URLs.", urls.size());

    } catch (IOException e) {
      log.error("Error fetching homepage: ", e);
    }

    List<Callable<Article>> tasks = new ArrayList<>();
    for (String url : urls) {
      tasks.add(() -> scrapeSingleArticle(url));
    }

    try {
      List<Future<Article>> results = executorService.invokeAll(tasks);
      for (Future<Article> result : results) {
        try {
          articles.add(result.get());
        } catch (ExecutionException e) {
          log.error("Error scraping article: ", e);
        }
      }
    } catch (InterruptedException e) {
      log.error("Error executing article scraping tasks: ", e);
    }

    try {
      htmlFilePath = fileService.saveArticleAsHtml(articles);
      log.info("Successfully saved {} articles as HTML.", articles.size());
    } catch (Exception e) {
      log.error("Error saving articles to HTML file: ", e);
    }

    emailService.sendEmail(htmlFilePath);

    return articles;
  }

  private Article scrapeSingleArticle(String url) {
    try {
      log.info("Scraping article: {}", url);
      Document articleDoc = Jsoup.connect(url).get();
      String title = articleDoc.title();
      String description = scrapeDescription(articleDoc);
      String content = scrapeContent(articleDoc);
      content = AIService.rewriteText(content).replaceAll("(?s)<think>.*?</think>", "");

      log.info("Successfully scraped article: {}", title);
      return new Article(url, title, description, content);

    } catch (IOException e) {
      log.error("Error scraping article {}: ", url, e);
      return null;
    }
  }

  private String scrapeContent(Document doc) {
    log.debug("Scraping content from article...");
    return doc.select("p.Normal").stream()
        .map(Element::text)
        .collect(Collectors.joining("</br>"))
        .replace("\"", "");
  }

  private String scrapeDescription(Document doc) {
    log.debug("Scraping description from article...");
    Element descriptionElement = doc.select("p.description").first();
    return descriptionElement != null ? descriptionElement.text() : "Mô tả không có sẵn";
  }
}
