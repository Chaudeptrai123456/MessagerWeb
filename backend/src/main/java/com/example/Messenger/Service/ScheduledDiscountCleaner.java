package com.example.Messenger.Service;

import com.example.Messenger.Repository.DiscountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class ScheduledDiscountCleaner {

    private final DiscountRepository discountRepository;

    public ScheduledDiscountCleaner(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    // Mỗi ngày 00:00 (giờ server)
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanExpiredDiscountsDaily() {
        LocalDate today = LocalDate.now();
        int deleted = discountRepository.deleteExpiredDiscounts(today);
        System.out.println("🕛 Auto-cleaned " + deleted + " expired discounts at midnight");
    }
}
