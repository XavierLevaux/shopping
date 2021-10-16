package be.xl.shopping.domain.core.customer;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.shopping.domain.core.customer.entity.CustomerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerIdTest {
   @Test
   void when_customer_id_is_created_i_get_its_id() {
      UUID uuid = UUID.randomUUID();
      CustomerId customerId = new CustomerId(uuid);

      assertThat(customerId.id()).isEqualTo(uuid);
   }
}