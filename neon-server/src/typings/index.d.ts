
import * as log4javascript from "log4javascript";

declare namespace neon {
    const NEON_SERVER: string;
    export function setNeonServerUrl(url: string): void;
    export function ready(functionToRun: () => any): void; 
    export function serviceUrl(servicePath: string, serviceName: string, queryParamsString: string): string; 

    export namespace util {
        export class AjaxRequest {
            constructor(xhr: XMLHttpRequest);
            abort(): void;
            always(cb: (resp: any) => void): this;
            done(cb: (resp: any) => void): this;
            fail(cb: (resp: any) => void): this;
        }

        export namespace ajaxUtils {
            export function doDelete(url: string, opts: any): neon.util.AjaxRequest;
            export function doGet(url: string, opts: any): neon.util.AjaxRequest;
            export function doPost(url: string, opts: any): neon.util.AjaxRequest;
            export function doPostBinary(
                binary: Blob, 
                url: string,
                successCallback: (resp: any) => any,
                errorCallback: (resp: any) => any
            ): neon.util.AjaxRequest;
            export function doPostJSON(object: any, url: string, opts: any): neon.util.AjaxRequest;
            export function setStartStopCallbacks(requestStart: (resp: any) => any, requestEnd: (resp: any) => any);
            export function useDefaultstartStopCallbacks();
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
            export function getLogger(name: string): log4javascript.Logger;
            export function useBrowserConsoleAppender(logger: log4javascript.Logger);
            export function usePopupAppender(logger: log4javascript.Logger);
        }

        export namespace owfUtils {
            export function isRunningInOWF(): boolean;
        }
    }

    export namespace eventing {

        export interface EventBusInterface {
            publish(channel: string, message: any);
            subscribe(channel: string, callback: (resp: any) => void);
            unsubscribe(subscription: any);
        }

        export namespace owf {
            export class OWFEventBus implements EventBusInterface {
                subscriptions_: any[];

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
            addFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            removeFilter(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            replaceFilter(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            clearFilters(successCallback?: (resp: any) => any, errorCallback?: (resp: any) => any): neon.util.AjaxRequest;
            clearFiltersSilently(successCallback?: (resp: any) => any, errorCallback?: (resp: any) => any): neon.util.AjaxRequest;
            addSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            removeSelection(id: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            replaceSelection(id: string, filter: neon.query.Filter, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
            createFilterKey_(id: string, filter: neon.query.Filter): {id: string, filter: neon.query.Filter};
            createChannelCallback_(channelName: string, successCallback: (resp: any) => any): (resp: any) => any;
        }
    }

    export namespace query {
        export enum SortOrder { ASCENDING, DESCENDING }
        export class Transform {
            constructor(name: string);

            params(params: any): this;
        }

        //the objects created by neon.where (but not neon.Query.where) which are
        //opaque
        export interface WherePredicate {
            type: string;
        }

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

        export class Filter {
            filterName: string;
            databaseName: string;
            tableName: string;
            whereClause: WherePredicate;

            static getFilterState(
                databaseName: string,
                tableName: string,
                successCallback: (resp: any) => any,
                errorCallback: (resp: any) => any
            ): neon.util.AjaxRequest;

            constructor();
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
                queries: any,
                successCallback: (resp: any) => void,
                errorCallback: (resp: any) => void,
                fileType: number
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

        //type union used in the groupBy signature
        type GroupByParam = string | neon.query.GroupByFunctionClause;

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

            geoIntersection(locationField: string, points: neon.util.LatLon[], geometryType: string): Query;
            geoWithin(locationField: string, points: neon.util.LatLon): Query;

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
                center: neon.util.LatLon,
                distance: number,
                distanceUnit: string
            ): Query;
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

        export function and(...clauses: WherePredicate[]): BooleanClause;
        export function or(...clauses: WherePredicate[]): BooleanClause;
        export function where(fieldName: string, op: string, value: string|number|Date): WherePredicate;
    }

    namespace widget {
        export function getInstanceId(qualifier: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
        export function getSavedState(id: string, successCallback: (resp: any) => any): neon.util.AjaxRequest;
        export function saveState(
            instanceId: string,
            stateObject: any,
            successCallback: (resp: any) => any,
            errorCallback: (resp: any) => any
        ): neon.util.AjaxRequest;
        export function getPropertyKeys(successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        export function getProperty(key: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        export function removeProperty(key: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
        export function setProperty(key: string, value: string, successCallback: (resp: any) => any, errorCallback: (resp: any) => any): neon.util.AjaxRequest;
    }
}
export = neon;
export as namespace neon;
