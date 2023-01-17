package com.mymoney.tracker.dao;


import com.mymoney.tracker.data.AssetType;
import com.mymoney.tracker.data.Portfolio;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * DB stub to provide data information
 * @author mayur
 */
@Component
@Data
public class MyMoneyDbStub {

  /**
   * Setup data for assets
   */
  @PostConstruct
  public void setUpData(){
    assetSequence.add(AssetType.EQUITY);
    assetSequence.add(AssetType.DEBT);
    assetSequence.add(AssetType.GOLD);
  }


  public Set<AssetType> assetSequence;
  public Portfolio initialAllocation;
  public Portfolio initialSip;
  public Map<AssetType, Double> weights = new HashMap<>();
  public TreeMap<Month, Map<AssetType, Double>> monthlyMarketChangeRate = new TreeMap<>();
  public TreeMap<Month, Portfolio> monthlyBalance = new TreeMap<>();


  public MyMoneyDbStub() {
    assetSequence = new HashSet<>();
  }
}
