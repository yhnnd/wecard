function removeFriend(friendId) {
    const model = document.querySelector("[ng-controller='controller']");
    let $scope = angular.element(model).scope();
    // 从好友分组中删除该好友
    $scope.userData.groups = _.map($scope.userData.groups, function (group) {
        group.friends = _.filter(group.friends, function (friend) {
            return friend.id !== friendId;
        });
        return group;
    });
    // 从聊天列表中删除该好友
    $scope.userData.chatList = _.filter($scope.userData.chatList, function (chatItem) {
        return chatItem.type !== "friend" || chatItem.friend.id !== friendId;
    });
    // 从联系人列表中删除该好友
    $scope.userData.allChatItems = _.filter($scope.userData.allChatItems, function (chatItem) {
        return chatItem.type !== "friend" || chatItem.friend.id !== friendId;
    });
    // 如果正在和该人聊天，应该退出聊天
    $scope.$apply();
}