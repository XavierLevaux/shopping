package be.xl.shopping.domain.core.customer.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.customer.entity.Customer;
import java.util.UUID;

public record AddressChanged(
    UUID customerId,
    String street,
    String streetNumber,
    String postCode,
    String city,
    String countryName,
    Version version) implements DomainEvent<Customer> {

   @Override
   public String getType() {
      return "customer-address-changed";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}

