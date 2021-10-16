package be.xl.shopping.domain.core.customer.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.customer.entity.Customer;
import java.time.Instant;
import java.util.UUID;

public record CustomerRegistered(UUID customerId,
                                 String firstName,
                                 String lastName,
                                 String emailAddress,
                                 Instant affiliationDate, Version version) implements DomainEvent<Customer> {

   @Override
   public String getType() {
      return "customer-registered";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
