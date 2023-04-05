function updateMessage(messageId) {
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.get.message,
        data: {
            "messageId": messageId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            // console.log("update message: " + JSON.stringify(data));
            switch (data.result) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "Message does not exist":
                    bsAlert("update new message: 消息不存在");
                    break;
                case "MessageId cannot be empty":
                    bsAlert("update new message: 消息 ID 不能为空");
                    break;
                case "Find success":
                    var model = document.getElementById('chat-message-list');
                    let scope = angular.element(model).scope();
                    scope.chatMessageList = _.map(scope.chatMessageList, function (message) {
                        return message.id === messageId ? data.message : message;
                    });
                    scope.$apply();
                    break;
            }
        },
        error: function (data) {
            console.log("[ERROR] update message (" + messageId + "): " + JSON.stringify(data));
        }
    });
}