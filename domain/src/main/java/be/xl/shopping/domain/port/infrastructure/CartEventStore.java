package be.xl.shopping.domain.port.infrastructure;

import be.xl.architecture.Port;
import be.xl.eventsourcing.eventstore.EventStore;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;

@Port
public interface CartEventStore extends EventStore<Cart, CartId> {

}
