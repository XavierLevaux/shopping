package be.xl.shopping.persistence.cart.occurent;

import be.xl.architecture.eventsourcing.eventstore.StaleAggregateVersionException;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.shopping.domain.core.cart.entity.Cart;
import be.xl.shopping.domain.core.cart.entity.CartId;
import be.xl.shopping.domain.core.cart.event.CartCreated;
import be.xl.shopping.domain.core.cart.event.ProductAddedToCart;
import be.xl.shopping.domain.core.cart.event.ProductQuantityRemovedFromCart;
import be.xl.shopping.persistence.cart.EventStoreAbstractIntegrationTest;
import be.xl.shopping.persistence.cart.occurrent.OccurrentEventStore;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static be.xl.architecture.eventsourcing.model.Version.initialVersion;
import static be.xl.architecture.eventsourcing.model.Version.version;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;


@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OccurrentEventStoreIntegrationTest extends EventStoreAbstractIntegrationTest {

    protected final UUID cartId = UUID.randomUUID();
    protected final UUID customerId = UUID.randomUUID();
    protected final UUID productId = UUID.randomUUID();

    @Autowired
    private OccurrentEventStore eventStore;

    @Nested
    class Given_I_have_domain_events {

        protected DomainEvents<Cart, CartId> events;

        public Given_I_have_domain_events() {
            events = new DomainEvents<>(new CartId(cartId), initialVersion(), List.of(
                    new CartCreated(cartId, customerId, version(1)),
                    new ProductAddedToCart(cartId, customerId, productId, 5, version(2))
            ));
        }

        @Nested
        class When_I_save_a_new_aggregate {

            public When_I_save_a_new_aggregate() {
                eventStore.saveNewAggregate(new CartId(cartId), events);
            }

            @Test
            void then_I_can_load_those_events_from_the_store() {
                Optional<DomainEvents<Cart, CartId>> domainEvents = eventStore
                        .loadEvents(new CartId(cartId));

                assertThat(domainEvents).isNotEmpty();
                assertThat(domainEvents.get()).containsExactly(
                        new CartCreated(cartId, customerId, version(1)),
                        new ProductAddedToCart(cartId, customerId, productId, 5, version(2))
                );
            }
        }
    }

    @Nested
    class Given_I_have_domain_events_saved {

        public Given_I_have_domain_events_saved() {
            DomainEvents<Cart, CartId> events = new DomainEvents<>(
                    new CartId(cartId),
                    initialVersion(),
                    List.of(
                            new CartCreated(cartId, customerId, version(1)),
                            new ProductAddedToCart(cartId, customerId, productId, 5, version(2))
                    ));
            eventStore.saveNewAggregate(new CartId(cartId), events);
        }

        @Nested
        class When_I_update {

            public When_I_update() {
                DomainEvents<Cart, CartId> events = new DomainEvents<>(
                        new CartId(cartId),
                        version(2),
                        List.of(
                                new ProductQuantityRemovedFromCart(cartId, customerId, productId, 2,
                                        version(3))
                        ));
                eventStore.updateExistingAggregate(new CartId(cartId), events);
            }

            @Test
            void then_I_can_load_all_events() {
                Optional<DomainEvents<Cart, CartId>> domainEvents = eventStore
                        .loadEvents(new CartId(cartId));

                assertThat(domainEvents).isNotEmpty();

                DomainEvents<Cart, CartId> expectedDomainEvents = new DomainEvents<>(
                        new CartId(cartId),
                        version(0),
                        List.of(
                                new CartCreated(cartId, customerId, version(1)),
                                new ProductAddedToCart(cartId, customerId, productId, 5,
                                        version(2)),
                                new ProductQuantityRemovedFromCart(cartId, customerId, productId, 2,
                                        version(3))
                        ));

                assertThat(domainEvents.get()).isEqualTo(
                        expectedDomainEvents
                );
            }
        }

        @Nested
        class When_I_update_with_stale_version {

            private final DomainEvents<Cart, CartId> events;

            public When_I_update_with_stale_version() {
                events = new DomainEvents<>(
                        new CartId(cartId),
                        version(1),
                        List.of(
                                new ProductAddedToCart(cartId, customerId, UUID.randomUUID(), 5, version(2)))
                );
            }

            @Test
            void then_I_should_receive_an_error_saying_persisted_version_is_more_recent() {
                CartId aggregateId = new CartId(cartId);
                StaleAggregateVersionException exception = catchThrowableOfType(
                        () -> eventStore.updateExistingAggregate(
                                aggregateId,
                                events
                        ),
                        StaleAggregateVersionException.class
                );

                assertThat(exception.getAggregateVersion()).isEqualTo(2);
                assertThat(exception.getPresentedVersion()).isEqualTo(1);
                assertThat(exception.getAggregateName()).isEqualTo(aggregateId.getAggregateName());
            }

        }
    }
}