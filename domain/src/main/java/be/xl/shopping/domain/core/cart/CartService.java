package be.xl.shopping.domain.core.cart;

import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.catalog.entity.ProductId;
import be.xl.shopping.domain.core.customer.entity.CustomerId;
import be.xl.shopping.domain.port.command.AddProductToCartCommand;
import be.xl.shopping.domain.port.command.CreateCartCommand;
import be.xl.shopping.domain.port.command.RemoveProductFromCartCommand;
import java.util.UUID;

public class CartService {

   private final EventStore<Cart, CartId> eventStore;

   public CartService(
       EventStore<Cart, CartId> cartEventStore) {
      this.eventStore = cartEventStore;
   }

   public void handleCreateCart(CreateCartCommand command) {
      DomainEvents<Cart, CartId> domainEvents = Cart
          .createCart(CartId.of(command.cartId), CustomerId.of(command.customerId));
      eventStore.saveNewAggregate(CartId.of(command.cartId), domainEvents);
   }

   public void handleAddProductToCart(AddProductToCartCommand command) {
      Cart cart = getCart(command.cartId);
      eventStore.updateExistingAggregate(CartId.of(command.cartId), cart.getVersion(),
          cart.addProduct(ProductId.of(command.productId), command.quantity));
   }

   private Cart getCart(UUID cartId) {
      DomainEvents<Cart, CartId> domainEvents = eventStore.loadEvents(CartId.of(cartId))
          .orElseThrow(() -> new RuntimeException("No events found for re-hydrating"));
      return Cart.reHydrate(domainEvents);
   }


   public void handleRemoveProductFromCart(RemoveProductFromCartCommand command) {
      Cart cart = getCart(command.cartId);
      eventStore.updateExistingAggregate(CartId.of(command.cartId), cart.getVersion(),
          cart.removeProduct(ProductId.of(command.productId), command.quantity));
   }

}
