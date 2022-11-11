package net.auoeke.result;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 A container of a result of some computation or exceptions that occurred therein.
 An instance of {@link Result} can be a {@link Success} or a {@link Failure}.
 <p>
 A success contains a {@link #value} of type {@code V?};
 which can also be queried by the methods {@link #valueOr}, {@link #valueOrNull} and {@link #valueOrGet}.
 <br>A success with value {@code V} may be denoted by {@code Success(V)}.
 <p>
 A failure can contain an exception that is known as its {@link #cause} and 1 or more {@link #multiplySuppressed suppressed} exceptions;
 but it may be empty. The method {@link #exception} can be used in order to get an exception describing
 the reason for failure and is thrown by {@link Failure#value}.
 <br>A failure with cause {@code C} may be denoted by {@code Failure(C)}.

 @param <V> the type of the successful result of the computation
 @since 0.0.0
 */
public sealed interface Result<V> {
	/**
	 Returns a {@code Success(value)}.

	 @param value the value of the success; which may be {@code null}
	 @param <V> the type of {@code value}
	 @return a {@code Success(value)}
	 @since 0.0.0
	 */
	static <V> Success<V> success(V value) {
		return new Success<>(value);
	}

	/**
	 Returns a {@code Failure(cause)}.

	 @param cause the exception of the failure; which may be {@code null}
	 @param <V> the type of the value
	 @return a {@code Failure(cause)}
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
	 Returns a {@link Result} equivalent to {@code optional}.
	 If it has a value, then the result is a {@code Success(optional.get())};
	 otherwise the result is a {@code Failure(null)}.

	 @param optional an optional
	 @param <V> the type of the value
	 @return a result equivalent to {@code optional}
	 @since 0.1.0
	 */
	static <V> Result<V> of(Optional<V> optional) {
		return optional.isPresent() ? new Success<>(optional.get()) : new Failure<>(null, null);
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
	 If the failure does not have a cause and has {@link #multiplySuppressed suppressed} exceptions,
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
	Result<V> or(ThrowingSupplier<? extends V> alternative);

	/**
	 Returns {@code this} if this result {@link #isSuccess is a success} or else the value of {@code alternative.get()}.
	 If {@code alternative} returns a {@link Failure}, then

	 @param alternative a supplier of an alternative result if this result {@link #isFailure is a failure}
	 @return {@code this} if this result is a success or else the value of {@code alternative.get()}
	 @since 0.0.0
	 */
	Result<V> flatOr(Supplier<? extends Result<? extends V>> alternative);

	/**
	 Returns {@code this} if this result is a {@link Failure} or if it is a {@link Success}
	 and the application of {@code action} to its value is successful.
	 Otherwise, this method returns a new {@code Failure} containing the exception thrown by {@code action}.

	 @param action a consumer to call—if this result is a {@code Success}—with its value
	 @return if this result is a {@code Success} and {@code action} throws, a {@code Failure}; otherwise {@code this}
	 @since 0.0.0
	 */
	Result<V> and(ThrowingConsumer<? super V> action);

	Result<V> multiply(ThrowingRunnable action);

	Result<V> multiplySuppressed(ThrowingRunnable action);

	/**
	 Returns the {@link Result} of {@code supplier.get()}.
	 If this result is a {@link Failure}, it has a {@link #cause} and the new result is a failure,
	 then this result's cause is added to the new failure as a suppressed exception.

	 @param supplier a supplier whose value's {@link Result} is to replace this result
	 @return the supplier's {@link Result}
	 @since 0.0.0
	 */
	<W> Result<W> thenResult(ThrowingSupplier<? extends W> supplier);

	/**
	 Returns {@code result} if it or this result is a {@link Success}.
	 Otherwise, this result's exceptions are merged with {@code result}'s
	 exceptions in a {@link Failure} which is returned.
	 <p>
	 If the new failure has a cause, then this failure's cause is
	 {@link Throwable#addSuppressed suppressed}; otherwise the returned failure
	 derives it from this failure.

	 @param result a {@link Result} that is to replace this result
	 @return {@code result}
	 @since 0.0.0
	 */
	<R extends Result<?>> R then(R result);

	/**
	 Returns {@code supplier.get()}.
	 If this result is a {@link Failure}, it has a {@link #cause} and the supplied result is a failure,
	 then this result's cause is added to the supplied result as a suppressed exception.

	 @param supplier a supplier of a {@link Result} that is to replace this result
	 @return {@code supplier.get()}
	 @since 0.0.0
	 */
	<R extends Result<?>> R thenSupply(Supplier<? extends R> supplier);

	/**
	 If this result is a {@code Success}, then this method returns a {@code Result} of applying {@code mapper} to its value;
	 otherwise it returns {@code this}.

	 @param mapper a function to apply—if this result is a {@code Success}—to its value
	 @param <W> the type of the value returned by {@code mapper}
	 @return if this result is a {@code Success}, a {@code Result} of applying {@code mapper} to its value; else {@code this}
	 @since 0.0.0
	 */
	<W> Result<W> map(ThrowingFunction<? super V, ? extends W> mapper);

	/**
	 If this result is a {@code Success}, then this method returns the result of applying {@code mapper} to its value;
	 otherwise it returns {@code this}.

	 @param mapper a {@code Result}-returning function to apply—if this result is a {@code Success}—to its value
	 @param <W> the type of the value of the result returned by {@code mapper}
	 @return if this result is a {@code Success}, the result of applying {@code mapper} to its value; else {@code this}
	 @since 0.3.0
	 */
	<W> Result<W> flatMap(Function<? super V, ? extends Result<W>> mapper);

	Result<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper);

	/**
	 If this result is a {@code Success}, then this method returns {@code Failure(null)}
	 if its value does not match {@code predicate} or the {@link Result} of {@code predicate} if it throws;
	 <br>otherwise it returns {@code this}.

	 @param predicate a predicate against which to test this result
	 @return a {@code Failure} if this result is a {@code Success}
	 and its value does not match {@code predicate} or it throws; otherwise {@code this}
	 @since 0.0.0
	 */
	Result<V> filter(ThrowingPredicate<V> predicate);

	/**
	 Returns a {@code Failure(null)} if this result is a {@code Success(null)};
	 otherwise returns {@code this}.

	 @return a {@code Failure(null)} if this result is a {@code Success(null)} or else {@code this}
	 @since 0.2.0
	 */
	Result<V> filterNotNull();

	Result<V> ifFailure(ThrowingConsumer<? super Throwable> action);

	<E extends Throwable> Result<V> ifFailure(Class<E> type, ThrowingConsumer<? super E> action);

	Result<V> handle(ThrowingConsumer<? super Throwable> handler);

	<E extends Throwable> Result<V> handle(Class<E> type, ThrowingConsumer<? super E> handler);

	/**
	 Returns a new shallow copy of this result.

	 @return a new shallow copy of this result
	 */
	Result<V> clone();

	private static <T> T cast(Object object) {
		return (T) object;
	}

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

		@Override public Success<V> or(ThrowingSupplier<? extends V> alternative) {
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

		@Override public Result<V> multiply(ThrowingRunnable action) {
			return this.and(v -> action.run());
		}

		@Override public <W> Result<W> map(ThrowingFunction<? super V, ? extends W> mapper) {
			return of(() -> mapper.apply(this.value));
		}

		@Override public <W> Result<W> flatMap(Function<? super V, ? extends Result<W>> mapper) {
			return mapper.apply(this.value);
		}

		@Override public Success<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper) {
			return this;
		}

		@Override public Result<V> filter(ThrowingPredicate<V> predicate) {
			try {
				return predicate.test(this.value) ? this : new Failure<>(null, null);
			} catch (Throwable trouble) {
				return new Failure<>(trouble, null);
			}
		}

		@Override public Result<V> filterNotNull() {
			return this.filter(Objects::nonNull);
		}

		@Override public Result<V> multiplySuppressed(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				return new Failure<>(null, List.of(cause));
			}
		}

		@Override public <W> Result<W> thenResult(ThrowingSupplier<? extends W> supplier) {
			return of(supplier);
		}

		@Override public <R extends Result<?>> R then(R result) {
			return result;
		}

		@Override public <R extends Result<?>> R thenSupply(Supplier<? extends R> supplier) {
			return supplier.get();
		}

		@Override public Result<V> and(ThrowingConsumer<? super V> action) {
			try {
				action.accept(this.value());
				return this;
			} catch (Throwable trouble) {
				return new Failure<>(trouble, null);
			}
		}

		@Override public Success<V> ifFailure(ThrowingConsumer<? super Throwable> action) {
			return this;
		}

		@Override public <E extends Throwable> Success<V> ifFailure(Class<E> type, ThrowingConsumer<? super E> action) {
			return this;
		}

		@Override public Success<V> handle(ThrowingConsumer<? super Throwable> handler) {
			return this;
		}

		@Override public <E extends Throwable> Success<V> handle(Class<E> type, ThrowingConsumer<? super E> handler) {
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

		@Override public Result<V> or(ThrowingSupplier<? extends V> alternative) {
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

		@Override public Failure<V> multiply(ThrowingRunnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable cause) {
				if (this.cause == null) {
					return new Failure<>(cause, this.suppressed == null ? null : List.copyOf(this.suppressed));
				}

				this.cause.addSuppressed(cause);
				return new Failure<>(this.cause, null);
			}
		}

		@Override public <W> Failure<W> map(ThrowingFunction<? super V, ? extends W> mapper) {
			return (Failure<W>) this;
		}

		@Override public <W> Failure<W> flatMap(Function<? super V, ? extends Result<W>> mapper) {
			return (Failure<W>) this;
		}

		@Override public Failure<V> flatMapFailure(Function<? super Throwable, ? extends Result<? extends V>> mapper) {
			return (Failure<V>) mapper.apply(this.cause);
		}

		@Override public Failure<V> filter(ThrowingPredicate<V> predicate) {
			return this;
		}

		@Override public Result<V> filterNotNull() {
			return this;
		}

		@Override public Failure<V> multiplySuppressed(ThrowingRunnable action) {
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

		@Override public <W> Result<W> thenResult(ThrowingSupplier<? extends W> supplier) {
			return this.then(of(supplier));
		}

		@Override public <R extends Result<?>> R then(R result) {
			return (R) result.flatMapFailure(cause -> {
				var failure = (Failure<?>) result;

				if (cause == null) {
					if (this.cause != null) {
						var suppressed = failure.suppressed;

						if (suppressed != null) {
							suppressed.forEach(this.cause::addSuppressed);
						}

						return cast(this);
					}

					if (this.suppressed != null && failure.suppressed != null) {
						var suppressed = new ArrayList<>(failure.suppressed);
						suppressed.addAll(this.suppressed);

						return new Failure<>(null, suppressed);
					}

					return cast(failure.suppressed == null ? this : failure);
				}

				if (this.cause != null) {
					cause.addSuppressed(this.cause);
				} else if (this.suppressed != null) {
					this.suppressed.forEach(cause::addSuppressed);
				}

				return cast(failure);
			});
		}

		@Override public <R extends Result<?>> R thenSupply(Supplier<? extends R> supplier) {
			return this.then(supplier.get());
		}

		@Override public Failure<V> and(ThrowingConsumer<? super V> action) {
			return this;
		}

		@Override public Failure<V> ifFailure(ThrowingConsumer<? super Throwable> action) {
			try {
				action.accept(this.cause);
				return this;
			} catch (Throwable trouble) {
				return this.then(new Failure<>(trouble, null));
			}
		}

		@Override public <E extends Throwable> Failure<V> ifFailure(Class<E> type, ThrowingConsumer<? super E> action) {
			return type.isInstance(this.cause) ? this.ifFailure((ThrowingConsumer<Throwable>) action) : this;
		}

		@Override public Failure<V> handle(ThrowingConsumer<? super Throwable> handler) {
			try {
				handler.accept(this.cause);
				return new Failure<>(null, null);
			} catch (Throwable trouble) {
				return this.then(new Failure<>(trouble, null));
			}
		}

		@Override public <E extends Throwable> Failure<V> handle(Class<E> type, ThrowingConsumer<? super E> handler) {
			return type.isInstance(this.cause) ? this.handle((ThrowingConsumer<Throwable>) handler) : this;
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
