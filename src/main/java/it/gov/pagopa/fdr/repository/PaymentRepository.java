package it.gov.pagopa.fdr.repository;

import io.micrometer.core.annotation.Timed;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

  @ConfigProperty(name = "payments.batch.size")
  Integer batchSize;

  @ConfigProperty(name = "quarkus.datasource.jdbc.additional-jdbc-properties.sendBufferSize")
  Integer sendBufferSize;

  public static final String INDEX = "index";
  private final EntityManager entityManager;

  private final Logger log;

  public static final String QUERY_GET_BY_FLOW_ID = "flowId = ?1";

  public static final String QUERY_DELETE_BY_ID = "id in ?1";

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "flowId = ?1" + " and index in ?2";

  public static final String INSERT_IN_BULK =
      "INSERT INTO payment (flow_id, iuv, iur, index, amount, pay_date, pay_status, transfer_id,"
          + " created, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String COPY_PAYMENT_SQL =
      "COPY payment (flow_id, iuv, iur, \"index\", amount, pay_date, pay_status, transfer_id, created, updated) FROM STDIN WITH (FORMAT CSV)";

  private static final String COPY_PAYMENT_BINARY_SQL =
      "COPY payment (flow_id, iuv, iur, \"index\", amount, pay_date, pay_status, transfer_id, created, updated) FROM STDIN WITH (FORMAT BINARY)";

  private static final DateTimeFormatter COPY_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  public PaymentRepository(Logger log, EntityManager em) {
    this.log = log;
    this.entityManager = em;
  }

  public RepositoryPagedResult<PaymentEntity> findByPspAndIuvAndIur(
      String pspId,
      String iuv,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      String orgDomainId,
      int pageNumber,
      int pageSize) {

    StringBuilder query =
        new StringBuilder(
            "SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.flow WHERE p.flow.pspDomainId = :psp");
    Parameters params = new Parameters().and("psp", pspId);

    if (orgDomainId != null) {
      query.append("  and p.flow.orgDomainId = :orgDomainId");
      params.and("orgDomainId", orgDomainId);
    }
    if (iuv != null) {
      query.append(" and iuv = :iuv");
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query.append(" and iur = :iur");
      params.and("iur", iur);
    }
    if (createdFrom != null) {
      query.append(" and p.created >= :createdFrom");
      params.and("createdFrom", createdFrom);
    }
    if (createdTo != null) {
      query.append(" and p.created <= :createdTo");
      params.and("createdTo", createdTo);
    }

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    PanacheQuery<PaymentEntity> resultPage =
        PaymentEntity.findPageByQuery(query.toString(), sort, params).page(page);
    return getPagedResult(resultPage);
  }

  public long countByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {

    return count(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes);
  }

  @Timed(
      value = "paymentRepository.createEntityInBulk.task",
      description = "Time taken to perform createEntityInBulk",
      percentiles = 0.95,
      histogram = true)
  public void createEntityInBulk(List<PaymentEntity> entityBatch) throws SQLException {

    Session session = entityManager.unwrap(Session.class);

    try (PreparedStatement preparedStatement =
        session.doReturningWork(connection -> connection.prepareStatement(INSERT_IN_BULK))) {

      int count = 0;
      for (PaymentEntity payment : entityBatch) {
        payment.exportInPreparedStatement(preparedStatement);
        preparedStatement.addBatch();
        count++;

        if (count % batchSize == 0) {
          preparedStatement.executeBatch();
        }
      }
      preparedStatement.executeBatch();
    } catch (SQLException e) {
      log.error("An error occurred while executing payments bulk insert", e);
      throw e;
    }
  }

  @Timed(
      value = "paymentRepository.createEntityInBulkCopy.task",
      description = "Time taken to perform createEntityInBulkCopy",
      percentiles = 0.95,
      histogram = true)
  public void createEntityInBulkCopy(List<PaymentEntity> entityBatch) throws SQLException {
    Session session = entityManager.unwrap(Session.class);
    try {
      session.doWork(
          connection -> {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();
            byte[] csvPayload = buildCsvPayload(entityBatch);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvPayload)) {
              copyManager.copyIn(COPY_PAYMENT_SQL, inputStream, sendBufferSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
          });
    } catch (RuntimeException e) {
      log.error("An error occurred while executing payments bulk insert via COPY", e);
      throw e;
    }
  }

    @Timed(
            value = "paymentRepository.createEntityInBulkCopyStream.task",
            description = "Time taken to perform createEntityInBulkCopyStream",
            percentiles = 0.95,
            histogram = true)
    public void createEntityInBulkCopyStream(List<PaymentEntity> entityBatch) throws SQLException {
        try {
            // Unwrapping della connessione fisica PostgreSQL
            BaseConnection pgConn = entityManager.unwrap(Session.class)
                    .doReturningWork(conn -> conn.unwrap(BaseConnection.class));
            CopyManager copyManager = new CopyManager(pgConn);

            // Setup del pipe per lo streaming
            PipedInputStream inputStream = new PipedInputStream(65536); // Buffer di 64KB
            PipedOutputStream outputStream = new PipedOutputStream(inputStream);

            // Thread separato per la generazione del CSV per evitare deadlock nel pipe
            ForkJoinPool.commonPool().execute(() -> {
                try (outputStream) {
                    for (PaymentEntity entity : entityBatch) {
                        String row = buildCsvRow(entity);
                        outputStream.write(row.getBytes(StandardCharsets.UTF_8));
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    log.error("Errore durante lo streaming CSV", e);
                }
            });

            // PostgreSQL inizia a leggere dal pipe non appena i primi byte sono pronti
            copyManager.copyIn(COPY_PAYMENT_SQL, inputStream);

        } catch (Exception e) {
            log.error("Errore critico durante COPY bulk load", e);
            throw new RuntimeException(e);
        }
    }

  @Timed(
      value = "paymentRepository.createEntityInBulkCopyBinary.task",
      description = "Time taken to perform createEntityInBulkCopyBinary",
      percentiles = 0.95,
      histogram = true)
  public void createEntityInBulkCopyBinary(List<PaymentEntity> entityBatch) throws SQLException {
    log.infof("Starting COPY BINARY for %d payments", entityBatch.size());
    Session session = entityManager.unwrap(Session.class);
    try {
      session.doWork(
          connection -> {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();

            byte[] binaryPayload = buildBinaryPayload(entityBatch);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryPayload)) {
              copyManager.copyIn(COPY_PAYMENT_BINARY_SQL, inputStream);
            } catch (IOException e) {
                throw new RuntimeException("IOException during COPY BINARY", e);
            } catch (SQLException e) {
                throw new RuntimeException("SQLException during COPY BINARY: " + e.getMessage(), e);
            }
          });
    } catch (RuntimeException e) {
      log.error("An error occurred while executing payments bulk insert via COPY BINARY", e);
      throw e;
    }
  }


    @Timed(
            value = "paymentRepository.createEntityInBulkCopyBinaryStream.task",
            description = "Time taken to perform createEntityInBulkCopyBinaryStream",
            percentiles = 0.95,
            histogram = true)
    public void createEntityInBulkCopyBinaryStream(List<PaymentEntity> entityBatch) {

        log.infof("Starting STREAMING COPY BINARY for %d payments", entityBatch.size());

        try {
            // Unwrapping della connessione fisica PostgreSQL
            BaseConnection pgConn = entityManager.unwrap(Session.class)
                    .doReturningWork(conn -> conn.unwrap(BaseConnection.class));
            CopyManager copyManager = new CopyManager(pgConn);

            // Setup del pipe per lo streaming binario
            // Un buffer di 1MB (1048576) è ideale per saturare la banda di rete Azure GP
            PipedInputStream inputStream = new PipedInputStream(1048576);
            PipedOutputStream outputStream = new PipedOutputStream(inputStream);

            // Task asincrono per la generazione del payload binario
            ForkJoinPool.commonPool().execute(() -> {
                try (OutputStream out = outputStream) {
                    // 1. Binary header signature
                    out.write("PGCOPY\n\377\r\n\0".getBytes(StandardCharsets.ISO_8859_1));
                    // 2. Flags field (32-bit, 0) + Header extension (32-bit, 0)
                    writeInt32(out, 0);
                    writeInt32(out, 0);

                    // 3. Write each row
                    for (PaymentEntity payment : entityBatch) {
                        writeInt16(out, 10); // 10 columns

                        writeNumericFromLong(out, payment.getFlowId());
                        writeText(out, payment.getIuv());
                        writeText(out, payment.getIur());
                        writeNumericFromLong(out, payment.getIndex());
                        writeDouble(out, payment.getAmount());
                        writeTimestamp(out, payment.getPayDate());
                        writeText(out, payment.getPayStatus());
                        writeNumericFromLong(out, payment.getTransferId());
                        writeTimestamp(out, payment.getCreated());
                        writeTimestamp(out, payment.getUpdated());
                    }

                    // 4. File trailer (-1 as int16)
                    writeInt16(out, -1);
                    out.flush();
                } catch (Exception e) {
                    log.error("Errore fatale durante lo streaming BINARY verso DB", e);
                }
            });

            // Esecuzione del comando COPY BINARY leggendo dallo stream
            copyManager.copyIn(COPY_PAYMENT_BINARY_SQL, inputStream, sendBufferSize);

        } catch (Exception e) {
            log.error("Errore critico durante COPY BINARY bulk load", e);
            throw new RuntimeException(e);
        }
    }

  private byte[] buildBinaryPayload(List<PaymentEntity> entityBatch) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      // Binary header signature
      out.write("PGCOPY\n\377\r\n\0".getBytes(StandardCharsets.ISO_8859_1));

      // Flags field (32-bit integer, 0 = no OIDs)
      writeInt32(out, 0);

      // Header extension area length (32-bit integer, 0 = no extensions)
      writeInt32(out, 0);

      // Write each row
      for (PaymentEntity payment : entityBatch) {
        // Field count (16-bit integer)
        writeInt16(out, 10); // 10 columns

        // flow_id (NUMERIC) - NOT BIGINT!
        writeNumericFromLong(out, payment.getFlowId());
        //writeBigInt(out, payment.getFlowId());

        // iuv (TEXT)
        writeText(out, payment.getIuv());

        // iur (TEXT)
        writeText(out, payment.getIur());

        // index (NUMERIC) - NOT BIGINT!
        writeNumericFromLong(out, payment.getIndex());

        // amount (DOUBLE PRECISION)
        writeDouble(out, payment.getAmount());

        // pay_date (TIMESTAMP)
        writeTimestamp(out, payment.getPayDate());

        // pay_status (TEXT)
        writeText(out, payment.getPayStatus());

        // transfer_id (NUMERIC) - NOT BIGINT!
        writeNumericFromLong(out, payment.getTransferId());

        // created (TIMESTAMP)
        writeTimestamp(out, payment.getCreated());

        // updated (TIMESTAMP)
        writeTimestamp(out, payment.getUpdated());
      }

      // File trailer (16-bit integer -1)
      writeInt16(out, -1);

    } catch (IOException e) {
      throw new RuntimeException("Error building binary payload", e);
    }

    byte[] result = out.toByteArray();
    log.infof("Binary payload complete: %d total bytes for %d payments", result.length, entityBatch.size());
    return result;
  }

  private void writeInt16(ByteArrayOutputStream out, int value) throws IOException {
    out.write((value >> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  private void writeInt32(ByteArrayOutputStream out, int value) throws IOException {
    out.write((value >> 24) & 0xFF);
    out.write((value >> 16) & 0xFF);
    out.write((value >> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  private void writeInt64(ByteArrayOutputStream out, long value) throws IOException {
    out.write((int) ((value >> 56) & 0xFF));
    out.write((int) ((value >> 48) & 0xFF));
    out.write((int) ((value >> 40) & 0xFF));
    out.write((int) ((value >> 32) & 0xFF));
    out.write((int) ((value >> 24) & 0xFF));
    out.write((int) ((value >> 16) & 0xFF));
    out.write((int) ((value >> 8) & 0xFF));
    out.write((int) (value & 0xFF));
  }

  private void writeBigInt(ByteArrayOutputStream out, Long value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of BIGINT (8 bytes)
      writeInt64(out, value);
    }
  }

  private void writeText(ByteArrayOutputStream out, String value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      writeInt32(out, bytes.length); // Length of data
      out.write(bytes);
    }
  }

  private void writeDouble(ByteArrayOutputStream out, BigDecimal value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of DOUBLE PRECISION (8 bytes)
      double doubleValue = value.doubleValue();
      long bits = Double.doubleToLongBits(doubleValue);
      writeInt64(out, bits);
    }
  }

  private void writeNumericFromLong(ByteArrayOutputStream out, Long value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      // Convert Long to BigDecimal and use writePostgreSQLNumeric
      BigDecimal decimal = new BigDecimal(value);
      writePostgreSQLNumeric(out, decimal);
    }
  }

  private void writeNumeric(ByteArrayOutputStream out, BigDecimal value) throws IOException {
    if (value == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      // PostgreSQL NUMERIC binary format is complex (ndigits, weight, sign, dscale, digits)
      // For simplicity and compatibility, we implement the full NUMERIC format
      writePostgreSQLNumeric(out, value);
    }
  }

  private void writePostgreSQLNumeric(ByteArrayOutputStream out, BigDecimal value) throws IOException {
    // PostgreSQL NUMERIC binary format:
    // - ndigits (int16): number of base-10000 digits
    // - weight (int16): weight of first digit (can be negative for pure fractional values)
    // - sign (int16): NUMERIC_POS=0x0000, NUMERIC_NEG=0x4000, NUMERIC_NAN=0xC000
    // - dscale (int16): display scale
    // - digits (int16[]): base-10000 digits

    // Handle special case of zero
    if (value.compareTo(BigDecimal.ZERO) == 0) {
      writeInt32(out, 8); // Size: 4 int16s, no digits
      writeInt16(out, 0); // ndigits
      writeInt16(out, 0); // weight
      writeInt16(out, 0x0000); // sign (positive)
      writeInt16(out, 0); // dscale
      return;
    }

    String strValue = value.toPlainString();
    boolean isNegative = value.signum() < 0;
    if (isNegative) {
      strValue = strValue.substring(1); // Remove minus sign
    }

    String[] parts = strValue.split("\\.");
    String integerPart = parts[0];
    String fractionalPart = parts.length > 1 ? parts[1] : "";
    int dscale = fractionalPart.length();

    // Pad integer part to groups of 4 from the left
    while (integerPart.length() % 4 != 0) {
      integerPart = "0" + integerPart;
    }

    // Pad fractional part to groups of 4 from the right
    while (fractionalPart.length() % 4 != 0) {
      fractionalPart = fractionalPart + "0";
    }

    // Convert to base-10000 digits
    int[] intDigits = new int[integerPart.length() / 4];
    for (int i = 0; i < intDigits.length; i++) {
      intDigits[i] = Integer.parseInt(integerPart.substring(i * 4, (i + 1) * 4));
    }

    int[] fracDigits = new int[fractionalPart.length() / 4];
    for (int i = 0; i < fracDigits.length; i++) {
      fracDigits[i] = Integer.parseInt(fractionalPart.substring(i * 4, (i + 1) * 4));
    }

    // Remove leading zeros from integer part
    int intStartIdx = 0;
    while (intStartIdx < intDigits.length && intDigits[intStartIdx] == 0) {
      intStartIdx++;
    }

    // Remove trailing zeros from fractional part
    int fracEndIdx = fracDigits.length;
    while (fracEndIdx > 0 && fracDigits[fracEndIdx - 1] == 0) {
      fracEndIdx--;
    }

    // Calculate number of significant digits
    int intDigitCount = intDigits.length - intStartIdx;
    int ndigits = intDigitCount + fracEndIdx;

    // Calculate weight: position of first significant digit (can be negative)
    int weight;
    if (intDigitCount > 0) {
      // Has integer part: weight is number of integer digit groups - 1
      weight = intDigitCount - 1;
    } else {
      // Only fractional part: weight is -(position of first digit group + 1)
      // For 0.01: first group is at position 0, so weight = -1
      // For 0.0001: first group is at position 0, so weight = -1
      weight = -1;
    }

    // Calculate total size: 4 * int16 (8 bytes) + ndigits * int16
    int totalSize = 8 + (ndigits * 2);

    writeInt32(out, totalSize);

    // Write ndigits
    writeInt16(out, ndigits);

    // Write weight
    writeInt16(out, weight);

    // Write sign (0x0000 = positive, 0x4000 = negative)
    writeInt16(out, isNegative ? 0x4000 : 0x0000);

    // Write dscale
    writeInt16(out, dscale);

    // Write integer digits (skip leading zeros)
    StringBuilder digitsStr = new StringBuilder("[");
    for (int i = intStartIdx; i < intDigits.length; i++) {
      if (digitsStr.length() > 1) digitsStr.append(", ");
      digitsStr.append(intDigits[i]);
      writeInt16(out, intDigits[i]);
    }

    // Write fractional digits (skip trailing zeros, but keep ALL digits up to fracEndIdx)
    for (int i = 0; i < fracEndIdx; i++) {
      if (digitsStr.length() > 1) digitsStr.append(", ");
      digitsStr.append(fracDigits[i]);
      writeInt16(out, fracDigits[i]);
    }
    digitsStr.append("]");
  }

  private void writeTimestamp(ByteArrayOutputStream out, Instant instant) throws IOException {
    if (instant == null) {
      writeInt32(out, -1); // NULL indicator
    } else {
      writeInt32(out, 8); // Length of TIMESTAMP (8 bytes)
      // PostgreSQL epoch: 2000-01-01 00:00:00 UTC
      long pgEpoch = Instant.parse("2000-01-01T00:00:00Z").getEpochSecond();
      long microseconds = (instant.getEpochSecond() - pgEpoch) * 1_000_000L
                        + instant.getNano() / 1000L;
      writeInt64(out, microseconds);
    }
  }

    private void writeInt16(OutputStream out, int value) throws IOException {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeInt32(OutputStream out, int value) throws IOException {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeInt64(OutputStream out, long value) throws IOException {
        out.write((int) ((value >> 56) & 0xFF));
        out.write((int) ((value >> 48) & 0xFF));
        out.write((int) ((value >> 40) & 0xFF));
        out.write((int) ((value >> 32) & 0xFF));
        out.write((int) ((value >> 24) & 0xFF));
        out.write((int) ((value >> 16) & 0xFF));
        out.write((int) ((value >> 8) & 0xFF));
        out.write((int) (value & 0xFF));
    }

    private void writeBigInt(OutputStream out, Long value) throws IOException {
        if (value == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            writeInt32(out, 8); // Length of BIGINT (8 bytes)
            writeInt64(out, value);
        }
    }

    private void writeText(OutputStream out, String value) throws IOException {
        if (value == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeInt32(out, bytes.length); // Length of data
            out.write(bytes);
        }
    }

    private void writeDouble(OutputStream out, BigDecimal value) throws IOException {
        if (value == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            writeInt32(out, 8); // Length of DOUBLE PRECISION (8 bytes)
            double doubleValue = value.doubleValue();
            long bits = Double.doubleToLongBits(doubleValue);
            writeInt64(out, bits);
        }
    }

    private void writeNumericFromLong(OutputStream out, Long value) throws IOException {
        if (value == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            // Convert Long to BigDecimal and use writePostgreSQLNumeric
            BigDecimal decimal = new BigDecimal(value);
            writePostgreSQLNumeric(out, decimal);
        }
    }

    private void writeNumeric(OutputStream out, BigDecimal value) throws IOException {
        if (value == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            // PostgreSQL NUMERIC binary format is complex (ndigits, weight, sign, dscale, digits)
            // For simplicity and compatibility, we implement the full NUMERIC format
            writePostgreSQLNumeric(out, value);
        }
    }

    private void writePostgreSQLNumeric(OutputStream out, BigDecimal value) throws IOException {
        // PostgreSQL NUMERIC binary format:
        // - ndigits (int16): number of base-10000 digits
        // - weight (int16): weight of first digit (can be negative for pure fractional values)
        // - sign (int16): NUMERIC_POS=0x0000, NUMERIC_NEG=0x4000, NUMERIC_NAN=0xC000
        // - dscale (int16): display scale
        // - digits (int16[]): base-10000 digits

        // Handle special case of zero
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            writeInt32(out, 8); // Size: 4 int16s, no digits
            writeInt16(out, 0); // ndigits
            writeInt16(out, 0); // weight
            writeInt16(out, 0x0000); // sign (positive)
            writeInt16(out, 0); // dscale
            return;
        }

        String strValue = value.toPlainString();
        boolean isNegative = value.signum() < 0;
        if (isNegative) {
            strValue = strValue.substring(1); // Remove minus sign
        }

        String[] parts = strValue.split("\\.");
        String integerPart = parts[0];
        String fractionalPart = parts.length > 1 ? parts[1] : "";
        int dscale = fractionalPart.length();

        // Pad integer part to groups of 4 from the left
        while (integerPart.length() % 4 != 0) {
            integerPart = "0" + integerPart;
        }

        // Pad fractional part to groups of 4 from the right
        while (fractionalPart.length() % 4 != 0) {
            fractionalPart = fractionalPart + "0";
        }

        // Convert to base-10000 digits
        int[] intDigits = new int[integerPart.length() / 4];
        for (int i = 0; i < intDigits.length; i++) {
            intDigits[i] = Integer.parseInt(integerPart.substring(i * 4, (i + 1) * 4));
        }

        int[] fracDigits = new int[fractionalPart.length() / 4];
        for (int i = 0; i < fracDigits.length; i++) {
            fracDigits[i] = Integer.parseInt(fractionalPart.substring(i * 4, (i + 1) * 4));
        }

        // Remove leading zeros from integer part
        int intStartIdx = 0;
        while (intStartIdx < intDigits.length && intDigits[intStartIdx] == 0) {
            intStartIdx++;
        }

        // Remove trailing zeros from fractional part
        int fracEndIdx = fracDigits.length;
        while (fracEndIdx > 0 && fracDigits[fracEndIdx - 1] == 0) {
            fracEndIdx--;
        }

        // Calculate number of significant digits
        int intDigitCount = intDigits.length - intStartIdx;
        int ndigits = intDigitCount + fracEndIdx;

        // Calculate weight: position of first significant digit (can be negative)
        int weight;
        if (intDigitCount > 0) {
            // Has integer part: weight is number of integer digit groups - 1
            weight = intDigitCount - 1;
        } else {
            // Only fractional part: weight is -(position of first digit group + 1)
            // For 0.01: first group is at position 0, so weight = -1
            // For 0.0001: first group is at position 0, so weight = -1
            weight = -1;
        }

        // Calculate total size: 4 * int16 (8 bytes) + ndigits * int16
        int totalSize = 8 + (ndigits * 2);

        writeInt32(out, totalSize);

        // Write ndigits
        writeInt16(out, ndigits);

        // Write weight
        writeInt16(out, weight);

        // Write sign (0x0000 = positive, 0x4000 = negative)
        writeInt16(out, isNegative ? 0x4000 : 0x0000);

        // Write dscale
        writeInt16(out, dscale);

        // Write integer digits (skip leading zeros)
        StringBuilder digitsStr = new StringBuilder("[");
        for (int i = intStartIdx; i < intDigits.length; i++) {
            if (digitsStr.length() > 1) digitsStr.append(", ");
            digitsStr.append(intDigits[i]);
            writeInt16(out, intDigits[i]);
        }

        // Write fractional digits (skip trailing zeros, but keep ALL digits up to fracEndIdx)
        for (int i = 0; i < fracEndIdx; i++) {
            if (digitsStr.length() > 1) digitsStr.append(", ");
            digitsStr.append(fracDigits[i]);
            writeInt16(out, fracDigits[i]);
        }
        digitsStr.append("]");
    }

    private void writeTimestamp(OutputStream out, Instant instant) throws IOException {
        if (instant == null) {
            writeInt32(out, -1); // NULL indicator
        } else {
            writeInt32(out, 8); // Length of TIMESTAMP (8 bytes)
            // PostgreSQL epoch: 2000-01-01 00:00:00 UTC
            long pgEpoch = Instant.parse("2000-01-01T00:00:00Z").getEpochSecond();
            long microseconds = (instant.getEpochSecond() - pgEpoch) * 1_000_000L
                    + instant.getNano() / 1000L;
            writeInt64(out, microseconds);
        }
    }

  private byte[] buildCsvPayload(List<PaymentEntity> entityBatch) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (PaymentEntity payment : entityBatch) {
      String[] fields = {
        toCsvField(payment.getFlowId()),
        toCsvField(payment.getIuv()),
        toCsvField(payment.getIur()),
        toCsvField(payment.getIndex()),
        toCsvField(payment.getAmount()),
        toCsvField(formatInstant(payment.getPayDate())),
        toCsvField(payment.getPayStatus()),
        toCsvField(payment.getTransferId()),
        toCsvField(formatInstant(payment.getCreated())),
        toCsvField(formatInstant(payment.getUpdated()))
      };
      String row = String.join(",", fields) + "\n";
      out.writeBytes(row.getBytes(StandardCharsets.UTF_8));
    }
    return out.toByteArray();
  }

  private String buildCsvRow(PaymentEntity entity) {
        return String.join(",",
                toCsvField(entity.getFlowId()),
                escapeCsv(entity.getIuv()),
                escapeCsv(entity.getIur()),
                toCsvField(entity.getIndex()),
                toCsvField(entity.getAmount()),
                formatInstant(entity.getPayDate()),
                escapeCsv(entity.getPayStatus()),
                toCsvField(entity.getTransferId()),
                formatInstant(entity.getCreated()),
                formatInstant(entity.getUpdated())
        ) + "\n";
    }

  private String toCsvField(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return escapeCsv((String) value);
    }
    return value.toString();
  }

  private String formatInstant(Instant instant) {
    if (instant == null) {
      return "";
    }
    return COPY_TIMESTAMP_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
  }

  private String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return value == null ? "" : value;
    }
    boolean needsQuoting =
        value.contains(",")
            || value.contains("\"")
            || value.contains("\n")
            || value.contains("\r")
            || value.contains("\\");
    String escaped = value.replace("\\", "\\\\").replace("\"", "\"\"");
    if (needsQuoting) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  public PanacheQuery<PaymentEntity> findPageByFlowId(Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    return find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
  }

  public RepositoryPagedResult<PaymentEntity> findByFlowId(
      Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    PanacheQuery<PaymentEntity> resultPage = find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
    return getPagedResult(resultPage);
  }

  public List<PaymentEntity> findByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return find(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes).list();
  }

  public void deleteEntityInBulk(List<PaymentEntity> entityBatch) {

    Set<Long> ids = entityBatch.stream().map(PaymentEntity::getId).collect(Collectors.toSet());
    delete(QUERY_DELETE_BY_ID, ids);
  }
}
