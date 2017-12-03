package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.database.H2KeyValue;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.12.03.
 */
public class SendMail {
    private final static Logger logger = Logger.getLogger(SendMail.class);
    public static void send() {

        try{
            final String emailMessage = Stat.get();
            System.out.println(emailMessage);
            final String fromEmail = "rnetkk4@gmail.com"; //requires valid gmail id
            final String password = "Alma1241"; // correct password for gmail id
            final String toEmail = "rnetkk4@gmail.com"; // can be any email id

            System.out.println("TLSEmail Start");
            Properties props = new Properties();
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
            props.put("mail.smtp.port", "587"); //TLS Port
            props.put("mail.smtp.auth", "true"); //enable authentication
            props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            };
            Session session = Session.getInstance(props, auth);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("statistics");
            message.setText(emailMessage);
            Transport.send(message);
            logger.info("Mail Sent");
            H2KeyValue.set(H2KeyValue.LAST_SENDING, String.valueOf(System.currentTimeMillis()));
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            logger.error("Mail fail", ex);
        }
    }

    public static void main(String[] args) {
        Globals.get().executor.schedule(() -> Interface.getInterfaces(), 1, TimeUnit.SECONDS);
        Globals.get().executor.schedule(() -> Stat.getInstance().init(), 5, TimeUnit.SECONDS);
        Globals.get().executor.schedule(() -> SendMail.send(), 9, TimeUnit.SECONDS);
    }
}
