package ds454.locomotive.steamservice.util;

public sealed interface Result<V, E> {
    record Success<V, E>(V value) implements Result<V, E> {}
    record Failure<V, E>(E error) implements Result<V, E> {}
}
