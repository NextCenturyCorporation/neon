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
package com.vividsolutions.jts.geom

/**
 * This is a mock object to avoid run-time issues with ElasticSearch features that are not used
 * by Neon.  The optional features use the Vivid Solutions JTS library which is not compatible
 * with Apache 2.0.  Neon uses an ElasticSearch Query Builder that references JTS classes but does
 * not require them.  In a pure Java system, this is not an issue.  Howevever, Neon utilizes Groovy
 * code.  The Groovy runtime checks for the existence of all directly reference classes
 * regardless of whether or not they are required by our runtime methods.
 *
 * For more information, please reference the following ElasticSearch Issues,
 * - https://github.com/elastic/elasticsearch/issues/9891
 * - https://github.com/elastic/elasticsearch/issues/13397
 *
 * This class will be removed when a cleaner solution is found or ElasticSearch releases
 * an update that replaces the optional JTS library with Apache SIS.
 */
@SuppressWarnings('EmptyClass')
public class Geometry {
}