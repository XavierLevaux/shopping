package be.xl.shopping.domain.core.cart.event;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.eventsourcing.model.DomainEventBuilder;
import be.xl.shopping.domain.core.cart.entity.Cart;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ProductAddedToCart implements DomainEvent<Cart> {
   public static final String TYPE = "product-added-to-cart";

   private final UUID cartId;
   private final UUID customerId;
   private final UUID productId;
   private final int quantity;
   private final Long version;

   public ProductAddedToCart(UUID cartId, UUID customerId, UUID productId, int quantity,
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

      ProductAddedToCart that = (ProductAddedToCart) o;

      if (quantity != that.quantity) {
         return false;
      }
      if (!cartId.equals(that.cartId)) {
         return false;
      }
      if (!customerId.equals(that.customerId)) {
         return false;
      }
      if (!productId.equals(that.productId)) {
         return false;
      }
      return version.equals(that.version);
   }

   @Override
   public int hashCode() {
      int result = cartId.hashCode();
      result = 31 * result + customerId.hashCode();
      result = 31 * result + productId.hashCode();
      result = 31 * result + quantity;
      result = 31 * result + version.hashCode();
      return result;
   }

   public static final class ProductAddedToCartBuilder implements DomainEventBuilder<Cart> {
      private final UUID cartId;
      private final UUID customerId;
      private final UUID productId;
      private final int quantity;

      private ProductAddedToCartBuilder(UUID cartId, UUID customerId, UUID productId,
          int quantity) {
         this.cartId = cartId;
         this.customerId = customerId;
         this.productId = productId;
         this.quantity = quantity;
      }

      public static ProductAddedToCartBuilder productAddedToCart(UUID cartId, UUID customerId,
          UUID productId, int quantity) {
         return new ProductAddedToCartBuilder(cartId, customerId, productId, quantity);
      }

      @Override
      public DomainEvent<Cart> build(Long version) {
         return new ProductAddedToCart(cartId, customerId, productId, quantity, version);
      }
   }
}
