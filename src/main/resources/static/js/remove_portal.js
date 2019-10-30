// 退出房间或被踢出房间后，不会收到服务器指令调用该函数
function removeChatItem(chatItemId) {
    var model = document.getElementById('user-data');
    let scope = angular.element(model).scope();
    scope.userData.chatList = _.filter(scope.userData.chatList, function (chatItem) {
        return chatItem.id !== chatItemId;
    });
    scope.userData.allChatItems = _.filter(scope.userData.allChatItems, function (chatItem) {
        return chatItem.id !== chatItemId;
    });
    scope.$apply();
}