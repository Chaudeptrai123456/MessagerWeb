package com.example.Messenger.Service;

import com.example.Messenger.Repository.DiscountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.time.LocalDate;

@Component
public class ExpiredDiscountCleaner {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredDiscountCleaner.class);
    private final DiscountRepository discountRepository;

    public ExpiredDiscountCleaner(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cleanExpiredDiscounts() {
        LocalDate today = LocalDate.now();
        int deleted = discountRepository.deleteExpiredDiscounts(today);
        logger.info("🧹 Dọn dẹp {} discount đã hết hạn (endDate < {}).", deleted, today);
    }
}