// 添加新的聊天室
function loadNewChatRoom(roomId) {
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.getRoom,
        data: {
            "roomId": roomId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            switch (data.message) {
                case "Find success":
                    if (!data.room) {
                        bsAlert("[ERROR] load new chatRoom (" + roomId + "): 未找到该聊天室");
                    } else {
                        console.log("load new chatRoom: " + JSON.stringify(data));
                        const model = document.querySelector("[ng-controller='controller']");
                        let scope = angular.element(model).scope();
                        scope.userData.rooms.push(data.room);
                        scope.$apply();
                    }
                    break;
                case "Find failed":
                    bsAlert("[ERROR] load new chatRoom (" + roomId + "): 无法找到该房间。错误信息: " + data.message);
                    break;
                default:
                    bsAlert("[ERROR] load new chatRoom (" + roomId + "): " + data.message);
                    break;
            }
        },
        error: function (data) {
            bsAlert("[ERROR] load new chatRoom (" + roomId + "): 服务器错误。错误代码: " + data.message);
        }
    });
}