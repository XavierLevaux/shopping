package be.xl.shopping.persistence.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface DomainEventMixIn {
   @JsonIgnore
   abstract String getType();
}
