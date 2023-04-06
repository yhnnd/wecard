function enablePressEnterToSend() {
    localStorage.setItem("press_enter_to_send", true);
    console.log("enable press enter to send");
}

function disablePressEnterToSend() {
    localStorage.setItem("press_enter_to_send", false);
    console.log("disable press enter to send");
}

function init_button_values() {
    $("#toggle-press-enter-to-send").attr("data-value", localStorage.getItem("press_enter_to_send"));
    $("#toggle-night-mode").attr("data-value", localStorage.getItem("is_night_mode_enabled"));
    $("#toggle-debug-mode").attr("data-value", localStorage.getItem("debug_mode_enabled"));
    $("#toggle-allow-error-prompt").attr("data-value", localStorage.getItem("debug_mode_is_allow_error_prompt"));
    $("#toggle-always-view-card-in-another-page").attr("data-value", window.toggleViewCardMethod.storeValue());
    ios_button_init(".settings");
}

// 监听消息输入框键盘按下
function listenPressEnter() {
    let input = $('#message');
    if (input) {
        input.off('keydown').on('keydown', function (e) {
            if (e.keyCode == 13) {// 如果按下的键是回车
                if (localStorage.getItem("press_enter_to_send") == undefined) {// == 不能变成 ===
                    if (confirm("是否开启按回车发送消息？")) {
                        enablePressEnterToSend();
                    } else {
                        disablePressEnterToSend();
                    }
                    init_button_values();
                }
                if (![undefined, null, false, 0, "false", "0"].includes(localStorage.getItem("press_enter_to_send"))) {
                    // 如果已开启按回车发送消息功能
                    e.preventDefault();
                    var model = document.getElementById('user-data');
                    let scope = angular.element(model).scope();
                    scope.sendMessage();// 发送消息
                }
            }
        });
    } else {
        alert("listenPressEnter: ERROR 未找到聊天窗口的消息输入框");
    }
}

// 因为聊天窗口被封装到组件中了, 组件加载需要一些时间, 所以延时开启监听程序
setTimeout(listenPressEnter, 2000);



var nightMode = {
    isEnabled: undefined,
    getScope: function () {
        let model = document.querySelector('[ng-controller="controller"]');
        let scope = angular.element(model).scope();
        if (!scope) {
            model = document.getElementById('card-groups');
            scope = angular.element(model).scope();
        }
        return scope;
    },
    notifyScope: function () {
        let scope = this.getScope();
        if (scope) {
            scope.nightMode = this;
            scope.$apply();
        } else {
            bsConfirm("nightMode.notifyScope", "scope not found", "alert-danger");
        }
    },
    enable: function () {
        this.isEnabled = true;
        localStorage.setItem("is_night_mode_enabled", true);
        this.notifyScope();
        const content = "<div class='btn btn-sm btn-outline-primary py-0' onclick='window.location.reload()'>刷新页面</div>之后生效";
        bsAlert("按时开启夜间模式功能已启用", content, "dme dme-b", -1);
    },
    disable: function () {
        this.isEnabled = false;
        localStorage.setItem("is_night_mode_enabled", false);
        this.notifyScope();
        const content = "<div class='btn btn-sm btn-outline-primary py-0' onclick='window.location.reload()'>刷新页面</div>之后生效";
        bsAlert("按时开启夜间模式功能已停用", content, "alert-secondary", -1);
    },
    init: function () {
        this.isEnabled = JSON.parse(localStorage.getItem("is_night_mode_enabled"));
    }
};

(function (nightMode) {
    nightMode.init();
})(window.nightMode);



var debugMode = {
    isEnabled: undefined,
    getScope: function () {
        let model = document.querySelector('[ng-controller="controller"]');
        let scope = angular.element(model).scope();
        if (!scope) {
            model = document.getElementById('card-groups');
            scope = angular.element(model).scope();
        }
        return scope;
    },
    notifyScope: function () {
        const scope = this.getScope();
        if (scope) {
            scope.debugMode = this;
            scope.$apply();
        } else {
            bsConfirm("debugMode.notifyScope", "scope not found", "alert-danger");
        }
    },
    enable: function () {
        this.isEnabled = true;
        localStorage.setItem("debug_mode_enabled", true);
        this.notifyScope();
    },
    disable: function () {
        this.isEnabled = false;
        localStorage.setItem("debug_mode_enabled", false);
        this.notifyScope();
    },
    isAllowErrorPrompt: undefined,
    allowErrorPrompt () {
        this.isAllowErrorPrompt = true;
        localStorage.setItem("debug_mode_is_allow_error_prompt", true);
        this.notifyScope();
    },
    muteErrorPrompt () {
        this.isAllowErrorPrompt = false;
        localStorage.setItem("debug_mode_is_allow_error_prompt", false);
        this.notifyScope();
    },
    init: function () {
        this.isEnabled = JSON.parse(localStorage.getItem("debug_mode_enabled"));
        this.isAllowErrorPrompt = JSON.parse(localStorage.getItem("debug_mode_is_allow_error_prompt"));
    }
};





var toggleViewCardMethod = {
    scope: function() {
        return window.debugMode.getScope();
    },
    storeValue: function(value) {
        if (value == undefined) {
            return window.localStorage.getItem("always_view_card_in_another_page");
        } else {
            window.localStorage.setItem("always_view_card_in_another_page", value);
        }
    },
    on: function () {
        this.storeValue(true);
        var $scope = this.scope();
        $scope.$apply(function () {
            $scope.alwaysViewCardInAnotherPage = true;
        });
    },
    off: function () {
        this.storeValue(false);
        var $scope = this.scope();
        $scope.$apply(function () {
            $scope.alwaysViewCardInAnotherPage = false;
        });
    }
};




(function (debugMode) {
    debugMode.init();

    function checkDebugModeConfigured () {
        if (debugMode.getScope()) {
            init_button_values();// 激活设置开关

            if (debugMode.isEnabled == undefined) {// == 不能变成 ===
                bsConfirm({
                    title: "开发者模式（全局设置）",
                    content: "是否开启开发者模式？<br>(此设置永久有效)",
                    alertClass: "alert-primary",
                    rejectText: "关闭 (Disable)",
                    confirmText: "开启 (Enable)",
                    rejectCallback: function () {
                        debugMode.disable();
                        init_button_values();// 刷新设置开关
                    },
                    confirmCallback: function () {
                        debugMode.enable();
                        init_button_values();// 刷新设置开关
                        bsConfirm({
                            title: "开发者模式（全局设置）",
                            content: "是否开启错误警告功能？<br>(此设置永久有效)",
                            alertClass: "alert-warning",
                            confirmText: "开启 (Enable)",
                            rejectText: "关闭 (Disable)",
                            confirmCallback: function () {
                                debugMode.allowErrorPrompt();
                                init_button_values();// 刷新设置开关
                                bsAlert("press_enter_to_send.js","开发者模式已开启","alert-primary");
                            },
                            rejectCallback: function () {
                                debugMode.muteErrorPrompt();
                                init_button_values();// 刷新设置开关
                                bsAlert("press_enter_to_send.js","错误警告功能被停用","alert-secondary");
                            }
                        });
                    }
                });
            }
        }
    }

    // 延时执行检查开发者模式是否被配置的任务
    setTimeout(checkDebugModeConfigured, 3000);

})(debugMode);