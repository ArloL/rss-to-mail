package io.github.arlol.fake;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

public final class RecordingMailSender implements JavaMailSender {

	private final List<SimpleMailMessage> messages = new ArrayList<>();

	public List<SimpleMailMessage> messages() {
		return List.copyOf(messages);
	}

	@Override
	public void send(SimpleMailMessage... simpleMessages) {
		messages.addAll(List.of(simpleMessages));
	}

	@Override
	public MimeMessage createMimeMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MimeMessage createMimeMessage(InputStream contentStream) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void send(MimeMessage... mimeMessages) {
		throw new UnsupportedOperationException();
	}

}
