package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedItemProcessor {

	private final FeedItemRepository feedItemRepository;
	private final ChannelRepository channelRepository;
	private final JavaMailSender mailSender;

	public FeedItemProcessor(
			FeedItemRepository feedItemRepository,
			ChannelRepository channelRepository,
			JavaMailSender mailSender
	) {
		this.feedItemRepository = feedItemRepository;
		this.channelRepository = channelRepository;
		this.mailSender = mailSender;
	}

	@Transactional
	public boolean processMails(String from, String[] to) {
		Optional<FeedItem> optItem = feedItemRepository
				.findFirstByProcessedIsFalse();
		if (optItem.isPresent()) {
			FeedItem item = optItem.orElseThrow();
			item = item.toBuilder().processed(true).build();
			item = feedItemRepository.save(item);
			Channel channel = channelRepository.findById(item.getChannelId())
					.orElseThrow();
			String subject = item.getTitle();
			if (channel.getName() != null && !channel.getName().isBlank()) {
				subject += " - " + channel.getName();
			}
			String text = "";
			if (item.getLink() != null) {
				text = item.getLink();
			}
			if (item.getIsPermaLink()) {
				text = item.getGuid();
			}

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);

			return true;
		}
		return false;
	}

}
