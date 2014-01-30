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
