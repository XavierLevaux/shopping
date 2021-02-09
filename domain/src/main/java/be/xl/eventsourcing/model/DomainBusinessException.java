package be.xl.eventsourcing.model;

import java.util.UUID;

public class DomainBusinessException extends RuntimeException {
   public final UUID aggregateId;
   public final String message;

   public DomainBusinessException(UUID aggregateId, String message) {
      super(message);
      this.aggregateId = aggregateId;
      this.message = message;
   }
}
