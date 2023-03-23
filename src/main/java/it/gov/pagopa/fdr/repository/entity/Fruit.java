package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.Optional;
import javax.persistence.Entity;

@Entity
public class Fruit extends PanacheEntity {
  public String name;
  public String description;

  public static Optional<Fruit> findByNameOptional(String name) {
    return find("name", name).firstResultOptional();
  }

  public static long deleteByName(String name) {
    return delete("name", name);
  }
}
