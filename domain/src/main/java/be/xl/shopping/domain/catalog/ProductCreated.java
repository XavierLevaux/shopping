package be.xl.shopping.domain.catalog;

import be.xl.eventsourcing.model.DomainEvent;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ProductCreated implements DomainEvent<Product> {

   private final UUID id;
   private final String description;
   private final Long version;

   public ProductCreated(UUID id, String description, Long version) {
      this.id = id;
      this.description = description;
      this.version = version;
   }

   public ProductCreated(UUID id, String description) {
      this(id, description, null);
   }

   @Override
   public String getType() {
      return "product-created";
   }

   @Override
   public Long getVersion() {
      return version;
   }
}
