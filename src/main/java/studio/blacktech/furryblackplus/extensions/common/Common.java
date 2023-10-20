package studio.blacktech.furryblackplus.extensions.common;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;

import static studio.blacktech.furryblackplus.core.common.enhance.TimeEnhance.SYSTEM_OFFSET;

public final class Common {

  private static final Base64.Encoder encoder = Base64.getEncoder();
  private static final Base64.Decoder decoder = Base64.getDecoder();

  public static boolean isToday(long time) {
    LocalDate now = LocalDate.now();
    LocalDate that = LocalDate.ofInstant(Instant.ofEpochMilli(time), SYSTEM_OFFSET);
    return now.getYear() == that.getYear() && now.getDayOfYear() == that.getDayOfYear();
  }

  public static String encode(String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    byte[] encoded = encoder.encode(bytes);
    return new String(encoded, StandardCharsets.UTF_8);
  }

  public static String decode(String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    byte[] encoded = decoder.decode(bytes);
    return new String(encoded, StandardCharsets.UTF_8);
  }

  public static String encode(Object content) {
    byte[] bytes = String.valueOf(content).getBytes(StandardCharsets.UTF_8);
    byte[] encoded = encoder.encode(bytes);
    return new String(encoded, StandardCharsets.UTF_8);
  }

  public static String decode(Object content) {
    byte[] bytes = String.valueOf(content).getBytes(StandardCharsets.UTF_8);
    byte[] encoded = decoder.decode(bytes);
    return new String(encoded, StandardCharsets.UTF_8);
  }
}
