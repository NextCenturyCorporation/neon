package com.ncc.neon.language

import com.ncc.neon.query.Query



public interface QueryParser{

    Query parse(String text)
}