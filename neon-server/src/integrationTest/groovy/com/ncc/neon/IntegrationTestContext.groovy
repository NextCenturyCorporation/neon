package com.ncc.neon
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.mongo.MongoIntegrationTestContext
import com.ncc.neon.transform.TransformerRegistry
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.SimpleThreadScope


@Configuration
@ComponentScan(basePackages = ['com.ncc.neon'])
@Profile('integrationtest')
class IntegrationTestContext {

    @Bean
    ConnectionManager connectionManagerBean(){
        return new ConnectionManager()
    }

    @Bean
    MetadataConnection metadataConnection(){
        return new MetadataConnection(MongoIntegrationTestContext.MONGO)
    }

    @Bean
    CustomScopeConfigurer scopeConfigurer() {
        return new CustomScopeConfigurer(scopes: ["session":new SimpleThreadScope()])
    }

    @Bean
    TransformerRegistry transformerRegistry(){
        new TransformerRegistry()
    }

}
