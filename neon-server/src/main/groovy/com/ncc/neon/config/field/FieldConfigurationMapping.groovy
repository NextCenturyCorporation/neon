package com.ncc.neon.config.field

import org.codehaus.jackson.map.annotate.JsonDeserialize
import org.codehaus.jackson.map.annotate.JsonSerialize

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

/**
 * Object representation of a configuration bundle. Serializes to JSON for storage.
 */

class FieldConfigurationMapping {

    @JsonSerialize(keyUsing = WidgetDataSetKeySerializer)
    @JsonDeserialize(keyUsing = WidgetDataSetKeyDeserializer)
    Map<WidgetDataSet, ColumnMapping> fieldConfigurationMapping = [:]

    void put(WidgetDataSet widgetDataSet, ColumnMapping mapping) {
        fieldConfigurationMapping.put(widgetDataSet, mapping)
    }

    ColumnMapping get(WidgetDataSet widgetDataSet) {
        def mapping = fieldConfigurationMapping.get(widgetDataSet)
        if(!mapping){
            return new ColumnMapping()
        }
        return mapping
    }
}
