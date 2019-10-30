(function () {
    angular
        .module("module-index")
        .controller("cardCommentXSController", cardCommentXSController)
        .directive("wcCardCommentXs", wcCardCommentXs)

    /* @ngInject */
    function cardCommentXSController($scope, $window) {
        $scope.deleteComment               = $scope.$parent.deleteComment;
        $scope.likeComment                 = $scope.$parent.likeComment;
        $scope.replyComment                = $scope.$parent.replyComment;
    }

    /* @ngInject */
    function wcCardCommentXs() {
        return {
            restrict: 'E',
            scope: {
                "current_card": "=currentCard",
                "comment": "=comment",
                "user": "=user"
            },
            controller: "cardCommentXSController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-comment-xs.html';
            }
        }
    }
})();