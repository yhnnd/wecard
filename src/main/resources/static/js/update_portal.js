// 更新聊天项
function updateChatItem(chatItemId) {
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.getPortal,
        data: {
            "chatItemId": chatItemId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            console.log("update chat item: " + JSON.stringify(data));
            switch (data.message) {
                case "Find success":
                    const model = document.querySelector("[ng-controller='controller']");
                    let scope = angular.element(model).scope();
                    scope.userData.chatList = _.filter(scope.userData.chatList, function (chatItem) { return chatItem.id !== chatItemId; });
                    scope.userData.chatList.unshift(data.chatItem);
                    scope.$apply();
                    break;
                case "ChatItem does not exist":
                    bsAlert("[ERROR] update chat item (" + chatItemId + "): 聊天项不存在");
                    break;
                case "ChatItem permission error":
                    bsAlert("[ERROR] update chat item (" + chatItemId + "): 没有权限访问该聊天项");
                    break;
                default:
                    bsAlert("[ERROR] update chat item (" + chatItemId + "): " + data.message);
                    break;
            }
        },
        error: function (data) {
            console.log("[ERROR] update chat item (" + chatItemId + "): " + JSON.stringify(data));
        }
    });
}