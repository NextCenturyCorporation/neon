package com.ncc.neon.user_import

import groovy.transform.ToString

@ToString(includeNames = true)

public class UserFieldDataBundle {
    String format
    List<FieldTypePair> fields
}