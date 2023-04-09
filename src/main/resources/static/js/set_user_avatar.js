// 上传用户头像文件 apis.user.set.avatar, 'avatar'
// 上传用户主页背景图片 apis.user.set.styleImg, 'styleImg'
function uploadFile(apiName, parameterName, fileInput) {
    let loadingId = startLoading(max.loading.delay.time);
    var file = $(fileInput)[0].files[0];
    var formData = new FormData();
    formData.append(parameterName, file);
    $.ajax({
        url: window.getHttpRoot() + apiName,
        type: 'POST',
        data: formData,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        processData: false,
        contentType: false,
        success: function (result) {
            stopLoading(loadingId);
            switch (result.message) {
                case "Please login":
                    gotoLogin("index.html");
                    break;
                case "File cannot be empty":
                    bsAlert("[ERROR] uploadFile(" + parameterName + "): 文件不能为空");
                    break;
                case "Filename error":
                    bsAlert("[ERROR] uploadFile(" + parameterName + "): 文件名不能为空");
                    break;
                case "Format error":
                    bsAlert("[ERROR] uploadFile(" + parameterName + "): 文件格式错误");
                    break;
                case "Set fail":
                    bsAlert("[ERROR] uploadFile(" + parameterName + "): 上传文件失败");
                    break;
                case "Set success":
                    if (result[parameterName + "Url"]) {
                        const model = document.querySelector("[ng-controller='controller']");
                        let $scope = angular.element(model).scope();
                        $scope.userData.user[parameterName + "Url"] = result[parameterName + "Url"];
                        $scope.$apply();
                        bsAlert("上传文件成功");
                    } else {
                        bsAlert("上传文件成功, 然而服务器并没有返回文件的 URL");
                    }
                    break;
                default:
                    bsAlert("[ERROR] uploadFile(" + parameterName + "): 未知错误: " + result.message);
            }
        },
        error: function (result) {
            stopLoading(loadingId);
            bsAlert("[ERROR] uploadFile(" + parameterName + "): 无法连接服务器，错误代码 " + result.status);
        }
    });
}