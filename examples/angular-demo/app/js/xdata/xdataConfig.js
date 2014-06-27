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
 var XDATA = {};

 XDATA.ACTIVITY_LOGGER_URL = "http://xd-draper.xdata.data-tactics-corp.com:1337";
 XDATA.COMPONENT = "Neon Demo";
 XDATA.COMPONENT_VERSION = "0.8.0-SNAPSHOT";

 XDATA.activityLogger = new activityLogger('lib/draperlab/draper.activity_worker-2.1.1.js').echo(true).testing(false);

// Register the xdata logger with a server.
XDATA.activityLogger.registerActivityLogger(XDATA.ACTIVITY_LOGGER_URL, 
	XDATA.COMPONENT,
	XDATA.COMPONENT_VERSION);