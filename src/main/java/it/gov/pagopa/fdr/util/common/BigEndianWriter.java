package it.gov.pagopa.fdr.util.common;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * The class permits to write Java types into an OutputStream using Big-Endian byte order,
 * tailored for the PostgreSQL Binary COPY format.
 */
public class BigEndianWriter {

  /**
   * The method writes a 16-bit integer (short) in big-endian byte order.
   */
  public static void writeInt16(OutputStream out, int value) throws IOException {
    byte[] buffer = new byte[2];
    buffer[0] = (byte)(value >>> 8);
    buffer[1] = (byte)(value);
    out.write(buffer);
  }

  /**
   * The method writes a 32-bit integer in big-endian byte order.
   */
  public static void writeInt32(OutputStream out, int value) throws IOException {
    byte[] buffer = new byte[4];
    buffer[0] = (byte)(value >>> 24);
    buffer[1] = (byte)(value >>> 16);
    buffer[2] = (byte)(value >>> 8);
    buffer[3] = (byte)(value);
    out.write(buffer);
  }

  /**
   * The method writes a 64-bit long in big-endian byte order.
   */
  private static void writeInt64(OutputStream out, long value) throws IOException {
    byte[] buffer = new byte[8];
    buffer[0] = (byte)(value >>> 56);
    buffer[1] = (byte)(value >>> 48);
    buffer[2] = (byte)(value >>> 40);
    buffer[3] = (byte)(value >>> 32);
    buffer[4] = (byte)(value >>> 24);
    buffer[5] = (byte)(value >>> 16);
    buffer[6] = (byte)(value >>> 8);
    buffer[7] = (byte)(value);
    out.write(buffer);
  }

  /**
   * The method writes a Long value as PostgreSQL BIGINT (8-byte integer).
   * If the value is null, it writes -1 as the length.
   */
  public static void writeBigInt(OutputStream out, Long value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of BIGINT (8 bytes)
      writeInt64(out, value);
    }
  }

  /**
   * The method writes a string as UTF-8.
   * Adds a 32-bit length header. If the value is null, it writes -1 as the length.
   */
  public static void writeText(OutputStream out, String value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      writeInt32(out, bytes.length); // Length of VARCHAR (string size * 1 byte)
      out.write(bytes);
    }
  }


  /**
   * The method writes a BigDecimal as PostgreSQL NUMERIC.
   * Uses the correct PostgreSQL binary NUMERIC format with base-10000 digits.
   *
   * PostgreSQL NUMERIC binary format:
   *   - ndigits: number of base-10000 digits
   *   - weight: index of the most significant digit (0 = units position in base-10000)
   *   - sign: 0x0000 positive, 0x4000 negative
   *   - dscale: number of decimal digits after the decimal point
   *   - digits[]: array of base-10000 digits, most significant first
   *
   * Example: 0.01
   *   In base-10000: 0.01 = 100 * 10000^(-1)  →  digits=[100], weight=-1, dscale=2
   *
   * Example: 123.45
   *   In base-10000: 123.45 = 123 * 10000^0 + 4500 * 10000^(-1) →  digits=[123,4500], weight=0, dscale=2
   */
  public static void writeNumeric(OutputStream out, BigDecimal value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
      return;
    }

    // Handle zero as special case
    if (value.compareTo(BigDecimal.ZERO) == 0) {
      writeInt32(out, 8);
      writeInt16(out, 0); // ndigits
      writeInt16(out, 0); // weight
      writeInt16(out, 0); // sign (positive)
      writeInt16(out, Math.max(0, value.scale())); // dscale
      return;
    }

    boolean isNegative = value.signum() < 0;
    BigDecimal absValue = value.abs();
    int dscale = Math.max(0, absValue.scale()); // decimal digits after point

    // Number of base-10000 "fractional" groups needed to represent dscale decimal digits.
    // e.g. dscale=2 → 1 group (holds up to 4 decimal digits)
    // e.g. dscale=5 → 2 groups
    int fracGroups = (dscale + 3) / 4;

    // Pad the unscaled integer so it aligns to a base-10000 boundary.
    // e.g. 0.01 → unscaledInt=1, fracGroups=1, paddingPow=2 → paddedInt=100
    // e.g. 123.45 → unscaledInt=12345, fracGroups=1, paddingPow=2 → paddedInt=1234500
    java.math.BigInteger unscaledInt = absValue.unscaledValue();
    int paddingPow = fracGroups * 4 - dscale;
    if (paddingPow > 0) {
      unscaledInt = unscaledInt.multiply(java.math.BigInteger.TEN.pow(paddingPow));
    }

    // Decompose paddedInt into base-10000 digits (LSB first)
    java.math.BigInteger base = java.math.BigInteger.valueOf(10000);
    java.util.List<Integer> digitsList = new java.util.ArrayList<>();
    while (unscaledInt.compareTo(java.math.BigInteger.ZERO) > 0) {
      java.math.BigInteger[] divRem = unscaledInt.divideAndRemainder(base);
      digitsList.add(divRem[1].intValue());
      unscaledInt = divRem[0];
    }

    int ndigits = digitsList.size();
    // weight = number of integer base-10000 groups - 1
    // = (total groups - fractional groups) - 1
    int weight = ndigits - fracGroups - 1;

    int totalSize = 8 + ndigits * 2;
    writeInt32(out, totalSize);
    writeInt16(out, ndigits);
    writeInt16(out, weight);
    writeInt16(out, isNegative ? 0x4000 : 0x0000);
    writeInt16(out, dscale);

    // Write digits from MSB to LSB
    for (int i = ndigits - 1; i >= 0; i--) {
      writeInt16(out, digitsList.get(i));
    }
  }


  /**
   * The method writes an Instant as a PostgreSQL TIMESTAMP.
   * Converts the timestamp to microseconds since the PostgreSQL epoch (2000-01-01 00:00:00 UTC).
   */
  public static void writeTimestamp(OutputStream out, Instant instant) throws IOException {
    if (instant == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of TIMESTAMP (8 bytes)
      long pgEpoch = Instant.parse("2000-01-01T00:00:00Z").getEpochSecond();
      long microseconds = (instant.getEpochSecond() - pgEpoch) * 1_000_000L + instant.getNano() / 1000L;
      writeInt64(out, microseconds);
    }
  }
}
