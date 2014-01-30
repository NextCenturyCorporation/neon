package com.ncc.neon.services

import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata
import com.ncc.neon.result.MetadataResolver
import com.ncc.neon.state.WidgetStates
import org.junit.Before
import org.junit.Test




class WidgetStateServiceTest {

    private WidgetStateService service

    @Before
    void setup(){
        service = new WidgetStateService()
        service.widgetStates = new WidgetStates()
    }

    @Test
    void "add and restore widget state"() {
        service.saveState("id", "state")
        assert service.restoreState("id") == "state"
    }

    @Test
    void "restore widget state that doesn't exist"() {
        assert !service.restoreState("id")
    }

    @Test
    void "object is not found in metadata store"() {
        def resolver = [getWidgetInitializationData : {
            widgetName -> new WidgetInitializationMetadata(widgetName: widgetName)
        }] as MetadataResolver
        service.metadataResolver = resolver
        assert !service.getWidgetInitialization("widget")
    }

    @Test
    void "object is found in metadata store"() {
        String data = "data"
        def resolver = [getWidgetInitializationData : {
            widgetName -> new WidgetInitializationMetadata(widgetName: widgetName, initDataJson: data)
        }] as MetadataResolver
        service.metadataResolver = resolver
        assert service.getWidgetInitialization("widget") == data
    }
}
