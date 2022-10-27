package net.auoeke.result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 A wrapper of a result of some computation or errors that occurred therein.

 @param <T> the type of the result of the computation
 @since 4.9.0
 */
public abstract sealed class Result<T> {
	public static <T> Result<T> success(T value) {
		return new Success<>(value);
	}

	public static <T> Result<T> failure(Throwable trouble) {
		return new Failure<>(trouble, null);
	}

	public static <T> Result<T> of(Supplier<T> action) {
		return new Success<T>(null).map(action);
	}

	public abstract boolean isSuccess();

	public final boolean isFailure() {
		return !this.isSuccess();
	}

	/**
	 If a value is present, then retrieve it; otherwise throw the exception.

	 @return the value if present
	 @throws Throwable if {@code this} is a {@link #isFailure}
	 */
	public abstract T value();

	public abstract T or(T alternative);

	public abstract T orGet(Supplier<T> alternative);

	public abstract T orNull();

	public abstract Result<T> and(Runnable action);

	public abstract Result<T> andSuppress(Runnable action);

	public abstract Result<T> map(Supplier<T> action);

	public abstract void ifSuccess(Consumer<T> action);

	public abstract void ifFailure(Consumer<Throwable> action);

	public static final class Success<T> extends Result<T> {
		private final T value;

		public Success(T value) {
			this.value = value;
		}

		@Override public boolean isSuccess() {
			return true;
		}

		@Override public T value() {
			return this.value;
		}

		@Override public T or(T alternative) {
			return this.value;
		}

		@Override public T orGet(Supplier<T> alternative) {
			return this.value;
		}

		@Override public T orNull() {
			return this.value;
		}

		@Override public Result<T> and(Runnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable trouble) {
				return new Failure<>(trouble, null);
			}
		}

		@Override public Result<T> andSuppress(Runnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable trouble) {
				return new Failure<>(null, List.of(trouble));
			}
		}

		@Override public Result<T> map(Supplier<T> action) {
			try {
				return new Success<>(action.get());
			} catch (Throwable trouble) {
				return new Failure<>(trouble, null);
			}
		}

		@Override public void ifSuccess(Consumer<T> action) {
			action.accept(this.value());
		}

		@Override public void ifFailure(Consumer<Throwable> action) {}
	}

	public static final class Failure<T> extends Result<T> {
		private final Throwable value;
		private final List<Throwable> suppressed;

		private Failure(Throwable value, List<Throwable> suppressed) {
			this.value = value;
			this.suppressed = suppressed;
		}

		@Override public boolean isSuccess() {
			return false;
		}

		@Override public T value() {
			var exception = new RuntimeException("no value", this.value);

			if (this.value == null) {
				this.suppressed.forEach(exception::addSuppressed);
			}

			throw exception;
		}

		@Override public T or(T alternative) {
			return alternative;
		}

		@Override public T orGet(Supplier<T> alternative) {
			return alternative.get();
		}

		@Override public T orNull() {
			return null;
		}

		@Override public Result<T> and(Runnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable trouble) {
				if (this.value == null) {
					return new Failure<>(trouble, this.suppressed);
				}

				this.value.addSuppressed(trouble);
				return new Failure<>(this.value, null);
			}
		}

		@Override public Result<T> andSuppress(Runnable action) {
			try {
				action.run();
				return this;
			} catch (Throwable trouble) {
				if (this.value == null) {
					var suppressed = this.suppressed == null ? new ArrayList<Throwable>() : this.suppressed;
					suppressed.add(trouble);

					return new Failure<>(null, suppressed);
				}

				this.value.addSuppressed(trouble);
				return new Failure<>(this.value, null);
			}
		}

		@Override public Result<T> map(Supplier<T> action) {
			try {
				return new Success<>(action.get());
			} catch (Throwable trouble) {
				if (this.value != null) {
					trouble.addSuppressed(this.value);
				}

				if (this.suppressed != null) {
					this.suppressed.forEach(trouble::addSuppressed);
					this.suppressed.clear();
				}

				return new Failure<>(trouble, this.suppressed);
			}
		}

		@Override public void ifSuccess(Consumer<T> action) {}

		@Override public void ifFailure(Consumer<Throwable> action) {
			action.accept(this.value);
		}
	}
}
