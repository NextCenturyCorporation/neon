package com.ncc.neon.result
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.query.ColumnMetadata
import com.ncc.neon.metadata.model.query.ColumnMetadataList
import com.ncc.neon.metadata.store.MetadataRetriever
import com.ncc.neon.query.NamedQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.clauses.SelectClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */
@Component
class MetadataResolver {

    @Autowired
    MetadataConnection metadataConnection

    ColumnMetadataList resolveQuery(Query query) {
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        List<String> columns = query.fields
        if (query.fields == SelectClause.ALL_FIELDS) {
            columns = []
        }
        return retriever.retrieve(query.databaseName, query.tableName, columns)
    }

    ColumnMetadataList resolveQuery(QueryGroup queryGroup) {
        def list = []
        queryGroup.namedQueries.each { NamedQuery nq ->
            ColumnMetadataList metadataList = resolveQuery(nq.query)
            metadataList.dataSet.each { ColumnMetadata metadata ->
                if(!list.contains(metadata)){
                    list << metadata
                }
            }
        }
        return new ColumnMetadataList(list)
    }

    WidgetAndDatasetMetadataList resolveQuery(String databaseName, String tableName, String widgetName){
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        return retriever.retrieve(databaseName, tableName, widgetName)
    }
}
