package be.xl.architecture.eventsourcing.eventstore;

import be.xl.architecture.eventsourcing.model.AggregateIdentifier;
import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.EventSourcedAggregateRoot;

public interface EventStream<T extends EventSourcedAggregateRoot<T, ID>, ID extends AggregateIdentifier> extends
    Iterable<DomainEvent<T>> {

   ID getAggregateId();

   String getAggregateName();
}
