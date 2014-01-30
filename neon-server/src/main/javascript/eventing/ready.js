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



/**
 * Stubs out OWF if it does not exist, and provides neon.ready()
 * which widgets should use before doing any OWF related work.
 * @class neon
 * @static
 */

if (typeof (OWF) === "undefined" || !OWF.Util.isRunningInOWF()) {
    window.OWF = {
        getIframeId: function () {
            return null;
        },
        getInstanceId: function () {
            return null;
        },
        ready: function(fnToExecute){
            fnToExecute();
        },
        Eventing: {
            publish: function () {
            },
            subscribe: function () {
            }
        }
    };
}

/**
 * Runs a function after the dom is loaded
 * @param functionToRun  the function to run.
 * @method ready
 */

neon.ready = function(functionToRun){
  $(function(){
     OWF.ready(functionToRun);
  });
};