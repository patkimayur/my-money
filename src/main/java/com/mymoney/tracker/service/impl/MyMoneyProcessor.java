package com.mymoney.tracker.service.impl;

import com.mymoney.tracker.dao.MyMoneyDbStub;
import com.mymoney.tracker.data.Command;
import com.mymoney.tracker.exception.MyMoneyCheckedException;
import com.mymoney.tracker.service.contract.MyMoneyCommandService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mayur
 * Class to process text file and commands
 */
@Service
@Log4j2
@AllArgsConstructor
public class MyMoneyProcessor {

  @Autowired
  private MyMoneyDbStub myMoneyDbStub;

  @Autowired
  private MyMoneyCommandService myMoneyCommandService;

  private MyMoneyCommandHelper myMoneyCommandHelper;

  /**
   * Used to read file line by line and execute commands provided in the file
   *
   * @param filename
   */
  public List<String> processCommandsFromFile(String filename) throws MyMoneyCheckedException {
    try (Stream<String> lines = Files.lines(Paths.get(filename))) {
      List<String> outputLines = lines.filter(l -> !l.trim()
                                                     .isEmpty())
                                      .map(line -> processCommand(line))
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());
      outputLines.stream().filter(Objects::nonNull).forEach(o -> System.out.println(o));
      return outputLines;
    } catch (IOException ex) {
      log.error("Unable to process from the provided file with filename: {}", filename);
      throw new MyMoneyCheckedException(ex, "Unable to process from the provided file with filename " + filename);
    }

  }

  private void displayOutput(List<String> outputLines) {
  }

  private String processCommand(String line) {
    String output = null;
    String[] commandAndInputs = line.split(" ");
    try {
      Command command = Command.valueOf(commandAndInputs[0]);
      int assetSeqSize = myMoneyDbStub.getAssetSequence().size();
      switch (command) {
        case ALLOCATE:
          validateInputs(commandAndInputs,  assetSeqSize + 1);
          List<Double> allocations = myMoneyCommandHelper.getDoubleValFromString(commandAndInputs, myMoneyDbStub.getAssetSequence().size());
          myMoneyCommandService.allocate(allocations);
          break;
        case SIP:
          validateInputs(commandAndInputs, assetSeqSize + 1);
          List<Double> sips = myMoneyCommandHelper.getDoubleValFromString(commandAndInputs, assetSeqSize);
          myMoneyCommandService.monthlySIP(sips);
          break;
        case CHANGE:
          validateInputs(commandAndInputs, assetSeqSize + 2);
          List<String> changeStr = Arrays
                                       .stream(commandAndInputs)
                                       .map(str -> str.replace("%", ""))
                                       .collect(Collectors.toList());;
          List <Double> rates = myMoneyCommandHelper.getDoubleValFromString(changeStr.toArray(new String[0]), assetSeqSize);
          Month month = Month.valueOf(commandAndInputs[assetSeqSize + 1]);
          myMoneyCommandService.monthlyChangeRate(rates, month);
          break;
        case BALANCE:
          validateInputs(commandAndInputs, 2);
          month = Month.valueOf(commandAndInputs[1]);
          output = myMoneyCommandService.getBalance(month);
          break;
        case REBALANCE:
          output = myMoneyCommandService.rebalance();
          break;
        default:
          throw new MyMoneyCheckedException("Invalid command provided " + command);
      }
    } catch (MyMoneyCheckedException e) {
      log.error("Command processing failed with exception", e);
    }
    return output;
  }

  private void validateInputs(String[] commandAndInputs, int size) throws MyMoneyCheckedException {
    if (commandAndInputs.length != size) {
      throw new MyMoneyCheckedException("The input provided is incorrect");
    }
  }
}