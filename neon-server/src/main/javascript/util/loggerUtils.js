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
