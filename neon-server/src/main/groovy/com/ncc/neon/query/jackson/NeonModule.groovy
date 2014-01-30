package com.ncc.neon.query.jackson
import org.bson.types.ObjectId
import org.codehaus.jackson.Version
import org.codehaus.jackson.map.module.SimpleModule


class NeonModule extends SimpleModule{

    NeonModule() {
        super("NeonModule", Version.unknownVersion())
        addSerializer(ObjectId, new ObjectIdSerializer())
        addSerializer(Date, new DateSerializer())
    }

}
