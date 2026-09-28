package com.bourse.wealthwise.domain.services;

import com.bourse.wealthwise.domain.entity.account.User;
import com.bourse.wealthwise.domain.entity.action.ActionType;
import com.bourse.wealthwise.domain.entity.action.Buy;
import com.bourse.wealthwise.domain.entity.action.Deposit;
import com.bourse.wealthwise.domain.entity.action.Sale;
import com.bourse.wealthwise.domain.entity.holding.Holding;
import com.bourse.wealthwise.domain.entity.portfolio.Portfolio;
import com.bourse.wealthwise.domain.entity.security.Security;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
public class HoldingServiceTest {
    @Autowired
    private HoldingService holdingActionService;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private SecurityPriceRepository securityPriceRepository;

    @Autowired
    private ActionRepository actionRepository;

    private Portfolio portfolio;
    private Security security;

    @BeforeEach
    public void setUp() {
        User user = User.builder().build();
        this.security = Security.builder().build();
        this.portfolio = new Portfolio("21e42b92-cef6-453f-9e52-fa76b1d830f6",
                user,
                "portfo");

        portfolioRepository.save(portfolio);

    }

    @Test
    public void somePortfoliosExist_lookForPortfolioWithWrongID_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> {
            holdingActionService.getHoldingForPortfolio(UUID.randomUUID().toString(), LocalDateTime.now());
        });
    }

    @Test
    public void somePortfoliosExist_lookForPortfolioWithNoHoldings_noHoldingsReturned() {
        assertEquals(holdingActionService.getHoldingForPortfolio(
                "21e42b92-cef6-453f-9e52-fa76b1d830f6",
                LocalDateTime.now()),
                List.of());
    }

    @Test
    public void newDepositActionEnters_getHoldings_noHoldingsReturned(){
        Deposit deposit = Deposit.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now().minusMinutes(1))
                .amount(BigInteger.valueOf(1000))
                .actionType(ActionType.DEPOSIT)
                .build();
        actionRepository.save(deposit);

        assertEquals(
                holdingActionService.getHoldingForPortfolio(
                        "21e42b92-cef6-453f-9e52-fa76b1d830f6",
                        LocalDateTime.now()
                ),
                List.of()
        );
        actionRepository.deleteById(deposit.getUuid());
    }

    @Test
    public void newBuyActionEnters_getHoldings_DecreasedBalanceReturned(){
        Deposit deposit = Deposit.builder()
                .uuid("21e42b92-cef6-453f-9e52-fa76b1d830f6")
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .amount(BigInteger.valueOf(1000))
                .actionType(ActionType.DEPOSIT)
                .build();
        actionRepository.save(deposit);
        securityPriceRepository.addPrice(security.getIsin(), LocalDateTime.now().toLocalDate(), 3);

        Buy buy = Buy.builder()
                .uuid("21e42b92-cef6-453f-9e52-fa76b1d830f6")
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .volume(BigInteger.valueOf(100))
                .price(3)
                .totalValue(BigInteger.valueOf(300))
                .security(security)
                .actionType(ActionType.BUY)
                .build();
        actionRepository.save(buy);
        Holding actual = holdingActionService.getHoldingForPortfolio("21e42b92-cef6-453f-9e52-fa76b1d830f6", LocalDateTime.now()).get(0);
        assertEquals(BigInteger.valueOf(100), actual.getVolume());
        assertEquals(security, actual.getSecurity());
        assertEquals(300.0, actual.getValue(), 0.01);
        actionRepository.deleteById(buy.getUuid());
        actionRepository.deleteById(deposit.getUuid());
        securityPriceRepository.clear();
    }

    @Test
    public void newBuyStockRightActionEnters_getHoldings_StockRightReturned() {
        Deposit deposit = Deposit.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .amount(BigInteger.valueOf(2000))
                .actionType(ActionType.DEPOSIT)
                .build();
        actionRepository.save(deposit);

        Security stockRight = Security.builder()
                .name(security.getName())
                .symbol("H" + security.getSymbol())
                .isin("H" + security.getIsin())
                .build();

        securityPriceRepository.addPrice(stockRight.getIsin(), LocalDateTime.now().toLocalDate(), 2);

        Buy buyStockRight = Buy.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .volume(BigInteger.valueOf(50))
                .price(2)
                .totalValue(BigInteger.valueOf(100))
                .security(stockRight)
                .actionType(ActionType.BUY)
                .build();
        actionRepository.save(buyStockRight);

        Holding actual = holdingActionService
                .getHoldingForPortfolio(portfolio.getUuid(), LocalDateTime.now())
                .stream()
                .filter(h -> h.getSecurity().equals(stockRight))
                .findFirst()
                .orElseThrow();

        assertEquals(BigInteger.valueOf(50), actual.getVolume());
        assertEquals(stockRight, actual.getSecurity());
        assertEquals(100.0, actual.getValue(), 0.01);

        actionRepository.deleteById(buyStockRight.getUuid());
        actionRepository.deleteById(deposit.getUuid());
        securityPriceRepository.clear();
    }

    @Test
    public void buyAndSellStockRightActionEnters_getHoldings_ZeroVolumeAfterSell() {
        Deposit deposit = Deposit.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .amount(BigInteger.valueOf(2000))
                .actionType(ActionType.DEPOSIT)
                .build();
        actionRepository.save(deposit);

        Security stockRight = Security.builder()
                .name(security.getName())
                .symbol("H" + security.getSymbol())
                .isin("H" + security.getIsin())
                .build();

        securityPriceRepository.addPrice(stockRight.getIsin(), LocalDateTime.now().toLocalDate(), 2);

        Buy buy = Buy.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now().minusMinutes(1))
                .volume(BigInteger.valueOf(50))
                .price(2)
                .totalValue(BigInteger.valueOf(100))
                .security(stockRight)
                .actionType(ActionType.BUY)
                .build();
        actionRepository.save(buy);

        Sale sale = Sale.builder()
                .uuid(UUID.randomUUID().toString())
                .portfolio(portfolio)
                .datetime(LocalDateTime.now())
                .volume(BigInteger.valueOf(50))
                .price(2)
                .totalValue(BigInteger.valueOf(100))
                .security(stockRight)
                .actionType(ActionType.SALE)
                .build();
        actionRepository.save(sale);

        List<Holding> holdings = holdingActionService.getHoldingForPortfolio(
                portfolio.getUuid(),
                LocalDateTime.now()
        );

        boolean stillHasRights = holdings.stream()
                .anyMatch(h -> h.getSecurity().equals(stockRight) && h.getVolume().compareTo(BigInteger.ZERO) > 0);

        assertEquals(false, stillHasRights);

        actionRepository.deleteById(sale.getUuid());
        actionRepository.deleteById(buy.getUuid());
        actionRepository.deleteById(deposit.getUuid());
        securityPriceRepository.clear();
    }
}

