function notFirefox() {
    return navigator.userAgent.toLowerCase().indexOf("firefox") >= 0;
}


function getRandomString() {
    return ("" + Math.random() * 10).split(".").join("");
}


function removeSlowly(item) {
    $(item).fadeOut(300);
    setTimeout(() => {
        $(item).remove();
    }, 300);
}


function hidePopover(item) {
    let id = $(item).attr("id");
    let toggle = $("[aria-describedby='" + id + "']");
    if (toggle.length > 0) {// 当按钮存在时，正常删除弹窗
        toggle.popover("hide");
    } else {// 当按钮不存在时，强制删除弹窗
        removeSlowly(item);
    }
}


function removeAllPopovers() {
    $(".popover").remove();
}


function removeAllTooltips() {
    $(".tooltip").remove();
}
