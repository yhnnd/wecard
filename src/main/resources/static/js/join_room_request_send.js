// 发送加入房间请求
function sendJoinRoomRequest(room, button) {
    let field = $(button).closest(".field");
    let rows = field.find(".row");
    let requestMessage = rows.eq(0).find("textarea").val();
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.joinRoom.send,
        data: {
            "roomId": room.id,
            "requestMessage": requestMessage
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            switch (data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                // room error
                case "RoomId cannot be empty":
                    bsAlert("[ERROR] 房间 id 不能为空");
                    break;
                case "Room does not exist":
                    bsAlert("[ERROR] 房间不存在");
                    break;
                // request error
                case "Send fail":
                    bsAlert("[ERROR] 发送请求失败");
                    break;
                case "Send success":
                    bsAlert("send join room request:", "发送请求成功", "alert-success");
                    hidePopover($(button).closest('.popover'));
                    break;
                // default
                default:
                    bsAlert("[ERROR] send join room request: 未知错误 " + data.message);
                    break;
            }
        },
        error: function (XMLHttpRequest, textStatus) {
            bsAlert("[ERROR] send join room request: " + XMLHttpRequest.status);
        }
    });
}