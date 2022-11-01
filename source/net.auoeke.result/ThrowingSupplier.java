package net.auoeke.result;

import java.util.function.Supplier;

/**
 A functional interface like {@link Supplier} whose method {@link #get} throws {@link Throwable}.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
	T get() throws Throwable;
}
