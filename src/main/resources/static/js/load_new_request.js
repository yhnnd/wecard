function loadNewRequest(requestId) {
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
            console.log("load new request: " + JSON.stringify(data));
            if (data.request && data.request.id) {
                var model = document.getElementById('chat-message-list');
                let scope = angular.element(model).scope();
                scope.chatMessageList.push(data.request);
                scope.$apply();
            } else {
                bsAlert("[ERROR] load new request: 服务器没有返回 request");
            }
        }
    });
}