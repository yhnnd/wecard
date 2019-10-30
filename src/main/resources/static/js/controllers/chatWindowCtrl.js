(function () {
    angular
        .module("module-index")
        .controller("chatWindowController", chatWindowController)
        .directive("wcChatWindow", wcChatWindow)

    /* @ngInject */
    function chatWindowController($scope, $window) {
        // values
        $scope.special_ids                = $scope.$parent.special_ids;
        // functions
        $scope.getInnerWidth              = $scope.$parent.getInnerWidth;
        $scope.load_more_messages         = $scope.$parent.load_more_messages;
        $scope.recall_friend_room_message = $scope.$parent.recall_friend_room_message;
        $scope.remove_message             = $scope.$parent.remove_message;
        $scope.getRoomMemberName          = $scope.$parent.getRoomMemberName;
        $scope.startChatWithRoomMember    = $scope.$parent.startChatWithRoomMember;
        $scope.recall_chat_room_message   = $scope.$parent.recall_chat_room_message;
        $scope.getAgreeAddFriendTitle     = $scope.$parent.getAgreeAddFriendTitle;
        $scope.getAgreeAddFriendContent   = $scope.$parent.getAgreeAddFriendContent;
        $scope.refuseAddFriendRequest     = $scope.$parent.refuseAddFriendRequest;
        $scope.agreeJoinRoomRequest       = $scope.$parent.agreeJoinRoomRequest;
        $scope.refuseJoinRoomRequest      = $scope.$parent.refuseJoinRoomRequest;
        $scope.sendMessage                = $scope.$parent.sendMessage;
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
            var theme = $window.localStorage.getItem("chat_window_background_color");
            if (theme && typeof theme === "string") {
                $scope.setChatWindowBackgroundColor(theme);
            }
        };
    }

    /* @ngInject */
    function wcChatWindow() {
        return {
            restrict: 'E',
            scope: {
                "currentChatItemType": "=",
                "chatMessageList": "=",
                "current": "=",
                "max": "=",
                "userData": "=",
                "unread_message_count": "=unreadMessageCount"
            },
            controller: "chatWindowController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/chat-window.html';
            }
        }
    }
})();