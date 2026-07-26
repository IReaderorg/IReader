package ireader.domain.js.loader

/**
 * CommonJS require() shim injected into live JS engines (J2V8/GraalVM) before
 * plugin code is evaluated. Most real LNReader plugins call require() at the
 * top level ("@libs/fetch", "@libs/novelStatus", "dayjs", "cheerio", ...);
 * without this shim eval throws and the source silently becomes a dead
 * "pending" catalog.
 *
 * ponytail: 'cheerio'/'htmlparser2' are load-safe stubs that return empty
 * results — plugins load and list, but cheerio-based parsing returns empty
 * until a real pure-JS HTML parser ships. Upgrade path: bundle a minimal
 * selector engine and swap the stub.
 */
object RequireShimJs {
    val CODE: String = """
        (function() {
            var modules = {};
            globalThis.__modules = modules;

            modules['@libs/filterInputs'] = {
                FilterTypes: {
                    Picker: 'Picker',
                    Text: 'Text',
                    TextInput: 'Text',
                    Switch: 'Switch',
                    Checkbox: 'Checkbox',
                    CheckboxGroup: 'Checkbox',
                    ExcludableCheckbox: 'ExcludableCheckbox',
                    ExcludableCheckboxGroup: 'XCheckbox',
                    TriState: 'TriState',
                    Sort: 'Sort',
                    Title: 'Title'
                }
            };

            modules['@libs/defaultCover'] = {
                defaultCover: 'https://via.placeholder.com/300x400?text=No+Cover'
            };

            modules['@libs/fetch'] = {
                fetchApi: function(url, options) { return fetch(url, options); },
                fetchText: function(url, options) {
                    return fetch(url, options).then(function(r) { return r.text(); });
                },
                fetchFile: function(url, options) {
                    return fetch(url, options).then(function(r) { return r.text(); });
                }
            };

            modules['@libs/novelStatus'] = {
                NovelStatus: {
                    Unknown: 'Unknown',
                    Ongoing: 'Ongoing',
                    Completed: 'Completed',
                    Licensed: 'Licensed',
                    PublishingFinished: 'Publishing Finished',
                    Cancelled: 'Cancelled',
                    OnHiatus: 'On Hiatus'
                }
            };

            // In-memory storage (per engine instance, not persisted)
            (function() {
                var mem = {};
                var kv = {
                    set: function(k, v) { mem[k] = v; },
                    get: function(k) { return mem.hasOwnProperty(k) ? mem[k] : null; },
                    'delete': function(k) { delete mem[k]; },
                    clearAll: function() { mem = {}; },
                    getAllKeys: function() { return Object.keys(mem); }
                };
                var webStorage = {
                    setItem: function(k, v) { mem[k] = String(v); },
                    getItem: function(k) { return mem.hasOwnProperty(k) ? mem[k] : null; },
                    removeItem: function(k) { delete mem[k]; },
                    clear: function() { mem = {}; }
                };
                modules['@libs/storage'] = {
                    storage: kv,
                    localStorage: webStorage,
                    sessionStorage: webStorage
                };
            })();

            modules['@libs/isAbsoluteUrl'] = {
                isUrlAbsolute: function(url) {
                    return !!url && (url.indexOf('http://') === 0 || url.indexOf('https://') === 0 || url.indexOf('//') === 0);
                }
            };

            // Minimal dayjs: enough for the parse/valueOf/unix/format calls
            // plugins typically use for chapter release times.
            (function() {
                function D(input) {
                    if (!(this instanceof D)) return new D(input);
                    this.d = input === undefined ? new Date() : new Date(input);
                }
                D.prototype.valueOf = function() { return this.d.getTime(); };
                D.prototype.unix = function() { return Math.floor(this.d.getTime() / 1000); };
                D.prototype.toISOString = function() { return this.d.toISOString(); };
                D.prototype.toDate = function() { return new Date(this.d.getTime()); };
                D.prototype.isValid = function() { return !isNaN(this.d.getTime()); };
                D.prototype.format = function() { return this.d.toISOString(); };
                D.prototype.add = function(n, unit) {
                    var ms = { second: 1e3, minute: 6e4, hour: 36e5, day: 864e5, week: 6048e5 }[unit] || 0;
                    var out = new D(this.d.getTime() + n * ms);
                    if (unit === 'month') { out = new D(this.d.getTime()); out.d.setMonth(out.d.getMonth() + n); }
                    if (unit === 'year') { out = new D(this.d.getTime()); out.d.setFullYear(out.d.getFullYear() + n); }
                    return out;
                };
                D.prototype.subtract = function(n, unit) { return this.add(-n, unit); };
                var dayjs = function(input) { return new D(input); };
                dayjs.unix = function(s) { return new D(s * 1000); };
                dayjs.extend = function() { return dayjs; };
                modules['dayjs'] = dayjs;
            })();

            modules['urlencode'] = {
                encode: function(s) { return encodeURIComponent(s); },
                decode: function(s) { return decodeURIComponent(s); }
            };
            modules['qs'] = {
                stringify: function(obj) {
                    var parts = [];
                    for (var k in obj) {
                        if (obj.hasOwnProperty(k) && obj[k] !== undefined && obj[k] !== null) {
                            parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(obj[k]));
                        }
                    }
                    return parts.join('&');
                },
                parse: function(str) {
                    var out = {};
                    String(str || '').replace(/^\?/, '').split('&').forEach(function(pair) {
                        if (!pair) return;
                        var i = pair.indexOf('=');
                        var k = i < 0 ? pair : pair.slice(0, i);
                        var v = i < 0 ? '' : pair.slice(i + 1);
                        out[decodeURIComponent(k)] = decodeURIComponent(v.replace(/\+/g, ' '));
                    });
                    return out;
                }
            };

            // Load-safe cheerio stub: plugins load + list, cheerio-based
            // parsing returns empty results. See RequireShimJs kdoc.
            (function() {
                function emptySel() {
                    var fn = function() { return emptySel(); };
                    fn.length = 0;
                    fn.text = function() { return ''; };
                    fn.html = function() { return null; };
                    fn.attr = function() { return undefined; };
                    fn.find = function() { return emptySel(); };
                    fn.first = function() { return emptySel(); };
                    fn.last = function() { return emptySel(); };
                    fn.eq = function() { return emptySel(); };
                    fn.next = function() { return emptySel(); };
                    fn.prev = function() { return emptySel(); };
                    fn.parent = function() { return emptySel(); };
                    fn.children = function() { return emptySel(); };
                    fn.each = function() { return fn; };
                    fn.map = function() { return { get: function() { return []; }, toArray: function() { return []; } }; };
                    fn.toArray = function() { return []; };
                    fn.get = function() { return undefined; };
                    fn.remove = function() { return fn; };
                    fn.hasClass = function() { return false; };
                    return fn;
                }
                function load(html) {
                    console.error('[require shim] cheerio.load() called but no HTML parser is available in this engine; selectors will return empty results');
                    var sel = function() { return emptySel(); };
                    sel.html = function() { return String(html || ''); };
                    sel.text = function() { return ''; };
                    return sel;
                }
                modules['cheerio'] = { load: load, default: { load: load } };
                modules['htmlparser2'] = {
                    Parser: function(handlers) {
                        console.error('[require shim] htmlparser2 stub used; parsing produces no events');
                        this.write = function() {};
                        this.end = function() {};
                        this.parseComplete = function() {};
                    }
                };
            })();

            globalThis.require = function(name) {
                if (modules.hasOwnProperty(name)) return modules[name];
                console.error('[require shim] Unknown module "' + name + '" — returning empty object. Available: ' + Object.keys(modules).join(', '));
                return {};
            };
        })();
    """.trimIndent()
}
