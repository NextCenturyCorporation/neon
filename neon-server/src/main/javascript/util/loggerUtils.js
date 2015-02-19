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

/**
 * Provides utility methods for working with a log4javascript logger
 * @class neon.util.loggerUtils
 * @static
 */

neon.util.loggerUtils = (function() {
    var popupAppender = new log4javascript.PopUpAppender();
    var browserConsoleAppender = new log4javascript.BrowserConsoleAppender();
    var layout = new log4javascript.PatternLayout("%d{HH:mm:ss} %-5p - %m%n");

    popupAppender.setLayout(layout);
    browserConsoleAppender.setThreshold(log4javascript.Level.ALL);

    /**
     * Sets whether or not the popup appender should be used for the logger
     * @method usePopupAppender
     * @param {Object} logger The log4javascript logger from which to attach/detach the popup appender
     */
    function usePopupAppender(logger) {
        logger.addAppender(popupAppender);
    }

    /**
     * Sets whether or not the browser console appender should be used for the logger
     * @method useBrowserConsoleAppender
     * @param {Object} logger The log4javascript logger from which to attach/detach the popup appender
     */
    function useBrowserConsoleAppender(logger) {
        logger.addAppender(browserConsoleAppender);
    }

    /**
     * Gets a global logger (with no name) that can be used by different classes. Any configuration changes to the
     * global logger will affect all usages of it.
     * @method getGlobalLogger
     * @return {Object} The global logger
     */
    function getGlobablLogger() {
        return log4javascript.getLogger("[global]");
    }

    /**
     * Creates a log4javascript logger
     * @method getLogger
     * @param {String} name The name of the logger
     * @return {Object} a log4javascript logger
     *
     */
    function getLogger(name) {
        return log4javascript.getLogger(name);
    }

    return {
        usePopupAppender: usePopupAppender,
        useBrowserConsoleAppender: useBrowserConsoleAppender,
        getGlobalLogger: getGlobablLogger,
        getLogger: getLogger
    };
})();
