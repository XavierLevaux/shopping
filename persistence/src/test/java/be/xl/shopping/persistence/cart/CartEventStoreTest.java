package be.xl.shopping.persistence.cart;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.cart.Cart;
import be.xl.shopping.domain.cart.CartId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CartEventStoreTest {

   @Disabled
   @Test
   void loadEventStream() {
      CartEventStore eventStore = new CartEventStore();
      UUID cartId = UUID.randomUUID();
      Optional<DomainEvents<Cart, CartId>> domainEvents = eventStore.loadEvents(CartId.of(cartId));

      assertThat(domainEvents).isPresent();
   }
}