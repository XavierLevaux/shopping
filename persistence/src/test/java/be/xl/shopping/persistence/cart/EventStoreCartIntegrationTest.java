package be.xl.shopping.persistence.cart;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.occurrent.application.converter.CloudEventConverter;
import org.occurrent.application.converter.jackson.JacksonCloudEventConverter.Builder;
import org.occurrent.eventstore.api.blocking.EventStream;
import org.occurrent.eventstore.mongodb.spring.blocking.SpringMongoEventStore;
import org.occurrent.subscription.api.blocking.Subscription;
import org.occurrent.subscription.api.blocking.SubscriptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.xl.architecture.eventsourcing.model.Version.version;
import static io.vavr.API.unchecked;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EventStoreCartIntegrationTest extends EventStoreAbstractIntegrationTest {

    @Autowired
    private SpringMongoEventStore springMongoEventStore;

    @Autowired
    private SubscriptionModel subscriptionModel;
    private static final String CART_EVENT_STREAM = "CartEventStream";

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        objectMapper.setDateFormat(rfc3339);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private CloudEventConverter<DomainEvent<Cart>> buildCloudEventConverter() {
        URI cloudEventSource = URI.create("urn:be.xl:shopping:cart");

        return new Builder<DomainEvent<Cart>>(
                buildObjectMapper(),
                cloudEventSource)
                .timeMapper(o -> {
//             e.getDate().toInstant()
                    Instant instant = Instant.now();
                    return LocalDateTime.ofInstant(instant, UTC).atOffset(UTC)
                            .truncatedTo(ChronoUnit.MILLIS);
                })
                .build();

    }

    private List<CloudEvent> serialize(List<DomainEvent<Cart>> events) {
        ObjectMapper objectMapper = buildObjectMapper();
        return events.stream()
                .map(event ->
                                CloudEventBuilder.v1()
                                        .withId(UUID.randomUUID().toString()) //TODO: ?? aggregate Id in DomainEvent
                                        .withSource(URI.create("urn:be.xl:shopping:cart"))
                                        .withType(event.getClass().getCanonicalName())
                                        .withData(unchecked(objectMapper::writeValueAsBytes).apply(event))
//                                        .withTime(LocalDateTime.now().atOffset(UTC))
                                        .build()
                ).collect(Collectors.toList());
    }

    @Test
    void test2() {
        subscribe();

        UUID cartId1 = UUID.randomUUID();
        UUID customerId1 = UUID.randomUUID();
        String streamId1 = "cart-" + cartId1;

        List<DomainEvent<Cart>> domainEvents1 = List.of(
                new CartCreated(cartId1, customerId1, version(1)),
                new ProductAddedToCart(cartId1, customerId1, UUID.randomUUID(), 3, version(2))
        );

        UUID cartId2 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();
        String streamId2 = "cart-" + cartId2;

        List<DomainEvent<Cart>> domainEvents2 = List.of(
                new CartCreated(cartId2, customerId2, version(1)),
                new ProductAddedToCart(cartId2, customerId2, UUID.randomUUID(), 3, version(2)),
                new ProductAddedToCart(cartId2, customerId2, UUID.randomUUID(), 1, version(3))
        );

        storeDomainEvents(streamId1, domainEvents1);
        storeDomainEvents(streamId2, domainEvents2);
        Stream<DomainEvent<Cart>> retrievedDomainEvents1 = retrieveDomainEvents(streamId1);
        Stream<DomainEvent<Cart>> retrievedDomainEvents2 = retrieveDomainEvents(streamId2);
        assertThat(retrievedDomainEvents1).containsExactlyElementsOf(domainEvents1);
        assertThat(retrievedDomainEvents2).containsExactlyElementsOf(domainEvents2);

        cancelSubscription();
    }

    private void storeDomainEvents(String streamId, List<DomainEvent<Cart>> domainEvents) {
        storeCloudEvents(streamId, serialize(domainEvents));
    }

    private Stream<DomainEvent<Cart>> retrieveDomainEvents(String streamId) {
        EventStream<CloudEvent> read = springMongoEventStore.read(streamId, 0, 100);
        List<CloudEvent> cloudEvents = read.eventList();

        CloudEventConverter<DomainEvent<Cart>> domainEventCloudEventConverter = buildCartCloudEventConverter();

//        System.out.println("Read domain events from EventStore");
//        for (CloudEvent event : cloudEvents) {
//            DomainEvent<Cart> domainEvent = domainEventCloudEventConverter.toDomainEvent(event);
//            System.out.println("domain event: " + domainEvent);
//        }

        return cloudEvents.stream()
                .map(domainEventCloudEventConverter::toDomainEvent);
    }

    private void storeCloudEvents(String streamId, List<CloudEvent> cloudEvents) {
        try {
            transactionTemplate.execute((transactionStatus) -> {
                springMongoEventStore.write(streamId, cloudEvents.stream());
                return transactionStatus;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed doing transaction!", e);
        }
    }


    private CloudEventConverter<DomainEvent<Cart>> buildCartCloudEventConverter() {
        URI cloudEventSource = URI.create("urn:be.xl:shopping:foo");

        return new Builder<DomainEvent<Cart>>(buildObjectMapper(), cloudEventSource).build();

    }

    private void subscribe() {
        // Now you can create subscriptions instances that subscribes to new events as they're written to an EventStore
        // Typically you do this after the Spring application context has finished loading. For example by subscribing to
        // the  (org.springframework.boot.context.event.ApplicationStartedEvent) or in a method annotated with (@PostConstruct)
        System.out.println("Subscribe to events");
        Subscription subscription = subscriptionModel.subscribe("events",
                this::doSomethingWithTheCloudEvent);
        subscription.waitUntilStarted(Duration.of(2, ChronoUnit.SECONDS));
    }

    private void cancelSubscription() {
        // You can later cancel the subscription by calling:
        subscriptionModel.cancelSubscription("events");
    }

    private void doSomethingWithTheCloudEvent(CloudEvent cloudEvent) {
        CloudEventConverter<DomainEvent<Cart>> cloudEventConverter = buildCloudEventConverter();
        DomainEvent<Cart> domainEvent = cloudEventConverter.toDomainEvent(cloudEvent);
        System.out.println("Received event from subscription: " + domainEvent);
    }

}