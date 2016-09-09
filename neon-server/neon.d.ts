
declare module "neon-framework" {
    import * as log4javascript from "log4javascript";

    interface AjaxRequest {
        abort(): void;

        always(cb: (resp: any) => void): this;
        done(cb: (resp: any) => void): this;
        fail(cb: (resp: any) => void): this;
    }

    interface LatLon {
        constructor(latDeg: number, lonDeg: number);

        validateArgs(latDeg: number, lonDeg: number);
    }

    //type union used in the groupBy signature
    export type GroupByParam = string | neon.query.GroupByFunctionClause;

    export namespace neon {
        const NEON_SERVER: string;
        function ready(functionToRun: () => any): void; 
        function serviceUrl(servicePath: string, serviceName: string, queryParamsString: string): string; 

        export namespace eventing {

            export interface EventBusInterface {
                publish(channel: string, message: any);
                subscribe(channel: string, callback: (resp: any) => void);
                unsubscribe(subscription: any);
            }

            export namespace owf {
                export class OWFEventBus implements EventBusInterface {
                    subscriptions_: Object[];

                    constructor();
                    publish(channel: string, message: any);
                    subscribe(channel: string, callback: (resp: any) => void);
                    unsubscribe(subscription: any);
                }
            }

            export namespace channels {
                let SELECTION_CHANGED: string;
                let FILTERS_CHANGED: string;
                let CONNECT_TO_HOST: string;
            }

            export class Messenger {
                id_: string;
                channels: string[];

                constructor();

                publish(channel: string, message: any);
                subscribe(channel: string, callback: (resp: any) => void);
                unsubscribe(subscription: any): neon.util.AjaxRequest;
                unsubscribeAll();
                events(callbacks: {[key:string]: (resp: any) => void});
                removeEvents();
                addFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                removeFilter(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                replaceFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                clearFilters(successCallback?: (resp: any) => any, errorCallback?: (resp: any) => any): AjaxRequest;
                clearFiltersSilently(successCallback?: (resp: any) => any, errorCallback?: (resp: any) => any): AjaxRequest;
                addSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                removeSelection(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                replaceSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): AjaxRequest;
                createFilterKey_(id: string, filter: neon.query.Filter): {id: string, filter: neon.query.Filter};
                createChannelCallback_(channelName: string, successCallback: (resp: any) => any): (resp: any) => any;
            }
        }

        export namespace query {
            function and(...clauses: WherePredicate[]): BooleanClause;
            function or(...clauses: WherePredicate[]): BooleanClause;
            function where(fieldName: string, op: string, value: string|number|Date): WherePredicate;

            export class Connection {
                //the three constants for databaseType
                static ELASTICSEARCH: string;
                static MONGO: string;
                static SPARK: string;

                host_: string;
                databaseType_: string;

                constructor();
                connect(databaseType: string, host: string): void;

                executeQuery(
                    query: Query,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                executeQueryGroup(
                    query: QueryGroup,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                executeArrayCountQuery(
                    databaseName: string,
                    tableName: string,
                    fieldName: string,
                    limit: number,
                    whereClause: WherePredicate,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                executeQueryService_(
                    query: Query,
                    successCallback: (resp: any) => void,
                    errorCallback: (resp: any) => void,
                    serviceName: string
                ): AjaxRequest;

                executeExport(
                    query: Query,
                    successCallback: (resp: any) => void,
                    errorCallback: (resp: any) => void,
                    fileType: string
                ): AjaxRequest;

                executeUpdateFile(
                    data: any,
                    successCallback: (resp: any) => void,
                    errorCallback: (resp: any) => void,
                    host?: string,
                    datbaseType?: string
                ): AjaxRequest;

                executeCheckTypeGuesses(
                    uuid: string,
                    successCallback: (resp: any) => void,
                    host?: string,
                    databaseType?: string
                ): AjaxRequest;

                executeLoadFileIntoDb(
                    data: any,
                    uuid: string,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void,
                    host?: string,
                    databaseType?: string
                ): AjaxRequest;

                executeCheckImportProgress(
                    uuid: string,
                    successCallback: (resp: any) => void,
                    host?: string,
                    databaseType?: string
                ): AjaxRequest;

                executeRemoveDataset(
                    user: string,
                    data: any,
                    sucessCallback: (resp: any) => void,
                    errorCallbalk: (resp: any) => void,
                    host: string,
                    databaseType: string
                ): AjaxRequest;

                getDatbaseNames(
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                getTableNames(
                    databaseName: string,
                    successCallback?: (resp: any) => void
                ): AjaxRequest;

                getFieldNames(
                    databasename: string,
                    tableName: string,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                getTableNamesAndFieldNames(
                    database: string,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                getTranslationCache(
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                setTranslationCache(
                    cache: any,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                saveState(
                    stateParams: any,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;
                 
                loadState(
                    stateParams: any,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                deleteState(
                    stateName: string,
                    successCallback: (resp: any) => void,
                    errorCallback: (resp: any) => void
                ): AjaxRequest;

                getAllStateNames(
                    successCallback: (resp: any) => void,
                    errorCallback: (resp: any) => void
                ): AjaxRequest;

                getStateName(
                    stateParams: any,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                getFieldTypes(
                    databaseName: string,
                    tableName: string,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;

                getFieldTypesForGroup(
                    databaseTotableNames: any,
                    successCallback: (resp: any) => void,
                    errorCallback?: (resp: any) => void
                ): AjaxRequest;
            }

            export class Query {
                static ASCENDING: number;
                static AVG: string;
                static COUNT: string;
                static DAY: string;
                static DESCENDING: number;
                static HOUR: string;
                static KM: string;
                static MAX: string;
                static METER: string;
                static MILE: string;
                static MIN: string;
                static MINUTE: string;
                static MONTH: string;
                static SECOND: string;
                static SUM: string;
                static YEAR: string;

                constructor();

                toString(): string;

                aggregate(
                    aggregationOperation: string,
                    aggregationField: string,
                    name?: string
                ): Query;
                distinct(): Query;

                enableAggregateArraysByElement(): Query;

                geoIntersection(locationField: string, points: LatLon[], geometryType: string): Query;
                geoWithin(locationField: string, points: LatLon): Query;

                groupBy(...fields: GroupByParam[]): Query;
                groupBy(fields: GroupByParam[]): Query;

                ignoreFilters(filterIds: string[]): Query;
                ignoreFilters(...filterIds: string[]): Query;

                limit(limit: number): Query;
                offset(offset: number): Query;
                selectFrom(databaseName: string , tableName: string): Query;
                selectionOnly(): Query;

                //the fields array should have an even number of elements where every even-indexed
                //element is a fieldName string and every odd-indexed element is the corresponding
                //sortOrder
                sortBy(fields: any[]): Query;
                sortBy(fieldName: string, sortOrder: SortOrder): Query;

                transform(transformObj: Transform): Query;

                where(filterProp: string, operator: string, value: any): Query;
                where(pred: WherePredicate): Query;

                withFields(fields: string[]): Query;
                withFields(...fields: string[]): Query;

                withinDistance(
                    locationField: string,
                    center: LatLon,
                    distance: number,
                    distanceUnit: string
                ): Query;
            }

            class QueryGroup {
                queries: Query[];
                ignoreFilters_: boolean;
                selectionOnly_: boolean;
                ignoredFilterIds_: string[];

                constructor();

                addQuery(query: Query): this;
                ignoreFilters(filterIds: string[]): this;
                selectionOnly(): this;
            }

            enum SortOrder { ASCENDING, DESCENDING }

            export class Transform {
                constructor(name: string);

                params(params: any): this;
            }

            //the objects created by neon.where (but not neon.Query.where) which are
            //opaque
            interface WherePredicate {}

            export class FieldFunction {
                constructor(operation: string, field: string, name: string);
            }

            export class BooleanClause implements WherePredicate {
                type: string;
                whereClauses: WherePredicate[];

                constructor(type: string, whereClauses: WherePredicate[]);
            }

            export class GroupByFunctionClause extends FieldFunction {
                constructor(operation: string, field: string, name: string);
            }

            export class WhereClause implements WherePredicate {
                type: string;
                lhs: string;
                operator: string;
                rhs: string|number|Date;

                constructor(lhs: string, operator: string, rhs: string);
            }

            export class SortClause {
                fieldName: string;
                sortOrder: number;

                constructor(fieldName: string, sortOrder: number);
            }

            export class LimitClause {
                limit: number;

                constructor(limit: number);
            }

            export class OffsetClause {
                offset: number;
                constructor(offsest: number);
            }

            export class WithinDistanceClause {
                type: string;
                locationField: string;
                center: LatLon;
                distance: number;
                distanceUnit: string;

                constructor(locationField: string, center: LatLon, distance: number, distanceUnit: string);
            }

            class intersectionClause {
                type: string;
                locationField: string;
                points: LatLon[];
                geometryType: string
                constructor(type: string, locationField: string, points: LatLon[], geometryType: string);
            }

            class withinClause {
                type: string;
                locationfield: string;
                points: LatLon[];

                constructor(locationField: string, points: LatLon[]);
            }

            export class Filter {
                filterName: string;
                databaseName: string;
                tableName: string;
                whereClause: WhereClause;

                static getFilterState(
                    databaseName: string,
                    tableName: string,
                    successCallback: (resp: any) => any,
                    errorCallback: (resp: any) => any
                ): AjaxRequest;

                constructor();
                geoIntersection(locationField: string, points: LatLon[], geometryType: string): this;
                geoWithin(locationField: string, points: LatLon): this;

                name(name: string): this;
                selectFrom(databaseName: string, tableName: string): this;
                where(): this;

                withinDistance(
                    locationField: string,
                    center: LatLon,
                    distance: number,
                    distanceUnit: string
                ): this;

            }

            function where(fieldName: string, op: string, value: string|number|Date): WherePredicate;
        }
        
        interface ajaxUtils {
            doDelete(url: string, opts: any): AjaxRequest;
            doGet(url: string, opts: any): AjaxRequest;
            doPost(url: string, opts: any): AjaxRequest;
            doPostBinary(
                binary: Blob, 
                url: string,
                successCallback: (resp: any) => any,
                errorCallback: (resp: any) => any
            ): AjaxRequest;
            doPostJSON(object: any, url: string, opts: any): AjaxRequest;
            setStartStopCallbacks(requestStart: (resp: any) => any, requestEnd: (resp: any) => any);
            useDefaultstartStopCallbacks();
        }

        interface arrayUtils {
            argumentsToArray(args: any[]): any[];
        }

        interface infoUtils {
            getNeonVersion(successCallback: (resp: any) => any): AjaxRequest;
        }

        interface loggerUtils {
            getGlobalLogger(): log4javascript.Logger;
            getLogger(name: string): log4javascript.Logger;
            useBrowserConsoleAppender(logger: log4javascript.Logger);
            usePopupAppender(logger: log4javascript.Logger);
        }

        interface owfUtils {
            isRunningInOWF(): boolean;
        }
        
        export namespace util {
            export class AjaxRequest implements AjaxRequest {
                constructor(xhr: XMLHttpRequest);
            }
            export let ajaxUtils: ajaxUtils;
            export let arrayUtils: arrayUtils;
            export let infoUtils: infoUtils;
            export let LatLon: LatLon;
            export let loggerUtils: loggerUtils;
            export let owfUtils: owfUtils;
        }

        export namespace widget {
            function getInstanceId(qualifier: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
            function getSavedState(id: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
            function saveState(
                instanceId: string,
                stateObject: any,
                successCallback: (resp: any) => any,
                errorCallback: (resp: any) => any
            ): AjaxRequest;
        }
    }
}

