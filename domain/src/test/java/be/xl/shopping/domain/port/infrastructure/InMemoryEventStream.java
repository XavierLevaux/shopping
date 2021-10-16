package be.xl.shopping.domain.port.infrastructure;

import be.xl.architecture.eventsourcing.eventstore.EventStream;
import be.xl.architecture.eventsourcing.model.AggregateIdentifier;
import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.EventSourcedAggregateRoot;
import be.xl.architecture.eventsourcing.model.Version;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class InMemoryEventStream<T extends EventSourcedAggregateRoot<T, ID>, ID extends AggregateIdentifier> implements
    EventStream<T, ID> {

   private final List<DomainEvent<T>> events;
   private final ID aggregateId;
   private final Version version;

   public InMemoryEventStream(ID aggregateId, Version version, List<DomainEvent<T>> events) {
      this.aggregateId = aggregateId;
      this.version = version;
      this.events = events;
   }

   @Override
   public Iterator<DomainEvent<T>> iterator() {
      return events.iterator();
   }

   @Override
   public void forEach(Consumer<? super DomainEvent<T>> action) {
      events.forEach(action);

   }

   @Override
   public Spliterator<DomainEvent<T>> spliterator() {
      return events.spliterator();
   }

   @Override
   public String getAggregateName() {
      return aggregateId.getAggregateName();
   }

   @Override
   public ID getAggregateId() {
      return aggregateId;
   }
}
