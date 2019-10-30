(function () {
    angular
        .module("module-index")
        .controller("cardCommentSmallController", cardCommentSmallController)
        .directive("wcCardCommentSm", wcCardCommentSmall)

    /* @ngInject */
    function cardCommentSmallController($scope, $window) {
        $scope.deleteComment               = $scope.$parent.deleteComment;
        $scope.likeComment                 = $scope.$parent.likeComment;
        $scope.replyComment                = $scope.$parent.replyComment;
    }

    /* @ngInject */
    function wcCardCommentSmall() {
        return {
            restrict: 'E',
            scope: {
                "current_card": "=currentCard",
                "comment": "=comment",
                "user": "=user"
            },
            controller: "cardCommentSmallController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-comment-sm.html';
            }
        }
    }
})();