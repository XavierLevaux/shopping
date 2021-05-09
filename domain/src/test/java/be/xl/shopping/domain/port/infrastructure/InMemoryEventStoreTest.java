package be.xl.shopping.domain.port.infrastructure;

import static be.xl.shopping.domain.core.cart.event.CartCreated.CartCreatedBuilder.cartCreated;
import static be.xl.shopping.domain.core.cart.event.ProductAddedToCart.ProductAddedToCartBuilder.productAddedToCart;
import static org.assertj.core.api.Assertions.assertThat;

import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryEventStoreTest {

   @Nested
   class Given_I_have_domain_events {
      protected final InMemoryEventStore<Cart, CartId> eventStore = new InMemoryEventStore<>();
      protected final UUID cartId = UUID.randomUUID();
      protected final UUID customerId = UUID.randomUUID();
      protected final UUID productId = UUID.randomUUID();

      protected DomainEvents<Cart, CartId> events;

      public Given_I_have_domain_events() {
         events = DomainEvents.<Cart, CartId>domainEvents(CartId.of(cartId), 0L)
             .withEvent(cartCreated(cartId, customerId))
             .withEvent(productAddedToCart(cartId, customerId, productId, 5));
      }

      @Nested
      class When_I_save_a_new_aggregate {

         public When_I_save_a_new_aggregate() {
            eventStore.saveNewAggregate(CartId.of(cartId), events);
         }

         @Test
         void then_I_can_load_those_events_from_the_store() {
            Optional<DomainEvents<Cart, CartId>> domainEvents = eventStore.loadEvents(CartId.of(cartId));

            assertThat(domainEvents).isNotEmpty();
            assertThat(domainEvents.get()).containsExactly(
                new CartCreated(cartId, customerId, 1L),
                new ProductAddedToCart(cartId, customerId, productId, 5, 2L)
            );
         }
      }
   }
}