package com.huylq.thaotool.service;

import com.huylq.thaotool.model.Article;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class FileService {

  public String saveArticleAsHtml(List<Article> articles) {
    long time = new Date().getTime();
    String fileName = "article_" + time + ".html";

    File directory = new File("articles/"+ time);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    File file = new File(directory, fileName);

    try (FileWriter writer = new FileWriter(file)) {
      StringBuilder htmlContent = new StringBuilder("<html>\n<head>\n<title>" + time + "</title>\n</head>\n<body>\n");

      for (Article article : articles) {
        htmlContent.append(String.format(
            "<a href=\"%s\" target=\"_blank\"><h1>%s</h1></a>\n",
            article.getUrl(), article.getTitle()
        ));
        htmlContent.append("<h3>").append(article.getDescription()).append("</h3>\n");
        htmlContent.append(article.getContent()).append("\n");
      }

      htmlContent.append("</body>\n</html>");

      writer.write(htmlContent.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return file.exists() ? file.getPath() : null;

  }

}
