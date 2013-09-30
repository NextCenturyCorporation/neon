package com.ncc.neon.session

import org.junit.Test


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

class WidgetStateTest {

    private static final String ID_1 = "1"
    private static final String ID_2 = "2"
    private static final String CONTENT_1 = "content1"
    private static final String CONTENT_2 = "content2"

    @Test
    void "ids must be equal for WidgetStates to be equal"(){
        WidgetState state1 = new WidgetState(ID_1, CONTENT_1)
        WidgetState state2 = new WidgetState(ID_1, CONTENT_1)

        assert state1 == state2

        state2 = new WidgetState(ID_1, CONTENT_2)

        assert state1 == state2
    }

    @Test
    void "if the ids are not equal the widget states are not equal"(){

        WidgetState state1 = new WidgetState(ID_1, CONTENT_1)
        WidgetState state2 = new WidgetState(ID_2, CONTENT_1)

        assert state1 != state2

        state2 = new WidgetState(ID_2, CONTENT_2)

        assert state1 != state2

    }

}
