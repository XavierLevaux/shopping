package be.xl.shopping.domain.core.customer;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.customer.entity.Address;
import be.xl.shopping.domain.core.customer.entity.Customer;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import be.xl.shopping.domain.core.customer.entity.EmailAddress;
import be.xl.shopping.domain.core.customer.event.AddressChanged;
import be.xl.shopping.domain.core.customer.event.CustomerRegistered;
import be.xl.shopping.domain.port.infrastructure.InMemoryEventStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CustomerTest {

   private final CustomerId customerId = new CustomerId(UUID.randomUUID());
   private final Address newYorkAddress = new Address(
       "Lexington Ave",
       "405",
       "New York",
       "NY 10174",
       "United States");
   private final Address naplesAddress = new Address(
       "12th Ave S",
       "60",
       "Naples",
       "FL 34102",
       "United States");
   private final EmailAddress johnAtSmithDotCom = new EmailAddress("john@smith.com");

   @Nested
   class when_registering_new_customer {

      final Instant registrationDate = Instant.now();
      private final EventStore<Customer, CustomerId> eventStore = new InMemoryEventStore<>();

      public when_registering_new_customer() {
         eventStore.saveNewAggregate(
             customerId,
             Customer.registerCustomer(customerId, "John", "Smith", johnAtSmithDotCom,
                 registrationDate)
         );
      }

      @Test
      void then_a_customer_registered_event_is_produced() {
         assertThat(eventStore.loadEvents(customerId)).contains(
             new DomainEvents<>(customerId, Version.initialVersion(), List.of(
                 new CustomerRegistered(customerId.id(), "John", "Smith", johnAtSmithDotCom.value(),
                     registrationDate,
                     new Version(1)))
             ));
      }

      @Nested
      class and_replaying_events {

         private final Customer customer;

         public and_replaying_events() {
            customer = Customer.reHydrate(eventStore.loadEvents(customerId).orElseThrow());
         }

         @Test
         void then_a_customer_domain_entity_is_correctly_built() {
            assertThat(customer.getId()).isEqualTo(customerId);
            assertThat(customer.getFirstName()).isEqualTo("John");
            assertThat(customer.getLastName()).isEqualTo("Smith");
            assertThat(customer.getAffiliationDate()).isEqualTo(registrationDate);
         }

         @Test
         void then_customer_email_address_is_not_verified() {
            assertThat(customer.isEmailAddressVerified()).isFalse();
         }
      }

      @Nested
      class and_I_confirm_email_address {

         private Customer customer;

         public and_I_confirm_email_address() {
            eventStore.updateExistingAggregate(customerId,
                Customer
                    .reHydrate(eventStore.loadEvents(customerId).orElseThrow())
                    .confirmEmailAddress(johnAtSmithDotCom)
            );
            customer = Customer
                .reHydrate(eventStore.loadEvents(customerId).orElseThrow());

         }

         @Test
         void then_a_customer_domain_entity_is_correctly_built() {
            assertThat(customer.getId()).isEqualTo(customerId);
            assertThat(customer.getFirstName()).isEqualTo("John");
            assertThat(customer.getLastName()).isEqualTo("Smith");
            assertThat(customer.getEmailAddress()).isEqualTo(johnAtSmithDotCom);
            assertThat(customer.isEmailAddressVerified()).isTrue();
            assertThat(customer.getAffiliationDate()).isEqualTo(registrationDate);
         }
      }
   }

   @Nested
   class when_I_have_a_new_customer_without_address {

      private final Customer customer;

      public when_I_have_a_new_customer_without_address() {
         final Instant registrationDate = Instant.now();

         customer = Customer.reHydrate(
             Customer.registerCustomer(customerId, "John", "Smith", johnAtSmithDotCom,
                 registrationDate)
         );
      }

      @Test
      void then_it_has_no_address() {
         assertThat(customer.getAddress()).isEmpty();
      }

      @Test
      void then_I_can_add_an_address_to_the_customer() {
         DomainEvents<Customer, CustomerId> domainEvents = customer.defineAddress(
             newYorkAddress.street(), newYorkAddress.streetNumber(), newYorkAddress.city(),
             newYorkAddress.postCode(), newYorkAddress.countryName());

         Customer customer = Customer.reHydrate(domainEvents);
         assertThat(customer.getAddress()).isEqualTo(Optional.of(newYorkAddress));
      }
   }

   @Nested
   class when_I_have_a_customer_with_address {

      private DomainEvents<Customer, CustomerId> domainEvents;

      public when_I_have_a_customer_with_address() {
         final Instant registrationDate = Instant.now();

         domainEvents = Customer
             .reHydrate(Customer.registerCustomer(customerId,
                 "John", "Smith", johnAtSmithDotCom, registrationDate)
             )
             .defineAddress(
                 newYorkAddress.street(),
                 newYorkAddress.streetNumber(),
                 newYorkAddress.city(),
                 newYorkAddress.postCode(),
                 newYorkAddress.countryName()
             );
      }

      @Test
      void then_I_can_change_its_address() {
         DomainEvents<Customer, CustomerId> newDomainEvents = Customer
             .reHydrate(domainEvents)
             .defineAddress(naplesAddress.street(), naplesAddress.streetNumber(),
                 naplesAddress.city(), naplesAddress.postCode(), naplesAddress.countryName());

         Customer customer = Customer.reHydrate(newDomainEvents);

         assertThat(newDomainEvents.events()).containsExactly(
             new AddressChanged(
                 customerId.id(),
                 naplesAddress.street(),
                 naplesAddress.streetNumber(),
                 naplesAddress.postCode(),
                 naplesAddress.city(),
                 naplesAddress.countryName(),
                 Version.version(3L)
             )
         );
         assertThat(customer.getAddress()).isEqualTo(Optional.of(naplesAddress));
      }
   }

}
