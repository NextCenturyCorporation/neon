

describe('publishing events to OWF channels', function () {

    /** a mock method that stands in for the OWF channel publish method */
    var publishMock;

    /** saves the original publish method so it can be restored at the end of the test */
    var originalPublishMethod = OWF.Eventing.publish;

    var databaseName = 'mockDatabaseName';
    var tableName = 'mockTableName';

    beforeEach(function () {
        publishMock = jasmine.createSpy('message publisher');
        OWF.Eventing.publish = publishMock;
    });

    afterEach(function () {
        // no good way way to do this only once after the whole test suite
        OWF.Eventing.publish = originalPublishMethod;
    });

    /**
     * Executes a query method and tests that the results are published to the specified channel
     * @param channel The channel the message should be published to
     * @param queryExecutorMethod The query executor method to execute
     * @param args The args to that query method
     * @param delegateMethodName The underlying query method to which the OWF query executor delegates its calls to. This
     * is used to ensure that the correct method is actually called.
     */
    function testResultsPublishedToChannel(channel, queryExecutorMethod, args, delegateMethodName) {
        // not all of the real methods actually return any results , but for this test it doesn't matter
        var mockResults = {mock: "results"};
        var delegateSpy = spyOn(neon.query, delegateMethodName).andCallThrough();
        neon.mock.AjaxMockUtils.mockNextAjaxCall(mockResults);
        queryExecutorMethod.apply(args);
        var expectedArgs = {};
        _.extend(expectedArgs, mockResults);
        // the owf query executor appends a source, so add it here
        expect(publishMock).toHaveBeenCalledWith(channel, expectedArgs);

        // verify that the correct delegate method on the query executor was actually called
        expect(delegateSpy).toHaveBeenCalled();
    }


    it('should publish add filter results', function () {
        var filterKey = {
            uuid: "84bc5064-c837-483b-8454-c8c72abe45f8",
            dataSet: {
                databaseName: databaseName,
                tableName: tableName
            }
        };
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            neon.eventing.publishing.addFilter,
            [filterKey, filter],
            'addFilter'
        );
    });

    it('should publish remove filter results', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            neon.eventing.publishing.removeFilter,
            ["filterId"],
            'removeFilter'
        );
    });

    it('should publish clear filter results', function () {

        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            neon.eventing.publishing.clearFilters,
            [],
            'clearFilters'
        );
    });

    it('should publish add selection', function () {
        var filterKey = {
            uuid: "84bc5064-c837-483b-8454-c8c72abe45f8",
            dataSet: {
                databaseName: databaseName,
                tableName: tableName
            }
        };
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            neon.eventing.publishing.addSelection,
            [filterKey, filter],
            'addSelection'
        );
    });

    it('should publish remove selection', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            neon.eventing.publishing.removeSelection,
            ["filterId"],
            'removeSelection'
        );
    });

    it('should publish clear selection', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            neon.eventing.publishing.clearSelection,
            [],
            'clearSelection'
        );
    });
});