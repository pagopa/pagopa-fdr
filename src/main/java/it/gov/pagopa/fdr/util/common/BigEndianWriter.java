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
   * The method writes  a BigDecimal as a DOUBLE PRECISION (8-byte float).
   * Note: This involves a precision loss if the BigDecimal exceeds double capacity.
   */
  public static void writeDouble(OutputStream out, BigDecimal value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of DOUBLE PRECISION (8 bytes)
      writeInt64(out, Double.doubleToLongBits(value.doubleValue()));
    }
  }

  /**
   * The method writes a positive Long value for NUMERIC.
   * Decomposes the value into base-10000 digits as required by PostgreSQL.
   */
  public static void writeNumericForPositiveLong(OutputStream out, Long value) throws IOException {
    if (value == null) {
      writeInt32(out, -1);
    } else {
      long rawValue = value;

      // Value is zero: apply a fast-path
      if (rawValue == 0) {
        writeInt32(out, 8);
        writeInt16(out, 0); // ndigits
        writeInt16(out, 0); // weight
        writeInt16(out, 0); // sign
        writeInt16(out, 0); // dscale
      } else {

        // Base-10000 decomposition (max 5 digits for long)
        int[] digits = new int[5];
        int ndigits = 0;

        while (rawValue != 0) {
          digits[ndigits++] = (int) (rawValue % 10000);
          rawValue /= 10000;
        }

        int weight = ndigits - 1;
        int totalSize = 8 + ndigits * 2;

        writeInt32(out, totalSize);
        writeInt16(out, ndigits);
        writeInt16(out, weight);
        writeInt16(out, 0); // sign = positive
        writeInt16(out, 0); // dscale = integer

        // Write from MSB to LSB
        for (int i = ndigits - 1; i >= 0; i--) {
          writeInt16(out, digits[i]);
        }
      }
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
