package net.auoeke.result;

import java.util.function.Function;

/**
 A functional interface like {@link Function} whose method {@link #apply} throws {@link Throwable}.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
	R apply(T t) throws Throwable;
}
