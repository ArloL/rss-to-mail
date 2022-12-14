package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.arlol.mail.MailMessage;
import io.github.arlol.mail.MailService;

@Service
public class FeedItemProcessor {

	private final FeedItemRepository feedItemRepository;
	private final MailService mailService;
	private final ChannelRepository channelRepository;

	public FeedItemProcessor(
			FeedItemRepository feedItemRepository,
			ChannelRepository channelRepository,
			MailService mailService
	) {
		this.feedItemRepository = feedItemRepository;
		this.channelRepository = channelRepository;
		this.mailService = mailService;
	}

	@Transactional
	public boolean processMails() {
		Optional<FeedItem> optItem = feedItemRepository
				.findFirstByProcessedIsFalse();
		if (optItem.isPresent()) {
			FeedItem item = optItem.get();
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
			mailService.send(
					MailMessage.builder().subject(subject).text(text).build()
			);
			return true;
		}
		return false;
	}

}
