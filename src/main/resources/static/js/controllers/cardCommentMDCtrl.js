(function () {
    angular
        .module("module-index")
        .controller("cardCommentMDController", cardCommentMDController)
        .directive("wcCardCommentMd", wcCardCommentMd)

    /* @ngInject */
    function cardCommentMDController($scope, $window, $timeout, $interval) {
        $scope.deleteComment               = $scope.$parent.deleteComment;
        $scope.likeComment                 = $scope.$parent.likeComment;
        $scope.replyComment                = $scope.$parent.replyComment;
        $scope.blink = function ($event) {
            let target = $($event.target);
            let anchor = target.attr("href");
            if (anchor == null) {
                anchor = target.closest('a').attr('href');
            }
            if (anchor) {
                let inputArea = $(anchor).closest('.reply-comment-input-area');
                let i = 0;
                let timer = $interval(function () {
                    inputArea.toggleClass('alert-info');
                    if (++i === 4) {
                        $interval.cancel(timer);
                    }
                }, 100);
            }
        }
    }

    /* @ngInject */
    function wcCardCommentMd() {
        return {
            restrict: 'E',
            scope: {
                "current_card": "=currentCard",
                "comment": "=comment",
                "user": "=user"
            },
            controller: "cardCommentMDController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-comment-md.html';
            }
        }
    }
})();