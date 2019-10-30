// 加载当前聊天窗口中新的消息
function loadNewMessage(messageId) {
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.get.message,
        data: {
            "messageId": messageId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            // console.log("load new message: " + JSON.stringify(data));
            switch (data.result) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "Message does not exist":
                    bsAlert("load new message: 消息不存在");
                    break;
                case "MessageId cannot be empty":
                    bsAlert("load new message: 消息 ID 不能为空");
                    break;
                case "Find success":
                    // 观察用户是否在浏览最新消息（必须在载入新消息之前调用）
                    let is_viewing_last_message = isViewingLastMessage();

                    let model = document.getElementById('chat-message-list');
                    let $scope = angular.element(model).scope();
                    if ($scope.userData.user) {
                        // bsAlert("load new message: " + JSON.stringify(data));
                        // 解析消息的发送者
                        if (data.message.roomMember) {
                            data.message.user = data.message.roomMember;
                        } else if (data.message.friend) {
                            data.message.user = data.message.friend;
                        }
                        // 将消息中的 URL 网址转换成 a 链接
                        data.message.url = append_message_linkable(data.message.text);

                        // 清空聊天窗口中的假消息（正在发送中的消息）
                        $scope.chatMessageList = $scope.chatMessageList.filter((message) => {
                            if (!message.id || message.id.startsWith("fake-id-") || message.sendStatus) {
                                return false;
                            } else {
                                return true;
                            }
                        });

                        // 将新消息载入到聊天窗口中
                        $scope.chatMessageList.push(data.message);
                        $scope.$apply();
                        
                        // 自动滚动聊天窗口（如果消息发送者是我，或者我正在浏览最新消息，就自动滚动窗口到最底部，否则不滚动窗口而是增加未读消息数）
                        if (data.message.user) {// 消息发送者是我
                            if (data.message.user.id === $scope.userData.user.id) {
                                gotoLastMessage(true);
                            } else if (is_viewing_last_message) {// 用户（我）正在浏览最新消息
                                gotoLastMessage(true);
                            } else {// 用户（我）正在查看历史消息
                                $scope.unread_message_count++;
                                $scope.$apply();
                            }
                        } else {
                            console.log("[ERROR] load new message: 收到一个格式错误的消息，该消息没有发送者");
                        }
                    } else {
                        bsAlert("load new message: 用户未登录，无法读取新消息");
                    }
                    break;
            }
        },
        error: function (data) {
            bsAlert("[ERROR] load new message: 服务器错误。错误代码：" + data.status);
        }
    });
}
