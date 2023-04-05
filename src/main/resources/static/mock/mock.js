class Mock {
    constructor() {
        this.callbacks = {};
        this.origin = "https://raw.githubusercontent.com/yhnnd/wecard/master/src/main/resources/static/mock/";
        this.data = new Proxy({}, {
            "set": function (target, key, value) {
                target[key] = value;
                console.log("set mock.data." + key, value);
                return true;
            },
            "get": function (target, key) {
                const value = target[key];
                console.log("get mock.data." + key, value);
                return value;
            }
        });
    }
    /* Load Mock Data from Github */
    loadData (key, filename) {
        this.key = key;
        const req = new XMLHttpRequest();
        req.addEventListener("load", () => {
            const value = JSON.parse(req.responseText);
            this.data[key] = value;
            const callback = this.callbacks[key];
            if (callback && typeof callback === "function") {
                callback();
            } else {
                console.log("mock.loadData: no callback for " + key);
            }
        });
        req.open("GET", this.origin + filename);
        req.send();
        return this;
    }
    then(func) {
        if (this.key) {
            this.callbacks[this.key] = func;
        } else {
            console.error("mock: use then without loadData");
        }
    }
}
