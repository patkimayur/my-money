package com.mymoney.tracker.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

/**
 * Runtime exception class to handle runtime exception
 */
@Data
@NoArgsConstructor
@Log4j2
public class MyMoneyRuntimeException extends RuntimeException {

  public MyMoneyRuntimeException(RuntimeException ex, String message, Object ... vars) {
    super(message,ex);
    log.error("Runtime exception occurred with message: {}", message);
    log.error(ex.getMessage(), ex, vars);
  }

  public MyMoneyRuntimeException(String message) {
    super(message);
    log.error("Runtime exception occurred with message: {}", message);
  }
}
