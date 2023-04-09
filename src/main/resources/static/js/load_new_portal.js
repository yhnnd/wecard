// 加载新的聊天项
function loadNewChatItem(chatItemId) {
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
            console.log("load new chat item: " + JSON.stringify(data));
            switch (data.message) {
                case "Find success":
                    if (!data.chatItem) {
                        bsAlert("[ERROR] load new chat item (" + chatItemId + "): 未找到聊天项");
                    } else {
                        const model = document.querySelector("[ng-controller='controller']");
                        let scope = angular.element(model).scope();
                        if (data.chatItem.type === "friend") {
                            data.chatItem.message = { type: "text", text: "我们已经是好友了，开始聊天吧" };
                        } else if (data.chatItem.type === "room") {
                            data.chatItem.message = { type: "text", text: "你已经是房间成员了，开始聊天吧" };
                        }
                        scope.userData.chatList.unshift(data.chatItem);
                        scope.userData.allChatItems.push(data.chatItem);
                        scope.$apply();
                    }
                    break;
                case "ChatItem does not exist":
                    bsAlert("[ERROR] load new chat item (" + chatItemId + "): 聊天项不存在");
                    break;
                case "ChatItem permission error":
                    bsAlert("[ERROR] load new chat item (" + chatItemId + "): 没有权限访问该聊天项");
                    break;
                default:
                    bsAlert("[ERROR] load new chat item (" + chatItemId + "): " + data.message);
                    break;
            }
        },
        error: function (data) {
            console.log("[ERROR] load new chat item (" + chatItemId + "): " + JSON.stringify(data));
        }
    });
}