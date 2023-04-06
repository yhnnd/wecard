function loadNewNotification(id) {
    $.ajax({
        type: "get",
        url: window.getHttpRoot() + apis.getNotification,
        data: {
            "id": id
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            console.log("load new notification: " + JSON.stringify(data));
            var model = document.querySelector('[ng-controller="controller"]');
            let scope = angular.element(model).scope();
            scope.chatMessageList.push(data.message);
            scope.$apply();
        }
    });
}