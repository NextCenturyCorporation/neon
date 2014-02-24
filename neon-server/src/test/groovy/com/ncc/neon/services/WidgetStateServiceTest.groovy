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

package com.ncc.neon.services

import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata
import com.ncc.neon.result.MetadataResolver
import com.ncc.neon.state.WidgetState
import com.ncc.neon.state.WidgetStates
import org.junit.Before
import org.junit.Test




class WidgetStateServiceTest {

    private WidgetStateService service

    @Before
    void setup() {
        service = new WidgetStateService()
        service.widgetStates = new WidgetStates()
    }

    @Test
    void "add and restore widget state"() {
        service.saveState(new WidgetState(clientId: "id", state: "state"))
        assert service.restoreState("id") == "state"
    }

    @Test
    void "restore widget state that doesn't exist"() {
        assert !service.restoreState("id")
    }

    @Test
    void "object is not found in metadata store"() {
        def resolver = [getWidgetInitializationData: {
            widgetName -> new WidgetInitializationMetadata(widgetName: widgetName)
        }] as MetadataResolver
        service.metadataResolver = resolver
        assert !service.getWidgetInitialization("widget")
    }

    @Test
    void "object is found in metadata store"() {
        String data = "data"
        def resolver = [getWidgetInitializationData: {
            widgetName -> new WidgetInitializationMetadata(widgetName: widgetName, initDataJson: data)
        }] as MetadataResolver
        service.metadataResolver = resolver
        assert service.getWidgetInitialization("widget") == data
    }
}
