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

import org.yaml.snakeyaml.*

/**
 * A helper class for parsing the jstestdriver config file and generating script tags to use
 * for the jasmine spec runner based on the jstestdriver config
 */
class JsTestDriverConfigParser {


    /** a tag in a javascript file that indicates it is the minified version */
    private static final String MINIFIED_INDICATOR = '-min'

    /** the files that match those from the jstestdriver config file spec */
    def matchingFiles = [] as LinkedHashSet

    /**
     * Parses the JsTestDriver config file and stores any files that match the paths in that file
     * in the matchingFiles set
     * @param projectDir The gradle project directory
     * @param jsTestDriverConfigFileRoot The directory relative to the project dir where the JsTestDriver config
     * file is stored
     * @param jsTestDriverConfigFileName The name of the JsTestDriver config file
     * @param excludes Any file names to exclude from matching files list
     */
    void parseJsTestDriverConfig(projectDir,
                                 jsTestDriverConfigFileRoot,
                                 jsTestDriverConfigFileName,
                                 excludes = [] as Set) {

        matchingFiles.clear()
        File jsTestDriverConfigFile = new File(new File(projectDir, jsTestDriverConfigFileRoot), jsTestDriverConfigFileName)
        def yaml = new Yaml().load(new FileInputStream(jsTestDriverConfigFile))
        def allExcludes = [] as Set
        allExcludes.addAll(excludes);
        if ( yaml.exclude ) {
            allExcludes.addAll(yaml.exclude)
        }
        def files = [];
        files.addAll(yaml.load)
        files.addAll(yaml.test)
        files.each { libPath ->
            def file = new File(libPath)
            def name = file.name
            if (name.indexOf('*') >= 0) {
                addMatchingFiles(projectDir, file, jsTestDriverConfigFileRoot)
            } else  {
                matchingFiles.add(libPath)
            }
        }
        matchingFiles.removeAll {
            def removed = allExcludes.contains(it)
            return removed
        }

        removeMinifiedDuplicates()
    }

    private def addMatchingFiles(projectDir, file, jsTestDriverConfigFileRoot) {

        // replace . with the escaped period (note we need to use \\. as part of the first arg since
        // that param is a regex, and then we replace it with a literal \\. to escape it in the regex).
        // then replace any file wildcard * with the regex match all characters
        def regex = ~(file.name.replaceAll('\\.', '\\.').replaceAll('\\*', '.*'))
        def libDir = new File(new File(projectDir, jsTestDriverConfigFileRoot), file.parentFile.path)
        libDir.eachFileRecurse { libFile ->
            if (libFile.name =~ regex) {
                def path = libFile.absolutePath
                matchingFiles.add(path.substring(path.indexOf(jsTestDriverConfigFileRoot) + jsTestDriverConfigFileRoot.length() + 1))
            }
        }
    }

    private def removeMinifiedDuplicates() {
        matchingFiles.removeAll { filename ->
            // if the file is a minified version and has a corresponding non minified version, remove it
            def index = filename.lastIndexOf(MINIFIED_INDICATOR)
            if ( index >= 0 ) {
                // some of the libraries strip off the min, and some replace it with "debug"
                def nonMinifiedVersion = filename.substring(0, index) + filename.substring(index + MINIFIED_INDICATOR.length())
                def debugVersion = filename.substring(0, index) + "-debug" + filename.substring(index + MINIFIED_INDICATOR.length())
                return [nonMinifiedVersion,debugVersion].any { matchingFiles.contains(it) }
            }
            return false
        }
    }

}
