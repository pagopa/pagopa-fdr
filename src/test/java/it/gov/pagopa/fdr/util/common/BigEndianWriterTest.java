package it.gov.pagopa.fdr.util.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BigEndianWriterTest {

  private ByteArrayOutputStream out;

  @BeforeEach
  void setUp() {
    out = new ByteArrayOutputStream();
  }

  // ─────────────────────────────────────────────
  // writeInt16
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeInt16")
  class WriteInt16 {

    @Test
    @DisplayName("writes 0 as two bytes 0x00 0x00")
    void writeZero() throws IOException {
      BigEndianWriter.writeInt16(out, 0);
      assertArrayEquals(new byte[]{0x00, 0x00}, out.toByteArray());
    }

    @Test
    @DisplayName("writes 256 as 0x01 0x00")
    void write256() throws IOException {
      BigEndianWriter.writeInt16(out, 256);
      assertArrayEquals(new byte[]{0x01, 0x00}, out.toByteArray());
    }

    @Test
    @DisplayName("writes 0x1234 as 0x12 0x34")
    void writeHex1234() throws IOException {
      BigEndianWriter.writeInt16(out, 0x1234);
      assertArrayEquals(new byte[]{0x12, 0x34}, out.toByteArray());
    }

    @Test
    @DisplayName("writes 0xFFFF as two bytes -1")
    void writeMaxUnsigned16() throws IOException {
      BigEndianWriter.writeInt16(out, 0xFFFF);
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, out.toByteArray());
    }
  }

  // ─────────────────────────────────────────────
  // writeInt32
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeInt32")
  class WriteInt32 {

    @Test
    @DisplayName("writes 0 as four 0x00 bytes")
    void writeZero() throws IOException {
      BigEndianWriter.writeInt32(out, 0);
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00}, out.toByteArray());
    }

    @Test
    @DisplayName("writes 1 as 0x00 0x00 0x00 0x01")
    void writeOne() throws IOException {
      BigEndianWriter.writeInt32(out, 1);
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x01}, out.toByteArray());
    }

    @Test
    @DisplayName("writes -1 as four 0xFF bytes (NULL indicator in COPY format)")
    void writeMinusOne() throws IOException {
      BigEndianWriter.writeInt32(out, -1);
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
          out.toByteArray());
    }

    @Test
    @DisplayName("writes 0x12345678 in big-endian order")
    void writeHexValue() throws IOException {
      BigEndianWriter.writeInt32(out, 0x12345678);
      assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, out.toByteArray());
    }
  }

  // ─────────────────────────────────────────────
  // writeBigInt
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeBigInt")
  class WriteBigInt {

    @Test
    @DisplayName("null writes the -1 indicator (4 bytes)")
    void writeNull() throws IOException {
      BigEndianWriter.writeBigInt(out, null);
      byte[] bytes = out.toByteArray();
      assertEquals(4, bytes.length);
      // length = -1
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes);
    }

    @Test
    @DisplayName("writes 0L: header 8, then 8 zero bytes")
    void writeZero() throws IOException {
      BigEndianWriter.writeBigInt(out, 0L);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length); // 4 (length) + 8 (value)
      // header
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x08}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      // value zero
      for (int i = 4; i < 12; i++) {
        assertEquals(0x00, bytes[i]);
      }
    }

    @Test
    @DisplayName("writes 1L correctly")
    void writeOne() throws IOException {
      BigEndianWriter.writeBigInt(out, 1L);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length);
      // header length = 8
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x08}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      // value: 0x00 00 00 00 00 00 00 01
      assertArrayEquals(
          new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01},
          new byte[]{bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11]});
    }

    @Test
    @DisplayName("writes Long.MAX_VALUE correctly")
    void writeLongMaxValue() throws IOException {
      BigEndianWriter.writeBigInt(out, Long.MAX_VALUE);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length);
      // header length = 8
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x08}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      // Long.MAX_VALUE = 0x7FFFFFFFFFFFFFFF
      assertArrayEquals(
          new byte[]{0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
              (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
          new byte[]{bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11]});
    }
  }

  // ─────────────────────────────────────────────
  // writeText
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeText")
  class WriteText {

    @Test
    @DisplayName("null writes the -1 indicator (4 bytes)")
    void writeNull() throws IOException {
      BigEndianWriter.writeText(out, null);
      byte[] bytes = out.toByteArray();
      assertEquals(4, bytes.length);
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes);
    }

    @Test
    @DisplayName("empty string writes length 0 and no data")
    void writeEmptyString() throws IOException {
      BigEndianWriter.writeText(out, "");
      byte[] bytes = out.toByteArray();
      assertEquals(4, bytes.length);
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00}, bytes);
    }

    @Test
    @DisplayName("simple ASCII string: correct length + UTF-8 content")
    void writeAsciiString() throws IOException {
      BigEndianWriter.writeText(out, "ABC");
      byte[] bytes = out.toByteArray();
      // 4 (length header) + 3 (UTF-8 bytes of "ABC")
      assertEquals(7, bytes.length);
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x03}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      assertEquals('A', bytes[4]);
      assertEquals('B', bytes[5]);
      assertEquals('C', bytes[6]);
    }

    @Test
    @DisplayName("multibyte UTF-8 string: length is in bytes, not characters")
    void writeMultibyteString() throws IOException {
      String s = "à"; // 2 UTF-8 bytes: 0xC3 0xA0
      BigEndianWriter.writeText(out, s);
      byte[] bytes = out.toByteArray();
      assertEquals(6, bytes.length); // 4 + 2
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x02}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      assertEquals((byte) 0xC3, bytes[4]);
      assertEquals((byte) 0xA0, bytes[5]);
    }
  }

  // ─────────────────────────────────────────────
  // writeNumeric
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeNumeric")
  class WriteNumeric {

    @Test
    @DisplayName("null writes the -1 indicator (4 bytes)")
    void writeNull() throws IOException {
      BigEndianWriter.writeNumeric(out, null);
      byte[] bytes = out.toByteArray();
      assertEquals(4, bytes.length);
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes);
    }

    @Test
    @DisplayName("zero: 8-byte header with ndigits=0")
    void writeZero() throws IOException {
      BigEndianWriter.writeNumeric(out, BigDecimal.ZERO);
      byte[] bytes = out.toByteArray();
      // 4 (length=8) + 2 (ndigits) + 2 (weight) + 2 (sign) + 2 (dscale) = 12
      assertEquals(12, bytes.length);
      // length = 8
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x08}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      // ndigits = 0
      assertArrayEquals(new byte[]{0x00, 0x00}, new byte[]{bytes[4], bytes[5]});
      // sign = 0 (positive)
      assertArrayEquals(new byte[]{0x00, 0x00}, new byte[]{bytes[8], bytes[9]});
    }

    @Test
    @DisplayName("positive integer 123: one base-10000 digit")
    void writeInteger123() throws IOException {
      BigEndianWriter.writeNumeric(out, new BigDecimal("123"));
      byte[] bytes = out.toByteArray();
      // totalSize = 8 + 1*2 = 10  → length header = 10
      // 4 + 10 = 14 total bytes
      assertEquals(14, bytes.length);
      // ndigits = 1
      int ndigits = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
      assertEquals(1, ndigits);
      // weight = 0  (1 integer group, 0 fractional → 1-0-1=0)
      int weight = (short) (((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF));
      assertEquals(0, weight);
      // sign positive
      int sign = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
      assertEquals(0x0000, sign);
      // dscale = 0
      int dscale = ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
      assertEquals(0, dscale);
      // digit[0] = 123
      int digit0 = ((bytes[12] & 0xFF) << 8) | (bytes[13] & 0xFF);
      assertEquals(123, digit0);
    }

    @Test
    @DisplayName("0.01: one fractional base-10000 digit with dscale=2")
    void writeFractional001() throws IOException {
      BigEndianWriter.writeNumeric(out, new BigDecimal("0.01"));
      byte[] bytes = out.toByteArray();
      // ndigits=1, weight=-1, dscale=2
      assertEquals(14, bytes.length);
      int ndigits = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
      assertEquals(1, ndigits);
      int weight = (short) (((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF));
      assertEquals(-1, weight);
      int sign = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
      assertEquals(0x0000, sign);
      int dscale = ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
      assertEquals(2, dscale);
      // 0.01 in base 10000: digit = 100
      int digit0 = ((bytes[12] & 0xFF) << 8) | (bytes[13] & 0xFF);
      assertEquals(100, digit0);
    }

    @Test
    @DisplayName("123.45: two digits, weight=0, dscale=2")
    void writeMixed12345() throws IOException {
      BigEndianWriter.writeNumeric(out, new BigDecimal("123.45"));
      byte[] bytes = out.toByteArray();
      // ndigits=2: [123, 4500], weight=0, dscale=2
      // totalSize = 8 + 2*2 = 12 → 4+12=16
      assertEquals(16, bytes.length);
      int ndigits = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
      assertEquals(2, ndigits);
      int weight = (short) (((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF));
      assertEquals(0, weight);
      int dscale = ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
      assertEquals(2, dscale);
      // MSB digit = 123
      int digit0 = ((bytes[12] & 0xFF) << 8) | (bytes[13] & 0xFF);
      assertEquals(123, digit0);
      // LSB digit = 4500
      int digit1 = ((bytes[14] & 0xFF) << 8) | (bytes[15] & 0xFF);
      assertEquals(4500, digit1);
    }

    @Test
    @DisplayName("negative value -42.5: sign = 0x4000")
    void writeNegative() throws IOException {
      BigEndianWriter.writeNumeric(out, new BigDecimal("-42.5"));
      byte[] bytes = out.toByteArray();
      int sign = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
      assertEquals(0x4000, sign);
    }

    @Test
    @DisplayName("value with scale > 4: dscale=5 → fracGroups=2")
    void writeLargeScale() throws IOException {
      BigEndianWriter.writeNumeric(out, new BigDecimal("1.12345"));
      byte[] bytes = out.toByteArray();
      int dscale = ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
      assertEquals(5, dscale);
    }
  }

  // ─────────────────────────────────────────────
  // writeTimestamp
  // ─────────────────────────────────────────────
  @Nested
  @DisplayName("writeTimestamp")
  class WriteTimestamp {

    @Test
    @DisplayName("null writes the -1 indicator (4 bytes)")
    void writeNull() throws IOException {
      BigEndianWriter.writeTimestamp(out, null);
      byte[] bytes = out.toByteArray();
      assertEquals(4, bytes.length);
      assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes);
    }

    @Test
    @DisplayName("PostgreSQL epoch (2000-01-01) → microseconds = 0")
    void writePostgresEpoch() throws IOException {
      Instant pgEpoch = Instant.parse("2000-01-01T00:00:00Z");
      BigEndianWriter.writeTimestamp(out, pgEpoch);
      byte[] bytes = out.toByteArray();
      // 4 (length=8) + 8 (value)
      assertEquals(12, bytes.length);
      // length = 8
      assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x08}, new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
      // microseconds = 0
      for (int i = 4; i < 12; i++) {
        assertEquals(0x00, bytes[i], "byte[" + i + "] should be 0");
      }
    }

    @Test
    @DisplayName("2000-01-01T00:00:01Z → microseconds = 1_000_000")
    void writeOneSecondAfterEpoch() throws IOException {
      Instant instant = Instant.parse("2000-01-01T00:00:01Z");
      BigEndianWriter.writeTimestamp(out, instant);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length);
      long micros = 0;
      for (int i = 4; i < 12; i++) {
        micros = (micros << 8) | (bytes[i] & 0xFF);
      }
      assertEquals(1_000_000L, micros);
    }

    @Test
    @DisplayName("2000-01-01T00:00:00.000001Z → microseconds = 1")
    void writeOneMicrosecondAfterEpoch() throws IOException {
      Instant instant = Instant.parse("2000-01-01T00:00:00.000001Z");
      BigEndianWriter.writeTimestamp(out, instant);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length);
      long micros = 0;
      for (int i = 4; i < 12; i++) {
        micros = (micros << 8) | (bytes[i] & 0xFF);
      }
      assertEquals(1L, micros);
    }

    @Test
    @DisplayName("date before PostgreSQL epoch → negative microseconds")
    void writeBeforeEpoch() throws IOException {
      Instant instant = Instant.parse("1999-12-31T23:59:59Z");
      BigEndianWriter.writeTimestamp(out, instant);
      byte[] bytes = out.toByteArray();
      assertEquals(12, bytes.length);
      long micros = 0;
      for (int i = 4; i < 12; i++) {
        micros = (micros << 8) | (bytes[i] & 0xFF);
      }
      // micros are already a signed long; negative value expected
      assertEquals(-1_000_000L, micros);
    }
  }
}

