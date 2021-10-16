package be.xl.shopping.domain.core.cart;

import static be.xl.architecture.eventsourcing.model.Version.version;
import static org.assertj.core.api.Assertions.assertThat;

import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.cart.entity.CartItem;
import be.xl.shopping.domain.core.cart.entity.ProductId;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import be.xl.shopping.domain.port.infrastructure.InMemoryEventStore;
import be.xl.shopping.domain.port.infrastructure.InMemoryEventStream;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CartTest {

   private final CustomerId customerId = new CustomerId(UUID.randomUUID());
   private final CartId cartId = new CartId(UUID.randomUUID());

   @Nested
   class when_a_cart_is_created {

      private final DomainEvents<Cart, CartId> domainEvents;

      when_a_cart_is_created() {
         domainEvents = Cart.createCart(cartId, customerId);
      }

      @Test
      void then_a_CartCreatedEvent_is_emitted() {
         assertThat(domainEvents.events()).hasSize(1);
         assertThat(domainEvents.events()).containsExactly(
             new CartCreated(cartId.id(), customerId.id(), new Version(1))
         );
      }
   }

   @Nested
   class when_I_apply_CartCreatedEvent_I_get_a_cart {

      private final Cart cart;
      private final EventStore<Cart, CartId> eventStore = new InMemoryEventStore<>();


      public when_I_apply_CartCreatedEvent_I_get_a_cart() {
         eventStore.saveNewAggregate(cartId, Cart.createCart(cartId, customerId));
         cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
      }

      @Test
      void then_cart_is_created() {
         assertThat(cart.getCartId()).isEqualTo(cartId);
         assertThat(cart.getCustomerId()).isEqualTo(customerId);
         assertThat(cart.getContent()).isEmpty();
      }
   }

   @Nested
   class when_I_have_an_existing_empty_cart {

      private final Cart cart;
      private final EventStore<Cart, CartId> cartEventStore = new InMemoryEventStore<>();

      public when_I_have_an_existing_empty_cart() {
         DomainEvents<Cart, CartId> domainEvents = Cart.createCart(cartId, customerId);
         cartEventStore.saveNewAggregate(cartId, domainEvents);

         cart = Cart.reHydrate(domainEvents);
      }

      @Nested
      class and_I_add_a_product_to_the_cart {

         private final int addedQuantity = 3;
         private final ProductId addedProductId = new ProductId(UUID.randomUUID());
         private final DomainEvents<Cart, CartId> domainEvents;

         public and_I_add_a_product_to_the_cart() {
            domainEvents = cart.addProduct(addedProductId, addedQuantity);
         }

         @Test
         void then_there_is_one_ProductAddedToCart_returned() {
            assertThat(domainEvents.events()).hasSize(1);
            assertThat(domainEvents.events()).containsExactly(
                new ProductAddedToCart(cartId.id(), customerId.id(), addedProductId.id(),
                    addedQuantity, new Version(2))
            );
         }
      }
   }

   @Nested
   class when_I_have_an_existing_cart_with_one_product {

      private Cart cart;
      private final EventStore<Cart, CartId> eventStore = new InMemoryEventStore<>();
      private final int existingQuantity = 3;
      private final ProductId existingProductId = new ProductId(UUID.randomUUID());

      public when_I_have_an_existing_cart_with_one_product() {
         eventStore.saveNewAggregate(cartId, Cart.createCart(cartId, customerId));
         cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
         eventStore.updateExistingAggregate(
             cartId,
             cart.addProduct(existingProductId, existingQuantity)
         );
         cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
      }


      @Nested
      class and_I_remove_the_same_product_with_existing_quantity_minus_1 {

         public and_I_remove_the_same_product_with_existing_quantity_minus_1() {
            eventStore.updateExistingAggregate(
                cartId,
                cart.removeProduct(existingProductId, existingQuantity - 1)
            );
            cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                new CartItem(existingProductId, 1)
            );
         }
      }

      @Nested
      class and_I_add_the_same_product {

         private final int addedQuantity = 2;

         public and_I_add_the_same_product() {
            eventStore.updateExistingAggregate(
                cartId,
                cart.addProduct(existingProductId, addedQuantity)
            );
            cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                new CartItem(existingProductId, existingQuantity + addedQuantity)
            );
         }
      }

      @Nested
      class and_I_remove_the_same_product_by_quantity_in_cart_minus_one {

         public and_I_remove_the_same_product_by_quantity_in_cart_minus_one() {
            eventStore.updateExistingAggregate(
                cartId,
                cart.removeProduct(existingProductId, existingQuantity - 1)
            );
            cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                new CartItem(existingProductId, 1)
            );
         }
      }

      @Nested
      class and_I_add_another_product {

         private final int addedQuantity = 2;
         private final ProductId otherProductId = new ProductId(UUID.randomUUID());

         public and_I_add_another_product() {
            eventStore.updateExistingAggregate(
                cartId,
                cart.addProduct(otherProductId, addedQuantity)
            );
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            Cart cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
            assertThat(cart.getContent()).hasSize(2);
            assertThat(cart.getContent()).containsExactly(
                new CartItem(existingProductId, existingQuantity),
                new CartItem(otherProductId, addedQuantity)
            );
         }
      }

   }

   @Nested
   class when_I_rehydrate_cart_from_two_events {

      protected final ProductId PRODUCT_ID = new ProductId(UUID.randomUUID());
      protected Cart cart;
      protected EventStore<Cart, CartId> eventStore = new InMemoryEventStore<>();

      public when_I_rehydrate_cart_from_two_events() {
         eventStore.saveNewAggregate(cartId, Cart.createCart(cartId, customerId));
         cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
         eventStore.updateExistingAggregate(cartId, cart.addProduct(PRODUCT_ID, 3));
         cart = Cart.reHydrate(eventStore.loadEvents(cartId).orElseThrow());
      }

      @Test
      void then_cart_has_right_attributes() {
         assertThat(cart.getCustomerId()).isEqualTo(customerId);
         assertThat(cart.getCartId()).isEqualTo(cartId);
         assertThat(cart.getContent()).containsExactly(new CartItem(PRODUCT_ID, 3));
      }

      @Test
      void then_cart_has_version_corresponding_to_version_of_last_event() {
         assertThat(cart.version).isEqualTo(new Version(2));
      }

   }


   @Test
   void rehydrate_cart_from_CartCreated_event() {
      DomainEvents<Cart, CartId> domainEvents = Cart.createCart(cartId, customerId);
      Cart cart = Cart.reHydrate(domainEvents);

      assertThat(cart.getCustomerId()).isEqualTo(customerId);
      assertThat(cart.getCartId()).isEqualTo(cartId);
      assertThat(cart.getContent()).isEmpty();
   }

   @Test
   void rehydrate_cart_from_ProductAddedToCart_event_throws_an_exception() {

      InMemoryEventStream<Cart, CartId> eventStream = new InMemoryEventStream<>(cartId,
          version(1),
          List.of(new CartCreated(cartId.id(), customerId.id(), version(1))));

      Cart cart = Cart.reHydrate(eventStream);

      assertThat(cart.getCustomerId()).isEqualTo(customerId);
      assertThat(cart.getCartId()).isEqualTo(cartId);
      assertThat(cart.getContent()).isEmpty();
   }
}
