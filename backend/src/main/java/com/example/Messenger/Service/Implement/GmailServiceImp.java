package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class GmailServiceImp {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, Order order) {
        System.out.println("test send email");
        String body = """
        Xin chào bạn,

        Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi! 🎉
        
        Đơn hàng của bạn đã được xác nhận thành công.
        
        🧾 Thông tin đơn hàng:
        - Mã đơn hàng: %s
        - Ngày đặt: %s
        - Tổng tiền: %s VNĐ
        - Phương thức thanh toán: %s
        
        🚚 Trạng thái hiện tại: Đang xử lý
        
        Chúng tôi sẽ gửi thông báo cho bạn khi đơn hàng được giao cho đơn vị vận chuyển.
        
        Trân trọng,
        Đội ngũ hỗ trợ khách hàng
        ---
        Cửa hàng của Châu 💙
        """.formatted(order.getId(), order.getCreatedAt(), order.getStatus(), "");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("phamchaugiatu123@gmail.com");
        message.setTo("nguyentienanh2001.dev@gmail.com");
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void announcementSaleOf() {

    }
}
