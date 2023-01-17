package com.mymoney.tracker.service.impl;

import com.mymoney.tracker.dao.MyMoneyDbStub;
import com.mymoney.tracker.data.AssetType;
import com.mymoney.tracker.exception.MyMoneyCheckedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mymoney.tracker.data.AssetType.DEBT;
import static com.mymoney.tracker.data.AssetType.EQUITY;
import static com.mymoney.tracker.data.AssetType.GOLD;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MyMoneyProcessorTest {

  @Spy
  private MyMoneyDbStub myMoneyDbStub;

  @Spy
  private MyMoneyCommandHelper myMoneyCommandHelper;

  @Spy
  private MyMoneyCommandServiceImpl myMoneyCommandService;

  @InjectMocks MyMoneyProcessor myMoneyProcessor;

  @Before
  public void setUp() throws Exception {
    Set<AssetType> assetTypes = new HashSet<>();
    assetTypes.add(EQUITY);
    assetTypes.add(DEBT);
    assetTypes.add(GOLD);
    Mockito.when(myMoneyDbStub.getAssetSequence())
           .thenReturn(assetTypes);

    myMoneyCommandService = new MyMoneyCommandServiceImpl(myMoneyDbStub, myMoneyCommandHelper);
    myMoneyProcessor = new MyMoneyProcessor(myMoneyDbStub,myMoneyCommandService, myMoneyCommandHelper);

  }

  @Test(expected = MyMoneyCheckedException.class)
  public void processCommandsFromFileWithInvalidInput() throws MyMoneyCheckedException {
    try {
      myMoneyProcessor.processCommandsFromFile("invalidFileName");
    }catch (MyMoneyCheckedException e){
      assertEquals("Unable to process from the provided file with filename invalidFileName", e.getMessage());
      throw e;
    }
  }

  @Test
  public void processCommandsFromFile() throws MyMoneyCheckedException, IOException {

    List<String> output = myMoneyProcessor.processCommandsFromFile(this.getClass().getClassLoader().getResource("inputFile.txt").getFile());
    Stream<String> expectedOutputStream = Files.lines(Paths.get(this.getClass().getClassLoader().getResource("outputFile.txt").getFile()));

    List<String> expectedOutput = expectedOutputStream.collect(Collectors.toList());

    List<String> matchList = output.stream()
                                      .filter(expectedOutput::contains)
                                      .collect(Collectors.toList());

    assertEquals(matchList.size(),output.size());
  }
}