function button_on(button) {
    $(button).find('.ios-button-circle').get(0).style.marginLeft = '28px';
    $(button).get(0).style.backgroundColor = 'limegreen';
    $(button).attr("data-value", "1");
}

function button_off(button) {
    $(button).find('.ios-button-circle').get(0).style.marginLeft = '0px';
    $(button).get(0).style.backgroundColor = 'lightgray';
    $(button).attr("data-value", "0");
}

function ios_button_init(scope) {
    if (!$(scope).length) {
        console.log("ios_button_init: cannot find scope");
    }
    $(scope).find('.ios-button').each(function (index, elem) {
        if ([1, "1", true, "true"].includes($(elem).attr("data-value"))) {
            button_on(elem);
        } else {
            button_off(elem);
        }
        $(elem).off("click").on("click", function () {
            if ($(elem).attr("data-value") === "1") {
                button_off(this);
                eval($(this).data("button-off"));
            } else {
                button_on(this);
                eval($(this).data("button-on"));
            }
        });
    });
}