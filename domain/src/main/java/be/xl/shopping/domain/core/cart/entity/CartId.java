package be.xl.shopping.domain.core.cart.entity;

import be.xl.architecture.eventsourcing.model.AggregateIdentifier;
import java.util.UUID;

public record CartId(UUID id) implements AggregateIdentifier {

   public static final String AGGREGATE_ROOT_NAME = "Shopping.Cart";

   @Override
   public String getAggregateName() {
      return AGGREGATE_ROOT_NAME;
   }
}
