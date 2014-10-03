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

package com.ncc.neon

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.sparksql.SparkSQLQueryExecutor
import com.ncc.neon.query.mongo.MongoQueryExecutor
import com.ncc.neon.query.transform.SalaryTransformer
import com.ncc.neon.query.result.Transformer
import com.ncc.neon.query.result.TransformerRegistry
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.SimpleThreadScope

@Configuration
@ComponentScan(basePackages = ['com.ncc.neon'])
class IntegrationTestContext {

    @Bean
    ConnectionManager connectionManagerBean(){
        return new ConnectionManager()
    }

    @Bean
    CustomScopeConfigurer scopeConfigurer() {
        return new CustomScopeConfigurer(scopes: ["session": new SimpleThreadScope(), "request": new SimpleThreadScope()])
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

    @Bean
    MongoQueryExecutor mongoQueryExecutor() {
        MongoQueryExecutor executor = new MongoQueryExecutor()
        executor.connectionManager = new ConnectionManager()
        return executor
    }

    @Bean
    SparkSQLQueryExecutor sparkSQLQueryExecutor() {
        SparkSQLQueryExecutor executor = new SparkSQLQueryExecutor()
        executor.connectionManager = new ConnectionManager()
        return executor
    }
    }
