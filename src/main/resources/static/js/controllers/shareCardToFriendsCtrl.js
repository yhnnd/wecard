(function () {
    angular
        .module("module-index")
        .controller("shareCardToFriendsController", shareCardToFriendsController)
        .directive("wcShareCardToFriendsModal", wcShareCardToFriendsModal);

    /* @ngInject */
    function shareCardToFriendsController($scope) {

        $scope.friendsToShare = [];

        $scope.hideModal = function (event) {
            var button = event.target;
            $(button).closest(".modal").hide().removeClass("show");
            if (!$(".modal.show").length) {
                $("body").removeClass("modal-open");// restore background scroll
            }

            $scope.friendsToShare = [];
            $scope.userData.groups.forEach(group => {
                group.friends.forEach(friend => {
                    if (friend.selectedForShareCard === true) {
                        $scope.friendsToShare.push(friend);
                    }
                })
            })
            $scope.$parent.shareCardToFriends_friendsSelected($scope.friendsToShare);
        };
    }

    /* @ngInject */
    function wcShareCardToFriendsModal() {
        return {
            restrict: 'E',
            scope: {
                "userData": "=userData"
            },
            controller: "shareCardToFriendsController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/share-card-to-friends-modal.html';
            }
        }
    }
})();