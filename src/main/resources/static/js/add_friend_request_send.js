// 发送加好友请求
function sendAddFriendRequest(friend, button) {
    let field = $(button).closest(".field");
    let rows = field.find(".row");
    let requestMessage = rows.eq(0).find("textarea").val();
    let remark = rows.eq(1).find("input").val();
    let groupId = rows.eq(2).find("select").val();
    let receiverUsername = friend.username;
    // alert(requestMessage + "\n" + remark + "\n" + groupId + "\n" + receiverUsername);
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.addFriend.send,
        data: {
            "username": receiverUsername,
            "groupId": groupId,
            "remark": remark,
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
                // username error
                case "Username cannot be empty":
                    bsAlert("用户名不能为空");
                    break;
                // group error
                case "GroupId cannot be empty":
                    bsAlert("分组不能为空");
                    break;
                case "Group does not exist":
                    bsAlert("分组不存在");
                    break;
                case "Group permission error":
                    bsAlert("无权限访问该分组");
                    break;
                // remark error
                // case "Remark cannot be empty":
                //     bsAlert("备注名不能为空");
                //     break;
                case "Remark contains special char":
                    bsAlert("备注名包含非法字符");
                    break;
                case "Remark is too long":
                    bsAlert("备注名过长");
                    break;
                // user error
                case "Receiver does not exist":
                    bsAlert("该用户不存在");
                    break;
                case "Receiver is yourself":
                    bsAlert("不能加自己为好友");
                    break;
                case "You have already been friends":
                    bsAlert("你已经是该用户的好友");
                    break;
                // request error
                case "Send fail":
                    bsAlert("发送请求失败");
                    break;
                case "Send success":
                    bsAlert("send add friend request", "发送请求成功", "alert-success");
                    hidePopover($(button).closest('.popover'));
                    break;
                // default
                default:
                    bsAlert("[ERROR] send add friend request: 未知错误: " + data.message);
                    break;
            }
        },
        error: function (XMLHttpRequest, textStatus) {
            bsAlert("[ERROR] send add friend request: 错误码: " + XMLHttpRequest.status);
        }
    });
}