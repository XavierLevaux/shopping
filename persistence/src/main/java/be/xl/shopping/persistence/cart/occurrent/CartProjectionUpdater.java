package be.xl.shopping.persistence.cart.occurrent;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.SECONDS;

import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.shopping.domain.core.cart.entity.Cart;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.annotation.PostConstruct;

import org.occurrent.application.converter.CloudEventConverter;
import org.occurrent.application.converter.jackson.JacksonCloudEventConverter.Builder;
import org.occurrent.subscription.api.blocking.SubscriptionModel;
import org.springframework.stereotype.Component;

@Component
public class CartProjectionUpdater {

    private final SubscriptionModel subscription;
    private final CloudEventConverter<DomainEvent<Cart>> domainEventCloudEventConverter;
    //   private final CurrentNameProjection currentNameProjection;
//   private final DeserializeCloudEventToDomainEvent deserializeCloudEventToDomainEvent;

    public CartProjectionUpdater(SubscriptionModel subscription
//       CurrentNameProjection currentNameProjection,
//       DeserializeCloudEventToDomainEvent deserializeCloudEventToDomainEvent
    ) {
        this.subscription = subscription;
//      this.currentNameProjection = currentNameProjection;
//      this.deserializeCloudEventToDomainEvent = deserializeCloudEventToDomainEvent;

        domainEventCloudEventConverter = buildCloudEventConverter();
    }

    @PostConstruct
    void startProjectionUpdater() {
        subscription
                .subscribe("current-name", cloudEvent -> {
//             DomainEvent domainEvent = deserializeCloudEventToDomainEvent.deserialize(cloudEvent);
                    DomainEvent<Cart> domainEvent = domainEventCloudEventConverter.toDomainEvent(cloudEvent);
                    System.out.println("update projection with event: " + domainEvent);
//             String streamId = getStreamId(cloudEvent);
//             CurrentName currentName = Match(domainEvent).of(
//                 Case($(instanceOf(NameDefined.class)),
//                     e -> new CurrentName(streamId, e.getName())),
//                 Case($(instanceOf(NameWasChanged.class)),
//                     e -> new CurrentName(streamId, e.getName())));
//             currentNameProjection.save(currentName);
                })
                .waitUntilStarted(Duration.of(2, SECONDS));
    }

    private CloudEventConverter<DomainEvent<Cart>> buildCloudEventConverter() {
        URI cloudEventSource = URI.create("urn:be.xl:cart");

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

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        objectMapper.setDateFormat(dateFormat);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}