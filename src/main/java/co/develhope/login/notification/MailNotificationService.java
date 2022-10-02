package co.develhope.login.notification;

import co.develhope.login.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationEmail(User user) {

        SimpleMailMessage sms = new SimpleMailMessage();
        sms.setTo(user.getEmail());
        sms.setFrom("devtestdevtest828117@tiscali.it");
        sms.setReplyTo("devtestdevtest828117@tiscali.it");
        sms.setSubject("ti sei iscritto al login demo");
        sms.setText("il codice di attivazione è:" + user.getActivationCode());
        mailSender.send(sms);

    }
}
