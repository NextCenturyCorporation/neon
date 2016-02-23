function clearCache() {
    $.get("services/adminservice/mongo/clearquerycache");
}

function setCache() {
    var cacheQueries = $('input[name="cacheQueries"][value="on"]')[0].checked ? true : false;
    $.post("services/adminservice/mongo/iscachingqueries/" + cacheQueries);
}

function getCache() {
    $.get("services/adminservice/mongo/iscachingqueries",
        function(caching){
            var cacheQueries = caching ? "on" : "off"
            $('input[name="cacheQueries"][value="' + cacheQueries + '"]').prop("checked", true);
        }
    );
}

function setCacheLimit() {
    $.post("services/adminservice/mongo/setquerycachelimit/" + $('input[name="cacheLimit"]').val());
}

function getCacheLimit() {
    $.get("services/adminservice/mongo/getquerycachelimit",
        function(limit){
            $('input[name="cacheLimit"]').val(limit);
        }
    );
}

function setCacheTimeLimit() {
    $.post("services/adminservice/mongo/setquerycachetimelimit/" + $('input[name="cacheTimeLimit"]').val());
}

function getCacheTimeLimit() {
    $.get("services/adminservice/mongo/getquerycachetimelimit",
        function(limit){
            $('input[name="cacheTimeLimit"]').val(limit);
        }
    );
}

function getMachineInfo() {
    $.get("services/adminservice/mongo/getmachineinformation",
        function(machineInfo){
            $(".osName").html(machineInfo.osName);
            $(".osVersion").html(machineInfo.osVersion);
            $(".osArch").html(machineInfo.osArch);
            $(".javaVersion").html(machineInfo.javaVersion);
            $(".maxMemory").html((machineInfo.maxMemory / 1024).toLocaleString() + " KB");
            $(".totalMemory").html((machineInfo.totalMemory / 1024).toLocaleString() + " KB");
            $(".freeMemory").html((machineInfo.freeMemory / 1024).toLocaleString() + " KB");
            $(".runningTime").html(elapsedTime(machineInfo.runningTime));
        }
    );
}

function elapsedTime(time) {
    var returningTime = "";
    var secondsInMilli = 1000;
    var minutesInMilli = secondsInMilli * 60;
    var hoursInMilli = minutesInMilli * 60;
    var daysInMilli = hoursInMilli * 24;

    var elapsedDays = Math.floor(time / daysInMilli);
    time = time % daysInMilli;
    if(elapsedDays) {
        returningTime += elapsedDays + " days, ";
    }

    var elapsedHours = Math.floor(time / hoursInMilli);
    time = time % hoursInMilli;
    if(elapsedDays || elapsedHours) {
        returningTime += elapsedHours + " hours, ";
    }

    var elapsedMinutes = Math.floor(time / minutesInMilli);
    time = time % minutesInMilli;
    if(elapsedDays || elapsedHours || elapsedMinutes) {
        returningTime += elapsedMinutes + " minutes, ";
    }

    var elapsedSeconds = Math.floor(time / secondsInMilli);
    returningTime += elapsedSeconds + " seconds";

    return returningTime;
}

function getActiveSessions() {
    $.get("services/adminservice/mongo/getsessions",
        function(sessions){
            $(".activeSessions").html(sessions);
        }
    );
}

getCache();
getCacheLimit();
getCacheTimeLimit();
getMachineInfo();
getActiveSessions();