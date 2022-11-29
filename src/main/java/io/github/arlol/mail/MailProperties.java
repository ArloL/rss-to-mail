package io.github.arlol.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "rss-to-mail.mail")
public class MailProperties {

	private String from;
	private String[] to;

}
