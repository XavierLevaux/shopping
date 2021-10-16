package be.xl.shopping.domain.core.cart.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;

public record ProductQuantityRemovedFromCart(UUID cartId,
                                             UUID customerId,
                                             UUID productId,
                                             Integer quantity, Version version) implements DomainEvent<Cart> {

   @Override
   public String getType() {
      return "product-quantity-removed-from-cart";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
