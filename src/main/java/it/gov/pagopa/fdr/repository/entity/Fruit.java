package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import java.util.Optional;

@Entity
public class Fruit extends PanacheEntity {
    public String name;
    public String description;

    public static Optional<Fruit> findByNameOptional(String name){
        return find("name", name).firstResultOptional();
    }
    public static long deleteByName(String name){
        return delete("name", name);
    }
}