(function () {
    angular
        .module("module-index")
        .controller("sharedCardPreviewController", sharedCardPreviewController)
        .directive("wcSharedCardPreview", wcSharedCardPreview)

    /* @ngInject */
    function sharedCardPreviewController($scope) {
        $scope.viewCard = $scope.$parent.viewCard;
        $scope.likeCard = $scope.$parent.likeCard;
        $scope.cancelLikeCard = $scope.$parent.cancelLikeCard;
        $scope.shareCard = $scope.$parent.shareCard;
        $scope.deleteCard = $scope.$parent.deleteCard;
    }

    /* @ngInject */
    function wcSharedCardPreview() {
        return {
            restrict: 'E',
            scope: {
                "card": "=card",
                "user": "=user"
            },
            controller: "sharedCardPreviewController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/shared-card-preview.html';
            }
        }
    }
})();