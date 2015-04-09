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

import org.junit.Test

class ConfigWriterTest {


    @Test
    void "written configuration can be read back into ConfigObject"() {

        ConfigObject obj = new ConfigObject(v:10d, w:5f, x:2, y:"str", z:true)
        def map = [a:1, b:2, c:obj, g:"ignoreMe"]
        def excludes = ["g"] as Set

        // write the config and read it back to test it
        String config  = new ConfigWriter(excludes).writeConfig("config", map)

        def restored = new ConfigSlurper().parse(config)["config"]

        assert restored.size() == 3
        assert restored["a"] == 1
        assert restored["b"] == 2
        assert restored["c"] == [v:10d, w:5f, x:2, y:"str", z:true]

    }


}
