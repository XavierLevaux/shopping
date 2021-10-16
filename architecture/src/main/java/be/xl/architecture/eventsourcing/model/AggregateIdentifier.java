package be.xl.architecture.eventsourcing.model;

import org.jmolecules.ddd.types.Identifier;

public interface AggregateIdentifier extends Identifier {
   String getAggregateName();
}
