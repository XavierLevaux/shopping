package be.xl.shopping.domain.cart;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEventBuilder;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CartCreated implements DomainEvent<Cart> {

   public static final String TYPE = "cart-created";

   private final UUID customerId;
   private final UUID cartId;
   private final Long version;

   public CartCreated(UUID cartId, UUID customerId, long version) {
      this.customerId = customerId;
      this.cartId = cartId;
      this.version = version;
   }

   public String getType() {
      return TYPE;
   }

   @Override
   public Long getVersion() {
      return version;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      CartCreated that = (CartCreated) o;

      if (!customerId.equals(that.customerId)) {
         return false;
      }
      if (!cartId.equals(that.cartId)) {
         return false;
      }
      return version.equals(that.version);
   }

   @Override
   public int hashCode() {
      int result = customerId.hashCode();
      result = 31 * result + cartId.hashCode();
      result = 31 * result + version.hashCode();
      return result;
   }

   public static final class CartCreatedBuilder implements DomainEventBuilder<Cart> {

      private final UUID customerId;
      private final UUID cartId;

      private CartCreatedBuilder(UUID cartId, UUID customerId) {
         this.customerId = customerId;
         this.cartId = cartId;
      }

      public static DomainEventBuilder<Cart> cartCreated(UUID cartId, UUID customerId) {
         return new CartCreatedBuilder(cartId, customerId);
      }

      @Override
      public CartCreated build(Long version) {
         return new CartCreated(cartId, customerId, version);
      }
   }
}
