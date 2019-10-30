// 回复卡片的评论
function replyComment(replyInput) {
    // startLoading(max.loading.delay.time);
    let parentId = $(replyInput).attr("data-parent-id");
    let replyText = $(replyInput).val();

    let model = document.getElementById('card-groups');
    let $scope = angular.element(model).scope();

    console.log("replyComment(): 正在给卡片 " + $scope.current_card.id + " 下的评论 " + parentId + " 添加回复 " + replyText);

    if (!$scope.current_card.id) {
        bsAlert("reply comment", "卡片 ID 不能为空", "alert-danger");
        return;
    }
    if (!parentId) {
        bsAlert("reply comment", "parent ID 不能为空", "alert-danger");
        return;
    }
    if (!replyText) {
        bsAlert("reply comment", "评论不能为空", "alert-danger");
        return;
    }

    let formData = new FormData();
    formData.append("cardId", $scope.current_card.id);
    formData.append("parentId", parentId);
    formData.append("text", replyText);
    $.ajax({
        url: "http://" + $scope.webRoot + apis.create.card.comment,
        type: 'POST',
        data: formData,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        processData: false,
        contentType: false,
        success: function (resultData) {
            // stopLoading();
            let result = {
                "data": resultData
            };
            switch (result.data.message) {
                case "Please login":
                    $scope.alert("[ERROR] replyComment(): 请先登录");
                    break;
                case "comment text too long":
                    $scope.alert("[ERROR] replyComment(): 文本内容过长");
                    break;
                case "comment create failed":
                    $scope.alert("[ERROR] replyComment(): 回复评论失败");
                    break;
                case "comment create success":
                    if (result.data.comment) {
                        // 清空回复评论输入框
                        $(replyInput).attr("value", "");
                        $(replyInput).val("");
                        // 刷新当前卡片的所有评论
                        $scope.current_card.comments = _.map($scope.current_card.comments, function (comment) {
                            // 如果当前遍历到的一级评论就是被回复的评论
                            if (comment.id === parentId) {
                                // 如果这个评论有被回复
                                if (comment.children) {
                                    // comment.children.unshift(result.data.comment);
                                    comment.children.push(result.data.comment);
                                } else {// 如果这个评论没有被回复过
                                    comment.children = [result.data.comment];
                                }
                            }
                            return comment;
                        });
                        $scope.$apply();
                    } else {
                        $scope.alert("[ERROR] replyComment(): 回复评论成功了，但是服务器没有返回（添加到数据库中的）回复评论对象");
                    }
                    break;
                default:
                    $scope.alert("[ERROR] replyComment(): 未知错误：" + result.data.message);
                    break;
            }
        },
        error: function (result) {
            // stopLoading();
            $scope.alert("[ERROR] replyComment(): 与服务器连接失败 错误代码: " + result.status);
        }
    });
}