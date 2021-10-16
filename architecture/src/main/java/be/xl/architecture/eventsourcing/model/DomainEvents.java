package be.xl.architecture.eventsourcing.model;

import be.xl.architecture.eventsourcing.eventstore.EventStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public record DomainEvents <T extends EventSourcedAggregateRoot<T, ID>, ID extends AggregateIdentifier> (
    ID aggregateId,
    Version fromAggregateVersion,
    List<DomainEvent<T>> events
) implements EventStream<T, ID> {

   public DomainEvents<T, ID> withEvents(DomainEvents<T, ID> otherEvents) {
      List<DomainEvent<T>> allEvents = new ArrayList<>(events);
      allEvents.addAll(otherEvents.events);

      Version newAggregateVersion = lastEventVersion(allEvents);
      return new DomainEvents<>(aggregateId, newAggregateVersion,
          Collections.unmodifiableList(allEvents));
   }

   private Version lastEventVersion(List<DomainEvent<T>> events) {
      return events.stream()
          .reduce(fromAggregateVersion, (version, tDomainEvent) -> tDomainEvent.getVersion(),
              (version, version2) -> version2);
   }

   public Version getFromAggregateVersion() {
      return fromAggregateVersion;
   }
   public Version getToAggregateVersion() {
      return lastEventVersion(events);
   }

   @Override
   public String getAggregateName() {
      return aggregateId.getAggregateName();
   }

   @Override
   public ID getAggregateId() {
      return aggregateId;
   }

   @Override
   public Iterator<DomainEvent<T>> iterator() {
      return this.events.iterator();
   }
}
