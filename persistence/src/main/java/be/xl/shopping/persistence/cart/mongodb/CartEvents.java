package be.xl.shopping.persistence.cart.mongodb;

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_events")
public class CartEvents {

   @Id
   private final String cartId;
   private Long version;
   private final List<Event> events;

   public CartEvents(String cartId, Long version,
       List<Event> events) {
      this.cartId = cartId;
      this.version = version;
      this.events = events;
   }

   public String getCartId() {
      return cartId;
   }

   public Long getVersion() {
      return version;
   }

   public List<Event> getEvents() {
      return events;
   }

   public void addEvents(CartEvents events) {
      this.version = events.version;
      this.events.addAll(events.events);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      CartEvents that = (CartEvents) o;

      return new EqualsBuilder()
          .append(cartId, that.cartId)
          .append(version, that.version)
          .append(events, that.events)
          .isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37)
          .append(cartId)
          .append(version)
          .append(events)
          .toHashCode();
   }
}
