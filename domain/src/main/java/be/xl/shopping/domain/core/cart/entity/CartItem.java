package be.xl.shopping.domain.core.cart.entity;


public record CartItem(ProductId productId, int quantity) {

   public CartItem addQuantity(int quantity) {
      return new CartItem(productId, this.quantity + quantity);
   }

   public CartItem removeQuantity(int quantity) {
      return new CartItem(productId, this.quantity - quantity);
   }
}
