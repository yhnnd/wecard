(function () {
    angular
        .module("module-index")
        .controller("shareCardController", shareCardController)
        .directive("wcShareCardModal", wcShareCardModal);

    angular
        .module("module-index")
        .service('shareCardService', shareCardService);

    /* @ngInject */
    function shareCardService($window) {
        // 分享（转发）卡片 - 开始
        this.shareCard = function (card, $scope) {
            if ($scope.user) {
                // $("#modal-share-card").modal("show");
                $("#modal-share-card").show().addClass("show");
                $("body").addClass("modal-open");// prevent background scroll
                $("#shared-card-title").text(card.title);
                $("#shared-card-text").text(card.text.substr(0,20) + " ...");// 被转发的卡片的正文
                $("#share-card-text").text("").val("").attr("placeholder", "转发卡片");
                $("#share-card-submit")
                    .removeClass("btn-success")
                    .addClass("btn-primary")
                    .text("转发卡片")
                    .attr("onclick", "shareCardSubmit('" + card.id + "', $('#share-card-text').val());");
            } else {
                $window.login('../index.html');
            }
        };
    }

    /* @ngInject */
    function shareCardController($scope, $window) {
        // Hide Share Card Modal
        $scope.hideShareCardModal = function (event) {
            var button = event.target;
            $(button).closest(".modal").hide().removeClass("show");
            if (!$(".modal.show").length) {
                $("body").removeClass("modal-open");// restore background scroll
            }
        };

        $scope.friendsToShare = [];

        $scope.$on("share_card_to_friends_friends_selected", function(event, data) {
            $scope.friendsToShare = data;
        });

        $scope.selectFriendsToShare = function () {
            $("#modal-share-card-to-friends").show().addClass("show");
        };

        $scope.removeFriendToShare = function (friend) {
            $scope.friendsToShare = _.filter($scope.friendsToShare, function(fr) {
                return fr.id !== friend.id;
            });
            $scope.userData.groups = $scope.userData.groups.map(group => {
                group.friends = group.friends.map(fr => {
                    if (fr.id === friend.id) {
                        fr.selectedForShareCard = false;
                    }
                    return fr;
                });
                return group;
            });
        };

        $scope.shareCardSubmit = $window.shareCardSubmit;
    }

    /* @ngInject */
    function wcShareCardModal() {
        return {
            restrict: 'E',
            scope: {
                "user": "=user",
                "userData": "=userData"
            },
            controller: "shareCardController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/share-card-modal.html';
            }
        }
    }
})();