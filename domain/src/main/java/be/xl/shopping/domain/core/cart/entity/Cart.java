package be.xl.shopping.domain.core.cart.entity;

import static be.xl.architecture.eventsourcing.model.DomainEventVersionGenerator.domainEventVersionGenerator;
import static io.vavr.collection.List.ofAll;

import be.xl.architecture.eventsourcing.eventstore.EventStream;
import be.xl.architecture.eventsourcing.model.DomainBusinessException;
import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.DomainEventVersionGenerator;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.EventSourcedAggregateRoot;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.architecture.eventsourcing.model.Versioned;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart;
import be.xl.shopping.domain.core.cart.event.ProductRemovedFromCart;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import java.util.List;
import org.jmolecules.ddd.types.AggregateRoot;

public class Cart implements EventSourcedAggregateRoot<Cart, CartId>, AggregateRoot<Cart, CartId>, Versioned {

   private final CartId cartId;
   private CustomerId customerId;
   private CartItems cartItems;
   public final Version version;

   private Cart(CartId cartId, CustomerId customerId, CartItems cartItems, Version version) {
      this.cartId = cartId;
      this.customerId = customerId;
      this.cartItems = cartItems;
      this.version = version;
   }

   private Cart(CartId cartId, Version version) {
      this.cartId = cartId;
      this.version = version;
   }

   @Override
   public CartId getId() {
      return cartId;
   }

   public CartId getCartId() {
      return cartId;
   }

   public CustomerId getCustomerId() {
      return customerId;
   }

   public CartItems getCartItems() {
      return cartItems;
   }

   @Override
   public Version getVersion() {
      return version;
   }

   public static DomainEvents<Cart, CartId> createCart(CartId cartId, CustomerId customerId) {
      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(
          Version.initialVersion());
      return new DomainEvents<>(cartId, versionGenerator.getAggregateVersion(),
          List.of(
              new CartCreated(cartId.id(), customerId.id(),
                  versionGenerator.nextVersion())
          ));
   }

   public DomainEvents<Cart, CartId> addProduct(ProductId productId, int quantity) {
      if (quantity <= 0) {
         throw new DomainBusinessException(cartId.id(), "Add a positive quantity!");
      }

      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(version);
      return new DomainEvents<>(cartId, versionGenerator.getAggregateVersion(),
          List.of(
              new ProductAddedToCart(cartId.id(), customerId.id(), productId.id(), quantity,
                  versionGenerator.nextVersion())
          ));
   }

   public DomainEvents<Cart, CartId> removeProduct(ProductId productId, int quantityToRemove) {
      if (quantityToRemove <= 0) {
         throw new DomainBusinessException(cartId.id(), "Add a positive quantity!");
      }
      Integer quantityOfProductInCart = cartItems.getProductQuantity(productId)
          .orElseThrow(() -> new IllegalStateException("Product is not in cart"));
      if (quantityOfProductInCart < quantityToRemove) {
         throw new DomainBusinessException(cartId.id(),
             "You cannot remove more quantity then content of cart");
      }

      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(version);
      if (quantityOfProductInCart == quantityToRemove) {
         return new DomainEvents<>(cartId, version, List.of(
             new ProductRemovedFromCart(cartId.id(), customerId.id(), productId.id(),
                 versionGenerator.nextVersion())
         ));
      } else {
         return new DomainEvents<>(cartId, version, List.of(
             new ProductQuantityRemovedFromCart(cartId.id(), customerId.id(),
                 productId.id(), quantityToRemove, versionGenerator.nextVersion())
         ));
      }
   }

   public List<CartItem> getContent() {
      return cartItems.asList();
   }

   public static Cart reHydrate(EventStream<Cart, CartId> eventStream) {
      return ofAll(eventStream)
          .foldLeft(new Cart(eventStream.getAggregateId(), Version.initialVersion()), Cart::apply);
   }

   public Cart apply(DomainEvent<Cart> event) {
      if (event instanceof CartCreated) {
         return apply((CartCreated) event);
      } else if (event.isType("product-added-to-cart")) {
         return apply((ProductAddedToCart) event);
      } else if (event.isType("product-quantity-removed-from-cart")) {
         return apply((ProductQuantityRemovedFromCart) event);
      } else {
         throw new IllegalArgumentException(
             String.format("This event %s is not supported", event));
      }
   }


   public Cart apply(CartCreated event) {
      return new Cart(new CartId(event.cartId()), new CustomerId(event.customerId()),
          CartItems.empty(), event.version());
   }

   public Cart apply(ProductAddedToCart event) {
      return new Cart(
          new CartId(event.cartId()),
          new CustomerId(event.customerId()),
          cartItems.addProduct(new ProductId(event.productId()), event.quantity()),
          event.getVersion());
   }

   public Cart apply(ProductQuantityRemovedFromCart event) {
      return new Cart(
          new CartId(event.cartId()),
          new CustomerId(event.customerId()),
          cartItems.removeProduct(new ProductId(event.productId()), event.quantity()),
          event.getVersion());
   }
}
