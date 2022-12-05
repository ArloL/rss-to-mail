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

	public FeedItemProcessor(
			FeedItemRepository feedItemRepository,
			MailService mailService
	) {
		this.feedItemRepository = feedItemRepository;
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
			mailService.send(
					MailMessage.builder()
							.subject(item.getTitle())
							.text(item.getLink())
							.build()
			);
			return true;
		}
		return false;
	}

}
