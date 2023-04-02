const forgotPassword = {
    confirmAccount: function (account) {
        console.log("confirm account: account is " + account);// debug
        // $.ajax({
        //     type: "get",
        //     url: "http://" + getWebRoot() + apis.forgotPassword.confirmAccount,
        //     data: {
        //         "account": account
        //     },
        //     crossDomain: true,
        //     xhrFields: {
        //         withCredentials: true
        //     },
        //     success: function (data) {
        //         switch (data.message) {
        //             case "Confirm success":
                        const data = {
                            coverUpPhone: "13512484392"
                        };
                        const model = document.getElementById('account');
                        const $scope = angular.element(model).scope();
                        $scope.step = 2;
                        $scope.phoneNumberMasked = data.coverUpPhone;
                        $scope.$apply();
        //                 break;
        //             case "Confirm fail":
        //                 bsAlert("确认账户失败");
        //                 break;
        //             case "The account does not exist":
        //                 $(input).val("").attr("value", "").attr("placeholder", "用户不存在");
        //                 break;
        //             default:
        //                 bsAlert("[ERROR] forgot password > confirm account: 未知错误 " + data.message);
        //                 break;
        //         }
        //     },
        //     error: function (data) {
        //         bsAlert("[ERROR] forgot password > confirm account: 无法连接服务器. 错误代码 " + data.status);
        //     }
        // });
    },
    sendVerCode: function () {
        console.log("send verCode: request sent");// debug
        // $.ajax({
        //     type: "get",
        //     url: "http://" + getWebRoot() + apis.forgotPassword.sendVercode,
        //     data: {
        //     },
        //     crossDomain: true,
        //     xhrFields: {
        //         withCredentials: true
        //     },
        //     success: function (data) {
        //         console.log("send verCode: message is '" + data.message + "'");// debug
        //         switch (data.message) {
        //             case "Send success":
                        var model = document.getElementById('account');
                        let $scope = angular.element(model).scope();
                        $scope.verCode.sendSuccess();
                        $scope.$apply();
        //                 break;
        //             case "Send fail":
        //                 bsAlert("验证码发送失败");
        //                 break;
        //             default:
        //                 bsAlert("[ERROR] forgot password > send verCode: 未知错误 " + data.message);
        //                 break;
        //         }
        //     },
        //     error: function (data) {
        //         bsAlert("[ERROR] forgot password > send verCode: 无法连接服务器. 错误代码 " + data.status);
        //     }
        // });
    },
    verifyVercode: function () {
        console.log("verify verCode: verCode = " + $("#verCode").val());// debug
        // $.ajax({
        //     type: "get",
        //     url: "http://" + getWebRoot() + apis.forgotPassword.verifyVercode,
        //     data: {
        //         "verCode": $("#verCode").val()
        //     },
        //     crossDomain: true,
        //     xhrFields: {
        //         withCredentials: true
        //     },
        //     success: function (data) {
                var model = document.getElementById('account');
                let $scope = angular.element(model).scope();
        //         if (data.message === "Verify success") {
                    $scope.step = 3;
        //         } else {
        //             let message = "账户认证失败";
        //             switch (data.message) {
        //                 case "Verification code cannot be empty":
        //                     message = "验证码不能为空";
        //                     break;
        //                 case "Verification code has not sent":
        //                     message = "验证码尚未发送";
        //                     break;
        //                 case "Verification code expired":
        //                     message = "验证码已过期";
        //                     break;
        //                 case "Verification code error":
        //                     message = "验证码错误";
        //                     break;
        //                 default:
        //                     bsAlert("[ERROR] forgot password > verify verCode: 未知错误 " + data.message);
        //                     break;
        //             }
        //             $scope.verCode.value = "";
        //             $scope.verCode.placeholder = message;
        //         }
                $scope.$apply();
        //     },
        //     error: function (data) {
        //         bsAlert("[ERROR] forgot password > verify verCode: 无法连接服务器. 错误代码 " + data.status);
        //     }
        // });
    },
    setPassword: function () {
        console.log("set password: password = " + $("#password").val());// debug
        // $.ajax({
        //     type: "get",
        //     url: "http://" + getWebRoot() + apis.forgotPassword.setPassword,
        //     data: {
        //         "password": $("#password").val()
        //     },
        //     crossDomain: true,
        //     xhrFields: {
        //         withCredentials: true
        //     },
        //     success: function (data) {
        //         switch (data.message) {
        //             case "Set success":
                        var model = document.getElementById('account');
                        let $scope = angular.element(model).scope();
                        $scope.step = 4;
                        $scope.$apply();
                        initGotoLoginCountDown();
        //                 break;
        //             case "Set fail":
        //                 bsAlert("密码重置失败");
        //                 break;
        //             case "Password is too short":
        //                 bsAlert("密码重置失败");
        //                 break;
        //             case "Password contains special char":
        //                 bsAlert("密码重置失败");
        //                 break;
        //             case "Password cannot be empty":
        //                 bsAlert("密码重置失败");
        //                 break;
        //             default:
        //                 bsAlert("[ERROR] forgot password > set password: 未知错误 " + data.message);
        //                 break;
        //         }
        //     },
        //     error: function (data) {
        //         bsAlert("[ERROR] forgot password > set password: 无法连接服务器. 错误代码 " + data.status);
        //     }
        // });
    }
};