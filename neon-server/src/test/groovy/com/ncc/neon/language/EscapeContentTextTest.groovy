package com.ncc.neon.language

import org.junit.Test



class EscapeContentTextTest{

    @Test
    public void testEscapeContextText(){
        String text = "(test)"
        String result = QueryCreator.escapeContextText(text)
        assert "test" == result
    }

    @Test
    public void testEscapeContextTextWithoutParens(){
        String text = "test"
        String result = QueryCreator.escapeContextText(text)
        assert text == result
    }
}
