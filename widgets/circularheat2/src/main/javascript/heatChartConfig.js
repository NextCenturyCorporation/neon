var HeatChartConfig = HeatChartConfig || {};

(function() {

	var config = {
		dataType: 'jsonp',
		baseUrl: 'http://everest-build:8081',
		jsonpCallback: 'callback'
	};

	HeatChartConfig.get = function() {
		return config;
	}

}());