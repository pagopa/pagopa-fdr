package it.gov.pagopa.fdr.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

}
