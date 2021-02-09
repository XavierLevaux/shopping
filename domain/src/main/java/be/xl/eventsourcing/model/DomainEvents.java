package be.xl.eventsourcing.model;

import be.xl.eventsourcing.eventstore.EventStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

public class DomainEvents<T extends AggregateRoot<T, ID>, ID extends Identifier>  implements EventStream<T, ID> {
   private final List<DomainEvent<T>> events;
   private final Long version;

   public DomainEvents(Long aggregateVersion) {
      this.version = aggregateVersion;
      this.events = Collections.emptyList();
   }

   private DomainEvents(Long version, List<DomainEvent<T>> events) {
      this.version = version;
      this.events = events;
   }

   public static <T extends AggregateRoot<T, ID>, ID extends Identifier> DomainEvents<T, ID> domainEvents(Long aggregateVersion) {
      return new DomainEvents<>(aggregateVersion);
   }

   long getNextVersion() {
      return version + 1;
   }

   public DomainEvents<T, ID> withEvent(DomainEventBuilder<T> eventBuilder) {
      long nextVersion = getNextVersion();

      ArrayList<DomainEvent<T>> newEvents = new ArrayList<>(events);
      newEvents.add(eventBuilder.build(nextVersion));
      return new DomainEvents<>(nextVersion, Collections.unmodifiableList(newEvents));
   }

   public DomainEvents<T, ID> add(DomainEvent<T> event) {

      return new DomainEvents<>(version, Collections.singletonList(event));
   }

   public DomainEvents<T, ID> add(DomainEvents<T, ID> events) {
      List<DomainEvent<T>> newDomainEvents = new ArrayList<>(this.events);
      newDomainEvents.addAll(events.getEvents());
      return new DomainEvents<>(version, Collections.unmodifiableList(newDomainEvents));
   }

   public List<DomainEvent<T>> getEvents() {
      return Collections.unmodifiableList(events);
   }

   @Override
   public long getAggregateVersion() {
      return version;
   }

   @Override
   public Iterator<DomainEvent<T>> iterator() {
      return getEvents().iterator();
   }
}
