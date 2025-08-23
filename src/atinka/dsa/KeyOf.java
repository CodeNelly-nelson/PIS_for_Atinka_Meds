package atinka.dsa;

/** Minimal key extractor (no java.util.function). */
@FunctionalInterface
public interface KeyOf<T, K> {
    K key(T value);
}
