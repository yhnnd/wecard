// 同意加入房间请求
function agreeJoinRoomRequest(request, button) {
    if (request && request.id) {
        $.ajax({
            type: "get",
            url: window.getHttpRoot() + apis.joinRoom.agree,
            data: {
                "requestId": request.id
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
                    case "RequestId cannot be empty":
                        bsAlert("请求 id 不能为空");
                        break;
                    case "Request does not exist":
                        bsAlert("请求不存在");
                        break;
                    case "Request permission error":
                        bsAlert("无权限访问该请求");
                        break;
                    case "Request has been refused":
                        bsAlert("该请求已经被拒绝");
                        break;
                    case "Request has been agreed":
                        bsAlert("该请求已经被同意");
                        break;
                    case "Requester have been a member of this room":
                        bsAlert("该用户已经是房间成员了");
                        break;
                    case "Agree fail":
                        bsAlert("无法同意该请求");
                        break;
                    case "Agree success":
                        // bsAlert("已同意该请求");
                        hidePopover($(button).closest('.popover'));
                        break;
                    default:
                        bsAlert("agree join room request 未知错误: " + data.message);
                        break;
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                bsAlert("[ERROR] agree join room request: " + XMLHttpRequest.status);
            }
        });
    } else {
        bsAlert("agree join room request: request id 不存在");
    }
}