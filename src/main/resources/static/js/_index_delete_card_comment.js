// Updated on 6 July Sat 2019 by yhn(tired)
// Adding Confirm Prompt to delete comment
function deleteComment(cardId, commentId) {

    let deleteCommentConfirm = function (cardId, commentId) {
        let loadingId = startLoading();

        const model = document.querySelector('[ng-controller="controller"]');
        let $scope = angular.element(model).scope();
        let formData = new FormData();
        formData.append("commentId", commentId);

        console.log("deleteComment(" + commentId + "): sending request ...");

        $.ajax({
            url: $scope.httpRoot + apis.recall.card.comment,
            type: 'POST',
            data: formData,
            crossDomain: true,
            xhrFields: {
                withCredentials: true
            },
            processData: false,
            contentType: false,
            success: function (resultData) {
                stopLoading(loadingId);

                let result = {
                    "data": resultData
                };
                switch (result.data.message) {
                    case "Please login":
                        bsAlert("[ERROR] deleteComment(): 请先登录");
                        break;
                    case "not comment creator":
                        bsAlert("[ERROR] deleteComment(): 不能删除别人的评论");
                        break;
                    case "comment has been deleted":
                        bsAlert("[ERROR] deleteComment(): 该评论已被删除");
                        break;
                    case "comment del failed":
                        bsAlert("[ERROR] deleteComment(): 删除评论失败");
                        break;
                    case "comment del success":
                        console.log("deleteComment(): 删除评论成功");
                        // 更新主页中的卡片中的评论
                        $scope.cardGroups = $scope.mapCardGroups($scope.cardGroups, function (card) {
                            // 如果主页中的某个卡片的 ID 等于当前正在查看的卡片的 ID
                            if (card.id === cardId) {
                                // 刷新这张卡片中的所有评论
                                card.comments = _.map(card.comments, function (comment) {
                                    // 如果这张卡片中有一级评论的 ID 等于被删除的评论的 ID，就更新该一级评论的状态
                                    if (comment.id === commentId) {
                                        comment.text = "该评论已被删除";
                                        comment.status = "delete";
                                    }
                                    // 刷新当前一级评论的所有二级评论
                                    comment.children = _.map(comment.children, function (reply) {
                                        // 如果二级评论的 ID 等于被删除评论的 ID，就更新该二级评论的状态
                                        if (reply.id === commentId) {
                                            reply.text = "该评论已被删除";
                                            reply.status = "delete";
                                        }
                                        return reply;
                                    });
                                    return comment;
                                });
                            }
                            ;
                            return card;
                        });
                        // 刷新当前卡片中的所有评论
                        if ($scope.current_card.id === cardId) {
                            // 刷新当前卡片中的所有评论
                            $scope.current_card.comments = _.map($scope.current_card.comments, function (comment) {
                                // 如果当前卡片中有一级评论的 ID 等于被删除的评论的 ID，就更新该一级评论的状态
                                if (comment.id === commentId) {
                                    comment.text = "该评论已被删除";
                                    comment.status = "delete";
                                }
                                // 刷新当前一级评论的所有二级评论
                                comment.children = _.map(comment.children, function (reply) {
                                    // 如果二级评论的 ID 等于被删除评论的 ID，就更新该二级评论的状态
                                    if (reply.id === commentId) {
                                        reply.text = "该评论已被删除";
                                        reply.status = "delete";
                                    }
                                    return reply;
                                });
                                return comment;
                            });
                        }
                        $scope.$apply();
                        break;
                    default:
                        bsAlert("[ERROR] deleteComment(): 未知错误：" + result.data.message);
                        break;
                }
            },
            error: function (result) {
                stopLoading(loadingId);
                bsAlert("[ERROR] deleteComment(): 与服务器连接失败 错误代码: " + result.status);
            }
        });
    };

    bsConfirm({
        "title": "delete comment",
        "content": "<span>确认删除卡片评论</span>？" +
            "<br>" +
            "<small><i class='fa fa-exclamation-triangle'></i>被删除的评论不可恢复</small>",
        "alertClass": "alert-danger",
        "confirmCallback": function () {
            deleteCommentConfirm(cardId, commentId);
        }
    });
}