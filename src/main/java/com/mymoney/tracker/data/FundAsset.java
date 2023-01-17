package com.mymoney.tracker.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Fund assets defining the asset type and amount
 * @author mayur
 */
@Data
@AllArgsConstructor
public class FundAsset {
  private AssetType assetType;
  private Double amount;
}
