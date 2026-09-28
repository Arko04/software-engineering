package com.bourse.wealthwise;

import com.bourse.wealthwise.listener.PortfolioMessageListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
public class WealthWiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthWiseApplication.class, args);
    }

}
