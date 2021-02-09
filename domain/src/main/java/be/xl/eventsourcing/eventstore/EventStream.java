package be.xl.eventsourcing.eventstore;

import be.xl.eventsourcing.model.DomainEvent;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

public interface EventStream<T extends AggregateRoot<T, ID>, ID extends Identifier> extends Iterable<DomainEvent<T>> {
   long getAggregateVersion();
}
