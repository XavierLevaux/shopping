package be.xl.shopping.domain.cart;

import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.catalog.ProductId;
import be.xl.shopping.domain.customer.CustomerId;
import java.util.UUID;

public class CartService {

   private final EventStore<Cart, CartId> eventStore;

   public CartService(
       EventStore<Cart, CartId> eventStore) {
      this.eventStore = eventStore;
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
