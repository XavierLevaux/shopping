package be.xl.shopping.domain.core.cart.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;

public record ProductRemovedFromCart(UUID cartId,
                                     UUID customerId,
                                     UUID productId, Version version) implements DomainEvent<Cart> {

   @Override
   public String getType() {
      return "product-removed-from-cart";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
