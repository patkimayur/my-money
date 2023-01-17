package com.mymoney.tracker.service.contract;

import com.mymoney.tracker.exception.MyMoneyCheckedException;

import java.time.Month;
import java.util.List;

public interface MyMoneyCommandService {
  Boolean allocate(List<Double> allocationValues) throws MyMoneyCheckedException;

  Boolean monthlySIP(List<Double> sips) throws MyMoneyCheckedException;

  void monthlyChangeRate(List<Double> changeRates, Month month) throws MyMoneyCheckedException;

  String getBalance(Month month);

  String rebalance();

}
