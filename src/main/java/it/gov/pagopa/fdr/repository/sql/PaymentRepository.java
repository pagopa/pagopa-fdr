package it.gov.pagopa.fdr.repository.sql;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Session;

@ApplicationScoped
public class PaymentRepository implements PanacheRepository<PaymentEntity> {

  @Inject private EntityManager entityManager;

  public static final String QUERY_DELETE_BY_FLOW_ID = "flowId = ?1";

  public static final String QUERY_DELETE_BY_ID = "id in ?1";

  public static final String QUERY_GET_BY_FLOW_OBJID_AND_INDEXES =
      "flowId = ?1" + " and index in ?2";

  public long deleteByFlow(Long flowId) {
    return delete(QUERY_DELETE_BY_FLOW_ID, flowId);
  }

  public long countByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return count(QUERY_GET_BY_FLOW_OBJID_AND_INDEXES, flowId, indexes);
  }

  public void createEntityInBulk(List<PaymentEntity> entityBatch) {

    // TODO evaluate bulkInsert
    long start = Calendar.getInstance().getTimeInMillis();

    Session session = entityManager.unwrap(Session.class);
    String sql =
        "INSERT INTO payment (flow_id, iuv, iur, index, amount, pay_date, pay_status, transfer_id,"
            + " created, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps =
        session.doReturningWork(connection -> connection.prepareStatement(sql))) {
      int count = 0;

      for (PaymentEntity payment : entityBatch) {
        ps.setLong(1, payment.flowId);
        ps.setString(2, payment.iuv);
        ps.setString(3, payment.iur);
        ps.setLong(4, payment.index);
        ps.setBigDecimal(5, payment.amount);
        ps.setTimestamp(6, payment.payDate != null ? Timestamp.from(payment.payDate) : null);
        ps.setString(7, payment.payStatus);
        ps.setLong(8, payment.transferId);
        ps.setTimestamp(9, payment.created != null ? Timestamp.from(payment.created) : null);
        ps.setTimestamp(10, payment.updated != null ? Timestamp.from(payment.updated) : null);
        ps.addBatch();

        if (++count % 5000 == 0) {
          ps.executeBatch(); // ✅ Esegui batch ogni 1000 elementi
        }
      }
      ps.executeBatch(); // ✅ Esegui gli ultimi record

    } catch (SQLException e) {
      e.printStackTrace();
    }

    // persist(entityBatch);
    System.out.printf(
        "Time of persistence for payments: "
            + (Calendar.getInstance().getTimeInMillis() - start)
            + " ms\n");
  }

  public List<PaymentEntity> findByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return find(QUERY_GET_BY_FLOW_OBJID_AND_INDEXES, flowId, indexes).list();
  }

  public void deleteEntityInBulk(List<PaymentEntity> entityBatch) {
    // TODO evaluate bulkDelete
    Set<Long> ids = entityBatch.stream().map(PaymentEntity::getId).collect(Collectors.toSet());
    delete(QUERY_DELETE_BY_ID, ids);
  }
}
