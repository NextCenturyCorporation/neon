
$(function(){
    neon.query.SERVER_URL = "http://localhost:11402/neon";
});


function query(databaseName, tableName){
    var query =  new neon.query.Query().selectFrom(databaseName, tableName);
    neon.query.executeQuery(query, function(data){
        $('#results').text(JSON.stringify(data.data));
    });
}