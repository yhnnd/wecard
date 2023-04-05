function likeCard(cardId) {
    // startLoading(max.loading.delay.time);
    const model = document.querySelector('[ng-controller="controller"]');
    let $scope = angular.element(model).scope();
    let formData = new FormData();
    formData.append("cardId", cardId);

    console.log("likeCard(" + cardId + ") sending request ...");
    
    $.ajax({
        url: "http://" + $scope.webRoot + apis.like.card,
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
                    $scope.alert("[ERROR] likeCard(): 请先登录");
                    break;
                case "card id is empty":
                    $scope.alert("[ERROR] likeCard(): 卡片 ID 不能为空");
                    break;
                case "like card repeated":
                    $scope.alert("[ERROR] likeCard(): 不能重复点赞");
                    // cancelLikeCard(cardId);
                    break;
                case "like card failed":
                    $scope.alert("[ERROR] likeCard(): 收藏卡片失败");
                    break;
                case "like card success":
                    console.log("likeCard(): 收藏卡片成功");
                    // 更新所有卡片
                    $scope.cardGroups = $scope.mapCardGroups($scope.cardGroups, function (card) {
                        if (card.id === cardId) {
                            card.cardHot++;
                            card.iLike = $scope.userData.user.id;
                        };
                        return card;
                    });
                    // 更新我的卡片
                    $scope.myCardGroups = $scope.mapCardGroups($scope.myCardGroups, function (card) {
                        if (card.id === cardId) {
                            card.cardHot++;
                            card.iLike = $scope.userData.user.id;
                        };
                        return card;
                    });
                    // 更新当前正在查看的卡片
                    if ($scope.current_card) {
                        if ($scope.current_card.id === cardId) {
                            $scope.current_card.iLike = $scope.userData.user.id;
                        } else if ($scope.current_card.share.id === cardId) {
                            $scope.current_card.share.iLike = $scope.userData.user.id;
                        }
                    }
                    $scope.$apply();
                    break;
                default:
                    $scope.alert("[ERROR] likeCard(): 未知错误：" + result.data.message);
                    break;
            }
        },
        error: function (result) {
            // stopLoading();
            $scope.alert("[ERROR] likeCard(): 与服务器连接失败 错误代码: " + result.status);
        }
    });
}