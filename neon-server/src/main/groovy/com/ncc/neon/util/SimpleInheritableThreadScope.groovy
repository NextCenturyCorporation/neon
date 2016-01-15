/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ncc.neon.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.core.NamedThreadLocal

/**
 * A simple thread-backed {@link Scope} implementation.
 *
 * <p><strong>Note:</strong> {@code SimpleThreadScope} <em>does not clean up
 * any objects</em> associated with it. As such, it is typically preferable to
 * use {@link org.springframework.web.context.request.RequestScope RequestScope}
 * in web environments.
 *
 * <p>For an implementation of a thread-based {@code Scope} with support for
 * destruction callbacks, refer to the
 * <a href="http://www.springbyexample.org/examples/custom-thread-scope-module.html">
*  Spring by Example Custom Thread Scope Module</a>.
 *
 * <p>Thanks to Eugene Kuleshov for submitting the original prototype for a thread scope!
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.web.context.request.RequestScope
 */
public class SimpleInheritableThreadScope implements Scope {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInheritableThreadScope)

	private static final ThreadLocal<Map<String, Object>> THREADSCOPE =
			new NamedThreadLocal<Map<String, Object>>("SimpleThreadScope") {
				@Override
				protected Map<String, Object> initialValue() {
					return [:]
				}
			}


	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> scope = this.THREADSCOPE.get()
		Object object = scope.get(name)
		if (object == null) {
			object = objectFactory.getObject()
			scope.put(name, object)
		}
		return object
	}

	public Object remove(String name) {
		Map<String, Object> scope = this.THREADSCOPE.get()
		return scope.remove(name)
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		LOGGER.debug("Registration of destruction callback {$name , $callback} not supported.")
	}

	public Object resolveContextualObject(String key) {
		LOGGER.debug("This method is a stub. Contextual object $key not registered.")
		return null
	}

	public String getConversationId() {
		return Thread.currentThread().getName()
	}

}