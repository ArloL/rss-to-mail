package io.github.arlol.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	private final JavaMailSender emailSender;
	private final MailProperties mailProperties;

	public MailService(
			JavaMailSender emailSender,
			MailProperties mailProperties
	) {
		this.emailSender = emailSender;
		this.mailProperties = mailProperties;
	}

	public void send(MailMessage mailMessage) {
		SimpleMailMessage message = mailMessage.toBuilder()
				.to(mailProperties.to())
				.from(mailProperties.from())
				.build()
				.toSimpleMailMessage();
		emailSender.send(message);
	}

}
