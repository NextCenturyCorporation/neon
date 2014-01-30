package com.ncc.neon.mongo
import com.mongodb.MongoClient
import com.ncc.neon.config.MongoConfigParser
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.transform.SalaryTransformer
import com.ncc.neon.transform.Transformer
import com.ncc.neon.transform.TransformerRegistry
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.SimpleThreadScope


/**
 * Spring bean configuration to use for the mongo configuration test
 */
@Configuration
@ComponentScan(basePackages = ['com.ncc.neon'])
@Profile('mongo-integrationtest')
class MongoIntegrationTestContext {

    static final MongoClient MONGO

    static {
        def hostsString = System.getProperty("mongo.hosts", "localhost")
        def serverAddresses = MongoConfigParser.createServerAddresses(hostsString)
        MONGO = new MongoClient(serverAddresses)
    }

    @Bean
    ConnectionManager connectionManagerBean(){
        return new ConnectionManager()
    }

    @Bean
    MetadataConnection metadataConnectionBean(){
        return new MetadataConnection(MONGO)
    }

    @Bean
    CustomScopeConfigurer scopeConfigurer() {
        return new CustomScopeConfigurer(scopes: ["session": new SimpleThreadScope()])
    }

    @Bean
    TransformerRegistry transformerRegistry(){
        TransformerRegistry registry = new TransformerRegistry()
        List<Transformer> registeredTransformers = [new SalaryTransformer()]
        registeredTransformers.each { Transformer transformer ->
            registry.register(transformer)
        }
        return registry
    }
}
