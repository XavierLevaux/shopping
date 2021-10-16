package be.xl.shopping.domain.core.customer.entity;

public record EmailAddress(String value) {

   public EmailAddress {
      //TODO: validate value is a valid email
   }
}
