package be.xl.shopping.domain.customer;

import be.xl.eventsourcing.model.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class CustomerRegistered implements DomainEvent<Customer> {

   private final UUID customerId;
   private final Instant affiliationDate;
   private final String firstName;
   private final String lastName;
   private final Long version;

   public CustomerRegistered(UUID customerId, String firstName, String lastName,
       Instant affiliationDate, Long version) {
      this.customerId = customerId;
      this.affiliationDate = affiliationDate;
      this.firstName = firstName;
      this.lastName = lastName;
      this.version = version;
   }

   public CustomerRegistered(UUID customerId, String firstName, String lastName,
       Instant affiliationDate) {
      this(customerId, firstName, lastName, affiliationDate, null);
   }

   @Override
   public String getType() {
      return "customer-registered";
   }

   @Override
   public Long getVersion() {
      return version;
   }
}
