package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment_publish")
public class FdrPaymentPublishEntityReactive extends ReactivePanacheMongoEntity {

    // Reactive persistence method for FdrPaymentPublishEntity objects
    public static Uni<Void> persistFdrPaymentPublishEntities(List<FdrPaymentPublishEntity> entities) {
        return persist(entities);
    }
}
