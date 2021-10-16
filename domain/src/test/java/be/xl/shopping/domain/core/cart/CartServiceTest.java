package be.xl.shopping.domain.core.cart;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.cart.entity.ProductId;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import be.xl.shopping.domain.port.command.AddProductToCartCommand;
import be.xl.shopping.domain.port.command.CreateCartCommand;
import be.xl.shopping.domain.port.command.RemoveProductFromCartCommand;
import be.xl.shopping.domain.port.infrastructure.InMemoryEventStore;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CartServiceTest {

   private final EventStore<Cart, CartId> cartEventStore = new InMemoryEventStore<>();
   private final CartService cartService = new CartService(cartEventStore);

   @Nested
   class when_I_create_a_cart {

      private final CartId cartId = new CartId(UUID.randomUUID());
      private final CustomerId customerId = new CustomerId(UUID.randomUUID());

      public when_I_create_a_cart() {

         CreateCartCommand command = new CreateCartCommand(cartId.id(), customerId.id());
         cartService.handleCreateCart(command);
      }

      @Test
      void then_events_are_stored_in_event_store() {
         assertThat(cartEventStore.loadEvents(cartId)).isNotEmpty();
         assertThat(cartEventStore.loadEvents(cartId).orElseThrow()).containsExactly(
             new CartCreated(cartId.id(), customerId.id(), new Version(1))
         );
      }

      @Nested
      class and_I_add_one_product {

         private final UUID productId = UUID.randomUUID();

         public and_I_add_one_product() {
            cartService.handleAddProductToCart(new AddProductToCartCommand(cartId.id(), productId, 5));
         }

         @Test
         void then_events_are_stored_in_event_store() {
            assertThat(cartEventStore.loadEvents(cartId)).isNotEmpty();
            assertThat(cartEventStore.loadEvents(cartId).orElseThrow()).containsExactly(
                new CartCreated(cartId.id(), customerId.id(), new Version(1)),
                new ProductAddedToCart(cartId.id(), customerId.id(), productId, 5, new Version(2))
            );
         }
      }

   }

   @Nested
   class when_I_have_a_cart_with_a_product {

      private final CartId cartId = new CartId(UUID.randomUUID());
      private final CustomerId customerId = new CustomerId(UUID.randomUUID());
      private final ProductId productId = new ProductId(UUID.randomUUID());

      public when_I_have_a_cart_with_a_product() {
         cartService.handleCreateCart(new CreateCartCommand(cartId.id(), customerId.id()));
         cartService.handleAddProductToCart(new AddProductToCartCommand(cartId.id(), productId.id(), 5));
      }

      @Nested
      class and_I_remove_some_quantity_of_that_product {

         public and_I_remove_some_quantity_of_that_product() {
            cartService.handleRemoveProductFromCart(
                new RemoveProductFromCartCommand(cartId.id(), productId.id(), 3));
         }

         @Test
         void then_cart_product_quantity_is_adapted() {
            assertThat(cartEventStore.loadEvents(cartId)).isNotEmpty();
            assertThat(cartEventStore.loadEvents(cartId).orElseThrow()).containsExactly(
                new CartCreated(cartId.id(), customerId.id(),
                    new Version(1)),
                new ProductAddedToCart(cartId.id(), customerId.id(), productId.id(), 5, new Version(2)),
                new ProductQuantityRemovedFromCart(cartId.id(), customerId.id(), productId.id(), 3, new Version(3))
            );
         }
      }
   }
}