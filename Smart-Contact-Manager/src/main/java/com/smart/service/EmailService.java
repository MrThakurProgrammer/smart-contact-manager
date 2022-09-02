package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to) {
		
		boolean f=false;
		
		String from="mr.thakurprogrammer91@gmail.com";
		
		//variable for gmail
		String host="smtp.gmail.com";
		
		Properties properties=System.getProperties();
		System.out.println("Properties: "+properties);
		
		//Setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//step 1: to get the session objects..
		Session session=Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("mr.thakurprogrammer91@gmail.com","zuvnmnmstjkbkdlb");
			}			
		});
		session.setDebug(true);
		
		//step 2: compose the message 
		MimeMessage m=new MimeMessage(session);
		
		try {
			
			//form email
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
			//m.setText(message);
			m.setContent(message, "text/html");
			
			
			
			//step 3: send the message using transport class
			Transport.send(m);
			
			System.out.println("Send success !!");
			
			f=true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}
	
}
