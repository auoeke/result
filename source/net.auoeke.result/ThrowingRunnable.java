package net.auoeke.result;

/**
 A functional interface like {@link Runnable} whose method {@link #run} throws {@link Throwable}.
 */
@FunctionalInterface
public interface ThrowingRunnable {
	void run() throws Throwable;
}
