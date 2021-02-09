package be.xl.shopping.persistence.cart;

import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.cart.Cart;
import be.xl.shopping.domain.cart.CartId;
import java.util.Optional;

public class CartEventStore implements EventStore<Cart, CartId> {

   @Override
   public Optional<DomainEvents<Cart, CartId>> loadEvents(CartId aggregateId) {
      return Optional.empty();
   }

   @Override
   public void saveNewAggregate(CartId aggregateId, DomainEvents<Cart, CartId> events) {

   }

   @Override
   public void updateExistingAggregate(CartId aggregateId, Long version,
       DomainEvents<Cart, CartId> events) {

   }
}
