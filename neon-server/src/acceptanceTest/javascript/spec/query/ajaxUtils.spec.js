

/**
 * This is an end to end acceptance test to verify that queries can be executed against a mongo instance.
 * These tests parallel those in the MongoQueryExecutorIntegrationTest.
 */
// neonServerUrl is generated dynamically during the build and included in the acceptance test helper file
neon.query.SERVER_URL = neonServerUrl;

describe('ajax utils', function () {

    it('cancels ajax request', function () {
        var statusText = "";
        // transformServiceUrl is generated dynamically during the build and included in the acceptance test helper file
        var request = neon.util.ajaxUtils.doGet(transformServiceUrl + '/neon/timeouttest', {
            // we should get an error with an "abort" status
            error: function (xhr, status, msg) {
                statusText = xhr.statusText;
            }
        });
        request.cancel();
        waitsFor(function () {
            return statusText === 'abort';
        });
    });

});