// "var" cannot be replaced by "let" because "let" does not belong to window
var appVersion = {
    "short-version": "2.8",
    "build-version": "0",
    "version": "2.8.0"
};

let servers = ["www.impte.com", "127.0.0.1:8000"];
let choice = window.location.origin.includes("impte.com") ? 0 : 1;

function getWebRoot() {
    return servers[choice];
}

function getFileRoot() {
    let a = window.location.href.split("/");
    return a.pop(), a.join("/");
}

function getHttpRoot() {
    return getFileRoot();
    // return "http://" + getWebRoot();
}

var special_ids = {
    "new_user_system_message": "new_user_system_message"
};

var apis = {
    "sendMessage": "/chat/sendMsgToChatItem",

    "getFriendGroup": "/find/group",
    "createFriendGroup": "/create/group",
    "removeFriendGroup": "/remove/group",

    "set": {
        "group": {
            "name": "/setGroupName"// 设置好友分组的名称
        },
        "chat": {
            "location": "/chat/setChatLocation"
        }
    },

    "removeFriend": "/remove/friend",// friendId

    "friend": {
        "set": {
            "remark": "/friend/set/remark",// friendId, remark (设置好友的备注名)
            "group": "/moveFriendToGroup"// 设置好友所在的分组
        }
    },

    "getRequest": "/find/request",

    "getPortal": "/find/chatItem",
    "removePortal": "/remove/chatItem",// 已废弃

    "getMessages": "/chat/loadMessages",// 参数: chatItemId // 用于加载历史消息// 已弃用，改用 get.messages.limited
    "get": {
        "user": {
            "data": "/refresh",// 无参数
            "cards": "/card/user",
            "following": "/user/find/following",
            "fans": "/user/find/fans"
        },
        "message": "/find/message",// 参数: messageId // 用于加载新消息
        "messages": {
            "limited": "/chat/loadMessages/limit",// 参数: chatItemId, offset, limit
        },
        "card": {
            "byId": "/card/read"
        },
        "cards": {
            "allUsers": "/card/load",
            "following": "/card/following",
            "mine": "/card/mine",
            "liked": "/card/liked",
            "shared": "/card/shared"
        },
        "room": {
            "members": "/room/members"// roomId
        }
    },
    "remove": {
        "friend": {
            "message": "/remove/friend/message"// messageId
        },
        "room": {
            "message": "/remove/room/message",// messageId
            "member": "/remove/roomMember"// roomId, memberId
        },
        "card": {
            "byId": "/card/del"
        }
    },
    "recall": {
        "friend": {
            "message": "/remove/friend/recall"// messageId
        },
        "room": {
            "message": "/remove/room/recall"// messageId
        },
        "card": {
            "comment": "/comment/del"// commentId
        }
    },

    "getRoom": "/find/room",
    "createRoom": "/create/room",
    "removeRoom": "/remove/room",// roomId
    "quitRoom": "/quit/room",// roomId
    "setRoomAdmin": "/room/set/admin",// roomId, memberId
    "disableRoomAdmin": "/remove/admin",// roomId, adminId
    "room": {
        "set": {
            "name": "/room/set/roomName",// roomId, roomName (设置自己的房间的房间名)
            "remark": "/room/set/remark"// roomId, remark (设置自己的群名片)
        }
    },

    "search": {
        "users": "/find/users",
        "rooms": "/find/rooms",
        "cards": "/card/search"// key(topic/content), value
    },

    "addFriend": {
        "send": "/friend/makeFriendRequest",
        "agree": "/friend/agreeMakeFriendRequest",
        "refuse": "/refuse/request"// requestId
    },

    "joinRoom": {
        "send": "/room/joinRoomRequest",
        "agree": "/room/agreeJoinRoomRequest",
        "refuse": "refuse/request"// requestId
    },

    "forgotPassword": {
        "confirmAccount": "/login/forgetPwd/confirmAccount",
        "sendVercode": "/login/forgetPwd/sendVerCode",
        "verifyVercode": "/login/forgetPwd/verifyVerCode",
        "setPassword": "/login/forgetPwd/setPassword"
    },

    "user": {
        "set": {
            "avatar": "/personSet/setAvatar",// avatar
            "styleImage": "/personSet/setStyleImg",// styleImg
            "nickname": "/personSet/setNickname",// nickname
            "signature": "/personSet/setSignature",// signature
            "city": "/personSet/setCity",// city
            "gender": "/personSet/setGender"// gender
        },
        "is": {
            "LoggedIn": "/account/isLogin"
        }
    },

    "create": {
        "card": {
            "text": "/card/create/text",
            "image": "/card/create/image",
            "audio": "/card/create/voice",
            "video": "/card/create/video",
            "quill": "/card/create/quill",
            "comment": "/comment/create",// text, cardId
            "commentReply": "/comment/create"// text, cardId, parentId(评论的 ID)
        }
    },
    "like": {
        "card": "/card/like",// cardId
        "comment": "/comment/like"// commentId
    },
    "cancel": {
        "like": {
            "card": "/card/unlike",// cardId
            "comment": "/comment/unlike"// commentId
        }
    },
    "share": {
        "card": "/card/create/share"// 参数: text, parentId(被转发卡片的 ID)
    },
    "follow": {
        "user": "/user/follow"// followingId
    },
    "unfollow": {
        "user": "/user/unfollow"// followingId
    }
};

var max = {
    loading: {
        delay: {
            time: 10000
        }
    },
    get: {
        messages: {
            limit: 15
        },
        cards: {
            limit: 10
        },
        card: {
            comments: {
                limit: 10
            }
        }
    },
    card: {
        title: {
            length: 20
        },
        text: {
            length: 1000
        },
        topics: {
            length: 3
        },
        images: {
            length: 6
        },
        videos: {
            length: 1
        },
        audios: {
            length: 1
        }
    }
};

