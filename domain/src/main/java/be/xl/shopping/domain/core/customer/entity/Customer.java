package be.xl.shopping.domain.core.customer.entity;

import static be.xl.architecture.eventsourcing.model.DomainEventVersionGenerator.domainEventVersionGenerator;
import static io.vavr.collection.List.ofAll;

import be.xl.architecture.eventsourcing.eventstore.EventStream;
import be.xl.architecture.eventsourcing.model.DomainBusinessException;
import be.xl.architecture.eventsourcing.model.DomainEvent;
import be.xl.architecture.eventsourcing.model.DomainEventVersionGenerator;
import be.xl.architecture.eventsourcing.model.DomainEvents;
import be.xl.architecture.eventsourcing.model.EventSourcedAggregateRoot;
import be.xl.architecture.eventsourcing.model.Version;
import be.xl.architecture.eventsourcing.model.Versioned;
import be.xl.shopping.domain.core.customer.event.AddressChanged;
import be.xl.shopping.domain.core.customer.event.AddressDefined;
import be.xl.shopping.domain.core.customer.event.CustomerEmailAddressConfirmed;
import be.xl.shopping.domain.core.customer.event.CustomerRegistered;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Customer implements EventSourcedAggregateRoot<Customer, CustomerId>, Versioned {

   private final CustomerId customerId;
   private Instant affiliationDate;
   private String firstName;
   private String lastName;
   private EmailAddress emailAddress;
   private boolean emailAddressVerified = false;
   public final Version version;
   private Address address;

   private Customer(CustomerId customerId, Version version) {
      this.customerId = customerId;
      this.version = version;
   }

   private Customer(CustomerId customerId,
       String firstName,
       String lastName,
       EmailAddress emailAddress,
       boolean emailAddressVerified,
       Instant affiliationDate,
       Version version) {
      this(customerId, version);
      this.firstName = firstName;
      this.lastName = lastName;
      this.emailAddress = emailAddress;
      this.emailAddressVerified = emailAddressVerified;
      this.affiliationDate = affiliationDate;
   }

   private Customer(CustomerId customerId,
       String firstName,
       String lastName,
       EmailAddress emailAddress,
       boolean emailAddressVerified,
       Instant affiliationDate,
       Address address,
       Version version) {
      this(customerId, firstName, lastName, emailAddress, emailAddressVerified, affiliationDate,
          version);
      this.address = address;
   }

   public static DomainEvents<Customer, CustomerId> registerCustomer(CustomerId customerId,
       String firstName, String lastName, EmailAddress emailAddress,
       Instant affiliationDate) {
      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(
          Version.initialVersion());
      return new DomainEvents<>(customerId, versionGenerator.getAggregateVersion(),
          List.of(
              new CustomerRegistered(customerId.id(), firstName, lastName, emailAddress.value(),
                  affiliationDate,
                  versionGenerator.nextVersion())
          ));
   }

   public DomainEvents<Customer, CustomerId> defineAddress(
       String street,
       String streetNumber,
       String city,
       String postCode,
       String countryName) {
      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(version);

      List<DomainEvent<Customer>> events = hasAddress() ? List.of(
          new AddressChanged(customerId.id(),
              street,
              streetNumber,
              postCode,
              city,
              countryName,
              versionGenerator.nextVersion()
          )
      ) : List.of(
          new AddressDefined(customerId.id(),
              street,
              streetNumber,
              postCode,
              city,
              countryName,
              versionGenerator.nextVersion()
          )
      );
      return new DomainEvents<>(customerId, versionGenerator.getAggregateVersion(), events);

   }

   public DomainEvents<Customer, CustomerId> confirmEmailAddress(EmailAddress emailAddress) {
      if (!this.emailAddress.equals(emailAddress)) {
         throw new DomainBusinessException(customerId.id(),
             "We cannot confirm another email than the one defined for a customer!");
      }

      if (isEmailAddressVerified()) {
         throw new DomainBusinessException(customerId.id(),
             "The customer email is already confirmed!");
      }

      DomainEventVersionGenerator versionGenerator = domainEventVersionGenerator(version);
      return new DomainEvents<>(customerId, versionGenerator.getAggregateVersion(),
          List.of(
              new CustomerEmailAddressConfirmed(customerId.id(), emailAddress.value(),
                  versionGenerator.nextVersion())
          ));

   }

   public static Customer reHydrate(EventStream<Customer, CustomerId> eventStream) {
      return ofAll(eventStream).foldLeft(
          new Customer(eventStream.getAggregateId(), Version.initialVersion()), Customer::apply);
   }

   private Customer apply(DomainEvent<Customer> domainEvent) {
      if (domainEvent instanceof CustomerRegistered) {
         return apply((CustomerRegistered) domainEvent);
      } else if (domainEvent instanceof AddressDefined) {
         return apply((AddressDefined) domainEvent);
      } else if (domainEvent instanceof AddressChanged) {
         return apply((AddressChanged) domainEvent);
      } else if (domainEvent instanceof CustomerEmailAddressConfirmed) {
         return apply((CustomerEmailAddressConfirmed) domainEvent);
      } else {
         throw new IllegalArgumentException(
             String.format("Not supported event %s", domainEvent.toString()));
      }
   }

   private Customer apply(CustomerRegistered event) {
      return new Customer(new CustomerId(event.customerId()), event.firstName(),
          event.lastName(), new EmailAddress(event.emailAddress()), false,
          event.affiliationDate(), event.getVersion());
   }

   private Customer apply(CustomerEmailAddressConfirmed event) {
      return new Customer(customerId, firstName,
          lastName, emailAddress, true,
          affiliationDate, event.getVersion());
   }

   private Customer apply(AddressDefined event) {
      return new Customer(new CustomerId(event.customerId()), this.firstName, lastName,
          emailAddress, emailAddressVerified,
          affiliationDate,
          new Address(event.street(), event.streetNumber(), event.city(), event.postCode(),
              event.countryName()), event.version());
   }

   private Customer apply(AddressChanged event) {
      return new Customer(new CustomerId(event.customerId()), firstName, lastName,
          emailAddress, emailAddressVerified,
          affiliationDate,
          new Address(event.street(), event.streetNumber(), event.city(), event.postCode(),
              event.countryName()), event.version());
   }

   @Override
   public CustomerId getId() {
      return customerId;
   }

   public Instant getAffiliationDate() {
      return affiliationDate;
   }

   public String getFirstName() {
      return firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public EmailAddress getEmailAddress() {
      return emailAddress;
   }

   @Override
   public Version getVersion() {
      return version;
   }

   public Optional<Address> getAddress() {
      return Optional.ofNullable(address);
   }

   private boolean hasAddress() {
      return getAddress().isPresent();
   }

   public boolean isEmailAddressVerified() {
      return emailAddressVerified;
   }
}
