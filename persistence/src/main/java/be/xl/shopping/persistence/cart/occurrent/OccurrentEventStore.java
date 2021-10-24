package be.xl.shopping.persistence.cart.occurrent;

import be.xl.architecture.Adapter;
import be.xl.architecture.eventsourcing.eventstore.EventStore;
import be.xl.architecture.eventsourcing.eventstore.StaleAggregateVersionException;
import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.occurrent.application.converter.CloudEventConverter;
import org.occurrent.application.converter.jackson.JacksonCloudEventConverter;
import org.occurrent.eventstore.api.WriteConditionNotFulfilledException;
import org.occurrent.eventstore.api.blocking.EventStream;
import org.occurrent.eventstore.mongodb.spring.blocking.SpringMongoEventStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.API.unchecked;

@Adapter
@Service
@Transactional(readOnly = true)
public class OccurrentEventStore implements EventStore<Cart, CartId> {
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
    public Optional<DomainEvents<Cart, CartId>> loadEvents(CartId aggregateId) {
        Stream<DomainEvent<Cart>> retrievedDomainEvents = retrieveDomainEvents(getStreamId(aggregateId));
        List<DomainEvent<Cart>> domainEvents = retrievedDomainEvents.toList();
        return domainEvents.isEmpty() ? Optional.empty() :
                Optional.of(new DomainEvents<>(aggregateId, Version.initialVersion(), domainEvents));
    }

    private String getStreamId(CartId aggregateId) {
        return aggregateId.getAggregateName() + aggregateId.id();
    }

    private Stream<DomainEvent<Cart>> retrieveDomainEvents(String streamId) {
        EventStream<CloudEvent> read = springMongoEventStore.read(streamId, 0, 100);
        List<CloudEvent> cloudEvents = read.eventList();

        CloudEventConverter<DomainEvent<Cart>> domainEventCloudEventConverter = buildCartCloudEventConverter();

        return cloudEvents.stream()
                .map(domainEventCloudEventConverter::toDomainEvent);
    }

    private CloudEventConverter<DomainEvent<Cart>> buildCartCloudEventConverter() {
        URI cloudEventSource = URI.create("urn:be.xl:shopping:foo");

        return new JacksonCloudEventConverter.Builder<DomainEvent<Cart>>(eventObjectMapper, cloudEventSource).build();
    }

    @Override
    @Transactional
    public void saveNewAggregate(CartId aggregateId, DomainEvents<Cart, CartId> events) {
        String streamId = aggregateId.getAggregateName() + aggregateId.id();

        storeCloudEvents(streamId, serialize(events.events()));
    }

    private void storeCloudEvents(String streamId, List<CloudEvent> cloudEvents) {
        springMongoEventStore.write(streamId, cloudEvents.stream());
    }

    private List<CloudEvent> serialize(List<DomainEvent<Cart>> events) {
        return events.stream()
                .map(event ->
                                CloudEventBuilder.v1()
                                        .withId(UUID.randomUUID().toString()) //TODO: ?? aggregate Id in DomainEvent
                                        .withSource(URI.create("urn:be.xl:shopping:cart"))
                                        .withType(event.getClass().getCanonicalName())
                                        .withData(unchecked(eventObjectMapper::writeValueAsBytes).apply(event))
//                                        .withTime(LocalDateTime.now().atOffset(UTC))
                                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateExistingAggregate(CartId aggregateId, DomainEvents<Cart, CartId> events) throws StaleAggregateVersionException {
        try {
            springMongoEventStore.write(
                    getStreamId(aggregateId),
                    events.fromAggregateVersion().version(),
                    serialize(events.events()).stream());
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
