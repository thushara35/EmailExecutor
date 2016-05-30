package org.wso2.lc.executor.sample.utils;

import org.wso2.lc.executor.sample.EmailExecutorException;
import org.wso2.lc.executor.sample.Constants.ExecutorConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This calls use to transform the email metadata to Email sender.
 */
public class EmailTransformUtils {

    private static final Log log = LogFactory.getLog(EmailTransformUtils.class);

    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private static InternetAddress SMTP_FROM_ADDRESS = null;
    private static Session SESSION;
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;

    /**
     * This method will take list of email address that need
     *
     * @param configContext         ConfigurationContext.
     * @param message               Mail body.
     * @param subject               Mail Subject.
     * @param toAddress             Email to address.
     * @throws org.wso2.lc.executor.sample.EmailExecutorException
     */
    public static void sendMimeMessage(ConfigurationContext configContext, String message, String subject,
            String toAddress) throws EmailExecutorException {
        Properties props = new Properties();
        if (configContext != null
                && configContext.getAxisConfiguration().getTransportOut(ExecutorConstants.MAIL_TO) != null) {
            List<Parameter> params = configContext.getAxisConfiguration().getTransportOut(ExecutorConstants.MAIL_TO)
                    .getParameters();
            for (Parameter param : params) {
                props.put(param.getName(), param.getValue());
            }
        }
        if (THREAD_POOL_EXECUTOR == null) {
            THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(MIN_THREAD, MAX_THREAD, DEFAULT_KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
        }
        String smtpFrom = props.getProperty(MailConstants.MAIL_SMTP_FROM);

        try {
            SMTP_FROM_ADDRESS = new InternetAddress(smtpFrom);
        } catch (AddressException e) {
            throw new EmailExecutorException("Error in transforming SMTP address.", e);
        }
        final String smtpUsername = props.getProperty(MailConstants.MAIL_SMTP_USERNAME);
        final String smtpPassword = props.getProperty(MailConstants.MAIL_SMTP_PASSWORD);
        if (smtpUsername != null && smtpPassword != null) {
            SESSION = Session.getInstance(props, new Authenticator() {

                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
        } else {
            SESSION = Session.getInstance(props);
        }
        THREAD_POOL_EXECUTOR
                .submit(new EmailSender(toAddress, subject, message.toString(), ExecutorConstants.TEXT_HTML));
    }


    static class EmailSender implements Runnable {
        String to;
        String subject;
        String body;
        String type;

        EmailSender(String to, String subject, String body, String type) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.type = type;
        }

        /**
         * Sending emails to the corresponding Email IDs'.
         */
        @Override
        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Format of the email:" + " " + to + "->" + type);
            }
            //Creating MIME object using initiated session.

            MimeMessage message = new MimeMessage(SESSION);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(body);
            String finalString = stringBuilder.toString();
            //Setting up the Email attributes and Email payload.

            try {
                message.setFrom(SMTP_FROM_ADDRESS);
                message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject);
                message.setSentDate(new Date());
                message.setContent(finalString, type);

                if (log.isDebugEnabled()) {
                    log.debug("Meta data of the email configured successfully");
                }
                Transport.send(message);

                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID" + " " + to + " " + "Successfully");
                }
            } catch (MessagingException e) {
                log.error("Error in sending the Email : " + SMTP_FROM_ADDRESS.toString() + "::" +e.getMessage(), e);
            }
        }

    }
}
