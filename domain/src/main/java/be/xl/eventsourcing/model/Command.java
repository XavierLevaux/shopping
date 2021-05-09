package be.xl.eventsourcing.model;

import be.xl.architecture.Port;
import java.util.UUID;

@Port
public interface Command {
   UUID aggregateId();
}
