package be.xl.architecture.eventsourcing.model;

import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

public interface AggregateIdentifier extends Identifier {
   String getAggregateName();
   UUID id();
}
