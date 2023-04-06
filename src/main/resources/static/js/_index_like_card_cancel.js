function cancelLikeCard(cardId) {
    const model = document.querySelector('[ng-controller="controller"]');
    let $scope = angular.element(model).scope();
    let formData = new FormData();
    formData.append("cardId", cardId);
    
    console.log("cancelLikeCard(" + cardId + "): sending request ...");

    $.ajax({
        url: $scope.httpRoot + apis.cancel.like.card,
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
                    $scope.alert("[ERROR] cancelLikeCard(): 请先登录");
                    break;
                case "card id is empty":
                    $scope.alert("[ERROR] cancelLikeCard(): 卡片 ID 不能为空");
                    break;
                case "did not like card":
                    $scope.alert("[ERROR] cancelLikeCard(): 没有赞过这张卡");
                    break;
                case "unlike card failed":
                    $scope.alert("[ERROR] cancelLikeCard(): 取消点赞失败");
                    break;
                case "unlike card success":
                    console.log("cancelLikeCard(): 取消点赞成功");
                    // 更新所有卡片
                    $scope.popularCardGroups = $scope.mapCardGroups($scope.popularCardGroups, function (card) {
                        if (card.id === cardId) {
                            card.cardHot--;
                            card.iLike = false;
                        };
                        return card;
                    });
                    // 更新我的卡片
                    $scope.myCardGroups = $scope.mapCardGroups($scope.myCardGroups, function (card) {
                        if (card.id === cardId) {
                            card.cardHot--;
                            card.iLike = false;
                        };
                        return card;
                    });
                    // 如果我正在浏览我赞过的卡片，就从我赞过的卡片中滤掉这张卡片
                    if (["cards"].includes($scope.pageLocation) && ["myLikes"].includes($scope.subPageLocation)) {
                        $scope.myCardGroups = $scope.filterCardGroups($scope.myCardGroups, function (card) {
                            return card.id !== cardId;
                        });
                    }
                    // 更新当前正在查看的卡片
                    if ($scope.current_card) {
                        if ($scope.current_card.id === cardId) {
                            $scope.current_card.iLike = false;
                        } else if ($scope.current_card.share.id === cardId) {
                            $scope.current_card.share.iLike = false;
                        }
                    }
                    $scope.$apply();
                    break;
                default:
                    $scope.alert("[ERROR] cancelLikeCard(): 未知错误：" + result.data.message);
                    break;
            }
        },
        error: function (result) {
            $scope.alert("[ERROR] cancelLikeCard(): 与服务器连接失败 错误代码: " + result.status);
        }
    });
}