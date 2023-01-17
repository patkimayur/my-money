package com.mymoney.tracker;


import com.mymoney.tracker.exception.GlobalExceptionHandler;
import com.mymoney.tracker.service.impl.MyMoneyProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.InputMismatchException;


/**
 * @author mayur
 * Application class to initiate the flow
 */
@SpringBootApplication
@Log4j2
public class MyMoneyPortfolioTrackerApplication implements CommandLineRunner {

  @Autowired
  private GlobalExceptionHandler globalExceptionHandler;

  @Autowired
  private MyMoneyProcessor myMoneyProcessor;

  public static void main(String[] args) {
    SpringApplication.run(MyMoneyPortfolioTrackerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);

    if (args.length != 1) {
      log.error("No input arguments were supplied");
      throw new InputMismatchException("Please specify input file");
    }

    log.info("One argument passed: {}", args[0]);
    myMoneyProcessor.processCommandsFromFile(args[0]);
    System.exit(0);
  }
}
