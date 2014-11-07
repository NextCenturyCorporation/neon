/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.common

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.Exec

import java.lang.reflect.Method

/**
 * GruntTask adopted from http://naleid.com/blog/2013/01/24/calling-gruntjs-tasks-from-gradle/
 */
class GruntTask extends Exec {
    static final String GRUNT_EXECUTABLE = Os.isFamily(Os.FAMILY_WINDOWS) ? "grunt.cmd" : "grunt"

    private static final Method PARENT_EXEC_METHOD

    boolean background = false

    GruntTask() {
        executable = GRUNT_EXECUTABLE
    }

    void gruntArgs(Object... gruntArgs) {
        this.args = gruntArgs as List
    }

    // this is a groovy hack that lets us override exec, but it will let us run the task in the background
    void exec() {
        def runnable = {super.exec()} as Runnable
        if (background) {
            Thread t = new Thread(runnable)
            t.daemon = true
            t.start()
        } else {
            runnable.run()
        }

    }

}