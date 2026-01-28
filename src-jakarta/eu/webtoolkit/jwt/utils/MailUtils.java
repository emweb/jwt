package eu.webtoolkit.jwt.utils;

import java.io.IOException;
import java.util.Properties;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import eu.webtoolkit.jwt.Configuration;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WString;

public class MailUtils {

	public static void sendMail(Message m) throws MessagingException {
		Transport.send(m);
	}

	public static boolean isEmpty(Address[] from) {
		return from == null || from.length == 0;
	}

	public static void setSubject(Message message, WString subject) throws MessagingException {
		message.setSubject(subject.toString());
	}

	public static void addHtmlBody(Message message, WString htmlBody) throws IOException, MessagingException {
		MimeMultipart mp = getMultiPart(message);
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setText(htmlBody.toString(), "UTF-8", "html");
		mp.addBodyPart(htmlPart);
	}
	
	private static MimeMultipart getMultiPart(Message message)  throws MessagingException {
		Object content = null;
		
		try {
		content = message.getContent();
		} catch (IOException ioe) {
			//when no content was previously set an IOException is thrown
		}
		if (content != null && content instanceof MimeMultipart) {
			return (MimeMultipart)content;
		} else {
			MimeMultipart mp = new MimeMultipart("alternative");
			message.setContent(mp);
			return mp;
		}
	}

	public static Properties getDefaultProperties() {
		WApplication app = WApplication.getInstance();
		Configuration conf = app.getEnvironment().getServer().getConfiguration();
		
		Properties properties = new Properties();
		properties.put("mail.smtp.host", conf.getProperty("smtp.host"));
		properties.put("mail.smtp.port", conf.getProperty("smtp.port"));
		return properties;		
	}

	public static void setBody(Message message, WString plainBody) throws MessagingException, IOException {
		MimeMultipart mp = getMultiPart(message);
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setText(plainBody.toString());
		mp.addBodyPart(htmlPart);
	}
}
