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
import com.ncc.neon.query.result.Transformer
import com.ncc.neon.query.result.TransformerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import java.nio.file.FileSystems

import groovy.lang.GroovyClassLoader

import groovy.io.FileType

/**
 * Spring bean configuration to use in production
 */
@Configuration
@Profile("production")
class ProductionAppContext {

	@Bean
	TransformerRegistry transformerRegistry() {
		TransformerRegistry registry = new TransformerRegistry()

		List<Transformer> registeredTransformers = []

		def path = FileSystems.getDefault().getPath(this.getClass().getResource("/transforms/").getPath());

		TransformLoader loader = new TransformLoader(path, registry);
		Thread th = new Thread(loader, "TransformLoader");
		th.start();

		registeredTransformers.each { Transformer transformer ->
			registry.register(transformer)
		}

		return registry
	}
}
