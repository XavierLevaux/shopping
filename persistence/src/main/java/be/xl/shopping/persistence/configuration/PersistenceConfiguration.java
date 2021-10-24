package be.xl.shopping.persistence.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
@ComponentScan(basePackages = {"be.xl.shopping.persistence"})
public class PersistenceConfiguration extends AbstractMongoClientConfiguration {

   @Value("${spring.mongo.host}")
   private String mongoHost;

   @Value("${spring.mongo.port}")
   private Integer mongoPort;

   @Value("${spring.mongo.databaseName}")
   private String databaseName;

   @Override
   public com.mongodb.client.MongoClient mongoClient() {
      ConnectionString connectionString = new ConnectionString(mongodbConnectionString());
      MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
          .applyConnectionString(connectionString)
          .build();

      return MongoClients.create(mongoClientSettings);
   }

   private String mongodbConnectionString() {
      return String.format("mongodb://%s:%d/%s", mongoHost, mongoPort, databaseName);
   }

   @Override
   protected String getDatabaseName() {
      return databaseName;
   }
}
