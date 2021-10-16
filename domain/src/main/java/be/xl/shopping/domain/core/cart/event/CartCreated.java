package be.xl.shopping.domain.core.cart.event;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;

public record CartCreated(UUID cartId, UUID customerId, Version version) implements DomainEvent<Cart> {

   @Override
   public String getType() {
      return "cart-created";
   }

   @Override
   public Version getVersion() {
      return version;
   }
}
