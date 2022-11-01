package lib;

import java.nio.file.Files;
import java.nio.file.Path;
import net.auoeke.result.Result;

import static net.auoeke.result.Result.of;

public class ModernIO {
	public static Result<String> readString(Path path) {
		return of(() -> Files.readString(path));
	}

	public static Result<Path> mkdir(Path path) {
		return of(() -> Files.createDirectory(path));
	}

	public static Result<Path> mkdirs(Path path) {
		return of(() -> Files.createDirectories(path));
	}
}
