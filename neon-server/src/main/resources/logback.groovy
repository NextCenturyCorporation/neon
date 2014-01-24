import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.WARN
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

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

logger("com.ncc.neon", WARN)  // set to DEBUG to turn on query logging

if (test) {
    root(WARN, ["CONSOLE"])
} else {
    root(WARN, ["CONSOLE", "FILE"])
}
