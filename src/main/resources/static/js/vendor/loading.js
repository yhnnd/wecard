// attention:
// "var" cannot be replaced by "let"
// because "var" can be attached to window but "let" cannot
var loading = {
    foreground: null,
    loadingIdPool: {},
    loadingIdPoolHistory: {},
    // 获取注册表
    getLoadingIdPool: function () {
        return this.loadingIdPool;
    },
    // 设置注册表
    setLoadingIdPool: function (loadingId, autoGenTaskName, devSpecTaskName) {
        let loadingIdPool = this.loadingIdPool;
        let taskInfo = {
            "autoGenTaskName": autoGenTaskName,
            "devSpecTaskName": devSpecTaskName
        };
        loadingIdPool[loadingId] = taskInfo;
        this.loadingIdPoolHistory[loadingId] = taskInfo;
        console.log("set_loading_id_pool(" + loadingId + ")", "pool:", loadingIdPool);
        return Object.keys(loadingIdPool).length;
    },
    // 删除注册表
    removeLoadingId: function (loadingId) {
        let loadingIdPool = this.loadingIdPool;
        delete loadingIdPool[loadingId];
        console.log("remove_loading_id_pool(" + loadingId + ")", "pool:", loadingIdPool);
        return Object.keys(loadingIdPool).length;
    },
    // 获取进程信息
    getLoadingTask: function (loadingId) {
        return this.loadingIdPool[loadingId];
    },
    // 获取正在运行的进程数量
    getLoadingTaskLength: function () {
        return Object.keys(this.loadingIdPool).length;
    }
};






// 显示 "加载中" 图标
function startLoading(maxLoadingTime, callerName) {
    let loadingId = "loading-" + getRandomString();
    let autoGenTaskName = startLoading.caller.name;

    let loadingTaskLength = window.loading.setLoadingIdPool(loadingId, autoGenTaskName, callerName);

    let loadingForeground = window.loading.foreground;
    let taskName = "<div class=\"task-name overflow-hidden\" id=\"name-of-" + loadingId + "\">" +
            "<span class='task-loading-id'>" + (window.screen.width >= 1000 ? loadingId + "&nbsp;" : "") + "</span>" +
            (autoGenTaskName ? "<span class='auto-gen-task-name'>" +
                "<span class='scope'>window</span><span class='dot'>.</span>" + autoGenTaskName + "</span>" : "") +
            (callerName ? "<span class='dev-spec-task-name'>" + callerName + "</span>" : "") +
            "</div>";
    // 如果【除了当前任务之外】没有正在加载中的任务
    if (!loadingForeground || loadingTaskLength < 2) {
        // 使背景模糊
        $("body>*")
            .not("script")
            .not(".loading-background")
            .addClass('loading-background');
        // 添加前景
        loadingForeground = $("<div id=\"loading-foreground\">")
            .addClass("loading-foreground w-100 fixed-top d-flex align-items-center justify-content-center")
            .css("height", window.innerHeight + "px");

        loadingForeground.append("<div>" +
                "   <div class=\"wrapper loading-spinner-wrapper\">" +
                "       <i class='fa fa-spinner fa-pulse fa-3x fa-fw'></i>" +
                "   </div>" + (
                    debugMode.isEnabled ?
                    "   <div class=\"wrapper loading-tasks-wrapper\">" +
                    "       <div class='loading-task-length-indicator'>" +
                    "           <span class=\"loading-task-length\">" +
                    "               " + loadingTaskLength +
                    "           </span>" +
                    "           <span>个任务正在执行</span>" +
                    "       </div>" +
                    "       <div class=\"wrapper loading-task-list\">" +
                    "           " + taskName +
                    "       </div>" +
                    "   </div>" : ""
                ) + "</div>");

        if (debugMode.isEnabled) {
            loadingForeground.append("<div class='fixed-top text-right'>" +
                "   <i class='fa fa-3x fa-times-circle text-white mt-3 mr-4' onclick='abortLoading()'></i>" +
                "</div>");
        }
        loadingForeground.appendTo("body");
        window.loading.foreground = loadingForeground;
    } else {
        // 如果有正在加载中的任务
        loadingForeground.find(".loading-task-length").text(loadingTaskLength);
        loadingForeground.find(".loading-task-list").append(taskName);
    }
    // 定时清理
    if (maxLoadingTime) {
        setTimeout(function () {
            if (window.loading.getLoadingTask(loadingId)) {
                stopLoading(loadingId, "timeout", maxLoadingTime);
            }
        }, maxLoadingTime);
    }
    return loadingId;
}


var loadingCompleteErrorMsgs = [
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-0 loading ID not found",
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-1 loading ID pool not found",
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-2 loading ID was not found in the loading ID pool",
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-3 loading foreground not found",
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-4 loading ID pool not found (after removing current loading ID)",
    "加载中组件在执行【停止加载】时出现错误: <br>ERR-5 loading ID was not found in the loading ID pool" +
    "<br>and was not in loading ID pool history either"
];


function stopLoadingError() {
    if (debugMode.isEnabled) {
        if (debugMode.isAllowErrorPrompt) {
            bsConfirm(...arguments);
        } else {
            console.log(...arguments);
        }
    }
}


// 删除 "加载中" 图标
function stopLoading(loadingId, exception, maxLoadingTime) {
    // 加载错误信息内容
    let errorMsg = window.loadingCompleteErrorMsgs;
    let loadingIdPoolHistory = window.loading.loadingIdPoolHistory;
    let loadingForeground = window.loading.foreground;
    let loadingTaskLength = window.loading.getLoadingTaskLength();
    let loadingTaskLengthNew = null;
    let taskStatus = "done";

    if (!loadingId) {
        stopLoadingError("stop loading:",errorMsg[0],"alert-danger");
        return false;
    }
    // 当前任务的信息
    let errorMsgMoreInfo = "<br><span>loading ID: " + loadingId + "</span>";
    // 获取最新的注册表
    let loadingIdPool = window.loading.getLoadingIdPool();
    // 注册表丢失
    if (!loadingIdPool) {
        stopLoadingError("stop loading:",errorMsg[1],"alert-danger");
        return false;
    }
    if (!loadingIdPool[loadingId]) {
        if (loadingIdPoolHistory[loadingId]) {
            // 获取更多有关当前任务的信息（从 History 中）
            errorMsgMoreInfo +=
                "<br><span class='text-dark'>task info below were found in history</span>" +
                "<br><span>task name (auto detected): " + loadingIdPoolHistory[loadingId].autoGenTaskName + "</span>" +
                "<br><span>task name (dev specified): " + loadingIdPoolHistory[loadingId].devSpecTaskName + "</span>";
            stopLoadingError("stop loading:", errorMsg[2] + errorMsgMoreInfo, "alert-danger");
        } else {
            stopLoadingError("stop loading:", errorMsg[5] + errorMsgMoreInfo, "alert-danger");
        }
        if (!exception) {
            return false;
        }
    } else {
        // 获取更多有关当前任务的信息
        errorMsgMoreInfo +=
            "<br><span>task name (auto detected): " + loadingIdPool[loadingId].autoGenTaskName + "</span>" +
            "<br><span>task name (dev specified): " + loadingIdPool[loadingId].devSpecTaskName + "</span>";
    }
    if (exception) {
        if (exception === "timeout") {
                taskStatus = "timeout";
                bsAlert("提示消息",
                    "网络连接不畅<br>请求已经发送 " + (maxLoadingTime / 1000) + " 秒了, 服务器仍未返回结果,<br>" +
                    "任务<span class='text-danger'>未被取消</span>, 而已经被转为后台运行.<br>" +
                    "任务名(自动生成): " + loadingIdPoolHistory[loadingId].autoGenTaskName + "<br>" +
                    "任务名(开发者指定): " + loadingIdPoolHistory[loadingId].devSpecTaskName,
                    "alert-warning",
                    60000);
        } else {
            bsAlert("服务器发生错误",
                "task name (auto detected): " + loadingIdPoolHistory[loadingId].autoGenTaskName + "<br>" +
                "task name (dev specified): " + loadingIdPoolHistory[loadingId].devSpecTaskName + "<br>" +
                "exception info is shown below<br>" +
                "status: " + exception.data.status + "<br>" +
                "error: " + exception.data.error + "<br>" +
                "message: " +
                "<pre class='w-100 break-all'>" + exception.data.message + "</pre>",
                "alert-danger",
                -1);
        }
    }

    // 从注册表中 删除 当前加载中的任务
    loadingTaskLengthNew = window.loading.removeLoadingId(loadingId);

    // 刷新正在加载中的任务的数量
    if (loadingForeground) {
        if (loadingTaskLengthNew && loadingTaskLengthNew < loadingTaskLength) {
            loadingForeground.find(".loading-task-length").text(loadingTaskLengthNew);
        }
        loadingForeground.find(".loading-task-list>.task-name#name-of-" + loadingId).addClass(taskStatus);
    } else {
        // 前景丢失
        stopLoadingError("stop loading:",errorMsg[3] + errorMsgMoreInfo,"alert-danger");
        return false;
    }
    // 获取删除当前任务后的的注册表
    loadingIdPool = window.loading.getLoadingIdPool();
    if (!loadingIdPool) {
        // 注册表丢失
        stopLoadingError("stop loading:",errorMsg[4] + errorMsgMoreInfo,"alert-danger");
        return false;
    } else if (JSON.stringify(loadingIdPool) === "{}") {
        console.log("no loadingId in the pool. stop loading...");
        window.loading.foreground = null;
        $(".loading-foreground").remove();
        $(".loading-background").removeClass("loading-background");
    }
}


// 用户强制关闭加载中页面
function abortLoading() {
    $(".loading-foreground").remove();
    $(".loading-background").removeClass("loading-background");
}