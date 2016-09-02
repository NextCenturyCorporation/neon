import * as log4javascript from "log4javascript";
import * as uuid from "node-uuid";

export let SERVER_URL: string;

export function ready(functionToRun: () => any): void; 
export function serviceUrl(servicePath: string, serviceName: string, queryParamsString: string): string; 

export namespace eventing {
    export let eventBus_: EventBusInterface;

    export namespace Channels {
        export const SELECTION_CHANGED: string;
        export const FILTERS_CHANGED: string;
        export const CONNECT_TO_HOST: string;
    }

    export interface EventBusInterface {}

    export function createEventBus_(): EventBusInterface;

    export class EventBus implements EventBusInterface {
        private channel_: string;
        private subscriptions_: Object;

        publish(channel: string, message: any, messengerId: string);
        subscribe(channel: string, callback: (resp: any) => void, messengerId: string);
        unsubscribe(subscription: any, messengerId: string);
    }

    export namespace owf {
        export class OWFEventBus implements EventBusInterface {
            private subscriptions_: Object;

            publish(channel: string, message: any);
            subscribe(channel: string, callback: (resp: any) => void);
            unsubscribe(subscription: any);
        }
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
        addFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        removeFilter(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        replaceFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        clearFilters(successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        clearFiltersSilently(successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        addSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        removeSelection(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        replaceSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        createFilterKey_(id: string, filter: neon.query.Filter): {id: string, filter: neon.query.Filter};
        createChannelCallback_(channelName: string, successCallback: (resp: any) => any): (resp: any) => any;
    }
}

export namespace query {
    export class Connection {
        constructor();

        //the three constants for databaseType
        static ELASTICSEARCH: string;
        static MONGO: string;
        static SPARK: string;

        connect(databaseType: string, host: string): void;

        executeQuery(
            query: Query,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        executeQueryGroup(
            query: QueryGroup,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        executeArrayCountQuery(
            databaseName: string,
            tableName: string,
            fieldName: string,
            limit: number,
            whereClause: WherePredicate,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        executeQueryService_(
            query: Query,
            successCallback: (resp: any) => void,
            errorCallback: (resp: any) => void,
            serviceName: string
        ): neon.util.AjaxRequest;

        executeExport(
            query: Query,
            successCallback: (resp: any) => void,
            errorCallback: (resp: any) => void,
            fileType: string
        ): neon.util.AjaxRequest;

        executeUpdateFile(
            data: any,
            successCallback: (resp: any) => void,
            errorCallback: (resp: any) => void,
            host?: string,
            datbaseType?: string
        ): neon.util.AjaxRequest;

        executeCheckTypeGuesses(
            uuid: string,
            successCallback: (resp: any) => void,
            host?: string,
            databaseType?: string
        ): neon.util.AjaxRequest;

        executeLoadFileIntoDb(
            data: any,
            uuid: string,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void,
            host?: string,
            databaseType?: string
        ): neon.util.AjaxRequest;

        executeCheckImportProgress(
            uuid: string,
            successCallback: (resp: any) => void,
            host?: string,
            databaseType?: string
        ): neon.util.AjaxRequest;

        executeRemoveDataset(
            user: string,
            data: any,
            sucessCallback: (resp: any) => void,
            errorCallbalk: (resp: any) => void,
            host: string,
            databaseType: string
        ): neon.util.AjaxRequest;

        getDatbaseNames(
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getTableNames(
            databaseName: string,
            successCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getFieldNames(
            databasename: string,
            tableName: string,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getTableNamesAndFieldNames(
            database: string,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getTranslationCache(
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        setTranslationCache(
            cache: any,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        saveState(
            stateParams: any,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;
         
        loadState(
            stateParams: any,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        deleteState(
            stateName: string,
            successCallback: (resp: any) => void,
            errorCallback: (resp: any) => void
        ): neon.util.AjaxRequest;

        getAllStateNames(
            successCallback: (resp: any) => void,
            errorCallback: (resp: any) => void
        ): neon.util.AjaxRequest;

        getStateName(
            stateParams: any,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getFieldTypes(
            databaseName: string,
            tableName: string,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;

        getFieldTypesForGroup(
            databaseTotableNames: any,
            successCallback: (resp: any) => void,
            errorCallback?: (resp: any) => void
        ): neon.util.AjaxRequest;
    }

    export class Filter {
        filterName: string;
        databaseName: string;
        tableName: string;
        whereClause: neon.query.WhereClause;

        static getFilterState(
            databaseName: string,
            tableName: string,
            successCallback: (resp: any) => any,
            errorCallback: (resp: any) => any
        ): neon.util.AjaxRequest;

        geoIntersection(locationField: string, points: neon.util.LatLon[], geometryType: string): this;
        geoWithin(locationField: string, points: neon.util.LatLon): this;

        name(name: string): this;
        selectFrom(databaseName: string, tableName: string): this;
        where(): this;

        withinDistance(
            locationField: string,
            center: neon.util.LatLon,
            distance: number,
            distanceUnit: string
        ): this;

    }

    export enum SortOrder { ASCENDING, DESCENDING }

    export class Transform {
        constructor(name: string);

        params(params: any): this;
    }

    //the objects created by neon.query.where (but not neon.query.Query.where) which are
    //opaque
    export interface WherePredicate {}

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
        rhs: string;

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
        center: neon.util.LatLon;
        distance: number;
        distanceUnit: string;

        constructor(locationField: string, center: neon.util.LatLon, distance: number, distanceUnit: string);
    }

    export class intersectionClause {
        type: string;
        locationField: string;
        points: neon.util.LatLon[];
        geometryType: string
        constructor(type: string, locationField: string, points: neon.util.LatLon[], geometryType: string);
    }

    export class withinClause {
        type: string;
        locationfield: string;
        points: neon.util.LatLon[];

        constructor(locationField: string, points: neon.util.LatLon[]);
    }

    //type union used in the groupBy signature
    export type GroupByParam = string | GroupByFunctionClause;

    export function where(fieldName: string, op: string, value: string): WherePredicate;
    export function and(...clauses: WherePredicate[]): WherePredicate;
    export function or(...clauses: WherePredicate[]): WherePredicate;

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
        ): this;
        distinct(): this;

        enableAggregateArraysByElement(): this;

        geoIntersection(locationField: string, points: neon.util.LatLon[], geometryType: string): this;
        geoWithin(locationField: string, points: neon.util.LatLon): this;

        groupBy(...fields: GroupByParam[]): this;
        groupBy(fields: GroupByParam[]): this;

        ignoreFilters(filterIds: string[]): this;
        ignoreFilters(...filterIds: string[]): this;

        limit(limit: number): this;
        offset(offset: number): this;
        selectFrom(databaseName: string , tableName: string): this;
        selectionOnly(): this;

        //the fields array should have an even number of elements where every even-indexed
        //element is a fieldName string and every odd-indexed element is the corresponding
        //sortOrder
        sortBy(fields: any[]): this;
        sortBy(fieldName: string, sortOrder: SortOrder): this;

        transform(transformObj: Transform): this;

        where(filterProp: string, operator: string, value: any): this;
        where(pred: WherePredicate): this;

        withFields(fields: string[]): this;
        withFields(...fields: string[]): this;

        withinDistance(
            locationField: string,
            center: neon.util.LatLon,
            distance: number,
            distanceUnit: string
        ): this;
    }

    export class QueryGroup {
        queries: Query[];
        ignoreFilters_: boolean;
        selectionOnly_: boolean;
        ignoredFilterIds_: string[];

        constructor();

        addQuery(query: Query): this;
        ignoreFilters(filterIds: string[]): this;
        selectionOnly(): this;
    }
}

export namespace util {
    export class AjaxRequest {
        constructor(xhr: XMLHttpRequest);

        abort(): void;

        always(cb: (resp: any) => void): this;
        done(cb: (resp: any) => void): this;
        fail(cb: (resp: any) => void): this;
    }

    export namespace ajaxUtils {
        function doDelete(url: string, opts: any): neon.util.AjaxRequest;
        function doGet(url: string, opts: any): neon.util.AjaxRequest;
        function doPost(url: string, opts: any): neon.util.AjaxRequest;
        function doPostBinary(
            binary: Blob, 
            url: string,
            successCallback: (resp: any) => any,
            errorCallback: (resp: any) => any
        ): neon.util.AjaxRequest;
        function doPostJSON(object: any, url: string, opts: any): neon.util.AjaxRequest;
        function setStartStopCallbacks(requestStart: (resp: any) => any, requestEnd: (resp: any) => any);
        function useDefaultstartStopCallbacks();
    }

    export namespace arrayUtils {
        export function argumentsToArray(args: any[]): any[];
    }

    export namespace infoUtils {
        export function getNeonVersion(successCallback: (resp: any) => any): neon.util.AjaxRequest;
    }

    export class LatLon {
        constructor(latDeg: number, lonDeg: number);

        validateArgs(latDeg: number, lonDeg: number);
    }

    export namespace loggerUtils {
        export function getGlobalLogger(): log4javascript.Logger;
        export function getLogger(name: string): jog4javascript.Logger;
        export function useBrowserConsoleAppender(logger: log4javascript.Logger);
        export function usePopupAppender(logger: log4javascript.Logger);
    }

    export namespace owfutils {
        export function isRunningInOWF(): boolean;
    }
}

export namespace widget {
    export function getInstanceId(qualifier: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
    export function getSavedState(id: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
    export function saveState(
        instanceId: string,
        stateObject: any,
        successCallback: (resp: any) => any,
        errorCallback: (resp: any) => any
    ): neon.util.AjaxRequest;
}

