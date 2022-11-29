package io.github.arlol.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rss-to-mail.mail")
public record MailProperties(
		String from,
		String[] to
) {

}
