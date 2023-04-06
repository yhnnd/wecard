(function () {
    let app = angular.module('module-index', []);

    app.controller("controller", function ($scope, $http, $timeout, $interval, $window, $sce) {

        $scope.httpRoot = $window.getHttpRoot();
        $scope.max = $window.max;
        $scope.$window = $window;
        // 用户个人信息
        $scope.user = null;
        // 当前正在查看的卡片
        $scope.current_card = null;
        // 卡片评论显示尺寸
        $scope.comment_size = "md";
        // 所有的代码高亮样式选项的数组
        $scope.allCodeHighlightStyles = [];



        $scope.login = function () {
            $scope.user = $window.mock.data.userdata.user;
            $scope.userData = {
                "user": $scope.user
            };
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


        $scope.replyComment = function (parentId) {
            let replyInput = $("textarea[data-parent-id='" + parentId + "']");
            let replyText = $(replyInput).val();

            console.log("replyComment(): 正在给卡片 " + $scope.current_card.id + " 下的评论 " + parentId + " 添加回复 " + replyText);

            if (!$scope.current_card.id) {
                bsAlert("reply comment", "卡片 ID 不能为空", "alert-danger");
                return;
            }
            if (!parentId) {
                bsAlert("reply comment", "parent ID 不能为空", "alert-danger");
                return;
            }
            if (!replyText) {
                bsAlert("reply comment", "评论不能为空", "alert-danger");
                return;
            }
            const reply = {
                "cardId": $scope.current_card.id,
                "parentId": parentId,
                "text": replyText,
                "status": "exist",
                "user": $scope.user
            };
            // 清空回复评论输入框
            $(replyInput).attr("value", "");
            $(replyInput).val("");
            // 刷新当前卡片的所有评论
            $scope.current_card.comments = _.map($scope.current_card.comments, function (comment) {
                // 如果当前遍历到的一级评论就是被回复的评论
                if (comment.id === parentId) {
                    // 如果这个评论有被回复
                    if (comment.children) {
                        // comment.children.unshift(reply);
                        comment.children.push(reply);
                    } else {// 如果这个评论没有被回复过
                        comment.children = [reply];
                    }
                }
                return comment;
            });
        };



        function getRandomString() {
            return ("" + Math.random() * 10).split(".").join("");
        }



        this.$onInit = function () {
            const currentCardId = getParameter("card-id");
            $window.mock = new Mock();
            $window.mock.loadData("cards", "cards.json");
            $window.mock.loadData("userdata", "user-data.json").then(function () {
                const card = _.find($window.mock.data.cards, function (item) {
                    return item.id === currentCardId;
                });
                $scope.viewCard(card);
            });
            // 初始化代码高亮样式列表
            $scope.initCodeHighlightStyles();
            // 初始化默认代码高亮样式
            let defaultStyleName = $window.localStorage.getItem("default_code_highlight_style_name");
            if (defaultStyleName == null) {
                defaultStyleName = "Default";
            }
            $scope.selectCodeHighlightStyle({ styleName: defaultStyleName });
        };



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
            let table = $window.wcTable.parseTable(data.tableContentLines).addClass("table table-sm border-bottom mb-0");
            return $sce.trustAsHtml(table.get(0).outerHTML);
        };



        $scope.inspect_image = function (imageUrl) {
            $window.inspect_image(imageUrl);
        };



        $scope.split = function (message, delimiter) {
            return message.split(delimiter);
        };



        $scope.getUsers = function (user, callback, errorCallback) {
            $http({
                method: 'GET',
                url: $scope.httpRoot + apis.search.users,
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
            }, function (err) {
                errorCallback && errorCallback(err);
            });
        };






        // 解析当前浏览的卡片的 WCML 内容
        // 此前为: 监听当前浏览的卡片的内容更新
        // 现改为: 被 AJAX 的 callback 调用
        function parseCurrentCardWCML() {
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
                        if (line.trim() === "```" || (line.startsWith("*") && line.endsWith("*"))) {
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
                                "plainTextPositions": [0, 2],
                                "iconId": iconId
                            };
                            let username = parameters[2];
                            let user = {
                                username: username
                            };
                            $scope.getUsers(user, function (Users) {
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
                                    " data-toggle='tooltip' title='" + errMsgZHCN + "'></span>");
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
                        lineNo++;
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
            if (!messageText) {
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

            $window.bsConfirm({
                "title": "即将打开外部链接",
                "content": "<div class='" + name + "'>确认打开外部链接 " + a[0].outerHTML + " 吗?</div>",
                "alertClass": "alert-danger",
                "confirmText": "打开链接",
                "confirmCallback": function () {
                    $("#" + name).attr({
                        "href": link,
                        "target": "_blank"
                    })[0].click();/* 如果不加上 [0] 则 click 事件无效 */
                },
                "rejectText": "取消",
                "rejectCallback": function () {}
            });
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


        function handleMarkdownLinks(field) {
            $(field).find("a").each(function (index, elem) {
                let url = $(elem).attr("href");
                $(elem)
                    .attr("href", "javascript:void(0);")
                    .off("click")
                    .on("click", function () {
                        $scope.openExternalLink(url);
                    });
            });
        }

        function handleMarkdownImages(field) {
            $(field).find("img")
                .addClass("img-thumbnail")
                .off("click")
                .on("click", function () {
                    let src = $(this).attr("src");
                    $scope.inspect_image(src);
                });
        }


        // 解析卡片 markdown 内容
        function parseCurrentCardMarkdown(card, field) {
            if (card && card.text) {
                if (marked) {
                    let languages = [];
                    marked.setOptions({
                        renderer: new marked.Renderer(),
                        highlight: function (code, language) {
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
                    $(field).html(marked.parse(card.text));
                    handleMarkdownLinks(field);
                    handleMarkdownImages(field);
                    $(field).find("pre").each(function (index, elem) {
                        if ($(elem).has("code") == false) {
                            $(elem).addClass("line break-all");
                        }
                    });
                    $(field).find("pre > code").each(function (index, elem) {
                        $(elem).addClass("hljs " + languages[index]);
                        $(elem).closest("pre").wrap("<details open>");
                        $(elem).closest("details").prepend("<summary class='bg-dark text-white px-2'>" + languages[index] + "</summary>");
                    });
                } else {
                    bsAlert("parseCurrentCardMarkdown():", "Markdown 插件缺失", "alert-danger", -1);
                }
            } else {
                bsAlert("parseCurrentCardMarkdown():", "卡片正文缺失", "alert-danger", -1);
            }
        }



        $scope.viewCard = function (card) {
            if (!card || !card.id) {
                bsAlert("$scope.viewCard():", "ERR-1 parameter 'card' was not provided", "alert-danger", -1);
                return false;
            }

            let isViewShared = false;
            if (arguments[1]) {
                if (typeof arguments[1] !== "string") {
                    bsAlert("$scope.viewCard(\"" + card.title + "\"):", "ERR-2 无法识别的附加指令: \"" + arguments[1] + "\"", "alert-danger");
                    return false;
                } else if (arguments[1] === "viewSharedCard") {
                    isViewShared = true;
                    loadingId = startLoading(max.loading.delay.time, "$scope.viewCard(\"" + card.title + "\",\"" + arguments[1] + "\")");
                } else {
                    bsAlert("$scope.viewCard(\"" + card.title + "\"):", "ERR-3 无法识别的附加指令: \"" + arguments[1] + "\"", "alert-danger");
                    return false;
                }
            }

            //   $http({
            //     method: "get",
            //     url: $scope.httpRoot + apis.get.card.byId,
            //     params: {
            //       cardId: card.id,
            //       start: 0,
            //       limit: $scope.max.get.card.comments.limit
            //     },
            //     crossDomain: true,
            //     withCredentials: true
            //   }).then(
            //     function(result) {
            /** Mock Data */
            const result = {
                data: {
                    "message": "card load success",
                    "card": card
                }
            };

            switch (result.data.message) {
                case "card load failed":
                    bsAlert("$scope.viewCard():", "卡片加载失败", "alert-danger", -1);
                    break;
                case "card load success":
                    if (!result.data.card) {
                        bsAlert("$scope.viewCard():", "卡片不存在", "alert-danger", -1);
                        return false;
                    }
                    if (
                        result.data.card.hasOwnProperty("ilike") &&
                        !result.data.card.hasOwnProperty("iLike")
                    ) {
                        result.data.card.iLike = result.data.card.ilike;
                    }
                    // 判断是否正在查看【被转发的卡片】
                    if (isViewShared) {
                        if ($scope.current_card && result.data.card) {
                            $scope.current_card.share = result.data.card;
                        }
                    } else {
                        // 正在查看的卡片不是【被转发的卡片】
                        $scope.current_card = null;
                        // 在有些情况下，成功收到卡片数据然而渲染失败，仍会显示前次浏览的卡片内容。
                        // 所以在【显示 modal】并【渲染卡片】之前，有必要先清空卡片数据。

                        if (result.data.card) {
                            $scope.current_card = result.data.card;
                            // 解析卡片的 WCML 内容
                            parseCurrentCardWCML();
                            // 解析卡片 markdown 内容
                            parseCurrentCardMarkdown(result.data.card, "#view-card #marked");

                            // 如果当前卡片是 share 类型的卡片
                            if (
                                card.type === "share" &&
                                $scope.current_card.type === "share"
                            ) {
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
                    $scope.$digest();
                    break;
                default:
                    bsAlert("$scope.viewCard():", "未知错误：" + result.data.message, "alert-danger", -1);
                    break;
            }
            //     },
            //     function() {
            //       bsAlert("$scope.viewCard():", "与服务器连接失败", "alert-danger", -1);
            //     }
            //   );
        };
    });
})()