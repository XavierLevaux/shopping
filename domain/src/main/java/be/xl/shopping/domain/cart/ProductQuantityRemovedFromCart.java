package be.xl.shopping.domain.cart;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEventBuilder;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@ToString
public class ProductQuantityRemovedFromCart implements DomainEvent<Cart> {

   public static final String TYPE = "product-quantity-removed-from-cart";
   private final UUID cartId;
   private final UUID customerId;
   private final UUID productId;
   private final Integer quantity;
   private final Long version;

   public ProductQuantityRemovedFromCart(UUID cartId, UUID customerId, UUID productId, Integer quantity,
       Long version) {
      this.cartId = cartId;
      this.customerId = customerId;
      this.productId = productId;
      this.quantity = quantity;
      this.version = version;
   }

   @Override
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

      ProductQuantityRemovedFromCart that = (ProductQuantityRemovedFromCart) o;

      return new EqualsBuilder()
          .append(cartId, that.cartId)
          .append(customerId, that.customerId)
          .append(productId, that.productId)
          .append(quantity, that.quantity)
          .append(version, that.version)
          .isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37)
          .append(cartId)
          .append(customerId)
          .append(productId)
          .append(quantity)
          .append(version)
          .toHashCode();
   }

   public static final class ProductQuantityRemovedFromCartBuilder implements DomainEventBuilder<Cart> {

      private final UUID cartId;
      private final UUID customerId;
      private final UUID productId;
      private final Integer quantity;

      private ProductQuantityRemovedFromCartBuilder(UUID cartId, UUID customerId, UUID productId, Integer quantity) {
         this.cartId = cartId;
         this.customerId = customerId;
         this.productId = productId;
         this.quantity = quantity;
      }

      public static ProductQuantityRemovedFromCartBuilder productQuantityRemovedFromCart(UUID cartId, UUID customerId,
          UUID productId, Integer quantity) {
         return new ProductQuantityRemovedFromCartBuilder(cartId, customerId, productId, quantity);
      }

      @Override
      public DomainEvent<Cart> build(Long version) {
         return new ProductQuantityRemovedFromCart(cartId, customerId, productId, quantity, version);
      }
   }
}
