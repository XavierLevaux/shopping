package be.xl.shopping.persistence.cart.mongodb;

public record Event(Long version, String type, String payload) {

}
