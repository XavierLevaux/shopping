package be.xl.shopping.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerIdTest {
   @Test
   void when_customer_id_is_created_i_get_its_id() {
      UUID uuid = UUID.randomUUID();
      CustomerId customerId = CustomerId.of(uuid);

      assertThat(customerId.getId()).isEqualTo(uuid);
   }
}