function append_message_linkable(messageText) {
    if(!messageText) return undefined;
    let newText = messageText, url = "";
    // replace "url" with "<a href='url'>" + url + "</a>"
    const beginPattern = ["http://", "https://", "ftp://", "www.", "wap."];
    const endPattern = [".com", ".net", ".org", ".edu", ".gov", ".cn", ".us", ".jp", ".hk", ".tw", ".uk", ".fr", ".de", ".sg", ".it", ".tv", ".io", ".top"];
    let URL_FOUND = false;
    for (let itBegin = 0; itBegin < beginPattern.length; ++itBegin) {
        // locate begin position "www."
        let begin = newText.indexOf(beginPattern[itBegin]);
        if (begin >= 0) {
            for (it = 0; it < endPattern.length; ++it) {
                // locate end position ".com"
                let end = newText.indexOf(endPattern[it]);
                if (end > begin + beginPattern[itBegin].length) {
                    // locate real end position. eg. "www.example.com.cn/login"
                    while (end + endPattern[it].length < newText.length &&
                    [' ', '\n', '\'', '\"', '>', '<'].includes(newText[end + endPattern[it].length]) &&
                    newText[end + endPattern[it].length].charAt(0) < 255) {
                        ++end;
                    }
                    // embrace url with <a> tags
                    let Protocol = itBegin < 3 ? "" : "http://";
                    url = Protocol + newText.substring(begin, end + endPattern[it].length);
                    // 生成 a 链接
                    let a = "<a class='d-inline-block w-100 text-color-animated' target='_blank' href='" + url + "' style='word-break: break-all;'>" +
                        newText.substring(begin, end + endPattern[it].length) +
                        "</a>";
                    // set new message after embrace <a> link
                    newText = newText.substr(0, begin) + a + newText.substr(end + endPattern[it].length);
                    URL_FOUND = true;
                    break;
                }
            }
        }
        if (URL_FOUND == true) break;
    }
    // return newText;
    return (URL_FOUND == true) ? url : undefined;
}