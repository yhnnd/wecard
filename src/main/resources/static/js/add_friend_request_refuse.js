// 拒绝加好友请求（通用接口，也能用于拒绝加入聊天室的请求）
function refuseAddFriendRequest(request, button) {
    if (request && request.id) {
        $.ajax({
            type: "get",
            url: window.getHttpRoot() + apis.addFriend.refuse,
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
                        bsAlert("[ERROR] 请求 id 不能为空");
                        break;
                    case "Request does not exist":
                        bsAlert("[ERROR] 请求不存在");
                        break;
                    case "Request type error":
                        bsAlert("[ERROR] 请求类型错误");
                        break;
                    case "Request permission error":
                        bsAlert("[ERROR] 无权限访问该请求");
                        break;
                    case "Request has been replied":
                        bsAlert("[ERROR] 该请求已经被回复");
                        break;
                    case "Refuse fail":
                        bsAlert("[ERROR] 拒绝请求失败");
                        break;
                    case "Refuse success":
                        bsAlert("refuse add friend request:","已拒绝该请求","alert-primary");
                        $(button).attr("disabled", true);
                        break;
                    default:
                        bsAlert("refuse add friend request:","未知错误: " + data.message,"alert-danger");
                        break;
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                bsAlert("[ERROR] refuse add friend request: " + XMLHttpRequest.status);
            }
        });
    } else {
        bsAlert("[ERROR] refuse add friend request: request id 不存在");
    }
}