package com.mymoney.tracker.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Global handler to handle all unhandled exceptions
 */
@Log4j2
@Component
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

  public void uncaughtException(Thread t, Throwable e) {
    log.error("Unhandled exception caught: {}", e);
  }
}
