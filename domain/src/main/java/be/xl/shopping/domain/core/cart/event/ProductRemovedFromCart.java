package be.xl.shopping.domain.core.cart.event;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEventBuilder;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@ToString
public class ProductRemovedFromCart implements DomainEvent<Cart> {

   private final UUID cartId;
   private final UUID customerId;
   private final UUID productId;
   private final Long version;

   public ProductRemovedFromCart(UUID cartId, UUID customerId, UUID productId,
       Long version) {
      this.cartId = cartId;
      this.customerId = customerId;
      this.productId = productId;
      this.version = version;
   }

   @Override
   public String getType() {
      return "product-removed-from-cart";
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

      ProductRemovedFromCart that = (ProductRemovedFromCart) o;

      return new EqualsBuilder()
          .append(cartId, that.cartId)
          .append(customerId, that.customerId)
          .append(productId, that.productId)
          .append(version, that.version)
          .isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37)
          .append(cartId)
          .append(customerId)
          .append(productId)
          .append(version)
          .toHashCode();
   }

   public static final class ProductRemovedFromCartBuilder implements DomainEventBuilder<Cart> {

      private final UUID cartId;
      private final UUID customerId;
      private final UUID productId;

      private ProductRemovedFromCartBuilder(UUID cartId, UUID customerId, UUID productId) {
         this.cartId = cartId;
         this.customerId = customerId;
         this.productId = productId;
      }

      public static ProductRemovedFromCartBuilder productRemovedFromCart(UUID cartId, UUID customerId,
          UUID productId) {
         return new ProductRemovedFromCartBuilder(cartId, customerId, productId);
      }

      @Override
      public DomainEvent<Cart> build(Long version) {
         return new ProductRemovedFromCart(cartId, customerId, productId, version);
      }
   }
}
