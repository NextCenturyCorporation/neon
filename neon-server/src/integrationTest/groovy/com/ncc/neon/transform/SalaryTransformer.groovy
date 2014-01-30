package com.ncc.neon.transform
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.TableQueryResult


class SalaryTransformer implements Transformer{

    @Override
    QueryResult convert(QueryResult queryResult, def params) {
        List<Map<String,Object>> data = queryResult.data
        data.each { Map<String,Object> rows ->
            rows.put("salary", rows.get("salary") * 1.1)
        }
        return new TableQueryResult(data)
    }

    @Override
    String getName() {
        return SalaryTransformer.name
    }
}
