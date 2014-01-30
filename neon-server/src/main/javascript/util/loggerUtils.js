

/**
 * Provides utility methods for working with a log4javascript logger
 * @class neon.util,loggerUtils
 * @static
 */

neon.util.loggerUtils = (function(){
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
    function usePopupAppender(logger){
        logger.addAppender(popupAppender);
    }

    /**
     * Sets whether or not the browser console appender should be used for the logger
     * @method useBrowserConsoleAppender
     * @param {Object} logger The log4javascript logger from which to attach/detach the popup appender
     */
    function useBrowserConsoleAppender(logger){
        logger.addAppender(browserConsoleAppender);
    }

    /**
     * Gets a global logger (with no name) that can be used by different classes. Any configuration changes to the
     * global logger will affect all usages of it.
     * @method getGlobalLogger
     * @return {Object} The global logger
     */
    function getGlobablLogger(){
        return log4javascript.getLogger("[global]");
    }

    /**
     * Creates a log4javascript logger
     * @method getLogger
     * @param {String} name The name of the logger
     * @return {Object} a log4javascript logger
     *
     */
    function getLogger(name){
        return log4javascript.getLogger(name);
    }

    return {
        usePopupAppender: usePopupAppender,
        useBrowserConsoleAppender: useBrowserConsoleAppender,
        getGlobalLogger:getGlobablLogger,
        getLogger: getLogger
    };

})();
