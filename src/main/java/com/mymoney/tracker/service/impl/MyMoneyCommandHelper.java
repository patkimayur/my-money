package com.mymoney.tracker.service.impl;

import com.mymoney.tracker.data.AssetType;
import com.mymoney.tracker.data.FundAsset;
import com.mymoney.tracker.data.Portfolio;
import com.mymoney.tracker.exception.MyMoneyCheckedException;
import com.mymoney.tracker.exception.MyMoneyRuntimeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Component
public class MyMoneyCommandHelper {


  public static Portfolio createInitialAllocation(List<Double> allocationValues, Set<AssetType> assetSequence) throws MyMoneyCheckedException {
    if (Objects.isNull(allocationValues) || allocationValues.size() != assetSequence.size()) {
      throw new MyMoneyRuntimeException("Incorrect input values provided for allocation");
    }


    List<AssetType> assetSequenceList = new ArrayList();
    assetSequenceList.addAll(assetSequence);
    List<FundAsset> fundAssets = IntStream.range(0, assetSequenceList.size())
             .mapToObj(i -> new FundAsset(assetSequenceList.get(i), allocationValues.get(i)))
                                          .collect(Collectors.toList());

    return new Portfolio(fundAssets);
  }

  public List<Double> getDoubleValFromString(String[] commandAndInputs, int limit) {
    return Arrays.stream(commandAndInputs).skip(1)
                 .limit(limit)
                 .map(Double::parseDouble)
                 .collect(Collectors.toList());
  }

  /**
   * Calculates the total balance after applying the sip & market change
   *
   * @param previousPortfolio
   * @param sip
   * @param changeRate
   * @return
   */
  public Portfolio getPortfolio(Portfolio previousPortfolio, Portfolio sip, Map<AssetType,Double> changeRate) {

    List<FundAsset> funds = previousPortfolio.getFundAssets();
    //for each fund asset in portfolio add the SIP amount
    if (Objects.nonNull(sip)) {
      IntStream.range(0, funds.size())
               .forEach(
                   index -> {
                     FundAsset fundAsset = funds.get(index);
                     double sipAmount = sip.getFundAssets().get(index).getAmount();
                     fundAsset.setAmount(Math.floor(fundAsset.getAmount() + sipAmount));
                   });
    }

    //for each fund on the sip amount apply the change rate
    funds.forEach(
        fundAsset -> {
          double rate = changeRate.get(fundAsset.getAssetType());
          double updatedAmount = fundAsset.getAmount() * (1 + rate / 100);
          fundAsset.setAmount(Math.floor(updatedAmount));
        });

    return previousPortfolio;
    }
}
