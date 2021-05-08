package be.xl.shopping.domain.core.catalog.entity;

import be.xl.eventsourcing.model.DomainEvent;
import be.xl.shopping.domain.core.catalog.event.ProductCreated;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Product {
   private final ProductId productId;
   private String description;
   private final List<DomainEvent<Product>> pendingEvents = new ArrayList<>();

   private Product(ProductId productId) {
      this.productId = productId;
   }

   private Product(ProductId productId, String description) {
      this(productId);
      this.description = description;
   }

   public static Product createProduct(ProductId productId, String description) {
      Product product = new Product(productId);
      product.pendingEvents.add(new ProductCreated(productId.getId(), description));
      return product;
   }

}
