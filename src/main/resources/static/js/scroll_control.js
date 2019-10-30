function scrollMessageIntoView(DOMElement, isSmooth) {
    let elementRect = DOMElement.getBoundingClientRect();
    let absoluteElementTop = elementRect.top + window.pageYOffset;
    let middle = absoluteElementTop - (window.innerHeight / 2);
    window.scrollTo({
        left: 0,
        top: middle,
        behavior: isSmooth ? "smooth" : "auto"
    });
}

// 滚动到页面最顶端
function gotoFirstMessage(isSmooth) {
    scrollMessageIntoView($('.chat-window .message-body:first').get(0), isSmooth);
}

// 滚动到页面最底端
function gotoLastMessage(isSmooth) {
    scrollMessageIntoView($('.chat-window .message-body:last').get(0), isSmooth);
    setUnreadMessageCount(0);
}

// 设置 scroll control 中的 badge 显示的未读消息数
function setUnreadMessageCount(count) {
    let model = document.getElementById('chat-message-list');
    if (model) {
        let $scope = angular.element(model).scope();
        if ($scope) {
            $scope.$apply(function () {
                $scope.unread_message_count = count;
            });
        }
    }
}

// 判断用户是否能看到页面最底端的消息
function isViewingLastMessage() {
    // 当前文档高度
    let documentHeight = $(document).height();
    // 滚动条所在位置的高度
    let totalheight = parseFloat($(window).height()) + parseFloat($(window).scrollTop());

    // bsAlert("documentHeight = " + documentHeight + "\ntotalHeight = " + totalheight);
    // 当前文档高度   小于或等于   滚动条所在位置高度  则是页面底部
    if (documentHeight <= totalheight + 100) {// 页面到达底部
        return true;
    }
    return false;
}

// 开关 scroll control
function toggle_scroll_control() {
    let toggle = $('.settings .toggle-scroll-control');
    // 如果开关是打开的
    if ($(toggle).hasClass('fa-toggle-on')) {// 关闭开关
        $(toggle).removeClass('fa-toggle-on').addClass('fa-toggle-off');
        return false;
    } else {// 开启开关
        $(toggle).removeClass('fa-toggle-off').addClass('fa-toggle-on');
        return true;
    }
}

// 监听是否到达整个文档的底部
$(window).scroll(function () {
    if (isViewingLastMessage()) {
        setUnreadMessageCount(0);
    }
});