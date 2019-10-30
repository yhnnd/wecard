// 加载新的好友分组
function loadNewFriendGroup(groupId) {
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.getFriendGroup,
        data: {
            "groupId": groupId
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            console.log("load new friend group: " + JSON.stringify(data));
            var model = document.getElementById('data-groups');
            let scope = angular.element(model).scope();
            scope.userData.groups.push(data.group);
            scope.$apply();
        }
    });
}