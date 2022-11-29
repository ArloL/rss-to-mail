package io.github.arlol.mail;

import java.util.Date;

import org.springframework.mail.SimpleMailMessage;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MailMessage {

	public SimpleMailMessage toSimpleMailMessage() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setReplyTo(replyTo);
		message.setTo(to);
		message.setCc(cc);
		message.setBcc(bcc);
		message.setSentDate(sentDate);
		message.setSubject(subject);
		message.setText(text);
		return message;
	}

	private String from;
	private String replyTo;
	private String[] to;
	private String[] cc;
	private String[] bcc;
	private Date sentDate;
	private String subject;
	private String text;

}
