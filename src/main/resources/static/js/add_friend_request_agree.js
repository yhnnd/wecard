// 同意加好友请求
function agreeAddFriendRequest(request, button) {
    if (request && request.id) {
        let field = $(button).closest(".field");
        let rows = field.find(".row");
        let remark = rows.eq(0).find("input").val();
        let groupId = rows.eq(1).find("select").val();
        if (groupId) {
            $.ajax({
                type: "get",
                url: window.getHttpRoot() + apis.addFriend.agree,
                data: {
                    "requestId": request.id,
                    "groupId": groupId,
                    "remark": remark
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
                        // request id error
                        case "RequestId cannot be empty":
                            bsAlert("[ERROR] 请求 id 不能为空");
                            break;
                        // group error
                        case "GroupId cannot be empty":
                            bsAlert("[ERROR] 分组不能为空");
                            break;
                        case "Group does not exist":
                            bsAlert("[ERROR] 分组不存在");
                            break;
                        case "Group permission error":
                            bsAlert("[ERROR] 无权限访问该分组");
                            break;
                        // remark error
                        case "Remark contains special char":
                            bsAlert("[ERROR] 备注名含有非法字符");
                            break;
                        case "Remark is too long":
                            bsAlert("[ERROR] 备注名过长");
                            break;
                        // user error
                        case "Receiver is yourself":
                            bsAlert("[ERROR] 不能加自己为好友");
                            break;
                        case "You have already been friends":
                            bsAlert("agree add friend request:","你已经是该用户的好友","alert-info");
                            break;
                        // request error
                        case "Request does not exist":
                            bsAlert("[ERROR] 请求不存在");
                            break;
                        case "Request permission error":
                            bsAlert("[ERROR] 无权限访问该请求");
                            break;
                        case "Request has been refused":
                            bsAlert("[ERROR] 该请求已经被拒绝");
                            break;
                        case "Request has been agreed":
                            bsAlert("[ERROR] 该请求已经被同意");
                            break;
                        case "Agree fail":
                            bsAlert("[ERROR] 无法同意该请求");
                            break;
                        case "Agree success":
                            // bsAlert("已同意该请求");
                            hidePopover($(button).closest('.popover'));
                            break;
                        default:
                            bsAlert("[ERROR] agree add friend request:", "未知错误: " + data.message);
                            break;
                    }
                },
                error: function (XMLHttpRequest, textStatus) {
                    bsAlert("[ERROR] agree add friend request: " + XMLHttpRequest.status);
                }
            });
        } else {
            bsAlert("[ERROR] 请选择分组");
        }
    } else {
        bsAlert("[ERROR] agree add friend request: request id 不存在");
    }
}