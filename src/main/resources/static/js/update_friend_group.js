// 更新好友分组
function updateFriendGroup(groupId) {
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
            console.log("update friend group: " + JSON.stringify(data));
            var model = document.getElementById('data-groups');
            let scope = angular.element(model).scope();
            scope.userData.groups = _.map(scope.userData.groups, function (group) {
                return group.id === data.group.id ? data.group : group;
            });
            scope.$apply();
        }
    });
}