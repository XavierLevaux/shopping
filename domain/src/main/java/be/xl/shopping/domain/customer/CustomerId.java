package be.xl.shopping.domain.customer;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jmolecules.ddd.types.Identifier;

@Value
@RequiredArgsConstructor(staticName = "of")
public class CustomerId implements Identifier {
   UUID id;
}
