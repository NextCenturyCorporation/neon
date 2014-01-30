package com.ncc.neon.config
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.transform.Transformer
import com.ncc.neon.transform.TransformerRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource

import javax.annotation.PostConstruct


/**
 * Spring bean configuration to use in production
 */
@Configuration
@PropertySource("classpath:neon.properties")
@Profile("production")
class ProductionAppContext {

    @Autowired
    private Environment environment

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionAppContext)

    @PostConstruct
    @SuppressWarnings('JavaIoPackageAccess') // suppress to allow local file overrides
    def postConstruct() {
        LOGGER.trace("In post construct")
        // TODO: NEON-89 Currently dev and production both use "production" as the environment (since we don't really have a production environment yet), which is why the override checks the user's home dir. In a real production environment, the file is likely to be somewhere else (such as /opt or /etc).  We can abstract this on a per environment basis.
        def override = new File(System.getProperty("user.home"), "neon/neon-override.properties")
        if (override.exists()) {
            def properties = new Properties()
            properties.load(new FileInputStream(override))
            environment.propertySources.addFirst(new PropertiesPropertySource("overrides", properties))
        }
    }


    @Bean
    ConnectionManager connectionManagerBean(){
        ConnectionManager manager = new ConnectionManager()
        String host = System.getProperty("mongo.hosts", "localhost")
        manager.initConnectionManager(new ConnectionInfo(DataSources.mongo, host))
        return manager
    }

    @Bean
    MetadataConnection metadataConnectionBean(){
        ConnectionManager bean = connectionManagerBean()
        return new MetadataConnection(bean.defaultConnectionClient.getMongo())
    }

    @Bean
    TransformerRegistry transformerRegistry(){
        TransformerRegistry registry = new TransformerRegistry()
        List<Transformer> registeredTransformers = []
        registeredTransformers.each { Transformer transformer ->
            registry.register(transformer)
        }
        return registry
    }



}
