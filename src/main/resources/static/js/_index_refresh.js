// 全部的任务数量
// 当全部任务都完成的时候, 就调用 $scope.$apply()
var totalNumForApply = 4;
// 已经完成的任务数量
var checkedNumForApply = 0;

// 更新已经完成的任务数量, 判断是否执行 $scope.$apply()
function checkForApply() {
    // 更新已经完成的任务数量
    window.checkedNumForApply = window.checkedNumForApply + 1;
    // 当已经完成的任务数量达到这一数值的时候(即全部任务都完成的时候), 就调用 $scope.$apply()
    if (window.checkedNumForApply >= window.totalNumForApply) {
        let $scope = angular.element("#card-groups").scope();
        if ($scope) {
            $scope.$apply();
        }
    }
}

// 加载所有人的热门卡片
function refreshAllCards(applyOnceDone) {
    let loadingId = startLoading();
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.get.cards.allUsers,
        data: {
            "sortKey": "popular",
            "start": 0,
            "limit": max.get.cards.limit
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            stopLoading(loadingId);
            switch (data.message) {
                case "card load over":
                    break;
                case "card load failed":
                    bsAlert("[ERROR] refreshAllCards(): 加载卡片列表失败");
                    break;
                case "card load success":
                    console.log("refreshAllCards(): 加载卡片列表成功");
                    function callback () {
                        let $scope = angular.element("#card-groups").scope();
                        if ($scope) {
                            if ($scope.pageLocation === 'marketplace' && $scope.subPageLocation === 'popular') {
                                // 处理从服务器接收的卡片数组
                                if (data.cards.length < max.get.cards.limit) {
                                    $scope.has_more_cards = false;
                                } else {
                                    $scope.has_more_cards = true;
                                }
                                $scope.cardGroups = $scope.makeCardGroups(data.cards);
                                $scope.cardLength = data.cards.length;
                                if ([undefined, true].includes(applyOnceDone)) {
                                    $scope.$apply();
                                }
                            } else {
                                console.log("refreshAllCards(): 加载卡片列表被中止, 因为当前页面不是默认卡片广场页面. 当前页面: '" +
                                    $scope.pageLocation + "' -> '" +
                                    $scope.subPageLocation + "'");
                            }
                            return true;
                        }
                        return false;
                    }
                    if (!callback()) {
                        setTimeout(() => {
                            callback();
                        }, 1000);
                    }
                    break;
                default:
                    bsAlert("[ERROR] refreshAllCards(): 加载卡片列表失败，错误信息：" + data.message);
                    break;
            }

            if (applyOnceDone === false) {
                checkForApply();
            }
        },
        error: function (data) {
            stopLoading(loadingId);
            bsAlert("[ERROR] refreshAllCards(): 与服务器连接失败，错误代码 " + data.status);
        }
    });
}


// 检查用户是否登录，加载用户个人信息
function refreshUserData4Cards(applyOnceDone) {
    let loadingId = startLoading();
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.user.is.LoggedIn,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            stopLoading(loadingId);
            let $scope = angular.element("#card-groups").scope();
            if ($scope) {
                $scope.user = null;
                switch (data.message) {
                    case "true":
                        if (data.user) {
                            console.log("refreshUserData4Cards(): 用户已经登录，加载用户个人信息成功");
                            $scope.user = data.user;
                            $scope.loadUserFollowing(data.user);
                            $scope.loadUserFans(data.user);
                        } else {
                            bsAlert("[ERROR] refreshUserData4Cards(): 用户登录了，但是服务器没有返回用户数据");
                        }
                        break;
                    case "false":
                        if ($scope.pageLocation !== 'marketplace') {
                            bsAlert("[ERROR] refreshUserData4Cards(): 用户没有登录");
                        }
                        break;
                    default:
                        bsAlert("[ERROR] refreshUserData4Cards(): 未知错误：" + data.message);
                        break;
                }
                if ([undefined, true].includes(applyOnceDone)) {
                    $scope.$apply();
                }
            }

            if (applyOnceDone === false) {
                checkForApply();
            }
        },
        error: function (data) {
            stopLoading(loadingId);
            bsAlert("[ERROR] refreshUserData4Cards(): 与服务器连接失败，错误代码 " + data.status);
        }
    });
}


function refreshUserData4Chat(applyOnceDone) {
    let scope = null;
    let loadingId = startLoading(max.loading.delay.time);
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + apis.get.user.data,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            stopLoading(loadingId);
            scope = angular.element("#user-data").scope();
            if (scope) {
                scope.userData = null;
                switch (data.message) {
                    case "Please login":
                        // 本函数在页面加载时执行，不能因为用户未登录就跳转到登录页面，因为这样会导致未登录用户无法查看（卡片）广场
                        // gotoPage('login.html', 'index.html');
                    break;
                    case "Refresh success":
                        scope.userData = data;
                        if ([undefined, true].includes(applyOnceDone)) {
                            scope.$apply();
                        }
                        connect_websocket();
                        // $("#module-homepage").show();
                    break;
                    default:
                        bsAlert("[ERROR] refreshUserData4Chat(): " + data.message);
                    break;
                }
            }

            if (applyOnceDone === false) {
                checkForApply();
            }
        },
        error: function (data) {
            stopLoading(loadingId);
            bsAlert("[ERROR] refreshUserData4Chat(): " + data.status);
        }
    });
    return scope;
}


function refreshMyCards(applyOnceDone) {
    let loadingId = startLoading();
    $.ajax({
        type: 'GET',
        url: "http://" + getWebRoot() + apis.get.cards.mine,
        data: {
            "sortKey": "time",
            "start": 0,
            "limit": max.get.cards.limit
        },
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function (data) {
            stopLoading(loadingId);
            let $scope = angular.element("#card-groups").scope();
            if ($scope) {
                switch (data.message) {
                    case "Please login":
                        break;
                    case "no data":
                        $scope.myCardLength = 0;
                        $scope.myCardGroups = [];
                        $scope.has_more_my_cards = false;
                        if ([undefined, true].includes(applyOnceDone)) {
                            $scope.$apply();
                        }
                        break;
                    case "card load failed":
                        $scope.alert("[ERROR] refreshMyCards(): 加载卡片失败");
                        break;
                    case "card load success":
                        // 处理从服务器接收的卡片数组
                        if (data.cards.length < $scope.max.get.cards.limit) {
                            $scope.has_more_my_cards = false;
                        } else {
                            $scope.has_more_my_cards = true;
                        }
                        $scope.myCardGroups = $scope.makeCardGroups(data.cards);
                        $scope.myCardLength = data.cards.length;
                        if ([undefined, true].includes(applyOnceDone)) {
                            $scope.$apply();
                        }
                        break;
                    default:
                        $scope.alert("[ERROR] refreshMyCards(): 加载卡片失败，错误信息：" + result.data.message);
                        break;
                }
            }

            if (applyOnceDone === false) {
                checkForApply();
            }
        },
        error: function () {
            stopLoading(loadingId);
            bsAlert("[ERROR] refreshMyCards(): 与服务器连接失败");
        }
    });
}


function refresh() {
    // 将已经完成的任务数量重置为 0
    window.checkedNumForApply = 0;
    // 将全部任务的数量设置为 4
    window.totalNumForApply = 4;
    // 同时开始执行以下四项任务, 
    // 当全部任务都被完成的时候, 才会执行 $scope.$apply()
    // 这样可以大幅减少加载数据的 "缓冲中" 时间
    refreshAllCards(false);
    refreshUserData4Cards(false);
    refreshUserData4Chat(false);
    refreshMyCards(false);
}