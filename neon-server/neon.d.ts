/**
 * A partial typescript definition for the Neon Framework JavaScript API.  Functions will
 * be added here as they are needed.
 */
declare namespace neon {
    export static public SERVER_URL: string = 'http://localhost:8080/neon'; 

    namespace util {
        export class AjaxRequest {
            constructor(xhr: XMLHttpRequest);

            abort(): void;

            always(cb: (resp: any) => void): this;
            done(cb: (resp: any) => void): this;
            fail(cb: (resp: any) => void): this;
        }

        export class LatLon {
            constructor(latDeg: number, lonDeg: number);

            validateArgs(latDeg: number, lonDeg: number);
        }

        namespace loggerUtils {
            usePopupAppender()
        }
    }

    namespace query {
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
                errorCallback?: (resp: any) => void
                serviceName: string
            ): neon.util.AjaxRequest;

            executeExport(
                query: Query,
                successCallback: (resp: any) => void,
                errorCallback: (resp: any) => void),
                fileType: string
            ): neon.util.ajaxRequest;

            executeUpdateFile(
                data: any,
                successCallback: (resp: any) => void,
                errorCallback: (resp: any) => void,
                host?: string,
                datbaseType?: string,
            ): neon.util.AjaxRequest;

            executeCheckTypeGuesses(
                uuid: string,
                successCallback: (resp: any) => void,
                host?: string,
                databaseType?: string
            }: neon.util.AjaxRequest;

            executeLoadFileIntoDb(
                data: any = {},
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
            ): neon.util.AjaxRequst;

            getFieldNames(
                databasename: string,
                tableName: string,
                successCallback: (resp: any) => void,
                errorCallback?: (resp: any) => void)
            ): neon.util.AjaxRequest;

            getTableNamesAndFieldNames(
                database: string,
                successCallback: (resp: any) => void
                errorCallback?: (resp: any) => void
            ): neon.util.AjaxRequest;

            getTranslationCache(
                successCallback: (resp: any) => void,
                errorCallback?: (resp: any) => void
            ): neon.util.AjaxRequest;

            setTranslationCache(
                cache: any,
                successCallback: (resp: any) => void
                errorCallback?: (resp: any) => void
            ): neon.util.AjaxRequest;

            saveState(
                stateParams: any,
                successCallback: (resp: any) => void,
                errorCallback?: (resp: any) => void,
            ): neon.util.AjaxRuest;
             
            loadState(
                stateParams: any,
                successCallback: (resp: any) => void,
                errorCallback?: (resp: any) => void
            ) neon.util.AjaxRequest;

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
            type: string = 'where';
            lhs: string;
            operator: string;
            rhs: string;

            constructor(lhs: string, operator: string, rhs: string);
        }

        export class SortClause {
            fieldName: string;
            sortOrder: number = 1;

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
            type: string = 'withinDistance';
            locationField: string;
            center: neon.util.LatLon,
            distance: number,
            distanceUnit: string

            constructor(locationField: string, center: neon.util.LatLon, distance: number, distanceUnit: string);
        }

        export class intersectionClause {
            type: string = 'geoIntersection';
            locationField: string;
            points: neon.util.LatLon[];
            geometryType: string
            constructor(type: string, locationField: string, points: neon.util.LatLon[], geometryType: string);
        }

        export class withinClause {
            type: string = 'geoIntersection';
            locationfield: string:
            points: neon.util.LatLon[];

            constructor(locationField: string, points: neon.util.LatLon[]);
        }

        //type union used in the groupBy signature
        export type GroupByParam = string | GroupByFunctionClause;

        export function where(fieldName: string, op: string, value: string): WherePredicate;
        export function and(...clauses: WherePredicate[]): WherePredicate;
        export function or(...clauses: WherePredicate[]): WherePredicate;

        export class Query {
            static ASCENDING: number = 1;
            static AVG: string = 'avg';
            static COUNT: string = 'count';
            static DAY: string = 'dayOfMonth';
            static DESCENDING: number = -1;
            static HOUR: string = 'hour';
            static KM: string = 'km';
            static MAX: string = 'max';
            static METER: string = 'meter';
            static MILE: string = 'mile';
            static MIN: string = 'min';
            static MINUTE: string = 'minute';
            static MONTH: string = 'month';
            static SECOND: string = 'second';
            static SUM: string = 'sum';
            static YEAR: string = 'year';

            constructor();

            toString(): string;

            aggregate(
                aggregationOperation: string,
                aggregationField: string,
                name?: string
            ): this;
            distinct(): this;

            enableAggregateArraysByElement(): this;

            geoIntersection(locationField: string, points: neon.util.LatLon[], geometryType: string):
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
            queries: Query[] = [];
            ignoreFilters_: boolean = false;
            selectionOnly_: boolean = false;
            ignoredFilterIds_: string[] = [];

            constructor();

            addQuery(query: Query): this;
            ignoreFilters(filterIds: string[]): this;
            selectionOnly(): this;
        }
    }

    export function ready(functionToRun: function): void; 
    export function serviceUrl(servicePath: string, serviceName: string, queryParamsString: string);
}

//still not clear on why this is necessary but it seems
//that it is
declare module 'neon' {
    export = neon;
}

