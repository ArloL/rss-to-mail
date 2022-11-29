package io.github.arlol;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

@ContextConfiguration(initializers = DatabaseTest.DataSourceInitializer.class)
@ActiveProfiles({ "default", "postgresql" })
public abstract class DatabaseTest {

	public static class DataSourceInitializer implements
			ApplicationContextInitializer<ConfigurableApplicationContext> {

		private final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(
				"postgres:15.1-alpine"
		).waitingFor(
				new WaitAllStrategy().withStrategy(Wait.forListeningPort())
						.withStrategy(
								Wait.forLogMessage(
										".*database system is ready to accept connections.*\\s",
										2
								)
						)
		);

		@Override
		public void initialize(
				ConfigurableApplicationContext applicationContext
		) {
			database.start();

			applicationContext
					.addApplicationListener((ContextClosedEvent event) -> {
						database.stop();
					});

			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
					applicationContext,
					"spring.test.database.replace=none",
					"spring.sql.init.mode=always",
					"spring.datasource.url=" + database.getJdbcUrl(),
					"spring.datasource.username=" + database.getUsername(),
					"spring.datasource.password=" + database.getPassword()
			);
		}

	}

}
