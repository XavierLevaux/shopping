package be.xl.shopping.domain.cart;

import be.xl.shopping.domain.catalog.ProductId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(staticName = "cartItem")
public class CartItem {

   private final ProductId productId;
   private final int quantity;

   public CartItem addQuantity(int quantity) {
      return new CartItem(productId, this.quantity + quantity);
   }

   public CartItem removeQuantity(int quantity) {
      return new CartItem(productId, this.quantity - quantity);
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this)
          .append("productId", productId)
          .append("quantity", quantity)
          .toString();
   }
}
