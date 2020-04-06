package org._brown_tech._custom;

import com.sun.mail.smtp.SMTPTransport;
import javafx.application.Platform;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * @author Mandela
 */

public class Email extends Issues {

    public String EMAIL_TO_CC = "";
    public String EMAIL_TEXT;
    public String EMAIL_TO;

    public Email(String EMAIL_TEXT, String EMAIL_TO) {
        this.EMAIL_TEXT = EMAIL_TEXT;
        this.EMAIL_TO = EMAIL_TO;
    }

    public String getEMAIL_TEXT() {
        return EMAIL_TEXT;
    }

    public String getEMAIL_TO() {
        return EMAIL_TO;
    }

    public void send() {
        try {
            final Properties properties = System.getProperties();
            final String SMTP_SERVER = "smtp server ";
            properties.put("mail.smtp.host", SMTP_SERVER);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "25");

            final Session session = Session.getInstance(properties, null);
            final Message message = new MimeMessage(session);
            final String EMAIL_FROM = "emandela60@gmail.com";
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getEMAIL_TO(), false));
            final String EMAIL_SUBJECT = "Password Reset";
            message.setSubject(EMAIL_SUBJECT);
            message.setText(getEMAIL_TEXT());
            message.setSentDate(new Date());

            final SMTPTransport smtpTransport = (SMTPTransport) session.getTransport("smtp");
            final String USERNAME = "emandela60@gmail.com";
            final String PASSWORD = "mandela2018";
            smtpTransport.connect(SMTP_SERVER, USERNAME, PASSWORD);
            smtpTransport.sendMessage(message, message.getAllRecipients());
            if (smtpTransport.getLastReturnCode() == 200) {
                Platform.runLater(() -> success_notification("Email sent. Check your email for the new password.").show());
            } else {
                Platform.runLater(() -> error_message("Failed!", "Email was not delivered").show());
            }
            smtpTransport.close();
        } catch (MessagingException e) {
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            e.printStackTrace();
            new Issues().programmer_error(e).show();
        }
    }
}
