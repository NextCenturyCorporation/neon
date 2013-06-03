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
 * Generates the list of spec file includes for a jasmine spec runner
 */
class JasmineIncludesGenerator {

    /** a tag in a javascript file that indicates it is the minified version */
    private static final String MINIFIED_INDICATOR = '-min'

    /** the files that match the provided patterns  */
    def matchingFiles = [] as LinkedHashSet

    /**
     * Generates the include patterns for the javascript spec file
     * @param projectDir The gradle project directory
     * @param testRoot The root directory for the javascript tests. Note the source file patterns should
     * be relative to here
     */
    void generateIncludes(projectDir,
                          testRoot) {

        matchingFiles.clear()
        def files = [];
        files.addAll('spec/*.spec.js')
        files.each { libPath ->
            def file = new File(libPath.replaceAll("\"", ""))
            def name = file.name
            if (name.indexOf('*') >= 0) {
                addMatchingFiles(projectDir, file, testRoot)
            } else {
                matchingFiles.add(libPath)
            }
        }
    }

    private def addMatchingFiles(projectDir, file, testRoot) {
        // replace . with the escaped period (note we need to use \\. as part of the first arg since
        // that param is a regex, and then we replace it with a literal \\. to escape it in the regex).
        // then replace any file wildcard * with the regex match all characters
        def regex = ~(file.name.replaceAll('\\.', '\\.').replaceAll('\\*', '.*'))
        def libDir = new File(new File(projectDir, testRoot), file.parentFile.path)
        libDir.eachFileRecurse { libFile ->
            if (libFile.name =~ regex) {
                def path = libFile.absolutePath
                matchingFiles.add(path.substring(path.indexOf(testRoot) + testRoot.length() + 1))
            }
        }
    }

}
