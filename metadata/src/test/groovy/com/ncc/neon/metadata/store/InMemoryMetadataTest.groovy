/*
 * Copyright 2014 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.metadata.store

import com.ncc.neon.metadata.model.ColumnMetadata
import com.ncc.neon.metadata.model.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.WidgetInitializationMetadata
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InMemoryMetadataTest {

    // arbitrary constants used for values in the metadata
    private static final String WIDGET_NAME1 = "Widget1"
    private static final String DATABASE_NAME1 = "dbName1"
    private static final String TABLE_NAME1 = "tableName1"
    private static final String DATABASE_NAME2 = "dbName2"
    private static final String TABLE_NAME2 = "tableName2"
    private static final String COLUMN_NAME1 = "columnName1"
    private static final String COLUMN_NAME2 = "columnName2"
    private static final String COLUMN_NAME3 = "columnName3"
    private static final String ELEMENT_ID1 = "element1"
    private static final String ELEMENT_ID2 = "element2"
    private static final String VALUE_1 = "value1"
    private static final String VALUE_2 = "value2"
    private static final String INIT_JSON = '{"key":"value"}'


    private InMemoryMetadata metadata


    @SuppressWarnings(['PublicInstanceField', 'NonFinalPublicField']) // public for junit to use
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Before
    void before() {
        metadata = new InMemoryMetadata()
    }

    @Test
    void "retrieve empty widget initialization metadata when none exists"() {
        WidgetInitializationMetadata empty = metadata.retrieve(WIDGET_NAME1)
        assert empty.widgetName == WIDGET_NAME1
        assert !empty.initDataJson
    }

    @Test
    void "retrieve stored widget initialization metadata"() {
        addWidgetInitializationMetadata()
        assertWidgetInitializationMetadata(metadata)
    }

    /**
     * Asserts that the metadata has the widget initialization data saved in this test
     * @param metadata
     */
    private static void assertWidgetInitializationMetadata(Metadata metadata) {
        WidgetInitializationMetadata retrieved = metadata.retrieve(WIDGET_NAME1)
        assert retrieved.widgetName == WIDGET_NAME1
        assert retrieved.initDataJson == INIT_JSON
    }

    @Test
    void "retrieve empty column metadata when database does not exist"() {
        List<ColumnMetadata> empty = metadata.retrieve("nonExistentDatabase", "nonExistentTable", [] as Set)
        assert empty.isEmpty()
    }

    @Test
    void "retrieve empty column metadata when table does not exist"() {
        List<ColumnMetadata> empty = metadata.retrieve(DATABASE_NAME1, "nonExistentTable", [] as Set)
        assert empty.isEmpty()
    }

    @Test
    void "retrieve empty column metadata when column filters are passed to non existent metadata"() {
        List<ColumnMetadata> empty = metadata.retrieve("nonExistentDatabase", "nonExistentTable", ["nonExistentColumn"] as Set)
        assert empty.isEmpty()
    }

    @Test
    void "retrieve stored column metadata"() {
        addColumnMetadata()
        assertColumnMetadata(metadata)
    }

    @Test
    void "clear column metadata"() {
        addColumnMetadata()
        metadata.clearColumnMetadata(DATABASE_NAME1, TABLE_NAME1)
        List<ColumnMetadata> retrieved = metadata.retrieve(DATABASE_NAME1, TABLE_NAME1, [] as Set)
        assert retrieved.isEmpty()
    }

    /**
     * Verifies that the metadata has the values for the column metadata as stored by this test
     * @param metadata
     */
    private static void assertColumnMetadata(Metadata metadata) {
        List<ColumnMetadata> retrieved = metadata.retrieve(DATABASE_NAME1, TABLE_NAME1, [] as Set)
        assert retrieved.size() == 2

        assert retrieved[0].databaseName == DATABASE_NAME1
        assert retrieved[0].tableName == TABLE_NAME1
        assert retrieved[0].columnName == COLUMN_NAME1
        assertColumnMetadataBooleanProperties(retrieved[0],
                ["numeric", "temporal", "text", "logical", "object", "array", "nullable"],
                ["heterogeneous"])

        assert retrieved[1].databaseName == DATABASE_NAME1
        assert retrieved[1].tableName == TABLE_NAME1
        assert retrieved[1].columnName == COLUMN_NAME2
        assertColumnMetadataBooleanProperties(retrieved[1],
                ["numeric", "temporal", "text", "logical", "object", "array", "nullable"],
                ["heterogeneous"])
    }

    @Test
    void "retrieve column metadata filtered by column names"() {
        addColumnMetadata()
        List<ColumnMetadata> retrieved = metadata.retrieve(DATABASE_NAME1, TABLE_NAME1, [COLUMN_NAME1] as Set)
        assert retrieved.size() == 1

        assert retrieved[0].databaseName == DATABASE_NAME1
        assert retrieved[0].tableName == TABLE_NAME1
        assert retrieved[0].columnName == COLUMN_NAME1
    }


    @Test
    void "retrieve empty widget dataset metadata when database does not exist"() {
        List<WidgetAndDatasetMetadata> empty = metadata.retrieve("nonExistentDatabase", "nonExistentTable", "nonExistentWidget")
        assert empty.isEmpty()
    }

    @Test
    void "retrieve empty widget dataset metadata when table does not exist"() {
        addWidgetAndDatasetMetadata()
        List<WidgetAndDatasetMetadata> empty = metadata.retrieve(DATABASE_NAME1, "nonExistentTable", "nonExistentWidget")
        assert empty.isEmpty()
    }


    @Test
    void "retrieve empty widget dataset metadata when none exists"() {
        addWidgetAndDatasetMetadata()
        List<WidgetAndDatasetMetadata> empty = metadata.retrieve(DATABASE_NAME1, TABLE_NAME1, "nonExistentWidget")
        assert empty.isEmpty()
    }

    @Test
    void "retrieve stored widget dataset metadata"() {
        addWidgetAndDatasetMetadata()
        assertWidgetAndDatasetMetadata(metadata)
    }

    /**
     * Verifies that the metadata has the widget and dataset metadata as stored by this test
     * @param metadata
     */
    private static void assertWidgetAndDatasetMetadata(Metadata metadata) {
        List<WidgetAndDatasetMetadata> retrieved = metadata.retrieve(DATABASE_NAME1, TABLE_NAME2, WIDGET_NAME1)
        assert retrieved.size() == 2

        assert retrieved[0].databaseName == DATABASE_NAME1
        assert retrieved[0].tableName == TABLE_NAME2
        assert retrieved[0].widgetName == WIDGET_NAME1
        assert retrieved[0].elementId == ELEMENT_ID1
        assert retrieved[0].value == VALUE_2

        assert retrieved[1].databaseName == DATABASE_NAME1
        assert retrieved[1].tableName == TABLE_NAME2
        assert retrieved[1].widgetName == WIDGET_NAME1
        assert retrieved[1].elementId == ELEMENT_ID2
        assert retrieved[1].value == VALUE_2
    }


    @Test
    void "file persistence"() {
        addWidgetInitializationMetadata()
        addColumnMetadata()
        addWidgetAndDatasetMetadata()
        File file = folder.newFile()

        // write and re-read back the metadata
        metadata.write(file)

        InMemoryMetadata restored = InMemoryMetadata.create(file)
        assertWidgetInitializationMetadata(restored)
        assertColumnMetadata(restored)
        assertWidgetAndDatasetMetadata(restored)
    }

    private static assertColumnMetadataBooleanProperties(ColumnMetadata metadata, def falseProps, def trueProps) {
        falseProps.each {
            assert !metadata.getProperty(it)
        }
        trueProps.each {
            assert metadata.getProperty(it)
        }
    }

    private void addWidgetInitializationMetadata() {
        WidgetInitializationMetadata data = new WidgetInitializationMetadata(widgetName: WIDGET_NAME1, initDataJson: INIT_JSON)
        metadata.store(data)
    }

    private void addColumnMetadata() {
        ColumnMetadata data1 = createColumnMetadata(DATABASE_NAME1, TABLE_NAME1, COLUMN_NAME1)
        ColumnMetadata data2 = createColumnMetadata(DATABASE_NAME1, TABLE_NAME1, COLUMN_NAME2)
        ColumnMetadata data3 = createColumnMetadata(DATABASE_NAME1, TABLE_NAME2, COLUMN_NAME1)
        ColumnMetadata data4 = createColumnMetadata(DATABASE_NAME2, TABLE_NAME2, COLUMN_NAME3)

        metadata.store(data1)
        metadata.store(data2)
        metadata.store(data3)
        metadata.store(data4)
    }

    private void addWidgetAndDatasetMetadata() {
        WidgetAndDatasetMetadata data1 = createWidgetAndDatasetMetadata(DATABASE_NAME1, TABLE_NAME1, ELEMENT_ID1, VALUE_1, WIDGET_NAME1)
        WidgetAndDatasetMetadata data2 = createWidgetAndDatasetMetadata(DATABASE_NAME1, TABLE_NAME2, ELEMENT_ID1, VALUE_2, WIDGET_NAME1)
        WidgetAndDatasetMetadata data3 = createWidgetAndDatasetMetadata(DATABASE_NAME1, TABLE_NAME2, ELEMENT_ID2, VALUE_2, WIDGET_NAME1)
        metadata.store(data1)
        metadata.store(data2)
        metadata.store(data3)
    }

    private static ColumnMetadata createColumnMetadata(String databaseName, String tableName, String columnName) {
        ColumnMetadata metadata = new ColumnMetadata(databaseName: databaseName,
                tableName: tableName, columnName: columnName)
        metadata.heterogeneous = true
        return metadata
    }

    private static WidgetAndDatasetMetadata createWidgetAndDatasetMetadata(String databaseName,
                                                                           String tableName, String elementId,
                                                                           String value, String widgetName) {
        return new WidgetAndDatasetMetadata(databaseName: databaseName, tableName: tableName, elementId: elementId,
                value: value, widgetName: widgetName)
    }


}
