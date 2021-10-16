package be.xl.architecture.eventsourcing.eventstore;

import be.xl.architecture.Port;
import be.xl.architecture.eventsourcing.model.AggregateIdentifier;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.EventSourcedAggregateRoot;
import java.util.Optional;

@Port
public interface EventStore<T extends EventSourcedAggregateRoot<T, ID>, ID extends AggregateIdentifier> {


   Optional<DomainEvents<T, ID>> loadEvents(ID aggregateId);

   void saveNewAggregate(ID aggregateId, DomainEvents<T, ID> events);

   void updateExistingAggregate(ID aggregateId, DomainEvents<T, ID> events)
       throws StaleAggregateVersionException;
}
