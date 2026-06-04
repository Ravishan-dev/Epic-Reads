package com.ravishandev.epicreads.mail;

import com.ravishandev.epicreads.provider.MailServiceProvider;
import com.ravishandev.epicreads.util.Env;
import io.rocketbase.mail.EmailTemplateBuilder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public abstract class Mailable implements Runnable{

    private final MailServiceProvider mailServiceProvider;
    private final EmailTemplateBuilder.EmailTemplateConfigBuilder emailTemplateBuilder;

    public Mailable(){
        this.mailServiceProvider = MailServiceProvider.getInstance();
        this.emailTemplateBuilder = EmailTemplateBuilder.builder();
    }

    @Override
    public void run() {
        try{
            Session mailSession  = Session.getInstance(mailServiceProvider.getProperties(), mailServiceProvider.getAuthenticator());
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            mimeMessage.setFrom(new InternetAddress(Env.getProperty("app.mail")));
            build(mimeMessage);
            if(mimeMessage.getRecipients(Message.RecipientType.TO).length > 0){
                Transport.send(mimeMessage);
                System.out.println("\u001B[32mEmail Sending Success!!!\u001B[32m");
            }else{
                throw new RuntimeException("Email Recipient Can not be Empty");
            }
        }catch (MessagingException e){
            throw new RuntimeException(e);
        }
    }

    public  abstract void build(Message message) throws MessagingException;

    public EmailTemplateBuilder.EmailTemplateConfigBuilder getEmailTemplateBuilder(){
        return emailTemplateBuilder;
    }
}
