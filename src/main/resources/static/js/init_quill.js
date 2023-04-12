function toggleQuillToolbarFont(selector) {
    const toolbar = $(selector).siblings(".ql-toolbar");
    toolbar.find(".ql-formats").eq(1).toggle();
}

function toggleQuillToolbarColor(selector) {
    const toolbar = $(selector).siblings(".ql-toolbar");
    toolbar.find(".ql-formats").eq(2).toggle();
}

function toggleQuillToolbarList(selector) {
    const toolbar = $(selector).siblings(".ql-toolbar");
    toolbar.find(".ql-formats").eq(3).toggle();
}

function toggleQuillToolbarCode(selector) {
    const toolbar = $(selector).siblings(".ql-toolbar");
    toolbar.find(".ql-formats").eq(4).toggle();
}

function initQuill(selector) {
    try {
        Quill;
    } catch (e) {
        return false;
    }
    // 当 quill 脚本未被加载的时候, 下面的代码会报错
    if (Quill !== undefined) {
        const quill = new Quill(selector, {
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline', "strike", "code"],
                    [{ "font": [] }, { "size": [] }, { "header": [1, 2, 3, false] }],
                    [{ 'color': [] }, { 'background': [] }],
                    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                    ['image', "blockquote", 'code-block']
                ]
            },
            placeholder: "卡片正文",
            theme: 'snow'  // or 'bubble'
        });

        // const imageButton = $("#editor-container").siblings(".ql-toolbar").find(".ql-image");
        // imageButton.replaceWith(imageButton.prop("outerHTML"));
        toggleQuillToolbarFont("#editor-container");
        toggleQuillToolbarColor("#editor-container");
        toggleQuillToolbarList("#editor-container");
        toggleQuillToolbarCode("#editor-container");
        return true;
    }
    return false;
}

setTimeout(() => {
    if (initQuill("#editor-container") !== true) {
        $("[href=\"#create-quill-card\"]").parent().hide();
    }
}, 1000);
