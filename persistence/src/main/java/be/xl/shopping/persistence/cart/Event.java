package be.xl.shopping.persistence.cart;

public record Event(Long version, String type, String payload) {

}
