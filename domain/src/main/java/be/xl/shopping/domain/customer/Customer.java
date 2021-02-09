package be.xl.shopping.domain.customer;

import static io.vavr.collection.List.ofAll;

import be.xl.eventsourcing.model.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
public class Customer implements AggregateRoot<Customer, CustomerId> {

   private final CustomerId customerId;
   private Instant affiliationDate;
   private String firstName;
   private String lastName;
   private final List<DomainEvent<Customer>> pendingEvents = new ArrayList<>();

   private Customer(CustomerId customerId) {
      this.customerId = customerId;
      //TODO: we should never remain in this state!
   }

   private Customer(CustomerId customerId, String firstName, String lastName,
       Instant affiliationDate) {
      this.customerId = customerId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.affiliationDate = affiliationDate;
   }

   public static Customer registerCustomer(CustomerId customerId, String firstName, String lastName,
       Instant affiliationDate) {
      Customer customer = new Customer(customerId, firstName, lastName, affiliationDate);
      customer.pendingEvents
          .add(new CustomerRegistered(customerId.getId(), firstName, lastName, affiliationDate));
      return customer;
   }

   public static Customer recreateFrom(CustomerId customerId, List<DomainEvent<Customer>> events) {
      return ofAll(events).foldLeft(new Customer(customerId), Customer::handle);

   }

   private Customer handle(DomainEvent<Customer> domainEvent) {
      if (domainEvent instanceof CustomerRegistered) {
         return customerRegistered((CustomerRegistered) domainEvent);
      } else {
         throw new IllegalArgumentException(
             String.format("Not supported event %s", domainEvent.toString()));
      }
   }

   private Customer customerRegistered(CustomerRegistered event) {
      return registerCustomer(CustomerId.of(event.getCustomerId()), event.getFirstName(),
          event.getLastName(), event.getAffiliationDate());
   }

   @Override
   public CustomerId getId() {
      return customerId;
   }

   public List<DomainEvent<Customer>> getPendingEvents() {
      return pendingEvents;
   }

}
