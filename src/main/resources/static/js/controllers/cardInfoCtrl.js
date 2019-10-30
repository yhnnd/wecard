(function () {
    angular
        .module("module-index")
        .controller("cardInfoController", cardInfoController)
        .directive("wcCardInfo", wcCardInfo)

    /* @ngInject */
    function cardInfoController($scope, $window) {
        // setTimeout(function () {
        //     $window.alert(JSON.stringify($scope.current_card));
        // },5000);
    }

    /* @ngInject */
    function wcCardInfo() {
        return {
            restrict: 'E',
            scope: {
                "current_card": "=currentCard"
            },
            controller: "cardInfoController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-info.html';
            }
        }
    }
})();