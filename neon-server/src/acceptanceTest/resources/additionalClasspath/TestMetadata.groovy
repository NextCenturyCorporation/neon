/*
 * Copyright 2016 Next Century Corporation
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
metadata {
    init {
        widget1 {
            initDataJson = '{"key1":"value1"}'
        }
    }
    columns {
        database1 {
            table1 {
                field1 {
                    numeric = true
                    logical = true
                    temporal = true
                    array = true
                    object = true
                    text = true
                    heterogeneous = true
                    nullable = true
                }
                field2 {
                    numeric = false
                    logical = false
                    temporal = false
                    array = false
                    object = false
                    text = false
                    heterogeneous = false
                    nullable = false
                }
            }
        }
    }
    widgets {
        database1 {
            table1 {
                widget1 {
                    aSelector {
                        value = "someValue"
                    }
                }
            }
        }
    }
}
