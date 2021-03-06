package be.xl.shopping.persistence.cart.mongodb;

import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CartEventsRepository extends MongoRepository<CartEvents, String> {

}
