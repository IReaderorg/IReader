package ireader.data.catalog.js

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Scripts that let JavaScriptCore run a published source bundle.
 * The bundle calls fetch and returns Promises. The host queues both,
 * and the iOS loader fills the responses in from Ktor.
 */
object JsRuntimeHost {
    private val json = Json { ignoreUnknownKeys = true }

    fun prelude(): String = """
        globalThis.__fetchQueue = [];
        globalThis.__fetchSeq = 0;
        globalThis.__pendingFetches = {};
        globalThis.__timerQueue = [];
        globalThis.__timerSeq = 0;
        globalThis.__ireaderResult = undefined;

        globalThis.setTimeout = function(fn, delay) {
            var args = Array.prototype.slice.call(arguments, 2);
            var id = ++globalThis.__timerSeq;
            globalThis.__timerQueue.push({
                id: id,
                due: Date.now() + (Number(delay) || 0),
                fn: function() { fn.apply(null, args); }
            });
            return id;
        };
        globalThis.clearTimeout = function(id) {
            globalThis.__timerQueue = globalThis.__timerQueue.filter(function(timer) {
                return timer.id !== id;
            });
        };
        if (typeof globalThis.queueMicrotask !== 'function') {
            globalThis.queueMicrotask = function(fn) { globalThis.setTimeout(fn, 0); };
        }
        globalThis.__drainTimers = function() {
            var now = Date.now();
            var ready = [];
            var waiting = [];
            for (var i = 0; i < globalThis.__timerQueue.length; i++) {
                var timer = globalThis.__timerQueue[i];
                if (timer.due <= now) ready.push(timer);
                else waiting.push(timer);
            }
            globalThis.__timerQueue = waiting;
            for (var j = 0; j < ready.length; j++) {
                try { ready[j].fn(); } catch (e) {}
            }
        };

        function ireaderHeaders(raw) {
            var headers = {};
            if (!raw) return headers;
            if (typeof raw.forEach === 'function') {
                raw.forEach(function(value, key) { headers[String(key)] = String(value); });
                return headers;
            }
            for (var key in raw) {
                if (Object.prototype.hasOwnProperty.call(raw, key)) headers[key] = String(raw[key]);
            }
            return headers;
        }

        globalThis.fetch = function(url, init) {
            return new Promise(function(resolve, reject) {
                var id = ++globalThis.__fetchSeq;
                globalThis.__pendingFetches[id] = { resolve: resolve, reject: reject };
                globalThis.__fetchQueue.push({
                    id: id,
                    url: String(url || ''),
                    method: (init && init.method) || 'GET',
                    headers: ireaderHeaders(init && init.headers),
                    body: (init && init.body != null) ? String(init.body) : ''
                });
            });
        };

        globalThis.__deliverFetch = function(id, status, url, body, headers) {
            var pending = globalThis.__pendingFetches[id];
            if (!pending) return;
            delete globalThis.__pendingFetches[id];
            if (!status) {
                pending.reject(new Error('fetch failed'));
                return;
            }
            var headerMap = headers || {};
            var response = {
                ok: status >= 200 && status < 300,
                status: status,
                statusText: String(status),
                url: url,
                headers: {
                    get: function(name) {
                        var wanted = String(name).toLowerCase();
                        for (var key in headerMap) {
                            if (Object.prototype.hasOwnProperty.call(headerMap, key) &&
                                String(key).toLowerCase() === wanted) {
                                return String(headerMap[key]);
                            }
                        }
                        return null;
                    },
                    forEach: function(callback) {
                        for (var key in headerMap) {
                            if (Object.prototype.hasOwnProperty.call(headerMap, key)) {
                                callback(String(headerMap[key]), key);
                            }
                        }
                    }
                },
                text: function() { return Promise.resolve(body || ''); },
                json: function() { return Promise.resolve(JSON.parse(body || '{}')); },
                arrayBuffer: function() { return Promise.resolve(new ArrayBuffer(0)); }
            };
            pending.resolve(response);
        };
    """.trimIndent()

    fun invoke(expression: String, fallback: String): String {
        val fallbackJs = jsString(fallback)
        return """
            (function() {
                var fallback = $fallbackJs;
                function store(value) {
                    if (typeof value === 'string') globalThis.__ireaderResult = value;
                    else {
                        try { globalThis.__ireaderResult = JSON.stringify(value); }
                        catch (e) { globalThis.__ireaderResult = fallback; }
                    }
                }
                try {
                    var pending = $expression;
                    if (pending && typeof pending.then === 'function') {
                        pending.then(store, function() { store(fallback); });
                    } else {
                        store(pending == null ? fallback : pending);
                    }
                } catch (e) {
                    store(fallback);
                }
            })();
        """.trimIndent()
    }

    fun searchCall(sourceId: String, registryId: String, query: String, page: Int): String {
        return invoke(sourceCall(sourceId, registryId, "search", "getMangaList", jsString(query), page), "[]")
    }

    fun detailsCall(sourceId: String, registryId: String, bookJson: String): String {
        return invoke(sourceCall(sourceId, registryId, "getBookDetails", "getMangaDetails", jsString(bookJson), null), "{}")
    }

    fun chaptersCall(sourceId: String, registryId: String, bookJson: String): String {
        return invoke(sourceCall(sourceId, registryId, "getChapters", "getChapterList", jsString(bookJson), null), "[]")
    }

    fun contentCall(sourceId: String, registryId: String, chapterJson: String): String {
        return invoke(sourceCall(sourceId, registryId, "getContent", "getPageList", jsString(chapterJson), null), "[]")
    }

    fun resetResult(): String = "globalThis.__ireaderResult = undefined;"

    fun readResult(): String = "globalThis.__ireaderResult"

    fun drainTimers(): String = "if (typeof globalThis.__drainTimers === 'function') globalThis.__drainTimers();"

    fun readFetchQueue(): String = "JSON.stringify(globalThis.__fetchQueue || [])"

    fun clearFetchQueue(): String = "globalThis.__fetchQueue = [];"

    fun deliverFetch(id: Int, status: Int, url: String, body: String, headers: Map<String, String>): String {
        val headersJson = json.encodeToString(headers)
        return "globalThis.__deliverFetch($id, $status, ${jsString(url)}, ${jsString(body)}, $headersJson);"
    }

    fun parseFetchQueue(raw: String): List<JsFetchRequest> {
        return try {
            json.decodeFromString<List<JsFetchRequest>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun registryId(pkg: String, name: String): String {
        val languages = setOf("en", "es", "ar", "fr", "id", "pt", "ru", "zh", "ja", "ko")
        val segment = pkg.split('.').firstOrNull { part ->
            part.length > 2 && part.lowercase() !in languages && part.lowercase() != "ireader"
        }
        if (segment != null) return segment.lowercase()
        return name.lowercase().filter { it.isLetterOrDigit() }
    }

    fun registryShim(initFunction: String): String {
        val initName = jsString(initFunction)
        return """
            (function() {
                var root = (typeof IReaderSources !== 'undefined') ? IReaderSources : null;
                if (root && root.SourceRegistry) {
                    globalThis.SourceRegistry = root.SourceRegistry;
                    if (typeof root.getHttpClient === 'function') globalThis.getHttpClient = root.getHttpClient;
                    if (typeof root.parseHtml === 'function') globalThis.parseHtml = root.parseHtml;
                    if (root.ireader) globalThis.ireader = root.ireader;
                }
                var registry = globalThis.SourceRegistry;
                if (registry && !registry.__ireaderWrapped && typeof registry.register === 'function') {
                    var real = registry.register.bind(registry);
                    registry.register = function(id, name, lang, factory) {
                        if (typeof name === 'function' && typeof factory === 'undefined') {
                            return real(id, id, 'en', name);
                        }
                        return real(id, name, lang, factory);
                    };
                    registry.__ireaderWrapped = true;
                }
                var initName = $initName;
                var fn = globalThis[initName];
                if (typeof fn !== 'function' && root) fn = findNamed(root, initName, 0, []);
                if (typeof fn === 'function') fn();

                function findNamed(obj, name, depth, seen) {
                    if (!obj || typeof obj !== 'object' || depth > 5 || seen.indexOf(obj) !== -1) return null;
                    seen.push(obj);
                    if (typeof obj[name] === 'function') return obj[name].bind(obj);
                    for (var key in obj) {
                        if (!Object.prototype.hasOwnProperty.call(obj, key)) continue;
                        var child = obj[key];
                        if (child && typeof child === 'object') {
                            var found = findNamed(child, name, depth + 1, seen);
                            if (typeof found === 'function') return found;
                        }
                    }
                    return null;
                }
            })();
        """.trimIndent()
    }

    private fun sourceCall(
        sourceId: String,
        registryId: String,
        bridgeMethod: String,
        sourceMethod: String,
        payloadJs: String,
        page: Int?,
    ): String {
        val pageLiteral = page?.toString() ?: "null"
        return """
            (function() {
                var sourceId = ${jsString(sourceId)};
                var registryId = ${jsString(registryId)};
                var payload = $payloadJs;
                var page = $pageLiteral;
                if (typeof SourceBridge !== 'undefined' && typeof SourceBridge.$bridgeMethod === 'function') {
                    return page == null
                        ? SourceBridge.$bridgeMethod(sourceId, payload)
                        : SourceBridge.$bridgeMethod(sourceId, payload, page);
                }
                var registry = globalThis.SourceRegistry;
                if (!registry || typeof registry.getSource !== 'function') return null;
                var source = registry.getSource(registryId);
                if (!source) return null;
                if (page != null) {
                    if (!payload && typeof source.getMangaList === 'function') return source.getMangaList([], page);
                    if (typeof source.search === 'function') return source.search(payload, page);
                    if (typeof source.getMangaList === 'function') return source.getMangaList([], page);
                    return null;
                }
                if (typeof source.$sourceMethod !== 'function') return null;
                var parsed = payload;
                try { parsed = JSON.parse(payload); } catch (e) {}
                return source.$sourceMethod(parsed, []);
            })()
        """.trimIndent()
    }

    private fun jsString(value: String): String = json.encodeToString(value)
}

@Serializable
data class JsFetchRequest(
    val id: Int,
    val url: String,
    val method: String = "GET",
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
)
