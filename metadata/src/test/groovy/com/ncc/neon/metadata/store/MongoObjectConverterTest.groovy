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

package com.ncc.neon.metadata.store

import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata
import org.junit.Test


class MongoObjectConverterTest {

    private final WidgetInitializationMetadata data = new WidgetInitializationMetadata(widgetName: "widget", initDataJson: '{"hello": "world"}')

    @Test
    void testConversion() {
        MongoObjectConverter converter = new MongoObjectConverter()
        def document = converter.convertToMongo(data)
        def obj = converter.convertToObject(document)

        assert obj.widgetName == data.widgetName
        assert obj.initDataJson == data.initDataJson
    }

}
