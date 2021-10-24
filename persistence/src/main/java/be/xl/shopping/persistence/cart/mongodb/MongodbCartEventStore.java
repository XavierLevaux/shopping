package be.xl.shopping.persistence.cart.mongodb;

import be.xl.architecture.Adapter;
import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.eventstore.StaleAggregateVersionException;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Adapter
@Service
@Transactional(readOnly = true)
public class MongodbCartEventStore implements EventStore<Cart, CartId> {

   private final CartEventsRepository repository;
   private final CartEventsMapper cartEventsMapper;

   public MongodbCartEventStore(
       CartEventsRepository repository,
       CartEventsMapper cartEventsMapper
   ) {
      this.repository = repository;
      this.cartEventsMapper = cartEventsMapper;
   }

   @Override
   public Optional<DomainEvents<Cart, CartId>> loadEvents(CartId aggregateId) {
      String id = aggregateId.id().toString();
      return repository.findById(id).map(cartEventsMapper::map);
   }

   @Override
   @Transactional
   public void saveNewAggregate(CartId aggregateId, DomainEvents<Cart, CartId> events) {
      CartEvents cartEvents = cartEventsMapper.mapDomainEvents(events);
      repository.save(cartEvents);
   }

   @Override
   @Transactional
   public void updateExistingAggregate(CartId aggregateId,
       DomainEvents<Cart, CartId> events) {
      String id = aggregateId.id().toString();
      CartEvents cartEvents = repository.findById(id).orElseThrow(() -> new NoSuchElementException(
          "Could not find existing events for aggregateId %s"
              .formatted(aggregateId.id().toString())));

      if (!cartEvents.getVersion().equals(events.getFromAggregateVersion().version())) {
         throw new StaleAggregateVersionException(aggregateId.getAggregateName(), Version.version(cartEvents.getVersion()), events.fromAggregateVersion());
      }

      cartEvents.addEvents(cartEventsMapper.mapDomainEvents(events));
      repository.save(cartEvents);
   }
}