package com.bourse.wealthwise.domain.action;

import com.bourse.wealthwise.domain.entity.account.User;
import com.bourse.wealthwise.domain.entity.action.ActionType;
import com.bourse.wealthwise.domain.entity.action.Buy;
import com.bourse.wealthwise.domain.entity.action.Sale;
import com.bourse.wealthwise.domain.entity.action.StockRightUsage;
import com.bourse.wealthwise.domain.entity.balance.BalanceChange;
import com.bourse.wealthwise.domain.entity.holding.Holding;
import com.bourse.wealthwise.domain.entity.portfolio.Portfolio;
import com.bourse.wealthwise.domain.entity.security.Security;
import com.bourse.wealthwise.domain.entity.security.SecurityChange;
import com.bourse.wealthwise.domain.entity.security.SecurityType;
import com.bourse.wealthwise.domain.services.HoldingService;
import com.bourse.wealthwise.repository.ActionRepository;
import com.bourse.wealthwise.repository.PortfolioRepository;
import com.bourse.wealthwise.repository.SecurityPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StockRightUsageTest {

    @Autowired
    private HoldingService holdingService;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private SecurityPriceRepository securityPriceRepository;
    @Autowired
    private ActionRepository actionRepository;

    private Portfolio portfolio;
    private Security stockRight;
    private Security option;
    private StockRightUsage stockRightUsage;

    @BeforeEach
    public void setUp() {
        User user = User.builder().build();
        this.portfolio = new Portfolio("21e42b92-cef6-453f-9e52-fa76b1d830f6",
                user,
                "portfo");

        portfolioRepository.save(portfolio);

        stockRight = Security.builder()
                .symbol("HABC")
                .isin("HISIN-ABC")
                .securityType(SecurityType.STOCK_RIGHT)
                .build();

        option = Security.builder()
                .symbol("O-ABC")
                .isin("ISIN-O-ABC")
                .securityType(SecurityType.OPTION)
                .build();

        Buy stockRightBuy = Buy.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now().minusMinutes(1))
                .volume(BigInteger.valueOf(10))
                .price(100)
                .totalValue(BigInteger.valueOf(1000))
                .security(stockRight)
                .actionType(ActionType.BUY)
                .build();
        actionRepository.save(stockRightBuy);

        stockRightUsage = StockRightUsage.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now().minusSeconds(1))
                .stockRight(stockRight)
                .option(option)
                .amount(BigInteger.valueOf(5))
                .build();
        actionRepository.save(stockRightUsage);
    }

    @Test
    public void buyStockRights_stockRightUsage_createsCorrectSecurityChanges() {
        List<SecurityChange> changes = stockRightUsage.getSecurityChanges();

        SecurityChange stockChange = changes.stream()
                .filter(c -> c.getSecurity().equals(stockRight))
                .findFirst().orElseThrow();
        assertEquals(BigInteger.valueOf(-5), stockChange.getVolumeChange());

        SecurityChange optionChange = changes.stream()
                .filter(c -> c.getSecurity().equals(option))
                .findFirst().orElseThrow();
        assertEquals(BigInteger.valueOf(5), optionChange.getVolumeChange());
    }

    @Test
    public void buyStockRights_stockRightUsageTwice_holdingDecreasesToZero() {
        StockRightUsage usage2 = StockRightUsage.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .stockRight(stockRight)
                .option(option)
                .amount(BigInteger.valueOf(5))
                .build();
        actionRepository.save(usage2);

        List<Holding> holdings = holdingService.getHoldingForPortfolio(
                portfolio.getUuid(),
                LocalDateTime.now()
        );

        Holding stockRightHolding = holdings.stream()
                .filter(h -> h.getSecurity().equals(stockRight))
                .findFirst()
                .orElse(null);

        assertNotNull(stockRightHolding, "Stock right holding should exist");
        assertEquals(BigInteger.ZERO, stockRightHolding.getVolume());

        Holding optionHolding = holdings.stream()
                .filter(h -> h.getSecurity().equals(option))
                .findFirst()
                .orElseThrow();
        assertEquals(BigInteger.valueOf(10), optionHolding.getVolume());
    }

    @Test
    public void buyStockRights_stockRightUsage_appliesBalanceFee() {
        List<BalanceChange> balances = stockRightUsage.getBalanceChanges();
        assertEquals(1, balances.size());

        BigInteger expectedFee = BigInteger.valueOf(5).multiply(BigInteger.valueOf(100));
        assertEquals(expectedFee.negate(), balances.get(0).getChange_amount());
    }

    @Test
    public void buyStockRights_stockRightUsage_saleCannotBeDoneOnOptionSecurity() {
        Sale sale = Sale.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .security(option)
                .volume(BigInteger.valueOf(5))
                .price(100)
                .totalValue(BigInteger.valueOf(500))
                .build();

        assertTrue(sale.getBalanceChanges().isEmpty());

        assertTrue(sale.getSecurityChanges().isEmpty());
    }

    @Test
    public void stockRightUsage_saleOfRemainingStockRight_succeeds() {
        Sale sale = Sale.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .security(stockRight)
                .volume(BigInteger.valueOf(5))
                .price(100)
                .totalValue(BigInteger.valueOf(500))
                .build();
        actionRepository.save(sale);

        List<Holding> holdings = holdingService.getHoldingForPortfolio(
                portfolio.getUuid(),
                LocalDateTime.now()
        );

        Holding stockRightHolding = holdings.stream()
                .filter(h -> h.getSecurity().equals(stockRight))
                .findFirst()
                .orElseThrow();
        assertEquals(BigInteger.ZERO, stockRightHolding.getVolume());

        Holding optionHolding = holdings.stream()
                .filter(h -> h.getSecurity().equals(option))
                .findFirst()
                .orElseThrow();
        assertEquals(BigInteger.valueOf(5), optionHolding.getVolume());
    }
}
