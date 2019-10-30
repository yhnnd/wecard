(function () {
    angular
        .module("module-index")
        .controller("cardPreviewController", cardPreviewController)
        .directive("wcCardPreview", wcCardPreview)

    /* @ngInject */
    function cardPreviewController($scope) {
        $scope.viewCard = $scope.$parent.viewCard;
        $scope.likeCard = $scope.$parent.likeCard;
        $scope.cancelLikeCard = $scope.$parent.cancelLikeCard;
        $scope.shareCard = $scope.$parent.shareCard;
        $scope.deleteCard = $scope.$parent.deleteCard;
    }

    /* @ngInject */
    function wcCardPreview() {
        return {
            restrict: 'E',
            scope: {
                "card": "=card",
                "user": "=user"
            },
            controller: "cardPreviewController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-preview.html';
            }
        }
    }
})();