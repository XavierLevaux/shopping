package be.xl.shopping.domain;

import static be.xl.shopping.domain.cart.CartCreated.CartCreatedBuilder.cartCreated;
import static be.xl.shopping.domain.cart.ProductAddedToCart.ProductAddedToCartBuilder.productAddedToCart;
import static org.assertj.core.api.Assertions.assertThat;

import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.cart.Cart;
import be.xl.shopping.domain.cart.CartCreated;
import be.xl.shopping.domain.cart.CartId;
import be.xl.shopping.domain.cart.ProductAddedToCart;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryEventStoreTest {

   @Test
   void store() {
      InMemoryEventStore<Cart, CartId> eventStore = new InMemoryEventStore<>();
      UUID cartId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      UUID productId = UUID.randomUUID();
      DomainEvents<Cart, CartId> events = DomainEvents.<Cart, CartId>domainEvents(0L)
          .withEvent(cartCreated(cartId, customerId))
          .withEvent(productAddedToCart(cartId, customerId, productId, 5));

      eventStore.saveNewAggregate(CartId.of(cartId), events);

      Optional<DomainEvents<Cart, CartId>> domainEvents = eventStore.loadEvents(CartId.of(cartId));
      assertThat(domainEvents).isNotEmpty();
      assertThat(domainEvents.get()).containsExactly(
          new CartCreated(cartId, customerId, 1L),
          new ProductAddedToCart(cartId, customerId, productId, 5, 2L)
      );
   }

}