package com.huylq.thaotool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  public void sendEmail(String path) {
    log.info("Sending email to thaodp@gmail.com");
  }

}
