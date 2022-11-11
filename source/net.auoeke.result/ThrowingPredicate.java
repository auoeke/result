package net.auoeke.result;

import java.util.function.Predicate;

/**
 A functional interface like {@link Predicate} whose method {@link #test} throws {@link Throwable}.
 */
@FunctionalInterface
public interface ThrowingPredicate<T> {
	boolean test(T t) throws Throwable;
}
