package be.xl.shopping.domain.port.infrastructure;

import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.eventsourcing.model.DomainEvents;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

public class InMemoryEventStore<T extends AggregateRoot<T, ID>, ID extends Identifier> implements
    EventStore<T, ID> {

   final Map<ID, DomainEvents<T, ID>> aggregateEvents = new HashMap<>();

   @Override
   public Optional<DomainEvents<T, ID>> loadEvents(ID aggregateId) {
      return aggregateEvents.containsKey(aggregateId) ? Optional
          .of(aggregateEvents.get(aggregateId)) : Optional.empty();
   }

   @Override
   public void saveNewAggregate(ID aggregateId, DomainEvents<T, ID> events) {
      if (aggregateEvents.containsKey(aggregateId)) {
         throw new IllegalStateException("Aggregate already exists ");
      }
      aggregateEvents.put(aggregateId, events);
   }

   @Override
   public void updateExistingAggregate(ID aggregateId, Long version, DomainEvents<T, ID> events) {
      DomainEvents<T, ID> domainEvents = Optional.of(aggregateEvents.get(aggregateId))
          .orElseThrow(() -> new IllegalStateException("Aggregate already exists "));
      aggregateEvents.put(aggregateId, domainEvents.add(events));
   }
}
