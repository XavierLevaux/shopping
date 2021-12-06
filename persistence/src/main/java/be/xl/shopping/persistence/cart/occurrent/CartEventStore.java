package be.xl.shopping.persistence.cart.occurrent;

import be.xl.architecture.Adapter;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.persistence.OccurrentEventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.occurrent.eventstore.mongodb.spring.blocking.SpringMongoEventStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Adapter
@Service
@Transactional(readOnly = true)
public class CartEventStore extends OccurrentEventStore<Cart, CartId> {
    public CartEventStore(SpringMongoEventStore springMongoEventStore, PlatformTransactionManager transactionManager, ObjectMapper eventObjectMapper) {
        super(springMongoEventStore, transactionManager, eventObjectMapper);
    }
}
