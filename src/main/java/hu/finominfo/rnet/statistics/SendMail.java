package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.database.H2KeyValue;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
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
            //System.out.println(emailMessage);
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
            message.setSubject("statistics and log");
            //message.setText(emailMessage);

            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            Path path = Paths.get("./log.zip");
            URI uri = URI.create("jar:" + path.toUri());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                Path externalTxtFile = Paths.get("./log/logging.log");
                Path pathInZipfile = zipfs.getPath("/logging.log");
                Files.copy(externalTxtFile, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
            }

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(emailMessage);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            String filename = "./log.zip";
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message);
            logger.info("Mail Sent");
            H2KeyValue.set(H2KeyValue.LAST_SENDING, String.valueOf(System.currentTimeMillis()));
        }catch(Exception ex){
            System.out.println(Utils.getStackTrace(ex));
            logger.error("Mail fail", ex);
        }
    }

    public static void main(String[] args) {
        Globals.get().executor.schedule(() -> Interface.getInterfaces(), 1, TimeUnit.SECONDS);
        Globals.get().executor.schedule(() -> Stat.getInstance().init(), 5, TimeUnit.SECONDS);
        Globals.get().executor.schedule(() -> SendMail.send(), 9, TimeUnit.SECONDS);
    }
}
