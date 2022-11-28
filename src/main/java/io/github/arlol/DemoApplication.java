package io.github.arlol;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.apptasticsoftware.rssreader.RssReader;

import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DemoApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	FeedItemRepository feedItemRepository;
	@Autowired
	ChannelRepository channelRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String url = "https://ra.co/xml/podcast.xml";
		Channel channel = Stream.of(Channel.builder().link(url).build())
				.map(
						c1 -> channelRepository.findByLink(c1.getLink())
								.map(
										c2 -> c1.toBuilder()
												.id(c2.getId())
												.build()
								)
								.orElse(c1)
				)
				.map(channelRepository::save)
				.findFirst()
				.orElseThrow();
		var articles = new RssReader().read(url)
				.map(
						item -> FeedItem.builder()
								.channelId(channel.getId())
								.title(item.getTitle().orElse(null))
								.description(item.getDescription().orElse(null))
								.link(item.getLink().orElse(null))
								.author(item.getAuthor().orElse(null))
								.category(item.getCategory().orElse(null))
								.guid(item.getGuid().orElse(null))
								.isPermaLink(item.getIsPermaLink().orElse(null))
								.pubDate(item.getPubDate().orElse(null))
								.build()
				)
				.map(
						i1 -> feedItemRepository.findByGuid(i1.getGuid())
								.map(
										i2 -> i1.toBuilder()
												.id(i2.getId())
												.build()
								)
								.orElse(i1)
				)
				.map(feedItemRepository::save)
				.map(item -> item.getTitle())
				.toList();
		log.info("{}", articles);
	}

}
