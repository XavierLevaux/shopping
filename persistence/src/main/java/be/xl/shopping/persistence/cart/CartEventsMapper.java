package be.xl.shopping.persistence.cart;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CartEventsMapper {

   private final ObjectMapper objectMapper;
   private final ObjectWriter writer;

   public CartEventsMapper() {
      objectMapper = new ObjectMapper();
      HashMap<Class<?>, Class<?>> sourceMixins = new HashMap<>();
      sourceMixins.put(DomainEvent.class, DomainEventMixIn.class);

      objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
//      objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
//      objectMapper.registerModule(new JavaTimeModule());
//      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

      objectMapper.setMixIns(sourceMixins);

      writer = objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT);
   }

   public DomainEvents<Cart, CartId> map(CartEvents cartEvents) {
      List<DomainEvent<Cart>> collect = cartEvents.getEvents().stream()
          .map(this::map)
          .collect(Collectors.toList());

      return new DomainEvents<>(
          new CartId(UUID.fromString(cartEvents.getCartId())),
          Version.version(cartEvents.getVersion()),
          collect
      );
   }

   @SuppressWarnings("unchecked")
   public DomainEvent<Cart> map(Event event) {
      try {
         return (DomainEvent<Cart>) objectMapper.readValue(
             event.payload(),
             Class.forName(event.type())
         );
      } catch (JsonProcessingException | ClassNotFoundException e) {
         throw new IllegalArgumentException(
             String.format(String.format("Could not convert event %s to DomainEvent!", event), e));
      }
   }

   public Event map(DomainEvent<Cart> domainEvent) {
      try {
         String payload = writer.writeValueAsString(domainEvent);

         return new Event(domainEvent.getVersion().version(), domainEvent.getClass().getName(),
             payload);

      } catch (JsonProcessingException e) {
         throw new IllegalArgumentException(
             String.format(String.format("Could not convert DomainEvent %s to Event!", domainEvent),
                 e));
      }
   }

   public CartEvents mapDomainEvents(DomainEvents<Cart, CartId> events) {
      return new CartEvents(
          events.aggregateId().id().toString(),
          events.getToAggregateVersion().version(),
          events.events().stream().map(this::map).collect(Collectors.toList())
      );
   }
}
