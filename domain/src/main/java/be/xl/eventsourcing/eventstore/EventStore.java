package be.xl.eventsourcing.eventstore;

import be.xl.eventsourcing.model.DomainEvents;
import java.util.Optional;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

public interface EventStore<T extends AggregateRoot<T, ID>, ID extends Identifier> {


   Optional<DomainEvents<T, ID>> loadEvents(ID aggregateId);

   void saveNewAggregate(ID aggregateId, DomainEvents<T, ID> events);
   void updateExistingAggregate(ID aggregateId, Long version, DomainEvents<T, ID> events);
}
