package be.xl.shopping.domain.core.cart.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public class CartItems {

   private final List<CartItem> items;

   public CartItems(List<CartItem> items) {
      this.items = Collections.unmodifiableList(items);
   }

   public static CartItems empty() {
      return new CartItems(Collections.emptyList());
   }

   CartItems addProduct(ProductId productId, int quantity) {
      List<CartItem> newItems = new ArrayList<>();
      boolean productFound = false;
      for (CartItem item : items) {
         if (item.productId().equals(productId)) {
            productFound = true;
            newItems.add(item.addQuantity(quantity));
         } else {
            newItems.add(item);
         }
      }
      if (!productFound) {
         newItems.add(new CartItem(productId, quantity));
      }
      return new CartItems(newItems);
   }

   public List<CartItem> asList() {
      return items;
   }

   public Optional<Integer> getProductQuantity(ProductId productId) {
      return items.stream().filter(cartItem -> cartItem.productId().equals(productId))
          .map(CartItem::quantity).findFirst();
   }

   public CartItems removeProduct(ProductId productId, Integer quantity) {
      List<CartItem> newItems = new ArrayList<>();
      items.forEach(cartItem -> newItems.add(
          cartItem.productId().equals(productId) ?
              new CartItem(productId, cartItem.quantity() - quantity) :
              cartItem)
      );
      return new CartItems(newItems);
   }
}
