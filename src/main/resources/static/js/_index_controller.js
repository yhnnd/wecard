const app = angular.module('module-index', []);


app.filter('textLengthSet', function () {
    return function (value, wordwise, max, tail) {
        if (!value) return '';
        max = parseInt(max, 10);
        if (!max) return value;
        if (value.length <= max) return value;
        value = value.substr(0, max);
        if (wordwise) {
            var lastspace = value.lastIndexOf(' ');
            if (lastspace !== -1) {
                value = value.substr(0, lastspace);
            }
        }
        return value + (tail || ' …');//'...'可以换成其它文字
    };
});


app.controller("controller", function ($scope, $http, $timeout, $interval, $window, shareCardService) {

    /* Chat Window */
    $scope.messageToSend = "";
    $scope.isCardLink = function(line) {
        return line.startsWith('@shareCard');
    };
    $scope.getCardId = function(line) {
        let beginIndex = line.indexOf(" ");
        if (beginIndex > 0) {
            return line.substr(beginIndex + 1);
        }
        return false;
    };
    $scope.getCardLink = function(line) {
        let cardId = $scope.getCardId(line);
        if (cardId) {
            return "http://" + getWebRoot() + "/card-page.html?card-id=" + cardId;
        }
        return "javascript:void(0);";
    };
    $scope.viewCardByLink = function(line) {
        $scope.$parent.viewCard({ id: $scope.getCardId(line) });
    };
    
    const chatWindowOriginalClasses = "chat-window container-fluid mt-0";
    
    $scope.setChatWindowBackgroundColor = function (theme) {
        $('.chat-window').attr('class', chatWindowOriginalClasses + ' chat-window-bg-' + theme);
        $window.localStorage.setItem("chat_window_background_color", theme);
    };
    
    this.$onInit = function() {
        /* Chat Window */
        var theme = $window.localStorage.getItem("chat_window_background_color");
        if (theme && typeof theme === "string") {
            $scope.setChatWindowBackgroundColor(theme);
        }
        // 读取客制化设置
        loadDataFromStorage("isNavbarBackgroundDark", "is_navbar_background_dark");
        loadDataFromStorage("isNavbarMinimized", "is_navbar_minimized");
        loadDataFromStorage("alwaysViewCardInAnotherPage", "always_view_card_in_another_page");
        loadDataFromStorage("is_card_columns_single_column_enabled", "is_card_columns_single_column_enabled");
    
        if ($scope.is_card_columns_single_column_enabled) {
            $scope.toggle_card_columns_single_column("on");
        }
    };













    $scope.getInnerWidth = function () {
        return $window.innerWidth;
    };

    $scope.is_scroll_control_enabled = true;

    $scope.toggle_scroll_control = function () {
        $scope.is_scroll_control_enabled = $window.toggle_scroll_control();
    };

    $scope.is_card_columns_single_column_enabled = true;
    
    $scope.toggle_card_columns_single_column = function (flag) {
        const textContent = `
.card-columns {
    column-count: 1;
    max-width: 600px;
    margin: 0 auto;
}
`;
        const style = $("<style>").addClass("single-column").text(textContent);
        let styleInline = $("style.single-column");
        if (!styleInline.length && [undefined, "on"].includes(flag)) {
            $("html > body").prepend(style);
        } else if ([undefined, "off"].includes(flag)) {
            styleInline.remove();
        }
    };
    
    $scope.toggle_card_columns_single_column_save = function (flag) {
        if (["on"].includes(flag)) {
            $scope.toggle_card_columns_single_column('on');
            $scope.is_card_columns_single_column_enabled = true;
            $window.localStorage.setItem('is_card_columns_single_column_enabled', true);
            return true;
        } else if (["off"].includes(flag)) {
            $scope.toggle_card_columns_single_column('off');
            $scope.is_card_columns_single_column_enabled = false;
            $window.localStorage.setItem('is_card_columns_single_column_enabled', false);
            return false;
        }
        return;
    };

    // 夜间模式
    $scope.nightMode = $window.nightMode;
    // 开发者模式
    $scope.debugMode = $window.debugMode;
    $scope.cardsViewMode = "classic";
    // 客制化设置
    // 导航栏背景色
    $scope.isNavbarBackgroundDark = true;
    // 导航栏的尺寸高度
    $scope.isNavbarMinimized = false;
    // 撰写卡片正文编辑器的类型
    $scope.writeCard_editorType = "v2.7";
    // 撰写卡片正文编辑区域的高度
    $scope.writeCard_textAreaHeight = 468;
    // 是否使用新窗口打开卡片
    $scope.alwaysViewCardInAnotherPage = false;

    // 读取浏览器存储的客制化设置的函数
    function loadDataFromStorage(scopeKey, storageKey) {
        var v = $window.localStorage.getItem(storageKey);
        if (v && typeof v === "string") {// (v instanceof String) is always false
            var oldv = $scope[scopeKey];
            try {
                $scope[scopeKey] = JSON.parse(v);
            } catch (e) {
                $scope[scopeKey] = oldv;
            }
        } else {
            $window.localStorage.setItem(storageKey, $scope[scopeKey]);
            console.log("loadDataFromStorage: localStorage.getItem(\"" + storageKey + "\") is now " + $scope[scopeKey] + ".");
        }
    }
    

    
    // 保存导航栏的客制化设置的函数
    $scope.rememberNavbarSettings = function() {
        $window.localStorage.setItem("is_navbar_background_dark", $scope.isNavbarBackgroundDark);
        $window.localStorage.setItem("is_navbar_minimized", $scope.isNavbarMinimized);
    };

    $scope.userAgent = window.navigator.userAgent;
    $scope.platform  = window.navigator.platform;
    $scope.webRoot = getWebRoot();
    $scope.special_ids = special_ids;

    $scope.pageLocation = "marketplace";
    $scope.prevPageLocation = $scope.pageLocation;
    // marketplace, mySocialCircle, cards
    // homepage、chat、groups、settings、searchResults

    $scope.subPageLocation = "popular";
    $scope.prevSubPageLocation = $scope.subPageLocation;
    // marketplace: popular, newest
    // mySocialCircle: myFollowingCards, myFollowingUsers, myFollowers
    // cards: myCards, write, myLikes, myShares
    // homepage: chatItemList, friendGroups, roomGroups, informGroups

    $scope.keyOf = {
        "popular": "popular",
        "newest": "time"
    };
    // 用户个人信息
    $scope.user = null;
    // 所有人的卡片
    $scope.cardLength = 0;
    $scope.cardGroups = [];
    $scope.has_more_cards = true;
    // 我关注的人的卡片
    $scope.has_more_following_cards = true;
    // 当前正在查看的卡片
    $scope.current_card = null;
    // 我的所有卡片
    $scope.myCardLength = 0;
    $scope.myCardGroups = [];
    $scope.has_more_my_cards = true;

    $scope.my_comment = "";
    $scope.my_topic = "";
    $scope.my_topic_placeholder = "输入话题";

    $scope.max = $window.max;

    $scope.card_to_create = {
        title: "",
        text: "",
        type: "",
        topics: [],//话题名
        files: [],
        file: null
    };







    // homepage 页面迁移过来的变量
    $scope.userData = {};
    $scope.chatMessageList = [];
    $scope.unread_message_count = 0;// 当前聊天窗口的未读消息数
    $scope.total_unread_message_count = 0;// 所有 chatItem 的未读消息数总和
    $scope.current = {
        sizeOf: {
            messagesLoadedLimited: 0,// 存储上次加载的消息数，如果小于 max.get.messages.limit 就没有更多消息可以加载了
            messagesLoadedLimitedUpdate: function (n) {
                this.messagesLoadedLimited = n;
            }
        }
    };

    $scope.current_friend = {};
    $scope.current_room = {};
    $scope.current_room_my_role = "";
    $scope.current_room_members = [];
    $scope.current_room_members_list_offset = 0;
    $scope.current_room_members_list_limit = 6;
    // 没有 $scope.currentChatItem 但是有 get_current_chat_item() 函数
    $scope.currentChatItemId = "external";// external 或 chatItemId 的值
    $scope.currentChatItemType = "unknown";
    $scope.currentChatItemName = "unknown";
    $scope.currentChatItemAvatar = "";
    // 没有 $scope.prevChatItem 但是有 getPrevChatItem() 函数
    $scope.prevChatItemId = $scope.currentChatItemId;
    $scope.prevChatItemType = $scope.currentChatItemType;
    $scope.prevChatItemName = $scope.currentChatItemName;
    $scope.prevChatItemAvatar = $scope.currentChatItemAvatar;

    $scope.this_friend = null;


    // 用于控制某些数据的输入框是否显示的 flag
    $scope.allow = {
        edit: {
            user: {
                nickname: false,
                signature: false,
                city: false,
                gender: false,
                phone: false
            }
        },
        set: {
            user: {
                group: {
                    name: {
                        oldGroupName: "",
                        enableBy: {
                            groupId: false
                        },
                        enable: function (group) {
                            this.oldGroupName = group.groupName;
                            this.enableBy.groupId = group.id;// 显示分组名输入框
                            setTimeout(() => {
                                $("input.friend-group-name[data-friend-group-id='" + group.id + "']").focus();
                        }, 10);
                        },
                        submit: function (group) {
                            this.enableBy.groupId = false;// 隐藏分组名输入框
                            $scope.set_friend_group_name(group, this.oldGroupName);// 提交新的分组名
                        }
                    }
                }
            },
            room: {
                name: false,// 群名称，只有群主能设置
                member: {
                    role: false,// 群成员身份，只有群主能设置
                    remark: false// 群名片，只有群成员自己能设置
                }
            },
            friend: {
                remark: false,// 我给好友设置备注名
                group: false// 我给好友设置分组
            }
        },
        remove: {
            room: {
                message: false// 删除消息记录，我可以删除任何人的消息记录
            }
        }
    };



    // 监听用户数据更新（从 homepage 页面迁移至此）
    $scope.$watch('userData', function (newValue, oldValue) {
        console.log("$watch(userData): userData has been changed from", oldValue, "to", newValue);
        if ($scope.userData) {
            // 重新在 userData 对象中植入一个函数，用于查看用户有没有某种类型的聊天项（如 room，friend，system，inform）（植入时不会调用该函数）
            $scope.userData.has_chat_item_type = function (itemType) {
                return _.some(this.allChatItems, function (item) {
                    return item.type === itemType;
                });
            }
            // 更新用户的未读消息数总和
            $scope.updateTotalUnreadMsgCount();
        }
    });



    // 监听用户数据更新
    $scope.$watch('pageLocation', function (newValue, oldValue) {
        if (newValue.startsWith("trans-")) {
            console.log("$watch: pageLocation '" + oldValue + "' is switching now");
            $scope.prevPageLocation = oldValue;// 记录之前的页面
        } else if (oldValue.startsWith("trans-")) {
            console.log("$watch: pageLocation has switched to '" + newValue + "'");
        } else {
            console.log("$watch: pageLocation change from '" + oldValue + "' to '" + newValue + "'");
            $scope.prevPageLocation = oldValue;// 记录之前的页面
        }
    });

    $scope.$watch('subPageLocation', function (newValue, oldValue) {
        if (newValue.startsWith("trans-")) {
            console.log("$watch: subPageLocation '" + oldValue + "' is switching now");
            $scope.prevSubPageLocation = oldValue;// 记录之前的页面
        } else if (oldValue.startsWith("trans-")) {
            console.log("$watch: subPageLocation has switched to '" + newValue + "'");
        } else {
            console.log("$watch: subPageLocation change from '" + oldValue + "' to '" + newValue + "'");
            $scope.prevSubPageLocation = oldValue;
        }

        if (newValue === "write") {
            checkDraftCard();
        }
    });

    // 检查是否有草稿待继续编辑
    function checkDraftCard() {
        let ids = $window.localStorage.getItem("draft_card_ids");
        if (ids != null) {
            try {
                ids = JSON.parse(ids);
            } catch (e) {
                return;
            }
            if (ids instanceof Array) {
                for (id of ids) {
                    let draft = $window.localStorage.getItem("draft_card_" + id);
                    if (draft != null) {
                        try {
                            draft = JSON.parse(draft);
                        } catch (e) {
                            return;
                        }
                        let draftCard = draft.card;
                        let draftUser = draft.user;
                        if ($scope.card_to_create.title === "" 
                        && $scope.card_to_create.text === ""
                        && JSON.stringify($scope.card_to_create.topics) === "[]"
                        && $scope.user.id === draftUser.id
                        && $window.confirm("发现草稿, 是否继续编辑?" + 
                                "\n\n详细信息:" +
                                "\n\n卡片标题: " + draftCard.title + 
                                "\n卡片正文: " + draftCard.text)) {
                            $scope.card_to_create.title  = draftCard.title;
                            $scope.card_to_create.text   = draftCard.text;
                            $scope.card_to_create.type   = draftCard.type;
                            $scope.card_to_create.topics = draftCard.topics;
                            break;
                        }
                    }
                }
            }
        }
    }

    // 跳转到指定页面
    $scope.gotoPage = function (pageName, subPageName, chatItemId/* optional */) {
        // 如果从聊天窗口回到消息列表, 则并不立即跳转, 而是开启动画, 一秒之后再跳转
        // 注意: 此时并未开始执行页面跳转, 所以我们不应该使用 prevPageLocation
        if (["homepage"].includes(pageName) && ["chat"].includes($scope.pageLocation)) {
            $scope.gotoPage("trans-chat-home", subPageName);
            $timeout(function() {
                $scope.gotoPage(pageName, subPageName);
            }, 1000);
            return true;
        }
        // 跳转到新的页面
        $scope.pageLocation = pageName;
        // 自动进入子页面
        if (["marketplace"].includes(pageName)) {
            if (!subPageName) {
                $scope.subPageLocation = 'popular';
            } else {
                $scope.subPageLocation = subPageName;
            }
            $scope.refresh();
            // 因为卡片广场和我关注的卡片共用同一个卡片数组，所以切换页面的时候不得不刷新
            // 同时，我们不需要在 refreshCurrentPage 函数中刷新卡片
            // 因为该函数调用本函数
        } else if (["mySocialCircle"].includes(pageName)) {
            if (!subPageName) {
                $scope.subPageLocation = 'myFollowingCards';
            } else {
                $scope.subPageLocation = subPageName;
            }
            $scope.refreshFollowingCards();
            // 因为卡片广场和我关注的卡片共用同一个卡片数组，所以切换页面的时候不得不刷新
            // 同时，我们不需要在 refreshCurrentPage 函数中刷新卡片
            // 因为该函数调用本函数
        } else if (["cards"].includes(pageName)) {
            if (!subPageName) {
                $scope.subPageLocation = 'myCards';
            } else {
                $scope.subPageLocation = subPageName;
            }
            // 因为卡片广场和我关注的卡片所用的卡片数组与我自己的卡片数组不是同一个数组，所以切换到本页面的时候不需要刷新卡片
        } else if (["chat"].includes(pageName)) {
            if (['refresh'].includes(chatItemId)) {
                setChatLocation($scope.currentChatItemId);
            } else {
                $scope.subPageLocation = "chat";
                setChatLocation(chatItemId);
                // 防止因页面切换动画而导致的页面被隐藏的错误
                $(".chat-window").show();
            }
        } else if (["homepage", "trans-chat-home"].includes(pageName)) {
            if (!subPageName) {
                $scope.subPageLocation = "chatItemList";
            } else {
                $scope.subPageLocation = subPageName;
            }
            if ($scope.subPageLocation === "chatItemList") {
                // 修复从聊天室返回聊天项列表页面的时候页面滚动到最底部的 bug
                window.scrollTo(0, 0);
            }
            // 修复从聊天主页进入聊天室, 未直接回到聊天项列表, 间接回到聊天主页, 聊天主页被隐藏的 bug
            $(".chat-list").show();
        } else if (["settings"].includes(pageName)) {
        }

        // 处理动画页面
        if (["trans-home-chat"].includes(pageName)) {
            // Animate from chat-list to chat-window so hide chat-list after animation is done
            $(".chat-list").addClass("remove-to-left-1s");
            $(".chat-window").addClass("show-to-left-1s");
            $timeout(function() {
                $(".chat-list").removeClass("remove-to-left-1s").hide();
                $(".chat-window").removeClass("show-to-left-1s");
            }, 1000);
        } else if (["trans-chat-home"].includes(pageName)) {
            // Animate from chat-window to chat-list so hide chat-window after animation is done
            $(".chat-window").addClass("remove-to-right-1s");
            $(".chat-list").addClass("show-to-right-1s");
            $timeout(function() {
                $(".chat-window").removeClass("remove-to-right-1s").hide();
                $(".chat-list").removeClass("show-to-right-1s");
            }, 1000);
        }

        if (!["chat"].includes(pageName) && $scope.currentChatItemId !== "external") {
            setChatLocation("external");
        }
        // 清空所有弹窗
        removeAllTooltips();
        removeAllPopovers();
    }

    // 返回之前的页面
    $scope.goBack = function () {
        $scope.gotoPage($scope.prevPageLocation, $scope.prevSubPageLocation);
    }


    // 获取所有未读消息数总和
    $scope.getTotalUnreadMsgCount = function () {
        var c = 0;
        if ($scope.userData && $scope.userData.chatList) {
            $scope.userData.chatList.forEach(function (ci) {
                c += ci.unreadNum;
            });
        }
        return c;
    };

    
    // 更新所有未读消息数总和
    $scope.updateTotalUnreadMsgCount = function () {
        $scope.total_unread_message_count = $scope.getTotalUnreadMsgCount();
    };


    // 将一维卡片数组转变成二维卡片数组
    $scope.makeCardGroups = function (cards) {
        let groups = [], group = [];
        for(let i = 0; i < cards.length; ++i) {
            if (cards[i].hasOwnProperty("ilike") && !cards[i].hasOwnProperty("iLike")) {
                cards[i].iLike = cards[i].ilike;
            }
            group.push(cards[i]);
            if (group.length == $scope.max.get.cards.limit || i == cards.length-1) {
                groups.push(group);
                group = [];
            }
        }
        return groups;
    }


    // 批量修改二维卡片数组
    $scope.mapCardGroups = function (cardGroups, mapper) {
        return _.map(cardGroups, function(cardGroup) {
            return _.map(cardGroup, mapper);
        });
    };


    // 过滤二维卡片数组
    $scope.filterCardGroups = function (cardGroups, filter) {
        return _.map(cardGroups, function(cardGroup) {
            return _.filter(cardGroup, filter);
        });
    };



    // 刷新当前窗口，重新加载所有（热门/最新）卡片
    $scope.refresh = function () {
        // let loadingId = startLoading(max.loading.delay.time, "$scope.refresh()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.cards.allUsers,
        //     params: {
        //         "sortKey": $scope.keyOf[$scope.subPageLocation],
        //         "start": 0,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     switch (result.data.message) {
        //         case "no data":
        //             $scope.cardLength = 0;
        //             $scope.cardGroups = [];
        //             $scope.has_more_cards = false;
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] $scope.refresh(): 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_cards = false;
        //             } else {
        //                 $scope.has_more_cards = true;
        //             }
        //             $scope.cardLength = result.data.cards.length;
        //             $scope.cardGroups = $scope.makeCardGroups(result.data.cards);
        //             break;
        //         default:
        //             $scope.alert("[ERROR] $scope.refresh(): 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] $scope.refresh(): 与服务器连接失败");
        // });
    };


    // 刷新用户数据，包括用户加入的房间列表及用户的好友分组
    $scope.refreshUserData = function () {
        // // 刷新当前窗口，更新用户所有数据
        // let loadingId = startLoading(max.loading.delay.time, "$scope.refreshUserData()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.user.data,
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     let data = result.data;
        //     if (data.message === "Please login") {
        //         $scope.alert("[ERROR] $scope.refreshUserData(): 请先登录");
        //     } else if (data.message === "Refresh fail") {
        //         $scope.alert("[ERROR] $scope.refreshUserData(): 更新用户数据失败");
        //     } else if (data.message === "Refresh success") {
        //         $scope.userData = data;
        //     } else {
        //         $scope.alert("[ERROR] $scope.refreshUserData(): 未知错误 " + data.message);
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] $scope.refreshUserData(): 无法连接服务器");
        //     gotoLogin("index.html");
        // });

        /* mock data (userData in refreshUserData) */
        $scope.user = {
            "id": "1",
            "username": "user",
            "nickname": "yhn",
            "grade": "3",
            "lastLoginTime": "just now",
            "followNum": "2",
            "fansNum": "23",
            "cardsNum": "0",
            "gender": "male",
            "city": "New York",
            "phone": "124 5322 3296",
            "signature": "fuck ccp",
            "avatarUrl": "https://media.discordapp.net/attachments/907832332537434152/993550228315701358/00096489135.jpg"
        };
        $scope.userData = {
            mesage: "Refresh success",
            user: {
                "id": "1",
                "username": "user",
                "nickname": "yhn",
                "grade": "3",
                "lastLoginTime": "just now",
                "followNum": "2",
                "fansNum": "23",
                "cardsNum": "0",
                "gender": "male",
                "city": "New York",
                "phone": "124 5322 3296",
                "signature": "fuck ccp",
                "avatarUrl": "https://media.discordapp.net/attachments/907832332537434152/993550228315701358/00096489135.jpg"
            },
            chatList: [{
                id: "1",
                type: "room",
                room: {
                    "roomName": "room 1",
                    "remark": "some room 1",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            },{
                id: "2",
                type: "room",
                room: {
                    "roomName": "room 2",
                    "remark": "some room 2",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            },{
                id: "3",
                type: "friend",
                friend: {
                    username: "user 1",
                    remark: "my friend 1",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            },{
                id: "4",
                type: "friend",
                friend: {
                    username: "user 2",
                    remark: "my friend 2",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            }],
            allChatItems: [{
                id: "1",
                type: "room",
                room: {
                    "id": "1",
                    "roomName": "room 1",
                    "remark": "some room 1"
                }
            },{
                id: "2",
                type: "room",
                room: {
                    "id": "2",
                    "roomName": "room 2",
                    "remark": "some room 2"
                }
            },{
                id: "3",
                type: "friend",
                friend: {
                    "id": "123",
                    "remark": "friend 1",
                    "username": "user"
                }
            },{
                id: "4",
                type: "friend",
                friend: {
                    "id": "123",
                    "remark": "friend 1",
                    "username": "user"
                }
            }]
        };
    };

    // 加载更多卡片
    $scope.load_more_cards = function () {
        // let loadingId = startLoading(max.loading.delay.time, "$scope.load_more_cards()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.cards.allUsers,
        //     params: {
        //         "sortKey": $scope.keyOf[$scope.subPageLocation],
        //         "start": $scope.cardLength,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);

        //     switch (result.data.message) {
        //         case "no data":
        //             $scope.has_more_cards = false;
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] load more cards: 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_cards = false;
        //             } else {
        //                 $scope.has_more_cards = true;
        //             }
        //             // 将新加载的卡片添加到已加载的卡片数组当中
        //             let newCardGroup = [];
        //             _.each(result.data.cards, function (card) {
        //                 if (card.hasOwnProperty("ilike") && !card.hasOwnProperty("iLike")) {
        //                     card.iLike = card.ilike;
        //                 }
        //                 card.isLoadedRightNow = true;// 高亮新加载的卡片
        //                 newCardGroup.push(card);
        //             });
        //             $scope.cardGroups.push(newCardGroup);
        //             $scope.cardLength += newCardGroup.length;
        //             // 解除新加载的卡片的高亮
        //             $timeout(function () {
        //                 $scope.cardGroups = _.map($scope.cardGroups, function (cardGroup) {
        //                     cardGroup = _.map(cardGroup, function(card) {
        //                         card.isLoadedRightNow = false;
        //                         return card;
        //                     });
        //                     return cardGroup;
        //                 });
        //             }, 10 * result.data.cards.length);
        //             break;
        //         default:
        //             $scope.alert("[ERROR] load more cards: 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] load more cards: 与服务器连接失败");
        // });
    };





    // 刷新当前窗口，重新加载所有我关注的人的所有卡片
    $scope.refreshFollowingCards = function () {
        // let loadingId = startLoading(max.loading.delay.time, "$scope.refreshFollowingCards()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.cards.following,
        //     params: {
        //         "start": 0,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);

        //     switch (result.data.message) {
        //         case "no data":
        //             $scope.cardLength = 0;
        //             $scope.cardGroups = [];
        //             $scope.has_more_following_cards = false;
        //             break;
        //         case "Please login":
        //             $scope.alert("[ERROR] $scope.refreshFollowingCards(): 请先登录");
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] $scope.refreshFollowingCards(): 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_following_cards = false;
        //             } else {
        //                 $scope.has_more_following_cards = true;
        //             }
        //             $scope.cardGroups = $scope.makeCardGroups(result.data.cards);
        //             $scope.cardLength += result.data.cards.length;
        //             break;
        //         default:
        //             $scope.alert("[ERROR] $scope.refreshFollowingCards(): 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] $scope.refreshFollowingCards(): 与服务器连接失败");
        // });
    };


    // 加载更多卡片
    $scope.load_more_following_cards = function () {
        // let loadingId = startLoading(max.loading.delay.time, "$scope.load_more_following_cards()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.cards.following,
        //     params: {
        //         "start": $scope.cardLength,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);

        //     switch (result.data.message) {
        //         case "no data":
        //             $scope.has_more_following_cards = false;
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] load more following cards: 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_following_cards = false;
        //             } else {
        //                 $scope.has_more_following_cards = true;
        //             }
        //             // 将新加载的卡片添加到已加载的卡片数组当中
        //             let newCardGroup = [];
        //             _.each(result.data.cards, function (card) {
        //                 if (card.hasOwnProperty("ilike") && !card.hasOwnProperty("iLike")) {
        //                     card.iLike = card.ilike;
        //                 }
        //                 card.isLoadedRightNow = true;// 高亮新加载的卡片
        //                 newCardGroup.push(card);
        //             });
        //             $scope.cardGroups.push(newCardGroup);
        //             $scope.cardLength += newCardGroup.length;
        //             // 解除新加载的卡片的高亮
        //             $timeout(function () {
        //                 $scope.cardGroups = _.map($scope.cardGroups, function (cardGroup) {
        //                     cardGroup = _.map(cardGroup, function(card) {
        //                         card.isLoadedRightNow = false;
        //                         return card;
        //                     });
        //                     return cardGroup;
        //                 });
        //             }, 10 * result.data.cards.length);
        //             break;
        //         default:
        //             $scope.alert("[ERROR] load more following cards: 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] load more following cards: 与服务器连接失败");
        // });
    };





    // 重新加载我的所有卡片
    $scope.refreshMyCards = function () {
        // let api = apis.get.cards.mine;
        // if ($scope.subPageLocation === "myLikes") {
        //     api = apis.get.cards.liked;
        // } else if ($scope.subPageLocation === "myShares") {
        //     api = apis.get.cards.shared;
        // }

        // let loadingId = startLoading(max.loading.delay.time, "$scope.refreshMyCards()");

        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + api,
        //     params: {
        //         "sortKey": "time",
        //         "start": 0,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     switch (result.data.message) {
        //         case "Please login":
        //             $scope.alert("[ERROR] $scope.refreshMyCards(): 请先登录");
        //             break;
        //         case "no data":
        //             $scope.myCardLength = 0;
        //             $scope.myCardGroups = [];
        //             $scope.has_more_my_cards = false;
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] $scope.refreshMyCards(): 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_my_cards = false;
        //             } else {
        //                 $scope.has_more_my_cards = true;
        //             }
        //             $scope.myCardGroups = $scope.makeCardGroups(result.data.cards);
        //             $scope.myCardLength = result.data.cards.length;
        //             break;
        //         default:
        //             $scope.alert("[ERROR] $scope.refreshMyCards(): 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] $scope.refreshMyCards(): 与服务器连接失败");
        // });
    };



    // 加载更多我的卡片
    $scope.load_more_my_cards = function () {
        // let api = apis.get.cards.mine;
        // if ($scope.subPageLocation === "myLikes") {
        //     api = apis.get.cards.liked;
        // } else if ($scope.subPageLocation === "myShares") {
        //     api = apis.get.cards.shared;
        // }
        // let loadingId = startLoading(max.loading.delay.time, "$scope.load_more_my_cards()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + api,
        //     params: {
        //         "sortKey": "time",
        //         "start": $scope.myCardLength,
        //         "limit": $scope.max.get.cards.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     switch (result.data.message) {
        //         case "no data":
        //             $scope.has_more_my_cards = false;
        //             break;
        //         case "card load failed":
        //             $scope.alert("[ERROR] load more my cards: 加载卡片失败");
        //             break;
        //         case "card load success":
        //             // 处理从服务器接收的卡片数组
        //             if (result.data.cards.length < $scope.max.get.cards.limit) {
        //                 $scope.has_more_my_cards = false;
        //             } else {
        //                 $scope.has_more_my_cards = true;
        //             }
        //             // 将新加载的卡片添加到已加载的卡片数组当中
        //             let newCardGroup = [];
        //             _.each(result.data.cards, function (card) {
        //                 if (card.hasOwnProperty("ilike") && !card.hasOwnProperty("iLike")) {
        //                     card.iLike = card.ilike;
        //                 }
        //                 card.isLoadedRightNow = true;// 高亮新加载的卡片
        //                 newCardGroup.push(card);
        //             });
        //             $scope.myCardGroups.push(newCardGroup);
        //             $scope.myCardLength += newCardGroup.length;
        //             // 解除新加载的卡片的高亮
        //             $timeout(function () {
        //                 $scope.myCardGroups = _.map($scope.myCardGroups, function (cardGroup) {
        //                     cardGroup = _.map(cardGroup, function(card) {
        //                         card.isLoadedRightNow = false;
        //                         return card;
        //                     });
        //                     return cardGroup;
        //                 });
        //             }, 10 * result.data.cards.length);
        //             break;
        //         default:
        //             $scope.alert("[ERROR] load more my cards: 加载卡片失败，错误信息：" + result.data.message);
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] load more my cards: 与服务器连接失败");
        // });
    };




    $scope.keyword = "";
    $scope.searchResults = [];
    $scope.isSearching = false;
    $scope.isSearchingLoading = false;
    // 搜索卡片
    $scope.search = function () {
        if ($scope.keyword !== "") {

            window.scrollTo(0,0);
            $scope.isSearching = true;
            $scope.isSearchingLoading = true;
            $window.keyword = $scope.keyword;

            $http({
                method: 'GET',
                url: "http://" + $scope.webRoot + apis.search.cards,
                params: {
                    "key": "content",
                    "value": $scope.keyword
                },
                crossDomain: true,
                withCredentials: true
            }).then(function (result) {
                $scope.isSearchingLoading = false;

                let data = result.data;
                switch (data.message) {
                    case "no data":
                        console.log("[ERROR] searchCard(): 关键词 '" + $scope.keyword + "' 没查询到数据");
                        $scope.searchResults = [];
                        break;
                    case "card search success":
                        if ($scope.keyword === $window.keyword) {
                            $scope.searchResults = data.cards;
                        }
                        break;
                    case "card search failed":
                        console.log("[ERROR] searchCard(): 查询失败");
                        break;
                    default:
                        $scope.alert("[ERROR] searchCard(): 未知错误 " + data.message);
                        break;
                }
            }, function () {
                $scope.isSearchingLoading = false;
                $scope.alert("[ERROR] searchCard(): 无法连接到服务器");
            });
        } else {// 输入框为空
            $scope.isSearching = false;
        }
    }

    $scope.stopSearch = function () {
        $scope.keyword = "";
        $scope.isSearching = false;
        $scope.isSearchingLoading = false;
        $scope.searchResults = [];
    }

    var lastInputTime = null;
    var scheduleTimer = null;

    $scope.scheduledSearch = function () {
        if (!scheduleTimer) {
            scheduleTimer = $interval(() => {
                if (Date.now() - lastInputTime > 1000) {
                    $scope.search();
                    $interval.cancel(scheduleTimer);
                    scheduleTimer = null;
                }
            }, 1000);
        } else {
            lastInputTime = Date.now();
        }
    };



    // 按关键词搜索卡片
    $scope.searchTopic = function (topicName) {
        let loadingId = startLoading(max.loading.delay.time, "$scope.searchTopic()");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.search.cards,
            params: {
                "key": "topic",
                "value": topicName
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            stopLoading(loadingId);

            $('[data-dismiss="modal"],[data-dismiss=\'modal\']').click();
            window.scrollTo(0,0);

            let data = result.data;
            switch (data.message) {
                case "no data":
                    $scope.alert("[ERROR] searchTopic(): 关键词 '" + topicName + "' 没查询到数据");
                    $scope.searchResults = [];
                    break;
                case "card search success":
                    $scope.searchResults = data.cards;
                    $scope.isSearching = true;
                    break;
                case "card search failed":
                    $scope.alert("[ERROR] searchTopic(): 查询失败, 关键词 '" + topicName + "'");
                    break;
                default:
                    $scope.alert("[ERROR] searchTopic(): 未知错误 " + data.message);
                    break;
            }
        }, function () {
            stopLoading(loadingId);
            $scope.alert("[ERROR] searchTopic(): 无法连接到服务器");
        });
    }

    $scope.askToSearchTopic = function (topic) {
        if (typeof topic === "string") {
            // (topic instanceof String) is always false
        } else {
            topic = topic.name;
        }
        $scope.confirm({
            title: "search topic",
            content: "搜索话题 '" + topic + "'?",
            alertClass: "alert-primary text-center",
            confirmText: "搜索",
            "confirmCallback": function() {
                $scope.searchTopic(topic);
                $('.fixed-top.alert').remove();
            },
            rejectText: "取消",
            "rejectCallback": function() {
                $('.fixed-top.alert').remove();
            }
        });
    };


    $scope.launchSearchTopicModal = function () {
        // let topicName = $window.prompt("输入一个话题名");
        // if (topicName) {
        //     $scope.searchTopic(topicName);
        // }
        $scope.subPageLocation = "topics";
    };





    // 加载我关注的人（注入到 user 中）
    $scope.loadUserFollowing = function (user) {
        // let loadingId = startLoading(max.loading.delay.time,"$scope.loadUserFollowing()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.user.following,
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     switch (result.data.message) {
        //         case "Please login":
        //             $scope.alert("$scope.loadUserFans(" + user.id + "):","请先登录","alert-danger");
        //             break;
        //         case "Find success":
        //             $scope.user.following = result.data.following;
        //             break;
        //         default:
        //             $scope.alert("$scope.loadUserFollowing(" + user.id + "):","未知错误: " + result.data.message,"alert-danger");
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("$scope.loadUserFollowing(" + user.id + "):","无法连接服务器","alert-danger");
        // });
    };

    // 加载我的粉丝（注入到 user 中）
    $scope.loadUserFans = function (user) {
        // let loadingId = startLoading(max.loading.delay.time,"$scope.loadUserFans()");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.user.fans,
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     switch (result.data.message) {
        //         case "Please login":
        //             $scope.alert("$scope.loadUserFans(" + user.id + "):","请先登录","alert-danger");
        //             break;
        //         case "Find success":
        //             $scope.user.fans = result.data.fans;
        //             break;
        //         default:
        //             $scope.alert("$scope.loadUserFans(" + user.id + "):","未知错误: " + result.data.message,"alert-danger");
        //             break;
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("$scope.loadUserFans(" + user.id + "):","无法连接服务器","alert-danger");
        // });
    };

    // 判断我是否关注了这个用户
    $scope.isFollowing = function (user) {
        // 如果用户未登录
        if (!$scope.user || !$scope.user.following) {
            return false;
        }
        // 如果参数为空
        if (!user) {
            return false;
        }
        // 如果用户已登录
        return _.find($scope.user.following, function (person) {
            return person.id === user.id;
        });
    };


    // 关注别人
    $scope.followUser = function (user) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.follow.user,
            params: {
                "followingId": user.id
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            switch (result.data.message) {
                case "Please login":
                    // bsAlert("$scope.followUser(" + user.id + "): 请先登录");
                    $window.location = "./login.html?target=./index.html";
                    break;
                case "FollowingId cannot be empty":
                    $scope.alert("$scope.followUser(" + user.id + "):","被关注的人的 ID 不能为空","alert-danger");
                    break;
                case "Cannot follow yourself":
                    $scope.alert("$scope.followUser(" + user.id + "):","不能关注自己","alert-danger");
                    break;
                case "Following does not exist":
                    $scope.alert("$scope.followUser(" + user.id + "):","被关注的人不存在","alert-danger");
                    break;
                case "Has followed":
                    $scope.alert("$scope.followUser(" + user.id + "):","已经关注他了","alert-danger");
                    break;
                case "Follow fail":
                    $scope.alert("$scope.followUser(" + user.id + "):","关注失败","alert-danger");
                    break;
                case "Follow success":
                    $scope.user.following.push(user);
                    break;
                default:
                    $scope.alert("$scope.followUser(" + user.id + "):","未知错误: " + result.data.message,"alert-danger");
                    break;
            }
        }, function () {
            $scope.alert("$scope.followUser(" + user.id + "):","无法连接服务器","alert-danger");
        });
    };


    // 取消关注别人
    $scope.unfollowUser = function (user) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.unfollow.user,
            params: {
                "followingId": user.id
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            switch (result.data.message) {
                case "Please login":
                    $scope.alert("$scope.unfollowUser(" + user.id + "): 请先登录");
                    $window.location = "./login.html?target=./index.html";
                    break;
                case "FollowingId cannot be empty":
                    $scope.alert("$scope.unfollowUser(" + user.id + "): 被关注的人的 ID 不能为空");
                    break;
                case "Cannot unfollow yourself":
                    $scope.alert("$scope.unfollowUser(" + user.id + "): 不能取消关注自己");
                    break;
                case "You are not following him":
                    $scope.alert("$scope.unfollowUser(" + user.id + "): 你没有关注此人");
                    break;
                case "Unfollow fail":
                    $scope.alert("$scope.unfollowUser(" + user.id + "): 取消关注失败");
                    break;
                case "Unfollow success":
                    $scope.user.following = _.filter($scope.user.following, function (person) {
                        return person.id !== user.id;
                    });
                    break;
                default:
                    $scope.alert("$scope.unfollowUser(" + user.id + "): " + "未知错误: " + result.data.message);
                    break;
            }
        }, function () {
            $scope.alert("$scope.unfollowUser(" + user.id + "): " + "无法连接服务器");
        });
    };








    $scope.addCardTopic = function () {
        if ($scope.my_topic) {
            for (let i in $scope.my_topic.split(",")) {
                let topic = $scope.my_topic.split(",")[i];
                if (topic) {
                    if ($scope.card_to_create.topics.length < $window.max.card.topics.length) {
                        $scope.my_topic_placeholder = "输入话题";
                        $scope.card_to_create.topics.push(topic.trim());
                    } else {
                        $scope.my_topic_placeholder = "话题数量太多了";
                    }
                } else {
                    $scope.my_topic_placeholder = "话题不能为空";
                }
            }
        }
        $scope.my_topic = "";
    };


    $scope.removeCardTopic = function (index) {
        $scope.card_to_create.topics = _.without($scope.card_to_create.topics, $scope.card_to_create.topics[index]);
    };

    $scope.removeAllCardTopics = function () {
        $scope.card_to_create.topics = [];
    };



    function parseLang(lang) {
        let map = {
            "C": "c++",
            "C++": "c++",
            "oc": "obj-c",
            "OC": "obj-c",
            "objective-c": "obj-c",
            "Objective-C": "obj-c"
        }
        if (map.hasOwnProperty(lang)) {
            return map[lang];
        }
        return lang;
    }


    function handleMarkdownLinks (field) {
        $(field).find("a").each(function(index, elem) {
            let url = $(elem).attr("href");
            $(elem)
                .attr("href", "javascript:void(0);")
                .off("click")
                .on("click", function() {
                    $scope.openExternalLink(url);
                });
        });
    }

    function handleMarkdownImages (field) {
        $(field).find("img")
            .addClass("img-thumbnail")
            .off("click")
            .on("click", function() {
                let src = $(this).attr("src");
                $scope.inspect_image(src);
            });
    }


    // 解析卡片 markdown 内容
    function parseCurrentCardMarkdown (card, field) {
        if (card == undefined || card.text == undefined) {
            bsAlert("parseCurrentCardMarkdown():", "卡片正文缺失", "alert-danger", -1);
        } else if (!marked) {
            bsAlert("parseCurrentCardMarkdown():", "Markdown 插件缺失", "alert-danger", -1);
        } else if (!DOMPurify) {
            bsAlert("parseCurrentCardMarkdown():", "DOMPurify 插件缺失", "alert-danger", -1);
        } else {
            let languages = [];
            marked.setOptions({
                renderer: new marked.Renderer(),
                highlight: function(code, language) {
                    let result = "<div class=\"alert-danger\">代码解析失败</div>";
                    let affix = "";
                    if (language) {
                        language = parseLang(language);
                        result = hljs.highlightAuto(code, [language]);
                    } else {
                        result = hljs.highlightAuto(code);
                        affix = " (自动识别)";
                    }
                    if (result.language == null) {
                        result.language = "无法识别语言类型";
                    }
                    languages.push(result.language + affix);
                    return result.value;
                },
                pedantic: false,
                gfm: true,
                breaks: true,
                sanitize: false,
                smartLists: true,
                smartypants: false,
                xhtml: false
            });
            var dirty = marked(card.text);
            var clean = DOMPurify.sanitize(dirty);
            $(field).html(clean);
            handleMarkdownLinks(field);
            handleMarkdownImages(field);
            // 处理不含有 <code> 的 <pre>
            $(field).find("pre").each(function(index, elem) {
                if ($(elem).has("code") == false) {
                    $(elem).addClass("line break-all");
                }
            });
            // 处理含有 <code> 的 <pre>
            $(field).find("pre > code").each(function(index, elem) {
                $(elem).addClass("hljs " + languages[index]);
                $(elem).closest("pre").wrap("<details open>");
                $(elem).closest("details").prepend("<summary class='bg-dark text-white px-2'>" + languages[index] + "</summary>");
            });
        }
    }



    $scope.viewCard = function (card) {
        if (!card || !card.id) {
            bsAlert("$scope.viewCard():","ERR-1 parameter 'card' was not provided","alert-danger");
            return false;
        }
        if ($scope.alwaysViewCardInAnotherPage) {
            $window.open("/card-page.html?card-id=" + card.id, "_blank");
            return true;
        }
        let loadingId = null;
        let isViewShared = false;
        if (arguments[1]) {
            if (typeof arguments[1] !== "string") {
                bsAlert("$scope.viewCard(\"" + card.title + "\"):","ERR-2 无法识别的附加指令: \"" + arguments[1] + "\"","alert-danger");
                return false;
            } else if (arguments[1] === "viewSharedCard"){
                isViewShared = true;
                loadingId = startLoading(max.loading.delay.time,"$scope.viewCard(\"" + card.title + "\",\"" + arguments[1] + "\")");
            } else {
                bsAlert("$scope.viewCard(\"" + card.title + "\"):","ERR-3 无法识别的附加指令: \"" + arguments[1] + "\"","alert-danger");
                return false;
            }
        } else {
            loadingId = startLoading(max.loading.delay.time,"$scope.viewCard(\"" + card.title + "\")");
        }

        $http({
            method: 'get',
            url: "http://" + $scope.webRoot + apis.get.card.byId,
            params: {
                cardId: card.id,
                start: 0,
                limit: $scope.max.get.card.comments.limit
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            stopLoading(loadingId);

            switch (result.data.message) {
                case "card load failed":
                    bsAlert("[ERROR] $scope.viewCard(): 卡片加载失败");
                    break;
                case "card load success":
                    if (result.data.card.hasOwnProperty("ilike") && !result.data.card.hasOwnProperty("iLike")) {
                        result.data.card.iLike = result.data.card.ilike;
                    }
                    // 判断是否正在查看【被转发的卡片】
                    if (isViewShared) {
                        if ($scope.current_card && result.data.card) {
                            $scope.current_card.share = result.data.card;
                            if (result.data.comments) {
                                $scope.current_card.share.comments = result.data.comments;
                            } else {
                                $scope.current_card.share.comments = null;
                            }
                        }
                    } else {
                        // 正在查看的卡片不是【被转发的卡片】
                        $scope.current_card = null;
                        // 在有些情况下，成功收到卡片数据然而渲染失败，仍会显示前次浏览的卡片内容。
                        // 所以在【显示 modal】并【渲染卡片】之前，有必要先清空卡片数据。
                        let cardView = $(".modal#view-card");
                        // 关闭之前打开的回复评论输入框 collapse
                        cardView.find(".collapse.border.show").removeClass("show");
                        cardView.modal("show");

                        if (result.data.card) {
                            $scope.current_card = result.data.card;
                            // 解析卡片的 WCML 内容
                            parseCurrentCardWCML();
                            // 解析卡片 markdown 内容
                            parseCurrentCardMarkdown(result.data.card, "#view-card #marked");
                            // 将服务器返回的卡片评论数组注入到卡片当中
                            if (result.data.comments) {
                                $scope.current_card.comments = result.data.comments;
                            } else {
                                $scope.current_card.comments = null;
                            }

                            // 如果当前卡片是 share 类型的卡片
                            if (card.type === "share" && $scope.current_card.type === "share") {
                                // 不幸的是, 此接口不会返回 share (即被转发的卡片), 所以下面的条件永远为假
                                if ($scope.current_card.share) {
                                    $scope.viewCard($scope.current_card.share, "viewSharedCard");
                                } else if (card.share) {
                                    // 然而本函数即使不用接口返回的数据, 也能从参数 card 中获取 share (即被转发的卡片)
                                    $scope.viewCard(card.share, "viewSharedCard");
                                } else {
                                    // 如果无法找到被转发的卡片的任何信息, 抛出异常
                                    bsAlert("[ERROR] $scope.viewCard(): 当前卡片是转发卡片, 但没有存储被转发卡片的信息");
                                }
                            }
                        }
                    }
                    break;
                default:
                    bsAlert("[ERROR] $scope.viewCard(): 未知错误：" + result.data.message);
                    break;
            }
        }, function () {
            stopLoading(loadingId);
            bsAlert("[ERROR] $scope.viewCard(): 与服务器连接失败");
        });
    };




    $scope.alert = function (title, content, alertClass) {
        return $window.bsAlert(title, content, alertClass);
    };

    $scope.confirm = function (params) {
        return $window.bsConfirm(params);
    };



    $scope.submitComment = function () {
        if (!$scope.current_card.id) {
            $scope.alert("submit comment", "卡片 ID 不能为空", "alert-danger");
            return;
        }
        if (!$scope.my_comment) {
            $scope.alert("submit comment", "评论不能为空", "alert-danger");
            return;
        }
        $window.submitComment();
    };

    $scope.likeComment = function (comment) {
        $window.likeComment(comment.id);
    };

    $scope.deleteComment = function (cardId, commentId) {
        $window.deleteComment(cardId, commentId);
    };

    $scope.likeCard = function (cardId) {
        $window.likeCard(cardId);
    };

    $scope.cancelLikeCard = function (cardId) {
        $window.cancelLikeCard(cardId);
    };

    $scope.shareCard = function (card) {
        shareCardService.shareCard(card, $scope);
    };

    $scope.shareCardToFriends_friendsSelected = function(friendsSelected) {
        $scope.$broadcast("share_card_to_friends_friends_selected", friendsSelected);
    };

    $scope.deleteCard = function (card) {
        $window.deleteCard(card);
    };

    $scope.inspect_image = function (imageUrl) {
        $window.inspect_image(imageUrl);
    };


    // 判断是否是从登录页面跳转过来的
    let loginTargetMap = {
        "my-social-circle": "mySocialCircle",
        // page 参数如果是 my-social-circle 就打开社交圈页面
        "my-cards": "cards",
        // page 参数如果是 my-cards 就打开我的卡片页面
        "homepage": "homepage"
        // page 参数如果是 homepage 就打开我的聊天页面
    };
    for (let pageName in loginTargetMap) {
        if ($window.location.search.indexOf("page=" + pageName) > 0) {
            $scope.gotoPage(loginTargetMap[pageName]);
        }
    }



    $scope.split = function (message, delimiter) {
        return message.split(delimiter);
    };



    $scope.getUsers = function (user, callback, errorCallback) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.search.users,
            params: {
                "username": user.username
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            switch (data.message) {
                case "Find success":
                    callback(data.users);
                    break;
                default:
                    errorCallback && errorCallback(data.message);
                    break;
            }
        }, function(err) {
            errorCallback && errorCallback(err);
        });
    };



    // 解析当前浏览的卡片的 WCML 内容
    // 此前为: 监听当前浏览的卡片的内容更新
    // 现改为: 被 AJAX 的 callback 调用
    function parseCurrentCardWCML () {
        if ($scope.current_card) {
            if ($scope.current_card.text) {
                let lines = $scope.current_card.text.split('\n');
                let plainTextIndexList = [];
                let scriptIndexList = [];
                let scriptSet = {};
                let isTableOpen = false;
                let tableOpenLineNo = null;
                for (let lineNo in lines) {
                    let line = lines[lineNo];
                    let descEnd = line.indexOf("]");
                    let begin = line.indexOf("(");
                    let end = line.indexOf(")");
                    // 判断是否是 Markdown 语法 (使用 Markdown 的语法插入代码)
                    if (line.trim() === "```" || (line.startsWith("*") && line.endsWith("*")) || line.startsWith("#")) {
                        $scope.current_card.isMarkdownEnabled = true;
                    }
                    // #1 Spot Markdown Image Link
                    if (line.startsWith("![") && descEnd > 1 && descEnd < begin && begin < end) {
                        let imageRemark = line.substring(begin + 1, end);
                        console.log("$watch('current_card'): image link was found in card text: '" + imageRemark + "', lineNO: " + lineNo);
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "markdown-image-link",
                            "imageSrc": imageRemark,
                            "imageSrcType": "external"
                        };
                        if (imageRemark.startsWith("图片-")) {
                            scriptSet["line-" + lineNo].imageSrcType = "local";
                        } else if (imageRemark.indexOf(".") < 0) {
                            // 图片地址不正确
                            // let badLineNo = scriptIndexList.pop();
                            delete scriptSet["line-" + lineNo];
                            plainTextIndexList.push(parseInt(lineNo));
                        }
                        // #2 Spot Wecard Version
                    } else if (line.startsWith("@wecard_version(") && line.indexOf(")") > 15) {
                        console.log("$watch('current_card'): script 'version' was found in card text: '" + line + "', lineNO: " + lineNo);
                        $scope.current_card.scriptIsEnabled = true;
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "wecard-version",
                            "versionNO": false,
                            "isUpdateAvailable": false
                        };
                        // #3 Spot User Icon
                    } else if (line.indexOf("@iconify(user,") >= 0 && line.indexOf(")") > 13) {
                        let indexBegin = line.indexOf("@iconify(user,");
                        let stringBefore = line.substr(0, indexBegin);

                        let stringAfter = line.substr(indexBegin);
                        let indexEnd = stringAfter.indexOf(")");
                        let stringScript = stringAfter.substring(0, indexEnd + 1);

                        stringAfter = stringAfter.substr(stringScript.length);
                        let lineSubStrings = [stringBefore, stringScript, stringAfter];
                        // 此代码有 bug, 此代码假定脚本在字符串的中间, 而没有考虑脚本在字符串开始或者末尾的情况
                        let indexParametersBegin = stringScript.indexOf("(");
                        let parametersLine = stringScript.substr(indexParametersBegin);
                        if (parametersLine[0] === "(") {
                            parametersLine = parametersLine.substr(1);
                        }
                        if (parametersLine[parametersLine.length - 1] === ")") {
                            parametersLine = parametersLine.substr(0, parametersLine.length - 1);
                        }
                        let parameters = parametersLine.trim().split(",");
                        for (let i in parameters) {
                            parameters[i] = parameters[i].trim();
                        }
                        let iconId = "icon-user-" + getRandomString() + "-" + Date.now();
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "iconify-user",
                            "lineSubStrings": lineSubStrings,
                            "scriptPositions": [1],
                            "plainTextPositions": [0,2],
                            "iconId": iconId
                        };
                        let username = parameters[2];
                        let user = {
                            username: username
                        };
                        $scope.getUsers(user, function(Users) {
                            if (Users) {
                                if (Users.length) {
                                    let User = Users[0];
                                    let icon = $(".iconify-user[data-icon-id='" + iconId + "']");
                                    icon.find(".original-script").hide();
                                    icon.find("img").attr("src", User.avatarUrl)
                                        .show()
                                        .attr("data-toggle", "popover")
                                        .attr('title', $scope.getUserInfoTitle(User))
                                        .attr("data-content", $scope.getUserInfoContent(User));
                                }
                            }
                        }, function (errMsg) {
                            let errMsgZHCN = "";
                            switch (errMsg) {
                                case "Please login":
                                    errMsgZHCN = "请先登录才能查看此用户";
                                    break;
                                case "Username cannot be empty":
                                    errMsgZHCN = "用户名为空, 用户不存在";
                                    break;
                                case "Find fail":
                                    errMsgZHCN = "查询用户失败";
                                    break;
                            }
                            let icon = $(".iconify-user[data-icon-id='" + iconId + "']");
                            icon.find(".original-script").hide();
                            icon.find("img").replaceWith("<span class='d-inline-block user-avatar-xs rounded-circle bg-dark'" +
                                " data-toggle='tooltip' title='" + errMsgZHCN+ "'></span>");
                        });
                        // #4 Spot Text of URL
                    } else if (line.indexOf("http://") === 0 || line.indexOf("https://") === 0) {
                        $scope.current_card.scriptIsEnabled = true;
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "text-url",
                            "a-link-class": "",
                            "a-link-href": line,
                            "a-link-text": line
                        };
                        // #5 Spot Table
                    } else if (line.startsWith("@table(begin)") || line.startsWith("@table(open)")) {
                        isTableOpen = true;
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "table",
                            "marks": ["begin", "open"],
                            "tableOpenLineNo": lineNo,
                            "tableId": "card-table-" + getRandomString(),
                            "tableContentLines": []
                        };
                        tableOpenLineNo = lineNo;
                    } else if (line.startsWith("@table(end)") || line.startsWith("@table(close)")) {
                        isTableOpen = false;
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "table",
                            "marks": ["end", "close"],
                            "tableOpenLineNo": tableOpenLineNo
                        };
                    } else if (isTableOpen && tableOpenLineNo) {
                        // 将代码所在的行号 push 到 indexList 中
                        scriptIndexList.push(parseInt(lineNo));
                        // 将代码解析的结果存储于 set 中
                        scriptSet["line-" + lineNo] = {
                            "type": "table",
                            "marks": ["line", "content", "contentLine"],
                            "tableOpenLineNo": tableOpenLineNo
                        };
                        scriptSet["line-" + tableOpenLineNo].tableContentLines.push(line);
                    } else {
                        console.log("$watch('current_card'): this line is plain text, lineNO: " + lineNo + ", line: \"" + line + "\"");
                        plainTextIndexList.push(parseInt(lineNo));
                    }
                    lineNo ++;
                }
                // 将卡片文本以换行符切割，生成字符串数组，注入到卡片数据中
                $scope.current_card.contentLines = lines;
                // 将卡片文本 lines 数组中未被解析的行的行号记录下来，注入到卡片数据中
                $scope.current_card.plainTextIndexList = plainTextIndexList;
                // 将卡片文本 lines 数组中被解析成功的【脚本信息】注入到卡片数据中
                $scope.current_card.scriptIndexList = scriptIndexList;
                $scope.current_card.scriptSet = scriptSet;
            }
        }
    }


    $scope.getScriptInfoByLineNumber = function ($index) {
        if ($scope.current_card) {
            if ($scope.current_card.scriptIndexList && $scope.current_card.scriptSet) {
                if ($scope.current_card.scriptIndexList.includes($index)) {
                    let value = $scope.current_card.scriptSet["line-" + $index];
                    if (value) {
                        return value;
                    }
                }
            }
        }
    };


    $scope.getCardImageByImageRemark = function (remark) {
        if ($scope.current_card && $scope.current_card.images) {
            let imageList = $scope.current_card.images.filter(function (image) {
                if (image.remark === remark) {
                    return true;
                } else {
                    return false;
                }
            });
            if (imageList && imageList.length) {
                return imageList[0];
            }
        }
        return false;
    };


    $scope.getCardImageSrcByLineNumber = function ($index) {
        let scriptInfo = $scope.getScriptInfoByLineNumber($index);
        if (scriptInfo) {
            if (scriptInfo.imageSrc) {
                switch (scriptInfo.imageSrcType) {
                    case "local":
                        let cardImage = $scope.getCardImageByImageRemark(scriptInfo.imageSrc);
                        if (cardImage && cardImage.url) {
                            return cardImage.url;
                        } else {
                            console.log("getCardImageSrcByLineNumber: error: cardImage not found");
                        }
                        break;
                    case "external":
                        return scriptInfo.imageSrc;
                        break;
                }
            }
        }
        return false;
    };


    $scope.getLinkableInfo = function (messageText) {
        if(!messageText) {
            return undefined;
        }
        let newText = messageText;
        let url = null;
        let a = null;
        let textPrev = null;
        let textNext = null;

        const beginPattern = ["http://", "https://", "ftp://", "www.", "wap."];
        const endPattern = [".com", ".net", ".org", ".edu", ".gov", ".cn", ".us", ".jp", ".hk", ".tw", ".uk", ".fr", ".de", ".sg", ".it", ".tv", ".io", ".top"];

        let URL_FOUND = false;
        for (let itBegin = 0; itBegin < beginPattern.length; ++itBegin) {
            // locate begin position "www."
            let begin = newText.indexOf(beginPattern[itBegin]);
            if (begin >= 0) {
                for (it = 0; it < endPattern.length; ++it) {
                    // locate end position ".com"
                    let end = newText.indexOf(endPattern[it]);
                    if (end > begin + beginPattern[itBegin].length) {
                        // locate real end position. eg. "www.example.com.cn/login"
                        while (end + endPattern[it].length < newText.length &&
                        [' ', '\n', '\'', '\"', '>', '<'].includes(newText[end + endPattern[it].length]) &&
                        newText[end + endPattern[it].length].charAt(0) < 255) {
                            ++end;
                        }
                        let Protocol = itBegin < 3 ? "" : "http://";
                        url = Protocol + newText.substring(begin, end + endPattern[it].length);
                        a = newText.substring(begin, end + endPattern[it].length);
                        textPrev = newText.substr(0, begin);
                        textNext = newText.substr(end + endPattern[it].length);
                        URL_FOUND = true;
                        break;
                    }
                }
            }
            if (URL_FOUND == true) {
                break;
            }
        }

        if (URL_FOUND == true) {
            return [newText, url, textPrev, a, textNext];
        } else {
            return undefined;
        }
    };


    $scope.openExternalLink = function (link) {
        let name = "external-link-" + getRandomString();
        let a = $("<a>");
        a.attr({
            "id": name,
            "href": "javascript:void(0);",
            "class": "text-danger text-decoration-none text-hover-decoration-none"
        }).text(link);

        $scope.confirm( {
            "title": "即将打开外部链接",
            "content": "<div class='" + name + "'>" +
                "确认打开外部链接 " + a[0].outerHTML + " 吗?" +
                "</div>",
            "alertClass": "alert-danger",
            "confirmText": "打开链接",
            "confirmCallback": function () {
                $("#" + name).attr({
                    "href": link,
                    "target": "_blank"
                })[0].click();/* 如果不加上 [0] 则 click 事件无效 */
                $("#" + name).attr({
                    "href": "javascript:void(0);"
                }).removeAttr("target");
            },
            "rejectText": "取消",
            "rejectCallback": function (event) {
                $("." + name).text("已取消");
                $(event.target)
                    .parent()
                    .find("button")
                    .off("click")
                    .on("click", function() {
                        $window.bsConfirmCloseModal();
                    });
                $timeout(function () {
                    $window.bsConfirmCloseModal();
                }, 1000);
            }
        });
    };


























    // 以下内容由 Homepage Controller 迁移至此





    // 告诉服务器用户端已经进入聊天窗口
    function setChatLocation(location) {
        // 如果退出聊天窗口，则存储当前浏览的聊天窗口
        //（但如果当前聊天窗口不存在，就保留之前存储的聊天窗口）
        if (["external"].includes(location) && !["external"].includes($scope.currentChatItemId)) {
            $scope.prevChatItemId = $scope.currentChatItemId;
            $scope.prevChatItemType = $scope.currentChatItemType;
            $scope.prevChatItemName = $scope.currentChatItemName;
            $scope.prevChatItemAvatar = $scope.currentChatItemAvatar;
        }
        $scope.currentChatItemId = location;
        console.log("setChatLocation(" + location + ")");
    }




    // 进入聊天室
    $scope.openChatRoom = function (chatItem) {
        // var beginTime = new Date().getTime();
        // // 注意: 此时并未开始执行页面跳转, 所以我们不应该使用 prevPageLocation
        // if (["homepage"].includes($scope.pageLocation)) {
        //     // Start Animation
        //     $scope.gotoPage("trans-home-chat");
        // }
        // // Start Loading
        // let loadingId = startLoading(max.loading.delay.time, "$scope.openChatRoom(\"" + chatItem.id + "\")");
        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.get.messages.limited,
        //     params: {
        //         "chatItemId": chatItem.id,
        //         "offset": 0,
        //         "limit": max.get.messages.limit
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     stopLoading(loadingId);
        //     let data = result.data;
        //     if (data.message === "Please login") {
        //         gotoLogin("index.html");
        //     } else if (data.message === "Load fail") {
        //         $scope.alert("[ERROR] open chat room: 加载失败");
        //     } else if (data.message === "Load success") {
        //         $scope.gotoPage("chat", "chat", chatItem.id);
        //         $scope.currentChatItemType = chatItem.type;
        //         // 好友房间
        //         if (chatItem.type === "friend") {
        //             // 缓存当前好友的个人信息（为防止当前 chatItem 中的 friend 中的信息不全，从 allChatItems 中获取好友的完整信息）
        //             $scope.current_friend = _.filter($scope.userData.allChatItems, function (e) {
        //                 return e.id === chatItem.id && e.type === "friend" && e.friend;
        //             })[0].friend;
        //             // 加载好友的备注名
        //             if (chatItem.friend) {
        //                 if (chatItem.friend.remark) {
        //                     $scope.currentChatItemName = chatItem.friend.remark;
        //                 } else if (chatItem.friend.username) {
        //                     $scope.currentChatItemName = chatItem.friend.username;
        //                 } else if (chatItem.friend.id) {
        //                     $scope.currentChatItemName = "用户" + chatItem.friend.id;
        //                 }
        //             } else {
        //                 $scope.alert("[ERROR] open chat room: 聊天项类型为 'friend', 但是聊天项中没有 'friend' 对象");
        //             }
        //             $scope.currentChatItemAvatar = chatItem.friend.avatarUrl;
        //         } else if (chatItem.type === "room") {
        //             $scope.currentChatItemName = chatItem.room.roomName;
        //             $scope.currentChatItemAvatar = chatItem.room.avatarUrl;
        //         } else if (chatItem.type === "system") {
        //             $scope.currentChatItemName = "系统消息";
        //             $scope.currentChatItemAvatar = "img/icon-system.jpg";
        //         } else if (chatItem.type === "inform") {
        //             $scope.currentChatItemName = "验证消息";
        //             $scope.currentChatItemAvatar = "img/icon-inform.jpg";
        //         } else {
        //             $scope.currentChatItemName = "未知聊天室";
        //             $scope.currentChatItemAvatar = "";
        //         }
        //         // 显示刚加载的聊天室内的消息（一定是最新的消息）
        //         $scope.current.sizeOf.messagesLoadedLimitedUpdate(data.messages.length);
        //         $scope.chatMessageList = data.messages.slice().reverse();
        //         // 遍历每一个消息
        //         _.each($scope.chatMessageList, function (msg) {
        //             // 将消息中的 URL 网址转换成 a 链接
        //             msg.url = append_message_linkable(msg.text);
        //         });
        //         const timeElapsed = new Date().getTime() - beginTime;
        //         // 滚动窗口到最新的消息
        //         if (["friend", "room"].includes($scope.currentChatItemType)) {
        //             setTimeout(() => {
        //                 $window.gotoMessage('last',false);
        //             }, 1010 - timeElapsed);
        //             // 1000 is for page switch animation and 10 is for angular putting messages in DOM
        //         }
        //         // 滚动窗口到最新的请求消息或系统消息
        //         if (["system", "inform"].includes($scope.currentChatItemType)) {
        //             setTimeout(() => {
        //                 scrollMessageIntoView($('.chat-window .card:last').get(0), false);
        //             }, 1010 - timeElapsed);
        //             // 1000 is for page switch animation and 10 is for angular putting messages in DOM
        //         }
        //         // 将未读消息数设为 0
        //         $scope.unread_message_count = 0;
        //         $scope.userData.chatList = _.map($scope.userData.chatList, function (e) {
        //             if (e.id === chatItem.id) {
        //                 e.unreadNum = 0;
        //             }
        //             return e;
        //         });
        //     } else {
        //         $scope.alert("[ERROR] open chat room: 未知错误: " + data.message);
        //     }
        // }, function () {
        //     stopLoading(loadingId);
        //     $scope.alert("[ERROR] open chat room: 与服务器连接失败");
        // });


        if (["homepage"].includes($scope.pageLocation)) {
            // Start Animation
            $scope.gotoPage("trans-home-chat");
        }

        /* mock data (messages in openChatRoom) */
        data = {
            messages: [{
                id: "1",
                text: "hi",
                messageCreateTime: "yesterday",
                user: {
                    id: "1",
                    username: "user",
                    "avatarUrl": "https://media.discordapp.net/attachments/907832332537434152/993550228315701358/00096489135.jpg"
                }
            },{
                id: "2",
                text: "hello",
                messageCreateTime: "yesterday",
                user: {
                    id: "1",
                    username: "user",
                    "avatarUrl": "https://media.discordapp.net/attachments/907832332537434152/993550228315701358/00096489135.jpg"
                }
            },{
                id: "3",
                text: "who is this?",
                messageCreateTime: "just now",
                user: {
                    id: "2",
                    username: "someone",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            },{
                id: "4",
                text: "your follower",
                messageCreateTime: "just now",
                user: {
                    id: "1",
                    username: "user",
                    "avatarUrl": "https://media.discordapp.net/attachments/907832332537434152/993550228315701358/00096489135.jpg"
                }
            },{
                id: "5",
                text: "emmmm",
                messageCreateTime: "just now",
                user: {
                    id: "2",
                    username: "someone",
                    "avatarUrl": "https://cdn.discordapp.com/avatars/934427574594076682/1290ff12e31175db26ab1aa62f0ae0b9.webp?size=1280"
                }
            }]
        };

        $scope.gotoPage("chat", "chat", chatItem.id);
        $scope.currentChatItemType = chatItem.type;
        // 好友房间
        if (chatItem.type === "friend") {
            // 缓存当前好友的个人信息
            $scope.current_friend = chatItem.friend;
            // set friend's name and show it on chat window top bar
            $scope.currentChatItemName = chatItem.friend.remark + " (" + chatItem.friend.username + ")";
            $scope.currentChatItemAvatar = chatItem.friend.avatarUrl;
        } else if (chatItem.type === "room") {
            // set room's name and show it on chat window top bar
            $scope.currentChatItemName = chatItem.room.remark + " (" + chatItem.room.roomName + ")";
            $scope.currentChatItemAvatar = chatItem.room.avatarUrl;
        } else if (chatItem.type === "system") {
            $scope.currentChatItemName = "系统消息";
            $scope.currentChatItemAvatar = "img/icon-system.jpg";
        } else if (chatItem.type === "inform") {
            $scope.currentChatItemName = "验证消息";
            $scope.currentChatItemAvatar = "img/icon-inform.jpg";
        } else {
            $scope.currentChatItemName = "未知聊天室";
            $scope.currentChatItemAvatar = "";
        }
        // 显示刚加载的聊天室内的消息（一定是最新的消息）
        $scope.current.sizeOf.messagesLoadedLimitedUpdate(data.messages.length);
        $scope.chatMessageList = data.messages;
        // 遍历每一个消息
        _.each($scope.chatMessageList, function (msg) {
            // 将消息中的 URL 网址转换成 a 链接
            msg.url = append_message_linkable(msg.text);
        });
        // 将未读消息数设为 0
        $scope.unread_message_count = 0;
        $scope.userData.chatList = _.map($scope.userData.chatList, function (e) {
            if (e.id === chatItem.id) {
                e.unreadNum = 0;
            }
            return e;
        });
        $scope.$apply();
    };







    // 加载更多历史消息
    $scope.load_more_messages = function () {
        let loadingId = startLoading(max.loading.delay.time, "$scope.load_more_messages()");
        let chatItem = $scope.get_current_chat_item();
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.get.messages.limited,
            params: {
                "chatItemId": chatItem.id,
                "offset": $scope.chatMessageList.length,
                "limit": $scope.max.get.messages.limit
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            stopLoading(loadingId);
            let data = result.data;
            if (data.message === "Please login") {
                gotoLogin("index.html");
            } else if (data.message === "Load fail") {
                $scope.alert("[ERROR] load more messages: 加载失败");
            } else if (data.message === "Load success") {
                // 显示聊天室内的消息（一定不是最新的消息）
                $scope.current.sizeOf.messagesLoadedLimitedUpdate(data.messages.length);
                let messages_loaded = data.messages.slice();
                _.each(messages_loaded, function (msg) {
                    // 将消息中的 URL 网址转换成 a 链接
                    msg.url = append_message_linkable(msg.text);
                    // 把刚加载的消息数组载入到聊天窗口中
                    $scope.chatMessageList.unshift(msg);
                });
            } else {
                $scope.alert("[ERROR] load more messages: 未知错误: " + data.message);
            }
        }, function () {
            stopLoading(loadingId);
            $scope.alert("[ERROR] load more messages: 与服务器连接失败");
        });
    };




    // $scope.splitMessage = function (message) {
    //     return message.split("\n");
    // };



    $scope.sendMessage = function () {
        const theUser = $scope.userData.user;
        if (!$scope.messageToSend || !theUser) {
            console.log("send message: message or user is undefined.");
            return false;
        }
        // start loading
        var fakeId = "fake-id-" + Date.now();
        let roomMember = null;
        if ($scope.currentChatItemType === "room") {
            roomMember = theUser;
            roomMember.roomRemark = " ";// cannot be empty string because empty string is false
        }
        $scope.chatMessageList.push({
            id: fakeId,
            user: theUser,
            roomMember: roomMember,
            text: $scope.messageToSend,
            sendStatus: "sending"
        });
        // go to bottom of window
        if ($window.gotoMessage) {
            $window.gotoMessage('last',true);
        }

        // const messageText = $scope.messageToSend;
        $scope.messageToSend = "";

        // $http({
        //     method: 'GET',
        //     url: "http://" + $scope.webRoot + apis.sendMessage,
        //     params: {
        //         "text": messageText,
        //         "chatItemId": $scope.currentChatItemId
        //     },
        //     crossDomain: true,
        //     withCredentials: true
        // }).then(function (result) {
        //     // stop loading
            $scope.chatMessageList = $scope.chatMessageList.map((message) => {
                if (message.id === fakeId) {
                    message.sendStatus = "done"
                }
                return message;
            });
            console.log("send message: Send success.");

        //     let isSendSuccess = false;
        //     switch (result.data.message) {
        //         case "Please login":
        //             gotoLogin("index.html");
        //             break;
        //         case "ChatItem does not exist":
        //             $scope.alert("[ERROR] send message: 该聊天项不存在");
        //             break;
        //         case "He/she is not your friend":
        //             $scope.alert("[ERROR] send message: 该用户不是你的好友");
        //             break;
        //         case "Text cannot be empty":
        //             $scope.alert("[ERROR] send message: 消息内容不能为空");
        //             break;
        //         case "Send fail":
        //             $scope.alert("[ERROR] send message: 发送失败");
        //             break;
        //         case "Send success":
        //             isSendSuccess = true;
        //             break;
        //         default:
        //             $scope.alert("[ERROR] send message: 未知错误 " + result.data.message);
        //             break;
        //     }
        //     if (!isSendSuccess) {
        //         if (confirm("发送消息失败，是否恢复输入框中的消息？")) {
        //             $scope.messageToSend = messageText;
        //         }
        //     }
        // });
    };




    $scope.removeChatItem = function (chatItemId) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.removePortal,
            params: {
                "chatItemId": chatItemId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            switch (result.data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "ChatItemId cannot be empty":
                    $scope.alert("删除失败，错误信息：" + result.data.message);
                    break;
                case "ChatItem does not exist":
                    $scope.alert("删除失败，错误信息：" + result.data.message);
                    break;
                case "Remove fail":
                    $scope.alert("删除失败，错误信息：" + result.data.message);
                    break;
                case "Remove success":
                    // 删除聊天项列表中对应的聊天项
                    $scope.userData.chatList = _.filter($scope.userData.chatList, function (chatItem) { return chatItem.id !== chatItemId; });
                    // 删除已经打开的聊天窗口
                    if ($scope.prevChatItemId === chatItemId) {
                        $scope.prevChatItemId = "external";
                    }
                    break;
            }
        });
    };





    // 添加好友分组
    $scope.addGroup = {
        placeholder: "不能含有特殊符号",
        groupName: "",
        isValid: true
    };

    $scope.createGroup = function () {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.createFriendGroup,
            params: {
                "groupName": $scope.addGroup.groupName
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            switch (data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "GroupName cannot be empty":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "分组名不能为空";
                    $scope.addGroup.isValid = false;
                    break;
                case "GroupName is too long":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "分组名过长";
                    $scope.addGroup.isValid = false;
                    break;
                case "GroupName contains special char":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "分组名含有非法字符";
                    $scope.addGroup.isValid = false;
                    break;
                case "GroupName already exists":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "该分组名已经存在";
                    $scope.addGroup.isValid = false;
                    break;
                case "Create fail":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "分组添加失败";
                    $scope.addGroup.isValid = false;
                    break;
                case "Create success":
                    $scope.addGroup.groupName = "";
                    $scope.addGroup.placeholder = "分组添加成功";
                    $scope.addGroup.isValid = true;
                    $(".modal").modal("hide");
                    break;
                default:
                    console.log("[ERROR] create group: 未知错误 " + data.message);
                    break;
            }
        }, function (error) {
            $scope.alert("create group: 发生错误 " + error.status);
        });
    }

    // 删除好友分组
    $scope.removeGroup = function (groupId) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.removeFriendGroup,
            params: {
                "groupId": groupId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            switch (data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "GroupId cannot be empty": break;
                case "Group is not empty":
                    $scope.alert("[ERROR] remove group: 只能删除空分组");
                    break;
                case "Group does not exist":
                    $scope.alert("[ERROR] remove group: 分组不存在");
                    break;
                case "Remove fail":
                    $scope.alert("[ERROR] remove group: 分组删除失败");
                    break;
                case "Remove success":
                    $scope.userData.groups = _.filter($scope.userData.groups, function (group) { return group.id !== groupId; });
                    break;
                default:
                    $scope.alert("[ERROR] remove group: 未知错误 " + data.message);
                    break;
            }
        }, function (error) {
            $scope.alert("[ERROR] remove group: 发生错误 " + error.status);
        });
    }



    // 创建房间
    $scope.createRoom = {
        placeholder: "请输入房间名",
        roomName: "",
        isValid: true
    };

    $scope.createRoomSubmit = function () {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.createRoom,
            params: {
                "roomName": $scope.createRoom.roomName
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            switch (data.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "RoomName cannot be empty":
                    $scope.alert("[ERROR] create room: 房间名不能为空");
                    break;
                case "RoomName is too long":
                    $scope.alert("[ERROR] create room: 房间名过长");
                    break;
                case "RoomName contains special char":
                    $scope.alert("[ERROR] create room: 房间名含有非法字符");
                    break;
                case "Create fail":
                    $scope.alert("[ERROR] create room: 创建房间失败");
                    break;
                case "Create success":
                    $scope.alert("create room", "创建房间成功", "alert-success");
                    $(".modal").modal("hide");
                    break;
                case "Grade error":
                    $scope.alert("[ERROR] create room: 用户等级过低, 无法创建房间");
                    $(".modal").modal("hide");
                    break;
                default:
                    $scope.alert("[ERROR] create room: " + data.message);
                    break;
            }
        }, function (error) {
            $scope.alert("[ERROR] create room: 发生错误 " + error.status);
        });
    }



    // 个人主页 - 查看头像
    $scope.getUserInfoAvatar = function (friend) {
        let wrapper = $("<div>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        let img = $("<img>").addClass("img-fluid").attr("src", friend.avatarUrl).appendTo(wrapper);
        return wrapper.html();
    }


    // 个人主页 - 标题
    $scope.getUserInfoTitle = function (friend) {
        let wrapper = $("<div>");
        wrapper.append("<span>" + friend.nickname +
            ($scope.debugMode.isEnabled ? "<small>(" + friend.username + ")</small>" : "") +
            " 的个人信息</span>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        return wrapper.html();
    }


    // 个人主页 - 内容
    $scope.getUserInfoContent = function (friend) {
        // console.log(JSON.stringify(friend));
        let container = $("<div>");
        let wrapper = $("<div>").addClass("popover-wrapper").appendTo(container);
        let background = $("<div>").addClass("background-image").css({
            "background-image": "url(" + friend.styleImgUrl + ")"
        }).appendTo(wrapper);
        let foreground = $("<div>").addClass("popover-content p-3").appendTo(wrapper);
        let field = $("<div>").addClass("w-100 text-center").appendTo(foreground);
        field.append("<div><img class='user-avatar-md img-thumbnail border' src='" + friend.avatarUrl + "'></div>");
        let userName = $("<p class='mt-1 mb-1'>" + friend.username + "</p>").appendTo(field);
        userName.append("<span class='fa fa-" + (friend.gender === "male" ? "mars text-primary" : "venus text-danger") + " ml-1'></span>");
        let moreInfo = $("<p>").addClass("mt-1 mb-2").appendTo(field);
        if (friend.city) {
            moreInfo.append("<span>城市 " + friend.city + "</span>");
        } else if (friend.grade) {
            moreInfo.append("<span>等级 " + friend.grade + "</span>");
        } else {
            moreInfo.append("<small>" + friend.id + "</small>");
        }
        $("<button>").addClass("btn btn-sm btn-primary").css({
            opacity: 0.8
        }).text("查看他的卡片").appendTo(field);
        return container.html();
    }


    // 加好友表单 - 标题
    $scope.getAddFriendTitle = function (friend) {
        let wrapper = $("<div>");
        wrapper.append("<span>填写请求信息</span>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        return wrapper.html();
    }



    // 加好友表单 - 内容
    $scope.getAddFriendContent = function (friend) {
        let container = $("<div>");
        let wrapper = $("<div>").addClass("popover-wrapper").appendTo(container);
        let background = $("<div>").addClass("background-image").css({
            "background-image": "url(" + friend.styleImgUrl + ")",
            "height": "200px"
        }).appendTo(wrapper);
        let foreground = $("<div>").addClass("popover-content").css({
            "height": "200px"
        }).appendTo(wrapper);
        let field = $("<div>").addClass("field w-100 text-center").appendTo(foreground);
        // input request message
        let row1 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>验证信息</div>").appendTo(field);
        let inputInfo = $("<textarea class='form-control form-control-sm w-100' style='font-size: .75rem;line-height: 1.2;'>");
        let col1 = $("<div class='col-7'>").append(inputInfo).appendTo(row1);
        // input remark
        let row2 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>设置备注</div>").appendTo(field);
        let inputRemark = $("<input type='text' class='form-control form-control-sm w-100' style='font-size: .75rem;line-height: 1.2;'>");
        let col2 = $("<div class='col-7'>").append(inputRemark).appendTo(row2);
        // select group
        let selectGroup = $("<select class='form-control form-control-sm w-100' style='font-size: .75rem;line-height: 1.2;'>");
        for (let i in $scope.userData.groups) {
            let group = $scope.userData.groups[i];
            selectGroup.append("<option value='" + group.id + "'>" + group.groupName + "</option>");
        }
        let row3 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>选择分组</div>").appendTo(field);
        let col3 = $("<div class='col-7'>").append(selectGroup).appendTo(row3);
        // send request
        $("<button>").addClass("btn btn-sm btn-primary").css({
            opacity: 0.8
        }).attr("onclick", "sendAddFriendRequest(" + angular.toJson(friend) + ",this)")
            .html("<span class='upper'>请求加好友</span>")
            .appendTo(field);
        return container.html();
    }





    // 同意加好友表单 - 标题
    $scope.getAgreeAddFriendTitle = function (request) {
        let wrapper = $("<div>");
        wrapper.append("<span>同意 " + request.requester.nickname + "(" + request.requester.username + ") 的加好友请求</span>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        return wrapper.html();
    }



    // 同意加好友表单 - 内容
    $scope.getAgreeAddFriendContent = function (request) {
        let container = $("<div>");
        let wrapper = $("<div>").addClass("popover-wrapper").appendTo(container);
        let background = $("<div>").addClass("background-image").css({
            "height": "145px"
        }).appendTo(wrapper);
        let foreground = $("<div>").addClass("popover-content").css({
            "height": "145px"
        }).appendTo(wrapper);
        let field = $("<div>").addClass("field w-100 pt-3 text-center").appendTo(foreground);
        // input remark
        let row2 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>设置备注</div>").appendTo(field);
        let inputRemark = $("<input type='text' class='form-control form-control-sm w-100' style='font-size: .75rem;line-height: 1.2;'>");
        let col2 = $("<div class='col-7'>").append(inputRemark).appendTo(row2);
        // select group
        let selectGroup = $("<select class='form-control form-control-sm w-100' style='font-size: .75rem;line-height: 1.2;'>");
        for (let i in $scope.userData.groups) {
            let group = $scope.userData.groups[i];
            selectGroup.append("<option value='" + group.id + "'>" + group.groupName + "</option>");
        }
        let row3 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>选择分组</div>").appendTo(field);
        let col3 = $("<div class='col-7'>").append(selectGroup).appendTo(row3);
        // send request
        $("<button>").addClass("btn btn-sm btn-primary").css({
            opacity: 0.8
        }).attr("onclick", "agreeAddFriendRequest(" + angular.toJson(request) + ",this)")
            .html("<span class='upper'>加为好友</span>")
            .appendTo(field);
        return container.html();
    }


    $scope.refuseAddFriendRequest = function (request, button) {
        refuseAddFriendRequest(request, button);
    }




    // 如果我的房间列表中有这个房间，就返回 false, 否则返回 true
    $scope.join_room_is_ok = function (userId, roomId) {
        if (!userId) {
            $scope.alert("join_room_is_ok:", "userId 没给");
            return false;
        } else if (!roomId) {
            $scope.alert("join_room_is_ok:", "roomId 没给");
            return false;
        }

        if (!$scope.userData.rooms) {
            return false;
        } else {
            let roomsHasThisRoom = _.some($scope.userData.rooms, function (room) {
                return room.id === roomId;
            });
            return !roomsHasThisRoom;
        }
        return false;
    }



    // 加入房间表单 - 标题
    $scope.getJoinRoomTitle = function (room) {
        let wrapper = $("<div>");
        wrapper.append("<span><i class='fa fa-home mr-1'></i>填写请求信息</span>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        return wrapper.html();
    }



    // 加入房间表单 - 内容
    $scope.getJoinRoomContent = function (room) {
        let container = $("<div>");
        let wrapper = $("<div>").addClass("popover-wrapper").appendTo(container);
        let background = $("<div>").addClass("background-image").appendTo(wrapper);
        let foreground = $("<div>").addClass("popover-content").appendTo(wrapper);
        let field = $("<div>").addClass("field w-100 pt-3 text-center").appendTo(foreground);
        // input request message
        let row1 = $("<div class='row mb-3'>").append("<div class='col-4 d-flex align-items-center justify-content-end'>验证信息</div>").appendTo(field);
        let inputInfo = $("<textarea class='form-control form-control-sm w-100' rows='4' style='font-size: .75rem;line-height: 1.2;'>");
        let col1 = $("<div class='col-7'>").append(inputInfo).appendTo(row1);
        $("<button>").addClass("btn btn-sm btn-primary").css({
            opacity: 0.8
        }).attr("onclick", "sendJoinRoomRequest(" + angular.toJson(room) + ",this)")
            .html("<span class='upper'>申请加入</span>")
            .appendTo(field);
        return container.html();
    }



    // 同意加入房间表单 - 标题 (已废弃)
    $scope.getAgreeJoinRoomTitle = function (request) {
        let wrapper = $("<div>");
        wrapper.append("<span>同意 " + request.requester.nickname + "(" + request.requester.username + ") 的加房间请求</span>");
        let button = $("<i>").addClass("fa fa-lg fa-times-circle mb-1 text-muted float-right").attr("onclick", "hidePopover($(this).closest('.popover'))").appendTo(wrapper);
        return wrapper.html();
    }



    // 同意加入房间表单 - 内容 (已废弃)
    $scope.getAgreeJoinRoomContent = function (request) {
        let container = $("<div>");
        let wrapper = $("<div>").addClass("popover-wrapper").appendTo(container);
        let background = $("<div>").addClass("background-image").appendTo(wrapper);
        let foreground = $("<div>").addClass("popover-content").appendTo(wrapper);
        let field = $("<div>").addClass("field w-100 pt-3 text-center").appendTo(foreground);
        // send request
        $("<button>").addClass("btn btn-sm btn-primary").css({
            opacity: 0.8
        }).attr("onclick", "agreeJoinRoomRequest(" + angular.toJson(request) + ",this)")
            .html("<span class='upper'>同意加入房间</span>")
            .appendTo(field);
        return container.html();
    }


    // 同意加入房间 - 提交
    $scope.agreeJoinRoomRequest = function (request, button) {
        agreeJoinRoomRequest(request, button);
    }

    // 拒绝加入房间 - 提交
    $scope.refuseJoinRoomRequest = function (request, button) {
        $scope.refuseAddFriendRequest(request, button);// 函数复用
    }






    // 根据好友 id 进入聊天室
    $scope.startChatWithFriend = function (friendId) {
        if ($scope.userData.allChatItems) {
            let chatItems = _.filter($scope.userData.allChatItems, function (e) {
                return e.type === "friend" && e.friend.id === friendId;
            });
            if (chatItems.length > 0) {
                $scope.openChatRoom(chatItems[0]);
            } else {
                $scope.alert("start chat with friend (" + friendId + "):", "找不到对应的 Chat Item", "alert-danger");
            }
        } else {
            $scope.alert("start chat with friend (" + friendId + "):", "用户没有任何 Chat Item", "alert-danger");
        }
    };



    // 根据房间 id 进入聊天室
    $scope.startChatInChatRoom = function (roomId) {
        if ($scope.userData.allChatItems) {
            let chatItems = _.filter($scope.userData.allChatItems, function (e) {
                return e.type === "room" && e.room.id === roomId;
            });
            if (chatItems.length > 0) {
                $scope.openChatRoom(chatItems[0]);
            } else {
                $scope.alert("start chat in chat room (" + roomId + "):", "找不到对应的 Chat Item", "alert-danger");
            }
        } else {
            $scope.alert("start chat in chat room (" + roomId + "):", "用户没有任何 Chat Item", "alert-danger");
        }
    };




    $scope.isToggle = function (elem) {
        elem = $(elem);
        let elemIsToggle = elem.is("[data-toggle]") || elem.is(".dropdown-item");
        let prntIsToggle = elem.parent().is("[data-toggle]") || elem.parent().is(".dropdown-item");
        let is_toggle = elemIsToggle || prntIsToggle;
        console.log("$scope.isToggle():", elem, is_toggle);
        return is_toggle;
    };


    // 在好友列表中 点击好友发起聊天
    $scope.clickFriend = function ($event, friendId) {
        $event.preventDefault();
        if (!$scope.isToggle($event.target)) {
            $scope.startChatWithFriend(friendId);
        }
    };


    // 在房间列表中 点击房间发起聊天
    $scope.clickRoom = function (target, roomId) {
        if (!$scope.isToggle(target)) {
            $scope.startChatInChatRoom(roomId);
        }
    };


    // 在查看聊天室成员时，点击某个用户头像发起私聊（如果不是好友就提示用户是否添加好友）
    $scope.startChatWithRoomMember = function (hisId) {
        if ($scope.userData.user.id !== hisId) {
            if ($scope.add_friend_is_ok($scope.userData.user.id, hisId)) {
                $scope.alert("start chat with room member", "你现在貌似还不是该用户的好友，要添加该用户为好友才能发起私聊", "alert-danger");
            } else {
                $scope.startChatWithFriend(hisId);
                // 关闭当前查看聊天室信息的模态窗
                $(".modal").modal("hide");
            }
        }
    };



    $scope.searchUserKeyword = "";
    $scope.searchUserResults = [];
    $scope.isSearchingUser = false;
    $scope.isSearchingUserLoading = false;
    // 搜索用户
    $scope.searchUser = function () {
        $scope.stopSearch();// 停止搜索卡片
        $scope.stopSearchRooms();// 停止搜索房间
        if ($scope.searchUserKeyword !== "") {
            window.scrollTo(0,0);
            $scope.isSearchingUserLoading = true;
            $http({
                method: 'GET',
                url: "http://" + $scope.webRoot + apis.search.users,
                params: {
                    "username": $scope.searchUserKeyword
                },
                crossDomain: true,
                withCredentials: true
            }).then(function (result) {
                $scope.isSearchingUserLoading = false;
                let data = result.data;
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "Username cannot be empty":
                        $scope.alert("[ERROR] searchUser: 关键字不能为空");
                        break;
                    case "Find fail":
                        $scope.alert("[ERROR] searchUser: 查询用户失败");
                        break;
                    case "Find success":
                        $scope.searchUserResults = data.users;
                        $scope.isSearchingUser = true;
                        break;
                    default:
                        $scope.alert("[ERROR] searchUser: 未知错误 " + data.message);
                        break;
                }
            }, function () {
                $scope.isSearchingUserLoading = false;
                $scope.alert("[ERROR] searchUser: 无网络连接");
            });
        } else {// 输入框为空
            $scope.isSearchingUser = false;
        }
    }

    $scope.stopSearchUser = function () {
        $scope.searchUserKeyword = "";
        $scope.isSearchingUser = false;
        $scope.searchUserResults = [];
    }



    $scope.searchRoomKeyword = "";
    $scope.searchRoomResults = [];
    $scope.isSearchingRoom = false;
    $scope.isSearchingRoomLoading = false;
    // 搜索房间
    $scope.searchRoom = function () {
        $scope.stopSearch();// 停止搜索卡片
        $scope.stopSearchUser();// 停止搜索用户
        if ($scope.searchRoomKeyword !== "") {
            window.scrollTo(0,0);
            $scope.isSearchingRoomLoading = true;
            $http({
                method: 'GET',
                url: "http://" + $scope.webRoot + apis.search.rooms,
                params: {
                    "roomName": $scope.searchRoomKeyword
                },
                crossDomain: true,
                withCredentials: true
            }).then(function (result) {
                $scope.isSearchingRoomLoading = false;
                let data = result.data;
                if (data.message) {
                    switch (data.message) {
                        case "Please login":
                            gotoLogin("index.html");
                            break;
                        case "Keyword cannot be empty":
                            $scope.alert("[ERROR] search room: 关键字不能为空");
                            break;
                        case "Find fail":
                            $scope.alert("[ERROR] search room: 查询房间失败");
                            break;
                        case "Find success":
                            $scope.searchRoomResults = data.rooms;
                            $scope.isSearchingRoom = true;
                            break;
                        default:
                            $scope.alert("[ERROR] search room: 未知错误 " + data.message);
                            break;
                    }
                } else {
                    $scope.alert("[ERROR] search room: 发生错误，服务器没有返回数据");
                }
            }, function (error) {
                $scope.isSearchingRoomLoading = false;
                $scope.alert("[ERROR] search room: 无法连接服务器，错误代码：" + error.status);
            });
        } else {// 输入框为空
            $scope.isSearchingRoom = false;
        }
    }

    $scope.stopSearchRooms = function () {
        $scope.searchRoomKeyword = "";
        $scope.isSearchingRoom = false;
        $scope.isSearchingRoomLoading = false;
        $scope.searchRoomResults = [];
    }







    $scope.show_room_info = function (roomId) {
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.get.room.members,
            params: {
                "roomId": roomId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("[ERROR] 房间 ID 不能为空");
                        break;
                    case "You are not member":
                        $scope.alert("[ERROR] 你不是房间成员");
                        break;
                    case "Find fail":
                        $scope.alert("[ERROR] 查询失败");
                        break;
                    case "Find success":
                        $scope.current_room_members = data.roomMembers;
                        $scope.current_room = _.filter($scope.userData.rooms, function (room) {
                            return room.id === roomId;
                        })[0];
                        $scope.current_room.show_members_info_col = [true, true, true, true, false];
                        $scope.current_room_my_role = data.myRole;
                        $("#room-info-modal").modal("show");
                        break;
                    default:
                        $scope.alert("[ERROR] show room info: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] show room info: 发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("[ERROR] show room info: 无法连接服务器，错误代码：" + error.status);
        });
    }



    $scope.show_friend_info = function (friendId) {
        let type = $scope.currentChatItemType;
        let item = $scope.get_current_chat_item();
        if (type && type === "friend" && item && item.friend.id === friendId) {
            $("#friend-info-modal").modal("show");
        } else {
            bsAlert("$scope.show_friend_info", "目前无法查看好友列表中的好友的个人页面<br>除非在聊天窗口中查看<br>此功能尚在开发当中.", "alert-danger");
        }
    }





    $scope.get_current_chat_item = function () {
        let current_chat_item = _.filter($scope.userData.allChatItems, function (chatItem) {
            return chatItem.type === $scope.currentChatItemType && chatItem.id === $scope.currentChatItemId;
        })[0];
        return current_chat_item;
    }



    $scope.getPrevChatItem = function () {
        let prevChatItem = _.filter($scope.userData.allChatItems, function (chatItem) {
            return chatItem.type === $scope.prevChatItemType && chatItem.id === $scope.prevChatItemId;
        })[0];
        return prevChatItem;
    };


    $scope.openPrevChatRoom = function () {
        $scope.openChatRoom($scope.getPrevChatItem());
    };





    $scope.show_current_chat_object_info = function () {
        let type = $scope.currentChatItemType;
        if (type) {
            let current_chat_item = $scope.get_current_chat_item();
            if (type === "room") {
                $scope.show_room_info(current_chat_item.room.id);
                $scope.current_room_members_list_offset = 0;
            } else if (type === "friend") {
                $scope.show_friend_info(current_chat_item.friend.id);
            } else {
                $scope.alert("[ERROR] show current chat object info:", "unknown chat item type '" + type + "'");
            }
        } else {
            $scope.alert("[ERROR] show current chat object info:", "current chat item type is null");
        }
    }



    $scope.set_room_admin_is_ok = function (my_role, his_role) {
        if (my_role === 'owner') {
            if (his_role === 'member') {
                return true;
            }
        }
        return false;
    }


    $scope.disable_room_admin_is_ok = function (my_role, his_role) {
        if (my_role === 'owner') {
            if (his_role === 'admin') {
                return true;
            }
        }
        return false;
    }


    $scope.set_room_admin = function (roomId, memberId) {
        console.log("set_room_admin(roomId: " + roomId + ", memberId: " + memberId + "): sending request...");
        removeAllTooltips();
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.setRoomAdmin,
            params: {
                "roomId": roomId,
                "memberId": memberId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "Room does not exist":
                        $scope.alert("房间不存在");
                        break;
                    case "You are not owner":
                        $scope.alert("没有权限");
                        break;
                    case "The user is not room member":
                        $scope.alert("该用户不是房间成员");
                        break;
                    case "The user has been admin":
                        $scope.alert("该用户已经是管理员");
                        break;
                    case "Set fail":
                        $scope.alert("设置管理员失败");
                        break;
                    case "Set success":
                        $scope.alert("set room admin:", "设置管理员成功", "alert-success");
                        $scope.current_room_members = _.map($scope.current_room_members, function (member) {
                            if (member.id === memberId) {
                                member.role = "admin";
                            }
                            return member;
                        });
                        break;
                    default:
                        $scope.alert("set room admin:", "未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("set room admin:", "发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("set room admin:", "无法连接服务器，错误代码：" + error.status);
        });
    }


    $scope.disable_room_admin = function (roomId, memberId) {
        console.log("disable_room_admin(roomId: " + roomId + ", adminId: " + memberId + "): sending request...");
        removeAllTooltips();
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.disableRoomAdmin,
            params: {
                "roomId": roomId,
                "adminId": memberId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "AdminId cannot be empty":
                        $scope.alert("管理员 ID 不能为空");
                        break;
                    case "You are not owner":
                        $scope.alert("没有权限");
                        break;
                    case "The user is not admin":
                        $scope.alert("该用户不是管理员");
                        break;
                    case "Set fail":
                        $scope.alert("取消管理员失败");
                        break;
                    case "Set success":
                        $scope.alert("disable room admin:", "取消管理员成功", "alert-success");
                        $scope.current_room_members = _.map($scope.current_room_members, function (member) {
                            if (member.id === memberId) {
                                member.role = "member";
                            }
                            return member;
                        });
                        break;
                    default:
                        $scope.alert("disable room admin:", "未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("disable room admin:", "发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("disable room admin:", "无法连接服务器，错误代码：" + error.status);
        });
    }







    // 如果我的好友列表中有这个人，就返回 false, 否则返回 true
    $scope.add_friend_is_ok = function (userId, hisId) {
        if (!userId) {
            $scope.alert("add_friend_is_ok:", "userId 没给");
            return false;
        } else if (!hisId) {
            $scope.alert("add_friend_is_ok:", "hisId 没给");
            return false;
        }

        if (userId === hisId) {
            return false;
        } else {
            let groupHasHim = _.some($scope.userData.groups, function (group) {
                return _.some(group.friends, function (friend) {
                    return friend.id === hisId;
                });
            });
            return !groupHasHim;
        }
        return false;
    }


    $scope.add_room_member_friend = function (memberId) {
        console.log("add_room_member_friend(memberId: " + memberId + "): sending request...");
        // 在用户查看聊天室成员时，点击加某个用户好友的时候调用此函数，但是这个函数不执行什么操作，因为点完加好友按钮会出现表单弹窗
    }






    $scope.remove_room_member_is_ok = function (my_role, his_role) {
        if (my_role === 'owner') {
            if (his_role !== 'owner') {
                return true;
            }
        } else if (my_role === 'admin') {
            if (his_role === 'member') {
                return true;
            }
        }
        return false;
    }

    $scope.remove_room_member = function (roomId, memberId) {
        console.log("remove_room_member(roomId: " + roomId + ", memberId: " + memberId + "): sending request...");
        if (confirm("是否要删除该成员？") === false) return;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.remove.room.member,
            params: {
                "roomId": roomId,
                "memberId": memberId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "MemberId cannot be empty":
                        $scope.alert("成员 ID 不能为空");
                        break;
                    case "Room does not exist":
                        $scope.alert("房间不存在");
                        break;
                    case "Unknown room member":
                        $scope.alert("该用户不是群成员");
                        break;
                    case "You are not admin":
                        $scope.alert("你不是管理员");
                        break;
                    case "You cannot remove owner":
                        $scope.alert("你不能移除群主");
                        break;
                    case "Insufficient permissions":
                        $scope.alert("权限不足");
                        break;
                    case "Remove fail":
                        $scope.alert("移除群成员失败");
                        break;
                    case "Remove success":
                        $scope.alert("remove room member:", "已移除群成员", "alert-success");
                        $scope.current_room_members = _.filter($scope.current_room_members, function (member) {
                            return member.id !== memberId;
                        });
                        break;
                    default:
                        $scope.alert("remove room member: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("remove room member:", "发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("remove room member:", "无法连接服务器，错误代码：" + error.status);
        });
    }




    $scope.allow_set_room_member_remark = function () {
        $scope.allow.set.room.member.remark = true;
        $timeout(function () {
            $('[ng-blur="set_room_member_remark()"]').focus();
        }, 100);
    }


    $scope.set_room_member_remark = function () {
        $scope.allow.set.room.member.remark = false;
        let remark = $('[ng-blur="set_room_member_remark()"]').val();
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.room.set.remark,
            params: {
                "roomId": $scope.current_room.id,
                "remark": remark
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "Remark cannot be empty":
                        $scope.alert("群名片不能为空");
                        break;
                    case "Room does not exist":
                        $scope.alert("房间不存在");
                        break;
                    case "You are not member":
                        $scope.alert("你不是群成员");
                        break;
                    case "Set fail":
                        $scope.alert("设置群名片失败");
                        break;
                    case "Set success":

                        break;
                    default:
                        $scope.alert("[ERROR] set room member remark: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] set room member remark: 发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("[ERROR] set room member remark: 无法连接服务器，错误代码：" + error.status);
        });
    }




    // 群主设置群名称
    $scope.allow_set_room_name = function () {
        $scope.allow.set.room.name = true;
        $timeout(function () {
            $('[ng-blur="set_room_name()"]').focus();
        }, 100);
    }


    $scope.set_room_name = function () {
        $scope.allow.set.room.name = false;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.room.set.name,
            params: {
                "roomId": $scope.current_room.id,
                "roomName": $scope.current_room.roomName
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "RoomName cannot be empty":
                        $scope.alert("房间名不能为空");
                        break;
                    case "RoomName is too long":
                        $scope.alert("房间名过长");
                        break;
                    case "Insufficient permissions":
                        $scope.alert("你没有权限修改群名称");
                        break;
                    case "Set fail":
                        $scope.alert("设置群名称失败");
                        break;
                    case "Set success":
                        // 更新当前聊天室的名字 ! ROOM !
                        let currentChatItem = $scope.get_current_chat_item();
                        if (currentChatItem.type === "room" && currentChatItem.room.id === $scope.current_room.id) {
                            $scope.currentChatItemName = $scope.current_room.roomName;
                        }
                        // 从联系人列表中更新群名称 ! ROOM !
                        $scope.userData.allChatItems = _.map($scope.userData.allChatItems, function (e) {
                            if (e.type === 'room' && e.room.id === $scope.current_room.id) {
                                e.room.roomName = $scope.current_room.roomName;
                            }
                            return e;
                        });
                        // 从聊天列表中更新群名称 ! ROOM !
                        $scope.userData.chatList = _.map($scope.userData.chatList, function (e) {
                            if (e.type === 'room' && e.room.id === $scope.current_room.id) {
                                e.room.roomName = $scope.current_room.roomName;
                            }
                            return e;
                        });
                        break;
                    default:
                        $scope.alert("[ERROR] set room name: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] set room name: 发生错误，服务器没有返回数据");
            } name
        }, function (error) {
            $scope.alert("[ERROR] set room name: 无法连接服务器，错误代码：" + error.status);
        });
    }




    $scope.remove_room = function (roomId) {
        console.log("remove_room(roomId: " + roomId + "): sending request...");
        if (confirm("是否要解散群？") === false) return;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.removeRoom,
            params: {
                "roomId": roomId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "Room does not exist":
                        $scope.alert("房间不存在");
                        break;
                    case "You are not owner":
                        $scope.alert("你不是管理员");
                        break;
                    case "Remove fail":
                        $scope.alert("解散群失败");
                        break;
                    case "Remove success":
                        $scope.alert("该群已被解散");
                        $(".modal").modal("hide");
                        $scope.goBack();
                        $scope.userData.rooms = _.filter($scope.userData.rooms, function (room) {
                            return room.id !== roomId;
                        });
                        $scope.userData.chatList = _.filter($scope.userData.chatList, function (chatItem) {
                            return chatItem.type !== "room" || chatItem.room.id !== roomId;
                        });
                        $scope.userData.allChatItems = _.filter($scope.userData.allChatItems, function (chatItem) {
                            return chatItem.type !== "room" || chatItem.room.id !== roomId;
                        });
                        break;
                    default:
                        $scope.alert("[ERROR] remove room: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] remove room: 发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("[ERROR] remove room: 无法连接服务器，错误代码：" + error.status);
        });
    }





    $scope.quit_room = function (roomId) {
        console.log("quit_room(roomId: " + roomId + "): sending request...");
        if (confirm("是否要退出聊天室？") === false) return;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.quitRoom,
            params: {
                "roomId": roomId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "RoomId cannot be empty":
                        $scope.alert("房间 ID 不能为空");
                        break;
                    case "Room does not exist":
                        $scope.alert("房间不存在");
                        break;
                    case "You are not member":
                        $scope.alert("你不是聊天室成员");
                        break;
                    case "Owner cannot quit room":
                        $scope.alert("群主不能退群");
                        break;
                    case "Quit fail":
                        $scope.alert("退出聊天室失败");
                        break;
                    case "Quit success":
                        $scope.alert("quit room:", "你已退出该聊天室", "alert-primary");
                        $(".modal").modal("hide");
                        $scope.goBack();
                        $scope.userData.rooms = _.filter($scope.userData.rooms, function (room) {
                            return room.id !== roomId;
                        });
                        $scope.userData.chatList = _.filter($scope.userData.chatList, function (chatItem) {
                            return chatItem.type !== "room" || chatItem.room.id !== roomId;
                        });
                        $scope.userData.allChatItems = _.filter($scope.userData.allChatItems, function (chatItem) {
                            return chatItem.type !== "room" || chatItem.room.id !== roomId;
                        });
                        break;
                    default:
                        $scope.alert("quit room:", "未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("quit room:", "发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("quit room:", "无法连接服务器，错误代码：" + error.status);
        });
    }












    // 给好友分组重命名
    $scope.set_friend_group_name = function (group, oldGroupName) {
        console.log("set_friend_group_name(): group = " + JSON.stringify(group));
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.set.group.name,
            params: {
                "groupId": group.id,
                "groupName": group.groupName
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "GroupId cannot be empty":
                        $scope.alert("[ERROR] set friend group name: 分组 ID 不能为空");
                        group.groupName = oldGroupName;
                        break;
                    case "Group does not exist":
                        $scope.alert("[ERROR] set friend group name: 分组不存在");
                        break;
                    case "GroupName cannot be empty":
                        $scope.alert("[ERROR] set friend group name: 分组名不能为空");
                        group.groupName = oldGroupName;
                        break;
                    case "GroupName contains special char":
                        $scope.alert("[ERROR] set friend group name: 分组名含有非法字符");
                        group.groupName = oldGroupName;
                        break;
                    case "GroupName is too long":
                        $scope.alert("[ERROR] set friend group name: 分组名过长");
                        group.groupName = oldGroupName;
                        break;
                    case "Set fail":
                        $scope.alert("[ERROR] set friend group name: 设置分组名失败");
                        group.groupName = oldGroupName;
                        break;
                    case "Set success":
                        $scope.alert("set friend group name:", "设置分组名成功", "alert-success");
                        break;
                    default:
                        $scope.alert("[ERROR] set friend group name: 未知错误 " + data.message);
                        group.groupName = oldGroupName;
                        break;
                }
            } else {
                $scope.alert("[ERROR] set friend group name: 发生错误，服务器没有返回数据");
                group.groupName = oldGroupName;
            }
        }, function (error) {
            $scope.alert("[ERROR] set friend group name: 无法连接服务器，错误代码：" + error.status);
            group.groupName = oldGroupName;
        });
    }




    // 返回好友所在的分组
    $scope.get_group_of_friend = function (friend) {
        if (!friend) {
            if ($scope.debugMode.isEnabled) {
                $scope.alert("get group of friend:","parameter 'friend' is not provided<br>" + getRandomString(), "alert-danger");
            } else {
                $window.console.log("get group of friend:", "parameter 'friend' is not provided " + getRandomString());
            }
            return false;
        }
        if ($scope.userData && $scope.userData.groups) {
            return _.filter($scope.userData.groups, function (group) {
                return _.find(group.friends, function (him) {
                    return him.id === friend.id;
                });
            })[0];
        } else {
            return false;
        }
    }

    // 开启选择（当前）好友分组功能
    $scope.allow_move_friend_to_group = function () {
        $scope.allow.set.friend.group = true;
        setTimeout(() => {
            $(".modal#friend-info-modal select.group-select").focus();
    }, 10);
    }

    // 移动好友到新的分组 - 提交
    $scope.move_friend_to_group = function (friend, group) {
        $scope.allow.edit.user.city = false;
        $scope.userData.user.city = $scope.user_address.value();
        // console.log("move_friend_to_group(): friend = " + JSON.stringify(friend) + ", group = " + JSON.stringify(group));
        console.log("move_friend_to_group(friend: " + friend.username + ", group: " + group.groupName + "): sending request ...");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.friend.set.group,
            params: {
                "friendId": friend.id,
                "groupId": group.id
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "GroupId cannot be empty":
                        console.log("分组 ID 不能为空");
                        break;
                    case "Group does not exist":
                        console.log("分组不存在");
                        break;
                    case "FriendId cannot be empty":
                        console.log("好友 ID 不能为空");
                        break;
                    case "Friend does not exist":
                        console.log("好友不存在");
                        break;
                    case "Friend is already in this group":
                        console.log("好友已经在这个分组里了");
                        break;
                    case "Move fail":
                        console.log("无法移动好友到这个分组");
                        break;
                    case "Move success":
                        console.log("移动好友到分组成功");
                        // 将好友从之前的分组中删除
                        $scope.userData.groups = _.map($scope.userData.groups, function (box) {
                            box.friends = _.filter(box.friends, function (him) {
                                return him.id !== friend.id;
                            });
                            return box;
                        });
                        // 将好友放到新的分组中
                        $scope.userData.groups = _.map($scope.userData.groups, function (box) {
                            if (box.id === group.id) {
                                box.friends.push(friend);
                            }
                            return box;
                        });
                        let modalSelectGroup = $(".modal.move-friend-to-group");
                        if (modalSelectGroup.length) {
                            bsAlert("move friend to new group", "设置分组成功", "alert-success");
                            modalSelectGroup.modal("hide");
                        }
                        break;
                    default:
                        $scope.alert("[ERROR] move_friend_to_group: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] move_friend_to_group: 发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("[ERROR] move_friend_to_group: 无法连接服务器，错误代码：" + error.status);
        });
    }


    // 将当前好友移动到选中的分组
    $scope.move_current_friend_to_group = function () {
        $scope.allow.set.friend.group = false;
        let modal = $(".modal#friend-info-modal");
        if (modal.length) {
            let groupId = modal.find('select.group-select').val();
            if (groupId) {
                let group = _.find($scope.userData.groups, function (group) {
                    return group.id === groupId;
                });
                // console.log("move_current_friend_to_group: 正在移动当前好友 " + $scope.current_friend.id + " 到分组 " + group.id);
                console.log("move_current_friend_to_group: 正在移动当前好友 " + $scope.current_friend.username + " 到分组 " + group.groupName);
                $scope.move_friend_to_group($scope.current_friend, group);
            } else {
                console.log("[ERROR] move_current_friend_to_group: 没获取到分组选择框的值");
            }
        } else {
            console.log("[ERROR] move_current_friend_to_group: 没找到模态窗");
        }
    }


    // 移动好友到新的分组 - 组件 - 选择要移动到的分组（这是在好友列表中设置某好友的分组，不一定是当前正在聊天的好友）
    $scope.move_friend_to_group_select_group = function (friend) {
        let modal = $(".modal.move-friend-to-group");
        if (modal.length) {
            $scope.this_friend = friend;
            modal.modal("show");
            modal.find("button.move-friend-to-group").on('click', function () {
                let groupId = modal.find('select.group-select').val();
                if (groupId) {
                    let group = _.find($scope.userData.groups, function (group) {
                        return group.id === groupId;
                    });
                    if (group) {
                        console.log("move_friend_to_group_select_group(): friend = " + JSON.stringify(friend) + ", group = " + JSON.stringify(group));
                        $scope.move_friend_to_group(friend, group);
                    } else {
                        console.log("[ERROR] move_friend_to_group_select_group(): 分组不存在");
                        $scope.alert("[ERROR] move_friend_to_group_select_group():", "分组不存在", "alert-danger");
                    }
                } else {
                    console.log("[ERROR] move_friend_to_group_select_group(): 无法获得选中的分组 ID");
                    $scope.alert("[ERROR] move_friend_to_group_select_group():", "无法获得选中的分组 ID", "alert-danger");
                }
            });
        } else {
            console.log("[ERROR] move_friend_to_group_select_group(): 没找到分组选择模态窗");
            $scope.alert("[ERROR] move_friend_to_group_select_group():", "没找到分组选择模态窗", "alert-danger");
            // 采用第二种方案，将当前正在聊天的好友变成这个好友，再设置他的分组
            $scope.current_friend = friend;
            $(".modal#friend-info-modal").modal("show");
        }
    }







    /* 用户个人信息设置 */







    $scope.allow_set_user_gender = function () {
        $scope.allow.edit.user.gender = true;
        $timeout(function () {
            $('[ng-blur="set_user_gender()"]').focus();
        }, 100);
    }


    $scope.set_user_gender = function () {
        $scope.allow.edit.user.gender = false;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.user.set.gender,
            params: {
                "gender": $scope.userData.user.gender
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "Gender cannot be empty":
                        console.log("性别不能为空");
                        break;
                    case "Unknown gender":
                        console.log("性别不可用");
                        break;
                    case "Set fail":
                        console.log("设置性别失败");
                        break;
                    case "Set success":
                        console.log("设置性别成功");
                        break;
                    default:
                        $scope.alert("[ERROR] set user gender: 未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("[ERROR] set user gender: 发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("[ERROR] set user gender: 无法连接服务器，错误代码：" + error.status);
        });
    }






    // 用户设置自己的昵称
    $scope.allow_set_user_nickname = function () {
        $scope.allow.edit.user.nickname = true;
        $timeout(function () {
            $('[ng-blur="set_user_nickname()"]').focus();
        }, 100);
    }

    $scope.set_user_nickname = function () {
        $scope.allow.edit.user.nickname = false;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.user.set.nickname,
            params: {
                "nickname": $scope.userData.user.nickname
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "Nickname cannot be empty":
                        $scope.alert("set user nickname:", "昵称不能为空", "alert-danger");
                        break;
                    case "Nickname contains special char":
                        $scope.alert("set user nickname:", "昵称包含非法字符", "alert-danger");
                        break;
                    case "Nickname is too long":
                        $scope.alert("set user nickname:", "昵称太长", "alert-danger");
                        break;
                    case "Set fail":
                        $scope.alert("set user nickname:", "设置昵称失败", "alert-danger");
                        break;
                    case "Set success":
                        $scope.alert("set user nickname:", "设置昵称成功", "alert-success");
                        break;
                    default:
                        $scope.alert("set user nickname:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("set user nickname:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            $scope.alert("set user nickname:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    }




    // 用户设置个性签名
    $scope.allow_set_user_signature = function () {
        $scope.allow.edit.user.signature = true;
        $timeout(function () {
            $('[ng-blur="set_user_signature()"]').focus();
        }, 100);
    }

    // 此函数有漏洞，在修改个性签名失败的情况下，输入框中的值无法恢复成原本的值
    $scope.set_user_signature = function () {
        $scope.allow.edit.user.signature = false;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.user.set.signature,
            params: {
                "signature": $scope.userData.user.signature
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "Signature cannot be empty":
                        $scope.alert("set user signature:", "个性签名不能为空", "alert-danger");
                        break;
                    case "Signature is too long":
                        $scope.alert("set user signature:", "个性签名太长", "alert-danger");
                        break;
                    case "Set fail":
                        $scope.alert("set user signature:", "设置个性签名失败", "alert-danger");
                        break;
                    case "Set success":
                        $scope.alert("set user signature:", "设置个性签名成功", "alert-success");
                        break;
                    default:
                        $scope.alert("set user signature:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("set user signature:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            $scope.alert("set user signature:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    };





    $scope.user_address = {
        legal: false,
        update: function () {
            this.legal = (this.value() !== "");
        },
        value: function () {
            let address = "", province = $("#s_province").val(), city = $("#s_city").val(), county = $("#s_county").val();
            if (!["-省-"].includes(province)) address += province;
            if (!["-市-", province].includes(city)) address += city;
            if (!["-区/县-"].includes(county)) address += county;
            return address;
        },
        cancel: function () {
            $scope.allow.edit.user.city = false;
            this.legal = false;
        }
    };

    $scope.allow_set_user_city = function () {
        $scope.allow.edit.user.city = true;
        $timeout(function () {
            $('#s_province').focus();
        }, 100);
    }

    $scope.set_user_city = function () {
        $scope.allow.edit.user.city = false;
        $scope.userData.user.city = $scope.user_address.value();
        console.log("set_user_city(): city = '" + $scope.userData.user.city + "'");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.user.set.city,
            params: {
                "city": $scope.userData.user.city
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "City cannot be empty":
                        console.log("城市不能为空");
                        break;
                    case "City contains special char":
                        console.log("城市含有非法字符");
                        break;
                    case "City is too long":
                        console.log("城市名称过长");
                        break;
                    case "Set fail":
                        console.log("设置城市失败");
                        break;
                    case "Set success":
                        console.log("设置城市成功");
                        break;
                    default:
                        $scope.alert("set user city:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("set user city:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            $scope.alert("set user city:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    }











    $scope.allow_set_friend_remark = function () {
        $scope.allow.set.friend.remark = true;
        $timeout(function () {
            $('[ng-blur="set_friend_remark()"]').focus();
        }, 100);
    }

    $scope.set_friend_remark = function (friend) {
        $scope.allow.set.friend.remark = false;
        let this_friend = $scope.current_friend;
        if (friend) {
            this_friend = friend;
        }
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.friend.set.remark,
            params: {
                "friendId": this_friend.id,
                "remark": this_friend.remark
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "FriendId cannot be empty":
                        $scope.alert("set friend remark:", "好友 ID 不能为空");
                        break;
                    case "Remark cannot be empty":
                        $scope.alert("set friend remark:", "好友备注名不能为空");
                        break;
                    case "Friend does not exist":
                        $scope.alert("set friend remark:", "好友不存在");
                        break;
                    case "Set fail":
                        $scope.alert("set friend remark:", "设置好友备注名失败");
                        break;
                    case "Set success":
                        // 更新当前好友聊天室的名字
                        let currentChatItem = $scope.get_current_chat_item();
                        if (currentChatItem.type === "friend" && currentChatItem.friend.id === this_friend.id) {
                            $scope.currentChatItemName = this_friend.remark;
                        }
                        // 从联系人列表中更新好友备注名
                        $scope.userData.allChatItems = _.map($scope.userData.allChatItems, function (e) {
                            if (e.type === 'friend' && e.friend.id === this_friend.id) {
                                e.friend.remark = this_friend.remark;
                            }
                            return e;
                        });
                        // 从聊天列表中更新好友备注名
                        $scope.userData.chatList = _.map($scope.userData.chatList, function (e) {
                            if (e.type === 'friend' && e.friend.id === this_friend.id) {
                                e.friend.remark = this_friend.remark;
                            }
                            return e;
                        });
                        // 从好友分组中更新好友备注名
                        $scope.userData.groups = _.map($scope.userData.groups, function (group) {
                            group.friends = _.map(group.friends, function (friend) {
                                if (friend.id === this_friend.id) {
                                    friend.remark = this_friend.remark;
                                }
                                return friend;
                            });
                            return group;
                        });
                        break;
                    default:
                        $scope.alert("set friend remark:", "未知错误 " + data.message);
                        break;
                }
            } else {
                $scope.alert("set friend remark:", "发生错误，服务器没有返回数据");
            }
        }, function (error) {
            $scope.alert("set friend remark:", "无法连接服务器，错误代码：" + error.status);
        });
    };



    $scope.open_set_friend_remark_modal = function(friend) {
        let modal = $(".modal.set-friend-remark-modal");
        if (modal.length) {
            $scope.this_friend = friend;
            modal.modal("show");
            modal.find("button.set-friend-remark-submit").on('click', function () {
                $scope.set_friend_remark(friend);
            });
        }
    };















    $scope.remove_friend = function (friendId) {
        console.log("remove_friend(friendId: " + friendId + "): sending request...");
        if (confirm("是否要删除该好友？") === false) return;
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.removeFriend,
            params: {
                "friendId": friendId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "FriendId cannot be empty":
                        $scope.alert("remove friend:", "好友 ID 不能为空");
                        break;
                    case "Friend does not exist":
                        $scope.alert("remove friend:", "不存在此好友");
                        break;
                    case "Remove fail":
                        $scope.alert("remove friend:", "删除好友失败");
                        break;
                    case "Remove success":
                        $scope.alert("remove friend:", "已删除该好友", "alert-success");
                        $(".modal").modal("hide");
                        if ($scope.pageLocation === "chat" && $scope.subPageLocation === "chat") {
                            $scope.goBack();
                        }
                        // 从好友分组中删除该好友
                        $scope.userData.groups = _.map($scope.userData.groups, function (group) {
                            group.friends = _.filter(group.friends, function (friend) {
                                return friend.id !== friendId;
                            });
                            return group;
                        });
                        // 从聊天列表中删除该好友
                        $scope.userData.chatList = _.filter($scope.userData.chatList, function (chatItem) {
                            return chatItem.type !== "friend" || chatItem.friend.id !== friendId;
                        });
                        // 从联系人列表中删除该好友
                        $scope.userData.allChatItems = _.filter($scope.userData.allChatItems, function (chatItem) {
                            return chatItem.type !== "friend" || chatItem.friend.id !== friendId;
                        });
                        break;
                    default:
                        $scope.alert("remove friend:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("remove friend:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            $scope.alert("remove friend:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    }





    // 删除消息（适用于: 好友, 房间）
    $scope.remove_message = function (messageId) {
        console.log("remove_message(): messageId = '" + messageId + "'");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.remove[$scope.currentChatItemType].message,
            params: {
                "messageId": messageId
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "MessageId cannot be empty":
                        console.log("消息 ID 不能为空");
                        break;
                    case "Message does not exist":
                        console.log("消息不存在");
                        break;
                    case "Message has been removed":
                        console.log("消息已经被撤回");
                        break;
                    case "Remove fail":
                        console.log("删除消息失败");
                        break;
                    case "Remove success":
                        console.log("删除消息成功");
                        // 删除消息是自己单方面的操作，自己只会收到回调消息，自己和对方都不会收到服务器指令
                        $scope.chatMessageList = _.filter($scope.chatMessageList, function (message) {
                            return message.id !== messageId;
                        });
                        removeAllTooltips();
                        break;
                    default:
                        $scope.alert("remove message:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("remove message:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            $scope.alert("remove message:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    }


    // 撤回聊天消息（适用于: 好友）
    $scope.recall_friend_room_message = function (messageId) {
        let loadingId = startLoading(max.loading.delay.time, "recall_friend_room_message(\"" + messageId + "\")");
        console.log("recall_friend_room_message(messageId:'" + messageId + "'): sending request ...");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.recall[$scope.currentChatItemType].message,
            params: {
                "messageId": messageId,
                "friendId": $scope.get_current_chat_item().friend.id
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            stopLoading(loadingId);

            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "MessageId cannot be empty":
                        console.log("消息 ID 不能为空");
                        break;
                    case "FriendId cannot be empty":
                        console.log("好友 ID 不能为空");
                        break;
                    case "Message does not exist":
                        console.log("消息不存在");
                        break;
                    case "Message has been recalled":
                        console.log("消息已经被撤回");
                        break;
                    case "Recall fail":
                        console.log("撤回消息失败");
                        break;
                    case "Recall success":
                        console.log("撤回消息成功");
                        removeAllTooltips();
                        // 撤回成功后不需要更新消息，会收到更新消息指令调用 updateMessage 函数
                        updateMessage(messageId);// (2018年5月20日) 有时候撤回消息收不到指令，需要手动更新消息
                        break;
                    default:
                        $scope.alert("recall message:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("recall message:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            stopLoading(loadingId);
            $scope.alert("recall message:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    }



    // 撤回聊天消息（适用于: 聊天室）
    $scope.recall_chat_room_message = function (messageId) {
        let loadingId = startLoading(max.loading.delay.time, "recall_chat_room_message(\"" + messageId + "\")");
        console.log("recall_chat_room_message(messageId:'" + messageId + "'): sending request ...");
        $http({
            method: 'GET',
            url: "http://" + $scope.webRoot + apis.recall[$scope.currentChatItemType].message,
            params: {
                "messageId": messageId,
                "roomId": $scope.get_current_chat_item().room.id
            },
            crossDomain: true,
            withCredentials: true
        }).then(function (result) {
            stopLoading(loadingId);

            let data = result.data;
            if (data.message) {
                switch (data.message) {
                    case "Please login":
                        gotoLogin("index.html");
                        break;
                    case "MessageId cannot be empty":
                        console.log("消息 ID 不能为空");
                        break;
                    case "RoomId cannot be empty":
                        console.log("房间 ID 不能为空");
                        break;
                    case "Message does not exist":
                        console.log("消息不存在");
                        break;
                    case "Room does not exist":
                        console.log("房间不存在");
                        break;
                    case "Message has been recalled":
                        console.log("消息已经被撤回");
                        break;
                    case "Recall fail":
                        console.log("撤回消息失败");
                        break;
                    case "Recall success":
                        console.log("撤回消息成功");
                        removeAllTooltips();
                        // 撤回成功后不需要更新消息，会收到更新消息指令调用 updateMessage 函数
                        updateMessage(messageId);// (2018年5月20日) 有时候撤回消息收不到指令，需要手动更新消息
                        break;
                    default:
                        $scope.alert("recall message:", "未知错误 " + data.message, "alert-danger");
                        break;
                }
            } else {
                $scope.alert("recall message:", "发生错误，服务器没有返回数据", "alert-danger");
            }
        }, function (error) {
            stopLoading(loadingId);
            $scope.alert("recall message:", "无法连接服务器，错误代码：" + error.status, "alert-danger");
        });
    };

});