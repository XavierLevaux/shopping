package be.xl.shopping.domain.core.cart.entity;

import static be.xl.shopping.domain.core.cart.event.CartCreated.CartCreatedBuilder.cartCreated;
import static be.xl.shopping.domain.core.cart.event.ProductAddedToCart.ProductAddedToCartBuilder.productAddedToCart;
import static be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart.ProductQuantityRemovedFromCartBuilder.productQuantityRemovedFromCart;
import static be.xl.shopping.domain.core.cart.event.ProductRemovedFromCart.ProductRemovedFromCartBuilder.productRemovedFromCart;

import be.xl.eventsourcing.eventstore.EventStream;
import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart;
import be.xl.shopping.domain.core.catalog.entity.ProductId;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import java.util.List;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
public class Cart implements AggregateRoot<Cart, CartId> {

   private final CartId cartId;
   private final CustomerId customerId;
   private final CartItems cartItems;
   public final long version;

   private Cart(CartId cartId, CustomerId customerId, CartItems cartItems, long version) {
      this.cartId = cartId;
      this.customerId = customerId;
      this.cartItems = cartItems;
      this.version = version;
   }

   private Cart(CartId cartId, CustomerId customerId) {
      this(cartId, customerId, CartItems.empty(), 1);
   }

   @Override
   public CartId getId() {
      return cartId;
   }

   public static DomainEvents<Cart, CartId> createCart(CartId cartId, CustomerId customerId) {
      return new DomainEvents<Cart, CartId>(0L)
          .withEvent(cartCreated(cartId.getId(), customerId.getId()));
   }

   public DomainEvents<Cart, CartId> addProduct(ProductId productId, int quantity) {
      if (quantity <= 0) {
         throw new IllegalArgumentException("Add a positive quantity!");
      }
      return new DomainEvents<Cart, CartId>(version)
          .withEvent(
              productAddedToCart(cartId.getId(), customerId.getId(), productId.getId(), quantity));
   }

   public DomainEvents<Cart, CartId> removeProduct(ProductId productId, int quantityToRemove) {
      if (quantityToRemove <= 0) {
         throw new IllegalArgumentException("Add a positive quantity!");
      }
      Integer quantityOfProductInCart = cartItems.getProductQuantity(productId)
          .orElseThrow(() -> new IllegalStateException("Product is not in cart"));
      if (quantityOfProductInCart < quantityToRemove) {
         throw new IllegalStateException("You cannot remove more quantity then content of cart");
      }
      if (quantityOfProductInCart == quantityToRemove) {
         return new DomainEvents<Cart, CartId>(version)
             .withEvent(
                 productRemovedFromCart(cartId.getId(), customerId.getId(), productId.getId()));
      } else {
         return new DomainEvents<Cart, CartId>(version)
             .withEvent(productQuantityRemovedFromCart(cartId.getId(), customerId.getId(),
                 productId.getId(), quantityToRemove));
      }
   }

   public List<CartItem> getContent() {
      return cartItems.asList();
   }

   public static Cart reHydrate(EventStream<Cart, CartId> eventStream) {
      Cart cart = null;
      for (DomainEvent<Cart> event : eventStream) {
         if (cart == null) {
            if (!event.getType().equals(CartCreated.TYPE)) {
               throw new IllegalStateException(
                   String.format("First event should be %s", CartCreated.TYPE));
            }
            cart = Cart.apply((CartCreated) event);
         } else {
            cart = cart.apply(event);
         }
      }
      return cart;
   }

   public Cart applyAll(List<DomainEvent<Cart>> events) {
      Cart cart = this;
      for (DomainEvent<Cart> event : events) {
         cart = cart.apply(event);
      }
      return cart;
   }

   private Cart apply(DomainEvent<Cart> event) {
      if (event.isType(CartCreated.TYPE)) {
         return apply((CartCreated) event);
      } else if (event.isType(ProductAddedToCart.TYPE)) {
         return apply((ProductAddedToCart) event);
      } else if (event.isType(ProductQuantityRemovedFromCart.TYPE)) {
         return apply((ProductQuantityRemovedFromCart) event);
      } else {
         throw new IllegalArgumentException(
             String.format("This event %s is not supported", event));
      }
   }


   public static Cart apply(CartCreated event) {
      return new Cart(CartId.of(event.getCartId()), CustomerId.of(event.getCustomerId()));
   }

   public Cart apply(ProductAddedToCart event) {
      return new Cart(
          CartId.of(event.getCartId()),
          CustomerId.of(event.getCustomerId()),
          cartItems.addProduct(ProductId.of(event.getProductId()), event.getQuantity()),
          event.getVersion());
   }

   public Cart apply(ProductQuantityRemovedFromCart event) {
      return new Cart(
          CartId.of(event.getCartId()),
          CustomerId.of(event.getCustomerId()),
          cartItems.removeProduct(ProductId.of(event.getProductId()), event.getQuantity()),
          event.getVersion());
   }
}
