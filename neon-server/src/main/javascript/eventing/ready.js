

/**
 * Stubs out OWF if it does not exist, and provides neon.ready()
 * which widgets should use before doing any OWF related work.
 * @class neon
 * @static
 */

if (typeof (OWF) === "undefined" || !OWF.Util.isRunningInOWF()) {
    window.OWF = {
        getIframeId: function () {
            return null;
        },
        getInstanceId: function () {
            return null;
        },
        ready: function(fnToExecute){
            fnToExecute();
        },
        Eventing: {
            publish: function () {
            },
            subscribe: function () {
            }
        }
    };
}

/**
 * Runs a function after the dom is loaded
 * @param functionToRun  the function to run.
 * @method ready
 */

neon.ready = function(functionToRun){
  $(function(){
     OWF.ready(functionToRun);
  });
};