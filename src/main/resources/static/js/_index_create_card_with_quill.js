function createQuillCard($scope, card) {

	let formData = new FormData();
	// 装载卡片标题
	formData.append("title", card.title);
	// 装载卡片正文
	let quillEditor = $("#editor-container > .ql-editor");
	quillEditor.find("span.ql-cursor").remove();
	formData.append("text", quillEditor.html());
	// 装载卡片话题
	for (let i = 0; i < card.topics.length; ++i) {
		formData.append("topics", card.topics[i]);
	}

	$.ajax({
		url: "http://" + window.getWebRoot() + apis.create.card.quill,
		type: 'POST',
		data: formData,
		crossDomain: true,
		xhrFields: {
				withCredentials: true
		},
		processData: false,
		contentType: false,
		success: function (resultData) {
			let result = {
				"data": resultData
			};
			switch (result.data.message) {
				case "Please login":
					createCardAlert("[ERROR] createQuillCard(): 请先登录");
					break;
				case "card title too long":
					createCardAlert("[ERROR] createQuillCard(): 文本标题过长");
					break;
				case "card text too long":
					createCardAlert("[ERROR] createQuillCard(): 文本内容过长");
					break;
				case "card create failed":
					createCardAlert("[ERROR] createQuillCard(): 卡片上传失败");
					break;
				case "card create success":
					if (result.data.card) {
						$scope.card_to_create.type = "";
						$scope.card_to_create.title = "";
						$scope.card_to_create.topics = [];
						$scope.$apply();
						$scope.alert("createQuillCard()", "上传卡片成功", "alert-success");
					} else {
						createCardAlert("[ERROR] createQuillCard(): 卡片上传成功了，但是服务器没有返回（添加到数据库中的）卡片对象");
					}
					break;
				default:
					createCardAlert("[ERROR] createQuillCard(): 未知错误：" + result.data.message);
					break;
			}
		},
		error: function() {
			bsAlert("[ERROR] createQuillCard(): 与服务器连接失败 错误代码: " + result.status);
		}
	});
}