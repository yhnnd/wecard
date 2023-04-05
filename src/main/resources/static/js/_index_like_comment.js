// 点赞某个评论
function likeComment(commentId) {
    const model = document.querySelector('[ng-controller="controller"]');
    let $scope = angular.element(model).scope();

    let formData = new FormData();
    formData.append("commentId", commentId);

    $.ajax({
        url: "http://" + $scope.webRoot + apis.like.comment,
        type: 'POST',
        data: formData,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        processData: false,
        contentType: false,
        success: function (resultData) {
            let result = {
                "data": resultData
            };
            switch (result.data.message) {
                case "Please login":
                    $scope.alert("[ERROR] likeComment(): 请先登录");
                    break;
                case "comment id is empty":
                    $scope.alert("[ERROR] likeComment(): 评论的 ID 不能为空");
                    break;
                case "like comment repeated":
                    cancelLikeComment(commentId);
                    break;
                case "like comment failed":
                    $scope.alert("[ERROR] likeComment(): 点赞评论失败");
                    break;
                case "like comment success":
                    // 刷新当前卡片的所有评论
                    $scope.current_card.comments = _.map($scope.current_card.comments, function (comment) {
                        // 如果当前遍历到的一级评论就是被赞的这个评论
                        if (comment.id === commentId) {
                            comment.commentHot++;
                        }
                        // 遍历这个评论的所有回复
                        if (comment.children) {
                            comment.children = _.map(comment.children, function (reply) {
                                if (reply.id === commentId) {
                                    reply.commentHot++;
                                }
                                return reply;
                            });
                        }
                        return comment;
                    });
                    $scope.$apply();
                    break;
                default:
                    $scope.alert("[ERROR] likeComment(): 未知错误：" + result.data.message);
                    break;
            }
        },
        error: function (result) {
            $scope.alert("[ERROR] likeComment(): 与服务器连接失败 错误代码: " + result.status);
        }
    });
}