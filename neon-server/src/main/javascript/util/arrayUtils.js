
/**
 * Utility methods for working with arrays
 * @namespace neon.util
 * @class arrayUtils
 */

neon.util.arrayUtils = {

    /**
     * Converts the javascript *arguments* to an array.
     * @method argumentsToArray
     * @param args The *arguments* variable from another function to convert to an array
     * @return {Array}
     */
    argumentsToArray: function(args) {
        return Array.prototype.slice.call(args);
    }
};