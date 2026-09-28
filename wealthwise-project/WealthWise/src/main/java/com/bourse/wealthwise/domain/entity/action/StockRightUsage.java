package com.bourse.wealthwise.domain.entity.action;

import com.bourse.wealthwise.domain.entity.action.utils.ActionVisitor;
import com.bourse.wealthwise.domain.entity.balance.BalanceChange;
import com.bourse.wealthwise.domain.entity.security.Security;
import com.bourse.wealthwise.domain.entity.security.SecurityChange;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@SuperBuilder
@Getter
public class StockRightUsage extends BaseAction {
    private final Security stockRight;
    private final Security option;
    private final BigInteger amount;

    private static final BigInteger STOCK_RIGHT_USAGE_FEE = BigInteger.valueOf(100);

    @Override
    public List<BalanceChange> getBalanceChanges() {
        return List.of(
                BalanceChange.builder()
                        .uuid(UUID.randomUUID())
                        .portfolio(this.portfolio)
                        .datetime(this.datetime)
                        .change_amount(this.amount.multiply(STOCK_RIGHT_USAGE_FEE).negate())
                        .action(this)
                        .build()
        );
    }

    @Override
    public List<SecurityChange> getSecurityChanges() {
        return List.of(
                SecurityChange.builder()
                        .uuid(UUID.randomUUID())
                        .portfolio(this.portfolio)
                        .datetime(this.datetime)
                        .security(this.stockRight)
                        .volumeChange(this.amount.negate())
                        .action(this)
                        .build(),
                SecurityChange.builder()
                        .uuid(UUID.randomUUID())
                        .portfolio(this.portfolio)
                        .datetime(this.datetime)
                        .security(this.option)
                        .volumeChange(this.amount)
                        .action(this)
                        .build()
        );
    }

    @Override
    public String accept(ActionVisitor visitor) {
        return visitor.visit(this);
    }

    private void setActionType() {
        this.actionType = ActionType.STOCK_RIGHT_USAGE;
    }

}
