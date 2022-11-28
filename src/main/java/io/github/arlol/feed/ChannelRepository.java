package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ChannelRepository extends CrudRepository<Channel, Long> {

	Optional<Channel> findByLink(String link);

}
