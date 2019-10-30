var v2_7 = {
    fieldSelector: false,
    field: false,
    saveCallback: false,
    LEFT: 37,
    UP: 38,
    RIGHT: 39,
    DOWN: 40,
    data: {
        prevKeyCode: false,
        helpTitle: "",
        helpContent: "",
        helpClass: ""
    },
    observerMap: new Map(),
    onTextChange: null,
    tools: function(event) {
        var self = this;
        return {
            "insertNewLine": function() {
                self.insertNewLine(event);
            },
            "getCursorPosition": function() {
                return self.getCursorPosition();
            }
        };
    },
    help: function () {
        if ($(".help-prompt").length) {
            removeAlert($(".help-prompt"));
        }
        bsAlert(this.data.helpTitle, this.data.helpContent, this.data.helpClass + " help-prompt", -1);
    },
    getField: function() {
        if (this.fieldSelector) {
            return $(this.fieldSelector);
        } else {
            return false;
        }
    },
    getScope: function () {
        let model = document.getElementById('user-data');
        let scope = angular.element(model).scope();
        if (!scope) {
            model = document.getElementById('card-groups');
            scope = angular.element(model).scope();
        }
        return scope;
    },
    getNewLine: function() {
        var id = "text-editable-" + new Date().getTime()
        var newLine = $("<pre>")
            .addClass("line break-all text-editable")
            .attr({
                id: id,
                contenteditable: true
            });
        return newLine;
    },
    insertNewLine: function (event) {
        // 获取当前文本行
        var target = null;
        if (event) {
            target = $(event.target).closest(".text-editable");
        } else {
            target = this.field.find(".text-editable").last();
        }
        // 新的文本行
        var newLine = this.getNewLine();
        // 在当前文本行的下面插入新的文本行
        target.after(newLine);
        // 获取新的文本行 DOM
        newLine = target.siblings("#" + newLine.attr("id"));
        // 为新的文本行 DOM 添加监听编辑事件
        this.addEditListener(newLine);
        // 将光标移动到新的文本行的开始
        newLine.focus();
        return newLine;
    },
    // 第一个参数是编辑区域的选择器, 类型是字符串
    // 第二个参数是保存编辑区域内容的回调函数, 调用的时候会注入参数 $scope 和 text
    // 其中
    //      $scope 是外部作用域
    //      text 是编辑区域的内容
    // 第三个参数是自定义的按键监听器, 调用的时候会注入参数 event, this.data, tools 以及 $scope
    // 其中
    //      event 是按键事件
    //      this.data 是存放自定义数据的空间, editor 不会访问其中的内容
    //      tools 是 editor 的公开方法的集合
    //      $scope 就是 editor 所绑定的作用域
    init: function(fieldSelector, saveCallback, keyPressListener, onTextChange) {
        // 绑定编辑区域
        this.fieldSelector = fieldSelector;
        // 绑定保存函数
        this.saveCallback = saveCallback;
        // 绑定编辑区域
        this.field = this.getField();
        // 绑定聚焦监听器
        this.addFocusListener();
        // 注册按键监听器
        this.keyPressListener = keyPressListener;
        // 注册文本改变监听器
        this.onTextChange = onTextChange;
        // 初始化编辑区域
        this.empty();
    },
    // 绑定点击事件, 当点击编辑区域的时候, 添加自定义的 focus 类
    addFocusListener: function() {
        var self = this;
        $(document).on("click", function(event) {
            var target = event.target;
            var parent = $(target).parent();
            var parent_2 = parent.parent();
            var parent_3 = parent_2.parent();
            if ($(target).is(self.fieldSelector)
            || parent.is(self.fieldSelector)
            || parent_2.is(self.fieldSelector)
            || parent_3.is(self.fieldSelector)
            ) {
                if (self.field.hasClass("focus") != true) {
                    console.log("v2.7 editor: clickListener: focused.");
                    self.field.addClass("focus");
                } else {
                    console.log("v2.7 editor: clickListener: already focused.");
                }
                // 如果点击到了编辑区域但未点击到文本行
                if ($(target).is(self.fieldSelector)) {
                    console.log("v2.7 editor: clickListener: auto focus on last line.");
                    self.field.find(".text-editable").last().focus();
                    self.setCursorPosition(self.getCursorPositionMax());
                }
            } else if (self.field.hasClass("focus")) {
                self.field.removeClass("focus");
                console.log("v2.7 editor: clickListener: blurred.");
                self.save();
            }
        });
    },
    getRandomString: function () {
        return ("" + Math.random() * 10).split(".").join("");
    },
    addEditListener: function(newLine) {
        let self = this;
        // 此处不能用 keypress, 因为 keypress 不能监听到 13(回车) 和 8(退格)
        // 此处加上 click 的原因是, 如果不加上 click, 用户通过鼠标移动光标, 将不会被 save 函数保存
        newLine.on("keydown", function (event) {
            self.edit(event);
        });
        // 创建观察器
        var oldValue = null;
        let observer = new MutationObserver(function (mutations, instance) {
            mutations.forEach(function (mutation) {
                if (mutation.type === "childList") {
                    if (mutation.target.childNodes.length) {
                        let newValue = mutation.target.childNodes[0].data;
                        if (oldValue === null) {
                            let event = mutation;
                            event.preventDefault = function() {};
                            self.onTextChange(newValue, self.data, event, self.tools(event));
                        }
                        oldValue = newValue;
                    } else {
                        console.log("v2.7 editor: editListener: line is empty.");
                        oldValue = null;
                    }
                }
            });
        });
        // 观察文本行内容改变
        let text = newLine.get(0);
        observer.observe(text, {
            childList: true
        });
        // 注册观察器
        let observerId = this.getRandomString();
        this.observerMap.set(observerId, observer);
        newLine.data("observer-id", observerId);
        // 当删除这一行的时候, 观察器将会被停止
    },
    stopObserver: function(target) {
        var observerId = target.data("observer-id");
        this.observerMap.get(observerId).disconnect();
    },
    empty: function() {
        var newLine = this.getNewLine();
        this.field.empty().append(newLine);
        newLine = this.field.find("#" + newLine.attr("id"));
        this.addEditListener(newLine);
        return newLine;
    },
    getCursorPosition: function() {
        var selection = window.getSelection();
        return selection.focusOffset;
    },
    setCursorPosition: function(pos) {
        var selection = window.getSelection();
        var posMax = this.getCursorPositionMax();
        if (pos <= posMax) {
            selection.setPosition(selection.focusNode, pos);
        } else {
            selection.setPosition(selection.focusNode, posMax);
        }
    },
    getCursorPositionMax: function() {
        var selection = window.getSelection();
        if (selection.focusNode.innerText != null) {
            // 仅在文本行为空行的时候有效
            return selection.focusNode.innerText.length;
        } else if (selection.focusNode.parentElement.innerText != null) {
            // 在文本行不是空行的时候有效
            return selection.focusNode.parentElement.innerText.length;
        }
        return false;
    },
    // 编辑可编辑的文本行
    edit: function (event) {
        var self = this;
        // 获取当前文本行
        var target = $(event.target).closest(".text-editable");
        var text = target.text();
        // 获取当前光标位置
        var cursorPosition = this.getCursorPosition();
        if ([13, 8, this.UP, this.DOWN, this.LEFT, this.RIGHT].includes(event.keyCode)) {
            // 如果能获取当前光标位置
            if (typeof cursorPosition === "number") {
                // 如果按下的键是回车
                if (event.keyCode == 13) {
                    // 防止新的文本行会因为被输入了 13 而被添加 <div><br></div>
                    event.preventDefault();
                    // console.log("v2.7 editor: edit: keycode = ", event.keyCode);
                    // 光标前的文本
                    var textprev = text.substr(0, cursorPosition);
                    // 光标后的文本
                    var textnext = text.substr(cursorPosition);
                    // 新的文本行
                    var newLine = this.getNewLine();
                    if (cursorPosition > 0) {
                        // 将光标前的文本保留在当前文本行中
                        target.text(textprev);
                        // 将光标后的文本移动到新的文本行中
                        newLine.text(textnext);
                        // 在当前文本行的下面插入新的文本行
                        target.after(newLine);
                    } else {
                        // 将光标前的文本移动到新的文本行中
                        newLine.text(textprev);
                        // 将光标后的文本保留在当前文本行中
                        target.text(textnext);
                        // 在当前文本行的上面插入新的文本行
                        target.before(newLine);
                    }
                    // 获取新的文本行 DOM
                    newLine = target.siblings("#" + newLine.attr("id"));
                    // 为新的文本行 DOM 添加监听编辑事件
                    this.addEditListener(newLine);
                    if (cursorPosition > 0) {
                        // 将光标移动到新的文本行的开始
                        newLine.focus();
                    }
                } else if (event.keyCode == 8 && cursorPosition == 0) {
                    // 如果光标在文本行的开始, 并且按下了退格键
                    event.preventDefault();
                    // console.log("v2.7 editor: edit: keycode = ", event.keyCode);
                    // console.dir(event);
                    var prev = target.prev();
                    if (prev && prev.length) {
                        // 将上一行变成可编辑模式 (如果上一行不是文本, 而是图片或者链接, 则默认是不可编辑的)
                        prev.attr("contenteditable", true);
                        // 获取上一行的文字
                        var prevText = prev.text();
                        // 判断上一行是不是空行
                        if (prevText && prevText.length) {
                            // 如果上一行不是空行
                            // 删掉这一行
                            this.stopObserver(target);
                            target.remove();
                            // 将这一行的文字加在上一行的行尾
                            prev.text(prevText + text).focus();
                            // 将光标置于上一行原来行尾的位置, 而不是上一行现在行尾的位置
                            this.setCursorPosition(prevText.length);
                        } else {// 如果上一行是空行
                            // 删掉上一行
                            this.stopObserver(prev);
                            prev.remove();
                        }
                    }
                } else if ([this.UP, this.LEFT].includes(event.keyCode)) {
                    // 如果不是在第一行, 按下了向上键, 或在这行的行首按下了向左键
                    var prev = target.prev();
                    if (prev && prev.length) {
                        if (event.keyCode === this.UP) {
                            event.preventDefault();
                            // 将上一行变成可编辑模式 (如果上一行不是文本, 而是图片或者链接, 则默认是不可编辑的)
                            prev.attr("contenteditable", true);
                            prev.focus();
                            this.setCursorPosition(cursorPosition);
                        } else if (event.keyCode === this.LEFT && cursorPosition == 0) {
                            event.preventDefault();
                            // 将上一行变成可编辑模式 (如果上一行不是文本, 而是图片或者链接, 则默认是不可编辑的)
                            prev.attr("contenteditable", true);
                            prev.focus();
                            this.setCursorPosition(this.getCursorPositionMax());
                        }
                    }
                } else if ([this.DOWN, this.RIGHT].includes(event.keyCode)) {
                    // 如果不是在最后一行, 按下了向下键, 或在这行的行尾按下了向右键
                    var next = target.next();
                    if (next && next.length) {
                        if (event.keyCode === this.DOWN) {
                            event.preventDefault();
                            // 将下一行变成可编辑模式 (如果下一行不是文本, 而是图片或者链接, 则默认是不可编辑的)
                            next.attr("contenteditable", true);
                            next.focus();
                            this.setCursorPosition(cursorPosition);
                        } else if (event.keyCode === this.RIGHT && cursorPosition == this.getCursorPositionMax()) {
                            event.preventDefault();
                            // 将下一行变成可编辑模式 (如果下一行不是文本, 而是图片或者链接, 则默认是不可编辑的)
                            next.attr("contenteditable", true);
                            next.focus();
                            // 默认将光标放在下一行的行首
                            // 因为 focus 默认将光标置于行首, 所以我们不需要再设置一次
                            // this.setCursorPosition(0);
                        }
                    }
                }
            } else {
                console.log("v2.7 editor: edit: cursor position not found.");
            }
        }
        this.save();
        this.keyPressListener(event, this.data, this.tools(event), this.getScope());
    },
    // 保存可编辑的文本行
    save: function () {
        var lines = [];
        var fieldLines = this.field.find(".text-editable");
        for(var i = 0; i < fieldLines.length; ++i) {
            var fieldLine = fieldLines.get(i);
            var line = fieldLine.innerText;
            lines.push(line);
        }
        var $scope = this.getScope();
        var text = lines.join("\n");
        if (!$scope) {
            console.log("v2.7 editor: save: $scope not found.");
        } else if (!text) {
            console.log("v2.7 editor: save: no content to save.");
        } else {
            this.saveCallback($scope, text);
            // console.log("v2.7 editor: save: saved. ", lines);
        }
    }
};