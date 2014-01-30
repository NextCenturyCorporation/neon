package com.ncc.neon.query.jackson
import com.ncc.neon.util.DateUtils
import org.codehaus.jackson.JsonGenerator
import org.codehaus.jackson.map.JsonSerializer
import org.codehaus.jackson.map.SerializerProvider


class DateSerializer extends JsonSerializer<Date> {

    @Override
    void serialize(Date value, JsonGenerator generator, SerializerProvider provider){
        generator.writeString(DateUtils.dateTimeToString(value))
    }
}