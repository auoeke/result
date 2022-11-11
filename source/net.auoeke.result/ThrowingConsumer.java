package net.auoeke.result;

import java.util.function.Consumer;

/**
 A functional interface like {@link Consumer} whose method {@link #accept} throws {@link Throwable}.
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {
	void accept(T t) throws Throwable;
}
