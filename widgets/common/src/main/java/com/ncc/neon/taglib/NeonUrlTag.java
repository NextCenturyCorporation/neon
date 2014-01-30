package com.ncc.neon.taglib;


import com.ncc.neon.NeonPropertiesLoader;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class NeonUrlTag extends SimpleTagSupport {

    public void doTag() throws JspException, IOException {
        NeonPropertiesLoader properties = new NeonPropertiesLoader();
        JspWriter out = getJspContext().getOut();
        String url = properties.getNeonUrl();
        out.println(url);
    }
}