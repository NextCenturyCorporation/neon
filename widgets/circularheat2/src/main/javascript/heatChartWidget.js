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

var HeatChartWidget = (function () {


	var widget = function(inHeatChartChannel) {

		var heatChartChannel = inHeatChartChannel || "com.nextcentury.everest.heatchart";

		//The Following functions are helper functions designed to get date ranges to send for 
		//OWF eventing.
		var setDateHourZero = function(time) {
			var tempTime = new Date(time);
			tempTime.setUTCHours(0);
			return tempTime;
		};

		var setDateHour24 = function(time) {
			var tempTime = new Date(time);
			tempTime.setUTCHours(24);
			return tempTime;
		};

		var addHours = function(time, h) {
			var tempTime = new Date(time);
			tempTime.setTime(tempTime.getTime() + (h * 60 * 60 * 1000));
			return tempTime;
		};

		var toLocaleHours = function(time) {
			var tempDate = new Date(time + " UTC");
			tempDate.setUTCMinutes(0);
			return tempDate;
		};

		var getFirstDateOfYear = function(date) {
			var tempDate = date;
			tempDate = new Date(tempDate.getFullYear(), 0, 1);
			tempDate.setUTCHours(0);
			return tempDate;
		};


		var getLastDateOfYear = function(date) {
			var tempDate = date;
			tempDate = new Date(tempDate.getFullYear(), 11, 31, 24);
			tempDate.setUTCHours(0);
			return tempDate;
		};

		var getFirstDateOfMonth = function(date) {
			var tempDate = date;
			tempDate = new Date(tempDate.getFullYear(), tempDate.getMonth(), 1);
			tempDate.setUTCHours(0);
			return tempDate;
		};

		var getLastDateOfMonth = function(date) {
			var tempDate = date;
			tempDate = new Date(tempDate.getFullYear(), tempDate.getMonth() + 1, 0, 24);
			tempDate.setUTCHours(0);
			return tempDate;
		};

		return {

			publishDateRange: function(mode, baseDate) {
				var checkOWF;

				if (OWF.Eventing.publish) {
					checkOWF = OWF.Eventing.publish;
				} else {
					checkOWF = function(message) {
						console.log("OWF Eventing API is not accessible.  The following was not published: ");
					};
				}
				switch (mode) {
					case "hour":
						checkOWF(self.heatChartChannel, JSON.stringify({
							startTime: toLocaleHours(baseDate),
							endTime: toLocaleHours(addHours(baseDate, 1))
						}));
						break;
					case "day":
						checkOWF(self.heatChartChannel, JSON.stringify({
							startTime: setDateHourZero(baseDate),
							endTime: setDateHour24(baseDate)
						}));
						break;
					case "month":
						checkOWF(self.heatChartChannel, JSON.stringify({
							startTime: getFirstDateOfMonth(baseDate),
							endTime: getLastDateOfMonth(baseDate)
						}));
						break;
					case "year":
						checkOWF(self.heatChartChannel, JSON.stringify({
							startTime: getFirstDateOfYear(baseDate),
							endTime: getLastDateOfYear(baseDate)
						}));
						break;
				}
			}

		};

	};

	return widget;

}());