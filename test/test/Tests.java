package test;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;
import lib.ModernIO;
import lib.Util;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class Tests {
	static final Path resources = Path.of("test/resources");

	@Test void test() {
		ModernIO.readString(resources.resolve("bar"))
			.and(text -> System.out.println("success: " + text))
			.ifFailure(trouble -> System.err.print("failure: "))
			.handle(NoSuchFileException.class, trouble -> System.err.printf("file `%s` not found%n", trouble.getFile()))
			.handle(Throwable.class, System.err::println);

		assert ModernIO.mkdirs(resources.resolve("foo")).isFailure();
		assert ModernIO.mkdirs(resources.resolve("bar")).isSuccess();
		assert Util.parseNumber("123e9999999").isSuccess();
		assert Util.parseNumber("1729").isSuccess();
		assert Util.parseNumber("NaN").isSuccess();
		assert Util.parseNumber("abc").isFailure();

		Stream.of(0, 300, 1 << 30, 1L << 62 , Float.MAX_VALUE, Double.MAX_VALUE, "abcd")
			.forEach(object -> Util.parseNumber(object.toString())
				.and(value -> System.out.println(object + " is a " + value.getClass().getName()))
				.ifFailure(trouble -> System.err.println(object + " is not a number"))
			);

		var bp = true;
	}
}
