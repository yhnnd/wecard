var websocket = null;

function connect_websocket(){
  if ('WebSocket' in window) {
    websocket = new WebSocket("ws://" + getWebRoot() + "/webSocketIMServer");
  } else if ('MozWebSocket' in window) {
    websocket = new MozWebSocket("ws://" + getWebRoot() + "/webSocketIMServer");
  } else {
    websocket = new SockJS("http://" + getWebRoot() + "/sockjs/webSocketIMServer");
  }

  websocket.onopen = onOpen;
  websocket.onmessage = onMessage;
  websocket.onerror = onError;
  websocket.onclose = onClose;

  function onOpen(openEvt) {
    //alert(openEvt.Data);
  }

  function onMessage(event) {
    var command = JSON.parse(event.data);
    console.log("(web socket) on message: command is " + JSON.stringify(command));
    setTimeout(() => {
      handleCommand(command);
    }, 100);
  }

  function onError() {
    $(".navbar-dark,.navbar-light").removeClass("bg-dark alert-secondary").addClass("bg-danger");
  }

  function onClose() { }

  function doSend() {
    if (websocket.readyState == websocket.OPEN) {
    } else {
      bsAlert("(web socket) do send: web socket 连接失败");
    }
  }
}

window.onbeforeunload = function () {
    websocket && websocket.close();
}

function handleCommand(command) {
    switch (command.type) {
        // 删除指令 remove_

        // 删除聊天项（删除好友时，好友必须收到这条指令以删除聊天项）（在聊天室内外都会收到该指令）// 已废弃
        case "remove_chatItem":
            removeChatItem(command.chatItemId);
            break;
        // 删除房间（在聊天室内外都会收到该指令）
        case "remove_room":
            removeRoom(command.roomId);
            break;
        // 删除好友（在聊天室内外都会收到该指令）
        case "remove_friend":
            removeFriend(command.friendId);
            break;

        // 更新指令 update_

        // 更新聊天列表（在聊天室内外都会收到该指令）
        case "update_chatItem":
            updateChatItem(command.chatItemId);
            break;
        // 请求状态已改变，更新该请求（在查看验证消息时会收到该指令）
        case "update_request":
            updateRequest(command.requestId);
            break;
        // 分组中有新的好友，更新该分组
        case "update_group":
            updateFriendGroup(command.groupId);
            break;
        // 撤回消息时调用
        case "update_message":
            updateMessage(command.messageId);
            break;

        // 加载指令 load_

        // 新的好友（或房间）已添加，加载该聊天项
        case "load_new_chatItem":
            loadNewChatItem(command.chatItemId);
            break;
        // 有新请求，加载该请求（只有在通知消息窗口内会收到该指令）
        case "load_new_request":
            loadNewRequest(command.requestId);
            break;
        // 有新消息，加载该消息（只有在聊天室内会收到该指令）
        case "load_new_message":
            loadNewMessage(command.messageId);
            break;
        case "load_new_notification":
            loadNewNotification(command.systemNotificationId);
            break;
        // 新的分组已添加，加载新分组
        case "load_new_group":
            loadNewFriendGroup(command.groupId);
            break;
        // 新的房间已添加，加载新房间
        case "load_new_room":
            loadNewChatRoom(command.roomId);
            break;
        default:
            bsAlert("[ERROR] handle command: 未知指令: " + command);
    }
}