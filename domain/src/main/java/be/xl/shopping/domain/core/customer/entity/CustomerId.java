package be.xl.shopping.domain.core.customer.entity;

import be.xl.architecture.eventsourcing.model.AggregateIdentifier;
import java.util.UUID;

public record CustomerId(UUID id) implements AggregateIdentifier {
   public static final String AGGREGATE_ROOT_NAME = "Shopping.Customer";

   @Override
   public String getAggregateName() {
      return AGGREGATE_ROOT_NAME;
   }
}
