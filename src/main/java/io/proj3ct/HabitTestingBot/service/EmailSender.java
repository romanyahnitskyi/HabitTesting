package io.proj3ct.HabitTestingBot.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailSender {

    public static void sendEmail(String recipientEmail, String subject, String name, int verificationCode) throws MessagingException {
        // Налаштування властивостей для вашого поштового сервера
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Ваші дані для входу на поштовий сервер
        final String myAccountEmail = "habittestingbot@gmail.com";
        final String password = "dkqy vwbc ycca izte";

        // Створення сесії з аутентифікацією
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        // Створення повідомлення з HTML тілом
        Message message = prepareMessage(session, myAccountEmail, recipientEmail, subject, name, verificationCode);

        // Відправлення повідомлення
        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipient, String subject, String name, int verificationCode) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);

            // Формування HTML тіла повідомлення
            String htmlContent = "<h1>Підтвердження акаунта</h1>"
                    + "<p>Привіт, " + name + "! Дякуємо за реєстрацію.</p>"
                    + "<p>Для завершення реєстрації, будь ласка, введіть наступний код підтвердження на сайті:</p>"
                    + "<h2>" + verificationCode + "</h2>"
                    + "<p>З повагою,<br>Ваш розробник.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            // Генерація унікального коду для верифікації
            int verificationCode = new Random().nextInt(9999); // Забезпечує код в діапазоні 100000-999999

            sendEmail("recipient_email@example.com", "Підтвердження акаунта", "Ім'я Отримувача", verificationCode);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}