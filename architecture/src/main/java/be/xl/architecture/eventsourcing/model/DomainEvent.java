package be.xl.architecture.eventsourcing.model;

public interface DomainEvent<T> extends Versioned {

   String getType();

   default boolean isType(String type) {
         return getType().equals(type);
   }
}
