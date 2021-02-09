package be.xl.shopping.domain.cart;

import be.xl.eventsourcing.eventstore.EventStream;
import be.xl.eventsourcing.model.DomainEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

public class InMemoryEventStream<T extends AggregateRoot<T, ID>, ID extends Identifier> implements
    EventStream<T, ID> {

   private final List<DomainEvent<T>> events;

   public InMemoryEventStream(List<DomainEvent<T>> events) {
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
   public long getAggregateVersion() {
      throw new IllegalStateException("Not yet implemented!");
   }
}
