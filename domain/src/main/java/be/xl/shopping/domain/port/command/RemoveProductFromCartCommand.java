package be.xl.shopping.domain.port.command;

import be.xl.eventsourcing.model.Command;
import java.util.UUID;

public class RemoveProductFromCartCommand implements Command {
   public final UUID cartId;
   public final UUID productId;
   public final int quantity;

   public RemoveProductFromCartCommand(UUID cartId, UUID productId, int quantity) {
      this.cartId = cartId;
      this.productId = productId;
      this.quantity = quantity;
   }

   @Override
   public UUID aggregateId() {
      return cartId;
   }
}
