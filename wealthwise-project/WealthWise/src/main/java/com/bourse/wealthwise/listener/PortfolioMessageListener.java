package com.bourse.wealthwise.listener;

import com.bourse.wealthwise.domain.entity.action.ActionType;
import com.bourse.wealthwise.domain.entity.action.BaseAction;
import com.bourse.wealthwise.domain.entity.action.CapitalRaise;
import com.bourse.wealthwise.domain.entity.holding.Holding;
import com.bourse.wealthwise.domain.entity.portfolio.Portfolio;
import com.bourse.wealthwise.domain.entity.security.Security;
import com.bourse.wealthwise.domain.entity.security.SecurityChange;
import com.bourse.wealthwise.domain.services.BalanceActionService;
import com.bourse.wealthwise.domain.services.HoldingService;
import com.bourse.wealthwise.repository.ActionRepository;
import com.bourse.wealthwise.repository.PortfolioRepository;
import com.bourse.wealthwise.repository.SecurityPriceRepository;
import com.bourse.wealthwise.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.jms.annotation.JmsListener;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioMessageListener {
    private ActionRepository actionRepository;
    private SecurityPriceRepository securityPriceRepository;
    private PortfolioRepository portfolioRepository;
    private SecurityRepository securityRepository;

    private HoldingService holdingService;

    private final String CMD_CAPITAL_RAISE = "CAPITAL_RAISE";

    @JmsListener(destination = "INQ")
    private void receive(String message){
        System.out.println("Received Message: "+message);
        String[] tokens = message.split(" ");
        switch (tokens[0]) {
            case CMD_CAPITAL_RAISE -> doCapitalRaise(tokens[1], Double.parseDouble(tokens[2]));
        }
    }

    private void doCapitalRaise(String securitySymbol, Double stockRightAmountPerShare){
        Security security = securityRepository.findSecurityBySymbol(securitySymbol);
        Security stockRight = securityRepository.findSecurityBySymbol("H"+securitySymbol);
        List<Portfolio> portfolios = portfolioRepository.findAll();
        LocalDateTime localDateTime = LocalDateTime.now();

        List<BigInteger> symbolVolumesPerPortfolio = portfolios.stream()
                .map(portfolio ->
                    holdingService.getHoldingForPortfolio(portfolio.getUuid(), localDateTime)
                    .stream()
                    .filter(holding -> holding.getSecurity().getIsin().equals(security.getIsin()))
                    .map(Holding::getVolume)
                    .reduce(BigInteger.ZERO, BigInteger::add)
                ).toList();

        for (int i = 0; i < portfolios.size(); i++) {
            BigInteger addedVolume = BigInteger.valueOf(
                    (long) Math.floor(
                    symbolVolumesPerPortfolio.get(i).doubleValue() * stockRightAmountPerShare)
            );
            CapitalRaise capitalRaise = CapitalRaise.builder()
                    .uuid(UUID.randomUUID().toString())
                    .portfolio(portfolios.get(i))
                    .datetime(localDateTime)
                    .volume(addedVolume)
                    .security(stockRight)
                    .actionType(ActionType.CAPITAL_RAISE)
                    .build();
            actionRepository.save(capitalRaise);
        }
    }
}
