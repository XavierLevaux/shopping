package be.xl.shopping.persistence.cart;

import static org.assertj.core.api.Assertions.assertThat;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CartEventsMapperTest {

   private final CartEventsMapper mapper = new CartEventsMapper();

   @Test
   void mapping_a_domain_event_to_a_persistence_event_should_filter_type() {
      String cartId = "8ba3313c-f132-416c-a4a5-81fe04cbd36c";
      String customrId = "e79146e6-b349-4135-aa2b-5052a8321159";
      DomainEvent<Cart> domainEvent = new CartCreated(
          UUID.fromString(cartId),
          UUID.fromString(customrId), Version
          .version(1L)
      );

      Event actual = mapper.map(domainEvent);

      Event expected = new Event(1L, "be.xl.shopping.domain.core.cart.event.CartCreated",
          "{\n"
              + "  \"cartId\" : \"8ba3313c-f132-416c-a4a5-81fe04cbd36c\",\n"
              + "  \"customerId\" : \"e79146e6-b349-4135-aa2b-5052a8321159\",\n"
              + "  \"version\" : {\n"
              + "    \"version\" : 1\n"
              + "  }\n"
              + "}"
      );
      assertThat(actual).isEqualTo(expected);
   }



   @Test
   void mapping_a_persistence_event_back_to_a_domain_event() {
      Event persistenceEvent = new Event(1L, "be.xl.shopping.domain.core.cart.event.CartCreated",
          "{\n"
              + "  \"cartId\" : \"8ba3313c-f132-416c-a4a5-81fe04cbd36c\",\n"
              + "  \"customerId\" : \"e79146e6-b349-4135-aa2b-5052a8321159\",\n"
              + "  \"version\" : {\n"
              + "    \"version\" : 1\n"
              + "  }\n"
              + "}"
      );

      DomainEvent<Cart> actual = mapper.map(persistenceEvent);

      assertThat(actual).isEqualTo(
          new CartCreated(
              UUID.fromString("8ba3313c-f132-416c-a4a5-81fe04cbd36c"),
              UUID.fromString("e79146e6-b349-4135-aa2b-5052a8321159"), Version
              .version(1L)
          )
      );
   }

}