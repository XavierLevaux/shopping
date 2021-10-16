package be.xl.shopping.domain.port.command;

import be.xl.architecture.Command;
import java.util.UUID;

public record CreateCartCommand(UUID cartId, UUID customerId) implements Command {

   @Override
   public UUID aggregateId() {
      return cartId;
   }
}
