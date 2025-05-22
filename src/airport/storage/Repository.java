package airport.storage;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para repositorios in‐memory.
 */
public interface Repository<T, ID> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
}
