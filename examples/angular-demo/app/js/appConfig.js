'use strict';
/*
 * Copyright 2014 Next Century Corporation
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

var DEMO_CONFIG = {};
DEMO_CONFIG.xdata = {};
// Set this to true to see what messages the application would log to the server
// Warning: This is very verbose
DEMO_CONFIG.xdata.echoToConsole = false;
// Set this to true to enable remove logging of the applications actions
DEMO_CONFIG.xdata.enableLogging = false;
// Change this to log to a different server
DEMO_CONFIG.xdata.ACTIVITY_LOGGER_URL = "http://xd-draper.xdata.data-tactics-corp.com:1337";
// Some XDATA logger settings that shouldn't need to be changed frequently
DEMO_CONFIG.xdata.COMPONENT = "Neon Demo";
DEMO_CONFIG.xdata.COMPONENT_VERSION = "0.8.0-SNAPSHOT";


DEMO_CONFIG.opencpu = {};
// Set this to true to use OpenCPU to do on-the-fly analysis of the data. Make sure
// the url variable is configured correctly.
DEMO_CONFIG.opencpu.enableOpenCpu = false;
// If enableOpenCpu is true, this must be the URL of an open cpu server that has the
// NeonAngularDemo R package installed (see the app/R/NeonAngularDemo directory)
DEMO_CONFIG.opencpu.url = 'http://neon-opencpu/ocpu/library/NeonAngularDemo/R';
// opencpu logging is off to keep the logs clean, turn it on to debug opencpu problems
DEMO_CONFIG.opencpu.enableLogging = false;
// By default, opencpu uses alerts when there are problems. We want to handle the errors gracefully instead
DEMO_CONFIG.opencpu.useAlerts = false;
