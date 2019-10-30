(function () {
    angular
        .module("module-index")
        .controller("cardViewController", cardViewController)
        .directive("wcCardViewModal", wcCardViewModal)

    /* @ngInject */
    function cardViewController($scope, $window, $sce, $timeout) {
        $scope.noPrevCard = false;
        $scope.noNextCard = false;
        // 卡片评论显示尺寸
        $scope.comment_size = "md";
        // 所有的代码高亮样式选项的数组
        $scope.allCodeHighlightStyles = [];

        $scope.isFollowing                 = $scope.$parent.isFollowing;
        $scope.followUser                  = $scope.$parent.followUser;
        $scope.unfollowUser                = $scope.$parent.unfollowUser;
        $scope.askToSearchTopic            = $scope.$parent.askToSearchTopic;
        $scope.getCardImageSrcByLineNumber = $scope.$parent.getCardImageSrcByLineNumber;
        $scope.inspect_image               = $scope.$parent.inspect_image;
        $scope.openExternalLink            = $scope.$parent.openExternalLink;
        $scope.reportCard                  = $scope.$parent.reportCard;
        $scope.likeCard                    = $scope.$parent.likeCard;
        $scope.cancelLikeCard              = $scope.$parent.cancelLikeCard;
        $scope.shareCard                   = $scope.$parent.shareCard;
        $scope.deleteCard                  = $scope.$parent.deleteCard;
        $scope.deleteComment               = $scope.$parent.deleteComment;
        $scope.likeComment                 = $scope.$parent.likeComment;
        $scope.replyComment                = $scope.$parent.replyComment;
        $scope.submitComment               = $scope.$parent.submitComment;

        $scope.setRenderer = function (renderer) {
            switch (renderer) {
                case "primitive":
                    $scope.current_card.isMarkdownEnabled = false;
                    $scope.current_card.scriptIsEnabled = false;
                    $scope.current_card.isHtmlEnabled = false;
                    break;
                case "wcml":
                    $scope.current_card.isMarkdownEnabled = false;
                    $scope.current_card.scriptIsEnabled = true;
                    $scope.current_card.isHtmlEnabled = false;
                    break;
                case "markdown":
                    $scope.current_card.isMarkdownEnabled = true;
                    $scope.current_card.scriptIsEnabled = true;
                    $scope.current_card.isHtmlEnabled = false;
                    break;
                case "html":
                    $scope.current_card.isMarkdownEnabled = false;
                    $scope.current_card.scriptIsEnabled = false;
                    $scope.current_card.isHtmlEnabled = true;
                    break;
                default:
                    break;
            }
        };



        $timeout(function() {
            // 初始化代码高亮样式列表
            $scope.initCodeHighlightStyles();
            // 初始化默认代码高亮样式
            let defaultStyleName = $window.localStorage.getItem("default_code_highlight_style_name");
            if (defaultStyleName == null) {
              defaultStyleName = "Default";
            }
            $scope.selectCodeHighlightStyle({styleName: defaultStyleName});
        }, 100);
    
    
    
        $scope.initCodeHighlightStyles = function () {
            $("link[rel=\"alternate stylesheet\"]").each(function () {
                $scope.allCodeHighlightStyles.push({
                    styleName: $(this).attr("title"),
                    selected: false
                });
            });
        };
    
    
    
        $scope.selectCodeHighlightStyle = function (option) {
            $("link[rel=\"alternate stylesheet\"]").each(function () {
                if ($(this).attr("title") === option.styleName) {
                    $(this).removeAttr("disabled");
                    $scope.allCodeHighlightStyles = $scope.allCodeHighlightStyles.map((e) => {
                        if (e.styleName === option.styleName) {
                            e.selected = true;
                            $window.localStorage.setItem("default_code_highlight_style_name", option.styleName);
                        } else {
                            e.selected = false;
                        }
                        return e;
                    });
                } else {
                    $(this).attr("disabled", "disabled");
                }
            });
        };



        $scope.getTable = function (data) {
            let table = $window.parseTable(data.tableContentLines).addClass("table table-sm border-bottom mb-0");
            return $sce.trustAsHtml(table.get(0).outerHTML);
        };

        function getCard(current_card, msg) {
            let target_card = current_card;
            let func = function () {
                target_card = current_card;
                return "found";
            };
            if (msg === "prev") {
                func = function (cardGroup) {
                    for (let i = 0; i < cardGroup.length; ++i) {
                        if (cardGroup[i].id === current_card.id) {
                            if (i > 0) {
                                target_card = cardGroup[i - 1];
                                return "found";
                            } else {
                                return "get last of prev group";
                            }
                        }
                    }
                };
            } else if (msg === "next") {
                func = function (cardGroup) {
                    for (let i = 0; i < cardGroup.length; ++i) {
                        if (cardGroup[i].id === current_card.id) {
                            if (i + 1 < cardGroup.length) {
                                target_card = cardGroup[i + 1];
                                return "found";
                            } else {
                                return "get first of next group";
                            }
                        }
                    }
                };
            }
            let cardGroups = $scope.$parent.cardGroups;
            let result = null;
            let target_group = [];
            for (let i = 0; i < cardGroups.length; ++i) {
                result = func(cardGroups[i]);
                if (result === "found") {
                    break;
                } else if (result === "get last of prev group") {
                    if (i > 0) {
                        target_group = cardGroups[i - 1];
                        if (target_group.length) {
                            target_card = target_group[target_group.length - 1];
                        }
                    }
                    break;
                } else if (result === "get first of next group") {
                    if (i + 1 < cardGroups.length) {
                        target_group = cardGroups[i + 1];
                        if (target_group.length) {
                            target_card = target_group[0];
                        }
                    }
                    break;
                }
            }
            return target_card;
        }

        $scope.viewPrevCard = function () {
            let card = getCard($scope.$parent.current_card, "prev");
            if (card.id === $scope.$parent.current_card.id) {
                $scope.noPrevCard = true;
                $timeout(function () {
                    $scope.noPrevCard = false;
                }, 1000);
            } else {
                $scope.$parent.viewCard(card);
            }
        };
        $scope.viewNextCard = function () {
            let card = getCard($scope.$parent.current_card, "next");
            if (card.id === $scope.$parent.current_card.id) {
                $scope.noNextCard = true;
                $timeout(function () {
                    $scope.noNextCard = false;
                }, 1000);
            } else {
                $scope.$parent.viewCard(card);
            }
        };
    }

    /* @ngInject */
    function wcCardViewModal() {
        return {
            restrict: 'E',
            scope: {
                "current_card": "=currentCard",
                "user": "=user"
            },
            controller: "cardViewController",
            templateUrl: function(element, attrs) {
                return attrs.templateUrl || 'js/templates/card-view-modal.html';
            }
        }
    }
})();