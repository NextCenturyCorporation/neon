/*
 * Copyright 2013 Next Century Corporation
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

package com.ncc.neon.language

import com.ncc.neon.query.Query
import org.junit.Test




class QueryCreatorTest {

    @Test
    void "one instance of query creator creates two different queries"(){
        //The query creator has one instance in the Antlr Parser,
        // which has one instance in the Language service.
        // We need to be able to create multiple Query Objects from the same QueryCreator.
        QueryCreator creator = new QueryCreator()
        Query query1 = creator.createQuery()
        Query query2 = creator.createQuery()

        assert query1 != query2
    }

}
