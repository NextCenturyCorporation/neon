package com.ncc.neon.query.jackson

import org.codehaus.jackson.map.ObjectMapper
import org.springframework.stereotype.Component

import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider



@Component
@Provider
class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.registerModule(new NeonModule())
        return objectMapper
    }
}