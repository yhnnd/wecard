// 我退出群，群主解散群，我被移出群都会收到系统指令调用此函数
function removeRoom(roomId) {
    var model = document.getElementById('user-data');
    let scope = angular.element(model).scope();
    scope.userData.rooms = _.filter(scope.userData.rooms, function (room) {
        return room.id !== roomId;
    });
    scope.userData.chatList = _.filter(scope.userData.chatList, function (chatItem) {
        return chatItem.type !== "room" || chatItem.room.id !== roomId;
    });
    scope.userData.allChatItems = _.filter(scope.userData.allChatItems, function (chatItem) {
        return chatItem.type !== "room" || chatItem.room.id !== roomId;
    });
    if (scope.current_room.id === roomId) {
        scope.goBack();
        // bsAlert("你已不是房间成员");
    }
    scope.$apply();
}