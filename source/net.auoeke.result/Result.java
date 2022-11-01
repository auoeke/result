package net.auoeke.result;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 A container of a result of some computation or exceptions that occurred therein.
 An instance of {@link Result} can be a {@link Success} or a {@link Failure}.
 <p>
 A success contains a {@link #value} of type {@code V?};
 which can also be queried by the methods {@link #valueOr}, {@link #valueOrNull} and {@link #valueOrGet}.
 <p>
 A failure can contain an exception that is known as its {@link #cause} and 1 or more {@link #suppress suppressed} exceptions;
 but it may be empty. The method {@link #exception} can be used in order to get an exception describing
 the reason for failure and is thrown by {@link Failure#value}.

 @param <V> the type of the successful result of the computation
 @since 0.0.0
 */
public sealed interface Result<V> {
	/**
	 Returns a {@link Success} containing {@code value}.

	 @param value the value of the success; which may be {@code null}
	 @param <V> the type of {@code value}
	 @return a {@link Success} containing {@code value}
	 @since 0.0.0
	 */
	static <V> Success<V> success(V value) {
		return new Success<>(value);
	}

	/**
	 Returns a {@link Failure} containing {@code cause}.

	 @param cause the exception of the failure; which may be {@code null}
	 @param <V> the type of the value
	 @return a {@link Failure} containing {@code cause}
	 @since 0.0.0
	 */
	static <V> Failure<V> failure(Throwable cause) {
		return new Failure<>(cause, null);
	}

	/**
	 Returns the {@link Result} of running {@code supplier}.
	 If it throws a {@link Throwable}, then a {@link Failure} with the {@link Throwable} as its {@link #cause} is returned.
	 Otherwise, a {@link Success}{@code <V>} containing the value of {@code supplier.get()} is returned.

	 @return a {@link Result} describing the result of {@code supplier.get()}
	 @since 0.0.0
	 */
	static <V> Result<V> of(ThrowingSupplier<? extends V> supplier) {
		try {
			return new Success<>(supplier.get());
		} catch (Throwable cause) {
			return new Failure<>(cause, null);
		}
	}

	/**
	 Returns the {@link Result} of running {@code runnable}.
	 If it throws a {@link Throwable}, then a {@link Failure} wrapping it is returned.
	 Otherwise, a {@link Success}{@code <Void>} containing {@code null} is returned.

	 @return a {@link Result} describing the result of {@code runnable.run()}
	 @since 0.0.0
	 */
	static Result<Void> ofVoid(ThrowingRunnable runnable) {
		return of(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 Returns whether the result is a {@link Success}.

	 @return {@code true} if {@code this} is a {@link Success}
	 @since 0.0.0
	 */
	boolean isSuccess();

	/**
	 Returns whether the result is a {@link Failure}.

	 @return {@code true} if {@code this} is a {@link Failure}
	 @since 0.0.0
	 */
	default boolean isFailure() {
		return !this.isSuccess();
	}

	/**
	 If this result {@link #isSuccess is a success}, then its value is returned;
	 otherwise an {@link #exception} is thrown possibly with {@link #cause detail} about the {@link Failure failure} as its {@link Throwable#getCause cause}.

	 @return the value if this result is a success
	 @throws NoSuchElementException if this result is a failure
	 @since 0.0.0
	 */
	V value() throws NoSuchElementException;

	/**
	 Returns {@link Optional#empty} if this result is a {@link Success} or a {@link Failure} without an associated cause;
	 otherwise returns an {@link Optional} containing an exception that caused the failure.
	 <p>
	 A failure might not have a cause if it has been {@link #handle handled},
	 the failure has been so {@link #failure constructed} or
	 it was produced by {@link #filter}.

	 @return an {@link Optional} that contains—if this result is a failure with a cause—the exception that caused it
	 @since 0.0.0
	 */
	Optional<Throwable> cause();

	/**
	 Returns an {@link Optional} containing a new exception if this result is a {@link Failure}
	 or {@link Optional#empty} if this result is a {@link Success}.
	 <p>
	 The cause of the exception is the {@link #cause} of the failure.
	 If the failure does not have a cause and has {@link #suppress suppressed} exceptions,
	 then they are added to the root exception returned by this method as suppressed exceptions.

	 @return an {@link Optional} that contains—if this result is a failure—a new exception possibly with detail
	 @since 0.0.0
	 */
	Optional<NoSuchElementException> exception();

	/**
	 Returns {@code this} is this result is a {@link Success} or else throws the {@link #exception}.

	 @return this result as a {@link Success}
	 @throws NoSuchElementException if this result is a {@link Failure}
	 @since 0.0.0
	 */
	Success<V> asSuccess() throws NoSuchElementException;

	/**
	 Returns {@code this} is this result is a {@link Failure} or else throws the {@link #exception}.

	 @return this result as a {@link Failure}
	 @throws ClassCastException if this result is a {@link Success}
	 @since 0.0.0
	 */
	Failure<V> asFailure() throws ClassCastException;

	/**
	 If this result {@link #isSuccess is a success}, then its value is returned;
	 otherwise the value of {@code alternative.get()} is returned.

	 @return the value if this result is a success or else the value of {@code alternative.get()}
	 @since 0.0.0
	 */
	V valueOr(V alternative);

	/**
	 If this result {@link #isSuccess is a success}, then its value is returned;
	 otherwise the value of {@code alternative.get()} is returned.

	 @return the value if this result is a success or else the value of {@code alternative.get()}
	 @throws NullPointerException if {@code alternative == null}
	 @since 0.0.0
	 */
	V valueOrGet(Supplier<? extends V> alternative);

	/**
	 If this result {@link #isSuccess is a success}, then its value is returned;
	 otherwise {@code null} is returned.

	 @return the value if this result is a success or else {@code null}
	 @since 0.0.0
	 */
	default V valueOrNull() {
		return this.valueOr(null);
	}

	/**
	 Returns {@code this} if this result {@link #isSuccess is a success} or else the {@link Result#of result of} {@code alternative.get()}.

	 @param alternative an alternative supplier to use if this result {@link #isFailure is a failure}
	 @return {@code this} if this result is a success or else the result of {@code alternative.get()}
	 @since 0.0.0
	 */
	Result<V> or(ThrowingSupplier<V> alternative);

	/**
	 Returns {@code this} if this result {@link #isSuccess is a success} or else the value of {@code alternative.get()}.
	 If {@code alternative} returns a {@link Failure}, then

	 @param alternative a supplier of an alternative result if this result {@link #isFailure is a failure}
	 @return {@code this} if this result is a success or else the value of {@code alternative.get()}
	 @since 0.0.0
	 */
	Result<V> flatOr(Supplier<? extends Result<? extends V>> alternative);

	/**
	 Returns the {@link Result} of {@code supplier.get()}.
	 If this result is a {@link Failure}, it has a {@link #cause} and the new result is a failure,
	 then this result's cause is added to the new failure as a suppressed exception.

	 @param supplier a supplier whose value's {@link Result} is to replace this result
	 @return the supplier's {@link Result}
	 @since 0.0.0
	 */
	<W> Result<W> replace(ThrowingSupplier<? extends W> supplier);

	/**
	 Returns {@code result}.
	 If this result is a {@link Failure}, it has a {@link #cause} and {@code result} is a failure,
	 then this result's cause is added to {@code result} as a suppressed exception.

	 @param result a {@link Result} that is to replace this result
	 @return {@code result}
	 @since 0.0.0
	 */
	<R extends Result<?>> R flatReplace(R result);

	/**
	 Returns {@code supplier.get()}.
	 If this result is a {@link Failure}, it has a {@link #cause} and the supplied result is a failure,
	 then this result's cause is added to the supplied result as a suppressed exception.

	 @param supplier a supplier of a {@link Result} that is to replace this result
	 @return {@code supplier.get()}
	 @since 0.0.0
	 */
	<R extends Result<?>> R supply(Supplier<? extends R> supplier);

	<W> Result<W> map(ThrowingFunction<V, W> mapper);

	Result<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper);

	Result<V> filter(Predicate<V> predicate);

	Result<V> and(ThrowingRunnable action);

	Result<V> suppress(ThrowingRunnable action);

	Result<V> ifSuccess(Consumer<? super V> action);

	Result<V> ifFailure(Consumer<Throwable> action);

	<E extends Throwable> Result<V> ifFailure(Class<E> type, Consumer<? super E> action);

	Result<V> handle(Consumer<Throwable> handler);

	<E extends Throwable> Result<V> handle(Class<E> type, Consumer<? super E> handler);

	/**
	 Returns a new shallow copy of this result.

	 @return a new shallow copy of this result
	 */
	Result<V> clone();

	/**
	 A successful result containing a {@code value}.

	 @param value the result's value
	 @param <V> the type of the value
	 @since 0.0.0
	 */
	record Success<V>(V value) implements Result<V> {
		@Override public boolean isSuccess() {
			return true;
		}

		@Override public V valueOr(V alternative) {
			return this.value;
		}

		@Override public V valueOrGet(Supplier<? extends V> alternative) {
			return this.value;
		}

		@Override public Success<V> or(ThrowingSupplier<V> alternative) {
			return this;
		}

		@Override public Success<V> flatOr(Supplier<? extends Result<? extends V>> alternative) {
			return this;
		}

		@Override public Optional<Throwable> cause() {
			return Optional.empty();
		}

		@Override public Optional<NoSuchElementException> exception() {
			return Optional.empty();
		}

		@Override public Success<V> asSuccess() {
			return this;
		}

		@Override public Failure<V> asFailure() {
			throw new ClassCastException("Success -> Failure");
		}

		@Override public Result<V> and(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				return new Failure<>(cause, null);
			}
		}

		@Override public <W> Result<W> map(ThrowingFunction<V, W> mapper) {
			return of(() -> mapper.apply(this.value));
		}

		@Override public Success<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper) {
			return this;
		}

		@Override public Result<V> filter(Predicate<V> predicate) {
			return predicate.test(this.value) ? this : new Failure<>(null, null);
		}

		@Override public Result<V> suppress(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				return new Failure<>(null, List.of(cause));
			}
		}

		@Override public <W> Result<W> replace(ThrowingSupplier<? extends W> supplier) {
			return of(supplier);
		}

		@Override public <R extends Result<?>> R flatReplace(R result) {
			return result;
		}

		@Override public <R extends Result<?>> R supply(Supplier<? extends R> supplier) {
			return supplier.get();
		}

		@Override public Success<V> ifSuccess(Consumer<? super V> action) {
			action.accept(this.value());
			return this;
		}

		@Override public Success<V> ifFailure(Consumer<Throwable> action) {
			return this;
		}

		@Override public <E extends Throwable> Success<V> ifFailure(Class<E> type, Consumer<? super E> action) {
			return this;
		}

		@Override public Success<V> handle(Consumer<Throwable> handler) {
			return this;
		}

		@Override public <E extends Throwable> Success<V> handle(Class<E> type, Consumer<? super E> handler) {
			return this;
		}

		@Override public int hashCode() {
			return Objects.hashCode(this.value);
		}

		@Override public boolean equals(Object object) {
			return object instanceof Success<?> result && Objects.equals(this.value, result.value);
		}

		@Override public Success<V> clone() {
			return new Success<>(this.value);
		}

		@Override public String toString() {
			return "Success(" + this.value + ')';
		}
	}

	/**
	 An unsuccessful result without a value but potentially with {@link Result#exception information} about the {@link Result#cause cause} about the failure.

	 @since 0.0.0
	 */
	final class Failure<V> implements Result<V> {
		private final Throwable cause;
		private final List<Throwable> suppressed;

		private Failure(Throwable cause, List<Throwable> suppressed) {
			this.cause = cause;
			this.suppressed = suppressed;
		}

		@Override public boolean isSuccess() {
			return false;
		}

		@Override public V value() {
			throw this.newException();
		}

		@Override public V valueOr(V alternative) {
			return alternative;
		}

		@Override public V valueOrGet(Supplier<? extends V> alternative) {
			return alternative.get();
		}

		@Override public Result<V> or(ThrowingSupplier<V> alternative) {
			return this.flatOr(() -> of(alternative));
		}

		@Override public Result<V> flatOr(Supplier<? extends Result<? extends V>> alternative) {
			return (Result<V>) alternative.get().ifFailure(cause -> {
				if (cause != null && this.cause != null) {
					cause.addSuppressed(this.cause);
				}
			});
		}

		@Override public Optional<Throwable> cause() {
			return Optional.ofNullable(this.cause);
		}

		@Override public Optional<NoSuchElementException> exception() {
			return Optional.of(this.newException());
		}

		@Override public Success<V> asSuccess() {
			throw this.newException();
		}

		@Override public Failure<V> asFailure() {
			return this;
		}

		@Override public Failure<V> and(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				if (this.cause == null) {
					return new Failure<>(cause, this.suppressed);
				}

				this.cause.addSuppressed(cause);
				return new Failure<>(this.cause, null);
			}
		}

		@Override public <W> Failure<W> map(ThrowingFunction<V, W> mapper) {
			return (Failure<W>) this;
		}

		@Override public Failure<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper) {
			return (Failure<V>) mapper.apply(this.cause);
		}

		@Override public Failure<V> filter(Predicate<V> predicate) {
			return this;
		}

		@Override public Failure<V> suppress(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				if (this.cause == null) {
					var suppressed = this.suppressed == null ? new ArrayList<Throwable>() : new ArrayList<>(this.suppressed);
					suppressed.add(cause);

					return new Failure<>(null, suppressed);
				}

				this.cause.addSuppressed(cause);
				return new Failure<>(this.cause, null);
			}
		}

		@Override public <W> Result<W> replace(ThrowingSupplier<? extends W> supplier) {
			return this.flatReplace(of(supplier));
		}

		@Override public <R extends Result<?>> R flatReplace(R result) {
			return (R) result.ifFailure(cause -> {
				if (this.cause != null) {
					cause.addSuppressed(this.cause);
				} else if (this.suppressed != null) {
					this.suppressed.forEach(cause::addSuppressed);
				}
			});
		}

		@Override public <R extends Result<?>> R supply(Supplier<? extends R> supplier) {
			return this.flatReplace(supplier.get());
		}

		@Override public Failure<V> ifSuccess(Consumer<? super V> action) {
			return this;
		}

		@Override public Failure<V> ifFailure(Consumer<Throwable> action) {
			action.accept(this.cause);
			return new Failure<>(this.cause, null);
		}

		@Override public <E extends Throwable> Failure<V> ifFailure(Class<E> type, Consumer<? super E> action) {
			if (type.isInstance(this.cause)) {
				action.accept((E) this.cause);
				return new Failure<>(this.cause, null);
			}

			return this;
		}

		@Override public Failure<V> handle(Consumer<Throwable> handler) {
			handler.accept(this.cause);
			return new Failure<>(null, null);
		}

		@Override public <E extends Throwable> Failure<V> handle(Class<E> type, Consumer<? super E> handler) {
			if (type.isInstance(this.cause)) {
				handler.accept((E) this.cause);
				return new Failure<>(null, null);
			}

			return this;
		}

		@Override public int hashCode() {
			return Objects.hash(this.cause, this.suppressed);
		}

		@Override public boolean equals(Object object) {
			return object instanceof Failure<?> result && Objects.equals(this.cause, result.cause) && Objects.equals(this.suppressed, result.suppressed);
		}

		@Override public Failure<V> clone() {
			return new Failure<>(this.cause, new ArrayList<>(this.suppressed));
		}

		@Override public String toString() {
			return "Failure(" + this.cause + ')';
		}

		private NoSuchElementException newException() {
			var exception = new NoSuchElementException("no value", this.cause);

			if (this.suppressed != null) {
				this.suppressed.forEach(exception::addSuppressed);
			}

			return exception;
		}
	}
}
