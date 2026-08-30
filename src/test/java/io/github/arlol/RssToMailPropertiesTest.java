package io.github.arlol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.arlol.RssToMailProperties.Config;

class RssToMailPropertiesTest {

	@Test
	void configsDefaultToAnEmptyList() {
		assertThat(
				new RssToMailProperties(false, "target/cache", null).configs()
		).isEmpty();
	}

	@Test
	void recipientsAndChannelsDefaultToEmptyLists() {
		Config config = new Config("from@example.com", null, null);

		assertThat(config.to()).isEmpty();
		assertThat(config.channels()).isEmpty();
	}

	@Test
	void configsWithTheSameRecipientsAreEqual() {
		Config config = new Config(
				"from@example.com",
				List.of("a@example.com"),
				List.of()
		);
		Config same = new Config(
				"from@example.com",
				List.of("a@example.com"),
				List.of()
		);

		assertThat(config).isEqualTo(same).hasSameHashCodeAs(same);
		assertThat(config.toString()).contains("a@example.com");
	}

}
