function updateRequest(requestId) {
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.getRequest,
        data: {
            "requestId": requestId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            if (data.message === "Find success") {
                console.log("update request: " + JSON.stringify(data));
                if (data.request && data.request.id) {
                    var model = document.getElementById('chat-message-list');
                    let scope = angular.element(model).scope();
                    scope.chatMessageList = _.map(scope.chatMessageList, function (request) {
                        return request.id !== data.request.id ? request : data.request;
                    });
                    scope.$apply();
                } else {
                    bsAlert("[ERROR] update request: 返回数据中没有 request");
                }
            } else {
                bsAlert("[ERROR] update request: " + data.message);
            }
        },
        error: function (data) {
            console.log("[ERROR] update request: " + JSON.stringify(data));
        }
    });
}