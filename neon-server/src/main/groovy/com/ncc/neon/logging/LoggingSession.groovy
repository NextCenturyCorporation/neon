package com.ncc.neon.logging
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
/*
 * Copyright 2014 Next Century Corporation
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

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class LoggingSession {

    private static final String DRAPER_SERVER_URL = "http://localhost:1337"

    private final LogData logData = register()

    LogData getLogData(){
        logData
    }

    private LogData register(){
        RESTClient restClient = new RESTClient("${DRAPER_SERVER_URL}/")
        HttpResponseDecorator resp = restClient.get(path: 'register')

        String ipAddress = resp.data.get("client_ip")
        String sessionId = resp.data.get("session_id")
        return new LogData(draperUrl: DRAPER_SERVER_URL, ipAddress: ipAddress, sessionId: sessionId)
    }

}
