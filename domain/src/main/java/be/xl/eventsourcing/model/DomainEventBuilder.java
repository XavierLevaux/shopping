package be.xl.eventsourcing.model;

public interface DomainEventBuilder<T> {
   DomainEvent<T> build(Long version);
}
