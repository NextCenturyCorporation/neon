package com.ncc.neon.result

import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.query.QueryResult
import org.apache.commons.lang.math.NumberUtils
import org.json.JSONArray
import org.json.JSONObject
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

class AssembleClientData {

    QueryResult queryResult
    def metadataObject
    String transformClassName
    List<String> transformParams

    String createClientData(){
        String data = queryResult.toJson()
        if (transformClassName) {
            data = applyTransform(data)
        }
        String metadata = handleMetadata(metadataObject)

        return assemble(data, metadata)
    }

    static FieldNames createFieldNames(Collection<String> names, WidgetAndDatasetMetadataList metadata){
        ColumnMapping mapping = new ColumnMapping()
        metadata.dataSet.each { WidgetAndDatasetMetadata data ->
            mapping.put(data.elementId, data.value)
        }

        new FieldNames(fieldNames: names, metadata: mapping)
    }

    private String handleMetadata(WidgetAndDatasetMetadataList data) {
        JSONArray array = new JSONArray()

        data.dataSet.each{ WidgetAndDatasetMetadata dataset ->
            JSONObject object = createFieldMapping(dataset)
            array.put(object)
        }

        return array.toString()
    }

    private JSONObject createFieldMapping(WidgetAndDatasetMetadata widgetAndDatasetMetadata) {
        JSONObject object = new JSONObject()
        object.put("elementId", widgetAndDatasetMetadata.elementId)
        object.put("value", widgetAndDatasetMetadata.value)

        return object
    }

    private String handleMetadata(ColumnMetadataList data) {
        JSONArray array = new JSONArray()

        data.dataSet.each{ ColumnMetadata column ->
            JSONObject object = createColumnObject(column)
            array.put(object)
        }

        return array.toString()
    }

    private JSONObject createColumnObject(ColumnMetadata column) {
        JSONObject object = new JSONObject()
        object.put("columnName", column.columnName)
        column.properties.each { k, v ->
            if (v == true) {
                object.put(k, v)
            }
        }
        return object
    }

    private String assemble(String data, String metadata){
        return "{\"data\":${data},\"metadata\":${metadata}}"
    }

    private def applyTransform(json) {
        def transform = instantiateTransform()
        return transform.apply(json)
    }

    private def instantiateTransform() {
        def typedParams = transformParams.collect { NumberUtils.isNumber(it) ? NumberUtils.createNumber(it) : it }
        def transformParamTypes = typedParams.collect { it.class }
        def transformClass = AssembleClientData.classLoader.loadClass(transformClassName)
        def constructor = transformClass.getConstructor(transformParamTypes as Class[])
        return constructor.newInstance(typedParams as Object[])
    }

}
