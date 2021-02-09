package be.xl.eventsourcing.model;

public interface DomainEvent<T> {

   String getType();

   default boolean isType(String type) {
         return getType().equals(type);
   }

   Long getVersion();
}
