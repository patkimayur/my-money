package com.mymoney.tracker.service.impl;

import com.mymoney.tracker.dao.MyMoneyDbStub;
import com.mymoney.tracker.data.AssetType;
import com.mymoney.tracker.data.FundAsset;
import com.mymoney.tracker.data.Portfolio;
import com.mymoney.tracker.exception.MyMoneyCheckedException;
import com.mymoney.tracker.exception.MyMoneyRuntimeException;
import com.mymoney.tracker.service.contract.MyMoneyCommandService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@AllArgsConstructor
@NoArgsConstructor
@Service
public class MyMoneyCommandServiceImpl implements MyMoneyCommandService {

  @Autowired
  private MyMoneyDbStub myMoneyDbStub;

  @Autowired
  private MyMoneyCommandHelper myMoneyCommandHelper;

  public static final String CANNOT_REBALANCE = "CANNOT_REBALANCE";

  @Override
  public Boolean allocate(List<Double> allocationValues) throws MyMoneyCheckedException {
    if (Objects.nonNull(myMoneyDbStub.getInitialAllocation())) {
      throw new MyMoneyCheckedException("The funds are already allocated");
    }
    myMoneyDbStub.initialAllocation = myMoneyCommandHelper.createInitialAllocation(allocationValues, myMoneyDbStub.getAssetSequence());
    myMoneyDbStub.weights = myMoneyDbStub.getInitialAllocation().getFundAssets().stream()
                                         .collect(
                                             Collectors.toMap(
                                                 FundAsset::getAssetType,
                                                 e -> e.getAmount() * 100 / myMoneyDbStub.initialAllocation.getTotalAmount()));

    return true;
  }

  @Override
  public Boolean monthlySIP(List<Double> sips) throws MyMoneyCheckedException {
    if (Objects.nonNull(myMoneyDbStub.initialSip)) {
      throw new MyMoneyCheckedException("The SIP has been registered already");
    }

    myMoneyDbStub.initialSip = myMoneyCommandHelper.createInitialAllocation(sips,myMoneyDbStub.getAssetSequence());
    return true;
  }

  @Override
  public void monthlyChangeRate(List<Double> changeRates, Month month) throws MyMoneyCheckedException {
    if (Objects.nonNull(myMoneyDbStub.getMonthlyMarketChangeRate().getOrDefault(month, null))) {
      throw new MyMoneyCheckedException("the month of " + month + " is already registered");
    }

    if (Objects.isNull(changeRates) || Objects.isNull(month)) {
      throw new MyMoneyRuntimeException("Incorrect input provided for change or month");
    }

    if (changeRates.size() != myMoneyDbStub.getAssetSequence().size()) {
      throw new MyMoneyRuntimeException("Incorrect input values provided for monthly change rate");
    }

    List<AssetType> assetSequenceList = new ArrayList();
    assetSequenceList.addAll(myMoneyDbStub.getAssetSequence());
    Map<AssetType, Double> change = new HashMap<>();
    IntStream.range(0, assetSequenceList.size())
                                          .mapToObj(i -> change.put(assetSequenceList.get(i), changeRates.get(i)))
                                          .collect(Collectors.toList());

    myMoneyDbStub.monthlyMarketChangeRate.put(month,change);
  }

  @Override
  public String getBalance(Month month) {
    updateBalance();

    Portfolio portfolio = myMoneyDbStub.getMonthlyBalance().get(month);

    return portfolio.toString();
  }

  private void updateBalance() {
    Map.Entry<Month, Portfolio> lastbalance =
        myMoneyDbStub.getMonthlyBalance().lastEntry();
    Map.Entry<Month, Map<AssetType, Double>> lastChange =
        myMoneyDbStub.getMonthlyMarketChangeRate().lastEntry();
    if (Objects.isNull(lastChange)) {
      throw new MyMoneyRuntimeException("Rate of Change is not set");
    }

    if (Objects.isNull(lastbalance)) {
      Portfolio portfolio =
          myMoneyCommandHelper.getPortfolio(
              myMoneyDbStub.getInitialAllocation(),
              null,
              myMoneyDbStub.getMonthlyMarketChangeRate().get(Month.JANUARY));

      myMoneyDbStub.getMonthlyBalance().put(Month.JANUARY, portfolio);
      lastbalance = myMoneyDbStub.getMonthlyBalance().lastEntry();
    }

    log.debug("Calculating balances for rest of the months");
    //pending change rates need to be calculated
    if (lastbalance.getKey() != lastChange.getKey()) {
      Month initialMonth = lastbalance.getKey();
      Month finalMonth = lastChange.getKey();
      for (int index = initialMonth.getValue(); index < finalMonth.getValue(); index++) {
        Month lastUpdatedMonth = Month.of(index);
        Month currentMonth = Month.of(index + 1);
        Portfolio lastPortfolio =
            myMoneyDbStub.getMonthlyBalance().get(lastUpdatedMonth).clone();
        Map<AssetType, Double> changeRate =
            myMoneyDbStub.getMonthlyMarketChangeRate().get(currentMonth);
        Portfolio newPortfolio =
            myMoneyCommandHelper.getPortfolio(lastPortfolio, myMoneyDbStub.getInitialSip(), changeRate);

        //as the rebalancing happens only in month of june or dec check if rebalance is required
        if (currentMonth.equals(Month.JUNE) || currentMonth.equals(Month.DECEMBER)) {
          List<FundAsset> fundAssets = newPortfolio.getFundAssets();
          double totalInvestment = newPortfolio.getTotalAmount();
          fundAssets.forEach(
              fundAsset -> {
                double desiredWeight = myMoneyDbStub.getWeights().get(fundAsset.getAssetType());
                fundAsset.setAmount(Math.floor(totalInvestment * desiredWeight / 100));
              });
        }
        myMoneyDbStub.getMonthlyBalance().putIfAbsent(currentMonth, newPortfolio);
      }
    }
  }

  @Override
  public String rebalance() {
    updateBalance();
    Month lastUpdatedMonth = myMoneyDbStub.getMonthlyBalance().lastEntry().getKey();
    Month rebalancedMonth = lastUpdatedMonth == Month.DECEMBER ? lastUpdatedMonth : Month.JUNE;
    Portfolio portfolio = myMoneyDbStub.getMonthlyBalance().getOrDefault(rebalancedMonth, null);
    return Objects.nonNull(portfolio) ? portfolio.toString() : CANNOT_REBALANCE;
  }
}
