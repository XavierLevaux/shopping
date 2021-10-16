package be.xl.shopping.domain.core.customer.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.customer.entity.Customer;
import java.util.UUID;

public record CustomerEmailAddressConfirmed(UUID id,
                                            String emailAddress,
                                            Version version) implements DomainEvent<Customer> {

   @Override
   public String getType() {
      return "customer-email-address-confirmed";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
