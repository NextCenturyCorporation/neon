package com.ncc.neon.state

import org.junit.Test




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
