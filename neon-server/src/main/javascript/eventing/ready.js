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

neon.ready = function(fnToRun){
  $(function(){
     OWF.ready(fnToRun);
  });
};