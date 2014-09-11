angular.module('neon.directives', [])
.directive('arrayTextForm', function() {
	return {
		restrict: "E",
		scope: {},
		templateUrl: "app/partials/directives/arrayTextForm.html",
		link: function( $scope ){
			$scope.fields = [""];

			$scope.addField = function() {
				$scope.strings.push("");
			};

			$scope.blur = function( $event, $index ){
				if($event.currentTarget.value === "" && $scope.fields.length > 1) {
					$scope.strings.splice($index, 1);
				} else {
					$scope.strings[ $index ] = $event.currentTarget.value;
				}
			};
		}
	};
})