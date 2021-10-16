package be.xl.shopping.domain.port.command;

import be.xl.architecture.Command;
import java.util.UUID;

public record AddProductToCartCommand(UUID cartId,
                                      UUID productId,
                                      int quantity
) implements Command {

   @Override
   public UUID aggregateId() {
      return cartId;
   }
}
