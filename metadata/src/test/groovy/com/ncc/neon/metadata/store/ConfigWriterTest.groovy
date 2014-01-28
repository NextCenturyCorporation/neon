package com.ncc.neon.metadata.store

import org.junit.Test

/*
 * ************************************************************************
 * Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
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
 */
class ConfigWriterTest {


    @Test
    void "written configuration can be read back into ConfigObject"() {

        ConfigObject obj = new ConfigObject(v:10d, w:5f, x:2, y:"str", z:true)
        def map = [a:1,b:2,c:obj,g:"ignoreMe"]
        def excludes = ["g"] as Set

        // write the config and read it back to test it
        String config  = new ConfigWriter(excludes).writeConfig("config",map)

        def restored = new ConfigSlurper().parse(config)["config"]

        assert restored.size() == 3
        assert restored["a"] == 1
        assert restored["b"] == 2
        assert restored["c"] == [v:10d, w:5f, x:2, y:"str", z:true]

    }


}
