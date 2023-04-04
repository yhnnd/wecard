class Mock {
    constructor() {
        this.data = new Proxy({}, {
            "set": function (target, key, value) {
                console.log("set mock.data." + key, value);
                target[key] = value;
                return true;
            },
            "get": function (target, key) {
                const value = target[key];
                console.log("get mock.data." + key, value);
                return value;
            }
        });
    }
}
window.mock = new Mock();