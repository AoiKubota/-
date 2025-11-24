package com.example.planvista.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@planvista.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    

    public void sendPasswordResetEmail(String toEmail, String token) {
        if (mailSender == null) {
            System.out.println("=== パスワードリセットメール ===");
            System.out.println("送信先: " + toEmail);
            System.out.println("リセットURL: " + baseUrl + "/pwd_form?token=" + token);
            System.out.println("===============================");
            return;
        }
        
        String resetUrl = baseUrl + "/pwd_form?token=" + token;
        String subject = "【PlanVista】パスワード再設定のご案内";
        String body = String.format(
            """
            PlanVistaをご利用いただきありがとうございます。
            
            パスワード再設定のリクエストを受け付けました。
            以下のURLをクリックして、新しいパスワードを設定してください。
            
            %s
            
            このリンクの有効期限は24時間です。
            
            ※このメールに心当たりがない場合は、このメールを破棄してください。
            
            ---
            PlanVista
            """,
            resetUrl
        );
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        
        try {
            mailSender.send(message);
            System.out.println("パスワードリセットメールを送信しました: " + toEmail);
        } catch (Exception e) {
            System.err.println("メール送信エラー: " + e.getMessage());
            throw new RuntimeException("メール送信に失敗しました", e);
        }
    }
    

    public void sendPasswordChangedEmail(String toEmail) {
        if (mailSender == null) {
            System.out.println("=== パスワード変更完了メール ===");
            System.out.println("送信先: " + toEmail);
            System.out.println("================================");
            return;
        }
        
        String subject = "【PlanVista】パスワード変更完了のお知らせ";
        String body = """
            PlanVistaをご利用いただきありがとうございます。
            
            パスワードの変更が完了しました。
            
            ※このメールに心当たりがない場合は、至急サポートまでご連絡ください。
            
            ---
            PlanVista
            """;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        
        try {
            mailSender.send(message);
            System.out.println("パスワード変更完了メールを送信しました: " + toEmail);
        } catch (Exception e) {
            System.err.println("メール送信エラー: " + e.getMessage());
        }
    }
}
