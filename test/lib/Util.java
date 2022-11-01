package lib;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.auoeke.result.Result;

public class Util {
	public static Result<? extends Number> parseNumber(String number) {
		return Result.<Number>of(() -> Byte.parseByte(number))
			.or(() -> Short.parseShort(number))
			.or(() -> Integer.parseInt(number))
			.or(() -> Long.parseLong(number))
			.or(() -> new BigInteger(number))
			.flatOr(() -> Result.of(() -> Float.parseFloat(number)).filter(f -> !Float.isInfinite(f)))
			.flatOr(() -> Result.of(() -> Double.parseDouble(number)).filter(Double::isFinite))
			.or(() -> new BigDecimal(number));
	}
}
