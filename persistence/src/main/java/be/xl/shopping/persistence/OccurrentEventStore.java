package be.xl.shopping.persistence;

import be.xl.architecture.Adapter;
import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.eventstore.StaleAggregateVersionException;
import be.xl.architecture.eventsourcing.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.jmolecules.ddd.types.Identifier;
import org.occurrent.application.converter.CloudEventConverter;
import org.occurrent.application.converter.jackson.JacksonCloudEventConverter;
import org.occurrent.eventstore.api.WriteConditionNotFulfilledException;
import org.occurrent.eventstore.api.blocking.EventStream;
import org.occurrent.eventstore.mongodb.spring.blocking.SpringMongoEventStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.API.unchecked;
import static java.time.ZoneOffset.UTC;

@Adapter
@Service
@Transactional(readOnly = true)
public abstract class OccurrentEventStore<
        AGGREGATE extends EventSourcedAggregateRoot<AGGREGATE, ID>,
        ID extends Identifier & AggregateIdentifier
        >
        implements EventStore<AGGREGATE, ID> {
    private final SpringMongoEventStore springMongoEventStore;
    protected final PlatformTransactionManager transactionManager;
    private final ObjectMapper eventObjectMapper;

    public OccurrentEventStore(SpringMongoEventStore springMongoEventStore,
                               PlatformTransactionManager transactionManager,
                               ObjectMapper eventObjectMapper) {
        this.springMongoEventStore = springMongoEventStore;
        this.transactionManager = transactionManager;
        this.eventObjectMapper = eventObjectMapper;
    }

    @Override
    public Optional<DomainEvents<AGGREGATE, ID>> loadEvents(ID aggregateId) {
        Stream<DomainEvent<AGGREGATE>> retrievedDomainEvents = retrieveDomainEvents(aggregateId);
        List<DomainEvent<AGGREGATE>> domainEvents = retrievedDomainEvents.toList();
        return domainEvents.isEmpty() ? Optional.empty() :
                Optional.of(new DomainEvents<>(aggregateId, Version.initialVersion(), domainEvents));
    }

    private String getStreamId(ID aggregateId) {
        return aggregateId.getAggregateName() + aggregateId.id();
    }

    private URI getCloudEventSource(ID aggregateId) {
        return URI.create("urn:%s:%s%s".formatted(
                OccurrentEventStore.class.getPackageName(),
                aggregateId.getAggregateName(),
                aggregateId.id()
        ));
    }

    private Stream<DomainEvent<AGGREGATE>> retrieveDomainEvents(ID aggregateId) {
        EventStream<CloudEvent> read = springMongoEventStore.read(getStreamId(aggregateId), 0, 100);
        List<CloudEvent> cloudEvents = read.eventList();

        CloudEventConverter<DomainEvent<AGGREGATE>> domainEventCloudEventConverter =
                buildCartCloudEventConverter(getCloudEventSource(aggregateId));

        return cloudEvents.stream().map(domainEventCloudEventConverter::toDomainEvent);
    }

    private CloudEventConverter<DomainEvent<AGGREGATE>> buildCartCloudEventConverter(URI cloudEventSource) {
        return new JacksonCloudEventConverter.Builder<DomainEvent<AGGREGATE>>(eventObjectMapper, cloudEventSource).build();
    }

    @Override
    @Transactional
    public void saveNewAggregate(ID aggregateId, DomainEvents<AGGREGATE, ID> events) {
        storeCloudEvents(aggregateId, serialize(aggregateId, events.events()));
    }

    private void storeCloudEvents(ID aggregateId, List<CloudEvent> cloudEvents) {
        springMongoEventStore.write(getStreamId(aggregateId), cloudEvents.stream());
    }

    private List<CloudEvent> serialize(ID aggregateId, List<DomainEvent<AGGREGATE>> events) {
        return events
                .stream()
                .map(event ->
                        CloudEventBuilder.v1()
                                .withId(UUID.randomUUID().toString()) //TODO: Decide on DomainEvent having an ID
                                .withSource(getCloudEventSource(aggregateId))
                                .withType(event.getClass().getCanonicalName())
                                .withData(unchecked(eventObjectMapper::writeValueAsBytes).apply(event))
                                .withTime(LocalDateTime.now().atOffset(UTC).truncatedTo(ChronoUnit.MILLIS)) //TODO: Decide on DomainEvent having a time
                                .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateExistingAggregate(ID aggregateId, DomainEvents<AGGREGATE, ID> events) throws StaleAggregateVersionException {
        try {
            springMongoEventStore.write(
                    getStreamId(aggregateId),
                    events.fromAggregateVersion().version(),
                    serialize(aggregateId, events.events()).stream()
            );
        } catch (WriteConditionNotFulfilledException e) {
            //TODO improve with check on version
            throw new StaleAggregateVersionException(
                    aggregateId.getAggregateName(),
                    Version.version(e.eventStreamVersion),
                    events.fromAggregateVersion()
            );
        }
    }
}
