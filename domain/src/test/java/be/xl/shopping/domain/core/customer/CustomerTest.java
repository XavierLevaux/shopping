package be.xl.shopping.domain.core.customer;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.shopping.domain.core.customer.entity.Customer;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import be.xl.shopping.domain.core.customer.event.CustomerRegistered;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CustomerTest {

   @Nested
   class when_registering_new_customer {
      final UUID uuid = UUID.randomUUID();
      final Instant registrationDate = Instant.now();
      final Customer customer;

      public when_registering_new_customer() {
          customer = Customer
             .registerCustomer(CustomerId.of(uuid), "John", "Smith", registrationDate);
      }

      @Test
      void then_a_customer_registered_event_is_produced() {

         assertThat(customer.getPendingEvents()).hasSize(1);
         assertThat(customer.getPendingEvents()).containsExactly(
             new CustomerRegistered(uuid, "John", "Smith", registrationDate)
         );


      }
   }

   @Test
   void when_replaying_customer_registered_event_i_rehydrate_customer() {
      UUID uuid = UUID.randomUUID();
      Instant registrationDate = Instant.now();
      CustomerRegistered customerRegistered = new CustomerRegistered(uuid, "John", "Smith",
          registrationDate);

      Customer customer = Customer.recreateFrom(CustomerId.of(uuid), Collections.singletonList(customerRegistered));

      assertThat(customer.getCustomerId()).isEqualTo(CustomerId.of(uuid));
      assertThat(customer.getFirstName()).isEqualTo("John");
      assertThat(customer.getLastName()).isEqualTo("Smith");
      assertThat(customer.getAffiliationDate()).isEqualTo(registrationDate);
   }

}
