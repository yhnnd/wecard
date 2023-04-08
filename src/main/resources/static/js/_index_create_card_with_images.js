function createImageCard($scope, card) {
    // 删掉了变量的初始化, 以及上传前的验证逻辑
    // 因为这些已在 createCard 函数中写了,
    // 没必要在这里再写一遍
    
    let formData = new FormData();
    formData.append("title", card.title);
    formData.append("text", card.text);
    for (let i = 0; i < card.topics.length; ++i) {
        formData.append("topics", card.topics[i]);
    }
    // 获取要上传的图片文件
    let cardImages = $(".select-image");
    for (let i = 0; i < max.card.images.length && i < cardImages.length; ++i) {
        let file = cardImages[i].files[0];
        if (file) {
            formData.append("files", file);
            formData.append("remarks", "card-img-" + i);
        }
    }

    if (formData.has("files")) {
        // 暂时禁用上传卡片按钮
        let createCardBtn = $("[onclick=\"createCard()\"]");
        let createCardBtnText = null;
        if (createCardBtn) {
            createCardBtnText = createCardBtn.text();
            createCardBtn.attr("disabled", true).text("卡片上传中...");
        }
        // 开始上传卡片
        $.ajax({
            url: $scope.httpRoot + apis.create.card.image,
            type: 'POST',
            data: formData,
            crossDomain: true,
            xhrFields: {
                withCredentials: true
            },
            processData: false,
            contentType: false,
            success: function (resultData) {
                // 恢复上传卡片按钮
                if (createCardBtn && createCardBtnText) {
                    createCardBtn.removeAttr("disabled").text(createCardBtnText);
                }

                let result = {
                    "data": resultData
                };
                switch (result.data.message) {
                    case "Please login":
                        bsAlert("[ERROR] createImageCard(): 请先登录");
                        break;
                    case "card title too long":
                        bsAlert("[ERROR] createImageCard(): 文本标题过长");
                        break;
                    case "card text too long":
                        bsAlert("[ERROR] createImageCard(): 文本内容过长");
                        break;
                    case "card create failed":
                        bsAlert("[ERROR] createImageCard(): 卡片上传失败");
                        saveCardAsDraft($scope.user, card, "image");
                        break;
                    case "card create success":
                        if (result.data.card) {
                            $scope.card_to_create.type = "";
                            $scope.card_to_create.title = "";
                            $scope.card_to_create.text = "";
                            $scope.card_to_create.topics = [];
                            $scope.$apply();
                            $scope.alert("createImageCard()", "上传卡片成功", "alert-success");
                            selectImage_cleanSelected();
                            v2_7.empty();
                        } else {
                            bsAlert("[ERROR] createImageCard(): 卡片上传成功了，但是服务器没有返回（添加到数据库中的）卡片对象");
                        }
                        break;
                    default:
                        bsAlert("[ERROR] createImageCard(): 未知错误：" + result.data.message);
                        saveCardAsDraft($scope.user, card, "image");
                        break;
                }
            },
            error: function (result) {
                bsAlert("[ERROR] createImageCard(): 与服务器连接失败 错误代码: " + result.status);
                saveCardAsDraft($scope.user, card, "image");
            }
        });

    } else {
        bsAlert("[ERROR] createImageCard(): 未找到卡片图片");
    }
}


function selectImage_fileSelected (input) {
    var files = input.files;
    var img = $(input).prev("img.card-image-preview").first();
    if (files && files.length && files[0]) {
        img.attr("onload", "selectImage_markObjectURL(this.src)");
        img.attr("src", window.URL.createObjectURL(files[0]));
        img.show().prev(".no-image-text").hide();
    } else {
        img.hide().prev(".no-image-text").show();
    }
    refreshSelectVisibility();
}

function selectImage_markObjectURL (src) {
    if (src) {
        var objectURLs = window.sessionStorage.getItem("object_urls");
        if (objectURLs) {
            objectURLs = JSON.parse(objectURLs);
            objectURLs.push(src);
        } else {
            objectURLs = [];
        }
        // 数组如果不用 JSON.stringify 直接写入 sessionStorage, 会没有 "[" 和 "]", 导致变量无法还原成 JSON 对象
        window.sessionStorage.setItem("object_urls", JSON.stringify(objectURLs));
    }
}

function selectImage_deleteObjectURL () {
    var objectURLs = window.sessionStorage.getItem("object_urls");
    if (objectURLs) {
        objectURLs = JSON.parse(objectURLs);
        for (var i = 0; i < objectURLs.length; i++) {
            var src = objectURLs[i];
            if (src) {
                window.URL.revokeObjectURL(src);
                delete objectURLs[i];
            }
        }
    }
    objectURLs = [];
    // 数组如果不用 JSON.stringify 直接写入 sessionStorage, 会没有 "[" 和 "]", 导致变量无法还原成 JSON 对象
    window.sessionStorage.setItem("object_urls", JSON.stringify(objectURLs));
}

var refreshSelectVisibilityHasInit = false;
var refreshSelectVisibilityCalledTimes = 0;

function refreshSelectVisibility(msg) {
    refreshSelectVisibilityCalledTimes ++;
    console.log("refreshSelectVisibilityCalledTimes = " + refreshSelectVisibilityCalledTimes);
    // ng-repeat 循环会产生 6 个 img, 然而在 Angular 被加载之前, 就有 1 个 img 存在了
    // 所以需要 init 7 次
    // 为了减少开销, 我们只在第 7 次运行 init
    if (msg === "init") {
        if (refreshSelectVisibilityCalledTimes > 6) {
            refreshSelectVisibilityHasInit = true;
            console.log("refreshSelectVisibility: do init");
        } else {
            console.log("refreshSelectVisibility: called times < 7, refuse to init.");
            return false;
        }
    }

    let selectImageWrappers = $(".card-image-preview-wrapper");
    if (selectImageWrappers) {
        let selectImages = selectImageWrappers.find("input");
        if (selectImages) {
            if (msg === "clean") {
                for (let i = 0; i < selectImages.length; ++ i) {
                    selectImages.val("");
                    selectImage_fileSelected(selectImages[i]);
                }
                $("#card_to_create_files_length").text(0);
            }
            selectImageWrappers.first().show();
            let fileLength = 0;
            if (selectImages[0].files && selectImages[0].files.length) {
                fileLength ++;
            }
            for (let index = 1; index < selectImages.length; ++index) {
                let show = false;
                let prevSelect = selectImages[index - 1];
                let currSelect = selectImages[index];
                if (prevSelect && currSelect) {
                    let prevValue = prevSelect.files;
                    if (prevValue) {
                        if (prevValue.length) {
                            show = true;// 如果上一个输入框被赋值了, 就显示当前输入框
                        }
                    }
                    let currValue = currSelect.files;
                    if (currValue) {
                        if (currValue.length) {
                            show = true;// 如果上一个输入框被清空了, 但是当前输入框被赋值了, 仍然显示当前输入框
                            fileLength ++;
                        }
                    }
                }
                if (show) {
                    selectImageWrappers.eq(index).show();
                } else {
                    selectImageWrappers.eq(index).hide();
                }
            }
            $("#card_to_create_files_length").text(fileLength);
        }
    }
}


function selectImage_cleanSelected () {
    var first = true;
    $(".card-image-preview-wrapper").each(function () {
        if (first) {
            $(this).show();
            first = false;
        } else {
            $(this).hide();
        }
    });
    refreshSelectVisibility("clean");
    selectImage_deleteObjectURL();
}
