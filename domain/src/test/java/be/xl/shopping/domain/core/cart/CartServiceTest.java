package be.xl.shopping.domain.core.cart;

import static be.xl.shopping.domain.core.cart.event.CartCreated.CartCreatedBuilder.cartCreated;
import static be.xl.shopping.domain.core.cart.event.ProductAddedToCart.ProductAddedToCartBuilder.productAddedToCart;
import static org.assertj.core.api.Assertions.assertThat;

import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.port.infrastructure.InMemoryEventStore;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart;
import be.xl.shopping.domain.port.command.AddProductToCartCommand;
import be.xl.shopping.domain.port.command.CreateCartCommand;
import be.xl.shopping.domain.port.command.RemoveProductFromCartCommand;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CartServiceTest {

   private final EventStore<Cart, CartId> cartEventStore = new InMemoryEventStore<>();
   private final CartService cartService = new CartService(cartEventStore);

   @Nested
   class when_I_create_a_cart {
      private final UUID cartId = UUID.randomUUID();
      private final UUID customerId = UUID.randomUUID();

      public when_I_create_a_cart() {

         CreateCartCommand command = new CreateCartCommand(cartId, customerId);
         cartService.handleCreateCart(command);
      }

      @Test
      void then_events_are_stored_in_event_store() {
         assertThat(cartEventStore.loadEvents(CartId.of(cartId))).isNotEmpty();
         assertThat(cartEventStore.loadEvents(CartId.of(cartId)).orElseThrow()).containsExactly(
             new CartCreated(cartId, customerId, 1L)
         );
      }

      @Nested
         class and_I_add_one_product {
         private final UUID productId = UUID.randomUUID();

         public and_I_add_one_product() {
            cartService.handleAddProductToCart(new AddProductToCartCommand(cartId, productId, 5));
         }

         @Test
         void then_events_are_stored_in_event_store() {
            assertThat(cartEventStore.loadEvents(CartId.of(cartId))).isNotEmpty();
            assertThat(cartEventStore.loadEvents(CartId.of(cartId)).orElseThrow()).containsExactly(
                new CartCreated(cartId, customerId, 1L),
                new ProductAddedToCart(cartId, customerId, productId, 5, 2L)
            );
         }
      }

   }

   @Nested
   class when_I_have_a_cart_with_a_product {
      private final UUID cartId = UUID.randomUUID();
      private final UUID customerId = UUID.randomUUID();
      private final UUID productId = UUID.randomUUID();

      public when_I_have_a_cart_with_a_product() {
         cartEventStore.saveNewAggregate(CartId.of(cartId), DomainEvents.<Cart, CartId>domainEvents(CartId.of(cartId),0L)
             .withEvent(cartCreated(cartId, customerId))
             .withEvent(productAddedToCart(cartId, customerId, productId, 5)));
      }

      @Nested
      class and_I_remove_some_quantity_of_that_product {

         public and_I_remove_some_quantity_of_that_product() {
            cartService.handleRemoveProductFromCart(new RemoveProductFromCartCommand(cartId, productId, 3));
         }

         @Test
         void then_cart_product_quantity_is_adapted() {
            assertThat(cartEventStore.loadEvents(CartId.of(cartId))).isNotEmpty();
            assertThat(cartEventStore.loadEvents(CartId.of(cartId)).orElseThrow()).containsExactly(
                new CartCreated(cartId, customerId, 1L),
                new ProductAddedToCart(cartId, customerId, productId, 5, 2L),
                new ProductQuantityRemovedFromCart(cartId, customerId, productId, 3, 3L)
            );
         }
      }
   }
}