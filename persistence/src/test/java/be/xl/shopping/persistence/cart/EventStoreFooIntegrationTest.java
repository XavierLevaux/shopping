package be.xl.shopping.persistence.cart;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.occurrent.application.converter.CloudEventConverter;
import org.occurrent.application.converter.jackson.JacksonCloudEventConverter.Builder;
import org.occurrent.eventstore.api.blocking.EventStream;
import org.occurrent.eventstore.mongodb.spring.blocking.SpringMongoEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EventStoreFooIntegrationTest extends EventStoreAbstractIntegrationTest {

    protected final UUID cartId = UUID.randomUUID();

    @Autowired
    private SpringMongoEventStore springMongoEventStore;

    @Test
    void test() {
        CloudEventData cloudEventData = PojoCloudEventData.wrap("{\n"
                + "   \"name\":\"john\"\n"
                + "}", String::getBytes);
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("urn:be.xl:shopping:foo"))
                .withType(Foo.class.getCanonicalName())
                .withData(cloudEventData)
                .build();
        springMongoEventStore.write(cartId.toString(), List.of(
                cloudEvent
        ).stream());
        EventStream<CloudEvent> read = springMongoEventStore.read(cartId.toString(), 0, 100);
        List<CloudEvent> cloudEvents = read.eventList();


        CloudEventConverter<Foo> cloudEventConverter = buildFooCloudEventConverter();

        for (CloudEvent event : cloudEvents) {
            Foo foo = cloudEventConverter.toDomainEvent(event);
            System.out.println(foo);
        }
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        objectMapper.setDateFormat(rfc3339);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private CloudEventConverter<Foo> buildFooCloudEventConverter() {
        URI cloudEventSource = URI.create("urn:be.xl:shopping:cart");

        return new Builder<Foo>(buildObjectMapper(), cloudEventSource).build();

    }
}