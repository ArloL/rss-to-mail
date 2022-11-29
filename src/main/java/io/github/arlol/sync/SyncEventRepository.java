package io.github.arlol.sync;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncEventRepository extends CrudRepository<SyncEvent, Long> {

	public static final String FIND_AND_DELETE_NEXT_SYNC_EVENT_QUERY = """
			WITH target_rows AS MATERIALIZED (
				SELECT id
				FROM sync_event
				ORDER BY id
				LIMIT 1
				FOR UPDATE
				SKIP LOCKED
			)
			DELETE FROM sync_event
			WHERE id IN (SELECT * FROM target_rows)
			RETURNING *
			""";

	@Query(FIND_AND_DELETE_NEXT_SYNC_EVENT_QUERY)
	Optional<SyncEvent> findAndDeleteNextSyncEvent();

}
