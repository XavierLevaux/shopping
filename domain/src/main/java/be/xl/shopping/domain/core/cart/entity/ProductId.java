package be.xl.shopping.domain.core.cart.entity;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record ProductId(UUID id) implements Identifier {}
