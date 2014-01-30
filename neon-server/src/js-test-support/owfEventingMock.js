

neon.mock.channelRegistry = {};

neon.mock.Channel = function() {
    this.callbacks = [];
}

OWF.Util = {
    isRunningInOWF: function(){
        return true;
    }
};

OWF.Eventing.subscribe = function(channelName, callback) {
    if ( !(channelName in neon.mock.channelRegistry) ) {
        neon.mock.channelRegistry[channelName] = new neon.mock.Channel();
    }
    neon.mock.channelRegistry[channelName].callbacks.push(callback)
};

OWF.Eventing.publish = function(channelName, message) {
    var channel = neon.mock.channelRegistry[channelName];
    channel.callbacks.forEach(function(callback) {
        callback.call(null, 'owfEventingMockSender', message);
    });

};

neon.mock.clearChannels = function() {
    neon.mock.channelRegistry = {};
};