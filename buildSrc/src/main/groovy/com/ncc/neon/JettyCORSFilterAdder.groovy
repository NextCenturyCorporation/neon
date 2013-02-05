package com.ncc.neon

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
 */

/**
 * Appends a filter to an existing web.xml that allows cross domain requests on jetty
 */
class JettyCORSFilterAdder {


    private static final def FILTER_XML = '''
 <filter>
   <filter-name>accept-all</filter-name>
   <filter-class>com.ncc.neon.AcceptAllRequestsServletFilter</filter-class>
 </filter>
 '''
    private static final def FILTER_MAPPING_XML = '''
<filter-mapping>
    <filter-name>accept-all</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
'''



    static def rewriteWebXml(inputFile, outputFile) {
        def filter = new XmlSlurper(false, false).parseText(FILTER_XML)
        def filterMapping = new XmlSlurper(false, false).parseText(FILTER_MAPPING_XML)
        def root = new XmlSlurper(false, false).parse(new File(inputFile))
        root.appendNode(filter)
        root.appendNode(filterMapping)

        def newXml = groovy.xml.XmlUtil.serialize(root)

        def out = new File(outputFile);
        out.delete();
        out << newXml;
    }

}


