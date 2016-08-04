/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.config

import com.google.common.reflect.ClassPath
//import com.ncc.neon.query.transform.GeoAggregationTransformer
import com.ncc.neon.query.result.Transformer
import com.ncc.neon.query.result.TransformerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Spring bean configuration to use in production
 */
@Configuration
@Profile("production")
class ProductionAppContext {

    /**
     * Uses this Class to get a resource folder from the classpath and return a Path to
     * that folder.
     * @return A Path object representing the transforms folder found on the classpath.
     */
    private Path getTransformsPath() {
        def pathString = this.getClass().getResource("/transforms").getPath()

        // Clean any leading slashes on windows paths with drive letters.
        if (System.getProperty("os.name").contains("indow")) {
            pathString = pathString.replaceFirst("/(\\w+):", "\$1:")
        }

        return FileSystems.getDefault().getPath(pathString)
    }

    @Bean
    TransformerRegistry transformerRegistry() {
        TransformerRegistry registry = new TransformerRegistry()
        List<Transformer> registeredTransformers = []
        ClassPath.from(ProductionAppContext.getClassLoader()).getTopLevelClasses("com.ncc.neon.query.transform").each {
            registeredTransformers << ProductionAppContext.getClassLoader().loadClass(it.getName()).newInstance()
        }

        def path = getTransformsPath()
        registeredTransformers.each { Transformer transformer ->
            registry.register(transformer)
        }

        TransformLoader loader = new TransformLoader(path, registry)
        Thread th = new Thread(loader, "TransformLoader")
        th.start()
        return registry
    }

}
