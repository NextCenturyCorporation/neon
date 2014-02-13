neon.util.registerNewLogger = function (componentName) {
    var ac = new activityLogger();

    $.ajax({
            url: neon.query.SERVER_URL + '/services/loggingservice',
            async: false,
            success: function(data){
                ac.clientHostname = data.ipAddress;
                ac.componentName = componentName;
                ac.componentVersion = "1.0";
                ac.sessionID = data.sessionId;
                ac.url = data.draperUrl;
            }
        });
    return ac;
};
