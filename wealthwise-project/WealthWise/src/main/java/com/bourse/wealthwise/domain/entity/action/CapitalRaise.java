package com.bourse.wealthwise.domain.entity.action;

import com.bourse.wealthwise.domain.entity.action.utils.ActionVisitor;
import com.bourse.wealthwise.domain.entity.balance.BalanceChange;
import com.bourse.wealthwise.domain.entity.holding.Holding;
import com.bourse.wealthwise.domain.entity.security.Security;
import com.bourse.wealthwise.domain.entity.security.SecurityChange;
import com.bourse.wealthwise.repository.ActionRepository;
import com.bourse.wealthwise.repository.PortfolioRepository;
import com.bourse.wealthwise.repository.SecurityPriceRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuperBuilder
@Getter
public class CapitalRaise extends BaseAction {
    private final Security security;
    private final BigInteger volume;

    @Override
    public List<BalanceChange> getBalanceChanges() {
        return List.of();
    }

    @Override
    public List<SecurityChange> getSecurityChanges() {
        return List.of(
                SecurityChange.builder()
                        .uuid(UUID.randomUUID())
                        .portfolio(this.portfolio)
                        .datetime(this.datetime)
                        .security(this.security)
                        .volumeChange(this.volume)
                        .action(this)
                        .build()
        );
    }

    @Override
    public String accept(ActionVisitor visitor) {
        return visitor.visit(this);
    }

    private void setActionType() {
        this.actionType = ActionType.CAPITAL_RAISE;
    }

}
