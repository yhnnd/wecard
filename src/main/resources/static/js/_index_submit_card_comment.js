// 提交卡片评论
function submitComment() {
    // let loadingId = startLoading(max.loading.delay.time);
    const model = document.querySelector('[ng-controller="controller"]');
    let $scope = angular.element(model).scope();
    /* Mock Data */
    const result = {
        data: {
            comment: {
                "id": "uuid_card_comment" + window.getRandomString(),
                "cardId": $scope.current_card.id,
                "text": $scope.my_comment,
                "user": $scope.userData.user,
                "status": "exist"
            }
        }
    };
    // let formData = new FormData();
    // formData.append("cardId", $scope.current_card.id);
    // formData.append("text", $scope.my_comment);
    // $.ajax({
    //     url: "http://" + $scope.webRoot + apis.create.card.comment,
    //     type: 'POST',
    //     data: formData,
    //     crossDomain: true,
    //     xhrFields: {
    //         withCredentials: true
    //     },
    //     processData: false,
    //     contentType: false,
    //     success: function (resultData) {
    //         stopLoading(loadingId);
    //         let result = {
    //             "data": resultData
    //         };
    //         switch (result.data.message) {
    //             case "Please login":
    //                 $scope.alert("[ERROR] submitComment(): 请先登录");
    //                 break;
    //             case "comment text too long":
    //                 $scope.alert("[ERROR] submitComment(): 文本内容过长");
    //                 break;
    //             case "comment create failed":
    //                 $scope.alert("[ERROR] submitComment(): 发表评论失败");
    //                 break;
    //             case "comment create success":
                    if (result.data.comment) {
                        $scope.my_comment = "";// 清空评论输入框
                        // $scope.current_card.comments.push(result.data.comment);
                        $scope.current_card.comments.unshift(result.data.comment);
                    } else {
                        $scope.alert("[ERROR] submitComment(): 评论成功了，但是服务器没有返回（添加到数据库中的）评论对象");
                    }
    //                 break;
    //             default:
    //                 $scope.alert("[ERROR] submitComment(): 未知错误：" + result.data.message);
    //                 break;
    //         }
    //     },
    //     error: function (result) {
    //         stopLoading(loadingId);
    //         $scope.alert("[ERROR] submitComment(): 与服务器连接失败 错误代码: " + result.status);
    //     }
    // });
}