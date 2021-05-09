package be.xl.shopping.domain.port.command;

import be.xl.eventsourcing.model.Command;
import java.util.UUID;

public class CreateCartCommand implements Command {

   public final UUID cartId;
   public final UUID customerId;

   public CreateCartCommand(UUID cartId,
       UUID customerId) {
      this.cartId = cartId;
      this.customerId = customerId;
   }

   @Override
   public UUID aggregateId() {
      return cartId;
   }
}
