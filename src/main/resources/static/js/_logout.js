function gotoPage(page, targetPage) {
    websocket && websocket.close();
    if (page === "login.html") {
        if (targetPage) {
            window.location.href = "./" + page + "?from=" + window.location.pathname + "&target=" + targetPage;
        } else {
            // if no target page given, targetPage is same as fromPage
            window.location.href = "./" + page + "?from=" + window.location.pathname + "&target=.." + window.location.pathname;
        }
    } else {
        window.location.href = "./" + page;
    }
}


function gotoLogin(targetPage) {
    gotoPage("login.html", targetPage);
}


// page = login.html
// targetPage = index.html / login.html / signup.html

function logout(page, targetPage) {
    $.ajax({
        type: "get",
        url: "http://" + getWebRoot() + "/logout",
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function () {
            gotoPage(page, targetPage);
        },
        error: function () {
            gotoPage(page, targetPage);
        }
    });
}
