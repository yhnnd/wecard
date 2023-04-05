// 分享（转发）卡片 - 提交
function shareCardSubmit(cardId, shareText) {
    console.log("shareCard(" + cardId + "): sending request ...");

    const model = document.querySelector('[ng-controller="controller"]');
    let $scope = angular.element(model).scope();

    let formData = new FormData();
    formData.append("parentId", cardId);
    formData.append("text", shareText);

    $.ajax({
        url: $scope.httpRoot + apis.share.card,
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
                    $scope.alert("[ERROR] shareCardSubmit(): 请先登录");
                    break;
                case "card type error":
                    $scope.alert("[ERROR] shareCardSubmit(): 卡片类型错误");
                    break;
                case "share id is empty":
                    $scope.alert("[ERROR] shareCardSubmit(): 被转发的卡片 ID 不能为空");
                    break;
                case "card text too long":
                    $scope.alert("[ERROR] shareCardSubmit(): 卡片文本过长");
                    break;
                case "card create failed":
                    $scope.alert("[ERROR] shareCardSubmit(): 转发卡片失败");
                    break;
                case "card create success":
                    if (result.data.card) {
                        $("#share-card-submit")
                            .removeClass("btn-primary")
                            .addClass("btn-success")
                            .text("转发成功")
                            .attr("onclick", "hideShareCardModal(this)");
                        // 更新被转发卡片的转发数量属性
                        $scope.cardGroups = $scope.mapCardGroups($scope.cardGroups, function(card) {
                            if (card.id === cardId) {
                                card.shareNum = card.shareNum + 1;
                            }
                            return card;
                        });
                        // 更新被转发卡片的转发数量属性
                        $scope.myCardGroups = $scope.mapCardGroups($scope.myCardGroups, function(card) {
                            if (card.id === cardId) {
                                card.shareNum ++;
                            }
                            return card;
                        });
                        // 更新我所转发的卡片
                        $scope.myCardGroups[0].unshift(result.data.card);
                        $scope.myCardLength ++;
                        $scope.$apply();
                        shareCardToFriends($scope, cardId, shareText);
                    } else {
                        $scope.alert("[ERROR] shareCardSubmit(): 服务器没有返回转发卡片的数据，请刷新页面");
                    }
                    break;
                default:
                    $scope.alert("[ERROR] shareCardSubmit(): 未知错误：" + result.data.message);
                    break;
            }
        },
        error: function (result) {
            $scope.alert("[ERROR] shareCardSubmit(): 与服务器连接失败 错误代码: " + result.status);
        }
    });
}


function shareCardToFriends($scope, cardId, shareText) {
    let model = document.getElementById('friends-to-share');
    let $scopeShareCard = angular.element(model).scope();
    let friendsToShare = [];
    for (i in $scopeShareCard.friendsToShare) {
        if ($scopeShareCard.friendsToShare.hasOwnProperty(i)) {
            friendsToShare.push($scopeShareCard.friendsToShare[i]);
        }
    }
    shareCardToFriendsRun($scope, cardId, friendsToShare, shareText);
}


function shareCardToFriendsRun($scope, cardId, friendsToShare, shareText) {
    let friend = friendsToShare.shift();
    if (friend) {
        let chatItem = getChatItemByFriendId($scope, friend.id);
        if (chatItem) {
            shareCardToFriendSendMessage(cardId, chatItem, shareText, function() {
                $("#share-card-to-friends-friends-list")
                    .find("[data-friend-id=\"" + friend.id + "\"]")
                    .addClass("list-group-item-success");
                shareCardToFriendsRun($scope, cardId, friendsToShare, shareText);
            });
        }
    }
}


function getChatItemByFriendId($scope, friendId) {
    if ($scope.userData.allChatItems) {
        let chatItems = _.filter($scope.userData.allChatItems, function (e) {
            return e.type === "friend" && e.friend.id === friendId;
        });
        if (chatItems.length > 0) {
            return chatItems[0];
        }
    }
    return false;
}


function shareCardToFriendSendMessage(cardId, chatItem, shareText, callback) {
    $.ajax({
        type: 'GET',
        url: window.getHttpRoot() + apis.sendMessage,
        data: {
            "text": shareText + "\n@shareCard " + cardId,
            "chatItemId": chatItem.id
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            switch (data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "ChatItem does not exist":
                    bsAlert("[ERROR] send message: 该聊天项不存在");
                    break;
                case "He/she is not your friend":
                    bsAlert("[ERROR] send message: 该用户不是你的好友");
                    break;
                case "Text cannot be empty":
                    bsAlert("[ERROR] send message: 消息内容不能为空");
                    break;
                case "Send fail":
                    bsAlert("[ERROR] send message: 发送失败");
                    break;
                case "Send success":
                    callback();
                    break;
                default:
                    bsAlert("[ERROR] send message: 未知错误 " + data.message);
                    break;
            }
        }
    });
}


/**
function setChatLocation(location, callback) {
    $.ajax({
        type: 'GET',
        url: window.getHttpRoot() + apis.set.chat.location,
        data: {
            "location": location
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            if (data.message === "Please login") {
                gotoLogin("index.html");
            } else if (data.message === "Set success") {
                console.log("set chat location(" + location + "): '" + data.message + "'");
                callback();
            } else {
                bsAlert("[ERROR] set chat location(" + location + "): '" + data.message + "'");
            }
        },
        error: function (result) {
            bsAlert("[ERROR] set chat location(" + location + "): '" + result + "'");
        }
    });
}
**/