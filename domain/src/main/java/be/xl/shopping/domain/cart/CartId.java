package be.xl.shopping.domain.cart;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jmolecules.ddd.types.Identifier;

@Value
@RequiredArgsConstructor(staticName = "of")
public class CartId implements Identifier {
   public UUID id;
}