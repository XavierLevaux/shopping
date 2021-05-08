package be.xl.shopping.domain.core.catalog.entity;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jmolecules.ddd.types.Identifier;

@Value
@RequiredArgsConstructor(staticName = "of")
public class ProductId implements Identifier {
   UUID id;
}
