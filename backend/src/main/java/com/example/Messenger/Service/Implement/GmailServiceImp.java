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
    private static final String CONFIRM_URL = "http://localhost:9999/api/orders/confirm?token=%s";

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
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendConfirmationEmail(String to, String token) {
        String confirmLink = CONFIRM_URL.formatted(token);
        String body = """
        Xin chào,

        Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi 🎉
        
        Để xác nhận đơn hàng, vui lòng nhấn vào liên kết bên dưới:
        
        🔗 %s

        Liên kết này sẽ hết hạn sau 30 phút.

        Trân trọng,
        Cửa hàng của Châu 💙
        """.formatted(confirmLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("phamchaugiatu123@gmail.com");
        message.setTo(to);
        message.setSubject("Xác nhận đơn hàng của bạn");
        message.setText(body);
        mailSender.send(message);
    }

    public void sendSuccessEmail(String to, Order order) {
        String body = """
        Đơn hàng của bạn đã được xác nhận thành công 🎉

        🧾 Mã đơn: %s
        Tổng tiền: %.2f VNĐ
        Trạng thái: %s

        Chúng tôi sẽ thông báo khi đơn hàng được giao cho đơn vị vận chuyển.

        Trân trọng,
        Cửa hàng của Châu 💙
        """.formatted(order.getId(), order.getTotalAmount(), order.getStatus());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("phamchaugiatu123@gmail.com");
        message.setTo(to);
        message.setSubject("✅ Đơn hàng xác nhận thành công!");
        message.setText(body);
        mailSender.send(message);
    }
}
