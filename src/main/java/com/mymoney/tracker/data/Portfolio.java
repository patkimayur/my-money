package com.mymoney.tracker.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Portfolio which includes different types of funds allocated
 * @author mayur
 */
@Data
@AllArgsConstructor
public class Portfolio {
  private List<FundAsset> fundAssets;


  public Double getTotalAmount(){
    Double totalInvestment = 0d;
    for(FundAsset fundAsset: fundAssets){
      totalInvestment+=fundAsset.getAmount().doubleValue();
    }
    return totalInvestment;
  }

  @Override
  public Portfolio clone() {
    return new Portfolio(
        fundAssets.stream()
             .map(fundAsset -> new FundAsset(fundAsset.getAssetType(), fundAsset.getAmount()))
             .collect(Collectors.toList()));
  }

  @Override
  public String toString() {
    return fundAssets.stream()
                .map(fundAsset -> Integer.toString((int) Math.floor(fundAsset.getAmount())))
                .collect(Collectors.joining(" "));
  }
}
