package com.mymoney.tracker.service.impl;

import com.mymoney.tracker.dao.MyMoneyDbStub;
import com.mymoney.tracker.data.AssetType;
import com.mymoney.tracker.exception.MyMoneyCheckedException;
import com.mymoney.tracker.exception.MyMoneyRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import static com.mymoney.tracker.data.AssetType.DEBT;
import static com.mymoney.tracker.data.AssetType.EQUITY;
import static com.mymoney.tracker.data.AssetType.GOLD;
import static com.mymoney.tracker.service.impl.MyMoneyCommandServiceImpl.CANNOT_REBALANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MyMoneyCommandServiceImplTest {

  @Spy
  private MyMoneyDbStub myMoneyDbStub;

  @Spy
  private MyMoneyCommandHelper myMoneyCommandHelper;

  @InjectMocks
  private MyMoneyCommandServiceImpl myMoneyCommandService;

  @Before
  public void setUp() {
    Set<AssetType> assetTypes = new HashSet<>();
    assetTypes.add(EQUITY);
    assetTypes.add(DEBT);
    assetTypes.add(GOLD);
    Mockito.when(myMoneyDbStub.getAssetSequence())
           .thenReturn(assetTypes);
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testAllocateNull() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.allocate(null);
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input values provided for allocation");
      throw e;
    }
  }

  @Test
  public void testAllocateCorrectValues() throws MyMoneyCheckedException {
    List<Double> initialAllocation = Arrays.asList(75d, 10d, 15d);
    myMoneyCommandService.allocate(initialAllocation);
    assertEquals(initialAllocation.stream()
                                  .mapToDouble(Double::doubleValue)
                                  .sum(), myMoneyDbStub.initialAllocation.getTotalAmount());
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testAllocateWrongValues() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.allocate(Arrays.asList(5d, 26d, 32d, 37d));
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input values provided for allocation");
      throw e;
    }
  }

  @Test(expected = MyMoneyCheckedException.class)
  public void testAllocateAlreadyAllocated() throws MyMoneyCheckedException {
    List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
    myMoneyCommandService.allocate(initialAllocation);

    try {
      myMoneyCommandService.allocate(initialAllocation);
    } catch (MyMoneyCheckedException e) {
      assertEquals("The funds are already allocated", e.getMessage());
      throw e;
    }
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testSipWithNullValues() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.monthlySIP(null);
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input values provided for allocation");
      throw e;
    }

  }

  @Test
  public void testSip() throws MyMoneyCheckedException {
    List<Double> sipAmounts = Arrays.asList(30d, 40d, 60d);
    myMoneyCommandService.monthlySIP(sipAmounts);
    assertEquals(sipAmounts.stream()
                           .mapToDouble(Double::doubleValue)
                           .sum(), myMoneyDbStub.initialSip.getTotalAmount());
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testSipWithWrongValues() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.monthlySIP(Arrays.asList(5d, 26d, 32d, 37d));
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input values provided for allocation");
      throw e;
    }
  }

  @Test(expected = MyMoneyCheckedException.class)
  public void testSipAlreadyAllocated() throws MyMoneyCheckedException {

    List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
    myMoneyCommandService.monthlySIP(initialAllocation);

    try {
      myMoneyCommandService.monthlySIP(initialAllocation);
    } catch (MyMoneyCheckedException e) {
      assertEquals("The SIP has been registered already", e.getMessage());
      throw e;
    }
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testChangeWithNullValues() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.monthlyChangeRate(null, Month.SEPTEMBER);
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input provided for change or month");
      throw e;
    }
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testChangeWithWrongValues() throws MyMoneyCheckedException {
    try {
      myMoneyCommandService.monthlyChangeRate(Arrays.asList(5d, 26d, 32d, 37d), Month.SEPTEMBER);
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Incorrect input values provided for monthly change rate");
      throw e;
    }
  }

  @Test
  public void testChange() throws MyMoneyCheckedException {
    List<Double> changeRate = Arrays.asList(10d, 20d, 30d);
    myMoneyCommandService.monthlyChangeRate(changeRate, Month.SEPTEMBER);
    assertEquals(changeRate.size(), myMoneyDbStub.monthlyMarketChangeRate.get(Month.SEPTEMBER)
                                                                         .size());
  }

  @Test(expected = MyMoneyCheckedException.class)
  public void testChangeAlreadyAllocatedForMonth() throws MyMoneyCheckedException {
    List<Double> changeRate = Arrays.asList(10d, 20d, 30d);
    myMoneyCommandService.monthlyChangeRate(changeRate, Month.SEPTEMBER);

    try {
      myMoneyCommandService.monthlyChangeRate(changeRate, Month.SEPTEMBER);
    } catch (MyMoneyCheckedException e) {
      assertEquals("the month of SEPTEMBER is already registered", e.getMessage());
      throw e;
    }
  }

  @Test(expected = MyMoneyRuntimeException.class)
  public void testBalanceException() throws MyMoneyCheckedException {
    myMoneyCommandService.allocate(Arrays.asList(100d, 200d, 300d));

    try {
      myMoneyCommandService.getBalance(Month.SEPTEMBER);
    } catch (MyMoneyRuntimeException e) {
      assertEquals(e.getMessage(), "Rate of Change is not set");
      throw e;
    }
  }

  @Test
  public void testBalance() throws DataFormatException, MyMoneyCheckedException {
    myMoneyCommandService.allocate(Arrays.asList(60d, 100d, 50d));
    myMoneyCommandService.monthlySIP(Arrays.asList(500d, 100d, 50d));
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(4d, 8d, 2d), Month.JANUARY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(-8d, 30d, 5d), Month.FEBRUARY);
    assertEquals("517 270 106", myMoneyCommandService.getBalance(Month.FEBRUARY));
  }

  @Test
  public void testBalanceWithDiffInput() throws DataFormatException, MyMoneyCheckedException {
    myMoneyCommandService.allocate(Arrays.asList(6000d, 3000d, 1000d));
    myMoneyCommandService.monthlySIP(Arrays.asList(2000d, 1000d, 500d));
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(4d, 10d, 2d), Month.JANUARY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(-10.00d, 40.00d, 0.00d), Month.FEBRUARY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(12.50d, 12.50d, 12.50d), Month.MARCH);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(8.00d, -3.00d, 7.00d), Month.APRIL);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(13.00d, 21.00d, 10.50d), Month.MAY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(10.00d, 8.00d, -5.00d), Month.JUNE);
    assertEquals("10593 7897 2272", myMoneyCommandService.getBalance(Month.MARCH));
  }


  @Test
  public void testRebalance() throws MyMoneyCheckedException {
    myMoneyCommandService.allocate(Arrays.asList(6000d, 3000d, 1000d));
    myMoneyCommandService.monthlySIP(Arrays.asList(2000d, 1000d, 500d));
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(4d, 10d, 2d), Month.JANUARY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(-10d, 40d, 0d), Month.FEBRUARY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(12.50d, 12.50d, 12.50d), Month.MARCH);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(8d, -3d, 7d), Month.APRIL);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(13d, 21d, 10.50d), Month.MAY);
    myMoneyCommandService.monthlyChangeRate(Arrays.asList(10d, 8d, -5d), Month.JUNE);
    assertEquals("23619 11809 3936", myMoneyCommandService.rebalance());
  }

    @Test
    public void testRebalanceWithWrongInputs() throws MyMoneyCheckedException {
      myMoneyCommandService.allocate(Arrays.asList(6000d, 3000d, 1000d));
      myMoneyCommandService.monthlySIP(Arrays.asList(2000d, 1000d, 500d));
      myMoneyCommandService.monthlyChangeRate(Arrays.asList(4d, 10d, 2d), Month.JANUARY);
      String result = myMoneyCommandService.rebalance();
      assertEquals(CANNOT_REBALANCE, result);
    }


}