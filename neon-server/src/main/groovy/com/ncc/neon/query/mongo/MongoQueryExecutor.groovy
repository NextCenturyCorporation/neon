package com.ncc.neon.query.mongo
import com.mongodb.DB
import com.mongodb.MongoClient
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.*
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.transform.TransformerNotFoundException
import com.ncc.neon.query.Transform
import com.ncc.neon.transform.Transformer
import com.ncc.neon.transform.TransformerRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Executes queries against a mongo data store
 */
@Component
class MongoQueryExecutor implements QueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    @Autowired
    TransformerRegistry registry

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult execute(Query query, QueryOptions options) {
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)
        QueryResult queryResult = worker.executeQuery(mongoQuery)
        return transform(query.transform, queryResult)
    }

    QueryResult transform(Transform transform, QueryResult queryResult) {
        if(!transform){
            return queryResult
        }

        String transformName = transform.transformName
        Transformer transformer = registry.getTransformer(transformName)
        if(!transformer){
            throw new TransformerNotFoundException("Transform ${transformName} does not exist.")
        }

        return transformer.convert(queryResult, transform.params)
    }

    @Override
    QueryResult execute(QueryGroup queryGroup, QueryOptions options) {
        TableQueryResult queryResult = new TableQueryResult()
        queryGroup.queries.each {
            def result = execute(it, options)
            queryResult.data.addAll(result.data)
        }
        return queryResult
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing Mongo SHOW DATABASES")
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName) {
        DB database = mongo.getDB(dbName)
        LOGGER.info("Executing Mongo SHOW COLLECTIONS on database {}", dbName)
        database.getCollectionNames().collect { it }
    }

    @Override
    QueryResult getFieldNames(String databaseName, String tableName) {
        def db = mongo.getDB(databaseName)
        def collection = db.getCollection(tableName)
        def result = collection.findOne()
        return new ListQueryResult(result?.keySet() ?: [])
    }

    private AbstractMongoQueryWorker createMongoQueryWorker(Query query) {
        if (query.isDistinct) {
            LOGGER.debug("Using distinct mongo query worker")
            return new DistinctMongoQueryWorker(mongo)
        } else if (query.aggregates || query.groupByClauses) {
            LOGGER.debug("Using aggregate mongo query worker")
            return new AggregateMongoQueryWorker(mongo)
        }
        LOGGER.debug("Using simple mongo query worker")
        return new SimpleMongoQueryWorker(mongo)
    }

    private MongoClient getMongo(){
        connectionManager.getConnectionClient().getMongo()
    }

}
