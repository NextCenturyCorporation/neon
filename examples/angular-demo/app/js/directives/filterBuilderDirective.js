angular.module('filterBuilderDirective', []).directive('filterBuilder', ['ConnectionService', 
	function(connectionService) {

	return {
		templateUrl: 'partials/filterBuilder.html',
		restrict: 'EA',
		scope: {

		},
		link: function($scope, el, attr) {
			var messenger = new neon.eventing.Messenger();

			
		}
	}
}]);
