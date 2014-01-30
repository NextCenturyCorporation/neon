package com.ncc.neon.query.jackson
import org.bson.types.ObjectId
import org.codehaus.jackson.JsonGenerator
import org.codehaus.jackson.map.JsonSerializer
import org.codehaus.jackson.map.SerializerProvider


class ObjectIdSerializer extends JsonSerializer<ObjectId> {

    @Override
    void serialize(ObjectId value, JsonGenerator generator, SerializerProvider provider){
        generator.writeString(value.toString())
    }
}
