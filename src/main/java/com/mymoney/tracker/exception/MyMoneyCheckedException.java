package com.mymoney.tracker.exception;

import lombok.Data;
import lombok.extern.log4j.Log4j2;


/**
 * Checked exception class to handle expected checked exception
 * @author mayur
 */
@Data
@Log4j2
public class MyMoneyCheckedException extends Exception {

  public MyMoneyCheckedException(String message){
    super(message);
    log.error("Checked exception occurred with message: {}", message);
  }

  public MyMoneyCheckedException(Exception ex, String message, Object ... vars) {
    super(message,ex);
    log.error("Checked exception occurred with message: {}", message);
    log.error(ex.getMessage(), ex, vars);
  }
}
