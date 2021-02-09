package be.xl.shopping.domain.cart;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.catalog.ProductId;
import be.xl.shopping.domain.customer.CustomerId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CartTest {

   private final CustomerId customerId = CustomerId.of(UUID.randomUUID());
   private final CartId cartId = CartId.of(UUID.randomUUID());

   @Nested
   class when_a_cart_is_created {

      private final DomainEvents<Cart, CartId> domainEvents;

      when_a_cart_is_created() {
         domainEvents = Cart.createCart(cartId, customerId);
      }

      @Test
      void then_a_CartCreatedEvent_is_emitted() {
         assertThat(domainEvents.getEvents()).hasSize(1);
         assertThat(domainEvents.getEvents()).containsExactly(
             new CartCreated(cartId.getId(), customerId.getId(), 1L)
         );
      }
   }

   @Nested
   class when_I_apply_CartCreatedEvent_I_get_a_cart {

      private final Cart cart;

      public when_I_apply_CartCreatedEvent_I_get_a_cart() {
         cart = Cart.apply(new CartCreated(cartId.getId(), customerId.getId(), 0L));
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

      public when_I_have_an_existing_empty_cart() {
         cart = Cart.apply(new CartCreated(cartId.getId(), customerId.getId(), 0L));
      }

      @Nested
      class and_I_add_a_product_to_the_cart {

         private final int addedQuantity = 3;
         private final ProductId addedProductId = ProductId.of(UUID.randomUUID());
         private final DomainEvents<Cart, CartId> domainEvents;

         public and_I_add_a_product_to_the_cart() {
            domainEvents = cart.addProduct(addedProductId, addedQuantity);
         }

         @Test
         void then_there_is_one_ProductAddedToCart_returned() {
            assertThat(domainEvents.getEvents()).hasSize(1);
            assertThat(domainEvents.getEvents()).containsExactly(
                new ProductAddedToCart(cartId.getId(), customerId.getId(), addedProductId.getId(),
                    addedQuantity, 2L)
            );
         }
      }
   }

   @Nested
   class when_I_have_an_existing_cart_with_one_product {

      private Cart cart;
      private final int existingQuantity = 3;
      private final ProductId existingProductId = ProductId.of(UUID.randomUUID());

      public when_I_have_an_existing_cart_with_one_product() {
         cart = Cart.apply(new CartCreated(customerId.getId(), cartId.getId(), 1L));
         cart = cart.applyAll(cart.addProduct(existingProductId, existingQuantity).getEvents());
      }


      @Nested
      class and_I_remove_the_same_product_with_existing_quantity_minus_1 {

         public and_I_remove_the_same_product_with_existing_quantity_minus_1() {
            cart = cart
                .applyAll(cart.removeProduct(existingProductId, existingQuantity - 1).getEvents());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                CartItem.cartItem(existingProductId, 1)
            );
         }
      }

      @Nested
      class and_I_add_the_same_product {

         private final int addedQuantity = 2;

         public and_I_add_the_same_product() {
            cart = cart.applyAll(cart.addProduct(existingProductId, addedQuantity).getEvents());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                CartItem.cartItem(existingProductId, existingQuantity + addedQuantity)
            );
         }
      }

      @Nested
      class and_I_remove_the_same_product_by_quantity_in_cart_minus_one {

         public and_I_remove_the_same_product_by_quantity_in_cart_minus_one() {
            cart = cart
                .applyAll(cart.removeProduct(existingProductId, existingQuantity - 1).getEvents());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(cart.getContent()).hasSize(1);
            assertThat(cart.getContent()).containsExactly(
                CartItem.cartItem(existingProductId, 1)
            );
         }
      }

      @Nested
      class and_I_add_another_product {

         Cart nestedCart = cart;

         private final int addedQuantity = 2;
         private final ProductId otherProductId = ProductId.of(UUID.randomUUID());

         public and_I_add_another_product() {
            nestedCart = nestedCart
                .applyAll(cart.addProduct(otherProductId, addedQuantity).getEvents());
         }

         @Test
         void then_the_cart_item_quantity_is_adapted_for_product() {
            assertThat(nestedCart.getContent()).hasSize(2);
            assertThat(nestedCart.getContent()).containsExactly(
                CartItem.cartItem(existingProductId, existingQuantity),
                CartItem.cartItem(otherProductId, addedQuantity)
            );
         }
      }

   }

   @Nested
   class when_I_rehydrate_cart_from_two_events {

      protected final ProductId PRODUCT_ID = ProductId.of(UUID.randomUUID());
      protected final Cart cart;

      public when_I_rehydrate_cart_from_two_events() {

         List<DomainEvent<Cart>> domainEvents = List.of(
             new CartCreated(customerId.getId(), cartId.getId(), 0L),
             new ProductAddedToCart(cartId.getId(), customerId.getId(), PRODUCT_ID.getId(), 3,
                 2L));
         InMemoryEventStream<Cart, CartId> eventStream = new InMemoryEventStream<>(domainEvents);

         cart = Cart.reHydrate(eventStream);
      }

      @Test
      void then_cart_has_right_attributes() {
         assertThat(cart.getCustomerId()).isEqualTo(customerId);
         assertThat(cart.getCartId()).isEqualTo(cartId);
         assertThat(cart.getContent()).containsExactly(CartItem.cartItem(PRODUCT_ID, 3));
      }

      @Test
      void then_cart_has_version_corresponding_to_version_of_last_event() {
         assertThat(cart.getVersion()).isEqualTo(2L);
      }

   }


   @Test
   void rehydrate_cart_from_CartCreated_event() {
      InMemoryEventStream<Cart, CartId> eventStream = new InMemoryEventStream<>(
          List.of(new CartCreated(cartId.getId(), customerId.getId(), 1L)));
      Cart cart = Cart.reHydrate(eventStream);

      assertThat(cart.getCustomerId()).isEqualTo(customerId);
      assertThat(cart.getCartId()).isEqualTo(cartId);
      assertThat(cart.getContent()).isEmpty();
   }

   @Test
   void rehydrate_cart_from_ProductAddedToCart_event_throws_an_exception() {

      InMemoryEventStream<Cart, CartId> eventStream = new InMemoryEventStream<>(
          List.of(new CartCreated(cartId.getId(), customerId.getId(), 1L)));

      Cart cart = Cart.reHydrate(eventStream);

      assertThat(cart.getCustomerId()).isEqualTo(customerId);
      assertThat(cart.getCartId()).isEqualTo(cartId);
      assertThat(cart.getContent()).isEmpty();
   }
}
