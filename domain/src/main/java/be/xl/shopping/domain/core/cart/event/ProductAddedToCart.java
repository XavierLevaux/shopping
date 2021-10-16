package be.xl.shopping.domain.core.cart.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;

public record ProductAddedToCart(UUID cartId,
                                 UUID customerId,
                                 UUID productId,
                                 int quantity, Version version) implements DomainEvent<Cart> {

   public String getType() {
      return "product-added-to-cart";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
