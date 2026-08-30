package io.github.arlol.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

abstract class FakeCrudRepository<T> implements CrudRepository<T, Long> {

	protected final List<T> entities = new ArrayList<>();
	protected long sequence;

	@Override
	public Iterable<T> findAll() {
		return List.copyOf(entities);
	}

	@Override
	public long count() {
		return entities.size();
	}

	@Override
	public void deleteAll() {
		entities.clear();
	}

	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<T> findById(Long unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsById(Long unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<T> findAllById(Iterable<Long> unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Long unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(T unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> unused) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(Iterable<? extends T> unused) {
		throw new UnsupportedOperationException();
	}

}
