// Updated on 6 July Sat 2019 by yhn
// Adding Confirm Prompt to delete card
function deleteCard(card) {

    let deleteCardConfirm = function(card) {
        let loadingId = startLoading();

        let $scope = angular.element('[ng-controller="controller"]').scope();

        console.log("deleteCard(" + card.id + "): sending request ...");

        $.ajax({
            url: $scope.httpRoot + apis.remove.card.byId,
            type: 'post',
            data: {
                "cardId": card.id
            },
            crossDomain: true,
            xhrFields: {
                withCredentials: true
            },
            success: function (resultData) {
                stopLoading(loadingId);
                let result = {
                    "data": resultData
                };
                switch (result.data.message) {
                    case "Please login":
                        bsAlert("[ERROR] deleteCard(): 请先登录");
                        break;
                    case "card has been deleted":
                        bsAlert("[ERROR] deleteCard(): 卡片已经被删除");
                        break;
                    case "not card creator":
                        bsAlert("[ERROR] deleteCard(): 不是卡片作者，无权删除卡片");
                        break;
                    case "card del failed":
                        bsAlert("[ERROR] deleteCard(): 卡片删除失败");
                        break;
                    case "card del success":
                        // 刷新所有卡片
                        $scope.cardGroups = $scope.mapCardGroups($scope.cardGroups, function (c) {
                            if (c.id === card.id) {
                                c.title = c.text = "该卡片已被删除";
                                c.images = c.video = c.voice = null;
                                c.topics = null;
                                c.status = "delete";
                            }
                            return c;
                        });
                        // 刷新我的所有卡片
                        $scope.myCardGroups = $scope.mapCardGroups($scope.myCardGroups, function (c) {
                            if (c.id === card.id) {
                                c.title = c.text = "该卡片已被删除";
                                c.images = c.video = c.voice = null;
                                c.topics = null;
                                c.status = "delete";
                            }
                            return c;
                        });
                        // 刷新当前卡片
                        if ($scope.current_card) {
                            if ($scope.current_card.id === card.id) {
                                $scope.current_card.title = $scope.current_card.text = "该卡片已被删除";
                                $scope.current_card.images = $scope.current_card.video = $scope.current_card.voice = null;
                                $scope.current_card.topics = null;
                                $scope.current_card.status = "delete";
                            }
                        }
                        $scope.$apply();
                        break;
                    default:
                        bsAlert("[ERROR] deleteCard(): 未知错误：" + result.data.message);
                        break;
                }
            },
            error: function (result) {
                stopLoading(loadingId);
                bsAlert("[ERROR] deleteCard(): 与服务器连接失败 错误代码: " + result.status);
            }
        });
    };

    bsConfirm({
        "title": "delete card",
        "content": "<span>确认删除卡片</span>？" +
            "<br>" +
            "<small><i class='fa fa-exclamation-triangle'></i>被删除的卡片不可恢复</small>",
        "alertClass": "alert-danger",
        "confirmCallback": function () {
            deleteCardConfirm(card);
            bsConfirmCloseModal();
        }
    });
}