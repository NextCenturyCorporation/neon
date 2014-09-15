angular.module('arrayTextFormDirective', [])
.directive('arrayTextForm', function() {
	return {
		restrict: "E",
		scope: {
			fields: '='
		},
		templateUrl: "app/partials/directives/arrayTextForm.html",
		link: function( $scope ){
			$scope.addField = function() {
				$scope.fields.push("");
			};

			$scope.blur = function( $event, $index ){
				if($event.currentTarget.value === "" && $scope.fields.length > 1) {
					$scope.fields.splice($index, 1);
				} else {
					$scope.fields[ $index ] = $event.currentTarget.value;
				}
			};
		}
	};
})