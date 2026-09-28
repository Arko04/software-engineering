package com.bourse.wealthwise.domain.services;

import com.bourse.wealthwise.domain.entity.action.ActionType;
import com.bourse.wealthwise.domain.entity.holding.Holding;
import com.bourse.wealthwise.domain.entity.security.SecurityChange;
import com.bourse.wealthwise.repository.ActionRepository;
import com.bourse.wealthwise.repository.PortfolioRepository;
import com.bourse.wealthwise.repository.SecurityPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final ActionRepository actionRepository;
    private final PortfolioRepository portfolioRepository;
    private final SecurityPriceRepository securityPriceRepository;

    public List<Holding> getHoldingForPortfolio(String portfolioId, LocalDateTime localDateTime) {
        return actionRepository.findAllActionsOf(
                        portfolioRepository.findById(portfolioId)
                                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"))
                                .getUuid()
                ).stream()
                .filter(action -> action.getDatetime() != null && action.getDatetime().isBefore(localDateTime))
                .flatMap(action -> action.getSecurityChanges().stream())
                .collect(Collectors.groupingBy(
                        SecurityChange::getSecurity,
                        Collectors.reducing(BigInteger.ZERO, SecurityChange::getVolumeChange,
                                BigInteger::add)
                )).entrySet().stream()
                .map(entry -> Holding.builder()
                        .security(entry.getKey())
                        .volume(entry.getValue())
                        .value((securityPriceRepository
                                .getPrice(entry.getKey().getIsin(), localDateTime.toLocalDate())
                                .orElse(0.0)) * entry.getValue().doubleValue())
                        .build()
                ).toList();
    }

}
