package com.bourse.wealthwise.domain.entity.holding;

import com.bourse.wealthwise.domain.entity.security.Security;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@Builder
public class Holding {
    private Security security;
    private BigInteger volume;
    private double value;
}
