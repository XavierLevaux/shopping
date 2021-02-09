package be.xl.eventsourcing.model;

import java.util.UUID;

public interface Command {
   UUID aggregateId();
}
