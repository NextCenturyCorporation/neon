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

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.WARN


def layoutPattern = "%d{yyyy-MM-dd HH:mm:ss} %logger %-5p - %m%n"

// used for configuring appenders based on environment
def test = System.getProperty("unit.test") || System.getProperty("integration.test") || System.getProperty("acceptance.test")

// configure the log directory
def logDir = "./logs"
def logDirProperty = "log.dir"
if (System.getProperty(logDirProperty)) {
    logDir = System.getProperty(logDirProperty)
} else {
    System.err.println "WARNING: Could not find the ${logDirProperty} property, defaulting to ${logDir}"
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = layoutPattern
    }
}

// this appender is not used for testing and we don't want to even create the appender
// because it willl create a log file in the source tree
if (!test) {
    appender("FILE", RollingFileAppender) {
        file = "${logDir}/neon.log"
        rollingPolicy(FixedWindowRollingPolicy) {
            fileNamePattern = "${logDir}/neon%i.log"
            minIndex = 1
            maxIndex = 3
        }
        triggeringPolicy(SizeBasedTriggeringPolicy) {
            maxFileSize = "5MB"
        }
        encoder(PatternLayoutEncoder) {
            pattern = layoutPattern
        }
    }
}

logger("com.ncc.neon", INFO)  // set to DEBUG to turn on query logging

if (test) {
    root(WARN, ["CONSOLE"])
} else {
    root(WARN, ["CONSOLE", "FILE"])
}
