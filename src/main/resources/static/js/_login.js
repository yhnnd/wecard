// 获取当前页面 url 中的参数
function getParameter(parameterName) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i = 0; i < vars.length; i++) {
    var pair = vars[i].split("=");
    if (pair[0] == parameterName) {
      return pair[1];
    }
  }
  return false;
}

// 跳转到 QQ 登录
function qqLogin() {
  window.location = getHttpRoot() + "/qqLogin";
}

// 显示登录错误信息
function printError(message, status, textStatus) {
  var field = $(".error-message-wrapper").css("visibility", "visible");
  field.find(".error-message").text(message);
  field.find(".error-status").text(status);
  field.find(".error-text-status").text(textStatus);
}

// 检查是否已经登录
function checkIsLoggedIn() {
  $.ajax({
    type: "get",
    url: window.getHttpRoot() + "/refresh",
    crossDomain: true,
    xhrFields: {
      withCredentials: true
    },
    success: function(data) {
      if (data.message === "Please login") {
        console.log("当前用户未登录");
      } else {
        loginSuccess();
      }
    },
    error: function(XMLHttpRequest, textStatus) {
      printError(
        "check is login: 无法与服务器建立连接",
        XMLHttpRequest.status,
        textStatus
      );
    }
  });
}

// 获取登录成功之后应该跳转到的页面
function getTargetPage() {
  var target = getParameter("target");
  const defaultTarget = "./index.html";
  if (target) {
    return target;
  }
  return defaultTarget;
}

// 登录成功之后执行的函数
function loginSuccess() {
  var otherParameters = "?from=" + window.location.pathname;
  var fromPage = getParameter("from");
  var toSubPage = getParameter("page");
  if (fromPage) {
    otherParameters += "&login-from=" + fromPage;
  }
  if (toSubPage) {
    otherParameters += "&page=" + toSubPage;
  }
  window.location.href = getTargetPage() + otherParameters;
}

// 登录 提交表单
function loginSubmit() {
  $.ajax({
    type: "get",
    url: window.getHttpRoot() + "/login",
    data: {
      username: $("#login-username").val(),
      password: $("#login-password").val()
    },
    crossDomain: true,
    xhrFields: {
      withCredentials: true
    },
    success: function(data) {
      if (["Login success", "The browser have Login"].includes(data.message)) {
        loginSuccess();
      } else if (["Have login elsewhere"].includes(data.message)) {
        printError("已在其他设备上登录");
      } else if (data.message === "Login fail") {
        printError("登录失败");
      } else if (data.message === "Username cannot be empty") {
        printError("用户名不能为空");
      } else if (data.message === "Password cannot be empty") {
        printError("密码不能为空");
      } else if (data.message === "Username or password error") {
        printError("用户名或密码错误");
      } else {
        printError("未知错误", "", data.message);
      }
    },
    error: function(XMLHttpRequest, textStatus) {
      printError("login: 无法连接服务器，错误代码", "", XMLHttpRequest.status);
    }
  });
}
