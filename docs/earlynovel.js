// Bundled plugin with all dependencies included
// Built for LNReader Android (J2V8 - V8 JavaScript Engine)
// Supports: ES6+, async/await, Promises, arrow functions

"use strict";
var LNReaderPlugin = (() => {
  var __create = Object.create;
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getProtoOf = Object.getPrototypeOf;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __name = (target, value) => __defProp(target, "name", { value, configurable: true });
  var __esm = (fn, res) => function __init() {
    return fn && (res = (0, fn[__getOwnPropNames(fn)[0]])(fn = 0)), res;
  };
  var __commonJS = (cb, mod2) => function __require() {
    return mod2 || (0, cb[__getOwnPropNames(cb)[0]])((mod2 = { exports: {} }).exports, mod2), mod2.exports;
  };
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toESM = (mod2, isNodeMode, target) => (target = mod2 != null ? __create(__getProtoOf(mod2)) : {}, __copyProps(
    // If the importer is in node compatibility mode or this is not an ESM
    // file that has been converted to a CommonJS file using a Babel-
    // compatible transform (i.e. "__esModule" has not been set), then set
    // "default" to the CommonJS "module.exports" for node compatibility.
    isNodeMode || !mod2 || !mod2.__esModule ? __defProp(target, "default", { value: mod2, enumerable: true }) : target,
    mod2
  ));
  var __toCommonJS = (mod2) => __copyProps(__defProp({}, "__esModule", { value: true }), mod2);

  // ../node_modules/esbuild-plugin-polyfill-node/polyfills/__dirname.js
  var init_dirname = __esm({
    "../node_modules/esbuild-plugin-polyfill-node/polyfills/__dirname.js"() {
    }
  });

  // ../node_modules/@jspm/core/nodelibs/browser/process.js
  function unimplemented(name) {
    throw new Error("Node.js process " + name + " is not supported by JSPM core outside of Node.js");
  }
  function cleanUpNextTick() {
    if (!draining || !currentQueue)
      return;
    draining = false;
    if (currentQueue.length) {
      queue = currentQueue.concat(queue);
    } else {
      queueIndex = -1;
    }
    if (queue.length)
      drainQueue();
  }
  function drainQueue() {
    if (draining)
      return;
    var timeout = setTimeout(cleanUpNextTick, 0);
    draining = true;
    var len = queue.length;
    while (len) {
      currentQueue = queue;
      queue = [];
      while (++queueIndex < len) {
        if (currentQueue)
          currentQueue[queueIndex].run();
      }
      queueIndex = -1;
      len = queue.length;
    }
    currentQueue = null;
    draining = false;
    clearTimeout(timeout);
  }
  function nextTick(fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
      for (var i = 1; i < arguments.length; i++)
        args[i - 1] = arguments[i];
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining)
      setTimeout(drainQueue, 0);
  }
  function Item(fun, array) {
    this.fun = fun;
    this.array = array;
  }
  function noop() {
  }
  function _linkedBinding(name) {
    unimplemented("_linkedBinding");
  }
  function dlopen(name) {
    unimplemented("dlopen");
  }
  function _getActiveRequests() {
    return [];
  }
  function _getActiveHandles() {
    return [];
  }
  function assert(condition, message) {
    if (!condition) throw new Error(message || "assertion error");
  }
  function hasUncaughtExceptionCaptureCallback() {
    return false;
  }
  function uptime() {
    return _performance.now() / 1e3;
  }
  function hrtime(previousTimestamp) {
    var baseNow = Math.floor((Date.now() - _performance.now()) * 1e-3);
    var clocktime = _performance.now() * 1e-3;
    var seconds = Math.floor(clocktime) + baseNow;
    var nanoseconds = Math.floor(clocktime % 1 * 1e9);
    if (previousTimestamp) {
      seconds = seconds - previousTimestamp[0];
      nanoseconds = nanoseconds - previousTimestamp[1];
      if (nanoseconds < 0) {
        seconds--;
        nanoseconds += nanoPerSec;
      }
    }
    return [seconds, nanoseconds];
  }
  function on() {
    return process2;
  }
  function listeners(name) {
    return [];
  }
  var queue, draining, currentQueue, queueIndex, title, arch, platform, env, argv, execArgv, version, versions, emitWarning, binding, umask, cwd, chdir, release, browser, _rawDebug, moduleLoadList, domain, _exiting, config, reallyExit, _kill, cpuUsage, resourceUsage, memoryUsage, kill, exit, openStdin, allowedNodeEnvironmentFlags, features, _fatalExceptions, setUncaughtExceptionCaptureCallback, _tickCallback, _debugProcess, _debugEnd, _startProfilerIdleNotifier, _stopProfilerIdleNotifier, stdout, stderr, stdin, abort, pid, ppid, execPath, debugPort, argv0, _preload_modules, setSourceMapsEnabled, _performance, nanoPerSec, _maxListeners, _events, _eventsCount, addListener, once, off, removeListener, removeAllListeners, emit, prependListener, prependOnceListener, process2, nowOffset;
  var init_process = __esm({
    "../node_modules/@jspm/core/nodelibs/browser/process.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      __name(unimplemented, "unimplemented");
      queue = [];
      draining = false;
      queueIndex = -1;
      __name(cleanUpNextTick, "cleanUpNextTick");
      __name(drainQueue, "drainQueue");
      __name(nextTick, "nextTick");
      __name(Item, "Item");
      Item.prototype.run = function() {
        this.fun.apply(null, this.array);
      };
      title = "browser";
      arch = "x64";
      platform = "browser";
      env = {
        PATH: "/usr/bin",
        LANG: typeof navigator !== "undefined" ? navigator.language + ".UTF-8" : void 0,
        PWD: "/",
        HOME: "/home",
        TMP: "/tmp"
      };
      argv = ["/usr/bin/node"];
      execArgv = [];
      version = "v16.8.0";
      versions = {};
      emitWarning = /* @__PURE__ */ __name(function(message, type) {
        console.warn((type ? type + ": " : "") + message);
      }, "emitWarning");
      binding = /* @__PURE__ */ __name(function(name) {
        unimplemented("binding");
      }, "binding");
      umask = /* @__PURE__ */ __name(function(mask) {
        return 0;
      }, "umask");
      cwd = /* @__PURE__ */ __name(function() {
        return "/";
      }, "cwd");
      chdir = /* @__PURE__ */ __name(function(dir) {
      }, "chdir");
      release = {
        name: "node",
        sourceUrl: "",
        headersUrl: "",
        libUrl: ""
      };
      __name(noop, "noop");
      browser = true;
      _rawDebug = noop;
      moduleLoadList = [];
      __name(_linkedBinding, "_linkedBinding");
      domain = {};
      _exiting = false;
      config = {};
      __name(dlopen, "dlopen");
      __name(_getActiveRequests, "_getActiveRequests");
      __name(_getActiveHandles, "_getActiveHandles");
      reallyExit = noop;
      _kill = noop;
      cpuUsage = /* @__PURE__ */ __name(function() {
        return {};
      }, "cpuUsage");
      resourceUsage = cpuUsage;
      memoryUsage = cpuUsage;
      kill = noop;
      exit = noop;
      openStdin = noop;
      allowedNodeEnvironmentFlags = {};
      __name(assert, "assert");
      features = {
        inspector: false,
        debug: false,
        uv: false,
        ipv6: false,
        tls_alpn: false,
        tls_sni: false,
        tls_ocsp: false,
        tls: false,
        cached_builtins: true
      };
      _fatalExceptions = noop;
      setUncaughtExceptionCaptureCallback = noop;
      __name(hasUncaughtExceptionCaptureCallback, "hasUncaughtExceptionCaptureCallback");
      _tickCallback = noop;
      _debugProcess = noop;
      _debugEnd = noop;
      _startProfilerIdleNotifier = noop;
      _stopProfilerIdleNotifier = noop;
      stdout = void 0;
      stderr = void 0;
      stdin = void 0;
      abort = noop;
      pid = 2;
      ppid = 1;
      execPath = "/bin/usr/node";
      debugPort = 9229;
      argv0 = "node";
      _preload_modules = [];
      setSourceMapsEnabled = noop;
      _performance = {
        now: typeof performance !== "undefined" ? performance.now.bind(performance) : void 0,
        timing: typeof performance !== "undefined" ? performance.timing : void 0
      };
      if (_performance.now === void 0) {
        nowOffset = Date.now();
        if (_performance.timing && _performance.timing.navigationStart) {
          nowOffset = _performance.timing.navigationStart;
        }
        _performance.now = () => Date.now() - nowOffset;
      }
      __name(uptime, "uptime");
      nanoPerSec = 1e9;
      __name(hrtime, "hrtime");
      hrtime.bigint = function(time) {
        var diff = hrtime(time);
        if (typeof BigInt === "undefined") {
          return diff[0] * nanoPerSec + diff[1];
        }
        return BigInt(diff[0] * nanoPerSec) + BigInt(diff[1]);
      };
      _maxListeners = 10;
      _events = {};
      _eventsCount = 0;
      __name(on, "on");
      addListener = on;
      once = on;
      off = on;
      removeListener = on;
      removeAllListeners = on;
      emit = noop;
      prependListener = on;
      prependOnceListener = on;
      __name(listeners, "listeners");
      process2 = {
        version,
        versions,
        arch,
        platform,
        browser,
        release,
        _rawDebug,
        moduleLoadList,
        binding,
        _linkedBinding,
        _events,
        _eventsCount,
        _maxListeners,
        on,
        addListener,
        once,
        off,
        removeListener,
        removeAllListeners,
        emit,
        prependListener,
        prependOnceListener,
        listeners,
        domain,
        _exiting,
        config,
        dlopen,
        uptime,
        _getActiveRequests,
        _getActiveHandles,
        reallyExit,
        _kill,
        cpuUsage,
        resourceUsage,
        memoryUsage,
        kill,
        exit,
        openStdin,
        allowedNodeEnvironmentFlags,
        assert,
        features,
        _fatalExceptions,
        setUncaughtExceptionCaptureCallback,
        hasUncaughtExceptionCaptureCallback,
        emitWarning,
        nextTick,
        _tickCallback,
        _debugProcess,
        _debugEnd,
        _startProfilerIdleNotifier,
        _stopProfilerIdleNotifier,
        stdout,
        stdin,
        stderr,
        abort,
        umask,
        chdir,
        cwd,
        env,
        title,
        argv,
        execArgv,
        pid,
        ppid,
        execPath,
        debugPort,
        hrtime,
        argv0,
        _preload_modules,
        setSourceMapsEnabled
      };
    }
  });

  // ../node_modules/esbuild-plugin-polyfill-node/polyfills/process.js
  var init_process2 = __esm({
    "../node_modules/esbuild-plugin-polyfill-node/polyfills/process.js"() {
      init_process();
    }
  });

  // ../node_modules/@jspm/core/nodelibs/browser/chunk-DtuTasat.js
  function dew$2() {
    if (_dewExec$2) return exports$2;
    _dewExec$2 = true;
    exports$2.byteLength = byteLength;
    exports$2.toByteArray = toByteArray;
    exports$2.fromByteArray = fromByteArray;
    var lookup = [];
    var revLookup = [];
    var Arr = typeof Uint8Array !== "undefined" ? Uint8Array : Array;
    var code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    for (var i = 0, len = code.length; i < len; ++i) {
      lookup[i] = code[i];
      revLookup[code.charCodeAt(i)] = i;
    }
    revLookup["-".charCodeAt(0)] = 62;
    revLookup["_".charCodeAt(0)] = 63;
    function getLens(b64) {
      var len2 = b64.length;
      if (len2 % 4 > 0) {
        throw new Error("Invalid string. Length must be a multiple of 4");
      }
      var validLen = b64.indexOf("=");
      if (validLen === -1) validLen = len2;
      var placeHoldersLen = validLen === len2 ? 0 : 4 - validLen % 4;
      return [validLen, placeHoldersLen];
    }
    __name(getLens, "getLens");
    function byteLength(b64) {
      var lens = getLens(b64);
      var validLen = lens[0];
      var placeHoldersLen = lens[1];
      return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
    }
    __name(byteLength, "byteLength");
    function _byteLength(b64, validLen, placeHoldersLen) {
      return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
    }
    __name(_byteLength, "_byteLength");
    function toByteArray(b64) {
      var tmp;
      var lens = getLens(b64);
      var validLen = lens[0];
      var placeHoldersLen = lens[1];
      var arr = new Arr(_byteLength(b64, validLen, placeHoldersLen));
      var curByte = 0;
      var len2 = placeHoldersLen > 0 ? validLen - 4 : validLen;
      var i2;
      for (i2 = 0; i2 < len2; i2 += 4) {
        tmp = revLookup[b64.charCodeAt(i2)] << 18 | revLookup[b64.charCodeAt(i2 + 1)] << 12 | revLookup[b64.charCodeAt(i2 + 2)] << 6 | revLookup[b64.charCodeAt(i2 + 3)];
        arr[curByte++] = tmp >> 16 & 255;
        arr[curByte++] = tmp >> 8 & 255;
        arr[curByte++] = tmp & 255;
      }
      if (placeHoldersLen === 2) {
        tmp = revLookup[b64.charCodeAt(i2)] << 2 | revLookup[b64.charCodeAt(i2 + 1)] >> 4;
        arr[curByte++] = tmp & 255;
      }
      if (placeHoldersLen === 1) {
        tmp = revLookup[b64.charCodeAt(i2)] << 10 | revLookup[b64.charCodeAt(i2 + 1)] << 4 | revLookup[b64.charCodeAt(i2 + 2)] >> 2;
        arr[curByte++] = tmp >> 8 & 255;
        arr[curByte++] = tmp & 255;
      }
      return arr;
    }
    __name(toByteArray, "toByteArray");
    function tripletToBase64(num) {
      return lookup[num >> 18 & 63] + lookup[num >> 12 & 63] + lookup[num >> 6 & 63] + lookup[num & 63];
    }
    __name(tripletToBase64, "tripletToBase64");
    function encodeChunk(uint8, start, end2) {
      var tmp;
      var output = [];
      for (var i2 = start; i2 < end2; i2 += 3) {
        tmp = (uint8[i2] << 16 & 16711680) + (uint8[i2 + 1] << 8 & 65280) + (uint8[i2 + 2] & 255);
        output.push(tripletToBase64(tmp));
      }
      return output.join("");
    }
    __name(encodeChunk, "encodeChunk");
    function fromByteArray(uint8) {
      var tmp;
      var len2 = uint8.length;
      var extraBytes = len2 % 3;
      var parts = [];
      var maxChunkLength = 16383;
      for (var i2 = 0, len22 = len2 - extraBytes; i2 < len22; i2 += maxChunkLength) {
        parts.push(encodeChunk(uint8, i2, i2 + maxChunkLength > len22 ? len22 : i2 + maxChunkLength));
      }
      if (extraBytes === 1) {
        tmp = uint8[len2 - 1];
        parts.push(lookup[tmp >> 2] + lookup[tmp << 4 & 63] + "==");
      } else if (extraBytes === 2) {
        tmp = (uint8[len2 - 2] << 8) + uint8[len2 - 1];
        parts.push(lookup[tmp >> 10] + lookup[tmp >> 4 & 63] + lookup[tmp << 2 & 63] + "=");
      }
      return parts.join("");
    }
    __name(fromByteArray, "fromByteArray");
    return exports$2;
  }
  function dew$1() {
    if (_dewExec$1) return exports$1;
    _dewExec$1 = true;
    /*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> */
    exports$1.read = function(buffer, offset, isLE, mLen, nBytes) {
      var e2, m;
      var eLen = nBytes * 8 - mLen - 1;
      var eMax = (1 << eLen) - 1;
      var eBias = eMax >> 1;
      var nBits = -7;
      var i = isLE ? nBytes - 1 : 0;
      var d = isLE ? -1 : 1;
      var s = buffer[offset + i];
      i += d;
      e2 = s & (1 << -nBits) - 1;
      s >>= -nBits;
      nBits += eLen;
      for (; nBits > 0; e2 = e2 * 256 + buffer[offset + i], i += d, nBits -= 8) {
      }
      m = e2 & (1 << -nBits) - 1;
      e2 >>= -nBits;
      nBits += mLen;
      for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8) {
      }
      if (e2 === 0) {
        e2 = 1 - eBias;
      } else if (e2 === eMax) {
        return m ? NaN : (s ? -1 : 1) * Infinity;
      } else {
        m = m + Math.pow(2, mLen);
        e2 = e2 - eBias;
      }
      return (s ? -1 : 1) * m * Math.pow(2, e2 - mLen);
    };
    exports$1.write = function(buffer, value, offset, isLE, mLen, nBytes) {
      var e2, m, c;
      var eLen = nBytes * 8 - mLen - 1;
      var eMax = (1 << eLen) - 1;
      var eBias = eMax >> 1;
      var rt = mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0;
      var i = isLE ? 0 : nBytes - 1;
      var d = isLE ? 1 : -1;
      var s = value < 0 || value === 0 && 1 / value < 0 ? 1 : 0;
      value = Math.abs(value);
      if (isNaN(value) || value === Infinity) {
        m = isNaN(value) ? 1 : 0;
        e2 = eMax;
      } else {
        e2 = Math.floor(Math.log(value) / Math.LN2);
        if (value * (c = Math.pow(2, -e2)) < 1) {
          e2--;
          c *= 2;
        }
        if (e2 + eBias >= 1) {
          value += rt / c;
        } else {
          value += rt * Math.pow(2, 1 - eBias);
        }
        if (value * c >= 2) {
          e2++;
          c /= 2;
        }
        if (e2 + eBias >= eMax) {
          m = 0;
          e2 = eMax;
        } else if (e2 + eBias >= 1) {
          m = (value * c - 1) * Math.pow(2, mLen);
          e2 = e2 + eBias;
        } else {
          m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen);
          e2 = 0;
        }
      }
      for (; mLen >= 8; buffer[offset + i] = m & 255, i += d, m /= 256, mLen -= 8) {
      }
      e2 = e2 << mLen | m;
      eLen += mLen;
      for (; eLen > 0; buffer[offset + i] = e2 & 255, i += d, e2 /= 256, eLen -= 8) {
      }
      buffer[offset + i - d] |= s * 128;
    };
    return exports$1;
  }
  function dew() {
    if (_dewExec) return exports2;
    _dewExec = true;
    const base64 = dew$2();
    const ieee754 = dew$1();
    const customInspectSymbol = typeof Symbol === "function" && typeof Symbol["for"] === "function" ? Symbol["for"]("nodejs.util.inspect.custom") : null;
    exports2.Buffer = Buffer2;
    exports2.SlowBuffer = SlowBuffer;
    exports2.INSPECT_MAX_BYTES = 50;
    const K_MAX_LENGTH = 2147483647;
    exports2.kMaxLength = K_MAX_LENGTH;
    Buffer2.TYPED_ARRAY_SUPPORT = typedArraySupport();
    if (!Buffer2.TYPED_ARRAY_SUPPORT && typeof console !== "undefined" && typeof console.error === "function") {
      console.error("This browser lacks typed array (Uint8Array) support which is required by `buffer` v5.x. Use `buffer` v4.x if you require old browser support.");
    }
    function typedArraySupport() {
      try {
        const arr = new Uint8Array(1);
        const proto = {
          foo: /* @__PURE__ */ __name(function() {
            return 42;
          }, "foo")
        };
        Object.setPrototypeOf(proto, Uint8Array.prototype);
        Object.setPrototypeOf(arr, proto);
        return arr.foo() === 42;
      } catch (e2) {
        return false;
      }
    }
    __name(typedArraySupport, "typedArraySupport");
    Object.defineProperty(Buffer2.prototype, "parent", {
      enumerable: true,
      get: /* @__PURE__ */ __name(function() {
        if (!Buffer2.isBuffer(this)) return void 0;
        return this.buffer;
      }, "get")
    });
    Object.defineProperty(Buffer2.prototype, "offset", {
      enumerable: true,
      get: /* @__PURE__ */ __name(function() {
        if (!Buffer2.isBuffer(this)) return void 0;
        return this.byteOffset;
      }, "get")
    });
    function createBuffer(length) {
      if (length > K_MAX_LENGTH) {
        throw new RangeError('The value "' + length + '" is invalid for option "size"');
      }
      const buf = new Uint8Array(length);
      Object.setPrototypeOf(buf, Buffer2.prototype);
      return buf;
    }
    __name(createBuffer, "createBuffer");
    function Buffer2(arg, encodingOrOffset, length) {
      if (typeof arg === "number") {
        if (typeof encodingOrOffset === "string") {
          throw new TypeError('The "string" argument must be of type string. Received type number');
        }
        return allocUnsafe(arg);
      }
      return from(arg, encodingOrOffset, length);
    }
    __name(Buffer2, "Buffer");
    Buffer2.poolSize = 8192;
    function from(value, encodingOrOffset, length) {
      if (typeof value === "string") {
        return fromString(value, encodingOrOffset);
      }
      if (ArrayBuffer.isView(value)) {
        return fromArrayView(value);
      }
      if (value == null) {
        throw new TypeError("The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value);
      }
      if (isInstance(value, ArrayBuffer) || value && isInstance(value.buffer, ArrayBuffer)) {
        return fromArrayBuffer(value, encodingOrOffset, length);
      }
      if (typeof SharedArrayBuffer !== "undefined" && (isInstance(value, SharedArrayBuffer) || value && isInstance(value.buffer, SharedArrayBuffer))) {
        return fromArrayBuffer(value, encodingOrOffset, length);
      }
      if (typeof value === "number") {
        throw new TypeError('The "value" argument must not be of type number. Received type number');
      }
      const valueOf = value.valueOf && value.valueOf();
      if (valueOf != null && valueOf !== value) {
        return Buffer2.from(valueOf, encodingOrOffset, length);
      }
      const b = fromObject(value);
      if (b) return b;
      if (typeof Symbol !== "undefined" && Symbol.toPrimitive != null && typeof value[Symbol.toPrimitive] === "function") {
        return Buffer2.from(value[Symbol.toPrimitive]("string"), encodingOrOffset, length);
      }
      throw new TypeError("The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value);
    }
    __name(from, "from");
    Buffer2.from = function(value, encodingOrOffset, length) {
      return from(value, encodingOrOffset, length);
    };
    Object.setPrototypeOf(Buffer2.prototype, Uint8Array.prototype);
    Object.setPrototypeOf(Buffer2, Uint8Array);
    function assertSize(size) {
      if (typeof size !== "number") {
        throw new TypeError('"size" argument must be of type number');
      } else if (size < 0) {
        throw new RangeError('The value "' + size + '" is invalid for option "size"');
      }
    }
    __name(assertSize, "assertSize");
    function alloc(size, fill, encoding) {
      assertSize(size);
      if (size <= 0) {
        return createBuffer(size);
      }
      if (fill !== void 0) {
        return typeof encoding === "string" ? createBuffer(size).fill(fill, encoding) : createBuffer(size).fill(fill);
      }
      return createBuffer(size);
    }
    __name(alloc, "alloc");
    Buffer2.alloc = function(size, fill, encoding) {
      return alloc(size, fill, encoding);
    };
    function allocUnsafe(size) {
      assertSize(size);
      return createBuffer(size < 0 ? 0 : checked(size) | 0);
    }
    __name(allocUnsafe, "allocUnsafe");
    Buffer2.allocUnsafe = function(size) {
      return allocUnsafe(size);
    };
    Buffer2.allocUnsafeSlow = function(size) {
      return allocUnsafe(size);
    };
    function fromString(string, encoding) {
      if (typeof encoding !== "string" || encoding === "") {
        encoding = "utf8";
      }
      if (!Buffer2.isEncoding(encoding)) {
        throw new TypeError("Unknown encoding: " + encoding);
      }
      const length = byteLength(string, encoding) | 0;
      let buf = createBuffer(length);
      const actual = buf.write(string, encoding);
      if (actual !== length) {
        buf = buf.slice(0, actual);
      }
      return buf;
    }
    __name(fromString, "fromString");
    function fromArrayLike(array) {
      const length = array.length < 0 ? 0 : checked(array.length) | 0;
      const buf = createBuffer(length);
      for (let i = 0; i < length; i += 1) {
        buf[i] = array[i] & 255;
      }
      return buf;
    }
    __name(fromArrayLike, "fromArrayLike");
    function fromArrayView(arrayView) {
      if (isInstance(arrayView, Uint8Array)) {
        const copy = new Uint8Array(arrayView);
        return fromArrayBuffer(copy.buffer, copy.byteOffset, copy.byteLength);
      }
      return fromArrayLike(arrayView);
    }
    __name(fromArrayView, "fromArrayView");
    function fromArrayBuffer(array, byteOffset, length) {
      if (byteOffset < 0 || array.byteLength < byteOffset) {
        throw new RangeError('"offset" is outside of buffer bounds');
      }
      if (array.byteLength < byteOffset + (length || 0)) {
        throw new RangeError('"length" is outside of buffer bounds');
      }
      let buf;
      if (byteOffset === void 0 && length === void 0) {
        buf = new Uint8Array(array);
      } else if (length === void 0) {
        buf = new Uint8Array(array, byteOffset);
      } else {
        buf = new Uint8Array(array, byteOffset, length);
      }
      Object.setPrototypeOf(buf, Buffer2.prototype);
      return buf;
    }
    __name(fromArrayBuffer, "fromArrayBuffer");
    function fromObject(obj) {
      if (Buffer2.isBuffer(obj)) {
        const len = checked(obj.length) | 0;
        const buf = createBuffer(len);
        if (buf.length === 0) {
          return buf;
        }
        obj.copy(buf, 0, 0, len);
        return buf;
      }
      if (obj.length !== void 0) {
        if (typeof obj.length !== "number" || numberIsNaN(obj.length)) {
          return createBuffer(0);
        }
        return fromArrayLike(obj);
      }
      if (obj.type === "Buffer" && Array.isArray(obj.data)) {
        return fromArrayLike(obj.data);
      }
    }
    __name(fromObject, "fromObject");
    function checked(length) {
      if (length >= K_MAX_LENGTH) {
        throw new RangeError("Attempt to allocate Buffer larger than maximum size: 0x" + K_MAX_LENGTH.toString(16) + " bytes");
      }
      return length | 0;
    }
    __name(checked, "checked");
    function SlowBuffer(length) {
      if (+length != length) {
        length = 0;
      }
      return Buffer2.alloc(+length);
    }
    __name(SlowBuffer, "SlowBuffer");
    Buffer2.isBuffer = /* @__PURE__ */ __name(function isBuffer(b) {
      return b != null && b._isBuffer === true && b !== Buffer2.prototype;
    }, "isBuffer");
    Buffer2.compare = /* @__PURE__ */ __name(function compare(a2, b) {
      if (isInstance(a2, Uint8Array)) a2 = Buffer2.from(a2, a2.offset, a2.byteLength);
      if (isInstance(b, Uint8Array)) b = Buffer2.from(b, b.offset, b.byteLength);
      if (!Buffer2.isBuffer(a2) || !Buffer2.isBuffer(b)) {
        throw new TypeError('The "buf1", "buf2" arguments must be one of type Buffer or Uint8Array');
      }
      if (a2 === b) return 0;
      let x = a2.length;
      let y = b.length;
      for (let i = 0, len = Math.min(x, y); i < len; ++i) {
        if (a2[i] !== b[i]) {
          x = a2[i];
          y = b[i];
          break;
        }
      }
      if (x < y) return -1;
      if (y < x) return 1;
      return 0;
    }, "compare");
    Buffer2.isEncoding = /* @__PURE__ */ __name(function isEncoding(encoding) {
      switch (String(encoding).toLowerCase()) {
        case "hex":
        case "utf8":
        case "utf-8":
        case "ascii":
        case "latin1":
        case "binary":
        case "base64":
        case "ucs2":
        case "ucs-2":
        case "utf16le":
        case "utf-16le":
          return true;
        default:
          return false;
      }
    }, "isEncoding");
    Buffer2.concat = /* @__PURE__ */ __name(function concat(list, length) {
      if (!Array.isArray(list)) {
        throw new TypeError('"list" argument must be an Array of Buffers');
      }
      if (list.length === 0) {
        return Buffer2.alloc(0);
      }
      let i;
      if (length === void 0) {
        length = 0;
        for (i = 0; i < list.length; ++i) {
          length += list[i].length;
        }
      }
      const buffer = Buffer2.allocUnsafe(length);
      let pos = 0;
      for (i = 0; i < list.length; ++i) {
        let buf = list[i];
        if (isInstance(buf, Uint8Array)) {
          if (pos + buf.length > buffer.length) {
            if (!Buffer2.isBuffer(buf)) buf = Buffer2.from(buf);
            buf.copy(buffer, pos);
          } else {
            Uint8Array.prototype.set.call(buffer, buf, pos);
          }
        } else if (!Buffer2.isBuffer(buf)) {
          throw new TypeError('"list" argument must be an Array of Buffers');
        } else {
          buf.copy(buffer, pos);
        }
        pos += buf.length;
      }
      return buffer;
    }, "concat");
    function byteLength(string, encoding) {
      if (Buffer2.isBuffer(string)) {
        return string.length;
      }
      if (ArrayBuffer.isView(string) || isInstance(string, ArrayBuffer)) {
        return string.byteLength;
      }
      if (typeof string !== "string") {
        throw new TypeError('The "string" argument must be one of type string, Buffer, or ArrayBuffer. Received type ' + typeof string);
      }
      const len = string.length;
      const mustMatch = arguments.length > 2 && arguments[2] === true;
      if (!mustMatch && len === 0) return 0;
      let loweredCase = false;
      for (; ; ) {
        switch (encoding) {
          case "ascii":
          case "latin1":
          case "binary":
            return len;
          case "utf8":
          case "utf-8":
            return utf8ToBytes(string).length;
          case "ucs2":
          case "ucs-2":
          case "utf16le":
          case "utf-16le":
            return len * 2;
          case "hex":
            return len >>> 1;
          case "base64":
            return base64ToBytes(string).length;
          default:
            if (loweredCase) {
              return mustMatch ? -1 : utf8ToBytes(string).length;
            }
            encoding = ("" + encoding).toLowerCase();
            loweredCase = true;
        }
      }
    }
    __name(byteLength, "byteLength");
    Buffer2.byteLength = byteLength;
    function slowToString(encoding, start, end2) {
      let loweredCase = false;
      if (start === void 0 || start < 0) {
        start = 0;
      }
      if (start > this.length) {
        return "";
      }
      if (end2 === void 0 || end2 > this.length) {
        end2 = this.length;
      }
      if (end2 <= 0) {
        return "";
      }
      end2 >>>= 0;
      start >>>= 0;
      if (end2 <= start) {
        return "";
      }
      if (!encoding) encoding = "utf8";
      while (true) {
        switch (encoding) {
          case "hex":
            return hexSlice(this, start, end2);
          case "utf8":
          case "utf-8":
            return utf8Slice(this, start, end2);
          case "ascii":
            return asciiSlice(this, start, end2);
          case "latin1":
          case "binary":
            return latin1Slice(this, start, end2);
          case "base64":
            return base64Slice(this, start, end2);
          case "ucs2":
          case "ucs-2":
          case "utf16le":
          case "utf-16le":
            return utf16leSlice(this, start, end2);
          default:
            if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
            encoding = (encoding + "").toLowerCase();
            loweredCase = true;
        }
      }
    }
    __name(slowToString, "slowToString");
    Buffer2.prototype._isBuffer = true;
    function swap(b, n2, m) {
      const i = b[n2];
      b[n2] = b[m];
      b[m] = i;
    }
    __name(swap, "swap");
    Buffer2.prototype.swap16 = /* @__PURE__ */ __name(function swap16() {
      const len = this.length;
      if (len % 2 !== 0) {
        throw new RangeError("Buffer size must be a multiple of 16-bits");
      }
      for (let i = 0; i < len; i += 2) {
        swap(this, i, i + 1);
      }
      return this;
    }, "swap16");
    Buffer2.prototype.swap32 = /* @__PURE__ */ __name(function swap32() {
      const len = this.length;
      if (len % 4 !== 0) {
        throw new RangeError("Buffer size must be a multiple of 32-bits");
      }
      for (let i = 0; i < len; i += 4) {
        swap(this, i, i + 3);
        swap(this, i + 1, i + 2);
      }
      return this;
    }, "swap32");
    Buffer2.prototype.swap64 = /* @__PURE__ */ __name(function swap64() {
      const len = this.length;
      if (len % 8 !== 0) {
        throw new RangeError("Buffer size must be a multiple of 64-bits");
      }
      for (let i = 0; i < len; i += 8) {
        swap(this, i, i + 7);
        swap(this, i + 1, i + 6);
        swap(this, i + 2, i + 5);
        swap(this, i + 3, i + 4);
      }
      return this;
    }, "swap64");
    Buffer2.prototype.toString = /* @__PURE__ */ __name(function toString2() {
      const length = this.length;
      if (length === 0) return "";
      if (arguments.length === 0) return utf8Slice(this, 0, length);
      return slowToString.apply(this, arguments);
    }, "toString");
    Buffer2.prototype.toLocaleString = Buffer2.prototype.toString;
    Buffer2.prototype.equals = /* @__PURE__ */ __name(function equals(b) {
      if (!Buffer2.isBuffer(b)) throw new TypeError("Argument must be a Buffer");
      if (this === b) return true;
      return Buffer2.compare(this, b) === 0;
    }, "equals");
    Buffer2.prototype.inspect = /* @__PURE__ */ __name(function inspect() {
      let str = "";
      const max = exports2.INSPECT_MAX_BYTES;
      str = this.toString("hex", 0, max).replace(/(.{2})/g, "$1 ").trim();
      if (this.length > max) str += " ... ";
      return "<Buffer " + str + ">";
    }, "inspect");
    if (customInspectSymbol) {
      Buffer2.prototype[customInspectSymbol] = Buffer2.prototype.inspect;
    }
    Buffer2.prototype.compare = /* @__PURE__ */ __name(function compare(target, start, end2, thisStart, thisEnd) {
      if (isInstance(target, Uint8Array)) {
        target = Buffer2.from(target, target.offset, target.byteLength);
      }
      if (!Buffer2.isBuffer(target)) {
        throw new TypeError('The "target" argument must be one of type Buffer or Uint8Array. Received type ' + typeof target);
      }
      if (start === void 0) {
        start = 0;
      }
      if (end2 === void 0) {
        end2 = target ? target.length : 0;
      }
      if (thisStart === void 0) {
        thisStart = 0;
      }
      if (thisEnd === void 0) {
        thisEnd = this.length;
      }
      if (start < 0 || end2 > target.length || thisStart < 0 || thisEnd > this.length) {
        throw new RangeError("out of range index");
      }
      if (thisStart >= thisEnd && start >= end2) {
        return 0;
      }
      if (thisStart >= thisEnd) {
        return -1;
      }
      if (start >= end2) {
        return 1;
      }
      start >>>= 0;
      end2 >>>= 0;
      thisStart >>>= 0;
      thisEnd >>>= 0;
      if (this === target) return 0;
      let x = thisEnd - thisStart;
      let y = end2 - start;
      const len = Math.min(x, y);
      const thisCopy = this.slice(thisStart, thisEnd);
      const targetCopy = target.slice(start, end2);
      for (let i = 0; i < len; ++i) {
        if (thisCopy[i] !== targetCopy[i]) {
          x = thisCopy[i];
          y = targetCopy[i];
          break;
        }
      }
      if (x < y) return -1;
      if (y < x) return 1;
      return 0;
    }, "compare");
    function bidirectionalIndexOf(buffer, val2, byteOffset, encoding, dir) {
      if (buffer.length === 0) return -1;
      if (typeof byteOffset === "string") {
        encoding = byteOffset;
        byteOffset = 0;
      } else if (byteOffset > 2147483647) {
        byteOffset = 2147483647;
      } else if (byteOffset < -2147483648) {
        byteOffset = -2147483648;
      }
      byteOffset = +byteOffset;
      if (numberIsNaN(byteOffset)) {
        byteOffset = dir ? 0 : buffer.length - 1;
      }
      if (byteOffset < 0) byteOffset = buffer.length + byteOffset;
      if (byteOffset >= buffer.length) {
        if (dir) return -1;
        else byteOffset = buffer.length - 1;
      } else if (byteOffset < 0) {
        if (dir) byteOffset = 0;
        else return -1;
      }
      if (typeof val2 === "string") {
        val2 = Buffer2.from(val2, encoding);
      }
      if (Buffer2.isBuffer(val2)) {
        if (val2.length === 0) {
          return -1;
        }
        return arrayIndexOf(buffer, val2, byteOffset, encoding, dir);
      } else if (typeof val2 === "number") {
        val2 = val2 & 255;
        if (typeof Uint8Array.prototype.indexOf === "function") {
          if (dir) {
            return Uint8Array.prototype.indexOf.call(buffer, val2, byteOffset);
          } else {
            return Uint8Array.prototype.lastIndexOf.call(buffer, val2, byteOffset);
          }
        }
        return arrayIndexOf(buffer, [val2], byteOffset, encoding, dir);
      }
      throw new TypeError("val must be string, number or Buffer");
    }
    __name(bidirectionalIndexOf, "bidirectionalIndexOf");
    function arrayIndexOf(arr, val2, byteOffset, encoding, dir) {
      let indexSize = 1;
      let arrLength = arr.length;
      let valLength = val2.length;
      if (encoding !== void 0) {
        encoding = String(encoding).toLowerCase();
        if (encoding === "ucs2" || encoding === "ucs-2" || encoding === "utf16le" || encoding === "utf-16le") {
          if (arr.length < 2 || val2.length < 2) {
            return -1;
          }
          indexSize = 2;
          arrLength /= 2;
          valLength /= 2;
          byteOffset /= 2;
        }
      }
      function read(buf, i2) {
        if (indexSize === 1) {
          return buf[i2];
        } else {
          return buf.readUInt16BE(i2 * indexSize);
        }
      }
      __name(read, "read");
      let i;
      if (dir) {
        let foundIndex = -1;
        for (i = byteOffset; i < arrLength; i++) {
          if (read(arr, i) === read(val2, foundIndex === -1 ? 0 : i - foundIndex)) {
            if (foundIndex === -1) foundIndex = i;
            if (i - foundIndex + 1 === valLength) return foundIndex * indexSize;
          } else {
            if (foundIndex !== -1) i -= i - foundIndex;
            foundIndex = -1;
          }
        }
      } else {
        if (byteOffset + valLength > arrLength) byteOffset = arrLength - valLength;
        for (i = byteOffset; i >= 0; i--) {
          let found = true;
          for (let j = 0; j < valLength; j++) {
            if (read(arr, i + j) !== read(val2, j)) {
              found = false;
              break;
            }
          }
          if (found) return i;
        }
      }
      return -1;
    }
    __name(arrayIndexOf, "arrayIndexOf");
    Buffer2.prototype.includes = /* @__PURE__ */ __name(function includes(val2, byteOffset, encoding) {
      return this.indexOf(val2, byteOffset, encoding) !== -1;
    }, "includes");
    Buffer2.prototype.indexOf = /* @__PURE__ */ __name(function indexOf(val2, byteOffset, encoding) {
      return bidirectionalIndexOf(this, val2, byteOffset, encoding, true);
    }, "indexOf");
    Buffer2.prototype.lastIndexOf = /* @__PURE__ */ __name(function lastIndexOf(val2, byteOffset, encoding) {
      return bidirectionalIndexOf(this, val2, byteOffset, encoding, false);
    }, "lastIndexOf");
    function hexWrite(buf, string, offset, length) {
      offset = Number(offset) || 0;
      const remaining = buf.length - offset;
      if (!length) {
        length = remaining;
      } else {
        length = Number(length);
        if (length > remaining) {
          length = remaining;
        }
      }
      const strLen = string.length;
      if (length > strLen / 2) {
        length = strLen / 2;
      }
      let i;
      for (i = 0; i < length; ++i) {
        const parsed = parseInt(string.substr(i * 2, 2), 16);
        if (numberIsNaN(parsed)) return i;
        buf[offset + i] = parsed;
      }
      return i;
    }
    __name(hexWrite, "hexWrite");
    function utf8Write(buf, string, offset, length) {
      return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length);
    }
    __name(utf8Write, "utf8Write");
    function asciiWrite(buf, string, offset, length) {
      return blitBuffer(asciiToBytes(string), buf, offset, length);
    }
    __name(asciiWrite, "asciiWrite");
    function base64Write(buf, string, offset, length) {
      return blitBuffer(base64ToBytes(string), buf, offset, length);
    }
    __name(base64Write, "base64Write");
    function ucs2Write(buf, string, offset, length) {
      return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length);
    }
    __name(ucs2Write, "ucs2Write");
    Buffer2.prototype.write = /* @__PURE__ */ __name(function write(string, offset, length, encoding) {
      if (offset === void 0) {
        encoding = "utf8";
        length = this.length;
        offset = 0;
      } else if (length === void 0 && typeof offset === "string") {
        encoding = offset;
        length = this.length;
        offset = 0;
      } else if (isFinite(offset)) {
        offset = offset >>> 0;
        if (isFinite(length)) {
          length = length >>> 0;
          if (encoding === void 0) encoding = "utf8";
        } else {
          encoding = length;
          length = void 0;
        }
      } else {
        throw new Error("Buffer.write(string, encoding, offset[, length]) is no longer supported");
      }
      const remaining = this.length - offset;
      if (length === void 0 || length > remaining) length = remaining;
      if (string.length > 0 && (length < 0 || offset < 0) || offset > this.length) {
        throw new RangeError("Attempt to write outside buffer bounds");
      }
      if (!encoding) encoding = "utf8";
      let loweredCase = false;
      for (; ; ) {
        switch (encoding) {
          case "hex":
            return hexWrite(this, string, offset, length);
          case "utf8":
          case "utf-8":
            return utf8Write(this, string, offset, length);
          case "ascii":
          case "latin1":
          case "binary":
            return asciiWrite(this, string, offset, length);
          case "base64":
            return base64Write(this, string, offset, length);
          case "ucs2":
          case "ucs-2":
          case "utf16le":
          case "utf-16le":
            return ucs2Write(this, string, offset, length);
          default:
            if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
            encoding = ("" + encoding).toLowerCase();
            loweredCase = true;
        }
      }
    }, "write");
    Buffer2.prototype.toJSON = /* @__PURE__ */ __name(function toJSON() {
      return {
        type: "Buffer",
        data: Array.prototype.slice.call(this._arr || this, 0)
      };
    }, "toJSON");
    function base64Slice(buf, start, end2) {
      if (start === 0 && end2 === buf.length) {
        return base64.fromByteArray(buf);
      } else {
        return base64.fromByteArray(buf.slice(start, end2));
      }
    }
    __name(base64Slice, "base64Slice");
    function utf8Slice(buf, start, end2) {
      end2 = Math.min(buf.length, end2);
      const res = [];
      let i = start;
      while (i < end2) {
        const firstByte = buf[i];
        let codePoint = null;
        let bytesPerSequence = firstByte > 239 ? 4 : firstByte > 223 ? 3 : firstByte > 191 ? 2 : 1;
        if (i + bytesPerSequence <= end2) {
          let secondByte, thirdByte, fourthByte, tempCodePoint;
          switch (bytesPerSequence) {
            case 1:
              if (firstByte < 128) {
                codePoint = firstByte;
              }
              break;
            case 2:
              secondByte = buf[i + 1];
              if ((secondByte & 192) === 128) {
                tempCodePoint = (firstByte & 31) << 6 | secondByte & 63;
                if (tempCodePoint > 127) {
                  codePoint = tempCodePoint;
                }
              }
              break;
            case 3:
              secondByte = buf[i + 1];
              thirdByte = buf[i + 2];
              if ((secondByte & 192) === 128 && (thirdByte & 192) === 128) {
                tempCodePoint = (firstByte & 15) << 12 | (secondByte & 63) << 6 | thirdByte & 63;
                if (tempCodePoint > 2047 && (tempCodePoint < 55296 || tempCodePoint > 57343)) {
                  codePoint = tempCodePoint;
                }
              }
              break;
            case 4:
              secondByte = buf[i + 1];
              thirdByte = buf[i + 2];
              fourthByte = buf[i + 3];
              if ((secondByte & 192) === 128 && (thirdByte & 192) === 128 && (fourthByte & 192) === 128) {
                tempCodePoint = (firstByte & 15) << 18 | (secondByte & 63) << 12 | (thirdByte & 63) << 6 | fourthByte & 63;
                if (tempCodePoint > 65535 && tempCodePoint < 1114112) {
                  codePoint = tempCodePoint;
                }
              }
          }
        }
        if (codePoint === null) {
          codePoint = 65533;
          bytesPerSequence = 1;
        } else if (codePoint > 65535) {
          codePoint -= 65536;
          res.push(codePoint >>> 10 & 1023 | 55296);
          codePoint = 56320 | codePoint & 1023;
        }
        res.push(codePoint);
        i += bytesPerSequence;
      }
      return decodeCodePointsArray(res);
    }
    __name(utf8Slice, "utf8Slice");
    const MAX_ARGUMENTS_LENGTH = 4096;
    function decodeCodePointsArray(codePoints) {
      const len = codePoints.length;
      if (len <= MAX_ARGUMENTS_LENGTH) {
        return String.fromCharCode.apply(String, codePoints);
      }
      let res = "";
      let i = 0;
      while (i < len) {
        res += String.fromCharCode.apply(String, codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH));
      }
      return res;
    }
    __name(decodeCodePointsArray, "decodeCodePointsArray");
    function asciiSlice(buf, start, end2) {
      let ret = "";
      end2 = Math.min(buf.length, end2);
      for (let i = start; i < end2; ++i) {
        ret += String.fromCharCode(buf[i] & 127);
      }
      return ret;
    }
    __name(asciiSlice, "asciiSlice");
    function latin1Slice(buf, start, end2) {
      let ret = "";
      end2 = Math.min(buf.length, end2);
      for (let i = start; i < end2; ++i) {
        ret += String.fromCharCode(buf[i]);
      }
      return ret;
    }
    __name(latin1Slice, "latin1Slice");
    function hexSlice(buf, start, end2) {
      const len = buf.length;
      if (!start || start < 0) start = 0;
      if (!end2 || end2 < 0 || end2 > len) end2 = len;
      let out = "";
      for (let i = start; i < end2; ++i) {
        out += hexSliceLookupTable[buf[i]];
      }
      return out;
    }
    __name(hexSlice, "hexSlice");
    function utf16leSlice(buf, start, end2) {
      const bytes = buf.slice(start, end2);
      let res = "";
      for (let i = 0; i < bytes.length - 1; i += 2) {
        res += String.fromCharCode(bytes[i] + bytes[i + 1] * 256);
      }
      return res;
    }
    __name(utf16leSlice, "utf16leSlice");
    Buffer2.prototype.slice = /* @__PURE__ */ __name(function slice2(start, end2) {
      const len = this.length;
      start = ~~start;
      end2 = end2 === void 0 ? len : ~~end2;
      if (start < 0) {
        start += len;
        if (start < 0) start = 0;
      } else if (start > len) {
        start = len;
      }
      if (end2 < 0) {
        end2 += len;
        if (end2 < 0) end2 = 0;
      } else if (end2 > len) {
        end2 = len;
      }
      if (end2 < start) end2 = start;
      const newBuf = this.subarray(start, end2);
      Object.setPrototypeOf(newBuf, Buffer2.prototype);
      return newBuf;
    }, "slice");
    function checkOffset(offset, ext, length) {
      if (offset % 1 !== 0 || offset < 0) throw new RangeError("offset is not uint");
      if (offset + ext > length) throw new RangeError("Trying to access beyond buffer length");
    }
    __name(checkOffset, "checkOffset");
    Buffer2.prototype.readUintLE = Buffer2.prototype.readUIntLE = /* @__PURE__ */ __name(function readUIntLE(offset, byteLength2, noAssert) {
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) checkOffset(offset, byteLength2, this.length);
      let val2 = this[offset];
      let mul = 1;
      let i = 0;
      while (++i < byteLength2 && (mul *= 256)) {
        val2 += this[offset + i] * mul;
      }
      return val2;
    }, "readUIntLE");
    Buffer2.prototype.readUintBE = Buffer2.prototype.readUIntBE = /* @__PURE__ */ __name(function readUIntBE(offset, byteLength2, noAssert) {
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) {
        checkOffset(offset, byteLength2, this.length);
      }
      let val2 = this[offset + --byteLength2];
      let mul = 1;
      while (byteLength2 > 0 && (mul *= 256)) {
        val2 += this[offset + --byteLength2] * mul;
      }
      return val2;
    }, "readUIntBE");
    Buffer2.prototype.readUint8 = Buffer2.prototype.readUInt8 = /* @__PURE__ */ __name(function readUInt8(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 1, this.length);
      return this[offset];
    }, "readUInt8");
    Buffer2.prototype.readUint16LE = Buffer2.prototype.readUInt16LE = /* @__PURE__ */ __name(function readUInt16LE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 2, this.length);
      return this[offset] | this[offset + 1] << 8;
    }, "readUInt16LE");
    Buffer2.prototype.readUint16BE = Buffer2.prototype.readUInt16BE = /* @__PURE__ */ __name(function readUInt16BE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 2, this.length);
      return this[offset] << 8 | this[offset + 1];
    }, "readUInt16BE");
    Buffer2.prototype.readUint32LE = Buffer2.prototype.readUInt32LE = /* @__PURE__ */ __name(function readUInt32LE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return (this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16) + this[offset + 3] * 16777216;
    }, "readUInt32LE");
    Buffer2.prototype.readUint32BE = Buffer2.prototype.readUInt32BE = /* @__PURE__ */ __name(function readUInt32BE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return this[offset] * 16777216 + (this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3]);
    }, "readUInt32BE");
    Buffer2.prototype.readBigUInt64LE = defineBigIntMethod(/* @__PURE__ */ __name(function readBigUInt64LE(offset) {
      offset = offset >>> 0;
      validateNumber(offset, "offset");
      const first2 = this[offset];
      const last2 = this[offset + 7];
      if (first2 === void 0 || last2 === void 0) {
        boundsError(offset, this.length - 8);
      }
      const lo = first2 + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24;
      const hi = this[++offset] + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + last2 * 2 ** 24;
      return BigInt(lo) + (BigInt(hi) << BigInt(32));
    }, "readBigUInt64LE"));
    Buffer2.prototype.readBigUInt64BE = defineBigIntMethod(/* @__PURE__ */ __name(function readBigUInt64BE(offset) {
      offset = offset >>> 0;
      validateNumber(offset, "offset");
      const first2 = this[offset];
      const last2 = this[offset + 7];
      if (first2 === void 0 || last2 === void 0) {
        boundsError(offset, this.length - 8);
      }
      const hi = first2 * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
      const lo = this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last2;
      return (BigInt(hi) << BigInt(32)) + BigInt(lo);
    }, "readBigUInt64BE"));
    Buffer2.prototype.readIntLE = /* @__PURE__ */ __name(function readIntLE(offset, byteLength2, noAssert) {
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) checkOffset(offset, byteLength2, this.length);
      let val2 = this[offset];
      let mul = 1;
      let i = 0;
      while (++i < byteLength2 && (mul *= 256)) {
        val2 += this[offset + i] * mul;
      }
      mul *= 128;
      if (val2 >= mul) val2 -= Math.pow(2, 8 * byteLength2);
      return val2;
    }, "readIntLE");
    Buffer2.prototype.readIntBE = /* @__PURE__ */ __name(function readIntBE(offset, byteLength2, noAssert) {
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) checkOffset(offset, byteLength2, this.length);
      let i = byteLength2;
      let mul = 1;
      let val2 = this[offset + --i];
      while (i > 0 && (mul *= 256)) {
        val2 += this[offset + --i] * mul;
      }
      mul *= 128;
      if (val2 >= mul) val2 -= Math.pow(2, 8 * byteLength2);
      return val2;
    }, "readIntBE");
    Buffer2.prototype.readInt8 = /* @__PURE__ */ __name(function readInt8(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 1, this.length);
      if (!(this[offset] & 128)) return this[offset];
      return (255 - this[offset] + 1) * -1;
    }, "readInt8");
    Buffer2.prototype.readInt16LE = /* @__PURE__ */ __name(function readInt16LE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 2, this.length);
      const val2 = this[offset] | this[offset + 1] << 8;
      return val2 & 32768 ? val2 | 4294901760 : val2;
    }, "readInt16LE");
    Buffer2.prototype.readInt16BE = /* @__PURE__ */ __name(function readInt16BE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 2, this.length);
      const val2 = this[offset + 1] | this[offset] << 8;
      return val2 & 32768 ? val2 | 4294901760 : val2;
    }, "readInt16BE");
    Buffer2.prototype.readInt32LE = /* @__PURE__ */ __name(function readInt32LE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16 | this[offset + 3] << 24;
    }, "readInt32LE");
    Buffer2.prototype.readInt32BE = /* @__PURE__ */ __name(function readInt32BE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return this[offset] << 24 | this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3];
    }, "readInt32BE");
    Buffer2.prototype.readBigInt64LE = defineBigIntMethod(/* @__PURE__ */ __name(function readBigInt64LE(offset) {
      offset = offset >>> 0;
      validateNumber(offset, "offset");
      const first2 = this[offset];
      const last2 = this[offset + 7];
      if (first2 === void 0 || last2 === void 0) {
        boundsError(offset, this.length - 8);
      }
      const val2 = this[offset + 4] + this[offset + 5] * 2 ** 8 + this[offset + 6] * 2 ** 16 + (last2 << 24);
      return (BigInt(val2) << BigInt(32)) + BigInt(first2 + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24);
    }, "readBigInt64LE"));
    Buffer2.prototype.readBigInt64BE = defineBigIntMethod(/* @__PURE__ */ __name(function readBigInt64BE(offset) {
      offset = offset >>> 0;
      validateNumber(offset, "offset");
      const first2 = this[offset];
      const last2 = this[offset + 7];
      if (first2 === void 0 || last2 === void 0) {
        boundsError(offset, this.length - 8);
      }
      const val2 = (first2 << 24) + // Overflow
      this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
      return (BigInt(val2) << BigInt(32)) + BigInt(this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last2);
    }, "readBigInt64BE"));
    Buffer2.prototype.readFloatLE = /* @__PURE__ */ __name(function readFloatLE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return ieee754.read(this, offset, true, 23, 4);
    }, "readFloatLE");
    Buffer2.prototype.readFloatBE = /* @__PURE__ */ __name(function readFloatBE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 4, this.length);
      return ieee754.read(this, offset, false, 23, 4);
    }, "readFloatBE");
    Buffer2.prototype.readDoubleLE = /* @__PURE__ */ __name(function readDoubleLE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 8, this.length);
      return ieee754.read(this, offset, true, 52, 8);
    }, "readDoubleLE");
    Buffer2.prototype.readDoubleBE = /* @__PURE__ */ __name(function readDoubleBE(offset, noAssert) {
      offset = offset >>> 0;
      if (!noAssert) checkOffset(offset, 8, this.length);
      return ieee754.read(this, offset, false, 52, 8);
    }, "readDoubleBE");
    function checkInt(buf, value, offset, ext, max, min) {
      if (!Buffer2.isBuffer(buf)) throw new TypeError('"buffer" argument must be a Buffer instance');
      if (value > max || value < min) throw new RangeError('"value" argument is out of bounds');
      if (offset + ext > buf.length) throw new RangeError("Index out of range");
    }
    __name(checkInt, "checkInt");
    Buffer2.prototype.writeUintLE = Buffer2.prototype.writeUIntLE = /* @__PURE__ */ __name(function writeUIntLE(value, offset, byteLength2, noAssert) {
      value = +value;
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) {
        const maxBytes = Math.pow(2, 8 * byteLength2) - 1;
        checkInt(this, value, offset, byteLength2, maxBytes, 0);
      }
      let mul = 1;
      let i = 0;
      this[offset] = value & 255;
      while (++i < byteLength2 && (mul *= 256)) {
        this[offset + i] = value / mul & 255;
      }
      return offset + byteLength2;
    }, "writeUIntLE");
    Buffer2.prototype.writeUintBE = Buffer2.prototype.writeUIntBE = /* @__PURE__ */ __name(function writeUIntBE(value, offset, byteLength2, noAssert) {
      value = +value;
      offset = offset >>> 0;
      byteLength2 = byteLength2 >>> 0;
      if (!noAssert) {
        const maxBytes = Math.pow(2, 8 * byteLength2) - 1;
        checkInt(this, value, offset, byteLength2, maxBytes, 0);
      }
      let i = byteLength2 - 1;
      let mul = 1;
      this[offset + i] = value & 255;
      while (--i >= 0 && (mul *= 256)) {
        this[offset + i] = value / mul & 255;
      }
      return offset + byteLength2;
    }, "writeUIntBE");
    Buffer2.prototype.writeUint8 = Buffer2.prototype.writeUInt8 = /* @__PURE__ */ __name(function writeUInt8(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 1, 255, 0);
      this[offset] = value & 255;
      return offset + 1;
    }, "writeUInt8");
    Buffer2.prototype.writeUint16LE = Buffer2.prototype.writeUInt16LE = /* @__PURE__ */ __name(function writeUInt16LE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
      this[offset] = value & 255;
      this[offset + 1] = value >>> 8;
      return offset + 2;
    }, "writeUInt16LE");
    Buffer2.prototype.writeUint16BE = Buffer2.prototype.writeUInt16BE = /* @__PURE__ */ __name(function writeUInt16BE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
      this[offset] = value >>> 8;
      this[offset + 1] = value & 255;
      return offset + 2;
    }, "writeUInt16BE");
    Buffer2.prototype.writeUint32LE = Buffer2.prototype.writeUInt32LE = /* @__PURE__ */ __name(function writeUInt32LE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
      this[offset + 3] = value >>> 24;
      this[offset + 2] = value >>> 16;
      this[offset + 1] = value >>> 8;
      this[offset] = value & 255;
      return offset + 4;
    }, "writeUInt32LE");
    Buffer2.prototype.writeUint32BE = Buffer2.prototype.writeUInt32BE = /* @__PURE__ */ __name(function writeUInt32BE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
      this[offset] = value >>> 24;
      this[offset + 1] = value >>> 16;
      this[offset + 2] = value >>> 8;
      this[offset + 3] = value & 255;
      return offset + 4;
    }, "writeUInt32BE");
    function wrtBigUInt64LE(buf, value, offset, min, max) {
      checkIntBI(value, min, max, buf, offset, 7);
      let lo = Number(value & BigInt(4294967295));
      buf[offset++] = lo;
      lo = lo >> 8;
      buf[offset++] = lo;
      lo = lo >> 8;
      buf[offset++] = lo;
      lo = lo >> 8;
      buf[offset++] = lo;
      let hi = Number(value >> BigInt(32) & BigInt(4294967295));
      buf[offset++] = hi;
      hi = hi >> 8;
      buf[offset++] = hi;
      hi = hi >> 8;
      buf[offset++] = hi;
      hi = hi >> 8;
      buf[offset++] = hi;
      return offset;
    }
    __name(wrtBigUInt64LE, "wrtBigUInt64LE");
    function wrtBigUInt64BE(buf, value, offset, min, max) {
      checkIntBI(value, min, max, buf, offset, 7);
      let lo = Number(value & BigInt(4294967295));
      buf[offset + 7] = lo;
      lo = lo >> 8;
      buf[offset + 6] = lo;
      lo = lo >> 8;
      buf[offset + 5] = lo;
      lo = lo >> 8;
      buf[offset + 4] = lo;
      let hi = Number(value >> BigInt(32) & BigInt(4294967295));
      buf[offset + 3] = hi;
      hi = hi >> 8;
      buf[offset + 2] = hi;
      hi = hi >> 8;
      buf[offset + 1] = hi;
      hi = hi >> 8;
      buf[offset] = hi;
      return offset + 8;
    }
    __name(wrtBigUInt64BE, "wrtBigUInt64BE");
    Buffer2.prototype.writeBigUInt64LE = defineBigIntMethod(/* @__PURE__ */ __name(function writeBigUInt64LE(value, offset = 0) {
      return wrtBigUInt64LE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
    }, "writeBigUInt64LE"));
    Buffer2.prototype.writeBigUInt64BE = defineBigIntMethod(/* @__PURE__ */ __name(function writeBigUInt64BE(value, offset = 0) {
      return wrtBigUInt64BE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
    }, "writeBigUInt64BE"));
    Buffer2.prototype.writeIntLE = /* @__PURE__ */ __name(function writeIntLE(value, offset, byteLength2, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) {
        const limit = Math.pow(2, 8 * byteLength2 - 1);
        checkInt(this, value, offset, byteLength2, limit - 1, -limit);
      }
      let i = 0;
      let mul = 1;
      let sub = 0;
      this[offset] = value & 255;
      while (++i < byteLength2 && (mul *= 256)) {
        if (value < 0 && sub === 0 && this[offset + i - 1] !== 0) {
          sub = 1;
        }
        this[offset + i] = (value / mul >> 0) - sub & 255;
      }
      return offset + byteLength2;
    }, "writeIntLE");
    Buffer2.prototype.writeIntBE = /* @__PURE__ */ __name(function writeIntBE(value, offset, byteLength2, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) {
        const limit = Math.pow(2, 8 * byteLength2 - 1);
        checkInt(this, value, offset, byteLength2, limit - 1, -limit);
      }
      let i = byteLength2 - 1;
      let mul = 1;
      let sub = 0;
      this[offset + i] = value & 255;
      while (--i >= 0 && (mul *= 256)) {
        if (value < 0 && sub === 0 && this[offset + i + 1] !== 0) {
          sub = 1;
        }
        this[offset + i] = (value / mul >> 0) - sub & 255;
      }
      return offset + byteLength2;
    }, "writeIntBE");
    Buffer2.prototype.writeInt8 = /* @__PURE__ */ __name(function writeInt8(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 1, 127, -128);
      if (value < 0) value = 255 + value + 1;
      this[offset] = value & 255;
      return offset + 1;
    }, "writeInt8");
    Buffer2.prototype.writeInt16LE = /* @__PURE__ */ __name(function writeInt16LE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
      this[offset] = value & 255;
      this[offset + 1] = value >>> 8;
      return offset + 2;
    }, "writeInt16LE");
    Buffer2.prototype.writeInt16BE = /* @__PURE__ */ __name(function writeInt16BE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
      this[offset] = value >>> 8;
      this[offset + 1] = value & 255;
      return offset + 2;
    }, "writeInt16BE");
    Buffer2.prototype.writeInt32LE = /* @__PURE__ */ __name(function writeInt32LE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
      this[offset] = value & 255;
      this[offset + 1] = value >>> 8;
      this[offset + 2] = value >>> 16;
      this[offset + 3] = value >>> 24;
      return offset + 4;
    }, "writeInt32LE");
    Buffer2.prototype.writeInt32BE = /* @__PURE__ */ __name(function writeInt32BE(value, offset, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
      if (value < 0) value = 4294967295 + value + 1;
      this[offset] = value >>> 24;
      this[offset + 1] = value >>> 16;
      this[offset + 2] = value >>> 8;
      this[offset + 3] = value & 255;
      return offset + 4;
    }, "writeInt32BE");
    Buffer2.prototype.writeBigInt64LE = defineBigIntMethod(/* @__PURE__ */ __name(function writeBigInt64LE(value, offset = 0) {
      return wrtBigUInt64LE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
    }, "writeBigInt64LE"));
    Buffer2.prototype.writeBigInt64BE = defineBigIntMethod(/* @__PURE__ */ __name(function writeBigInt64BE(value, offset = 0) {
      return wrtBigUInt64BE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
    }, "writeBigInt64BE"));
    function checkIEEE754(buf, value, offset, ext, max, min) {
      if (offset + ext > buf.length) throw new RangeError("Index out of range");
      if (offset < 0) throw new RangeError("Index out of range");
    }
    __name(checkIEEE754, "checkIEEE754");
    function writeFloat(buf, value, offset, littleEndian, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) {
        checkIEEE754(buf, value, offset, 4);
      }
      ieee754.write(buf, value, offset, littleEndian, 23, 4);
      return offset + 4;
    }
    __name(writeFloat, "writeFloat");
    Buffer2.prototype.writeFloatLE = /* @__PURE__ */ __name(function writeFloatLE(value, offset, noAssert) {
      return writeFloat(this, value, offset, true, noAssert);
    }, "writeFloatLE");
    Buffer2.prototype.writeFloatBE = /* @__PURE__ */ __name(function writeFloatBE(value, offset, noAssert) {
      return writeFloat(this, value, offset, false, noAssert);
    }, "writeFloatBE");
    function writeDouble(buf, value, offset, littleEndian, noAssert) {
      value = +value;
      offset = offset >>> 0;
      if (!noAssert) {
        checkIEEE754(buf, value, offset, 8);
      }
      ieee754.write(buf, value, offset, littleEndian, 52, 8);
      return offset + 8;
    }
    __name(writeDouble, "writeDouble");
    Buffer2.prototype.writeDoubleLE = /* @__PURE__ */ __name(function writeDoubleLE(value, offset, noAssert) {
      return writeDouble(this, value, offset, true, noAssert);
    }, "writeDoubleLE");
    Buffer2.prototype.writeDoubleBE = /* @__PURE__ */ __name(function writeDoubleBE(value, offset, noAssert) {
      return writeDouble(this, value, offset, false, noAssert);
    }, "writeDoubleBE");
    Buffer2.prototype.copy = /* @__PURE__ */ __name(function copy(target, targetStart, start, end2) {
      if (!Buffer2.isBuffer(target)) throw new TypeError("argument should be a Buffer");
      if (!start) start = 0;
      if (!end2 && end2 !== 0) end2 = this.length;
      if (targetStart >= target.length) targetStart = target.length;
      if (!targetStart) targetStart = 0;
      if (end2 > 0 && end2 < start) end2 = start;
      if (end2 === start) return 0;
      if (target.length === 0 || this.length === 0) return 0;
      if (targetStart < 0) {
        throw new RangeError("targetStart out of bounds");
      }
      if (start < 0 || start >= this.length) throw new RangeError("Index out of range");
      if (end2 < 0) throw new RangeError("sourceEnd out of bounds");
      if (end2 > this.length) end2 = this.length;
      if (target.length - targetStart < end2 - start) {
        end2 = target.length - targetStart + start;
      }
      const len = end2 - start;
      if (this === target && typeof Uint8Array.prototype.copyWithin === "function") {
        this.copyWithin(targetStart, start, end2);
      } else {
        Uint8Array.prototype.set.call(target, this.subarray(start, end2), targetStart);
      }
      return len;
    }, "copy");
    Buffer2.prototype.fill = /* @__PURE__ */ __name(function fill(val2, start, end2, encoding) {
      if (typeof val2 === "string") {
        if (typeof start === "string") {
          encoding = start;
          start = 0;
          end2 = this.length;
        } else if (typeof end2 === "string") {
          encoding = end2;
          end2 = this.length;
        }
        if (encoding !== void 0 && typeof encoding !== "string") {
          throw new TypeError("encoding must be a string");
        }
        if (typeof encoding === "string" && !Buffer2.isEncoding(encoding)) {
          throw new TypeError("Unknown encoding: " + encoding);
        }
        if (val2.length === 1) {
          const code = val2.charCodeAt(0);
          if (encoding === "utf8" && code < 128 || encoding === "latin1") {
            val2 = code;
          }
        }
      } else if (typeof val2 === "number") {
        val2 = val2 & 255;
      } else if (typeof val2 === "boolean") {
        val2 = Number(val2);
      }
      if (start < 0 || this.length < start || this.length < end2) {
        throw new RangeError("Out of range index");
      }
      if (end2 <= start) {
        return this;
      }
      start = start >>> 0;
      end2 = end2 === void 0 ? this.length : end2 >>> 0;
      if (!val2) val2 = 0;
      let i;
      if (typeof val2 === "number") {
        for (i = start; i < end2; ++i) {
          this[i] = val2;
        }
      } else {
        const bytes = Buffer2.isBuffer(val2) ? val2 : Buffer2.from(val2, encoding);
        const len = bytes.length;
        if (len === 0) {
          throw new TypeError('The value "' + val2 + '" is invalid for argument "value"');
        }
        for (i = 0; i < end2 - start; ++i) {
          this[i + start] = bytes[i % len];
        }
      }
      return this;
    }, "fill");
    const errors = {};
    function E(sym, getMessage, Base) {
      var _a;
      errors[sym] = (_a = class extends Base {
        constructor() {
          super();
          Object.defineProperty(this, "message", {
            value: getMessage.apply(this, arguments),
            writable: true,
            configurable: true
          });
          this.name = `${this.name} [${sym}]`;
          this.stack;
          delete this.name;
        }
        get code() {
          return sym;
        }
        set code(value) {
          Object.defineProperty(this, "code", {
            configurable: true,
            enumerable: true,
            value,
            writable: true
          });
        }
        toString() {
          return `${this.name} [${sym}]: ${this.message}`;
        }
      }, __name(_a, "NodeError"), _a);
    }
    __name(E, "E");
    E("ERR_BUFFER_OUT_OF_BOUNDS", function(name) {
      if (name) {
        return `${name} is outside of buffer bounds`;
      }
      return "Attempt to access memory outside buffer bounds";
    }, RangeError);
    E("ERR_INVALID_ARG_TYPE", function(name, actual) {
      return `The "${name}" argument must be of type number. Received type ${typeof actual}`;
    }, TypeError);
    E("ERR_OUT_OF_RANGE", function(str, range, input) {
      let msg = `The value of "${str}" is out of range.`;
      let received = input;
      if (Number.isInteger(input) && Math.abs(input) > 2 ** 32) {
        received = addNumericalSeparator(String(input));
      } else if (typeof input === "bigint") {
        received = String(input);
        if (input > BigInt(2) ** BigInt(32) || input < -(BigInt(2) ** BigInt(32))) {
          received = addNumericalSeparator(received);
        }
        received += "n";
      }
      msg += ` It must be ${range}. Received ${received}`;
      return msg;
    }, RangeError);
    function addNumericalSeparator(val2) {
      let res = "";
      let i = val2.length;
      const start = val2[0] === "-" ? 1 : 0;
      for (; i >= start + 4; i -= 3) {
        res = `_${val2.slice(i - 3, i)}${res}`;
      }
      return `${val2.slice(0, i)}${res}`;
    }
    __name(addNumericalSeparator, "addNumericalSeparator");
    function checkBounds(buf, offset, byteLength2) {
      validateNumber(offset, "offset");
      if (buf[offset] === void 0 || buf[offset + byteLength2] === void 0) {
        boundsError(offset, buf.length - (byteLength2 + 1));
      }
    }
    __name(checkBounds, "checkBounds");
    function checkIntBI(value, min, max, buf, offset, byteLength2) {
      if (value > max || value < min) {
        const n2 = typeof min === "bigint" ? "n" : "";
        let range;
        {
          if (min === 0 || min === BigInt(0)) {
            range = `>= 0${n2} and < 2${n2} ** ${(byteLength2 + 1) * 8}${n2}`;
          } else {
            range = `>= -(2${n2} ** ${(byteLength2 + 1) * 8 - 1}${n2}) and < 2 ** ${(byteLength2 + 1) * 8 - 1}${n2}`;
          }
        }
        throw new errors.ERR_OUT_OF_RANGE("value", range, value);
      }
      checkBounds(buf, offset, byteLength2);
    }
    __name(checkIntBI, "checkIntBI");
    function validateNumber(value, name) {
      if (typeof value !== "number") {
        throw new errors.ERR_INVALID_ARG_TYPE(name, "number", value);
      }
    }
    __name(validateNumber, "validateNumber");
    function boundsError(value, length, type) {
      if (Math.floor(value) !== value) {
        validateNumber(value, type);
        throw new errors.ERR_OUT_OF_RANGE("offset", "an integer", value);
      }
      if (length < 0) {
        throw new errors.ERR_BUFFER_OUT_OF_BOUNDS();
      }
      throw new errors.ERR_OUT_OF_RANGE("offset", `>= ${0} and <= ${length}`, value);
    }
    __name(boundsError, "boundsError");
    const INVALID_BASE64_RE = /[^+/0-9A-Za-z-_]/g;
    function base64clean(str) {
      str = str.split("=")[0];
      str = str.trim().replace(INVALID_BASE64_RE, "");
      if (str.length < 2) return "";
      while (str.length % 4 !== 0) {
        str = str + "=";
      }
      return str;
    }
    __name(base64clean, "base64clean");
    function utf8ToBytes(string, units) {
      units = units || Infinity;
      let codePoint;
      const length = string.length;
      let leadSurrogate = null;
      const bytes = [];
      for (let i = 0; i < length; ++i) {
        codePoint = string.charCodeAt(i);
        if (codePoint > 55295 && codePoint < 57344) {
          if (!leadSurrogate) {
            if (codePoint > 56319) {
              if ((units -= 3) > -1) bytes.push(239, 191, 189);
              continue;
            } else if (i + 1 === length) {
              if ((units -= 3) > -1) bytes.push(239, 191, 189);
              continue;
            }
            leadSurrogate = codePoint;
            continue;
          }
          if (codePoint < 56320) {
            if ((units -= 3) > -1) bytes.push(239, 191, 189);
            leadSurrogate = codePoint;
            continue;
          }
          codePoint = (leadSurrogate - 55296 << 10 | codePoint - 56320) + 65536;
        } else if (leadSurrogate) {
          if ((units -= 3) > -1) bytes.push(239, 191, 189);
        }
        leadSurrogate = null;
        if (codePoint < 128) {
          if ((units -= 1) < 0) break;
          bytes.push(codePoint);
        } else if (codePoint < 2048) {
          if ((units -= 2) < 0) break;
          bytes.push(codePoint >> 6 | 192, codePoint & 63 | 128);
        } else if (codePoint < 65536) {
          if ((units -= 3) < 0) break;
          bytes.push(codePoint >> 12 | 224, codePoint >> 6 & 63 | 128, codePoint & 63 | 128);
        } else if (codePoint < 1114112) {
          if ((units -= 4) < 0) break;
          bytes.push(codePoint >> 18 | 240, codePoint >> 12 & 63 | 128, codePoint >> 6 & 63 | 128, codePoint & 63 | 128);
        } else {
          throw new Error("Invalid code point");
        }
      }
      return bytes;
    }
    __name(utf8ToBytes, "utf8ToBytes");
    function asciiToBytes(str) {
      const byteArray = [];
      for (let i = 0; i < str.length; ++i) {
        byteArray.push(str.charCodeAt(i) & 255);
      }
      return byteArray;
    }
    __name(asciiToBytes, "asciiToBytes");
    function utf16leToBytes(str, units) {
      let c, hi, lo;
      const byteArray = [];
      for (let i = 0; i < str.length; ++i) {
        if ((units -= 2) < 0) break;
        c = str.charCodeAt(i);
        hi = c >> 8;
        lo = c % 256;
        byteArray.push(lo);
        byteArray.push(hi);
      }
      return byteArray;
    }
    __name(utf16leToBytes, "utf16leToBytes");
    function base64ToBytes(str) {
      return base64.toByteArray(base64clean(str));
    }
    __name(base64ToBytes, "base64ToBytes");
    function blitBuffer(src, dst, offset, length) {
      let i;
      for (i = 0; i < length; ++i) {
        if (i + offset >= dst.length || i >= src.length) break;
        dst[i + offset] = src[i];
      }
      return i;
    }
    __name(blitBuffer, "blitBuffer");
    function isInstance(obj, type) {
      return obj instanceof type || obj != null && obj.constructor != null && obj.constructor.name != null && obj.constructor.name === type.name;
    }
    __name(isInstance, "isInstance");
    function numberIsNaN(obj) {
      return obj !== obj;
    }
    __name(numberIsNaN, "numberIsNaN");
    const hexSliceLookupTable = function() {
      const alphabet = "0123456789abcdef";
      const table = new Array(256);
      for (let i = 0; i < 16; ++i) {
        const i16 = i * 16;
        for (let j = 0; j < 16; ++j) {
          table[i16 + j] = alphabet[i] + alphabet[j];
        }
      }
      return table;
    }();
    function defineBigIntMethod(fn) {
      return typeof BigInt === "undefined" ? BufferBigIntNotDefined : fn;
    }
    __name(defineBigIntMethod, "defineBigIntMethod");
    function BufferBigIntNotDefined() {
      throw new Error("BigInt not supported");
    }
    __name(BufferBigIntNotDefined, "BufferBigIntNotDefined");
    return exports2;
  }
  var exports$2, _dewExec$2, exports$1, _dewExec$1, exports2, _dewExec;
  var init_chunk_DtuTasat = __esm({
    "../node_modules/@jspm/core/nodelibs/browser/chunk-DtuTasat.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      exports$2 = {}, _dewExec$2 = false;
      __name(dew$2, "dew$2");
      exports$1 = {}, _dewExec$1 = false;
      __name(dew$1, "dew$1");
      exports2 = {}, _dewExec = false;
      __name(dew, "dew");
    }
  });

  // ../node_modules/@jspm/core/nodelibs/browser/buffer.js
  var exports3, Buffer, INSPECT_MAX_BYTES, kMaxLength;
  var init_buffer = __esm({
    "../node_modules/@jspm/core/nodelibs/browser/buffer.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_chunk_DtuTasat();
      exports3 = dew();
      exports3["Buffer"];
      exports3["SlowBuffer"];
      exports3["INSPECT_MAX_BYTES"];
      exports3["kMaxLength"];
      Buffer = exports3.Buffer;
      INSPECT_MAX_BYTES = exports3.INSPECT_MAX_BYTES;
      kMaxLength = exports3.kMaxLength;
    }
  });

  // ../node_modules/esbuild-plugin-polyfill-node/polyfills/buffer.js
  var init_buffer2 = __esm({
    "../node_modules/esbuild-plugin-polyfill-node/polyfills/buffer.js"() {
      init_buffer();
    }
  });

  // node_modules/domelementtype/lib/index.js
  var require_lib = __commonJS({
    "node_modules/domelementtype/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.Doctype = exports4.CDATA = exports4.Tag = exports4.Style = exports4.Script = exports4.Comment = exports4.Directive = exports4.Text = exports4.Root = exports4.isTag = exports4.ElementType = void 0;
      var ElementType;
      (function(ElementType2) {
        ElementType2["Root"] = "root";
        ElementType2["Text"] = "text";
        ElementType2["Directive"] = "directive";
        ElementType2["Comment"] = "comment";
        ElementType2["Script"] = "script";
        ElementType2["Style"] = "style";
        ElementType2["Tag"] = "tag";
        ElementType2["CDATA"] = "cdata";
        ElementType2["Doctype"] = "doctype";
      })(ElementType = exports4.ElementType || (exports4.ElementType = {}));
      function isTag7(elem) {
        return elem.type === ElementType.Tag || elem.type === ElementType.Script || elem.type === ElementType.Style;
      }
      __name(isTag7, "isTag");
      exports4.isTag = isTag7;
      exports4.Root = ElementType.Root;
      exports4.Text = ElementType.Text;
      exports4.Directive = ElementType.Directive;
      exports4.Comment = ElementType.Comment;
      exports4.Script = ElementType.Script;
      exports4.Style = ElementType.Style;
      exports4.Tag = ElementType.Tag;
      exports4.CDATA = ElementType.CDATA;
      exports4.Doctype = ElementType.Doctype;
    }
  });

  // node_modules/domhandler/lib/node.js
  var require_node = __commonJS({
    "node_modules/domhandler/lib/node.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __extends = exports4 && exports4.__extends || /* @__PURE__ */ function() {
        var extendStatics = /* @__PURE__ */ __name(function(d, b) {
          extendStatics = Object.setPrototypeOf || { __proto__: [] } instanceof Array && function(d2, b2) {
            d2.__proto__ = b2;
          } || function(d2, b2) {
            for (var p in b2) if (Object.prototype.hasOwnProperty.call(b2, p)) d2[p] = b2[p];
          };
          return extendStatics(d, b);
        }, "extendStatics");
        return function(d, b) {
          if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
          extendStatics(d, b);
          function __() {
            this.constructor = d;
          }
          __name(__, "__");
          d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
        };
      }();
      var __assign = exports4 && exports4.__assign || function() {
        __assign = Object.assign || function(t2) {
          for (var s, i = 1, n2 = arguments.length; i < n2; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
              t2[p] = s[p];
          }
          return t2;
        };
        return __assign.apply(this, arguments);
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.cloneNode = exports4.hasChildren = exports4.isDocument = exports4.isDirective = exports4.isComment = exports4.isText = exports4.isCDATA = exports4.isTag = exports4.Element = exports4.Document = exports4.CDATA = exports4.NodeWithChildren = exports4.ProcessingInstruction = exports4.Comment = exports4.Text = exports4.DataNode = exports4.Node = void 0;
      var domelementtype_1 = require_lib();
      var Node = (
        /** @class */
        function() {
          function Node2() {
            this.parent = null;
            this.prev = null;
            this.next = null;
            this.startIndex = null;
            this.endIndex = null;
          }
          __name(Node2, "Node");
          Object.defineProperty(Node2.prototype, "parentNode", {
            // Read-write aliases for properties
            /**
             * Same as {@link parent}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.parent;
            }, "get"),
            set: /* @__PURE__ */ __name(function(parent2) {
              this.parent = parent2;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(Node2.prototype, "previousSibling", {
            /**
             * Same as {@link prev}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.prev;
            }, "get"),
            set: /* @__PURE__ */ __name(function(prev2) {
              this.prev = prev2;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(Node2.prototype, "nextSibling", {
            /**
             * Same as {@link next}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.next;
            }, "get"),
            set: /* @__PURE__ */ __name(function(next2) {
              this.next = next2;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          Node2.prototype.cloneNode = function(recursive) {
            if (recursive === void 0) {
              recursive = false;
            }
            return cloneNode2(this, recursive);
          };
          return Node2;
        }()
      );
      exports4.Node = Node;
      var DataNode = (
        /** @class */
        function(_super) {
          __extends(DataNode2, _super);
          function DataNode2(data2) {
            var _this = _super.call(this) || this;
            _this.data = data2;
            return _this;
          }
          __name(DataNode2, "DataNode");
          Object.defineProperty(DataNode2.prototype, "nodeValue", {
            /**
             * Same as {@link data}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.data;
            }, "get"),
            set: /* @__PURE__ */ __name(function(data2) {
              this.data = data2;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          return DataNode2;
        }(Node)
      );
      exports4.DataNode = DataNode;
      var Text3 = (
        /** @class */
        function(_super) {
          __extends(Text4, _super);
          function Text4() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.type = domelementtype_1.ElementType.Text;
            return _this;
          }
          __name(Text4, "Text");
          Object.defineProperty(Text4.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 3;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return Text4;
        }(DataNode)
      );
      exports4.Text = Text3;
      var Comment2 = (
        /** @class */
        function(_super) {
          __extends(Comment3, _super);
          function Comment3() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.type = domelementtype_1.ElementType.Comment;
            return _this;
          }
          __name(Comment3, "Comment");
          Object.defineProperty(Comment3.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 8;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return Comment3;
        }(DataNode)
      );
      exports4.Comment = Comment2;
      var ProcessingInstruction2 = (
        /** @class */
        function(_super) {
          __extends(ProcessingInstruction3, _super);
          function ProcessingInstruction3(name, data2) {
            var _this = _super.call(this, data2) || this;
            _this.name = name;
            _this.type = domelementtype_1.ElementType.Directive;
            return _this;
          }
          __name(ProcessingInstruction3, "ProcessingInstruction");
          Object.defineProperty(ProcessingInstruction3.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 1;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return ProcessingInstruction3;
        }(DataNode)
      );
      exports4.ProcessingInstruction = ProcessingInstruction2;
      var NodeWithChildren = (
        /** @class */
        function(_super) {
          __extends(NodeWithChildren2, _super);
          function NodeWithChildren2(children2) {
            var _this = _super.call(this) || this;
            _this.children = children2;
            return _this;
          }
          __name(NodeWithChildren2, "NodeWithChildren");
          Object.defineProperty(NodeWithChildren2.prototype, "firstChild", {
            // Aliases
            /** First child of the node. */
            get: /* @__PURE__ */ __name(function() {
              var _a;
              return (_a = this.children[0]) !== null && _a !== void 0 ? _a : null;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(NodeWithChildren2.prototype, "lastChild", {
            /** Last child of the node. */
            get: /* @__PURE__ */ __name(function() {
              return this.children.length > 0 ? this.children[this.children.length - 1] : null;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(NodeWithChildren2.prototype, "childNodes", {
            /**
             * Same as {@link children}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.children;
            }, "get"),
            set: /* @__PURE__ */ __name(function(children2) {
              this.children = children2;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          return NodeWithChildren2;
        }(Node)
      );
      exports4.NodeWithChildren = NodeWithChildren;
      var CDATA = (
        /** @class */
        function(_super) {
          __extends(CDATA2, _super);
          function CDATA2() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.type = domelementtype_1.ElementType.CDATA;
            return _this;
          }
          __name(CDATA2, "CDATA");
          Object.defineProperty(CDATA2.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 4;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return CDATA2;
        }(NodeWithChildren)
      );
      exports4.CDATA = CDATA;
      var Document4 = (
        /** @class */
        function(_super) {
          __extends(Document5, _super);
          function Document5() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.type = domelementtype_1.ElementType.Root;
            return _this;
          }
          __name(Document5, "Document");
          Object.defineProperty(Document5.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 9;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return Document5;
        }(NodeWithChildren)
      );
      exports4.Document = Document4;
      var Element2 = (
        /** @class */
        function(_super) {
          __extends(Element3, _super);
          function Element3(name, attribs, children2, type) {
            if (children2 === void 0) {
              children2 = [];
            }
            if (type === void 0) {
              type = name === "script" ? domelementtype_1.ElementType.Script : name === "style" ? domelementtype_1.ElementType.Style : domelementtype_1.ElementType.Tag;
            }
            var _this = _super.call(this, children2) || this;
            _this.name = name;
            _this.attribs = attribs;
            _this.type = type;
            return _this;
          }
          __name(Element3, "Element");
          Object.defineProperty(Element3.prototype, "nodeType", {
            get: /* @__PURE__ */ __name(function() {
              return 1;
            }, "get"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(Element3.prototype, "tagName", {
            // DOM Level 1 aliases
            /**
             * Same as {@link name}.
             * [DOM spec](https://dom.spec.whatwg.org)-compatible alias.
             */
            get: /* @__PURE__ */ __name(function() {
              return this.name;
            }, "get"),
            set: /* @__PURE__ */ __name(function(name) {
              this.name = name;
            }, "set"),
            enumerable: false,
            configurable: true
          });
          Object.defineProperty(Element3.prototype, "attributes", {
            get: /* @__PURE__ */ __name(function() {
              var _this = this;
              return Object.keys(this.attribs).map(function(name) {
                var _a, _b;
                return {
                  name,
                  value: _this.attribs[name],
                  namespace: (_a = _this["x-attribsNamespace"]) === null || _a === void 0 ? void 0 : _a[name],
                  prefix: (_b = _this["x-attribsPrefix"]) === null || _b === void 0 ? void 0 : _b[name]
                };
              });
            }, "get"),
            enumerable: false,
            configurable: true
          });
          return Element3;
        }(NodeWithChildren)
      );
      exports4.Element = Element2;
      function isTag7(node) {
        return (0, domelementtype_1.isTag)(node);
      }
      __name(isTag7, "isTag");
      exports4.isTag = isTag7;
      function isCDATA(node) {
        return node.type === domelementtype_1.ElementType.CDATA;
      }
      __name(isCDATA, "isCDATA");
      exports4.isCDATA = isCDATA;
      function isText2(node) {
        return node.type === domelementtype_1.ElementType.Text;
      }
      __name(isText2, "isText");
      exports4.isText = isText2;
      function isComment2(node) {
        return node.type === domelementtype_1.ElementType.Comment;
      }
      __name(isComment2, "isComment");
      exports4.isComment = isComment2;
      function isDirective2(node) {
        return node.type === domelementtype_1.ElementType.Directive;
      }
      __name(isDirective2, "isDirective");
      exports4.isDirective = isDirective2;
      function isDocument3(node) {
        return node.type === domelementtype_1.ElementType.Root;
      }
      __name(isDocument3, "isDocument");
      exports4.isDocument = isDocument3;
      function hasChildren3(node) {
        return Object.prototype.hasOwnProperty.call(node, "children");
      }
      __name(hasChildren3, "hasChildren");
      exports4.hasChildren = hasChildren3;
      function cloneNode2(node, recursive) {
        if (recursive === void 0) {
          recursive = false;
        }
        var result;
        if (isText2(node)) {
          result = new Text3(node.data);
        } else if (isComment2(node)) {
          result = new Comment2(node.data);
        } else if (isTag7(node)) {
          var children2 = recursive ? cloneChildren(node.children) : [];
          var clone_1 = new Element2(node.name, __assign({}, node.attribs), children2);
          children2.forEach(function(child) {
            return child.parent = clone_1;
          });
          if (node.namespace != null) {
            clone_1.namespace = node.namespace;
          }
          if (node["x-attribsNamespace"]) {
            clone_1["x-attribsNamespace"] = __assign({}, node["x-attribsNamespace"]);
          }
          if (node["x-attribsPrefix"]) {
            clone_1["x-attribsPrefix"] = __assign({}, node["x-attribsPrefix"]);
          }
          result = clone_1;
        } else if (isCDATA(node)) {
          var children2 = recursive ? cloneChildren(node.children) : [];
          var clone_2 = new CDATA(children2);
          children2.forEach(function(child) {
            return child.parent = clone_2;
          });
          result = clone_2;
        } else if (isDocument3(node)) {
          var children2 = recursive ? cloneChildren(node.children) : [];
          var clone_3 = new Document4(children2);
          children2.forEach(function(child) {
            return child.parent = clone_3;
          });
          if (node["x-mode"]) {
            clone_3["x-mode"] = node["x-mode"];
          }
          result = clone_3;
        } else if (isDirective2(node)) {
          var instruction = new ProcessingInstruction2(node.name, node.data);
          if (node["x-name"] != null) {
            instruction["x-name"] = node["x-name"];
            instruction["x-publicId"] = node["x-publicId"];
            instruction["x-systemId"] = node["x-systemId"];
          }
          result = instruction;
        } else {
          throw new Error("Not implemented yet: ".concat(node.type));
        }
        result.startIndex = node.startIndex;
        result.endIndex = node.endIndex;
        if (node.sourceCodeLocation != null) {
          result.sourceCodeLocation = node.sourceCodeLocation;
        }
        return result;
      }
      __name(cloneNode2, "cloneNode");
      exports4.cloneNode = cloneNode2;
      function cloneChildren(childs) {
        var children2 = childs.map(function(child) {
          return cloneNode2(child, true);
        });
        for (var i = 1; i < children2.length; i++) {
          children2[i].prev = children2[i - 1];
          children2[i - 1].next = children2[i];
        }
        return children2;
      }
      __name(cloneChildren, "cloneChildren");
    }
  });

  // node_modules/domhandler/lib/index.js
  var require_lib2 = __commonJS({
    "node_modules/domhandler/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __exportStar = exports4 && exports4.__exportStar || function(m, exports5) {
        for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports5, p)) __createBinding(exports5, m, p);
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.DomHandler = void 0;
      var domelementtype_1 = require_lib();
      var node_js_1 = require_node();
      __exportStar(require_node(), exports4);
      var defaultOpts3 = {
        withStartIndices: false,
        withEndIndices: false,
        xmlMode: false
      };
      var DomHandler = (
        /** @class */
        function() {
          function DomHandler2(callback, options, elementCB) {
            this.dom = [];
            this.root = new node_js_1.Document(this.dom);
            this.done = false;
            this.tagStack = [this.root];
            this.lastNode = null;
            this.parser = null;
            if (typeof options === "function") {
              elementCB = options;
              options = defaultOpts3;
            }
            if (typeof callback === "object") {
              options = callback;
              callback = void 0;
            }
            this.callback = callback !== null && callback !== void 0 ? callback : null;
            this.options = options !== null && options !== void 0 ? options : defaultOpts3;
            this.elementCB = elementCB !== null && elementCB !== void 0 ? elementCB : null;
          }
          __name(DomHandler2, "DomHandler");
          DomHandler2.prototype.onparserinit = function(parser) {
            this.parser = parser;
          };
          DomHandler2.prototype.onreset = function() {
            this.dom = [];
            this.root = new node_js_1.Document(this.dom);
            this.done = false;
            this.tagStack = [this.root];
            this.lastNode = null;
            this.parser = null;
          };
          DomHandler2.prototype.onend = function() {
            if (this.done)
              return;
            this.done = true;
            this.parser = null;
            this.handleCallback(null);
          };
          DomHandler2.prototype.onerror = function(error) {
            this.handleCallback(error);
          };
          DomHandler2.prototype.onclosetag = function() {
            this.lastNode = null;
            var elem = this.tagStack.pop();
            if (this.options.withEndIndices) {
              elem.endIndex = this.parser.endIndex;
            }
            if (this.elementCB)
              this.elementCB(elem);
          };
          DomHandler2.prototype.onopentag = function(name, attribs) {
            var type = this.options.xmlMode ? domelementtype_1.ElementType.Tag : void 0;
            var element = new node_js_1.Element(name, attribs, void 0, type);
            this.addNode(element);
            this.tagStack.push(element);
          };
          DomHandler2.prototype.ontext = function(data2) {
            var lastNode = this.lastNode;
            if (lastNode && lastNode.type === domelementtype_1.ElementType.Text) {
              lastNode.data += data2;
              if (this.options.withEndIndices) {
                lastNode.endIndex = this.parser.endIndex;
              }
            } else {
              var node = new node_js_1.Text(data2);
              this.addNode(node);
              this.lastNode = node;
            }
          };
          DomHandler2.prototype.oncomment = function(data2) {
            if (this.lastNode && this.lastNode.type === domelementtype_1.ElementType.Comment) {
              this.lastNode.data += data2;
              return;
            }
            var node = new node_js_1.Comment(data2);
            this.addNode(node);
            this.lastNode = node;
          };
          DomHandler2.prototype.oncommentend = function() {
            this.lastNode = null;
          };
          DomHandler2.prototype.oncdatastart = function() {
            var text3 = new node_js_1.Text("");
            var node = new node_js_1.CDATA([text3]);
            this.addNode(node);
            text3.parent = node;
            this.lastNode = text3;
          };
          DomHandler2.prototype.oncdataend = function() {
            this.lastNode = null;
          };
          DomHandler2.prototype.onprocessinginstruction = function(name, data2) {
            var node = new node_js_1.ProcessingInstruction(name, data2);
            this.addNode(node);
          };
          DomHandler2.prototype.handleCallback = function(error) {
            if (typeof this.callback === "function") {
              this.callback(error, this.dom);
            } else if (error) {
              throw error;
            }
          };
          DomHandler2.prototype.addNode = function(node) {
            var parent2 = this.tagStack[this.tagStack.length - 1];
            var previousSibling = parent2.children[parent2.children.length - 1];
            if (this.options.withStartIndices) {
              node.startIndex = this.parser.startIndex;
            }
            if (this.options.withEndIndices) {
              node.endIndex = this.parser.endIndex;
            }
            parent2.children.push(node);
            if (previousSibling) {
              node.prev = previousSibling;
              previousSibling.next = node;
            }
            node.parent = parent2;
            this.lastNode = null;
          };
          return DomHandler2;
        }()
      );
      exports4.DomHandler = DomHandler;
      exports4.default = DomHandler;
    }
  });

  // node_modules/entities/lib/generated/decode-data-html.js
  var require_decode_data_html = __commonJS({
    "node_modules/entities/lib/generated/decode-data-html.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.default = new Uint16Array(
        // prettier-ignore
        '\u1D41<\xD5\u0131\u028A\u049D\u057B\u05D0\u0675\u06DE\u07A2\u07D6\u080F\u0A4A\u0A91\u0DA1\u0E6D\u0F09\u0F26\u10CA\u1228\u12E1\u1415\u149D\u14C3\u14DF\u1525\0\0\0\0\0\0\u156B\u16CD\u198D\u1C12\u1DDD\u1F7E\u2060\u21B0\u228D\u23C0\u23FB\u2442\u2824\u2912\u2D08\u2E48\u2FCE\u3016\u32BA\u3639\u37AC\u38FE\u3A28\u3A71\u3AE0\u3B2E\u0800EMabcfglmnoprstu\\bfms\x7F\x84\x8B\x90\x95\x98\xA6\xB3\xB9\xC8\xCFlig\u803B\xC6\u40C6P\u803B&\u4026cute\u803B\xC1\u40C1reve;\u4102\u0100iyx}rc\u803B\xC2\u40C2;\u4410r;\uC000\u{1D504}rave\u803B\xC0\u40C0pha;\u4391acr;\u4100d;\u6A53\u0100gp\x9D\xA1on;\u4104f;\uC000\u{1D538}plyFunction;\u6061ing\u803B\xC5\u40C5\u0100cs\xBE\xC3r;\uC000\u{1D49C}ign;\u6254ilde\u803B\xC3\u40C3ml\u803B\xC4\u40C4\u0400aceforsu\xE5\xFB\xFE\u0117\u011C\u0122\u0127\u012A\u0100cr\xEA\xF2kslash;\u6216\u0176\xF6\xF8;\u6AE7ed;\u6306y;\u4411\u0180crt\u0105\u010B\u0114ause;\u6235noullis;\u612Ca;\u4392r;\uC000\u{1D505}pf;\uC000\u{1D539}eve;\u42D8c\xF2\u0113mpeq;\u624E\u0700HOacdefhilorsu\u014D\u0151\u0156\u0180\u019E\u01A2\u01B5\u01B7\u01BA\u01DC\u0215\u0273\u0278\u027Ecy;\u4427PY\u803B\xA9\u40A9\u0180cpy\u015D\u0162\u017Aute;\u4106\u0100;i\u0167\u0168\u62D2talDifferentialD;\u6145leys;\u612D\u0200aeio\u0189\u018E\u0194\u0198ron;\u410Cdil\u803B\xC7\u40C7rc;\u4108nint;\u6230ot;\u410A\u0100dn\u01A7\u01ADilla;\u40B8terDot;\u40B7\xF2\u017Fi;\u43A7rcle\u0200DMPT\u01C7\u01CB\u01D1\u01D6ot;\u6299inus;\u6296lus;\u6295imes;\u6297o\u0100cs\u01E2\u01F8kwiseContourIntegral;\u6232eCurly\u0100DQ\u0203\u020FoubleQuote;\u601Duote;\u6019\u0200lnpu\u021E\u0228\u0247\u0255on\u0100;e\u0225\u0226\u6237;\u6A74\u0180git\u022F\u0236\u023Aruent;\u6261nt;\u622FourIntegral;\u622E\u0100fr\u024C\u024E;\u6102oduct;\u6210nterClockwiseContourIntegral;\u6233oss;\u6A2Fcr;\uC000\u{1D49E}p\u0100;C\u0284\u0285\u62D3ap;\u624D\u0580DJSZacefios\u02A0\u02AC\u02B0\u02B4\u02B8\u02CB\u02D7\u02E1\u02E6\u0333\u048D\u0100;o\u0179\u02A5trahd;\u6911cy;\u4402cy;\u4405cy;\u440F\u0180grs\u02BF\u02C4\u02C7ger;\u6021r;\u61A1hv;\u6AE4\u0100ay\u02D0\u02D5ron;\u410E;\u4414l\u0100;t\u02DD\u02DE\u6207a;\u4394r;\uC000\u{1D507}\u0100af\u02EB\u0327\u0100cm\u02F0\u0322ritical\u0200ADGT\u0300\u0306\u0316\u031Ccute;\u40B4o\u0174\u030B\u030D;\u42D9bleAcute;\u42DDrave;\u4060ilde;\u42DCond;\u62C4ferentialD;\u6146\u0470\u033D\0\0\0\u0342\u0354\0\u0405f;\uC000\u{1D53B}\u0180;DE\u0348\u0349\u034D\u40A8ot;\u60DCqual;\u6250ble\u0300CDLRUV\u0363\u0372\u0382\u03CF\u03E2\u03F8ontourIntegra\xEC\u0239o\u0274\u0379\0\0\u037B\xBB\u0349nArrow;\u61D3\u0100eo\u0387\u03A4ft\u0180ART\u0390\u0396\u03A1rrow;\u61D0ightArrow;\u61D4e\xE5\u02CAng\u0100LR\u03AB\u03C4eft\u0100AR\u03B3\u03B9rrow;\u67F8ightArrow;\u67FAightArrow;\u67F9ight\u0100AT\u03D8\u03DErrow;\u61D2ee;\u62A8p\u0241\u03E9\0\0\u03EFrrow;\u61D1ownArrow;\u61D5erticalBar;\u6225n\u0300ABLRTa\u0412\u042A\u0430\u045E\u047F\u037Crrow\u0180;BU\u041D\u041E\u0422\u6193ar;\u6913pArrow;\u61F5reve;\u4311eft\u02D2\u043A\0\u0446\0\u0450ightVector;\u6950eeVector;\u695Eector\u0100;B\u0459\u045A\u61BDar;\u6956ight\u01D4\u0467\0\u0471eeVector;\u695Fector\u0100;B\u047A\u047B\u61C1ar;\u6957ee\u0100;A\u0486\u0487\u62A4rrow;\u61A7\u0100ct\u0492\u0497r;\uC000\u{1D49F}rok;\u4110\u0800NTacdfglmopqstux\u04BD\u04C0\u04C4\u04CB\u04DE\u04E2\u04E7\u04EE\u04F5\u0521\u052F\u0536\u0552\u055D\u0560\u0565G;\u414AH\u803B\xD0\u40D0cute\u803B\xC9\u40C9\u0180aiy\u04D2\u04D7\u04DCron;\u411Arc\u803B\xCA\u40CA;\u442Dot;\u4116r;\uC000\u{1D508}rave\u803B\xC8\u40C8ement;\u6208\u0100ap\u04FA\u04FEcr;\u4112ty\u0253\u0506\0\0\u0512mallSquare;\u65FBerySmallSquare;\u65AB\u0100gp\u0526\u052Aon;\u4118f;\uC000\u{1D53C}silon;\u4395u\u0100ai\u053C\u0549l\u0100;T\u0542\u0543\u6A75ilde;\u6242librium;\u61CC\u0100ci\u0557\u055Ar;\u6130m;\u6A73a;\u4397ml\u803B\xCB\u40CB\u0100ip\u056A\u056Fsts;\u6203onentialE;\u6147\u0280cfios\u0585\u0588\u058D\u05B2\u05CCy;\u4424r;\uC000\u{1D509}lled\u0253\u0597\0\0\u05A3mallSquare;\u65FCerySmallSquare;\u65AA\u0370\u05BA\0\u05BF\0\0\u05C4f;\uC000\u{1D53D}All;\u6200riertrf;\u6131c\xF2\u05CB\u0600JTabcdfgorst\u05E8\u05EC\u05EF\u05FA\u0600\u0612\u0616\u061B\u061D\u0623\u066C\u0672cy;\u4403\u803B>\u403Emma\u0100;d\u05F7\u05F8\u4393;\u43DCreve;\u411E\u0180eiy\u0607\u060C\u0610dil;\u4122rc;\u411C;\u4413ot;\u4120r;\uC000\u{1D50A};\u62D9pf;\uC000\u{1D53E}eater\u0300EFGLST\u0635\u0644\u064E\u0656\u065B\u0666qual\u0100;L\u063E\u063F\u6265ess;\u62DBullEqual;\u6267reater;\u6AA2ess;\u6277lantEqual;\u6A7Eilde;\u6273cr;\uC000\u{1D4A2};\u626B\u0400Aacfiosu\u0685\u068B\u0696\u069B\u069E\u06AA\u06BE\u06CARDcy;\u442A\u0100ct\u0690\u0694ek;\u42C7;\u405Eirc;\u4124r;\u610ClbertSpace;\u610B\u01F0\u06AF\0\u06B2f;\u610DizontalLine;\u6500\u0100ct\u06C3\u06C5\xF2\u06A9rok;\u4126mp\u0144\u06D0\u06D8ownHum\xF0\u012Fqual;\u624F\u0700EJOacdfgmnostu\u06FA\u06FE\u0703\u0707\u070E\u071A\u071E\u0721\u0728\u0744\u0778\u078B\u078F\u0795cy;\u4415lig;\u4132cy;\u4401cute\u803B\xCD\u40CD\u0100iy\u0713\u0718rc\u803B\xCE\u40CE;\u4418ot;\u4130r;\u6111rave\u803B\xCC\u40CC\u0180;ap\u0720\u072F\u073F\u0100cg\u0734\u0737r;\u412AinaryI;\u6148lie\xF3\u03DD\u01F4\u0749\0\u0762\u0100;e\u074D\u074E\u622C\u0100gr\u0753\u0758ral;\u622Bsection;\u62C2isible\u0100CT\u076C\u0772omma;\u6063imes;\u6062\u0180gpt\u077F\u0783\u0788on;\u412Ef;\uC000\u{1D540}a;\u4399cr;\u6110ilde;\u4128\u01EB\u079A\0\u079Ecy;\u4406l\u803B\xCF\u40CF\u0280cfosu\u07AC\u07B7\u07BC\u07C2\u07D0\u0100iy\u07B1\u07B5rc;\u4134;\u4419r;\uC000\u{1D50D}pf;\uC000\u{1D541}\u01E3\u07C7\0\u07CCr;\uC000\u{1D4A5}rcy;\u4408kcy;\u4404\u0380HJacfos\u07E4\u07E8\u07EC\u07F1\u07FD\u0802\u0808cy;\u4425cy;\u440Cppa;\u439A\u0100ey\u07F6\u07FBdil;\u4136;\u441Ar;\uC000\u{1D50E}pf;\uC000\u{1D542}cr;\uC000\u{1D4A6}\u0580JTaceflmost\u0825\u0829\u082C\u0850\u0863\u09B3\u09B8\u09C7\u09CD\u0A37\u0A47cy;\u4409\u803B<\u403C\u0280cmnpr\u0837\u083C\u0841\u0844\u084Dute;\u4139bda;\u439Bg;\u67EAlacetrf;\u6112r;\u619E\u0180aey\u0857\u085C\u0861ron;\u413Ddil;\u413B;\u441B\u0100fs\u0868\u0970t\u0500ACDFRTUVar\u087E\u08A9\u08B1\u08E0\u08E6\u08FC\u092F\u095B\u0390\u096A\u0100nr\u0883\u088FgleBracket;\u67E8row\u0180;BR\u0899\u089A\u089E\u6190ar;\u61E4ightArrow;\u61C6eiling;\u6308o\u01F5\u08B7\0\u08C3bleBracket;\u67E6n\u01D4\u08C8\0\u08D2eeVector;\u6961ector\u0100;B\u08DB\u08DC\u61C3ar;\u6959loor;\u630Aight\u0100AV\u08EF\u08F5rrow;\u6194ector;\u694E\u0100er\u0901\u0917e\u0180;AV\u0909\u090A\u0910\u62A3rrow;\u61A4ector;\u695Aiangle\u0180;BE\u0924\u0925\u0929\u62B2ar;\u69CFqual;\u62B4p\u0180DTV\u0937\u0942\u094CownVector;\u6951eeVector;\u6960ector\u0100;B\u0956\u0957\u61BFar;\u6958ector\u0100;B\u0965\u0966\u61BCar;\u6952ight\xE1\u039Cs\u0300EFGLST\u097E\u098B\u0995\u099D\u09A2\u09ADqualGreater;\u62DAullEqual;\u6266reater;\u6276ess;\u6AA1lantEqual;\u6A7Dilde;\u6272r;\uC000\u{1D50F}\u0100;e\u09BD\u09BE\u62D8ftarrow;\u61DAidot;\u413F\u0180npw\u09D4\u0A16\u0A1Bg\u0200LRlr\u09DE\u09F7\u0A02\u0A10eft\u0100AR\u09E6\u09ECrrow;\u67F5ightArrow;\u67F7ightArrow;\u67F6eft\u0100ar\u03B3\u0A0Aight\xE1\u03BFight\xE1\u03CAf;\uC000\u{1D543}er\u0100LR\u0A22\u0A2CeftArrow;\u6199ightArrow;\u6198\u0180cht\u0A3E\u0A40\u0A42\xF2\u084C;\u61B0rok;\u4141;\u626A\u0400acefiosu\u0A5A\u0A5D\u0A60\u0A77\u0A7C\u0A85\u0A8B\u0A8Ep;\u6905y;\u441C\u0100dl\u0A65\u0A6FiumSpace;\u605Flintrf;\u6133r;\uC000\u{1D510}nusPlus;\u6213pf;\uC000\u{1D544}c\xF2\u0A76;\u439C\u0480Jacefostu\u0AA3\u0AA7\u0AAD\u0AC0\u0B14\u0B19\u0D91\u0D97\u0D9Ecy;\u440Acute;\u4143\u0180aey\u0AB4\u0AB9\u0ABEron;\u4147dil;\u4145;\u441D\u0180gsw\u0AC7\u0AF0\u0B0Eative\u0180MTV\u0AD3\u0ADF\u0AE8ediumSpace;\u600Bhi\u0100cn\u0AE6\u0AD8\xEB\u0AD9eryThi\xEE\u0AD9ted\u0100GL\u0AF8\u0B06reaterGreate\xF2\u0673essLes\xF3\u0A48Line;\u400Ar;\uC000\u{1D511}\u0200Bnpt\u0B22\u0B28\u0B37\u0B3Areak;\u6060BreakingSpace;\u40A0f;\u6115\u0680;CDEGHLNPRSTV\u0B55\u0B56\u0B6A\u0B7C\u0BA1\u0BEB\u0C04\u0C5E\u0C84\u0CA6\u0CD8\u0D61\u0D85\u6AEC\u0100ou\u0B5B\u0B64ngruent;\u6262pCap;\u626DoubleVerticalBar;\u6226\u0180lqx\u0B83\u0B8A\u0B9Bement;\u6209ual\u0100;T\u0B92\u0B93\u6260ilde;\uC000\u2242\u0338ists;\u6204reater\u0380;EFGLST\u0BB6\u0BB7\u0BBD\u0BC9\u0BD3\u0BD8\u0BE5\u626Fqual;\u6271ullEqual;\uC000\u2267\u0338reater;\uC000\u226B\u0338ess;\u6279lantEqual;\uC000\u2A7E\u0338ilde;\u6275ump\u0144\u0BF2\u0BFDownHump;\uC000\u224E\u0338qual;\uC000\u224F\u0338e\u0100fs\u0C0A\u0C27tTriangle\u0180;BE\u0C1A\u0C1B\u0C21\u62EAar;\uC000\u29CF\u0338qual;\u62ECs\u0300;EGLST\u0C35\u0C36\u0C3C\u0C44\u0C4B\u0C58\u626Equal;\u6270reater;\u6278ess;\uC000\u226A\u0338lantEqual;\uC000\u2A7D\u0338ilde;\u6274ested\u0100GL\u0C68\u0C79reaterGreater;\uC000\u2AA2\u0338essLess;\uC000\u2AA1\u0338recedes\u0180;ES\u0C92\u0C93\u0C9B\u6280qual;\uC000\u2AAF\u0338lantEqual;\u62E0\u0100ei\u0CAB\u0CB9verseElement;\u620CghtTriangle\u0180;BE\u0CCB\u0CCC\u0CD2\u62EBar;\uC000\u29D0\u0338qual;\u62ED\u0100qu\u0CDD\u0D0CuareSu\u0100bp\u0CE8\u0CF9set\u0100;E\u0CF0\u0CF3\uC000\u228F\u0338qual;\u62E2erset\u0100;E\u0D03\u0D06\uC000\u2290\u0338qual;\u62E3\u0180bcp\u0D13\u0D24\u0D4Eset\u0100;E\u0D1B\u0D1E\uC000\u2282\u20D2qual;\u6288ceeds\u0200;EST\u0D32\u0D33\u0D3B\u0D46\u6281qual;\uC000\u2AB0\u0338lantEqual;\u62E1ilde;\uC000\u227F\u0338erset\u0100;E\u0D58\u0D5B\uC000\u2283\u20D2qual;\u6289ilde\u0200;EFT\u0D6E\u0D6F\u0D75\u0D7F\u6241qual;\u6244ullEqual;\u6247ilde;\u6249erticalBar;\u6224cr;\uC000\u{1D4A9}ilde\u803B\xD1\u40D1;\u439D\u0700Eacdfgmoprstuv\u0DBD\u0DC2\u0DC9\u0DD5\u0DDB\u0DE0\u0DE7\u0DFC\u0E02\u0E20\u0E22\u0E32\u0E3F\u0E44lig;\u4152cute\u803B\xD3\u40D3\u0100iy\u0DCE\u0DD3rc\u803B\xD4\u40D4;\u441Eblac;\u4150r;\uC000\u{1D512}rave\u803B\xD2\u40D2\u0180aei\u0DEE\u0DF2\u0DF6cr;\u414Cga;\u43A9cron;\u439Fpf;\uC000\u{1D546}enCurly\u0100DQ\u0E0E\u0E1AoubleQuote;\u601Cuote;\u6018;\u6A54\u0100cl\u0E27\u0E2Cr;\uC000\u{1D4AA}ash\u803B\xD8\u40D8i\u016C\u0E37\u0E3Cde\u803B\xD5\u40D5es;\u6A37ml\u803B\xD6\u40D6er\u0100BP\u0E4B\u0E60\u0100ar\u0E50\u0E53r;\u603Eac\u0100ek\u0E5A\u0E5C;\u63DEet;\u63B4arenthesis;\u63DC\u0480acfhilors\u0E7F\u0E87\u0E8A\u0E8F\u0E92\u0E94\u0E9D\u0EB0\u0EFCrtialD;\u6202y;\u441Fr;\uC000\u{1D513}i;\u43A6;\u43A0usMinus;\u40B1\u0100ip\u0EA2\u0EADncareplan\xE5\u069Df;\u6119\u0200;eio\u0EB9\u0EBA\u0EE0\u0EE4\u6ABBcedes\u0200;EST\u0EC8\u0EC9\u0ECF\u0EDA\u627Aqual;\u6AAFlantEqual;\u627Cilde;\u627Eme;\u6033\u0100dp\u0EE9\u0EEEuct;\u620Fortion\u0100;a\u0225\u0EF9l;\u621D\u0100ci\u0F01\u0F06r;\uC000\u{1D4AB};\u43A8\u0200Ufos\u0F11\u0F16\u0F1B\u0F1FOT\u803B"\u4022r;\uC000\u{1D514}pf;\u611Acr;\uC000\u{1D4AC}\u0600BEacefhiorsu\u0F3E\u0F43\u0F47\u0F60\u0F73\u0FA7\u0FAA\u0FAD\u1096\u10A9\u10B4\u10BEarr;\u6910G\u803B\xAE\u40AE\u0180cnr\u0F4E\u0F53\u0F56ute;\u4154g;\u67EBr\u0100;t\u0F5C\u0F5D\u61A0l;\u6916\u0180aey\u0F67\u0F6C\u0F71ron;\u4158dil;\u4156;\u4420\u0100;v\u0F78\u0F79\u611Cerse\u0100EU\u0F82\u0F99\u0100lq\u0F87\u0F8Eement;\u620Builibrium;\u61CBpEquilibrium;\u696Fr\xBB\u0F79o;\u43A1ght\u0400ACDFTUVa\u0FC1\u0FEB\u0FF3\u1022\u1028\u105B\u1087\u03D8\u0100nr\u0FC6\u0FD2gleBracket;\u67E9row\u0180;BL\u0FDC\u0FDD\u0FE1\u6192ar;\u61E5eftArrow;\u61C4eiling;\u6309o\u01F5\u0FF9\0\u1005bleBracket;\u67E7n\u01D4\u100A\0\u1014eeVector;\u695Dector\u0100;B\u101D\u101E\u61C2ar;\u6955loor;\u630B\u0100er\u102D\u1043e\u0180;AV\u1035\u1036\u103C\u62A2rrow;\u61A6ector;\u695Biangle\u0180;BE\u1050\u1051\u1055\u62B3ar;\u69D0qual;\u62B5p\u0180DTV\u1063\u106E\u1078ownVector;\u694FeeVector;\u695Cector\u0100;B\u1082\u1083\u61BEar;\u6954ector\u0100;B\u1091\u1092\u61C0ar;\u6953\u0100pu\u109B\u109Ef;\u611DndImplies;\u6970ightarrow;\u61DB\u0100ch\u10B9\u10BCr;\u611B;\u61B1leDelayed;\u69F4\u0680HOacfhimoqstu\u10E4\u10F1\u10F7\u10FD\u1119\u111E\u1151\u1156\u1161\u1167\u11B5\u11BB\u11BF\u0100Cc\u10E9\u10EEHcy;\u4429y;\u4428FTcy;\u442Ccute;\u415A\u0280;aeiy\u1108\u1109\u110E\u1113\u1117\u6ABCron;\u4160dil;\u415Erc;\u415C;\u4421r;\uC000\u{1D516}ort\u0200DLRU\u112A\u1134\u113E\u1149ownArrow\xBB\u041EeftArrow\xBB\u089AightArrow\xBB\u0FDDpArrow;\u6191gma;\u43A3allCircle;\u6218pf;\uC000\u{1D54A}\u0272\u116D\0\0\u1170t;\u621Aare\u0200;ISU\u117B\u117C\u1189\u11AF\u65A1ntersection;\u6293u\u0100bp\u118F\u119Eset\u0100;E\u1197\u1198\u628Fqual;\u6291erset\u0100;E\u11A8\u11A9\u6290qual;\u6292nion;\u6294cr;\uC000\u{1D4AE}ar;\u62C6\u0200bcmp\u11C8\u11DB\u1209\u120B\u0100;s\u11CD\u11CE\u62D0et\u0100;E\u11CD\u11D5qual;\u6286\u0100ch\u11E0\u1205eeds\u0200;EST\u11ED\u11EE\u11F4\u11FF\u627Bqual;\u6AB0lantEqual;\u627Dilde;\u627FTh\xE1\u0F8C;\u6211\u0180;es\u1212\u1213\u1223\u62D1rset\u0100;E\u121C\u121D\u6283qual;\u6287et\xBB\u1213\u0580HRSacfhiors\u123E\u1244\u1249\u1255\u125E\u1271\u1276\u129F\u12C2\u12C8\u12D1ORN\u803B\xDE\u40DEADE;\u6122\u0100Hc\u124E\u1252cy;\u440By;\u4426\u0100bu\u125A\u125C;\u4009;\u43A4\u0180aey\u1265\u126A\u126Fron;\u4164dil;\u4162;\u4422r;\uC000\u{1D517}\u0100ei\u127B\u1289\u01F2\u1280\0\u1287efore;\u6234a;\u4398\u0100cn\u128E\u1298kSpace;\uC000\u205F\u200ASpace;\u6009lde\u0200;EFT\u12AB\u12AC\u12B2\u12BC\u623Cqual;\u6243ullEqual;\u6245ilde;\u6248pf;\uC000\u{1D54B}ipleDot;\u60DB\u0100ct\u12D6\u12DBr;\uC000\u{1D4AF}rok;\u4166\u0AE1\u12F7\u130E\u131A\u1326\0\u132C\u1331\0\0\0\0\0\u1338\u133D\u1377\u1385\0\u13FF\u1404\u140A\u1410\u0100cr\u12FB\u1301ute\u803B\xDA\u40DAr\u0100;o\u1307\u1308\u619Fcir;\u6949r\u01E3\u1313\0\u1316y;\u440Eve;\u416C\u0100iy\u131E\u1323rc\u803B\xDB\u40DB;\u4423blac;\u4170r;\uC000\u{1D518}rave\u803B\xD9\u40D9acr;\u416A\u0100di\u1341\u1369er\u0100BP\u1348\u135D\u0100ar\u134D\u1350r;\u405Fac\u0100ek\u1357\u1359;\u63DFet;\u63B5arenthesis;\u63DDon\u0100;P\u1370\u1371\u62C3lus;\u628E\u0100gp\u137B\u137Fon;\u4172f;\uC000\u{1D54C}\u0400ADETadps\u1395\u13AE\u13B8\u13C4\u03E8\u13D2\u13D7\u13F3rrow\u0180;BD\u1150\u13A0\u13A4ar;\u6912ownArrow;\u61C5ownArrow;\u6195quilibrium;\u696Eee\u0100;A\u13CB\u13CC\u62A5rrow;\u61A5own\xE1\u03F3er\u0100LR\u13DE\u13E8eftArrow;\u6196ightArrow;\u6197i\u0100;l\u13F9\u13FA\u43D2on;\u43A5ing;\u416Ecr;\uC000\u{1D4B0}ilde;\u4168ml\u803B\xDC\u40DC\u0480Dbcdefosv\u1427\u142C\u1430\u1433\u143E\u1485\u148A\u1490\u1496ash;\u62ABar;\u6AEBy;\u4412ash\u0100;l\u143B\u143C\u62A9;\u6AE6\u0100er\u1443\u1445;\u62C1\u0180bty\u144C\u1450\u147Aar;\u6016\u0100;i\u144F\u1455cal\u0200BLST\u1461\u1465\u146A\u1474ar;\u6223ine;\u407Ceparator;\u6758ilde;\u6240ThinSpace;\u600Ar;\uC000\u{1D519}pf;\uC000\u{1D54D}cr;\uC000\u{1D4B1}dash;\u62AA\u0280cefos\u14A7\u14AC\u14B1\u14B6\u14BCirc;\u4174dge;\u62C0r;\uC000\u{1D51A}pf;\uC000\u{1D54E}cr;\uC000\u{1D4B2}\u0200fios\u14CB\u14D0\u14D2\u14D8r;\uC000\u{1D51B};\u439Epf;\uC000\u{1D54F}cr;\uC000\u{1D4B3}\u0480AIUacfosu\u14F1\u14F5\u14F9\u14FD\u1504\u150F\u1514\u151A\u1520cy;\u442Fcy;\u4407cy;\u442Ecute\u803B\xDD\u40DD\u0100iy\u1509\u150Drc;\u4176;\u442Br;\uC000\u{1D51C}pf;\uC000\u{1D550}cr;\uC000\u{1D4B4}ml;\u4178\u0400Hacdefos\u1535\u1539\u153F\u154B\u154F\u155D\u1560\u1564cy;\u4416cute;\u4179\u0100ay\u1544\u1549ron;\u417D;\u4417ot;\u417B\u01F2\u1554\0\u155BoWidt\xE8\u0AD9a;\u4396r;\u6128pf;\u6124cr;\uC000\u{1D4B5}\u0BE1\u1583\u158A\u1590\0\u15B0\u15B6\u15BF\0\0\0\0\u15C6\u15DB\u15EB\u165F\u166D\0\u1695\u169B\u16B2\u16B9\0\u16BEcute\u803B\xE1\u40E1reve;\u4103\u0300;Ediuy\u159C\u159D\u15A1\u15A3\u15A8\u15AD\u623E;\uC000\u223E\u0333;\u623Frc\u803B\xE2\u40E2te\u80BB\xB4\u0306;\u4430lig\u803B\xE6\u40E6\u0100;r\xB2\u15BA;\uC000\u{1D51E}rave\u803B\xE0\u40E0\u0100ep\u15CA\u15D6\u0100fp\u15CF\u15D4sym;\u6135\xE8\u15D3ha;\u43B1\u0100ap\u15DFc\u0100cl\u15E4\u15E7r;\u4101g;\u6A3F\u0264\u15F0\0\0\u160A\u0280;adsv\u15FA\u15FB\u15FF\u1601\u1607\u6227nd;\u6A55;\u6A5Clope;\u6A58;\u6A5A\u0380;elmrsz\u1618\u1619\u161B\u161E\u163F\u164F\u1659\u6220;\u69A4e\xBB\u1619sd\u0100;a\u1625\u1626\u6221\u0461\u1630\u1632\u1634\u1636\u1638\u163A\u163C\u163E;\u69A8;\u69A9;\u69AA;\u69AB;\u69AC;\u69AD;\u69AE;\u69AFt\u0100;v\u1645\u1646\u621Fb\u0100;d\u164C\u164D\u62BE;\u699D\u0100pt\u1654\u1657h;\u6222\xBB\xB9arr;\u637C\u0100gp\u1663\u1667on;\u4105f;\uC000\u{1D552}\u0380;Eaeiop\u12C1\u167B\u167D\u1682\u1684\u1687\u168A;\u6A70cir;\u6A6F;\u624Ad;\u624Bs;\u4027rox\u0100;e\u12C1\u1692\xF1\u1683ing\u803B\xE5\u40E5\u0180cty\u16A1\u16A6\u16A8r;\uC000\u{1D4B6};\u402Amp\u0100;e\u12C1\u16AF\xF1\u0288ilde\u803B\xE3\u40E3ml\u803B\xE4\u40E4\u0100ci\u16C2\u16C8onin\xF4\u0272nt;\u6A11\u0800Nabcdefiklnoprsu\u16ED\u16F1\u1730\u173C\u1743\u1748\u1778\u177D\u17E0\u17E6\u1839\u1850\u170D\u193D\u1948\u1970ot;\u6AED\u0100cr\u16F6\u171Ek\u0200ceps\u1700\u1705\u170D\u1713ong;\u624Cpsilon;\u43F6rime;\u6035im\u0100;e\u171A\u171B\u623Dq;\u62CD\u0176\u1722\u1726ee;\u62BDed\u0100;g\u172C\u172D\u6305e\xBB\u172Drk\u0100;t\u135C\u1737brk;\u63B6\u0100oy\u1701\u1741;\u4431quo;\u601E\u0280cmprt\u1753\u175B\u1761\u1764\u1768aus\u0100;e\u010A\u0109ptyv;\u69B0s\xE9\u170Cno\xF5\u0113\u0180ahw\u176F\u1771\u1773;\u43B2;\u6136een;\u626Cr;\uC000\u{1D51F}g\u0380costuvw\u178D\u179D\u17B3\u17C1\u17D5\u17DB\u17DE\u0180aiu\u1794\u1796\u179A\xF0\u0760rc;\u65EFp\xBB\u1371\u0180dpt\u17A4\u17A8\u17ADot;\u6A00lus;\u6A01imes;\u6A02\u0271\u17B9\0\0\u17BEcup;\u6A06ar;\u6605riangle\u0100du\u17CD\u17D2own;\u65BDp;\u65B3plus;\u6A04e\xE5\u1444\xE5\u14ADarow;\u690D\u0180ako\u17ED\u1826\u1835\u0100cn\u17F2\u1823k\u0180lst\u17FA\u05AB\u1802ozenge;\u69EBriangle\u0200;dlr\u1812\u1813\u1818\u181D\u65B4own;\u65BEeft;\u65C2ight;\u65B8k;\u6423\u01B1\u182B\0\u1833\u01B2\u182F\0\u1831;\u6592;\u65914;\u6593ck;\u6588\u0100eo\u183E\u184D\u0100;q\u1843\u1846\uC000=\u20E5uiv;\uC000\u2261\u20E5t;\u6310\u0200ptwx\u1859\u185E\u1867\u186Cf;\uC000\u{1D553}\u0100;t\u13CB\u1863om\xBB\u13CCtie;\u62C8\u0600DHUVbdhmptuv\u1885\u1896\u18AA\u18BB\u18D7\u18DB\u18EC\u18FF\u1905\u190A\u1910\u1921\u0200LRlr\u188E\u1890\u1892\u1894;\u6557;\u6554;\u6556;\u6553\u0280;DUdu\u18A1\u18A2\u18A4\u18A6\u18A8\u6550;\u6566;\u6569;\u6564;\u6567\u0200LRlr\u18B3\u18B5\u18B7\u18B9;\u655D;\u655A;\u655C;\u6559\u0380;HLRhlr\u18CA\u18CB\u18CD\u18CF\u18D1\u18D3\u18D5\u6551;\u656C;\u6563;\u6560;\u656B;\u6562;\u655Fox;\u69C9\u0200LRlr\u18E4\u18E6\u18E8\u18EA;\u6555;\u6552;\u6510;\u650C\u0280;DUdu\u06BD\u18F7\u18F9\u18FB\u18FD;\u6565;\u6568;\u652C;\u6534inus;\u629Flus;\u629Eimes;\u62A0\u0200LRlr\u1919\u191B\u191D\u191F;\u655B;\u6558;\u6518;\u6514\u0380;HLRhlr\u1930\u1931\u1933\u1935\u1937\u1939\u193B\u6502;\u656A;\u6561;\u655E;\u653C;\u6524;\u651C\u0100ev\u0123\u1942bar\u803B\xA6\u40A6\u0200ceio\u1951\u1956\u195A\u1960r;\uC000\u{1D4B7}mi;\u604Fm\u0100;e\u171A\u171Cl\u0180;bh\u1968\u1969\u196B\u405C;\u69C5sub;\u67C8\u016C\u1974\u197El\u0100;e\u1979\u197A\u6022t\xBB\u197Ap\u0180;Ee\u012F\u1985\u1987;\u6AAE\u0100;q\u06DC\u06DB\u0CE1\u19A7\0\u19E8\u1A11\u1A15\u1A32\0\u1A37\u1A50\0\0\u1AB4\0\0\u1AC1\0\0\u1B21\u1B2E\u1B4D\u1B52\0\u1BFD\0\u1C0C\u0180cpr\u19AD\u19B2\u19DDute;\u4107\u0300;abcds\u19BF\u19C0\u19C4\u19CA\u19D5\u19D9\u6229nd;\u6A44rcup;\u6A49\u0100au\u19CF\u19D2p;\u6A4Bp;\u6A47ot;\u6A40;\uC000\u2229\uFE00\u0100eo\u19E2\u19E5t;\u6041\xEE\u0693\u0200aeiu\u19F0\u19FB\u1A01\u1A05\u01F0\u19F5\0\u19F8s;\u6A4Don;\u410Ddil\u803B\xE7\u40E7rc;\u4109ps\u0100;s\u1A0C\u1A0D\u6A4Cm;\u6A50ot;\u410B\u0180dmn\u1A1B\u1A20\u1A26il\u80BB\xB8\u01ADptyv;\u69B2t\u8100\xA2;e\u1A2D\u1A2E\u40A2r\xE4\u01B2r;\uC000\u{1D520}\u0180cei\u1A3D\u1A40\u1A4Dy;\u4447ck\u0100;m\u1A47\u1A48\u6713ark\xBB\u1A48;\u43C7r\u0380;Ecefms\u1A5F\u1A60\u1A62\u1A6B\u1AA4\u1AAA\u1AAE\u65CB;\u69C3\u0180;el\u1A69\u1A6A\u1A6D\u42C6q;\u6257e\u0261\u1A74\0\0\u1A88rrow\u0100lr\u1A7C\u1A81eft;\u61BAight;\u61BB\u0280RSacd\u1A92\u1A94\u1A96\u1A9A\u1A9F\xBB\u0F47;\u64C8st;\u629Birc;\u629Aash;\u629Dnint;\u6A10id;\u6AEFcir;\u69C2ubs\u0100;u\u1ABB\u1ABC\u6663it\xBB\u1ABC\u02EC\u1AC7\u1AD4\u1AFA\0\u1B0Aon\u0100;e\u1ACD\u1ACE\u403A\u0100;q\xC7\xC6\u026D\u1AD9\0\0\u1AE2a\u0100;t\u1ADE\u1ADF\u402C;\u4040\u0180;fl\u1AE8\u1AE9\u1AEB\u6201\xEE\u1160e\u0100mx\u1AF1\u1AF6ent\xBB\u1AE9e\xF3\u024D\u01E7\u1AFE\0\u1B07\u0100;d\u12BB\u1B02ot;\u6A6Dn\xF4\u0246\u0180fry\u1B10\u1B14\u1B17;\uC000\u{1D554}o\xE4\u0254\u8100\xA9;s\u0155\u1B1Dr;\u6117\u0100ao\u1B25\u1B29rr;\u61B5ss;\u6717\u0100cu\u1B32\u1B37r;\uC000\u{1D4B8}\u0100bp\u1B3C\u1B44\u0100;e\u1B41\u1B42\u6ACF;\u6AD1\u0100;e\u1B49\u1B4A\u6AD0;\u6AD2dot;\u62EF\u0380delprvw\u1B60\u1B6C\u1B77\u1B82\u1BAC\u1BD4\u1BF9arr\u0100lr\u1B68\u1B6A;\u6938;\u6935\u0270\u1B72\0\0\u1B75r;\u62DEc;\u62DFarr\u0100;p\u1B7F\u1B80\u61B6;\u693D\u0300;bcdos\u1B8F\u1B90\u1B96\u1BA1\u1BA5\u1BA8\u622Arcap;\u6A48\u0100au\u1B9B\u1B9Ep;\u6A46p;\u6A4Aot;\u628Dr;\u6A45;\uC000\u222A\uFE00\u0200alrv\u1BB5\u1BBF\u1BDE\u1BE3rr\u0100;m\u1BBC\u1BBD\u61B7;\u693Cy\u0180evw\u1BC7\u1BD4\u1BD8q\u0270\u1BCE\0\0\u1BD2re\xE3\u1B73u\xE3\u1B75ee;\u62CEedge;\u62CFen\u803B\xA4\u40A4earrow\u0100lr\u1BEE\u1BF3eft\xBB\u1B80ight\xBB\u1BBDe\xE4\u1BDD\u0100ci\u1C01\u1C07onin\xF4\u01F7nt;\u6231lcty;\u632D\u0980AHabcdefhijlorstuwz\u1C38\u1C3B\u1C3F\u1C5D\u1C69\u1C75\u1C8A\u1C9E\u1CAC\u1CB7\u1CFB\u1CFF\u1D0D\u1D7B\u1D91\u1DAB\u1DBB\u1DC6\u1DCDr\xF2\u0381ar;\u6965\u0200glrs\u1C48\u1C4D\u1C52\u1C54ger;\u6020eth;\u6138\xF2\u1133h\u0100;v\u1C5A\u1C5B\u6010\xBB\u090A\u016B\u1C61\u1C67arow;\u690Fa\xE3\u0315\u0100ay\u1C6E\u1C73ron;\u410F;\u4434\u0180;ao\u0332\u1C7C\u1C84\u0100gr\u02BF\u1C81r;\u61CAtseq;\u6A77\u0180glm\u1C91\u1C94\u1C98\u803B\xB0\u40B0ta;\u43B4ptyv;\u69B1\u0100ir\u1CA3\u1CA8sht;\u697F;\uC000\u{1D521}ar\u0100lr\u1CB3\u1CB5\xBB\u08DC\xBB\u101E\u0280aegsv\u1CC2\u0378\u1CD6\u1CDC\u1CE0m\u0180;os\u0326\u1CCA\u1CD4nd\u0100;s\u0326\u1CD1uit;\u6666amma;\u43DDin;\u62F2\u0180;io\u1CE7\u1CE8\u1CF8\u40F7de\u8100\xF7;o\u1CE7\u1CF0ntimes;\u62C7n\xF8\u1CF7cy;\u4452c\u026F\u1D06\0\0\u1D0Arn;\u631Eop;\u630D\u0280lptuw\u1D18\u1D1D\u1D22\u1D49\u1D55lar;\u4024f;\uC000\u{1D555}\u0280;emps\u030B\u1D2D\u1D37\u1D3D\u1D42q\u0100;d\u0352\u1D33ot;\u6251inus;\u6238lus;\u6214quare;\u62A1blebarwedg\xE5\xFAn\u0180adh\u112E\u1D5D\u1D67ownarrow\xF3\u1C83arpoon\u0100lr\u1D72\u1D76ef\xF4\u1CB4igh\xF4\u1CB6\u0162\u1D7F\u1D85karo\xF7\u0F42\u026F\u1D8A\0\0\u1D8Ern;\u631Fop;\u630C\u0180cot\u1D98\u1DA3\u1DA6\u0100ry\u1D9D\u1DA1;\uC000\u{1D4B9};\u4455l;\u69F6rok;\u4111\u0100dr\u1DB0\u1DB4ot;\u62F1i\u0100;f\u1DBA\u1816\u65BF\u0100ah\u1DC0\u1DC3r\xF2\u0429a\xF2\u0FA6angle;\u69A6\u0100ci\u1DD2\u1DD5y;\u445Fgrarr;\u67FF\u0900Dacdefglmnopqrstux\u1E01\u1E09\u1E19\u1E38\u0578\u1E3C\u1E49\u1E61\u1E7E\u1EA5\u1EAF\u1EBD\u1EE1\u1F2A\u1F37\u1F44\u1F4E\u1F5A\u0100Do\u1E06\u1D34o\xF4\u1C89\u0100cs\u1E0E\u1E14ute\u803B\xE9\u40E9ter;\u6A6E\u0200aioy\u1E22\u1E27\u1E31\u1E36ron;\u411Br\u0100;c\u1E2D\u1E2E\u6256\u803B\xEA\u40EAlon;\u6255;\u444Dot;\u4117\u0100Dr\u1E41\u1E45ot;\u6252;\uC000\u{1D522}\u0180;rs\u1E50\u1E51\u1E57\u6A9Aave\u803B\xE8\u40E8\u0100;d\u1E5C\u1E5D\u6A96ot;\u6A98\u0200;ils\u1E6A\u1E6B\u1E72\u1E74\u6A99nters;\u63E7;\u6113\u0100;d\u1E79\u1E7A\u6A95ot;\u6A97\u0180aps\u1E85\u1E89\u1E97cr;\u4113ty\u0180;sv\u1E92\u1E93\u1E95\u6205et\xBB\u1E93p\u01001;\u1E9D\u1EA4\u0133\u1EA1\u1EA3;\u6004;\u6005\u6003\u0100gs\u1EAA\u1EAC;\u414Bp;\u6002\u0100gp\u1EB4\u1EB8on;\u4119f;\uC000\u{1D556}\u0180als\u1EC4\u1ECE\u1ED2r\u0100;s\u1ECA\u1ECB\u62D5l;\u69E3us;\u6A71i\u0180;lv\u1EDA\u1EDB\u1EDF\u43B5on\xBB\u1EDB;\u43F5\u0200csuv\u1EEA\u1EF3\u1F0B\u1F23\u0100io\u1EEF\u1E31rc\xBB\u1E2E\u0269\u1EF9\0\0\u1EFB\xED\u0548ant\u0100gl\u1F02\u1F06tr\xBB\u1E5Dess\xBB\u1E7A\u0180aei\u1F12\u1F16\u1F1Als;\u403Dst;\u625Fv\u0100;D\u0235\u1F20D;\u6A78parsl;\u69E5\u0100Da\u1F2F\u1F33ot;\u6253rr;\u6971\u0180cdi\u1F3E\u1F41\u1EF8r;\u612Fo\xF4\u0352\u0100ah\u1F49\u1F4B;\u43B7\u803B\xF0\u40F0\u0100mr\u1F53\u1F57l\u803B\xEB\u40EBo;\u60AC\u0180cip\u1F61\u1F64\u1F67l;\u4021s\xF4\u056E\u0100eo\u1F6C\u1F74ctatio\xEE\u0559nential\xE5\u0579\u09E1\u1F92\0\u1F9E\0\u1FA1\u1FA7\0\0\u1FC6\u1FCC\0\u1FD3\0\u1FE6\u1FEA\u2000\0\u2008\u205Allingdotse\xF1\u1E44y;\u4444male;\u6640\u0180ilr\u1FAD\u1FB3\u1FC1lig;\u8000\uFB03\u0269\u1FB9\0\0\u1FBDg;\u8000\uFB00ig;\u8000\uFB04;\uC000\u{1D523}lig;\u8000\uFB01lig;\uC000fj\u0180alt\u1FD9\u1FDC\u1FE1t;\u666Dig;\u8000\uFB02ns;\u65B1of;\u4192\u01F0\u1FEE\0\u1FF3f;\uC000\u{1D557}\u0100ak\u05BF\u1FF7\u0100;v\u1FFC\u1FFD\u62D4;\u6AD9artint;\u6A0D\u0100ao\u200C\u2055\u0100cs\u2011\u2052\u03B1\u201A\u2030\u2038\u2045\u2048\0\u2050\u03B2\u2022\u2025\u2027\u202A\u202C\0\u202E\u803B\xBD\u40BD;\u6153\u803B\xBC\u40BC;\u6155;\u6159;\u615B\u01B3\u2034\0\u2036;\u6154;\u6156\u02B4\u203E\u2041\0\0\u2043\u803B\xBE\u40BE;\u6157;\u615C5;\u6158\u01B6\u204C\0\u204E;\u615A;\u615D8;\u615El;\u6044wn;\u6322cr;\uC000\u{1D4BB}\u0880Eabcdefgijlnorstv\u2082\u2089\u209F\u20A5\u20B0\u20B4\u20F0\u20F5\u20FA\u20FF\u2103\u2112\u2138\u0317\u213E\u2152\u219E\u0100;l\u064D\u2087;\u6A8C\u0180cmp\u2090\u2095\u209Dute;\u41F5ma\u0100;d\u209C\u1CDA\u43B3;\u6A86reve;\u411F\u0100iy\u20AA\u20AErc;\u411D;\u4433ot;\u4121\u0200;lqs\u063E\u0642\u20BD\u20C9\u0180;qs\u063E\u064C\u20C4lan\xF4\u0665\u0200;cdl\u0665\u20D2\u20D5\u20E5c;\u6AA9ot\u0100;o\u20DC\u20DD\u6A80\u0100;l\u20E2\u20E3\u6A82;\u6A84\u0100;e\u20EA\u20ED\uC000\u22DB\uFE00s;\u6A94r;\uC000\u{1D524}\u0100;g\u0673\u061Bmel;\u6137cy;\u4453\u0200;Eaj\u065A\u210C\u210E\u2110;\u6A92;\u6AA5;\u6AA4\u0200Eaes\u211B\u211D\u2129\u2134;\u6269p\u0100;p\u2123\u2124\u6A8Arox\xBB\u2124\u0100;q\u212E\u212F\u6A88\u0100;q\u212E\u211Bim;\u62E7pf;\uC000\u{1D558}\u0100ci\u2143\u2146r;\u610Am\u0180;el\u066B\u214E\u2150;\u6A8E;\u6A90\u8300>;cdlqr\u05EE\u2160\u216A\u216E\u2173\u2179\u0100ci\u2165\u2167;\u6AA7r;\u6A7Aot;\u62D7Par;\u6995uest;\u6A7C\u0280adels\u2184\u216A\u2190\u0656\u219B\u01F0\u2189\0\u218Epro\xF8\u209Er;\u6978q\u0100lq\u063F\u2196les\xF3\u2088i\xED\u066B\u0100en\u21A3\u21ADrtneqq;\uC000\u2269\uFE00\xC5\u21AA\u0500Aabcefkosy\u21C4\u21C7\u21F1\u21F5\u21FA\u2218\u221D\u222F\u2268\u227Dr\xF2\u03A0\u0200ilmr\u21D0\u21D4\u21D7\u21DBrs\xF0\u1484f\xBB\u2024il\xF4\u06A9\u0100dr\u21E0\u21E4cy;\u444A\u0180;cw\u08F4\u21EB\u21EFir;\u6948;\u61ADar;\u610Firc;\u4125\u0180alr\u2201\u220E\u2213rts\u0100;u\u2209\u220A\u6665it\xBB\u220Alip;\u6026con;\u62B9r;\uC000\u{1D525}s\u0100ew\u2223\u2229arow;\u6925arow;\u6926\u0280amopr\u223A\u223E\u2243\u225E\u2263rr;\u61FFtht;\u623Bk\u0100lr\u2249\u2253eftarrow;\u61A9ightarrow;\u61AAf;\uC000\u{1D559}bar;\u6015\u0180clt\u226F\u2274\u2278r;\uC000\u{1D4BD}as\xE8\u21F4rok;\u4127\u0100bp\u2282\u2287ull;\u6043hen\xBB\u1C5B\u0AE1\u22A3\0\u22AA\0\u22B8\u22C5\u22CE\0\u22D5\u22F3\0\0\u22F8\u2322\u2367\u2362\u237F\0\u2386\u23AA\u23B4cute\u803B\xED\u40ED\u0180;iy\u0771\u22B0\u22B5rc\u803B\xEE\u40EE;\u4438\u0100cx\u22BC\u22BFy;\u4435cl\u803B\xA1\u40A1\u0100fr\u039F\u22C9;\uC000\u{1D526}rave\u803B\xEC\u40EC\u0200;ino\u073E\u22DD\u22E9\u22EE\u0100in\u22E2\u22E6nt;\u6A0Ct;\u622Dfin;\u69DCta;\u6129lig;\u4133\u0180aop\u22FE\u231A\u231D\u0180cgt\u2305\u2308\u2317r;\u412B\u0180elp\u071F\u230F\u2313in\xE5\u078Ear\xF4\u0720h;\u4131f;\u62B7ed;\u41B5\u0280;cfot\u04F4\u232C\u2331\u233D\u2341are;\u6105in\u0100;t\u2338\u2339\u621Eie;\u69DDdo\xF4\u2319\u0280;celp\u0757\u234C\u2350\u235B\u2361al;\u62BA\u0100gr\u2355\u2359er\xF3\u1563\xE3\u234Darhk;\u6A17rod;\u6A3C\u0200cgpt\u236F\u2372\u2376\u237By;\u4451on;\u412Ff;\uC000\u{1D55A}a;\u43B9uest\u803B\xBF\u40BF\u0100ci\u238A\u238Fr;\uC000\u{1D4BE}n\u0280;Edsv\u04F4\u239B\u239D\u23A1\u04F3;\u62F9ot;\u62F5\u0100;v\u23A6\u23A7\u62F4;\u62F3\u0100;i\u0777\u23AElde;\u4129\u01EB\u23B8\0\u23BCcy;\u4456l\u803B\xEF\u40EF\u0300cfmosu\u23CC\u23D7\u23DC\u23E1\u23E7\u23F5\u0100iy\u23D1\u23D5rc;\u4135;\u4439r;\uC000\u{1D527}ath;\u4237pf;\uC000\u{1D55B}\u01E3\u23EC\0\u23F1r;\uC000\u{1D4BF}rcy;\u4458kcy;\u4454\u0400acfghjos\u240B\u2416\u2422\u2427\u242D\u2431\u2435\u243Bppa\u0100;v\u2413\u2414\u43BA;\u43F0\u0100ey\u241B\u2420dil;\u4137;\u443Ar;\uC000\u{1D528}reen;\u4138cy;\u4445cy;\u445Cpf;\uC000\u{1D55C}cr;\uC000\u{1D4C0}\u0B80ABEHabcdefghjlmnoprstuv\u2470\u2481\u2486\u248D\u2491\u250E\u253D\u255A\u2580\u264E\u265E\u2665\u2679\u267D\u269A\u26B2\u26D8\u275D\u2768\u278B\u27C0\u2801\u2812\u0180art\u2477\u247A\u247Cr\xF2\u09C6\xF2\u0395ail;\u691Barr;\u690E\u0100;g\u0994\u248B;\u6A8Bar;\u6962\u0963\u24A5\0\u24AA\0\u24B1\0\0\0\0\0\u24B5\u24BA\0\u24C6\u24C8\u24CD\0\u24F9ute;\u413Amptyv;\u69B4ra\xEE\u084Cbda;\u43BBg\u0180;dl\u088E\u24C1\u24C3;\u6991\xE5\u088E;\u6A85uo\u803B\xAB\u40ABr\u0400;bfhlpst\u0899\u24DE\u24E6\u24E9\u24EB\u24EE\u24F1\u24F5\u0100;f\u089D\u24E3s;\u691Fs;\u691D\xEB\u2252p;\u61ABl;\u6939im;\u6973l;\u61A2\u0180;ae\u24FF\u2500\u2504\u6AABil;\u6919\u0100;s\u2509\u250A\u6AAD;\uC000\u2AAD\uFE00\u0180abr\u2515\u2519\u251Drr;\u690Crk;\u6772\u0100ak\u2522\u252Cc\u0100ek\u2528\u252A;\u407B;\u405B\u0100es\u2531\u2533;\u698Bl\u0100du\u2539\u253B;\u698F;\u698D\u0200aeuy\u2546\u254B\u2556\u2558ron;\u413E\u0100di\u2550\u2554il;\u413C\xEC\u08B0\xE2\u2529;\u443B\u0200cqrs\u2563\u2566\u256D\u257Da;\u6936uo\u0100;r\u0E19\u1746\u0100du\u2572\u2577har;\u6967shar;\u694Bh;\u61B2\u0280;fgqs\u258B\u258C\u0989\u25F3\u25FF\u6264t\u0280ahlrt\u2598\u25A4\u25B7\u25C2\u25E8rrow\u0100;t\u0899\u25A1a\xE9\u24F6arpoon\u0100du\u25AF\u25B4own\xBB\u045Ap\xBB\u0966eftarrows;\u61C7ight\u0180ahs\u25CD\u25D6\u25DErrow\u0100;s\u08F4\u08A7arpoon\xF3\u0F98quigarro\xF7\u21F0hreetimes;\u62CB\u0180;qs\u258B\u0993\u25FAlan\xF4\u09AC\u0280;cdgs\u09AC\u260A\u260D\u261D\u2628c;\u6AA8ot\u0100;o\u2614\u2615\u6A7F\u0100;r\u261A\u261B\u6A81;\u6A83\u0100;e\u2622\u2625\uC000\u22DA\uFE00s;\u6A93\u0280adegs\u2633\u2639\u263D\u2649\u264Bppro\xF8\u24C6ot;\u62D6q\u0100gq\u2643\u2645\xF4\u0989gt\xF2\u248C\xF4\u099Bi\xED\u09B2\u0180ilr\u2655\u08E1\u265Asht;\u697C;\uC000\u{1D529}\u0100;E\u099C\u2663;\u6A91\u0161\u2669\u2676r\u0100du\u25B2\u266E\u0100;l\u0965\u2673;\u696Alk;\u6584cy;\u4459\u0280;acht\u0A48\u2688\u268B\u2691\u2696r\xF2\u25C1orne\xF2\u1D08ard;\u696Bri;\u65FA\u0100io\u269F\u26A4dot;\u4140ust\u0100;a\u26AC\u26AD\u63B0che\xBB\u26AD\u0200Eaes\u26BB\u26BD\u26C9\u26D4;\u6268p\u0100;p\u26C3\u26C4\u6A89rox\xBB\u26C4\u0100;q\u26CE\u26CF\u6A87\u0100;q\u26CE\u26BBim;\u62E6\u0400abnoptwz\u26E9\u26F4\u26F7\u271A\u272F\u2741\u2747\u2750\u0100nr\u26EE\u26F1g;\u67ECr;\u61FDr\xEB\u08C1g\u0180lmr\u26FF\u270D\u2714eft\u0100ar\u09E6\u2707ight\xE1\u09F2apsto;\u67FCight\xE1\u09FDparrow\u0100lr\u2725\u2729ef\xF4\u24EDight;\u61AC\u0180afl\u2736\u2739\u273Dr;\u6985;\uC000\u{1D55D}us;\u6A2Dimes;\u6A34\u0161\u274B\u274Fst;\u6217\xE1\u134E\u0180;ef\u2757\u2758\u1800\u65CAnge\xBB\u2758ar\u0100;l\u2764\u2765\u4028t;\u6993\u0280achmt\u2773\u2776\u277C\u2785\u2787r\xF2\u08A8orne\xF2\u1D8Car\u0100;d\u0F98\u2783;\u696D;\u600Eri;\u62BF\u0300achiqt\u2798\u279D\u0A40\u27A2\u27AE\u27BBquo;\u6039r;\uC000\u{1D4C1}m\u0180;eg\u09B2\u27AA\u27AC;\u6A8D;\u6A8F\u0100bu\u252A\u27B3o\u0100;r\u0E1F\u27B9;\u601Arok;\u4142\u8400<;cdhilqr\u082B\u27D2\u2639\u27DC\u27E0\u27E5\u27EA\u27F0\u0100ci\u27D7\u27D9;\u6AA6r;\u6A79re\xE5\u25F2mes;\u62C9arr;\u6976uest;\u6A7B\u0100Pi\u27F5\u27F9ar;\u6996\u0180;ef\u2800\u092D\u181B\u65C3r\u0100du\u2807\u280Dshar;\u694Ahar;\u6966\u0100en\u2817\u2821rtneqq;\uC000\u2268\uFE00\xC5\u281E\u0700Dacdefhilnopsu\u2840\u2845\u2882\u288E\u2893\u28A0\u28A5\u28A8\u28DA\u28E2\u28E4\u0A83\u28F3\u2902Dot;\u623A\u0200clpr\u284E\u2852\u2863\u287Dr\u803B\xAF\u40AF\u0100et\u2857\u2859;\u6642\u0100;e\u285E\u285F\u6720se\xBB\u285F\u0100;s\u103B\u2868to\u0200;dlu\u103B\u2873\u2877\u287Bow\xEE\u048Cef\xF4\u090F\xF0\u13D1ker;\u65AE\u0100oy\u2887\u288Cmma;\u6A29;\u443Cash;\u6014asuredangle\xBB\u1626r;\uC000\u{1D52A}o;\u6127\u0180cdn\u28AF\u28B4\u28C9ro\u803B\xB5\u40B5\u0200;acd\u1464\u28BD\u28C0\u28C4s\xF4\u16A7ir;\u6AF0ot\u80BB\xB7\u01B5us\u0180;bd\u28D2\u1903\u28D3\u6212\u0100;u\u1D3C\u28D8;\u6A2A\u0163\u28DE\u28E1p;\u6ADB\xF2\u2212\xF0\u0A81\u0100dp\u28E9\u28EEels;\u62A7f;\uC000\u{1D55E}\u0100ct\u28F8\u28FDr;\uC000\u{1D4C2}pos\xBB\u159D\u0180;lm\u2909\u290A\u290D\u43BCtimap;\u62B8\u0C00GLRVabcdefghijlmoprstuvw\u2942\u2953\u297E\u2989\u2998\u29DA\u29E9\u2A15\u2A1A\u2A58\u2A5D\u2A83\u2A95\u2AA4\u2AA8\u2B04\u2B07\u2B44\u2B7F\u2BAE\u2C34\u2C67\u2C7C\u2CE9\u0100gt\u2947\u294B;\uC000\u22D9\u0338\u0100;v\u2950\u0BCF\uC000\u226B\u20D2\u0180elt\u295A\u2972\u2976ft\u0100ar\u2961\u2967rrow;\u61CDightarrow;\u61CE;\uC000\u22D8\u0338\u0100;v\u297B\u0C47\uC000\u226A\u20D2ightarrow;\u61CF\u0100Dd\u298E\u2993ash;\u62AFash;\u62AE\u0280bcnpt\u29A3\u29A7\u29AC\u29B1\u29CCla\xBB\u02DEute;\u4144g;\uC000\u2220\u20D2\u0280;Eiop\u0D84\u29BC\u29C0\u29C5\u29C8;\uC000\u2A70\u0338d;\uC000\u224B\u0338s;\u4149ro\xF8\u0D84ur\u0100;a\u29D3\u29D4\u666El\u0100;s\u29D3\u0B38\u01F3\u29DF\0\u29E3p\u80BB\xA0\u0B37mp\u0100;e\u0BF9\u0C00\u0280aeouy\u29F4\u29FE\u2A03\u2A10\u2A13\u01F0\u29F9\0\u29FB;\u6A43on;\u4148dil;\u4146ng\u0100;d\u0D7E\u2A0Aot;\uC000\u2A6D\u0338p;\u6A42;\u443Dash;\u6013\u0380;Aadqsx\u0B92\u2A29\u2A2D\u2A3B\u2A41\u2A45\u2A50rr;\u61D7r\u0100hr\u2A33\u2A36k;\u6924\u0100;o\u13F2\u13F0ot;\uC000\u2250\u0338ui\xF6\u0B63\u0100ei\u2A4A\u2A4Ear;\u6928\xED\u0B98ist\u0100;s\u0BA0\u0B9Fr;\uC000\u{1D52B}\u0200Eest\u0BC5\u2A66\u2A79\u2A7C\u0180;qs\u0BBC\u2A6D\u0BE1\u0180;qs\u0BBC\u0BC5\u2A74lan\xF4\u0BE2i\xED\u0BEA\u0100;r\u0BB6\u2A81\xBB\u0BB7\u0180Aap\u2A8A\u2A8D\u2A91r\xF2\u2971rr;\u61AEar;\u6AF2\u0180;sv\u0F8D\u2A9C\u0F8C\u0100;d\u2AA1\u2AA2\u62FC;\u62FAcy;\u445A\u0380AEadest\u2AB7\u2ABA\u2ABE\u2AC2\u2AC5\u2AF6\u2AF9r\xF2\u2966;\uC000\u2266\u0338rr;\u619Ar;\u6025\u0200;fqs\u0C3B\u2ACE\u2AE3\u2AEFt\u0100ar\u2AD4\u2AD9rro\xF7\u2AC1ightarro\xF7\u2A90\u0180;qs\u0C3B\u2ABA\u2AEAlan\xF4\u0C55\u0100;s\u0C55\u2AF4\xBB\u0C36i\xED\u0C5D\u0100;r\u0C35\u2AFEi\u0100;e\u0C1A\u0C25i\xE4\u0D90\u0100pt\u2B0C\u2B11f;\uC000\u{1D55F}\u8180\xAC;in\u2B19\u2B1A\u2B36\u40ACn\u0200;Edv\u0B89\u2B24\u2B28\u2B2E;\uC000\u22F9\u0338ot;\uC000\u22F5\u0338\u01E1\u0B89\u2B33\u2B35;\u62F7;\u62F6i\u0100;v\u0CB8\u2B3C\u01E1\u0CB8\u2B41\u2B43;\u62FE;\u62FD\u0180aor\u2B4B\u2B63\u2B69r\u0200;ast\u0B7B\u2B55\u2B5A\u2B5Flle\xEC\u0B7Bl;\uC000\u2AFD\u20E5;\uC000\u2202\u0338lint;\u6A14\u0180;ce\u0C92\u2B70\u2B73u\xE5\u0CA5\u0100;c\u0C98\u2B78\u0100;e\u0C92\u2B7D\xF1\u0C98\u0200Aait\u2B88\u2B8B\u2B9D\u2BA7r\xF2\u2988rr\u0180;cw\u2B94\u2B95\u2B99\u619B;\uC000\u2933\u0338;\uC000\u219D\u0338ghtarrow\xBB\u2B95ri\u0100;e\u0CCB\u0CD6\u0380chimpqu\u2BBD\u2BCD\u2BD9\u2B04\u0B78\u2BE4\u2BEF\u0200;cer\u0D32\u2BC6\u0D37\u2BC9u\xE5\u0D45;\uC000\u{1D4C3}ort\u026D\u2B05\0\0\u2BD6ar\xE1\u2B56m\u0100;e\u0D6E\u2BDF\u0100;q\u0D74\u0D73su\u0100bp\u2BEB\u2BED\xE5\u0CF8\xE5\u0D0B\u0180bcp\u2BF6\u2C11\u2C19\u0200;Ees\u2BFF\u2C00\u0D22\u2C04\u6284;\uC000\u2AC5\u0338et\u0100;e\u0D1B\u2C0Bq\u0100;q\u0D23\u2C00c\u0100;e\u0D32\u2C17\xF1\u0D38\u0200;Ees\u2C22\u2C23\u0D5F\u2C27\u6285;\uC000\u2AC6\u0338et\u0100;e\u0D58\u2C2Eq\u0100;q\u0D60\u2C23\u0200gilr\u2C3D\u2C3F\u2C45\u2C47\xEC\u0BD7lde\u803B\xF1\u40F1\xE7\u0C43iangle\u0100lr\u2C52\u2C5Ceft\u0100;e\u0C1A\u2C5A\xF1\u0C26ight\u0100;e\u0CCB\u2C65\xF1\u0CD7\u0100;m\u2C6C\u2C6D\u43BD\u0180;es\u2C74\u2C75\u2C79\u4023ro;\u6116p;\u6007\u0480DHadgilrs\u2C8F\u2C94\u2C99\u2C9E\u2CA3\u2CB0\u2CB6\u2CD3\u2CE3ash;\u62ADarr;\u6904p;\uC000\u224D\u20D2ash;\u62AC\u0100et\u2CA8\u2CAC;\uC000\u2265\u20D2;\uC000>\u20D2nfin;\u69DE\u0180Aet\u2CBD\u2CC1\u2CC5rr;\u6902;\uC000\u2264\u20D2\u0100;r\u2CCA\u2CCD\uC000<\u20D2ie;\uC000\u22B4\u20D2\u0100At\u2CD8\u2CDCrr;\u6903rie;\uC000\u22B5\u20D2im;\uC000\u223C\u20D2\u0180Aan\u2CF0\u2CF4\u2D02rr;\u61D6r\u0100hr\u2CFA\u2CFDk;\u6923\u0100;o\u13E7\u13E5ear;\u6927\u1253\u1A95\0\0\0\0\0\0\0\0\0\0\0\0\0\u2D2D\0\u2D38\u2D48\u2D60\u2D65\u2D72\u2D84\u1B07\0\0\u2D8D\u2DAB\0\u2DC8\u2DCE\0\u2DDC\u2E19\u2E2B\u2E3E\u2E43\u0100cs\u2D31\u1A97ute\u803B\xF3\u40F3\u0100iy\u2D3C\u2D45r\u0100;c\u1A9E\u2D42\u803B\xF4\u40F4;\u443E\u0280abios\u1AA0\u2D52\u2D57\u01C8\u2D5Alac;\u4151v;\u6A38old;\u69BClig;\u4153\u0100cr\u2D69\u2D6Dir;\u69BF;\uC000\u{1D52C}\u036F\u2D79\0\0\u2D7C\0\u2D82n;\u42DBave\u803B\xF2\u40F2;\u69C1\u0100bm\u2D88\u0DF4ar;\u69B5\u0200acit\u2D95\u2D98\u2DA5\u2DA8r\xF2\u1A80\u0100ir\u2D9D\u2DA0r;\u69BEoss;\u69BBn\xE5\u0E52;\u69C0\u0180aei\u2DB1\u2DB5\u2DB9cr;\u414Dga;\u43C9\u0180cdn\u2DC0\u2DC5\u01CDron;\u43BF;\u69B6pf;\uC000\u{1D560}\u0180ael\u2DD4\u2DD7\u01D2r;\u69B7rp;\u69B9\u0380;adiosv\u2DEA\u2DEB\u2DEE\u2E08\u2E0D\u2E10\u2E16\u6228r\xF2\u1A86\u0200;efm\u2DF7\u2DF8\u2E02\u2E05\u6A5Dr\u0100;o\u2DFE\u2DFF\u6134f\xBB\u2DFF\u803B\xAA\u40AA\u803B\xBA\u40BAgof;\u62B6r;\u6A56lope;\u6A57;\u6A5B\u0180clo\u2E1F\u2E21\u2E27\xF2\u2E01ash\u803B\xF8\u40F8l;\u6298i\u016C\u2E2F\u2E34de\u803B\xF5\u40F5es\u0100;a\u01DB\u2E3As;\u6A36ml\u803B\xF6\u40F6bar;\u633D\u0AE1\u2E5E\0\u2E7D\0\u2E80\u2E9D\0\u2EA2\u2EB9\0\0\u2ECB\u0E9C\0\u2F13\0\0\u2F2B\u2FBC\0\u2FC8r\u0200;ast\u0403\u2E67\u2E72\u0E85\u8100\xB6;l\u2E6D\u2E6E\u40B6le\xEC\u0403\u0269\u2E78\0\0\u2E7Bm;\u6AF3;\u6AFDy;\u443Fr\u0280cimpt\u2E8B\u2E8F\u2E93\u1865\u2E97nt;\u4025od;\u402Eil;\u6030enk;\u6031r;\uC000\u{1D52D}\u0180imo\u2EA8\u2EB0\u2EB4\u0100;v\u2EAD\u2EAE\u43C6;\u43D5ma\xF4\u0A76ne;\u660E\u0180;tv\u2EBF\u2EC0\u2EC8\u43C0chfork\xBB\u1FFD;\u43D6\u0100au\u2ECF\u2EDFn\u0100ck\u2ED5\u2EDDk\u0100;h\u21F4\u2EDB;\u610E\xF6\u21F4s\u0480;abcdemst\u2EF3\u2EF4\u1908\u2EF9\u2EFD\u2F04\u2F06\u2F0A\u2F0E\u402Bcir;\u6A23ir;\u6A22\u0100ou\u1D40\u2F02;\u6A25;\u6A72n\u80BB\xB1\u0E9Dim;\u6A26wo;\u6A27\u0180ipu\u2F19\u2F20\u2F25ntint;\u6A15f;\uC000\u{1D561}nd\u803B\xA3\u40A3\u0500;Eaceinosu\u0EC8\u2F3F\u2F41\u2F44\u2F47\u2F81\u2F89\u2F92\u2F7E\u2FB6;\u6AB3p;\u6AB7u\xE5\u0ED9\u0100;c\u0ECE\u2F4C\u0300;acens\u0EC8\u2F59\u2F5F\u2F66\u2F68\u2F7Eppro\xF8\u2F43urlye\xF1\u0ED9\xF1\u0ECE\u0180aes\u2F6F\u2F76\u2F7Approx;\u6AB9qq;\u6AB5im;\u62E8i\xED\u0EDFme\u0100;s\u2F88\u0EAE\u6032\u0180Eas\u2F78\u2F90\u2F7A\xF0\u2F75\u0180dfp\u0EEC\u2F99\u2FAF\u0180als\u2FA0\u2FA5\u2FAAlar;\u632Eine;\u6312urf;\u6313\u0100;t\u0EFB\u2FB4\xEF\u0EFBrel;\u62B0\u0100ci\u2FC0\u2FC5r;\uC000\u{1D4C5};\u43C8ncsp;\u6008\u0300fiopsu\u2FDA\u22E2\u2FDF\u2FE5\u2FEB\u2FF1r;\uC000\u{1D52E}pf;\uC000\u{1D562}rime;\u6057cr;\uC000\u{1D4C6}\u0180aeo\u2FF8\u3009\u3013t\u0100ei\u2FFE\u3005rnion\xF3\u06B0nt;\u6A16st\u0100;e\u3010\u3011\u403F\xF1\u1F19\xF4\u0F14\u0A80ABHabcdefhilmnoprstux\u3040\u3051\u3055\u3059\u30E0\u310E\u312B\u3147\u3162\u3172\u318E\u3206\u3215\u3224\u3229\u3258\u326E\u3272\u3290\u32B0\u32B7\u0180art\u3047\u304A\u304Cr\xF2\u10B3\xF2\u03DDail;\u691Car\xF2\u1C65ar;\u6964\u0380cdenqrt\u3068\u3075\u3078\u307F\u308F\u3094\u30CC\u0100eu\u306D\u3071;\uC000\u223D\u0331te;\u4155i\xE3\u116Emptyv;\u69B3g\u0200;del\u0FD1\u3089\u308B\u308D;\u6992;\u69A5\xE5\u0FD1uo\u803B\xBB\u40BBr\u0580;abcfhlpstw\u0FDC\u30AC\u30AF\u30B7\u30B9\u30BC\u30BE\u30C0\u30C3\u30C7\u30CAp;\u6975\u0100;f\u0FE0\u30B4s;\u6920;\u6933s;\u691E\xEB\u225D\xF0\u272El;\u6945im;\u6974l;\u61A3;\u619D\u0100ai\u30D1\u30D5il;\u691Ao\u0100;n\u30DB\u30DC\u6236al\xF3\u0F1E\u0180abr\u30E7\u30EA\u30EEr\xF2\u17E5rk;\u6773\u0100ak\u30F3\u30FDc\u0100ek\u30F9\u30FB;\u407D;\u405D\u0100es\u3102\u3104;\u698Cl\u0100du\u310A\u310C;\u698E;\u6990\u0200aeuy\u3117\u311C\u3127\u3129ron;\u4159\u0100di\u3121\u3125il;\u4157\xEC\u0FF2\xE2\u30FA;\u4440\u0200clqs\u3134\u3137\u313D\u3144a;\u6937dhar;\u6969uo\u0100;r\u020E\u020Dh;\u61B3\u0180acg\u314E\u315F\u0F44l\u0200;ips\u0F78\u3158\u315B\u109Cn\xE5\u10BBar\xF4\u0FA9t;\u65AD\u0180ilr\u3169\u1023\u316Esht;\u697D;\uC000\u{1D52F}\u0100ao\u3177\u3186r\u0100du\u317D\u317F\xBB\u047B\u0100;l\u1091\u3184;\u696C\u0100;v\u318B\u318C\u43C1;\u43F1\u0180gns\u3195\u31F9\u31FCht\u0300ahlrst\u31A4\u31B0\u31C2\u31D8\u31E4\u31EErrow\u0100;t\u0FDC\u31ADa\xE9\u30C8arpoon\u0100du\u31BB\u31BFow\xEE\u317Ep\xBB\u1092eft\u0100ah\u31CA\u31D0rrow\xF3\u0FEAarpoon\xF3\u0551ightarrows;\u61C9quigarro\xF7\u30CBhreetimes;\u62CCg;\u42DAingdotse\xF1\u1F32\u0180ahm\u320D\u3210\u3213r\xF2\u0FEAa\xF2\u0551;\u600Foust\u0100;a\u321E\u321F\u63B1che\xBB\u321Fmid;\u6AEE\u0200abpt\u3232\u323D\u3240\u3252\u0100nr\u3237\u323Ag;\u67EDr;\u61FEr\xEB\u1003\u0180afl\u3247\u324A\u324Er;\u6986;\uC000\u{1D563}us;\u6A2Eimes;\u6A35\u0100ap\u325D\u3267r\u0100;g\u3263\u3264\u4029t;\u6994olint;\u6A12ar\xF2\u31E3\u0200achq\u327B\u3280\u10BC\u3285quo;\u603Ar;\uC000\u{1D4C7}\u0100bu\u30FB\u328Ao\u0100;r\u0214\u0213\u0180hir\u3297\u329B\u32A0re\xE5\u31F8mes;\u62CAi\u0200;efl\u32AA\u1059\u1821\u32AB\u65B9tri;\u69CEluhar;\u6968;\u611E\u0D61\u32D5\u32DB\u32DF\u332C\u3338\u3371\0\u337A\u33A4\0\0\u33EC\u33F0\0\u3428\u3448\u345A\u34AD\u34B1\u34CA\u34F1\0\u3616\0\0\u3633cute;\u415Bqu\xEF\u27BA\u0500;Eaceinpsy\u11ED\u32F3\u32F5\u32FF\u3302\u330B\u330F\u331F\u3326\u3329;\u6AB4\u01F0\u32FA\0\u32FC;\u6AB8on;\u4161u\xE5\u11FE\u0100;d\u11F3\u3307il;\u415Frc;\u415D\u0180Eas\u3316\u3318\u331B;\u6AB6p;\u6ABAim;\u62E9olint;\u6A13i\xED\u1204;\u4441ot\u0180;be\u3334\u1D47\u3335\u62C5;\u6A66\u0380Aacmstx\u3346\u334A\u3357\u335B\u335E\u3363\u336Drr;\u61D8r\u0100hr\u3350\u3352\xEB\u2228\u0100;o\u0A36\u0A34t\u803B\xA7\u40A7i;\u403Bwar;\u6929m\u0100in\u3369\xF0nu\xF3\xF1t;\u6736r\u0100;o\u3376\u2055\uC000\u{1D530}\u0200acoy\u3382\u3386\u3391\u33A0rp;\u666F\u0100hy\u338B\u338Fcy;\u4449;\u4448rt\u026D\u3399\0\0\u339Ci\xE4\u1464ara\xEC\u2E6F\u803B\xAD\u40AD\u0100gm\u33A8\u33B4ma\u0180;fv\u33B1\u33B2\u33B2\u43C3;\u43C2\u0400;deglnpr\u12AB\u33C5\u33C9\u33CE\u33D6\u33DE\u33E1\u33E6ot;\u6A6A\u0100;q\u12B1\u12B0\u0100;E\u33D3\u33D4\u6A9E;\u6AA0\u0100;E\u33DB\u33DC\u6A9D;\u6A9Fe;\u6246lus;\u6A24arr;\u6972ar\xF2\u113D\u0200aeit\u33F8\u3408\u340F\u3417\u0100ls\u33FD\u3404lsetm\xE9\u336Ahp;\u6A33parsl;\u69E4\u0100dl\u1463\u3414e;\u6323\u0100;e\u341C\u341D\u6AAA\u0100;s\u3422\u3423\u6AAC;\uC000\u2AAC\uFE00\u0180flp\u342E\u3433\u3442tcy;\u444C\u0100;b\u3438\u3439\u402F\u0100;a\u343E\u343F\u69C4r;\u633Ff;\uC000\u{1D564}a\u0100dr\u344D\u0402es\u0100;u\u3454\u3455\u6660it\xBB\u3455\u0180csu\u3460\u3479\u349F\u0100au\u3465\u346Fp\u0100;s\u1188\u346B;\uC000\u2293\uFE00p\u0100;s\u11B4\u3475;\uC000\u2294\uFE00u\u0100bp\u347F\u348F\u0180;es\u1197\u119C\u3486et\u0100;e\u1197\u348D\xF1\u119D\u0180;es\u11A8\u11AD\u3496et\u0100;e\u11A8\u349D\xF1\u11AE\u0180;af\u117B\u34A6\u05B0r\u0165\u34AB\u05B1\xBB\u117Car\xF2\u1148\u0200cemt\u34B9\u34BE\u34C2\u34C5r;\uC000\u{1D4C8}tm\xEE\xF1i\xEC\u3415ar\xE6\u11BE\u0100ar\u34CE\u34D5r\u0100;f\u34D4\u17BF\u6606\u0100an\u34DA\u34EDight\u0100ep\u34E3\u34EApsilo\xEE\u1EE0h\xE9\u2EAFs\xBB\u2852\u0280bcmnp\u34FB\u355E\u1209\u358B\u358E\u0480;Edemnprs\u350E\u350F\u3511\u3515\u351E\u3523\u352C\u3531\u3536\u6282;\u6AC5ot;\u6ABD\u0100;d\u11DA\u351Aot;\u6AC3ult;\u6AC1\u0100Ee\u3528\u352A;\u6ACB;\u628Alus;\u6ABFarr;\u6979\u0180eiu\u353D\u3552\u3555t\u0180;en\u350E\u3545\u354Bq\u0100;q\u11DA\u350Feq\u0100;q\u352B\u3528m;\u6AC7\u0100bp\u355A\u355C;\u6AD5;\u6AD3c\u0300;acens\u11ED\u356C\u3572\u3579\u357B\u3326ppro\xF8\u32FAurlye\xF1\u11FE\xF1\u11F3\u0180aes\u3582\u3588\u331Bppro\xF8\u331Aq\xF1\u3317g;\u666A\u0680123;Edehlmnps\u35A9\u35AC\u35AF\u121C\u35B2\u35B4\u35C0\u35C9\u35D5\u35DA\u35DF\u35E8\u35ED\u803B\xB9\u40B9\u803B\xB2\u40B2\u803B\xB3\u40B3;\u6AC6\u0100os\u35B9\u35BCt;\u6ABEub;\u6AD8\u0100;d\u1222\u35C5ot;\u6AC4s\u0100ou\u35CF\u35D2l;\u67C9b;\u6AD7arr;\u697Bult;\u6AC2\u0100Ee\u35E4\u35E6;\u6ACC;\u628Blus;\u6AC0\u0180eiu\u35F4\u3609\u360Ct\u0180;en\u121C\u35FC\u3602q\u0100;q\u1222\u35B2eq\u0100;q\u35E7\u35E4m;\u6AC8\u0100bp\u3611\u3613;\u6AD4;\u6AD6\u0180Aan\u361C\u3620\u362Drr;\u61D9r\u0100hr\u3626\u3628\xEB\u222E\u0100;o\u0A2B\u0A29war;\u692Alig\u803B\xDF\u40DF\u0BE1\u3651\u365D\u3660\u12CE\u3673\u3679\0\u367E\u36C2\0\0\0\0\0\u36DB\u3703\0\u3709\u376C\0\0\0\u3787\u0272\u3656\0\0\u365Bget;\u6316;\u43C4r\xEB\u0E5F\u0180aey\u3666\u366B\u3670ron;\u4165dil;\u4163;\u4442lrec;\u6315r;\uC000\u{1D531}\u0200eiko\u3686\u369D\u36B5\u36BC\u01F2\u368B\0\u3691e\u01004f\u1284\u1281a\u0180;sv\u3698\u3699\u369B\u43B8ym;\u43D1\u0100cn\u36A2\u36B2k\u0100as\u36A8\u36AEppro\xF8\u12C1im\xBB\u12ACs\xF0\u129E\u0100as\u36BA\u36AE\xF0\u12C1rn\u803B\xFE\u40FE\u01EC\u031F\u36C6\u22E7es\u8180\xD7;bd\u36CF\u36D0\u36D8\u40D7\u0100;a\u190F\u36D5r;\u6A31;\u6A30\u0180eps\u36E1\u36E3\u3700\xE1\u2A4D\u0200;bcf\u0486\u36EC\u36F0\u36F4ot;\u6336ir;\u6AF1\u0100;o\u36F9\u36FC\uC000\u{1D565}rk;\u6ADA\xE1\u3362rime;\u6034\u0180aip\u370F\u3712\u3764d\xE5\u1248\u0380adempst\u3721\u374D\u3740\u3751\u3757\u375C\u375Fngle\u0280;dlqr\u3730\u3731\u3736\u3740\u3742\u65B5own\xBB\u1DBBeft\u0100;e\u2800\u373E\xF1\u092E;\u625Cight\u0100;e\u32AA\u374B\xF1\u105Aot;\u65ECinus;\u6A3Alus;\u6A39b;\u69CDime;\u6A3Bezium;\u63E2\u0180cht\u3772\u377D\u3781\u0100ry\u3777\u377B;\uC000\u{1D4C9};\u4446cy;\u445Brok;\u4167\u0100io\u378B\u378Ex\xF4\u1777head\u0100lr\u3797\u37A0eftarro\xF7\u084Fightarrow\xBB\u0F5D\u0900AHabcdfghlmoprstuw\u37D0\u37D3\u37D7\u37E4\u37F0\u37FC\u380E\u381C\u3823\u3834\u3851\u385D\u386B\u38A9\u38CC\u38D2\u38EA\u38F6r\xF2\u03EDar;\u6963\u0100cr\u37DC\u37E2ute\u803B\xFA\u40FA\xF2\u1150r\u01E3\u37EA\0\u37EDy;\u445Eve;\u416D\u0100iy\u37F5\u37FArc\u803B\xFB\u40FB;\u4443\u0180abh\u3803\u3806\u380Br\xF2\u13ADlac;\u4171a\xF2\u13C3\u0100ir\u3813\u3818sht;\u697E;\uC000\u{1D532}rave\u803B\xF9\u40F9\u0161\u3827\u3831r\u0100lr\u382C\u382E\xBB\u0957\xBB\u1083lk;\u6580\u0100ct\u3839\u384D\u026F\u383F\0\0\u384Arn\u0100;e\u3845\u3846\u631Cr\xBB\u3846op;\u630Fri;\u65F8\u0100al\u3856\u385Acr;\u416B\u80BB\xA8\u0349\u0100gp\u3862\u3866on;\u4173f;\uC000\u{1D566}\u0300adhlsu\u114B\u3878\u387D\u1372\u3891\u38A0own\xE1\u13B3arpoon\u0100lr\u3888\u388Cef\xF4\u382Digh\xF4\u382Fi\u0180;hl\u3899\u389A\u389C\u43C5\xBB\u13FAon\xBB\u389Aparrows;\u61C8\u0180cit\u38B0\u38C4\u38C8\u026F\u38B6\0\0\u38C1rn\u0100;e\u38BC\u38BD\u631Dr\xBB\u38BDop;\u630Eng;\u416Fri;\u65F9cr;\uC000\u{1D4CA}\u0180dir\u38D9\u38DD\u38E2ot;\u62F0lde;\u4169i\u0100;f\u3730\u38E8\xBB\u1813\u0100am\u38EF\u38F2r\xF2\u38A8l\u803B\xFC\u40FCangle;\u69A7\u0780ABDacdeflnoprsz\u391C\u391F\u3929\u392D\u39B5\u39B8\u39BD\u39DF\u39E4\u39E8\u39F3\u39F9\u39FD\u3A01\u3A20r\xF2\u03F7ar\u0100;v\u3926\u3927\u6AE8;\u6AE9as\xE8\u03E1\u0100nr\u3932\u3937grt;\u699C\u0380eknprst\u34E3\u3946\u394B\u3952\u395D\u3964\u3996app\xE1\u2415othin\xE7\u1E96\u0180hir\u34EB\u2EC8\u3959op\xF4\u2FB5\u0100;h\u13B7\u3962\xEF\u318D\u0100iu\u3969\u396Dgm\xE1\u33B3\u0100bp\u3972\u3984setneq\u0100;q\u397D\u3980\uC000\u228A\uFE00;\uC000\u2ACB\uFE00setneq\u0100;q\u398F\u3992\uC000\u228B\uFE00;\uC000\u2ACC\uFE00\u0100hr\u399B\u399Fet\xE1\u369Ciangle\u0100lr\u39AA\u39AFeft\xBB\u0925ight\xBB\u1051y;\u4432ash\xBB\u1036\u0180elr\u39C4\u39D2\u39D7\u0180;be\u2DEA\u39CB\u39CFar;\u62BBq;\u625Alip;\u62EE\u0100bt\u39DC\u1468a\xF2\u1469r;\uC000\u{1D533}tr\xE9\u39AEsu\u0100bp\u39EF\u39F1\xBB\u0D1C\xBB\u0D59pf;\uC000\u{1D567}ro\xF0\u0EFBtr\xE9\u39B4\u0100cu\u3A06\u3A0Br;\uC000\u{1D4CB}\u0100bp\u3A10\u3A18n\u0100Ee\u3980\u3A16\xBB\u397En\u0100Ee\u3992\u3A1E\xBB\u3990igzag;\u699A\u0380cefoprs\u3A36\u3A3B\u3A56\u3A5B\u3A54\u3A61\u3A6Airc;\u4175\u0100di\u3A40\u3A51\u0100bg\u3A45\u3A49ar;\u6A5Fe\u0100;q\u15FA\u3A4F;\u6259erp;\u6118r;\uC000\u{1D534}pf;\uC000\u{1D568}\u0100;e\u1479\u3A66at\xE8\u1479cr;\uC000\u{1D4CC}\u0AE3\u178E\u3A87\0\u3A8B\0\u3A90\u3A9B\0\0\u3A9D\u3AA8\u3AAB\u3AAF\0\0\u3AC3\u3ACE\0\u3AD8\u17DC\u17DFtr\xE9\u17D1r;\uC000\u{1D535}\u0100Aa\u3A94\u3A97r\xF2\u03C3r\xF2\u09F6;\u43BE\u0100Aa\u3AA1\u3AA4r\xF2\u03B8r\xF2\u09EBa\xF0\u2713is;\u62FB\u0180dpt\u17A4\u3AB5\u3ABE\u0100fl\u3ABA\u17A9;\uC000\u{1D569}im\xE5\u17B2\u0100Aa\u3AC7\u3ACAr\xF2\u03CEr\xF2\u0A01\u0100cq\u3AD2\u17B8r;\uC000\u{1D4CD}\u0100pt\u17D6\u3ADCr\xE9\u17D4\u0400acefiosu\u3AF0\u3AFD\u3B08\u3B0C\u3B11\u3B15\u3B1B\u3B21c\u0100uy\u3AF6\u3AFBte\u803B\xFD\u40FD;\u444F\u0100iy\u3B02\u3B06rc;\u4177;\u444Bn\u803B\xA5\u40A5r;\uC000\u{1D536}cy;\u4457pf;\uC000\u{1D56A}cr;\uC000\u{1D4CE}\u0100cm\u3B26\u3B29y;\u444El\u803B\xFF\u40FF\u0500acdefhiosw\u3B42\u3B48\u3B54\u3B58\u3B64\u3B69\u3B6D\u3B74\u3B7A\u3B80cute;\u417A\u0100ay\u3B4D\u3B52ron;\u417E;\u4437ot;\u417C\u0100et\u3B5D\u3B61tr\xE6\u155Fa;\u43B6r;\uC000\u{1D537}cy;\u4436grarr;\u61DDpf;\uC000\u{1D56B}cr;\uC000\u{1D4CF}\u0100jn\u3B85\u3B87;\u600Dj;\u600C'.split("").map(function(c) {
          return c.charCodeAt(0);
        })
      );
    }
  });

  // node_modules/entities/lib/generated/decode-data-xml.js
  var require_decode_data_xml = __commonJS({
    "node_modules/entities/lib/generated/decode-data-xml.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.default = new Uint16Array(
        // prettier-ignore
        "\u0200aglq	\x1B\u026D\0\0p;\u4026os;\u4027t;\u403Et;\u403Cuot;\u4022".split("").map(function(c) {
          return c.charCodeAt(0);
        })
      );
    }
  });

  // node_modules/entities/lib/decode_codepoint.js
  var require_decode_codepoint = __commonJS({
    "node_modules/entities/lib/decode_codepoint.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var _a;
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.replaceCodePoint = exports4.fromCodePoint = void 0;
      var decodeMap = /* @__PURE__ */ new Map([
        [0, 65533],
        // C1 Unicode control character reference replacements
        [128, 8364],
        [130, 8218],
        [131, 402],
        [132, 8222],
        [133, 8230],
        [134, 8224],
        [135, 8225],
        [136, 710],
        [137, 8240],
        [138, 352],
        [139, 8249],
        [140, 338],
        [142, 381],
        [145, 8216],
        [146, 8217],
        [147, 8220],
        [148, 8221],
        [149, 8226],
        [150, 8211],
        [151, 8212],
        [152, 732],
        [153, 8482],
        [154, 353],
        [155, 8250],
        [156, 339],
        [158, 382],
        [159, 376]
      ]);
      exports4.fromCodePoint = // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition, node/no-unsupported-features/es-builtins
      (_a = String.fromCodePoint) !== null && _a !== void 0 ? _a : function(codePoint) {
        var output = "";
        if (codePoint > 65535) {
          codePoint -= 65536;
          output += String.fromCharCode(codePoint >>> 10 & 1023 | 55296);
          codePoint = 56320 | codePoint & 1023;
        }
        output += String.fromCharCode(codePoint);
        return output;
      };
      function replaceCodePoint(codePoint) {
        var _a2;
        if (codePoint >= 55296 && codePoint <= 57343 || codePoint > 1114111) {
          return 65533;
        }
        return (_a2 = decodeMap.get(codePoint)) !== null && _a2 !== void 0 ? _a2 : codePoint;
      }
      __name(replaceCodePoint, "replaceCodePoint");
      exports4.replaceCodePoint = replaceCodePoint;
      function decodeCodePoint(codePoint) {
        return (0, exports4.fromCodePoint)(replaceCodePoint(codePoint));
      }
      __name(decodeCodePoint, "decodeCodePoint");
      exports4.default = decodeCodePoint;
    }
  });

  // node_modules/entities/lib/decode.js
  var require_decode = __commonJS({
    "node_modules/entities/lib/decode.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.decodeXML = exports4.decodeHTMLStrict = exports4.decodeHTMLAttribute = exports4.decodeHTML = exports4.determineBranch = exports4.EntityDecoder = exports4.DecodingMode = exports4.BinTrieFlags = exports4.fromCodePoint = exports4.replaceCodePoint = exports4.decodeCodePoint = exports4.xmlDecodeTree = exports4.htmlDecodeTree = void 0;
      var decode_data_html_js_1 = __importDefault(require_decode_data_html());
      exports4.htmlDecodeTree = decode_data_html_js_1.default;
      var decode_data_xml_js_1 = __importDefault(require_decode_data_xml());
      exports4.xmlDecodeTree = decode_data_xml_js_1.default;
      var decode_codepoint_js_1 = __importStar(require_decode_codepoint());
      exports4.decodeCodePoint = decode_codepoint_js_1.default;
      var decode_codepoint_js_2 = require_decode_codepoint();
      Object.defineProperty(exports4, "replaceCodePoint", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_codepoint_js_2.replaceCodePoint;
      }, "get") });
      Object.defineProperty(exports4, "fromCodePoint", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_codepoint_js_2.fromCodePoint;
      }, "get") });
      var CharCodes;
      (function(CharCodes2) {
        CharCodes2[CharCodes2["NUM"] = 35] = "NUM";
        CharCodes2[CharCodes2["SEMI"] = 59] = "SEMI";
        CharCodes2[CharCodes2["EQUALS"] = 61] = "EQUALS";
        CharCodes2[CharCodes2["ZERO"] = 48] = "ZERO";
        CharCodes2[CharCodes2["NINE"] = 57] = "NINE";
        CharCodes2[CharCodes2["LOWER_A"] = 97] = "LOWER_A";
        CharCodes2[CharCodes2["LOWER_F"] = 102] = "LOWER_F";
        CharCodes2[CharCodes2["LOWER_X"] = 120] = "LOWER_X";
        CharCodes2[CharCodes2["LOWER_Z"] = 122] = "LOWER_Z";
        CharCodes2[CharCodes2["UPPER_A"] = 65] = "UPPER_A";
        CharCodes2[CharCodes2["UPPER_F"] = 70] = "UPPER_F";
        CharCodes2[CharCodes2["UPPER_Z"] = 90] = "UPPER_Z";
      })(CharCodes || (CharCodes = {}));
      var TO_LOWER_BIT = 32;
      var BinTrieFlags;
      (function(BinTrieFlags2) {
        BinTrieFlags2[BinTrieFlags2["VALUE_LENGTH"] = 49152] = "VALUE_LENGTH";
        BinTrieFlags2[BinTrieFlags2["BRANCH_LENGTH"] = 16256] = "BRANCH_LENGTH";
        BinTrieFlags2[BinTrieFlags2["JUMP_TABLE"] = 127] = "JUMP_TABLE";
      })(BinTrieFlags = exports4.BinTrieFlags || (exports4.BinTrieFlags = {}));
      function isNumber(code) {
        return code >= CharCodes.ZERO && code <= CharCodes.NINE;
      }
      __name(isNumber, "isNumber");
      function isHexadecimalCharacter(code) {
        return code >= CharCodes.UPPER_A && code <= CharCodes.UPPER_F || code >= CharCodes.LOWER_A && code <= CharCodes.LOWER_F;
      }
      __name(isHexadecimalCharacter, "isHexadecimalCharacter");
      function isAsciiAlphaNumeric2(code) {
        return code >= CharCodes.UPPER_A && code <= CharCodes.UPPER_Z || code >= CharCodes.LOWER_A && code <= CharCodes.LOWER_Z || isNumber(code);
      }
      __name(isAsciiAlphaNumeric2, "isAsciiAlphaNumeric");
      function isEntityInAttributeInvalidEnd(code) {
        return code === CharCodes.EQUALS || isAsciiAlphaNumeric2(code);
      }
      __name(isEntityInAttributeInvalidEnd, "isEntityInAttributeInvalidEnd");
      var EntityDecoderState;
      (function(EntityDecoderState2) {
        EntityDecoderState2[EntityDecoderState2["EntityStart"] = 0] = "EntityStart";
        EntityDecoderState2[EntityDecoderState2["NumericStart"] = 1] = "NumericStart";
        EntityDecoderState2[EntityDecoderState2["NumericDecimal"] = 2] = "NumericDecimal";
        EntityDecoderState2[EntityDecoderState2["NumericHex"] = 3] = "NumericHex";
        EntityDecoderState2[EntityDecoderState2["NamedEntity"] = 4] = "NamedEntity";
      })(EntityDecoderState || (EntityDecoderState = {}));
      var DecodingMode2;
      (function(DecodingMode3) {
        DecodingMode3[DecodingMode3["Legacy"] = 0] = "Legacy";
        DecodingMode3[DecodingMode3["Strict"] = 1] = "Strict";
        DecodingMode3[DecodingMode3["Attribute"] = 2] = "Attribute";
      })(DecodingMode2 = exports4.DecodingMode || (exports4.DecodingMode = {}));
      var EntityDecoder2 = (
        /** @class */
        function() {
          function EntityDecoder3(decodeTree, emitCodePoint, errors) {
            this.decodeTree = decodeTree;
            this.emitCodePoint = emitCodePoint;
            this.errors = errors;
            this.state = EntityDecoderState.EntityStart;
            this.consumed = 1;
            this.result = 0;
            this.treeIndex = 0;
            this.excess = 1;
            this.decodeMode = DecodingMode2.Strict;
          }
          __name(EntityDecoder3, "EntityDecoder");
          EntityDecoder3.prototype.startEntity = function(decodeMode) {
            this.decodeMode = decodeMode;
            this.state = EntityDecoderState.EntityStart;
            this.result = 0;
            this.treeIndex = 0;
            this.excess = 1;
            this.consumed = 1;
          };
          EntityDecoder3.prototype.write = function(str, offset) {
            switch (this.state) {
              case EntityDecoderState.EntityStart: {
                if (str.charCodeAt(offset) === CharCodes.NUM) {
                  this.state = EntityDecoderState.NumericStart;
                  this.consumed += 1;
                  return this.stateNumericStart(str, offset + 1);
                }
                this.state = EntityDecoderState.NamedEntity;
                return this.stateNamedEntity(str, offset);
              }
              case EntityDecoderState.NumericStart: {
                return this.stateNumericStart(str, offset);
              }
              case EntityDecoderState.NumericDecimal: {
                return this.stateNumericDecimal(str, offset);
              }
              case EntityDecoderState.NumericHex: {
                return this.stateNumericHex(str, offset);
              }
              case EntityDecoderState.NamedEntity: {
                return this.stateNamedEntity(str, offset);
              }
            }
          };
          EntityDecoder3.prototype.stateNumericStart = function(str, offset) {
            if (offset >= str.length) {
              return -1;
            }
            if ((str.charCodeAt(offset) | TO_LOWER_BIT) === CharCodes.LOWER_X) {
              this.state = EntityDecoderState.NumericHex;
              this.consumed += 1;
              return this.stateNumericHex(str, offset + 1);
            }
            this.state = EntityDecoderState.NumericDecimal;
            return this.stateNumericDecimal(str, offset);
          };
          EntityDecoder3.prototype.addToNumericResult = function(str, start, end2, base) {
            if (start !== end2) {
              var digitCount = end2 - start;
              this.result = this.result * Math.pow(base, digitCount) + parseInt(str.substr(start, digitCount), base);
              this.consumed += digitCount;
            }
          };
          EntityDecoder3.prototype.stateNumericHex = function(str, offset) {
            var startIdx = offset;
            while (offset < str.length) {
              var char = str.charCodeAt(offset);
              if (isNumber(char) || isHexadecimalCharacter(char)) {
                offset += 1;
              } else {
                this.addToNumericResult(str, startIdx, offset, 16);
                return this.emitNumericEntity(char, 3);
              }
            }
            this.addToNumericResult(str, startIdx, offset, 16);
            return -1;
          };
          EntityDecoder3.prototype.stateNumericDecimal = function(str, offset) {
            var startIdx = offset;
            while (offset < str.length) {
              var char = str.charCodeAt(offset);
              if (isNumber(char)) {
                offset += 1;
              } else {
                this.addToNumericResult(str, startIdx, offset, 10);
                return this.emitNumericEntity(char, 2);
              }
            }
            this.addToNumericResult(str, startIdx, offset, 10);
            return -1;
          };
          EntityDecoder3.prototype.emitNumericEntity = function(lastCp, expectedLength) {
            var _a;
            if (this.consumed <= expectedLength) {
              (_a = this.errors) === null || _a === void 0 ? void 0 : _a.absenceOfDigitsInNumericCharacterReference(this.consumed);
              return 0;
            }
            if (lastCp === CharCodes.SEMI) {
              this.consumed += 1;
            } else if (this.decodeMode === DecodingMode2.Strict) {
              return 0;
            }
            this.emitCodePoint((0, decode_codepoint_js_1.replaceCodePoint)(this.result), this.consumed);
            if (this.errors) {
              if (lastCp !== CharCodes.SEMI) {
                this.errors.missingSemicolonAfterCharacterReference();
              }
              this.errors.validateNumericCharacterReference(this.result);
            }
            return this.consumed;
          };
          EntityDecoder3.prototype.stateNamedEntity = function(str, offset) {
            var decodeTree = this.decodeTree;
            var current = decodeTree[this.treeIndex];
            var valueLength = (current & BinTrieFlags.VALUE_LENGTH) >> 14;
            for (; offset < str.length; offset++, this.excess++) {
              var char = str.charCodeAt(offset);
              this.treeIndex = determineBranch(decodeTree, current, this.treeIndex + Math.max(1, valueLength), char);
              if (this.treeIndex < 0) {
                return this.result === 0 || // If we are parsing an attribute
                this.decodeMode === DecodingMode2.Attribute && // We shouldn't have consumed any characters after the entity,
                (valueLength === 0 || // And there should be no invalid characters.
                isEntityInAttributeInvalidEnd(char)) ? 0 : this.emitNotTerminatedNamedEntity();
              }
              current = decodeTree[this.treeIndex];
              valueLength = (current & BinTrieFlags.VALUE_LENGTH) >> 14;
              if (valueLength !== 0) {
                if (char === CharCodes.SEMI) {
                  return this.emitNamedEntityData(this.treeIndex, valueLength, this.consumed + this.excess);
                }
                if (this.decodeMode !== DecodingMode2.Strict) {
                  this.result = this.treeIndex;
                  this.consumed += this.excess;
                  this.excess = 0;
                }
              }
            }
            return -1;
          };
          EntityDecoder3.prototype.emitNotTerminatedNamedEntity = function() {
            var _a;
            var _b = this, result = _b.result, decodeTree = _b.decodeTree;
            var valueLength = (decodeTree[result] & BinTrieFlags.VALUE_LENGTH) >> 14;
            this.emitNamedEntityData(result, valueLength, this.consumed);
            (_a = this.errors) === null || _a === void 0 ? void 0 : _a.missingSemicolonAfterCharacterReference();
            return this.consumed;
          };
          EntityDecoder3.prototype.emitNamedEntityData = function(result, valueLength, consumed) {
            var decodeTree = this.decodeTree;
            this.emitCodePoint(valueLength === 1 ? decodeTree[result] & ~BinTrieFlags.VALUE_LENGTH : decodeTree[result + 1], consumed);
            if (valueLength === 3) {
              this.emitCodePoint(decodeTree[result + 2], consumed);
            }
            return consumed;
          };
          EntityDecoder3.prototype.end = function() {
            var _a;
            switch (this.state) {
              case EntityDecoderState.NamedEntity: {
                return this.result !== 0 && (this.decodeMode !== DecodingMode2.Attribute || this.result === this.treeIndex) ? this.emitNotTerminatedNamedEntity() : 0;
              }
              // Otherwise, emit a numeric entity if we have one.
              case EntityDecoderState.NumericDecimal: {
                return this.emitNumericEntity(0, 2);
              }
              case EntityDecoderState.NumericHex: {
                return this.emitNumericEntity(0, 3);
              }
              case EntityDecoderState.NumericStart: {
                (_a = this.errors) === null || _a === void 0 ? void 0 : _a.absenceOfDigitsInNumericCharacterReference(this.consumed);
                return 0;
              }
              case EntityDecoderState.EntityStart: {
                return 0;
              }
            }
          };
          return EntityDecoder3;
        }()
      );
      exports4.EntityDecoder = EntityDecoder2;
      function getDecoder(decodeTree) {
        var ret = "";
        var decoder = new EntityDecoder2(decodeTree, function(str) {
          return ret += (0, decode_codepoint_js_1.fromCodePoint)(str);
        });
        return /* @__PURE__ */ __name(function decodeWithTrie(str, decodeMode) {
          var lastIndex = 0;
          var offset = 0;
          while ((offset = str.indexOf("&", offset)) >= 0) {
            ret += str.slice(lastIndex, offset);
            decoder.startEntity(decodeMode);
            var len = decoder.write(
              str,
              // Skip the "&"
              offset + 1
            );
            if (len < 0) {
              lastIndex = offset + decoder.end();
              break;
            }
            lastIndex = offset + len;
            offset = len === 0 ? lastIndex + 1 : lastIndex;
          }
          var result = ret + str.slice(lastIndex);
          ret = "";
          return result;
        }, "decodeWithTrie");
      }
      __name(getDecoder, "getDecoder");
      function determineBranch(decodeTree, current, nodeIdx, char) {
        var branchCount = (current & BinTrieFlags.BRANCH_LENGTH) >> 7;
        var jumpOffset = current & BinTrieFlags.JUMP_TABLE;
        if (branchCount === 0) {
          return jumpOffset !== 0 && char === jumpOffset ? nodeIdx : -1;
        }
        if (jumpOffset) {
          var value = char - jumpOffset;
          return value < 0 || value >= branchCount ? -1 : decodeTree[nodeIdx + value] - 1;
        }
        var lo = nodeIdx;
        var hi = lo + branchCount - 1;
        while (lo <= hi) {
          var mid = lo + hi >>> 1;
          var midVal = decodeTree[mid];
          if (midVal < char) {
            lo = mid + 1;
          } else if (midVal > char) {
            hi = mid - 1;
          } else {
            return decodeTree[mid + branchCount];
          }
        }
        return -1;
      }
      __name(determineBranch, "determineBranch");
      exports4.determineBranch = determineBranch;
      var htmlDecoder = getDecoder(decode_data_html_js_1.default);
      var xmlDecoder = getDecoder(decode_data_xml_js_1.default);
      function decodeHTML(str, mode) {
        if (mode === void 0) {
          mode = DecodingMode2.Legacy;
        }
        return htmlDecoder(str, mode);
      }
      __name(decodeHTML, "decodeHTML");
      exports4.decodeHTML = decodeHTML;
      function decodeHTMLAttribute(str) {
        return htmlDecoder(str, DecodingMode2.Attribute);
      }
      __name(decodeHTMLAttribute, "decodeHTMLAttribute");
      exports4.decodeHTMLAttribute = decodeHTMLAttribute;
      function decodeHTMLStrict(str) {
        return htmlDecoder(str, DecodingMode2.Strict);
      }
      __name(decodeHTMLStrict, "decodeHTMLStrict");
      exports4.decodeHTMLStrict = decodeHTMLStrict;
      function decodeXML(str) {
        return xmlDecoder(str, DecodingMode2.Strict);
      }
      __name(decodeXML, "decodeXML");
      exports4.decodeXML = decodeXML;
    }
  });

  // node_modules/entities/lib/generated/encode-html.js
  var require_encode_html = __commonJS({
    "node_modules/entities/lib/generated/encode-html.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      function restoreDiff(arr) {
        for (var i = 1; i < arr.length; i++) {
          arr[i][0] += arr[i - 1][0] + 1;
        }
        return arr;
      }
      __name(restoreDiff, "restoreDiff");
      exports4.default = new Map(/* @__PURE__ */ restoreDiff([[9, "&Tab;"], [0, "&NewLine;"], [22, "&excl;"], [0, "&quot;"], [0, "&num;"], [0, "&dollar;"], [0, "&percnt;"], [0, "&amp;"], [0, "&apos;"], [0, "&lpar;"], [0, "&rpar;"], [0, "&ast;"], [0, "&plus;"], [0, "&comma;"], [1, "&period;"], [0, "&sol;"], [10, "&colon;"], [0, "&semi;"], [0, { v: "&lt;", n: 8402, o: "&nvlt;" }], [0, { v: "&equals;", n: 8421, o: "&bne;" }], [0, { v: "&gt;", n: 8402, o: "&nvgt;" }], [0, "&quest;"], [0, "&commat;"], [26, "&lbrack;"], [0, "&bsol;"], [0, "&rbrack;"], [0, "&Hat;"], [0, "&lowbar;"], [0, "&DiacriticalGrave;"], [5, { n: 106, o: "&fjlig;" }], [20, "&lbrace;"], [0, "&verbar;"], [0, "&rbrace;"], [34, "&nbsp;"], [0, "&iexcl;"], [0, "&cent;"], [0, "&pound;"], [0, "&curren;"], [0, "&yen;"], [0, "&brvbar;"], [0, "&sect;"], [0, "&die;"], [0, "&copy;"], [0, "&ordf;"], [0, "&laquo;"], [0, "&not;"], [0, "&shy;"], [0, "&circledR;"], [0, "&macr;"], [0, "&deg;"], [0, "&PlusMinus;"], [0, "&sup2;"], [0, "&sup3;"], [0, "&acute;"], [0, "&micro;"], [0, "&para;"], [0, "&centerdot;"], [0, "&cedil;"], [0, "&sup1;"], [0, "&ordm;"], [0, "&raquo;"], [0, "&frac14;"], [0, "&frac12;"], [0, "&frac34;"], [0, "&iquest;"], [0, "&Agrave;"], [0, "&Aacute;"], [0, "&Acirc;"], [0, "&Atilde;"], [0, "&Auml;"], [0, "&angst;"], [0, "&AElig;"], [0, "&Ccedil;"], [0, "&Egrave;"], [0, "&Eacute;"], [0, "&Ecirc;"], [0, "&Euml;"], [0, "&Igrave;"], [0, "&Iacute;"], [0, "&Icirc;"], [0, "&Iuml;"], [0, "&ETH;"], [0, "&Ntilde;"], [0, "&Ograve;"], [0, "&Oacute;"], [0, "&Ocirc;"], [0, "&Otilde;"], [0, "&Ouml;"], [0, "&times;"], [0, "&Oslash;"], [0, "&Ugrave;"], [0, "&Uacute;"], [0, "&Ucirc;"], [0, "&Uuml;"], [0, "&Yacute;"], [0, "&THORN;"], [0, "&szlig;"], [0, "&agrave;"], [0, "&aacute;"], [0, "&acirc;"], [0, "&atilde;"], [0, "&auml;"], [0, "&aring;"], [0, "&aelig;"], [0, "&ccedil;"], [0, "&egrave;"], [0, "&eacute;"], [0, "&ecirc;"], [0, "&euml;"], [0, "&igrave;"], [0, "&iacute;"], [0, "&icirc;"], [0, "&iuml;"], [0, "&eth;"], [0, "&ntilde;"], [0, "&ograve;"], [0, "&oacute;"], [0, "&ocirc;"], [0, "&otilde;"], [0, "&ouml;"], [0, "&div;"], [0, "&oslash;"], [0, "&ugrave;"], [0, "&uacute;"], [0, "&ucirc;"], [0, "&uuml;"], [0, "&yacute;"], [0, "&thorn;"], [0, "&yuml;"], [0, "&Amacr;"], [0, "&amacr;"], [0, "&Abreve;"], [0, "&abreve;"], [0, "&Aogon;"], [0, "&aogon;"], [0, "&Cacute;"], [0, "&cacute;"], [0, "&Ccirc;"], [0, "&ccirc;"], [0, "&Cdot;"], [0, "&cdot;"], [0, "&Ccaron;"], [0, "&ccaron;"], [0, "&Dcaron;"], [0, "&dcaron;"], [0, "&Dstrok;"], [0, "&dstrok;"], [0, "&Emacr;"], [0, "&emacr;"], [2, "&Edot;"], [0, "&edot;"], [0, "&Eogon;"], [0, "&eogon;"], [0, "&Ecaron;"], [0, "&ecaron;"], [0, "&Gcirc;"], [0, "&gcirc;"], [0, "&Gbreve;"], [0, "&gbreve;"], [0, "&Gdot;"], [0, "&gdot;"], [0, "&Gcedil;"], [1, "&Hcirc;"], [0, "&hcirc;"], [0, "&Hstrok;"], [0, "&hstrok;"], [0, "&Itilde;"], [0, "&itilde;"], [0, "&Imacr;"], [0, "&imacr;"], [2, "&Iogon;"], [0, "&iogon;"], [0, "&Idot;"], [0, "&imath;"], [0, "&IJlig;"], [0, "&ijlig;"], [0, "&Jcirc;"], [0, "&jcirc;"], [0, "&Kcedil;"], [0, "&kcedil;"], [0, "&kgreen;"], [0, "&Lacute;"], [0, "&lacute;"], [0, "&Lcedil;"], [0, "&lcedil;"], [0, "&Lcaron;"], [0, "&lcaron;"], [0, "&Lmidot;"], [0, "&lmidot;"], [0, "&Lstrok;"], [0, "&lstrok;"], [0, "&Nacute;"], [0, "&nacute;"], [0, "&Ncedil;"], [0, "&ncedil;"], [0, "&Ncaron;"], [0, "&ncaron;"], [0, "&napos;"], [0, "&ENG;"], [0, "&eng;"], [0, "&Omacr;"], [0, "&omacr;"], [2, "&Odblac;"], [0, "&odblac;"], [0, "&OElig;"], [0, "&oelig;"], [0, "&Racute;"], [0, "&racute;"], [0, "&Rcedil;"], [0, "&rcedil;"], [0, "&Rcaron;"], [0, "&rcaron;"], [0, "&Sacute;"], [0, "&sacute;"], [0, "&Scirc;"], [0, "&scirc;"], [0, "&Scedil;"], [0, "&scedil;"], [0, "&Scaron;"], [0, "&scaron;"], [0, "&Tcedil;"], [0, "&tcedil;"], [0, "&Tcaron;"], [0, "&tcaron;"], [0, "&Tstrok;"], [0, "&tstrok;"], [0, "&Utilde;"], [0, "&utilde;"], [0, "&Umacr;"], [0, "&umacr;"], [0, "&Ubreve;"], [0, "&ubreve;"], [0, "&Uring;"], [0, "&uring;"], [0, "&Udblac;"], [0, "&udblac;"], [0, "&Uogon;"], [0, "&uogon;"], [0, "&Wcirc;"], [0, "&wcirc;"], [0, "&Ycirc;"], [0, "&ycirc;"], [0, "&Yuml;"], [0, "&Zacute;"], [0, "&zacute;"], [0, "&Zdot;"], [0, "&zdot;"], [0, "&Zcaron;"], [0, "&zcaron;"], [19, "&fnof;"], [34, "&imped;"], [63, "&gacute;"], [65, "&jmath;"], [142, "&circ;"], [0, "&caron;"], [16, "&breve;"], [0, "&DiacriticalDot;"], [0, "&ring;"], [0, "&ogon;"], [0, "&DiacriticalTilde;"], [0, "&dblac;"], [51, "&DownBreve;"], [127, "&Alpha;"], [0, "&Beta;"], [0, "&Gamma;"], [0, "&Delta;"], [0, "&Epsilon;"], [0, "&Zeta;"], [0, "&Eta;"], [0, "&Theta;"], [0, "&Iota;"], [0, "&Kappa;"], [0, "&Lambda;"], [0, "&Mu;"], [0, "&Nu;"], [0, "&Xi;"], [0, "&Omicron;"], [0, "&Pi;"], [0, "&Rho;"], [1, "&Sigma;"], [0, "&Tau;"], [0, "&Upsilon;"], [0, "&Phi;"], [0, "&Chi;"], [0, "&Psi;"], [0, "&ohm;"], [7, "&alpha;"], [0, "&beta;"], [0, "&gamma;"], [0, "&delta;"], [0, "&epsi;"], [0, "&zeta;"], [0, "&eta;"], [0, "&theta;"], [0, "&iota;"], [0, "&kappa;"], [0, "&lambda;"], [0, "&mu;"], [0, "&nu;"], [0, "&xi;"], [0, "&omicron;"], [0, "&pi;"], [0, "&rho;"], [0, "&sigmaf;"], [0, "&sigma;"], [0, "&tau;"], [0, "&upsi;"], [0, "&phi;"], [0, "&chi;"], [0, "&psi;"], [0, "&omega;"], [7, "&thetasym;"], [0, "&Upsi;"], [2, "&phiv;"], [0, "&piv;"], [5, "&Gammad;"], [0, "&digamma;"], [18, "&kappav;"], [0, "&rhov;"], [3, "&epsiv;"], [0, "&backepsilon;"], [10, "&IOcy;"], [0, "&DJcy;"], [0, "&GJcy;"], [0, "&Jukcy;"], [0, "&DScy;"], [0, "&Iukcy;"], [0, "&YIcy;"], [0, "&Jsercy;"], [0, "&LJcy;"], [0, "&NJcy;"], [0, "&TSHcy;"], [0, "&KJcy;"], [1, "&Ubrcy;"], [0, "&DZcy;"], [0, "&Acy;"], [0, "&Bcy;"], [0, "&Vcy;"], [0, "&Gcy;"], [0, "&Dcy;"], [0, "&IEcy;"], [0, "&ZHcy;"], [0, "&Zcy;"], [0, "&Icy;"], [0, "&Jcy;"], [0, "&Kcy;"], [0, "&Lcy;"], [0, "&Mcy;"], [0, "&Ncy;"], [0, "&Ocy;"], [0, "&Pcy;"], [0, "&Rcy;"], [0, "&Scy;"], [0, "&Tcy;"], [0, "&Ucy;"], [0, "&Fcy;"], [0, "&KHcy;"], [0, "&TScy;"], [0, "&CHcy;"], [0, "&SHcy;"], [0, "&SHCHcy;"], [0, "&HARDcy;"], [0, "&Ycy;"], [0, "&SOFTcy;"], [0, "&Ecy;"], [0, "&YUcy;"], [0, "&YAcy;"], [0, "&acy;"], [0, "&bcy;"], [0, "&vcy;"], [0, "&gcy;"], [0, "&dcy;"], [0, "&iecy;"], [0, "&zhcy;"], [0, "&zcy;"], [0, "&icy;"], [0, "&jcy;"], [0, "&kcy;"], [0, "&lcy;"], [0, "&mcy;"], [0, "&ncy;"], [0, "&ocy;"], [0, "&pcy;"], [0, "&rcy;"], [0, "&scy;"], [0, "&tcy;"], [0, "&ucy;"], [0, "&fcy;"], [0, "&khcy;"], [0, "&tscy;"], [0, "&chcy;"], [0, "&shcy;"], [0, "&shchcy;"], [0, "&hardcy;"], [0, "&ycy;"], [0, "&softcy;"], [0, "&ecy;"], [0, "&yucy;"], [0, "&yacy;"], [1, "&iocy;"], [0, "&djcy;"], [0, "&gjcy;"], [0, "&jukcy;"], [0, "&dscy;"], [0, "&iukcy;"], [0, "&yicy;"], [0, "&jsercy;"], [0, "&ljcy;"], [0, "&njcy;"], [0, "&tshcy;"], [0, "&kjcy;"], [1, "&ubrcy;"], [0, "&dzcy;"], [7074, "&ensp;"], [0, "&emsp;"], [0, "&emsp13;"], [0, "&emsp14;"], [1, "&numsp;"], [0, "&puncsp;"], [0, "&ThinSpace;"], [0, "&hairsp;"], [0, "&NegativeMediumSpace;"], [0, "&zwnj;"], [0, "&zwj;"], [0, "&lrm;"], [0, "&rlm;"], [0, "&dash;"], [2, "&ndash;"], [0, "&mdash;"], [0, "&horbar;"], [0, "&Verbar;"], [1, "&lsquo;"], [0, "&CloseCurlyQuote;"], [0, "&lsquor;"], [1, "&ldquo;"], [0, "&CloseCurlyDoubleQuote;"], [0, "&bdquo;"], [1, "&dagger;"], [0, "&Dagger;"], [0, "&bull;"], [2, "&nldr;"], [0, "&hellip;"], [9, "&permil;"], [0, "&pertenk;"], [0, "&prime;"], [0, "&Prime;"], [0, "&tprime;"], [0, "&backprime;"], [3, "&lsaquo;"], [0, "&rsaquo;"], [3, "&oline;"], [2, "&caret;"], [1, "&hybull;"], [0, "&frasl;"], [10, "&bsemi;"], [7, "&qprime;"], [7, { v: "&MediumSpace;", n: 8202, o: "&ThickSpace;" }], [0, "&NoBreak;"], [0, "&af;"], [0, "&InvisibleTimes;"], [0, "&ic;"], [72, "&euro;"], [46, "&tdot;"], [0, "&DotDot;"], [37, "&complexes;"], [2, "&incare;"], [4, "&gscr;"], [0, "&hamilt;"], [0, "&Hfr;"], [0, "&Hopf;"], [0, "&planckh;"], [0, "&hbar;"], [0, "&imagline;"], [0, "&Ifr;"], [0, "&lagran;"], [0, "&ell;"], [1, "&naturals;"], [0, "&numero;"], [0, "&copysr;"], [0, "&weierp;"], [0, "&Popf;"], [0, "&Qopf;"], [0, "&realine;"], [0, "&real;"], [0, "&reals;"], [0, "&rx;"], [3, "&trade;"], [1, "&integers;"], [2, "&mho;"], [0, "&zeetrf;"], [0, "&iiota;"], [2, "&bernou;"], [0, "&Cayleys;"], [1, "&escr;"], [0, "&Escr;"], [0, "&Fouriertrf;"], [1, "&Mellintrf;"], [0, "&order;"], [0, "&alefsym;"], [0, "&beth;"], [0, "&gimel;"], [0, "&daleth;"], [12, "&CapitalDifferentialD;"], [0, "&dd;"], [0, "&ee;"], [0, "&ii;"], [10, "&frac13;"], [0, "&frac23;"], [0, "&frac15;"], [0, "&frac25;"], [0, "&frac35;"], [0, "&frac45;"], [0, "&frac16;"], [0, "&frac56;"], [0, "&frac18;"], [0, "&frac38;"], [0, "&frac58;"], [0, "&frac78;"], [49, "&larr;"], [0, "&ShortUpArrow;"], [0, "&rarr;"], [0, "&darr;"], [0, "&harr;"], [0, "&updownarrow;"], [0, "&nwarr;"], [0, "&nearr;"], [0, "&LowerRightArrow;"], [0, "&LowerLeftArrow;"], [0, "&nlarr;"], [0, "&nrarr;"], [1, { v: "&rarrw;", n: 824, o: "&nrarrw;" }], [0, "&Larr;"], [0, "&Uarr;"], [0, "&Rarr;"], [0, "&Darr;"], [0, "&larrtl;"], [0, "&rarrtl;"], [0, "&LeftTeeArrow;"], [0, "&mapstoup;"], [0, "&map;"], [0, "&DownTeeArrow;"], [1, "&hookleftarrow;"], [0, "&hookrightarrow;"], [0, "&larrlp;"], [0, "&looparrowright;"], [0, "&harrw;"], [0, "&nharr;"], [1, "&lsh;"], [0, "&rsh;"], [0, "&ldsh;"], [0, "&rdsh;"], [1, "&crarr;"], [0, "&cularr;"], [0, "&curarr;"], [2, "&circlearrowleft;"], [0, "&circlearrowright;"], [0, "&leftharpoonup;"], [0, "&DownLeftVector;"], [0, "&RightUpVector;"], [0, "&LeftUpVector;"], [0, "&rharu;"], [0, "&DownRightVector;"], [0, "&dharr;"], [0, "&dharl;"], [0, "&RightArrowLeftArrow;"], [0, "&udarr;"], [0, "&LeftArrowRightArrow;"], [0, "&leftleftarrows;"], [0, "&upuparrows;"], [0, "&rightrightarrows;"], [0, "&ddarr;"], [0, "&leftrightharpoons;"], [0, "&Equilibrium;"], [0, "&nlArr;"], [0, "&nhArr;"], [0, "&nrArr;"], [0, "&DoubleLeftArrow;"], [0, "&DoubleUpArrow;"], [0, "&DoubleRightArrow;"], [0, "&dArr;"], [0, "&DoubleLeftRightArrow;"], [0, "&DoubleUpDownArrow;"], [0, "&nwArr;"], [0, "&neArr;"], [0, "&seArr;"], [0, "&swArr;"], [0, "&lAarr;"], [0, "&rAarr;"], [1, "&zigrarr;"], [6, "&larrb;"], [0, "&rarrb;"], [15, "&DownArrowUpArrow;"], [7, "&loarr;"], [0, "&roarr;"], [0, "&hoarr;"], [0, "&forall;"], [0, "&comp;"], [0, { v: "&part;", n: 824, o: "&npart;" }], [0, "&exist;"], [0, "&nexist;"], [0, "&empty;"], [1, "&Del;"], [0, "&Element;"], [0, "&NotElement;"], [1, "&ni;"], [0, "&notni;"], [2, "&prod;"], [0, "&coprod;"], [0, "&sum;"], [0, "&minus;"], [0, "&MinusPlus;"], [0, "&dotplus;"], [1, "&Backslash;"], [0, "&lowast;"], [0, "&compfn;"], [1, "&radic;"], [2, "&prop;"], [0, "&infin;"], [0, "&angrt;"], [0, { v: "&ang;", n: 8402, o: "&nang;" }], [0, "&angmsd;"], [0, "&angsph;"], [0, "&mid;"], [0, "&nmid;"], [0, "&DoubleVerticalBar;"], [0, "&NotDoubleVerticalBar;"], [0, "&and;"], [0, "&or;"], [0, { v: "&cap;", n: 65024, o: "&caps;" }], [0, { v: "&cup;", n: 65024, o: "&cups;" }], [0, "&int;"], [0, "&Int;"], [0, "&iiint;"], [0, "&conint;"], [0, "&Conint;"], [0, "&Cconint;"], [0, "&cwint;"], [0, "&ClockwiseContourIntegral;"], [0, "&awconint;"], [0, "&there4;"], [0, "&becaus;"], [0, "&ratio;"], [0, "&Colon;"], [0, "&dotminus;"], [1, "&mDDot;"], [0, "&homtht;"], [0, { v: "&sim;", n: 8402, o: "&nvsim;" }], [0, { v: "&backsim;", n: 817, o: "&race;" }], [0, { v: "&ac;", n: 819, o: "&acE;" }], [0, "&acd;"], [0, "&VerticalTilde;"], [0, "&NotTilde;"], [0, { v: "&eqsim;", n: 824, o: "&nesim;" }], [0, "&sime;"], [0, "&NotTildeEqual;"], [0, "&cong;"], [0, "&simne;"], [0, "&ncong;"], [0, "&ap;"], [0, "&nap;"], [0, "&ape;"], [0, { v: "&apid;", n: 824, o: "&napid;" }], [0, "&backcong;"], [0, { v: "&asympeq;", n: 8402, o: "&nvap;" }], [0, { v: "&bump;", n: 824, o: "&nbump;" }], [0, { v: "&bumpe;", n: 824, o: "&nbumpe;" }], [0, { v: "&doteq;", n: 824, o: "&nedot;" }], [0, "&doteqdot;"], [0, "&efDot;"], [0, "&erDot;"], [0, "&Assign;"], [0, "&ecolon;"], [0, "&ecir;"], [0, "&circeq;"], [1, "&wedgeq;"], [0, "&veeeq;"], [1, "&triangleq;"], [2, "&equest;"], [0, "&ne;"], [0, { v: "&Congruent;", n: 8421, o: "&bnequiv;" }], [0, "&nequiv;"], [1, { v: "&le;", n: 8402, o: "&nvle;" }], [0, { v: "&ge;", n: 8402, o: "&nvge;" }], [0, { v: "&lE;", n: 824, o: "&nlE;" }], [0, { v: "&gE;", n: 824, o: "&ngE;" }], [0, { v: "&lnE;", n: 65024, o: "&lvertneqq;" }], [0, { v: "&gnE;", n: 65024, o: "&gvertneqq;" }], [0, { v: "&ll;", n: new Map(/* @__PURE__ */ restoreDiff([[824, "&nLtv;"], [7577, "&nLt;"]])) }], [0, { v: "&gg;", n: new Map(/* @__PURE__ */ restoreDiff([[824, "&nGtv;"], [7577, "&nGt;"]])) }], [0, "&between;"], [0, "&NotCupCap;"], [0, "&nless;"], [0, "&ngt;"], [0, "&nle;"], [0, "&nge;"], [0, "&lesssim;"], [0, "&GreaterTilde;"], [0, "&nlsim;"], [0, "&ngsim;"], [0, "&LessGreater;"], [0, "&gl;"], [0, "&NotLessGreater;"], [0, "&NotGreaterLess;"], [0, "&pr;"], [0, "&sc;"], [0, "&prcue;"], [0, "&sccue;"], [0, "&PrecedesTilde;"], [0, { v: "&scsim;", n: 824, o: "&NotSucceedsTilde;" }], [0, "&NotPrecedes;"], [0, "&NotSucceeds;"], [0, { v: "&sub;", n: 8402, o: "&NotSubset;" }], [0, { v: "&sup;", n: 8402, o: "&NotSuperset;" }], [0, "&nsub;"], [0, "&nsup;"], [0, "&sube;"], [0, "&supe;"], [0, "&NotSubsetEqual;"], [0, "&NotSupersetEqual;"], [0, { v: "&subne;", n: 65024, o: "&varsubsetneq;" }], [0, { v: "&supne;", n: 65024, o: "&varsupsetneq;" }], [1, "&cupdot;"], [0, "&UnionPlus;"], [0, { v: "&sqsub;", n: 824, o: "&NotSquareSubset;" }], [0, { v: "&sqsup;", n: 824, o: "&NotSquareSuperset;" }], [0, "&sqsube;"], [0, "&sqsupe;"], [0, { v: "&sqcap;", n: 65024, o: "&sqcaps;" }], [0, { v: "&sqcup;", n: 65024, o: "&sqcups;" }], [0, "&CirclePlus;"], [0, "&CircleMinus;"], [0, "&CircleTimes;"], [0, "&osol;"], [0, "&CircleDot;"], [0, "&circledcirc;"], [0, "&circledast;"], [1, "&circleddash;"], [0, "&boxplus;"], [0, "&boxminus;"], [0, "&boxtimes;"], [0, "&dotsquare;"], [0, "&RightTee;"], [0, "&dashv;"], [0, "&DownTee;"], [0, "&bot;"], [1, "&models;"], [0, "&DoubleRightTee;"], [0, "&Vdash;"], [0, "&Vvdash;"], [0, "&VDash;"], [0, "&nvdash;"], [0, "&nvDash;"], [0, "&nVdash;"], [0, "&nVDash;"], [0, "&prurel;"], [1, "&LeftTriangle;"], [0, "&RightTriangle;"], [0, { v: "&LeftTriangleEqual;", n: 8402, o: "&nvltrie;" }], [0, { v: "&RightTriangleEqual;", n: 8402, o: "&nvrtrie;" }], [0, "&origof;"], [0, "&imof;"], [0, "&multimap;"], [0, "&hercon;"], [0, "&intcal;"], [0, "&veebar;"], [1, "&barvee;"], [0, "&angrtvb;"], [0, "&lrtri;"], [0, "&bigwedge;"], [0, "&bigvee;"], [0, "&bigcap;"], [0, "&bigcup;"], [0, "&diam;"], [0, "&sdot;"], [0, "&sstarf;"], [0, "&divideontimes;"], [0, "&bowtie;"], [0, "&ltimes;"], [0, "&rtimes;"], [0, "&leftthreetimes;"], [0, "&rightthreetimes;"], [0, "&backsimeq;"], [0, "&curlyvee;"], [0, "&curlywedge;"], [0, "&Sub;"], [0, "&Sup;"], [0, "&Cap;"], [0, "&Cup;"], [0, "&fork;"], [0, "&epar;"], [0, "&lessdot;"], [0, "&gtdot;"], [0, { v: "&Ll;", n: 824, o: "&nLl;" }], [0, { v: "&Gg;", n: 824, o: "&nGg;" }], [0, { v: "&leg;", n: 65024, o: "&lesg;" }], [0, { v: "&gel;", n: 65024, o: "&gesl;" }], [2, "&cuepr;"], [0, "&cuesc;"], [0, "&NotPrecedesSlantEqual;"], [0, "&NotSucceedsSlantEqual;"], [0, "&NotSquareSubsetEqual;"], [0, "&NotSquareSupersetEqual;"], [2, "&lnsim;"], [0, "&gnsim;"], [0, "&precnsim;"], [0, "&scnsim;"], [0, "&nltri;"], [0, "&NotRightTriangle;"], [0, "&nltrie;"], [0, "&NotRightTriangleEqual;"], [0, "&vellip;"], [0, "&ctdot;"], [0, "&utdot;"], [0, "&dtdot;"], [0, "&disin;"], [0, "&isinsv;"], [0, "&isins;"], [0, { v: "&isindot;", n: 824, o: "&notindot;" }], [0, "&notinvc;"], [0, "&notinvb;"], [1, { v: "&isinE;", n: 824, o: "&notinE;" }], [0, "&nisd;"], [0, "&xnis;"], [0, "&nis;"], [0, "&notnivc;"], [0, "&notnivb;"], [6, "&barwed;"], [0, "&Barwed;"], [1, "&lceil;"], [0, "&rceil;"], [0, "&LeftFloor;"], [0, "&rfloor;"], [0, "&drcrop;"], [0, "&dlcrop;"], [0, "&urcrop;"], [0, "&ulcrop;"], [0, "&bnot;"], [1, "&profline;"], [0, "&profsurf;"], [1, "&telrec;"], [0, "&target;"], [5, "&ulcorn;"], [0, "&urcorn;"], [0, "&dlcorn;"], [0, "&drcorn;"], [2, "&frown;"], [0, "&smile;"], [9, "&cylcty;"], [0, "&profalar;"], [7, "&topbot;"], [6, "&ovbar;"], [1, "&solbar;"], [60, "&angzarr;"], [51, "&lmoustache;"], [0, "&rmoustache;"], [2, "&OverBracket;"], [0, "&bbrk;"], [0, "&bbrktbrk;"], [37, "&OverParenthesis;"], [0, "&UnderParenthesis;"], [0, "&OverBrace;"], [0, "&UnderBrace;"], [2, "&trpezium;"], [4, "&elinters;"], [59, "&blank;"], [164, "&circledS;"], [55, "&boxh;"], [1, "&boxv;"], [9, "&boxdr;"], [3, "&boxdl;"], [3, "&boxur;"], [3, "&boxul;"], [3, "&boxvr;"], [7, "&boxvl;"], [7, "&boxhd;"], [7, "&boxhu;"], [7, "&boxvh;"], [19, "&boxH;"], [0, "&boxV;"], [0, "&boxdR;"], [0, "&boxDr;"], [0, "&boxDR;"], [0, "&boxdL;"], [0, "&boxDl;"], [0, "&boxDL;"], [0, "&boxuR;"], [0, "&boxUr;"], [0, "&boxUR;"], [0, "&boxuL;"], [0, "&boxUl;"], [0, "&boxUL;"], [0, "&boxvR;"], [0, "&boxVr;"], [0, "&boxVR;"], [0, "&boxvL;"], [0, "&boxVl;"], [0, "&boxVL;"], [0, "&boxHd;"], [0, "&boxhD;"], [0, "&boxHD;"], [0, "&boxHu;"], [0, "&boxhU;"], [0, "&boxHU;"], [0, "&boxvH;"], [0, "&boxVh;"], [0, "&boxVH;"], [19, "&uhblk;"], [3, "&lhblk;"], [3, "&block;"], [8, "&blk14;"], [0, "&blk12;"], [0, "&blk34;"], [13, "&square;"], [8, "&blacksquare;"], [0, "&EmptyVerySmallSquare;"], [1, "&rect;"], [0, "&marker;"], [2, "&fltns;"], [1, "&bigtriangleup;"], [0, "&blacktriangle;"], [0, "&triangle;"], [2, "&blacktriangleright;"], [0, "&rtri;"], [3, "&bigtriangledown;"], [0, "&blacktriangledown;"], [0, "&dtri;"], [2, "&blacktriangleleft;"], [0, "&ltri;"], [6, "&loz;"], [0, "&cir;"], [32, "&tridot;"], [2, "&bigcirc;"], [8, "&ultri;"], [0, "&urtri;"], [0, "&lltri;"], [0, "&EmptySmallSquare;"], [0, "&FilledSmallSquare;"], [8, "&bigstar;"], [0, "&star;"], [7, "&phone;"], [49, "&female;"], [1, "&male;"], [29, "&spades;"], [2, "&clubs;"], [1, "&hearts;"], [0, "&diamondsuit;"], [3, "&sung;"], [2, "&flat;"], [0, "&natural;"], [0, "&sharp;"], [163, "&check;"], [3, "&cross;"], [8, "&malt;"], [21, "&sext;"], [33, "&VerticalSeparator;"], [25, "&lbbrk;"], [0, "&rbbrk;"], [84, "&bsolhsub;"], [0, "&suphsol;"], [28, "&LeftDoubleBracket;"], [0, "&RightDoubleBracket;"], [0, "&lang;"], [0, "&rang;"], [0, "&Lang;"], [0, "&Rang;"], [0, "&loang;"], [0, "&roang;"], [7, "&longleftarrow;"], [0, "&longrightarrow;"], [0, "&longleftrightarrow;"], [0, "&DoubleLongLeftArrow;"], [0, "&DoubleLongRightArrow;"], [0, "&DoubleLongLeftRightArrow;"], [1, "&longmapsto;"], [2, "&dzigrarr;"], [258, "&nvlArr;"], [0, "&nvrArr;"], [0, "&nvHarr;"], [0, "&Map;"], [6, "&lbarr;"], [0, "&bkarow;"], [0, "&lBarr;"], [0, "&dbkarow;"], [0, "&drbkarow;"], [0, "&DDotrahd;"], [0, "&UpArrowBar;"], [0, "&DownArrowBar;"], [2, "&Rarrtl;"], [2, "&latail;"], [0, "&ratail;"], [0, "&lAtail;"], [0, "&rAtail;"], [0, "&larrfs;"], [0, "&rarrfs;"], [0, "&larrbfs;"], [0, "&rarrbfs;"], [2, "&nwarhk;"], [0, "&nearhk;"], [0, "&hksearow;"], [0, "&hkswarow;"], [0, "&nwnear;"], [0, "&nesear;"], [0, "&seswar;"], [0, "&swnwar;"], [8, { v: "&rarrc;", n: 824, o: "&nrarrc;" }], [1, "&cudarrr;"], [0, "&ldca;"], [0, "&rdca;"], [0, "&cudarrl;"], [0, "&larrpl;"], [2, "&curarrm;"], [0, "&cularrp;"], [7, "&rarrpl;"], [2, "&harrcir;"], [0, "&Uarrocir;"], [0, "&lurdshar;"], [0, "&ldrushar;"], [2, "&LeftRightVector;"], [0, "&RightUpDownVector;"], [0, "&DownLeftRightVector;"], [0, "&LeftUpDownVector;"], [0, "&LeftVectorBar;"], [0, "&RightVectorBar;"], [0, "&RightUpVectorBar;"], [0, "&RightDownVectorBar;"], [0, "&DownLeftVectorBar;"], [0, "&DownRightVectorBar;"], [0, "&LeftUpVectorBar;"], [0, "&LeftDownVectorBar;"], [0, "&LeftTeeVector;"], [0, "&RightTeeVector;"], [0, "&RightUpTeeVector;"], [0, "&RightDownTeeVector;"], [0, "&DownLeftTeeVector;"], [0, "&DownRightTeeVector;"], [0, "&LeftUpTeeVector;"], [0, "&LeftDownTeeVector;"], [0, "&lHar;"], [0, "&uHar;"], [0, "&rHar;"], [0, "&dHar;"], [0, "&luruhar;"], [0, "&ldrdhar;"], [0, "&ruluhar;"], [0, "&rdldhar;"], [0, "&lharul;"], [0, "&llhard;"], [0, "&rharul;"], [0, "&lrhard;"], [0, "&udhar;"], [0, "&duhar;"], [0, "&RoundImplies;"], [0, "&erarr;"], [0, "&simrarr;"], [0, "&larrsim;"], [0, "&rarrsim;"], [0, "&rarrap;"], [0, "&ltlarr;"], [1, "&gtrarr;"], [0, "&subrarr;"], [1, "&suplarr;"], [0, "&lfisht;"], [0, "&rfisht;"], [0, "&ufisht;"], [0, "&dfisht;"], [5, "&lopar;"], [0, "&ropar;"], [4, "&lbrke;"], [0, "&rbrke;"], [0, "&lbrkslu;"], [0, "&rbrksld;"], [0, "&lbrksld;"], [0, "&rbrkslu;"], [0, "&langd;"], [0, "&rangd;"], [0, "&lparlt;"], [0, "&rpargt;"], [0, "&gtlPar;"], [0, "&ltrPar;"], [3, "&vzigzag;"], [1, "&vangrt;"], [0, "&angrtvbd;"], [6, "&ange;"], [0, "&range;"], [0, "&dwangle;"], [0, "&uwangle;"], [0, "&angmsdaa;"], [0, "&angmsdab;"], [0, "&angmsdac;"], [0, "&angmsdad;"], [0, "&angmsdae;"], [0, "&angmsdaf;"], [0, "&angmsdag;"], [0, "&angmsdah;"], [0, "&bemptyv;"], [0, "&demptyv;"], [0, "&cemptyv;"], [0, "&raemptyv;"], [0, "&laemptyv;"], [0, "&ohbar;"], [0, "&omid;"], [0, "&opar;"], [1, "&operp;"], [1, "&olcross;"], [0, "&odsold;"], [1, "&olcir;"], [0, "&ofcir;"], [0, "&olt;"], [0, "&ogt;"], [0, "&cirscir;"], [0, "&cirE;"], [0, "&solb;"], [0, "&bsolb;"], [3, "&boxbox;"], [3, "&trisb;"], [0, "&rtriltri;"], [0, { v: "&LeftTriangleBar;", n: 824, o: "&NotLeftTriangleBar;" }], [0, { v: "&RightTriangleBar;", n: 824, o: "&NotRightTriangleBar;" }], [11, "&iinfin;"], [0, "&infintie;"], [0, "&nvinfin;"], [4, "&eparsl;"], [0, "&smeparsl;"], [0, "&eqvparsl;"], [5, "&blacklozenge;"], [8, "&RuleDelayed;"], [1, "&dsol;"], [9, "&bigodot;"], [0, "&bigoplus;"], [0, "&bigotimes;"], [1, "&biguplus;"], [1, "&bigsqcup;"], [5, "&iiiint;"], [0, "&fpartint;"], [2, "&cirfnint;"], [0, "&awint;"], [0, "&rppolint;"], [0, "&scpolint;"], [0, "&npolint;"], [0, "&pointint;"], [0, "&quatint;"], [0, "&intlarhk;"], [10, "&pluscir;"], [0, "&plusacir;"], [0, "&simplus;"], [0, "&plusdu;"], [0, "&plussim;"], [0, "&plustwo;"], [1, "&mcomma;"], [0, "&minusdu;"], [2, "&loplus;"], [0, "&roplus;"], [0, "&Cross;"], [0, "&timesd;"], [0, "&timesbar;"], [1, "&smashp;"], [0, "&lotimes;"], [0, "&rotimes;"], [0, "&otimesas;"], [0, "&Otimes;"], [0, "&odiv;"], [0, "&triplus;"], [0, "&triminus;"], [0, "&tritime;"], [0, "&intprod;"], [2, "&amalg;"], [0, "&capdot;"], [1, "&ncup;"], [0, "&ncap;"], [0, "&capand;"], [0, "&cupor;"], [0, "&cupcap;"], [0, "&capcup;"], [0, "&cupbrcap;"], [0, "&capbrcup;"], [0, "&cupcup;"], [0, "&capcap;"], [0, "&ccups;"], [0, "&ccaps;"], [2, "&ccupssm;"], [2, "&And;"], [0, "&Or;"], [0, "&andand;"], [0, "&oror;"], [0, "&orslope;"], [0, "&andslope;"], [1, "&andv;"], [0, "&orv;"], [0, "&andd;"], [0, "&ord;"], [1, "&wedbar;"], [6, "&sdote;"], [3, "&simdot;"], [2, { v: "&congdot;", n: 824, o: "&ncongdot;" }], [0, "&easter;"], [0, "&apacir;"], [0, { v: "&apE;", n: 824, o: "&napE;" }], [0, "&eplus;"], [0, "&pluse;"], [0, "&Esim;"], [0, "&Colone;"], [0, "&Equal;"], [1, "&ddotseq;"], [0, "&equivDD;"], [0, "&ltcir;"], [0, "&gtcir;"], [0, "&ltquest;"], [0, "&gtquest;"], [0, { v: "&leqslant;", n: 824, o: "&nleqslant;" }], [0, { v: "&geqslant;", n: 824, o: "&ngeqslant;" }], [0, "&lesdot;"], [0, "&gesdot;"], [0, "&lesdoto;"], [0, "&gesdoto;"], [0, "&lesdotor;"], [0, "&gesdotol;"], [0, "&lap;"], [0, "&gap;"], [0, "&lne;"], [0, "&gne;"], [0, "&lnap;"], [0, "&gnap;"], [0, "&lEg;"], [0, "&gEl;"], [0, "&lsime;"], [0, "&gsime;"], [0, "&lsimg;"], [0, "&gsiml;"], [0, "&lgE;"], [0, "&glE;"], [0, "&lesges;"], [0, "&gesles;"], [0, "&els;"], [0, "&egs;"], [0, "&elsdot;"], [0, "&egsdot;"], [0, "&el;"], [0, "&eg;"], [2, "&siml;"], [0, "&simg;"], [0, "&simlE;"], [0, "&simgE;"], [0, { v: "&LessLess;", n: 824, o: "&NotNestedLessLess;" }], [0, { v: "&GreaterGreater;", n: 824, o: "&NotNestedGreaterGreater;" }], [1, "&glj;"], [0, "&gla;"], [0, "&ltcc;"], [0, "&gtcc;"], [0, "&lescc;"], [0, "&gescc;"], [0, "&smt;"], [0, "&lat;"], [0, { v: "&smte;", n: 65024, o: "&smtes;" }], [0, { v: "&late;", n: 65024, o: "&lates;" }], [0, "&bumpE;"], [0, { v: "&PrecedesEqual;", n: 824, o: "&NotPrecedesEqual;" }], [0, { v: "&sce;", n: 824, o: "&NotSucceedsEqual;" }], [2, "&prE;"], [0, "&scE;"], [0, "&precneqq;"], [0, "&scnE;"], [0, "&prap;"], [0, "&scap;"], [0, "&precnapprox;"], [0, "&scnap;"], [0, "&Pr;"], [0, "&Sc;"], [0, "&subdot;"], [0, "&supdot;"], [0, "&subplus;"], [0, "&supplus;"], [0, "&submult;"], [0, "&supmult;"], [0, "&subedot;"], [0, "&supedot;"], [0, { v: "&subE;", n: 824, o: "&nsubE;" }], [0, { v: "&supE;", n: 824, o: "&nsupE;" }], [0, "&subsim;"], [0, "&supsim;"], [2, { v: "&subnE;", n: 65024, o: "&varsubsetneqq;" }], [0, { v: "&supnE;", n: 65024, o: "&varsupsetneqq;" }], [2, "&csub;"], [0, "&csup;"], [0, "&csube;"], [0, "&csupe;"], [0, "&subsup;"], [0, "&supsub;"], [0, "&subsub;"], [0, "&supsup;"], [0, "&suphsub;"], [0, "&supdsub;"], [0, "&forkv;"], [0, "&topfork;"], [0, "&mlcp;"], [8, "&Dashv;"], [1, "&Vdashl;"], [0, "&Barv;"], [0, "&vBar;"], [0, "&vBarv;"], [1, "&Vbar;"], [0, "&Not;"], [0, "&bNot;"], [0, "&rnmid;"], [0, "&cirmid;"], [0, "&midcir;"], [0, "&topcir;"], [0, "&nhpar;"], [0, "&parsim;"], [9, { v: "&parsl;", n: 8421, o: "&nparsl;" }], [44343, { n: new Map(/* @__PURE__ */ restoreDiff([[56476, "&Ascr;"], [1, "&Cscr;"], [0, "&Dscr;"], [2, "&Gscr;"], [2, "&Jscr;"], [0, "&Kscr;"], [2, "&Nscr;"], [0, "&Oscr;"], [0, "&Pscr;"], [0, "&Qscr;"], [1, "&Sscr;"], [0, "&Tscr;"], [0, "&Uscr;"], [0, "&Vscr;"], [0, "&Wscr;"], [0, "&Xscr;"], [0, "&Yscr;"], [0, "&Zscr;"], [0, "&ascr;"], [0, "&bscr;"], [0, "&cscr;"], [0, "&dscr;"], [1, "&fscr;"], [1, "&hscr;"], [0, "&iscr;"], [0, "&jscr;"], [0, "&kscr;"], [0, "&lscr;"], [0, "&mscr;"], [0, "&nscr;"], [1, "&pscr;"], [0, "&qscr;"], [0, "&rscr;"], [0, "&sscr;"], [0, "&tscr;"], [0, "&uscr;"], [0, "&vscr;"], [0, "&wscr;"], [0, "&xscr;"], [0, "&yscr;"], [0, "&zscr;"], [52, "&Afr;"], [0, "&Bfr;"], [1, "&Dfr;"], [0, "&Efr;"], [0, "&Ffr;"], [0, "&Gfr;"], [2, "&Jfr;"], [0, "&Kfr;"], [0, "&Lfr;"], [0, "&Mfr;"], [0, "&Nfr;"], [0, "&Ofr;"], [0, "&Pfr;"], [0, "&Qfr;"], [1, "&Sfr;"], [0, "&Tfr;"], [0, "&Ufr;"], [0, "&Vfr;"], [0, "&Wfr;"], [0, "&Xfr;"], [0, "&Yfr;"], [1, "&afr;"], [0, "&bfr;"], [0, "&cfr;"], [0, "&dfr;"], [0, "&efr;"], [0, "&ffr;"], [0, "&gfr;"], [0, "&hfr;"], [0, "&ifr;"], [0, "&jfr;"], [0, "&kfr;"], [0, "&lfr;"], [0, "&mfr;"], [0, "&nfr;"], [0, "&ofr;"], [0, "&pfr;"], [0, "&qfr;"], [0, "&rfr;"], [0, "&sfr;"], [0, "&tfr;"], [0, "&ufr;"], [0, "&vfr;"], [0, "&wfr;"], [0, "&xfr;"], [0, "&yfr;"], [0, "&zfr;"], [0, "&Aopf;"], [0, "&Bopf;"], [1, "&Dopf;"], [0, "&Eopf;"], [0, "&Fopf;"], [0, "&Gopf;"], [1, "&Iopf;"], [0, "&Jopf;"], [0, "&Kopf;"], [0, "&Lopf;"], [0, "&Mopf;"], [1, "&Oopf;"], [3, "&Sopf;"], [0, "&Topf;"], [0, "&Uopf;"], [0, "&Vopf;"], [0, "&Wopf;"], [0, "&Xopf;"], [0, "&Yopf;"], [1, "&aopf;"], [0, "&bopf;"], [0, "&copf;"], [0, "&dopf;"], [0, "&eopf;"], [0, "&fopf;"], [0, "&gopf;"], [0, "&hopf;"], [0, "&iopf;"], [0, "&jopf;"], [0, "&kopf;"], [0, "&lopf;"], [0, "&mopf;"], [0, "&nopf;"], [0, "&oopf;"], [0, "&popf;"], [0, "&qopf;"], [0, "&ropf;"], [0, "&sopf;"], [0, "&topf;"], [0, "&uopf;"], [0, "&vopf;"], [0, "&wopf;"], [0, "&xopf;"], [0, "&yopf;"], [0, "&zopf;"]])) }], [8906, "&fflig;"], [0, "&filig;"], [0, "&fllig;"], [0, "&ffilig;"], [0, "&ffllig;"]]));
    }
  });

  // node_modules/entities/lib/escape.js
  var require_escape = __commonJS({
    "node_modules/entities/lib/escape.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.escapeText = exports4.escapeAttribute = exports4.escapeUTF8 = exports4.escape = exports4.encodeXML = exports4.getCodePoint = exports4.xmlReplacer = void 0;
      exports4.xmlReplacer = /["&'<>$\x80-\uFFFF]/g;
      var xmlCodeMap = /* @__PURE__ */ new Map([
        [34, "&quot;"],
        [38, "&amp;"],
        [39, "&apos;"],
        [60, "&lt;"],
        [62, "&gt;"]
      ]);
      exports4.getCodePoint = // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      String.prototype.codePointAt != null ? function(str, index2) {
        return str.codePointAt(index2);
      } : (
        // http://mathiasbynens.be/notes/javascript-encoding#surrogate-formulae
        function(c, index2) {
          return (c.charCodeAt(index2) & 64512) === 55296 ? (c.charCodeAt(index2) - 55296) * 1024 + c.charCodeAt(index2 + 1) - 56320 + 65536 : c.charCodeAt(index2);
        }
      );
      function encodeXML(str) {
        var ret = "";
        var lastIdx = 0;
        var match;
        while ((match = exports4.xmlReplacer.exec(str)) !== null) {
          var i = match.index;
          var char = str.charCodeAt(i);
          var next2 = xmlCodeMap.get(char);
          if (next2 !== void 0) {
            ret += str.substring(lastIdx, i) + next2;
            lastIdx = i + 1;
          } else {
            ret += "".concat(str.substring(lastIdx, i), "&#x").concat((0, exports4.getCodePoint)(str, i).toString(16), ";");
            lastIdx = exports4.xmlReplacer.lastIndex += Number((char & 64512) === 55296);
          }
        }
        return ret + str.substr(lastIdx);
      }
      __name(encodeXML, "encodeXML");
      exports4.encodeXML = encodeXML;
      exports4.escape = encodeXML;
      function getEscaper(regex, map2) {
        return /* @__PURE__ */ __name(function escape(data2) {
          var match;
          var lastIdx = 0;
          var result = "";
          while (match = regex.exec(data2)) {
            if (lastIdx !== match.index) {
              result += data2.substring(lastIdx, match.index);
            }
            result += map2.get(match[0].charCodeAt(0));
            lastIdx = match.index + 1;
          }
          return result + data2.substring(lastIdx);
        }, "escape");
      }
      __name(getEscaper, "getEscaper");
      exports4.escapeUTF8 = getEscaper(/[&<>'"]/g, xmlCodeMap);
      exports4.escapeAttribute = getEscaper(/["&\u00A0]/g, /* @__PURE__ */ new Map([
        [34, "&quot;"],
        [38, "&amp;"],
        [160, "&nbsp;"]
      ]));
      exports4.escapeText = getEscaper(/[&<>\u00A0]/g, /* @__PURE__ */ new Map([
        [38, "&amp;"],
        [60, "&lt;"],
        [62, "&gt;"],
        [160, "&nbsp;"]
      ]));
    }
  });

  // node_modules/entities/lib/encode.js
  var require_encode = __commonJS({
    "node_modules/entities/lib/encode.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.encodeNonAsciiHTML = exports4.encodeHTML = void 0;
      var encode_html_js_1 = __importDefault(require_encode_html());
      var escape_js_1 = require_escape();
      var htmlReplacer = /[\t\n!-,./:-@[-`\f{-}$\x80-\uFFFF]/g;
      function encodeHTML(data2) {
        return encodeHTMLTrieRe(htmlReplacer, data2);
      }
      __name(encodeHTML, "encodeHTML");
      exports4.encodeHTML = encodeHTML;
      function encodeNonAsciiHTML(data2) {
        return encodeHTMLTrieRe(escape_js_1.xmlReplacer, data2);
      }
      __name(encodeNonAsciiHTML, "encodeNonAsciiHTML");
      exports4.encodeNonAsciiHTML = encodeNonAsciiHTML;
      function encodeHTMLTrieRe(regExp, str) {
        var ret = "";
        var lastIdx = 0;
        var match;
        while ((match = regExp.exec(str)) !== null) {
          var i = match.index;
          ret += str.substring(lastIdx, i);
          var char = str.charCodeAt(i);
          var next2 = encode_html_js_1.default.get(char);
          if (typeof next2 === "object") {
            if (i + 1 < str.length) {
              var nextChar = str.charCodeAt(i + 1);
              var value = typeof next2.n === "number" ? next2.n === nextChar ? next2.o : void 0 : next2.n.get(nextChar);
              if (value !== void 0) {
                ret += value;
                lastIdx = regExp.lastIndex += 1;
                continue;
              }
            }
            next2 = next2.v;
          }
          if (next2 !== void 0) {
            ret += next2;
            lastIdx = i + 1;
          } else {
            var cp = (0, escape_js_1.getCodePoint)(str, i);
            ret += "&#x".concat(cp.toString(16), ";");
            lastIdx = regExp.lastIndex += Number(cp !== char);
          }
        }
        return ret + str.substr(lastIdx);
      }
      __name(encodeHTMLTrieRe, "encodeHTMLTrieRe");
    }
  });

  // node_modules/entities/lib/index.js
  var require_lib3 = __commonJS({
    "node_modules/entities/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.decodeXMLStrict = exports4.decodeHTML5Strict = exports4.decodeHTML4Strict = exports4.decodeHTML5 = exports4.decodeHTML4 = exports4.decodeHTMLAttribute = exports4.decodeHTMLStrict = exports4.decodeHTML = exports4.decodeXML = exports4.DecodingMode = exports4.EntityDecoder = exports4.encodeHTML5 = exports4.encodeHTML4 = exports4.encodeNonAsciiHTML = exports4.encodeHTML = exports4.escapeText = exports4.escapeAttribute = exports4.escapeUTF8 = exports4.escape = exports4.encodeXML = exports4.encode = exports4.decodeStrict = exports4.decode = exports4.EncodingMode = exports4.EntityLevel = void 0;
      var decode_js_1 = require_decode();
      var encode_js_1 = require_encode();
      var escape_js_1 = require_escape();
      var EntityLevel;
      (function(EntityLevel2) {
        EntityLevel2[EntityLevel2["XML"] = 0] = "XML";
        EntityLevel2[EntityLevel2["HTML"] = 1] = "HTML";
      })(EntityLevel = exports4.EntityLevel || (exports4.EntityLevel = {}));
      var EncodingMode;
      (function(EncodingMode2) {
        EncodingMode2[EncodingMode2["UTF8"] = 0] = "UTF8";
        EncodingMode2[EncodingMode2["ASCII"] = 1] = "ASCII";
        EncodingMode2[EncodingMode2["Extensive"] = 2] = "Extensive";
        EncodingMode2[EncodingMode2["Attribute"] = 3] = "Attribute";
        EncodingMode2[EncodingMode2["Text"] = 4] = "Text";
      })(EncodingMode = exports4.EncodingMode || (exports4.EncodingMode = {}));
      function decode(data2, options) {
        if (options === void 0) {
          options = EntityLevel.XML;
        }
        var level = typeof options === "number" ? options : options.level;
        if (level === EntityLevel.HTML) {
          var mode = typeof options === "object" ? options.mode : void 0;
          return (0, decode_js_1.decodeHTML)(data2, mode);
        }
        return (0, decode_js_1.decodeXML)(data2);
      }
      __name(decode, "decode");
      exports4.decode = decode;
      function decodeStrict(data2, options) {
        var _a;
        if (options === void 0) {
          options = EntityLevel.XML;
        }
        var opts = typeof options === "number" ? { level: options } : options;
        (_a = opts.mode) !== null && _a !== void 0 ? _a : opts.mode = decode_js_1.DecodingMode.Strict;
        return decode(data2, opts);
      }
      __name(decodeStrict, "decodeStrict");
      exports4.decodeStrict = decodeStrict;
      function encode(data2, options) {
        if (options === void 0) {
          options = EntityLevel.XML;
        }
        var opts = typeof options === "number" ? { level: options } : options;
        if (opts.mode === EncodingMode.UTF8)
          return (0, escape_js_1.escapeUTF8)(data2);
        if (opts.mode === EncodingMode.Attribute)
          return (0, escape_js_1.escapeAttribute)(data2);
        if (opts.mode === EncodingMode.Text)
          return (0, escape_js_1.escapeText)(data2);
        if (opts.level === EntityLevel.HTML) {
          if (opts.mode === EncodingMode.ASCII) {
            return (0, encode_js_1.encodeNonAsciiHTML)(data2);
          }
          return (0, encode_js_1.encodeHTML)(data2);
        }
        return (0, escape_js_1.encodeXML)(data2);
      }
      __name(encode, "encode");
      exports4.encode = encode;
      var escape_js_2 = require_escape();
      Object.defineProperty(exports4, "encodeXML", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return escape_js_2.encodeXML;
      }, "get") });
      Object.defineProperty(exports4, "escape", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return escape_js_2.escape;
      }, "get") });
      Object.defineProperty(exports4, "escapeUTF8", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return escape_js_2.escapeUTF8;
      }, "get") });
      Object.defineProperty(exports4, "escapeAttribute", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return escape_js_2.escapeAttribute;
      }, "get") });
      Object.defineProperty(exports4, "escapeText", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return escape_js_2.escapeText;
      }, "get") });
      var encode_js_2 = require_encode();
      Object.defineProperty(exports4, "encodeHTML", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return encode_js_2.encodeHTML;
      }, "get") });
      Object.defineProperty(exports4, "encodeNonAsciiHTML", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return encode_js_2.encodeNonAsciiHTML;
      }, "get") });
      Object.defineProperty(exports4, "encodeHTML4", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return encode_js_2.encodeHTML;
      }, "get") });
      Object.defineProperty(exports4, "encodeHTML5", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return encode_js_2.encodeHTML;
      }, "get") });
      var decode_js_2 = require_decode();
      Object.defineProperty(exports4, "EntityDecoder", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.EntityDecoder;
      }, "get") });
      Object.defineProperty(exports4, "DecodingMode", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.DecodingMode;
      }, "get") });
      Object.defineProperty(exports4, "decodeXML", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeXML;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTML", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTML;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTMLStrict", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTMLStrict;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTMLAttribute", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTMLAttribute;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTML4", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTML;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTML5", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTML;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTML4Strict", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTMLStrict;
      }, "get") });
      Object.defineProperty(exports4, "decodeHTML5Strict", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeHTMLStrict;
      }, "get") });
      Object.defineProperty(exports4, "decodeXMLStrict", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return decode_js_2.decodeXML;
      }, "get") });
    }
  });

  // node_modules/dom-serializer/lib/foreignNames.js
  var require_foreignNames = __commonJS({
    "node_modules/dom-serializer/lib/foreignNames.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.attributeNames = exports4.elementNames = void 0;
      exports4.elementNames = new Map([
        "altGlyph",
        "altGlyphDef",
        "altGlyphItem",
        "animateColor",
        "animateMotion",
        "animateTransform",
        "clipPath",
        "feBlend",
        "feColorMatrix",
        "feComponentTransfer",
        "feComposite",
        "feConvolveMatrix",
        "feDiffuseLighting",
        "feDisplacementMap",
        "feDistantLight",
        "feDropShadow",
        "feFlood",
        "feFuncA",
        "feFuncB",
        "feFuncG",
        "feFuncR",
        "feGaussianBlur",
        "feImage",
        "feMerge",
        "feMergeNode",
        "feMorphology",
        "feOffset",
        "fePointLight",
        "feSpecularLighting",
        "feSpotLight",
        "feTile",
        "feTurbulence",
        "foreignObject",
        "glyphRef",
        "linearGradient",
        "radialGradient",
        "textPath"
      ].map(function(val2) {
        return [val2.toLowerCase(), val2];
      }));
      exports4.attributeNames = new Map([
        "definitionURL",
        "attributeName",
        "attributeType",
        "baseFrequency",
        "baseProfile",
        "calcMode",
        "clipPathUnits",
        "diffuseConstant",
        "edgeMode",
        "filterUnits",
        "glyphRef",
        "gradientTransform",
        "gradientUnits",
        "kernelMatrix",
        "kernelUnitLength",
        "keyPoints",
        "keySplines",
        "keyTimes",
        "lengthAdjust",
        "limitingConeAngle",
        "markerHeight",
        "markerUnits",
        "markerWidth",
        "maskContentUnits",
        "maskUnits",
        "numOctaves",
        "pathLength",
        "patternContentUnits",
        "patternTransform",
        "patternUnits",
        "pointsAtX",
        "pointsAtY",
        "pointsAtZ",
        "preserveAlpha",
        "preserveAspectRatio",
        "primitiveUnits",
        "refX",
        "refY",
        "repeatCount",
        "repeatDur",
        "requiredExtensions",
        "requiredFeatures",
        "specularConstant",
        "specularExponent",
        "spreadMethod",
        "startOffset",
        "stdDeviation",
        "stitchTiles",
        "surfaceScale",
        "systemLanguage",
        "tableValues",
        "targetX",
        "targetY",
        "textLength",
        "viewBox",
        "viewTarget",
        "xChannelSelector",
        "yChannelSelector",
        "zoomAndPan"
      ].map(function(val2) {
        return [val2.toLowerCase(), val2];
      }));
    }
  });

  // node_modules/dom-serializer/lib/index.js
  var require_lib4 = __commonJS({
    "node_modules/dom-serializer/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __assign = exports4 && exports4.__assign || function() {
        __assign = Object.assign || function(t2) {
          for (var s, i = 1, n2 = arguments.length; i < n2; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
              t2[p] = s[p];
          }
          return t2;
        };
        return __assign.apply(this, arguments);
      };
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.render = void 0;
      var ElementType = __importStar(require_lib());
      var entities_1 = require_lib3();
      var foreignNames_js_1 = require_foreignNames();
      var unencodedElements = /* @__PURE__ */ new Set([
        "style",
        "script",
        "xmp",
        "iframe",
        "noembed",
        "noframes",
        "plaintext",
        "noscript"
      ]);
      function replaceQuotes(value) {
        return value.replace(/"/g, "&quot;");
      }
      __name(replaceQuotes, "replaceQuotes");
      function formatAttributes(attributes, opts) {
        var _a;
        if (!attributes)
          return;
        var encode = ((_a = opts.encodeEntities) !== null && _a !== void 0 ? _a : opts.decodeEntities) === false ? replaceQuotes : opts.xmlMode || opts.encodeEntities !== "utf8" ? entities_1.encodeXML : entities_1.escapeAttribute;
        return Object.keys(attributes).map(function(key) {
          var _a2, _b;
          var value = (_a2 = attributes[key]) !== null && _a2 !== void 0 ? _a2 : "";
          if (opts.xmlMode === "foreign") {
            key = (_b = foreignNames_js_1.attributeNames.get(key)) !== null && _b !== void 0 ? _b : key;
          }
          if (!opts.emptyAttrs && !opts.xmlMode && value === "") {
            return key;
          }
          return "".concat(key, '="').concat(encode(value), '"');
        }).join(" ");
      }
      __name(formatAttributes, "formatAttributes");
      var singleTag = /* @__PURE__ */ new Set([
        "area",
        "base",
        "basefont",
        "br",
        "col",
        "command",
        "embed",
        "frame",
        "hr",
        "img",
        "input",
        "isindex",
        "keygen",
        "link",
        "meta",
        "param",
        "source",
        "track",
        "wbr"
      ]);
      function render2(node, options) {
        if (options === void 0) {
          options = {};
        }
        var nodes = "length" in node ? node : [node];
        var output = "";
        for (var i = 0; i < nodes.length; i++) {
          output += renderNode(nodes[i], options);
        }
        return output;
      }
      __name(render2, "render");
      exports4.render = render2;
      exports4.default = render2;
      function renderNode(node, options) {
        switch (node.type) {
          case ElementType.Root:
            return render2(node.children, options);
          // @ts-expect-error We don't use `Doctype` yet
          case ElementType.Doctype:
          case ElementType.Directive:
            return renderDirective(node);
          case ElementType.Comment:
            return renderComment(node);
          case ElementType.CDATA:
            return renderCdata(node);
          case ElementType.Script:
          case ElementType.Style:
          case ElementType.Tag:
            return renderTag(node, options);
          case ElementType.Text:
            return renderText(node, options);
        }
      }
      __name(renderNode, "renderNode");
      var foreignModeIntegrationPoints = /* @__PURE__ */ new Set([
        "mi",
        "mo",
        "mn",
        "ms",
        "mtext",
        "annotation-xml",
        "foreignObject",
        "desc",
        "title"
      ]);
      var foreignElements = /* @__PURE__ */ new Set(["svg", "math"]);
      function renderTag(elem, opts) {
        var _a;
        if (opts.xmlMode === "foreign") {
          elem.name = (_a = foreignNames_js_1.elementNames.get(elem.name)) !== null && _a !== void 0 ? _a : elem.name;
          if (elem.parent && foreignModeIntegrationPoints.has(elem.parent.name)) {
            opts = __assign(__assign({}, opts), { xmlMode: false });
          }
        }
        if (!opts.xmlMode && foreignElements.has(elem.name)) {
          opts = __assign(__assign({}, opts), { xmlMode: "foreign" });
        }
        var tag = "<".concat(elem.name);
        var attribs = formatAttributes(elem.attribs, opts);
        if (attribs) {
          tag += " ".concat(attribs);
        }
        if (elem.children.length === 0 && (opts.xmlMode ? (
          // In XML mode or foreign mode, and user hasn't explicitly turned off self-closing tags
          opts.selfClosingTags !== false
        ) : (
          // User explicitly asked for self-closing tags, even in HTML mode
          opts.selfClosingTags && singleTag.has(elem.name)
        ))) {
          if (!opts.xmlMode)
            tag += " ";
          tag += "/>";
        } else {
          tag += ">";
          if (elem.children.length > 0) {
            tag += render2(elem.children, opts);
          }
          if (opts.xmlMode || !singleTag.has(elem.name)) {
            tag += "</".concat(elem.name, ">");
          }
        }
        return tag;
      }
      __name(renderTag, "renderTag");
      function renderDirective(elem) {
        return "<".concat(elem.data, ">");
      }
      __name(renderDirective, "renderDirective");
      function renderText(elem, opts) {
        var _a;
        var data2 = elem.data || "";
        if (((_a = opts.encodeEntities) !== null && _a !== void 0 ? _a : opts.decodeEntities) !== false && !(!opts.xmlMode && elem.parent && unencodedElements.has(elem.parent.name))) {
          data2 = opts.xmlMode || opts.encodeEntities !== "utf8" ? (0, entities_1.encodeXML)(data2) : (0, entities_1.escapeText)(data2);
        }
        return data2;
      }
      __name(renderText, "renderText");
      function renderCdata(elem) {
        return "<![CDATA[".concat(elem.children[0].data, "]]>");
      }
      __name(renderCdata, "renderCdata");
      function renderComment(elem) {
        return "<!--".concat(elem.data, "-->");
      }
      __name(renderComment, "renderComment");
    }
  });

  // node_modules/domutils/lib/stringify.js
  var require_stringify = __commonJS({
    "node_modules/domutils/lib/stringify.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.innerText = exports4.textContent = exports4.getText = exports4.getInnerHTML = exports4.getOuterHTML = void 0;
      var domhandler_1 = require_lib2();
      var dom_serializer_1 = __importDefault(require_lib4());
      var domelementtype_1 = require_lib();
      function getOuterHTML(node, options) {
        return (0, dom_serializer_1.default)(node, options);
      }
      __name(getOuterHTML, "getOuterHTML");
      exports4.getOuterHTML = getOuterHTML;
      function getInnerHTML(node, options) {
        return (0, domhandler_1.hasChildren)(node) ? node.children.map(function(node2) {
          return getOuterHTML(node2, options);
        }).join("") : "";
      }
      __name(getInnerHTML, "getInnerHTML");
      exports4.getInnerHTML = getInnerHTML;
      function getText(node) {
        if (Array.isArray(node))
          return node.map(getText).join("");
        if ((0, domhandler_1.isTag)(node))
          return node.name === "br" ? "\n" : getText(node.children);
        if ((0, domhandler_1.isCDATA)(node))
          return getText(node.children);
        if ((0, domhandler_1.isText)(node))
          return node.data;
        return "";
      }
      __name(getText, "getText");
      exports4.getText = getText;
      function textContent3(node) {
        if (Array.isArray(node))
          return node.map(textContent3).join("");
        if ((0, domhandler_1.hasChildren)(node) && !(0, domhandler_1.isComment)(node)) {
          return textContent3(node.children);
        }
        if ((0, domhandler_1.isText)(node))
          return node.data;
        return "";
      }
      __name(textContent3, "textContent");
      exports4.textContent = textContent3;
      function innerText2(node) {
        if (Array.isArray(node))
          return node.map(innerText2).join("");
        if ((0, domhandler_1.hasChildren)(node) && (node.type === domelementtype_1.ElementType.Tag || (0, domhandler_1.isCDATA)(node))) {
          return innerText2(node.children);
        }
        if ((0, domhandler_1.isText)(node))
          return node.data;
        return "";
      }
      __name(innerText2, "innerText");
      exports4.innerText = innerText2;
    }
  });

  // node_modules/domutils/lib/traversal.js
  var require_traversal = __commonJS({
    "node_modules/domutils/lib/traversal.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.prevElementSibling = exports4.nextElementSibling = exports4.getName = exports4.hasAttrib = exports4.getAttributeValue = exports4.getSiblings = exports4.getParent = exports4.getChildren = void 0;
      var domhandler_1 = require_lib2();
      function getChildren2(elem) {
        return (0, domhandler_1.hasChildren)(elem) ? elem.children : [];
      }
      __name(getChildren2, "getChildren");
      exports4.getChildren = getChildren2;
      function getParent(elem) {
        return elem.parent || null;
      }
      __name(getParent, "getParent");
      exports4.getParent = getParent;
      function getSiblings2(elem) {
        var _a, _b;
        var parent2 = getParent(elem);
        if (parent2 != null)
          return getChildren2(parent2);
        var siblings2 = [elem];
        var prev2 = elem.prev, next2 = elem.next;
        while (prev2 != null) {
          siblings2.unshift(prev2);
          _a = prev2, prev2 = _a.prev;
        }
        while (next2 != null) {
          siblings2.push(next2);
          _b = next2, next2 = _b.next;
        }
        return siblings2;
      }
      __name(getSiblings2, "getSiblings");
      exports4.getSiblings = getSiblings2;
      function getAttributeValue(elem, name) {
        var _a;
        return (_a = elem.attribs) === null || _a === void 0 ? void 0 : _a[name];
      }
      __name(getAttributeValue, "getAttributeValue");
      exports4.getAttributeValue = getAttributeValue;
      function hasAttrib(elem, name) {
        return elem.attribs != null && Object.prototype.hasOwnProperty.call(elem.attribs, name) && elem.attribs[name] != null;
      }
      __name(hasAttrib, "hasAttrib");
      exports4.hasAttrib = hasAttrib;
      function getName(elem) {
        return elem.name;
      }
      __name(getName, "getName");
      exports4.getName = getName;
      function nextElementSibling2(elem) {
        var _a;
        var next2 = elem.next;
        while (next2 !== null && !(0, domhandler_1.isTag)(next2))
          _a = next2, next2 = _a.next;
        return next2;
      }
      __name(nextElementSibling2, "nextElementSibling");
      exports4.nextElementSibling = nextElementSibling2;
      function prevElementSibling2(elem) {
        var _a;
        var prev2 = elem.prev;
        while (prev2 !== null && !(0, domhandler_1.isTag)(prev2))
          _a = prev2, prev2 = _a.prev;
        return prev2;
      }
      __name(prevElementSibling2, "prevElementSibling");
      exports4.prevElementSibling = prevElementSibling2;
    }
  });

  // node_modules/domutils/lib/manipulation.js
  var require_manipulation = __commonJS({
    "node_modules/domutils/lib/manipulation.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.prepend = exports4.prependChild = exports4.append = exports4.appendChild = exports4.replaceElement = exports4.removeElement = void 0;
      function removeElement3(elem) {
        if (elem.prev)
          elem.prev.next = elem.next;
        if (elem.next)
          elem.next.prev = elem.prev;
        if (elem.parent) {
          var childs = elem.parent.children;
          var childsIndex = childs.lastIndexOf(elem);
          if (childsIndex >= 0) {
            childs.splice(childsIndex, 1);
          }
        }
        elem.next = null;
        elem.prev = null;
        elem.parent = null;
      }
      __name(removeElement3, "removeElement");
      exports4.removeElement = removeElement3;
      function replaceElement(elem, replacement) {
        var prev2 = replacement.prev = elem.prev;
        if (prev2) {
          prev2.next = replacement;
        }
        var next2 = replacement.next = elem.next;
        if (next2) {
          next2.prev = replacement;
        }
        var parent2 = replacement.parent = elem.parent;
        if (parent2) {
          var childs = parent2.children;
          childs[childs.lastIndexOf(elem)] = replacement;
          elem.parent = null;
        }
      }
      __name(replaceElement, "replaceElement");
      exports4.replaceElement = replaceElement;
      function appendChild(parent2, child) {
        removeElement3(child);
        child.next = null;
        child.parent = parent2;
        if (parent2.children.push(child) > 1) {
          var sibling = parent2.children[parent2.children.length - 2];
          sibling.next = child;
          child.prev = sibling;
        } else {
          child.prev = null;
        }
      }
      __name(appendChild, "appendChild");
      exports4.appendChild = appendChild;
      function append2(elem, next2) {
        removeElement3(next2);
        var parent2 = elem.parent;
        var currNext = elem.next;
        next2.next = currNext;
        next2.prev = elem;
        elem.next = next2;
        next2.parent = parent2;
        if (currNext) {
          currNext.prev = next2;
          if (parent2) {
            var childs = parent2.children;
            childs.splice(childs.lastIndexOf(currNext), 0, next2);
          }
        } else if (parent2) {
          parent2.children.push(next2);
        }
      }
      __name(append2, "append");
      exports4.append = append2;
      function prependChild(parent2, child) {
        removeElement3(child);
        child.parent = parent2;
        child.prev = null;
        if (parent2.children.unshift(child) !== 1) {
          var sibling = parent2.children[1];
          sibling.prev = child;
          child.next = sibling;
        } else {
          child.next = null;
        }
      }
      __name(prependChild, "prependChild");
      exports4.prependChild = prependChild;
      function prepend2(elem, prev2) {
        removeElement3(prev2);
        var parent2 = elem.parent;
        if (parent2) {
          var childs = parent2.children;
          childs.splice(childs.indexOf(elem), 0, prev2);
        }
        if (elem.prev) {
          elem.prev.next = prev2;
        }
        prev2.parent = parent2;
        prev2.prev = elem.prev;
        prev2.next = elem;
        elem.prev = prev2;
      }
      __name(prepend2, "prepend");
      exports4.prepend = prepend2;
    }
  });

  // node_modules/domutils/lib/querying.js
  var require_querying = __commonJS({
    "node_modules/domutils/lib/querying.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.findAll = exports4.existsOne = exports4.findOne = exports4.findOneChild = exports4.find = exports4.filter = void 0;
      var domhandler_1 = require_lib2();
      function filter3(test, node, recurse, limit) {
        if (recurse === void 0) {
          recurse = true;
        }
        if (limit === void 0) {
          limit = Infinity;
        }
        return find2(test, Array.isArray(node) ? node : [node], recurse, limit);
      }
      __name(filter3, "filter");
      exports4.filter = filter3;
      function find2(test, nodes, recurse, limit) {
        var result = [];
        var nodeStack = [nodes];
        var indexStack = [0];
        for (; ; ) {
          if (indexStack[0] >= nodeStack[0].length) {
            if (indexStack.length === 1) {
              return result;
            }
            nodeStack.shift();
            indexStack.shift();
            continue;
          }
          var elem = nodeStack[0][indexStack[0]++];
          if (test(elem)) {
            result.push(elem);
            if (--limit <= 0)
              return result;
          }
          if (recurse && (0, domhandler_1.hasChildren)(elem) && elem.children.length > 0) {
            indexStack.unshift(0);
            nodeStack.unshift(elem.children);
          }
        }
      }
      __name(find2, "find");
      exports4.find = find2;
      function findOneChild(test, nodes) {
        return nodes.find(test);
      }
      __name(findOneChild, "findOneChild");
      exports4.findOneChild = findOneChild;
      function findOne(test, nodes, recurse) {
        if (recurse === void 0) {
          recurse = true;
        }
        var elem = null;
        for (var i = 0; i < nodes.length && !elem; i++) {
          var node = nodes[i];
          if (!(0, domhandler_1.isTag)(node)) {
            continue;
          } else if (test(node)) {
            elem = node;
          } else if (recurse && node.children.length > 0) {
            elem = findOne(test, node.children, true);
          }
        }
        return elem;
      }
      __name(findOne, "findOne");
      exports4.findOne = findOne;
      function existsOne(test, nodes) {
        return nodes.some(function(checked) {
          return (0, domhandler_1.isTag)(checked) && (test(checked) || existsOne(test, checked.children));
        });
      }
      __name(existsOne, "existsOne");
      exports4.existsOne = existsOne;
      function findAll(test, nodes) {
        var result = [];
        var nodeStack = [nodes];
        var indexStack = [0];
        for (; ; ) {
          if (indexStack[0] >= nodeStack[0].length) {
            if (nodeStack.length === 1) {
              return result;
            }
            nodeStack.shift();
            indexStack.shift();
            continue;
          }
          var elem = nodeStack[0][indexStack[0]++];
          if (!(0, domhandler_1.isTag)(elem))
            continue;
          if (test(elem))
            result.push(elem);
          if (elem.children.length > 0) {
            indexStack.unshift(0);
            nodeStack.unshift(elem.children);
          }
        }
      }
      __name(findAll, "findAll");
      exports4.findAll = findAll;
    }
  });

  // node_modules/domutils/lib/legacy.js
  var require_legacy = __commonJS({
    "node_modules/domutils/lib/legacy.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.getElementsByTagType = exports4.getElementsByTagName = exports4.getElementById = exports4.getElements = exports4.testElement = void 0;
      var domhandler_1 = require_lib2();
      var querying_js_1 = require_querying();
      var Checks = {
        tag_name: /* @__PURE__ */ __name(function(name) {
          if (typeof name === "function") {
            return function(elem) {
              return (0, domhandler_1.isTag)(elem) && name(elem.name);
            };
          } else if (name === "*") {
            return domhandler_1.isTag;
          }
          return function(elem) {
            return (0, domhandler_1.isTag)(elem) && elem.name === name;
          };
        }, "tag_name"),
        tag_type: /* @__PURE__ */ __name(function(type) {
          if (typeof type === "function") {
            return function(elem) {
              return type(elem.type);
            };
          }
          return function(elem) {
            return elem.type === type;
          };
        }, "tag_type"),
        tag_contains: /* @__PURE__ */ __name(function(data2) {
          if (typeof data2 === "function") {
            return function(elem) {
              return (0, domhandler_1.isText)(elem) && data2(elem.data);
            };
          }
          return function(elem) {
            return (0, domhandler_1.isText)(elem) && elem.data === data2;
          };
        }, "tag_contains")
      };
      function getAttribCheck(attrib, value) {
        if (typeof value === "function") {
          return function(elem) {
            return (0, domhandler_1.isTag)(elem) && value(elem.attribs[attrib]);
          };
        }
        return function(elem) {
          return (0, domhandler_1.isTag)(elem) && elem.attribs[attrib] === value;
        };
      }
      __name(getAttribCheck, "getAttribCheck");
      function combineFuncs(a2, b) {
        return function(elem) {
          return a2(elem) || b(elem);
        };
      }
      __name(combineFuncs, "combineFuncs");
      function compileTest(options) {
        var funcs = Object.keys(options).map(function(key) {
          var value = options[key];
          return Object.prototype.hasOwnProperty.call(Checks, key) ? Checks[key](value) : getAttribCheck(key, value);
        });
        return funcs.length === 0 ? null : funcs.reduce(combineFuncs);
      }
      __name(compileTest, "compileTest");
      function testElement(options, node) {
        var test = compileTest(options);
        return test ? test(node) : true;
      }
      __name(testElement, "testElement");
      exports4.testElement = testElement;
      function getElements(options, nodes, recurse, limit) {
        if (limit === void 0) {
          limit = Infinity;
        }
        var test = compileTest(options);
        return test ? (0, querying_js_1.filter)(test, nodes, recurse, limit) : [];
      }
      __name(getElements, "getElements");
      exports4.getElements = getElements;
      function getElementById(id, nodes, recurse) {
        if (recurse === void 0) {
          recurse = true;
        }
        if (!Array.isArray(nodes))
          nodes = [nodes];
        return (0, querying_js_1.findOne)(getAttribCheck("id", id), nodes, recurse);
      }
      __name(getElementById, "getElementById");
      exports4.getElementById = getElementById;
      function getElementsByTagName(tagName, nodes, recurse, limit) {
        if (recurse === void 0) {
          recurse = true;
        }
        if (limit === void 0) {
          limit = Infinity;
        }
        return (0, querying_js_1.filter)(Checks["tag_name"](tagName), nodes, recurse, limit);
      }
      __name(getElementsByTagName, "getElementsByTagName");
      exports4.getElementsByTagName = getElementsByTagName;
      function getElementsByTagType(type, nodes, recurse, limit) {
        if (recurse === void 0) {
          recurse = true;
        }
        if (limit === void 0) {
          limit = Infinity;
        }
        return (0, querying_js_1.filter)(Checks["tag_type"](type), nodes, recurse, limit);
      }
      __name(getElementsByTagType, "getElementsByTagType");
      exports4.getElementsByTagType = getElementsByTagType;
    }
  });

  // node_modules/domutils/lib/helpers.js
  var require_helpers = __commonJS({
    "node_modules/domutils/lib/helpers.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.uniqueSort = exports4.compareDocumentPosition = exports4.DocumentPosition = exports4.removeSubsets = void 0;
      var domhandler_1 = require_lib2();
      function removeSubsets(nodes) {
        var idx = nodes.length;
        while (--idx >= 0) {
          var node = nodes[idx];
          if (idx > 0 && nodes.lastIndexOf(node, idx - 1) >= 0) {
            nodes.splice(idx, 1);
            continue;
          }
          for (var ancestor = node.parent; ancestor; ancestor = ancestor.parent) {
            if (nodes.includes(ancestor)) {
              nodes.splice(idx, 1);
              break;
            }
          }
        }
        return nodes;
      }
      __name(removeSubsets, "removeSubsets");
      exports4.removeSubsets = removeSubsets;
      var DocumentPosition;
      (function(DocumentPosition2) {
        DocumentPosition2[DocumentPosition2["DISCONNECTED"] = 1] = "DISCONNECTED";
        DocumentPosition2[DocumentPosition2["PRECEDING"] = 2] = "PRECEDING";
        DocumentPosition2[DocumentPosition2["FOLLOWING"] = 4] = "FOLLOWING";
        DocumentPosition2[DocumentPosition2["CONTAINS"] = 8] = "CONTAINS";
        DocumentPosition2[DocumentPosition2["CONTAINED_BY"] = 16] = "CONTAINED_BY";
      })(DocumentPosition = exports4.DocumentPosition || (exports4.DocumentPosition = {}));
      function compareDocumentPosition(nodeA, nodeB) {
        var aParents = [];
        var bParents = [];
        if (nodeA === nodeB) {
          return 0;
        }
        var current = (0, domhandler_1.hasChildren)(nodeA) ? nodeA : nodeA.parent;
        while (current) {
          aParents.unshift(current);
          current = current.parent;
        }
        current = (0, domhandler_1.hasChildren)(nodeB) ? nodeB : nodeB.parent;
        while (current) {
          bParents.unshift(current);
          current = current.parent;
        }
        var maxIdx = Math.min(aParents.length, bParents.length);
        var idx = 0;
        while (idx < maxIdx && aParents[idx] === bParents[idx]) {
          idx++;
        }
        if (idx === 0) {
          return DocumentPosition.DISCONNECTED;
        }
        var sharedParent = aParents[idx - 1];
        var siblings2 = sharedParent.children;
        var aSibling = aParents[idx];
        var bSibling = bParents[idx];
        if (siblings2.indexOf(aSibling) > siblings2.indexOf(bSibling)) {
          if (sharedParent === nodeB) {
            return DocumentPosition.FOLLOWING | DocumentPosition.CONTAINED_BY;
          }
          return DocumentPosition.FOLLOWING;
        }
        if (sharedParent === nodeA) {
          return DocumentPosition.PRECEDING | DocumentPosition.CONTAINS;
        }
        return DocumentPosition.PRECEDING;
      }
      __name(compareDocumentPosition, "compareDocumentPosition");
      exports4.compareDocumentPosition = compareDocumentPosition;
      function uniqueSort2(nodes) {
        nodes = nodes.filter(function(node, i, arr) {
          return !arr.includes(node, i + 1);
        });
        nodes.sort(function(a2, b) {
          var relative = compareDocumentPosition(a2, b);
          if (relative & DocumentPosition.PRECEDING) {
            return -1;
          } else if (relative & DocumentPosition.FOLLOWING) {
            return 1;
          }
          return 0;
        });
        return nodes;
      }
      __name(uniqueSort2, "uniqueSort");
      exports4.uniqueSort = uniqueSort2;
    }
  });

  // node_modules/domutils/lib/feeds.js
  var require_feeds = __commonJS({
    "node_modules/domutils/lib/feeds.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.getFeed = void 0;
      var stringify_js_1 = require_stringify();
      var legacy_js_1 = require_legacy();
      function getFeed(doc) {
        var feedRoot = getOneElement(isValidFeed, doc);
        return !feedRoot ? null : feedRoot.name === "feed" ? getAtomFeed(feedRoot) : getRssFeed(feedRoot);
      }
      __name(getFeed, "getFeed");
      exports4.getFeed = getFeed;
      function getAtomFeed(feedRoot) {
        var _a;
        var childs = feedRoot.children;
        var feed = {
          type: "atom",
          items: (0, legacy_js_1.getElementsByTagName)("entry", childs).map(function(item) {
            var _a2;
            var children2 = item.children;
            var entry = { media: getMediaElements(children2) };
            addConditionally(entry, "id", "id", children2);
            addConditionally(entry, "title", "title", children2);
            var href2 = (_a2 = getOneElement("link", children2)) === null || _a2 === void 0 ? void 0 : _a2.attribs["href"];
            if (href2) {
              entry.link = href2;
            }
            var description = fetch2("summary", children2) || fetch2("content", children2);
            if (description) {
              entry.description = description;
            }
            var pubDate = fetch2("updated", children2);
            if (pubDate) {
              entry.pubDate = new Date(pubDate);
            }
            return entry;
          })
        };
        addConditionally(feed, "id", "id", childs);
        addConditionally(feed, "title", "title", childs);
        var href = (_a = getOneElement("link", childs)) === null || _a === void 0 ? void 0 : _a.attribs["href"];
        if (href) {
          feed.link = href;
        }
        addConditionally(feed, "description", "subtitle", childs);
        var updated = fetch2("updated", childs);
        if (updated) {
          feed.updated = new Date(updated);
        }
        addConditionally(feed, "author", "email", childs, true);
        return feed;
      }
      __name(getAtomFeed, "getAtomFeed");
      function getRssFeed(feedRoot) {
        var _a, _b;
        var childs = (_b = (_a = getOneElement("channel", feedRoot.children)) === null || _a === void 0 ? void 0 : _a.children) !== null && _b !== void 0 ? _b : [];
        var feed = {
          type: feedRoot.name.substr(0, 3),
          id: "",
          items: (0, legacy_js_1.getElementsByTagName)("item", feedRoot.children).map(function(item) {
            var children2 = item.children;
            var entry = { media: getMediaElements(children2) };
            addConditionally(entry, "id", "guid", children2);
            addConditionally(entry, "title", "title", children2);
            addConditionally(entry, "link", "link", children2);
            addConditionally(entry, "description", "description", children2);
            var pubDate = fetch2("pubDate", children2) || fetch2("dc:date", children2);
            if (pubDate)
              entry.pubDate = new Date(pubDate);
            return entry;
          })
        };
        addConditionally(feed, "title", "title", childs);
        addConditionally(feed, "link", "link", childs);
        addConditionally(feed, "description", "description", childs);
        var updated = fetch2("lastBuildDate", childs);
        if (updated) {
          feed.updated = new Date(updated);
        }
        addConditionally(feed, "author", "managingEditor", childs, true);
        return feed;
      }
      __name(getRssFeed, "getRssFeed");
      var MEDIA_KEYS_STRING = ["url", "type", "lang"];
      var MEDIA_KEYS_INT = [
        "fileSize",
        "bitrate",
        "framerate",
        "samplingrate",
        "channels",
        "duration",
        "height",
        "width"
      ];
      function getMediaElements(where) {
        return (0, legacy_js_1.getElementsByTagName)("media:content", where).map(function(elem) {
          var attribs = elem.attribs;
          var media = {
            medium: attribs["medium"],
            isDefault: !!attribs["isDefault"]
          };
          for (var _i = 0, MEDIA_KEYS_STRING_1 = MEDIA_KEYS_STRING; _i < MEDIA_KEYS_STRING_1.length; _i++) {
            var attrib = MEDIA_KEYS_STRING_1[_i];
            if (attribs[attrib]) {
              media[attrib] = attribs[attrib];
            }
          }
          for (var _a = 0, MEDIA_KEYS_INT_1 = MEDIA_KEYS_INT; _a < MEDIA_KEYS_INT_1.length; _a++) {
            var attrib = MEDIA_KEYS_INT_1[_a];
            if (attribs[attrib]) {
              media[attrib] = parseInt(attribs[attrib], 10);
            }
          }
          if (attribs["expression"]) {
            media.expression = attribs["expression"];
          }
          return media;
        });
      }
      __name(getMediaElements, "getMediaElements");
      function getOneElement(tagName, node) {
        return (0, legacy_js_1.getElementsByTagName)(tagName, node, true, 1)[0];
      }
      __name(getOneElement, "getOneElement");
      function fetch2(tagName, where, recurse) {
        if (recurse === void 0) {
          recurse = false;
        }
        return (0, stringify_js_1.textContent)((0, legacy_js_1.getElementsByTagName)(tagName, where, recurse, 1)).trim();
      }
      __name(fetch2, "fetch");
      function addConditionally(obj, prop2, tagName, where, recurse) {
        if (recurse === void 0) {
          recurse = false;
        }
        var val2 = fetch2(tagName, where, recurse);
        if (val2)
          obj[prop2] = val2;
      }
      __name(addConditionally, "addConditionally");
      function isValidFeed(value) {
        return value === "rss" || value === "feed" || value === "rdf:RDF";
      }
      __name(isValidFeed, "isValidFeed");
    }
  });

  // node_modules/domutils/lib/index.js
  var require_lib5 = __commonJS({
    "node_modules/domutils/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __exportStar = exports4 && exports4.__exportStar || function(m, exports5) {
        for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports5, p)) __createBinding(exports5, m, p);
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.hasChildren = exports4.isDocument = exports4.isComment = exports4.isText = exports4.isCDATA = exports4.isTag = void 0;
      __exportStar(require_stringify(), exports4);
      __exportStar(require_traversal(), exports4);
      __exportStar(require_manipulation(), exports4);
      __exportStar(require_querying(), exports4);
      __exportStar(require_legacy(), exports4);
      __exportStar(require_helpers(), exports4);
      __exportStar(require_feeds(), exports4);
      var domhandler_1 = require_lib2();
      Object.defineProperty(exports4, "isTag", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.isTag;
      }, "get") });
      Object.defineProperty(exports4, "isCDATA", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.isCDATA;
      }, "get") });
      Object.defineProperty(exports4, "isText", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.isText;
      }, "get") });
      Object.defineProperty(exports4, "isComment", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.isComment;
      }, "get") });
      Object.defineProperty(exports4, "isDocument", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.isDocument;
      }, "get") });
      Object.defineProperty(exports4, "hasChildren", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_1.hasChildren;
      }, "get") });
    }
  });

  // node_modules/cheerio/dist/browser/options.js
  function flattenOptions(options, baseOptions) {
    if (!options) {
      return baseOptions !== null && baseOptions !== void 0 ? baseOptions : defaultOpts;
    }
    const opts = {
      _useHtmlParser2: !!options.xmlMode,
      ...baseOptions,
      ...options
    };
    if (options.xml) {
      opts._useHtmlParser2 = true;
      opts.xmlMode = true;
      if (options.xml !== true) {
        Object.assign(opts, options.xml);
      }
    } else if (options.xmlMode) {
      opts._useHtmlParser2 = true;
    }
    return opts;
  }
  var defaultOpts;
  var init_options = __esm({
    "node_modules/cheerio/dist/browser/options.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      defaultOpts = {
        _useHtmlParser2: false
      };
      __name(flattenOptions, "flattenOptions");
    }
  });

  // node_modules/cheerio/dist/browser/static.js
  var static_exports = {};
  __export(static_exports, {
    contains: () => contains,
    extract: () => extract,
    html: () => html,
    merge: () => merge,
    parseHTML: () => parseHTML,
    root: () => root,
    text: () => text,
    xml: () => xml
  });
  function render(that, dom, options) {
    if (!that)
      return "";
    return that(dom !== null && dom !== void 0 ? dom : that._root.children, null, void 0, options).toString();
  }
  function isOptions(dom, options) {
    return !options && typeof dom === "object" && dom != null && !("length" in dom) && !("type" in dom);
  }
  function html(dom, options) {
    const toRender = isOptions(dom) ? (options = dom, void 0) : dom;
    const opts = {
      ...this === null || this === void 0 ? void 0 : this._options,
      ...flattenOptions(options)
    };
    return render(this, toRender, opts);
  }
  function xml(dom) {
    const options = { ...this._options, xmlMode: true };
    return render(this, dom, options);
  }
  function text(elements) {
    const elems = elements !== null && elements !== void 0 ? elements : this ? this.root() : [];
    let ret = "";
    for (let i = 0; i < elems.length; i++) {
      ret += (0, import_domutils.textContent)(elems[i]);
    }
    return ret;
  }
  function parseHTML(data2, context, keepScripts = typeof context === "boolean" ? context : false) {
    if (!data2 || typeof data2 !== "string") {
      return null;
    }
    if (typeof context === "boolean") {
      keepScripts = context;
    }
    const parsed = this.load(data2, this._options, false);
    if (!keepScripts) {
      parsed("script").remove();
    }
    return [...parsed.root()[0].children];
  }
  function root() {
    return this(this._root);
  }
  function contains(container, contained) {
    if (contained === container) {
      return false;
    }
    let next2 = contained;
    while (next2 && next2 !== next2.parent) {
      next2 = next2.parent;
      if (next2 === container) {
        return true;
      }
    }
    return false;
  }
  function extract(map2) {
    return this.root().extract(map2);
  }
  function merge(arr1, arr2) {
    if (!isArrayLike(arr1) || !isArrayLike(arr2)) {
      return;
    }
    let newLength = arr1.length;
    const len = +arr2.length;
    for (let i = 0; i < len; i++) {
      arr1[newLength++] = arr2[i];
    }
    arr1.length = newLength;
    return arr1;
  }
  function isArrayLike(item) {
    if (Array.isArray(item)) {
      return true;
    }
    if (typeof item !== "object" || item === null || !("length" in item) || typeof item.length !== "number" || item.length < 0) {
      return false;
    }
    for (let i = 0; i < item.length; i++) {
      if (!(i in item)) {
        return false;
      }
    }
    return true;
  }
  var import_domutils;
  var init_static = __esm({
    "node_modules/cheerio/dist/browser/static.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domutils = __toESM(require_lib5(), 1);
      init_options();
      __name(render, "render");
      __name(isOptions, "isOptions");
      __name(html, "html");
      __name(xml, "xml");
      __name(text, "text");
      __name(parseHTML, "parseHTML");
      __name(root, "root");
      __name(contains, "contains");
      __name(extract, "extract");
      __name(merge, "merge");
      __name(isArrayLike, "isArrayLike");
    }
  });

  // node_modules/cheerio/dist/browser/utils.js
  function isCheerio(maybeCheerio) {
    return maybeCheerio.cheerio != null;
  }
  function camelCase(str) {
    return str.replace(/[._-](\w|$)/g, (_, x) => x.toUpperCase());
  }
  function cssCase(str) {
    return str.replace(/[A-Z]/g, "-$&").toLowerCase();
  }
  function domEach(array, fn) {
    const len = array.length;
    for (let i = 0; i < len; i++)
      fn(array[i], i);
    return array;
  }
  function isHtml(str) {
    const tagStart = str.indexOf("<");
    if (tagStart < 0 || tagStart > str.length - 3)
      return false;
    const tagChar = str.charCodeAt(tagStart + 1);
    return (tagChar >= CharacterCodes.LowerA && tagChar <= CharacterCodes.LowerZ || tagChar >= CharacterCodes.UpperA && tagChar <= CharacterCodes.UpperZ || tagChar === CharacterCodes.Exclamation) && str.includes(">", tagStart + 2);
  }
  var CharacterCodes;
  var init_utils = __esm({
    "node_modules/cheerio/dist/browser/utils.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      __name(isCheerio, "isCheerio");
      __name(camelCase, "camelCase");
      __name(cssCase, "cssCase");
      __name(domEach, "domEach");
      (function(CharacterCodes2) {
        CharacterCodes2[CharacterCodes2["LowerA"] = 97] = "LowerA";
        CharacterCodes2[CharacterCodes2["LowerZ"] = 122] = "LowerZ";
        CharacterCodes2[CharacterCodes2["UpperA"] = 65] = "UpperA";
        CharacterCodes2[CharacterCodes2["UpperZ"] = 90] = "UpperZ";
        CharacterCodes2[CharacterCodes2["Exclamation"] = 33] = "Exclamation";
      })(CharacterCodes || (CharacterCodes = {}));
      __name(isHtml, "isHtml");
    }
  });

  // node_modules/cheerio/dist/browser/api/attributes.js
  var attributes_exports = {};
  __export(attributes_exports, {
    addClass: () => addClass,
    attr: () => attr,
    data: () => data,
    hasClass: () => hasClass,
    prop: () => prop,
    removeAttr: () => removeAttr,
    removeClass: () => removeClass,
    toggleClass: () => toggleClass,
    val: () => val
  });
  function getAttr(elem, name, xmlMode) {
    var _a;
    if (!elem || !(0, import_domhandler.isTag)(elem))
      return void 0;
    (_a = elem.attribs) !== null && _a !== void 0 ? _a : elem.attribs = {};
    if (!name) {
      return elem.attribs;
    }
    if (hasOwn.call(elem.attribs, name)) {
      return !xmlMode && rboolean.test(name) ? name : elem.attribs[name];
    }
    if (elem.name === "option" && name === "value") {
      return text(elem.children);
    }
    if (elem.name === "input" && (elem.attribs["type"] === "radio" || elem.attribs["type"] === "checkbox") && name === "value") {
      return "on";
    }
    return void 0;
  }
  function setAttr(el, name, value) {
    if (value === null) {
      removeAttribute(el, name);
    } else {
      el.attribs[name] = `${value}`;
    }
  }
  function attr(name, value) {
    if (typeof name === "object" || value !== void 0) {
      if (typeof value === "function") {
        if (typeof name !== "string") {
          {
            throw new Error("Bad combination of arguments.");
          }
        }
        return domEach(this, (el, i) => {
          if ((0, import_domhandler.isTag)(el))
            setAttr(el, name, value.call(el, i, el.attribs[name]));
        });
      }
      return domEach(this, (el) => {
        if (!(0, import_domhandler.isTag)(el))
          return;
        if (typeof name === "object") {
          for (const objName of Object.keys(name)) {
            const objValue = name[objName];
            setAttr(el, objName, objValue);
          }
        } else {
          setAttr(el, name, value);
        }
      });
    }
    return arguments.length > 1 ? this : getAttr(this[0], name, this.options.xmlMode);
  }
  function getProp(el, name, xmlMode) {
    return name in el ? (
      // @ts-expect-error TS doesn't like us accessing the value directly here.
      el[name]
    ) : !xmlMode && rboolean.test(name) ? getAttr(el, name, false) !== void 0 : getAttr(el, name, xmlMode);
  }
  function setProp(el, name, value, xmlMode) {
    if (name in el) {
      el[name] = value;
    } else {
      setAttr(el, name, !xmlMode && rboolean.test(name) ? value ? "" : null : `${value}`);
    }
  }
  function prop(name, value) {
    var _a;
    if (typeof name === "string" && value === void 0) {
      const el = this[0];
      if (!el || !(0, import_domhandler.isTag)(el))
        return void 0;
      switch (name) {
        case "style": {
          const property = this.css();
          const keys = Object.keys(property);
          for (let i = 0; i < keys.length; i++) {
            property[i] = keys[i];
          }
          property.length = keys.length;
          return property;
        }
        case "tagName":
        case "nodeName": {
          return el.name.toUpperCase();
        }
        case "href":
        case "src": {
          const prop2 = (_a = el.attribs) === null || _a === void 0 ? void 0 : _a[name];
          if (typeof URL !== "undefined" && (name === "href" && (el.tagName === "a" || el.tagName === "link") || name === "src" && (el.tagName === "img" || el.tagName === "iframe" || el.tagName === "audio" || el.tagName === "video" || el.tagName === "source")) && prop2 !== void 0 && this.options.baseURI) {
            return new URL(prop2, this.options.baseURI).href;
          }
          return prop2;
        }
        case "innerText": {
          return (0, import_domutils2.innerText)(el);
        }
        case "textContent": {
          return (0, import_domutils2.textContent)(el);
        }
        case "outerHTML": {
          return this.clone().wrap("<container />").parent().html();
        }
        case "innerHTML": {
          return this.html();
        }
        default: {
          return getProp(el, name, this.options.xmlMode);
        }
      }
    }
    if (typeof name === "object" || value !== void 0) {
      if (typeof value === "function") {
        if (typeof name === "object") {
          throw new TypeError("Bad combination of arguments.");
        }
        return domEach(this, (el, i) => {
          if ((0, import_domhandler.isTag)(el)) {
            setProp(el, name, value.call(el, i, getProp(el, name, this.options.xmlMode)), this.options.xmlMode);
          }
        });
      }
      return domEach(this, (el) => {
        if (!(0, import_domhandler.isTag)(el))
          return;
        if (typeof name === "object") {
          for (const key of Object.keys(name)) {
            const val2 = name[key];
            setProp(el, key, val2, this.options.xmlMode);
          }
        } else {
          setProp(el, name, value, this.options.xmlMode);
        }
      });
    }
    return void 0;
  }
  function setData(elem, name, value) {
    var _a;
    (_a = elem.data) !== null && _a !== void 0 ? _a : elem.data = {};
    if (typeof name === "object")
      Object.assign(elem.data, name);
    else if (typeof name === "string" && value !== void 0) {
      elem.data[name] = value;
    }
  }
  function readAllData(el) {
    for (const domName of Object.keys(el.attribs)) {
      if (!domName.startsWith(dataAttrPrefix)) {
        continue;
      }
      const jsName = camelCase(domName.slice(dataAttrPrefix.length));
      if (!hasOwn.call(el.data, jsName)) {
        el.data[jsName] = parseDataValue(el.attribs[domName]);
      }
    }
    return el.data;
  }
  function readData(el, name) {
    const domName = dataAttrPrefix + cssCase(name);
    const data2 = el.data;
    if (hasOwn.call(data2, name)) {
      return data2[name];
    }
    if (hasOwn.call(el.attribs, domName)) {
      return data2[name] = parseDataValue(el.attribs[domName]);
    }
    return void 0;
  }
  function parseDataValue(value) {
    if (value === "null")
      return null;
    if (value === "true")
      return true;
    if (value === "false")
      return false;
    const num = Number(value);
    if (value === String(num))
      return num;
    if (rbrace.test(value)) {
      try {
        return JSON.parse(value);
      } catch {
      }
    }
    return value;
  }
  function data(name, value) {
    var _a;
    const elem = this[0];
    if (!elem || !(0, import_domhandler.isTag)(elem))
      return;
    const dataEl = elem;
    (_a = dataEl.data) !== null && _a !== void 0 ? _a : dataEl.data = {};
    if (name == null) {
      return readAllData(dataEl);
    }
    if (typeof name === "object" || value !== void 0) {
      domEach(this, (el) => {
        if ((0, import_domhandler.isTag)(el)) {
          if (typeof name === "object")
            setData(el, name);
          else
            setData(el, name, value);
        }
      });
      return this;
    }
    return readData(dataEl, name);
  }
  function val(value) {
    const querying = arguments.length === 0;
    const element = this[0];
    if (!element || !(0, import_domhandler.isTag)(element))
      return querying ? void 0 : this;
    switch (element.name) {
      case "textarea": {
        return this.text(value);
      }
      case "select": {
        const option = this.find("option:selected");
        if (!querying) {
          if (this.attr("multiple") == null && typeof value === "object") {
            return this;
          }
          this.find("option").removeAttr("selected");
          const values = typeof value === "object" ? value : [value];
          for (const val2 of values) {
            this.find(`option[value="${val2}"]`).attr("selected", "");
          }
          return this;
        }
        return this.attr("multiple") ? option.toArray().map((el) => text(el.children)) : option.attr("value");
      }
      case "input":
      case "option": {
        return querying ? this.attr("value") : this.attr("value", value);
      }
    }
    return void 0;
  }
  function removeAttribute(elem, name) {
    if (!elem.attribs || !hasOwn.call(elem.attribs, name))
      return;
    delete elem.attribs[name];
  }
  function splitNames(names) {
    return names ? names.trim().split(rspace) : [];
  }
  function removeAttr(name) {
    const attrNames = splitNames(name);
    for (const attrName of attrNames) {
      domEach(this, (elem) => {
        if ((0, import_domhandler.isTag)(elem))
          removeAttribute(elem, attrName);
      });
    }
    return this;
  }
  function hasClass(className) {
    return this.toArray().some((elem) => {
      const clazz = (0, import_domhandler.isTag)(elem) && elem.attribs["class"];
      let idx = -1;
      if (clazz && className.length > 0) {
        while ((idx = clazz.indexOf(className, idx + 1)) > -1) {
          const end2 = idx + className.length;
          if ((idx === 0 || rspace.test(clazz[idx - 1])) && (end2 === clazz.length || rspace.test(clazz[end2]))) {
            return true;
          }
        }
      }
      return false;
    });
  }
  function addClass(value) {
    if (typeof value === "function") {
      return domEach(this, (el, i) => {
        if ((0, import_domhandler.isTag)(el)) {
          const className = el.attribs["class"] || "";
          addClass.call([el], value.call(el, i, className));
        }
      });
    }
    if (!value || typeof value !== "string")
      return this;
    const classNames = value.split(rspace);
    const numElements = this.length;
    for (let i = 0; i < numElements; i++) {
      const el = this[i];
      if (!(0, import_domhandler.isTag)(el))
        continue;
      const className = getAttr(el, "class", false);
      if (className) {
        let setClass = ` ${className} `;
        for (const cn of classNames) {
          const appendClass = `${cn} `;
          if (!setClass.includes(` ${appendClass}`))
            setClass += appendClass;
        }
        setAttr(el, "class", setClass.trim());
      } else {
        setAttr(el, "class", classNames.join(" ").trim());
      }
    }
    return this;
  }
  function removeClass(name) {
    if (typeof name === "function") {
      return domEach(this, (el, i) => {
        if ((0, import_domhandler.isTag)(el)) {
          removeClass.call([el], name.call(el, i, el.attribs["class"] || ""));
        }
      });
    }
    const classes = splitNames(name);
    const numClasses = classes.length;
    const removeAll = arguments.length === 0;
    return domEach(this, (el) => {
      if (!(0, import_domhandler.isTag)(el))
        return;
      if (removeAll) {
        el.attribs["class"] = "";
      } else {
        const elClasses = splitNames(el.attribs["class"]);
        let changed = false;
        for (let j = 0; j < numClasses; j++) {
          const index2 = elClasses.indexOf(classes[j]);
          if (index2 >= 0) {
            elClasses.splice(index2, 1);
            changed = true;
            j--;
          }
        }
        if (changed) {
          el.attribs["class"] = elClasses.join(" ");
        }
      }
    });
  }
  function toggleClass(value, stateVal) {
    if (typeof value === "function") {
      return domEach(this, (el, i) => {
        if ((0, import_domhandler.isTag)(el)) {
          toggleClass.call([el], value.call(el, i, el.attribs["class"] || "", stateVal), stateVal);
        }
      });
    }
    if (!value || typeof value !== "string")
      return this;
    const classNames = value.split(rspace);
    const numClasses = classNames.length;
    const state = typeof stateVal === "boolean" ? stateVal ? 1 : -1 : 0;
    const numElements = this.length;
    for (let i = 0; i < numElements; i++) {
      const el = this[i];
      if (!(0, import_domhandler.isTag)(el))
        continue;
      const elementClasses = splitNames(el.attribs["class"]);
      for (let j = 0; j < numClasses; j++) {
        const index2 = elementClasses.indexOf(classNames[j]);
        if (state >= 0 && index2 < 0) {
          elementClasses.push(classNames[j]);
        } else if (state <= 0 && index2 >= 0) {
          elementClasses.splice(index2, 1);
        }
      }
      el.attribs["class"] = elementClasses.join(" ");
    }
    return this;
  }
  var import_domhandler, import_domutils2, hasOwn, rspace, dataAttrPrefix, rboolean, rbrace;
  var init_attributes = __esm({
    "node_modules/cheerio/dist/browser/api/attributes.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_static();
      init_utils();
      import_domhandler = __toESM(require_lib2(), 1);
      import_domutils2 = __toESM(require_lib5(), 1);
      hasOwn = Object.prototype.hasOwnProperty;
      rspace = /\s+/;
      dataAttrPrefix = "data-";
      rboolean = /^(?:autofocus|autoplay|async|checked|controls|defer|disabled|hidden|loop|multiple|open|readonly|required|scoped|selected)$/i;
      rbrace = /^{[^]*}$|^\[[^]*]$/;
      __name(getAttr, "getAttr");
      __name(setAttr, "setAttr");
      __name(attr, "attr");
      __name(getProp, "getProp");
      __name(setProp, "setProp");
      __name(prop, "prop");
      __name(setData, "setData");
      __name(readAllData, "readAllData");
      __name(readData, "readData");
      __name(parseDataValue, "parseDataValue");
      __name(data, "data");
      __name(val, "val");
      __name(removeAttribute, "removeAttribute");
      __name(splitNames, "splitNames");
      __name(removeAttr, "removeAttr");
      __name(hasClass, "hasClass");
      __name(addClass, "addClass");
      __name(removeClass, "removeClass");
      __name(toggleClass, "toggleClass");
    }
  });

  // node_modules/css-what/lib/es/types.js
  var SelectorType, IgnoreCaseMode, AttributeAction;
  var init_types = __esm({
    "node_modules/css-what/lib/es/types.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      (function(SelectorType2) {
        SelectorType2["Attribute"] = "attribute";
        SelectorType2["Pseudo"] = "pseudo";
        SelectorType2["PseudoElement"] = "pseudo-element";
        SelectorType2["Tag"] = "tag";
        SelectorType2["Universal"] = "universal";
        SelectorType2["Adjacent"] = "adjacent";
        SelectorType2["Child"] = "child";
        SelectorType2["Descendant"] = "descendant";
        SelectorType2["Parent"] = "parent";
        SelectorType2["Sibling"] = "sibling";
        SelectorType2["ColumnCombinator"] = "column-combinator";
      })(SelectorType || (SelectorType = {}));
      IgnoreCaseMode = {
        Unknown: null,
        QuirksMode: "quirks",
        IgnoreCase: true,
        CaseSensitive: false
      };
      (function(AttributeAction2) {
        AttributeAction2["Any"] = "any";
        AttributeAction2["Element"] = "element";
        AttributeAction2["End"] = "end";
        AttributeAction2["Equals"] = "equals";
        AttributeAction2["Exists"] = "exists";
        AttributeAction2["Hyphen"] = "hyphen";
        AttributeAction2["Not"] = "not";
        AttributeAction2["Start"] = "start";
      })(AttributeAction || (AttributeAction = {}));
    }
  });

  // node_modules/css-what/lib/es/parse.js
  function isTraversal(selector) {
    switch (selector.type) {
      case SelectorType.Adjacent:
      case SelectorType.Child:
      case SelectorType.Descendant:
      case SelectorType.Parent:
      case SelectorType.Sibling:
      case SelectorType.ColumnCombinator:
        return true;
      default:
        return false;
    }
  }
  function funescape(_, escaped, escapedWhitespace) {
    const high = parseInt(escaped, 16) - 65536;
    return high !== high || escapedWhitespace ? escaped : high < 0 ? (
      // BMP codepoint
      String.fromCharCode(high + 65536)
    ) : (
      // Supplemental Plane codepoint (surrogate pair)
      String.fromCharCode(high >> 10 | 55296, high & 1023 | 56320)
    );
  }
  function unescapeCSS(str) {
    return str.replace(reEscape, funescape);
  }
  function isQuote(c) {
    return c === 39 || c === 34;
  }
  function isWhitespace(c) {
    return c === 32 || c === 9 || c === 10 || c === 12 || c === 13;
  }
  function parse(selector) {
    const subselects = [];
    const endIndex = parseSelector(subselects, `${selector}`, 0);
    if (endIndex < selector.length) {
      throw new Error(`Unmatched selector: ${selector.slice(endIndex)}`);
    }
    return subselects;
  }
  function parseSelector(subselects, selector, selectorIndex) {
    let tokens = [];
    function getName(offset) {
      const match = selector.slice(selectorIndex + offset).match(reName);
      if (!match) {
        throw new Error(`Expected name, found ${selector.slice(selectorIndex)}`);
      }
      const [name] = match;
      selectorIndex += offset + name.length;
      return unescapeCSS(name);
    }
    __name(getName, "getName");
    function stripWhitespace(offset) {
      selectorIndex += offset;
      while (selectorIndex < selector.length && isWhitespace(selector.charCodeAt(selectorIndex))) {
        selectorIndex++;
      }
    }
    __name(stripWhitespace, "stripWhitespace");
    function readValueWithParenthesis() {
      selectorIndex += 1;
      const start = selectorIndex;
      let counter = 1;
      for (; counter > 0 && selectorIndex < selector.length; selectorIndex++) {
        if (selector.charCodeAt(selectorIndex) === 40 && !isEscaped(selectorIndex)) {
          counter++;
        } else if (selector.charCodeAt(selectorIndex) === 41 && !isEscaped(selectorIndex)) {
          counter--;
        }
      }
      if (counter) {
        throw new Error("Parenthesis not matched");
      }
      return unescapeCSS(selector.slice(start, selectorIndex - 1));
    }
    __name(readValueWithParenthesis, "readValueWithParenthesis");
    function isEscaped(pos) {
      let slashCount = 0;
      while (selector.charCodeAt(--pos) === 92)
        slashCount++;
      return (slashCount & 1) === 1;
    }
    __name(isEscaped, "isEscaped");
    function ensureNotTraversal() {
      if (tokens.length > 0 && isTraversal(tokens[tokens.length - 1])) {
        throw new Error("Did not expect successive traversals.");
      }
    }
    __name(ensureNotTraversal, "ensureNotTraversal");
    function addTraversal(type) {
      if (tokens.length > 0 && tokens[tokens.length - 1].type === SelectorType.Descendant) {
        tokens[tokens.length - 1].type = type;
        return;
      }
      ensureNotTraversal();
      tokens.push({ type });
    }
    __name(addTraversal, "addTraversal");
    function addSpecialAttribute(name, action) {
      tokens.push({
        type: SelectorType.Attribute,
        name,
        action,
        value: getName(1),
        namespace: null,
        ignoreCase: "quirks"
      });
    }
    __name(addSpecialAttribute, "addSpecialAttribute");
    function finalizeSubselector() {
      if (tokens.length && tokens[tokens.length - 1].type === SelectorType.Descendant) {
        tokens.pop();
      }
      if (tokens.length === 0) {
        throw new Error("Empty sub-selector");
      }
      subselects.push(tokens);
    }
    __name(finalizeSubselector, "finalizeSubselector");
    stripWhitespace(0);
    if (selector.length === selectorIndex) {
      return selectorIndex;
    }
    loop: while (selectorIndex < selector.length) {
      const firstChar = selector.charCodeAt(selectorIndex);
      switch (firstChar) {
        // Whitespace
        case 32:
        case 9:
        case 10:
        case 12:
        case 13: {
          if (tokens.length === 0 || tokens[0].type !== SelectorType.Descendant) {
            ensureNotTraversal();
            tokens.push({ type: SelectorType.Descendant });
          }
          stripWhitespace(1);
          break;
        }
        // Traversals
        case 62: {
          addTraversal(SelectorType.Child);
          stripWhitespace(1);
          break;
        }
        case 60: {
          addTraversal(SelectorType.Parent);
          stripWhitespace(1);
          break;
        }
        case 126: {
          addTraversal(SelectorType.Sibling);
          stripWhitespace(1);
          break;
        }
        case 43: {
          addTraversal(SelectorType.Adjacent);
          stripWhitespace(1);
          break;
        }
        // Special attribute selectors: .class, #id
        case 46: {
          addSpecialAttribute("class", AttributeAction.Element);
          break;
        }
        case 35: {
          addSpecialAttribute("id", AttributeAction.Equals);
          break;
        }
        case 91: {
          stripWhitespace(1);
          let name;
          let namespace = null;
          if (selector.charCodeAt(selectorIndex) === 124) {
            name = getName(1);
          } else if (selector.startsWith("*|", selectorIndex)) {
            namespace = "*";
            name = getName(2);
          } else {
            name = getName(0);
            if (selector.charCodeAt(selectorIndex) === 124 && selector.charCodeAt(selectorIndex + 1) !== 61) {
              namespace = name;
              name = getName(1);
            }
          }
          stripWhitespace(0);
          let action = AttributeAction.Exists;
          const possibleAction = actionTypes.get(selector.charCodeAt(selectorIndex));
          if (possibleAction) {
            action = possibleAction;
            if (selector.charCodeAt(selectorIndex + 1) !== 61) {
              throw new Error("Expected `=`");
            }
            stripWhitespace(2);
          } else if (selector.charCodeAt(selectorIndex) === 61) {
            action = AttributeAction.Equals;
            stripWhitespace(1);
          }
          let value = "";
          let ignoreCase = null;
          if (action !== "exists") {
            if (isQuote(selector.charCodeAt(selectorIndex))) {
              const quote = selector.charCodeAt(selectorIndex);
              let sectionEnd = selectorIndex + 1;
              while (sectionEnd < selector.length && (selector.charCodeAt(sectionEnd) !== quote || isEscaped(sectionEnd))) {
                sectionEnd += 1;
              }
              if (selector.charCodeAt(sectionEnd) !== quote) {
                throw new Error("Attribute value didn't end");
              }
              value = unescapeCSS(selector.slice(selectorIndex + 1, sectionEnd));
              selectorIndex = sectionEnd + 1;
            } else {
              const valueStart = selectorIndex;
              while (selectorIndex < selector.length && (!isWhitespace(selector.charCodeAt(selectorIndex)) && selector.charCodeAt(selectorIndex) !== 93 || isEscaped(selectorIndex))) {
                selectorIndex += 1;
              }
              value = unescapeCSS(selector.slice(valueStart, selectorIndex));
            }
            stripWhitespace(0);
            const forceIgnore = selector.charCodeAt(selectorIndex) | 32;
            if (forceIgnore === 115) {
              ignoreCase = false;
              stripWhitespace(1);
            } else if (forceIgnore === 105) {
              ignoreCase = true;
              stripWhitespace(1);
            }
          }
          if (selector.charCodeAt(selectorIndex) !== 93) {
            throw new Error("Attribute selector didn't terminate");
          }
          selectorIndex += 1;
          const attributeSelector = {
            type: SelectorType.Attribute,
            name,
            action,
            value,
            namespace,
            ignoreCase
          };
          tokens.push(attributeSelector);
          break;
        }
        case 58: {
          if (selector.charCodeAt(selectorIndex + 1) === 58) {
            tokens.push({
              type: SelectorType.PseudoElement,
              name: getName(2).toLowerCase(),
              data: selector.charCodeAt(selectorIndex) === 40 ? readValueWithParenthesis() : null
            });
            continue;
          }
          const name = getName(1).toLowerCase();
          let data2 = null;
          if (selector.charCodeAt(selectorIndex) === 40) {
            if (unpackPseudos.has(name)) {
              if (isQuote(selector.charCodeAt(selectorIndex + 1))) {
                throw new Error(`Pseudo-selector ${name} cannot be quoted`);
              }
              data2 = [];
              selectorIndex = parseSelector(data2, selector, selectorIndex + 1);
              if (selector.charCodeAt(selectorIndex) !== 41) {
                throw new Error(`Missing closing parenthesis in :${name} (${selector})`);
              }
              selectorIndex += 1;
            } else {
              data2 = readValueWithParenthesis();
              if (stripQuotesFromPseudos.has(name)) {
                const quot = data2.charCodeAt(0);
                if (quot === data2.charCodeAt(data2.length - 1) && isQuote(quot)) {
                  data2 = data2.slice(1, -1);
                }
              }
              data2 = unescapeCSS(data2);
            }
          }
          tokens.push({ type: SelectorType.Pseudo, name, data: data2 });
          break;
        }
        case 44: {
          finalizeSubselector();
          tokens = [];
          stripWhitespace(1);
          break;
        }
        default: {
          if (selector.startsWith("/*", selectorIndex)) {
            const endIndex = selector.indexOf("*/", selectorIndex + 2);
            if (endIndex < 0) {
              throw new Error("Comment was not terminated");
            }
            selectorIndex = endIndex + 2;
            if (tokens.length === 0) {
              stripWhitespace(0);
            }
            break;
          }
          let namespace = null;
          let name;
          if (firstChar === 42) {
            selectorIndex += 1;
            name = "*";
          } else if (firstChar === 124) {
            name = "";
            if (selector.charCodeAt(selectorIndex + 1) === 124) {
              addTraversal(SelectorType.ColumnCombinator);
              stripWhitespace(2);
              break;
            }
          } else if (reName.test(selector.slice(selectorIndex))) {
            name = getName(0);
          } else {
            break loop;
          }
          if (selector.charCodeAt(selectorIndex) === 124 && selector.charCodeAt(selectorIndex + 1) !== 124) {
            namespace = name;
            if (selector.charCodeAt(selectorIndex + 1) === 42) {
              name = "*";
              selectorIndex += 2;
            } else {
              name = getName(1);
            }
          }
          tokens.push(name === "*" ? { type: SelectorType.Universal, namespace } : { type: SelectorType.Tag, name, namespace });
        }
      }
    }
    finalizeSubselector();
    return selectorIndex;
  }
  var reName, reEscape, actionTypes, unpackPseudos, stripQuotesFromPseudos;
  var init_parse = __esm({
    "node_modules/css-what/lib/es/parse.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_types();
      reName = /^[^\\#]?(?:\\(?:[\da-f]{1,6}\s?|.)|[\w\-\u00b0-\uFFFF])+/;
      reEscape = /\\([\da-f]{1,6}\s?|(\s)|.)/gi;
      actionTypes = /* @__PURE__ */ new Map([
        [126, AttributeAction.Element],
        [94, AttributeAction.Start],
        [36, AttributeAction.End],
        [42, AttributeAction.Any],
        [33, AttributeAction.Not],
        [124, AttributeAction.Hyphen]
      ]);
      unpackPseudos = /* @__PURE__ */ new Set([
        "has",
        "not",
        "matches",
        "is",
        "where",
        "host",
        "host-context"
      ]);
      __name(isTraversal, "isTraversal");
      stripQuotesFromPseudos = /* @__PURE__ */ new Set(["contains", "icontains"]);
      __name(funescape, "funescape");
      __name(unescapeCSS, "unescapeCSS");
      __name(isQuote, "isQuote");
      __name(isWhitespace, "isWhitespace");
      __name(parse, "parse");
      __name(parseSelector, "parseSelector");
    }
  });

  // node_modules/css-what/lib/es/stringify.js
  function stringify(selector) {
    return selector.map((token) => token.map(stringifyToken).join("")).join(", ");
  }
  function stringifyToken(token, index2, arr) {
    switch (token.type) {
      // Simple types
      case SelectorType.Child:
        return index2 === 0 ? "> " : " > ";
      case SelectorType.Parent:
        return index2 === 0 ? "< " : " < ";
      case SelectorType.Sibling:
        return index2 === 0 ? "~ " : " ~ ";
      case SelectorType.Adjacent:
        return index2 === 0 ? "+ " : " + ";
      case SelectorType.Descendant:
        return " ";
      case SelectorType.ColumnCombinator:
        return index2 === 0 ? "|| " : " || ";
      case SelectorType.Universal:
        return token.namespace === "*" && index2 + 1 < arr.length && "name" in arr[index2 + 1] ? "" : `${getNamespace(token.namespace)}*`;
      case SelectorType.Tag:
        return getNamespacedName(token);
      case SelectorType.PseudoElement:
        return `::${escapeName(token.name, charsToEscapeInName)}${token.data === null ? "" : `(${escapeName(token.data, charsToEscapeInPseudoValue)})`}`;
      case SelectorType.Pseudo:
        return `:${escapeName(token.name, charsToEscapeInName)}${token.data === null ? "" : `(${typeof token.data === "string" ? escapeName(token.data, charsToEscapeInPseudoValue) : stringify(token.data)})`}`;
      case SelectorType.Attribute: {
        if (token.name === "id" && token.action === AttributeAction.Equals && token.ignoreCase === "quirks" && !token.namespace) {
          return `#${escapeName(token.value, charsToEscapeInName)}`;
        }
        if (token.name === "class" && token.action === AttributeAction.Element && token.ignoreCase === "quirks" && !token.namespace) {
          return `.${escapeName(token.value, charsToEscapeInName)}`;
        }
        const name = getNamespacedName(token);
        if (token.action === AttributeAction.Exists) {
          return `[${name}]`;
        }
        return `[${name}${getActionValue(token.action)}="${escapeName(token.value, charsToEscapeInAttributeValue)}"${token.ignoreCase === null ? "" : token.ignoreCase ? " i" : " s"}]`;
      }
    }
  }
  function getActionValue(action) {
    switch (action) {
      case AttributeAction.Equals:
        return "";
      case AttributeAction.Element:
        return "~";
      case AttributeAction.Start:
        return "^";
      case AttributeAction.End:
        return "$";
      case AttributeAction.Any:
        return "*";
      case AttributeAction.Not:
        return "!";
      case AttributeAction.Hyphen:
        return "|";
      case AttributeAction.Exists:
        throw new Error("Shouldn't be here");
    }
  }
  function getNamespacedName(token) {
    return `${getNamespace(token.namespace)}${escapeName(token.name, charsToEscapeInName)}`;
  }
  function getNamespace(namespace) {
    return namespace !== null ? `${namespace === "*" ? "*" : escapeName(namespace, charsToEscapeInName)}|` : "";
  }
  function escapeName(str, charsToEscape) {
    let lastIdx = 0;
    let ret = "";
    for (let i = 0; i < str.length; i++) {
      if (charsToEscape.has(str.charCodeAt(i))) {
        ret += `${str.slice(lastIdx, i)}\\${str.charAt(i)}`;
        lastIdx = i + 1;
      }
    }
    return ret.length > 0 ? ret + str.slice(lastIdx) : str;
  }
  var attribValChars, pseudoValChars, charsToEscapeInAttributeValue, charsToEscapeInPseudoValue, charsToEscapeInName;
  var init_stringify = __esm({
    "node_modules/css-what/lib/es/stringify.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_types();
      attribValChars = ["\\", '"'];
      pseudoValChars = [...attribValChars, "(", ")"];
      charsToEscapeInAttributeValue = new Set(attribValChars.map((c) => c.charCodeAt(0)));
      charsToEscapeInPseudoValue = new Set(pseudoValChars.map((c) => c.charCodeAt(0)));
      charsToEscapeInName = new Set([
        ...pseudoValChars,
        "~",
        "^",
        "$",
        "*",
        "+",
        "!",
        "|",
        ":",
        "[",
        "]",
        " ",
        "."
      ].map((c) => c.charCodeAt(0)));
      __name(stringify, "stringify");
      __name(stringifyToken, "stringifyToken");
      __name(getActionValue, "getActionValue");
      __name(getNamespacedName, "getNamespacedName");
      __name(getNamespace, "getNamespace");
      __name(escapeName, "escapeName");
    }
  });

  // node_modules/css-what/lib/es/index.js
  var es_exports = {};
  __export(es_exports, {
    AttributeAction: () => AttributeAction,
    IgnoreCaseMode: () => IgnoreCaseMode,
    SelectorType: () => SelectorType,
    isTraversal: () => isTraversal,
    parse: () => parse,
    stringify: () => stringify
  });
  var init_es = __esm({
    "node_modules/css-what/lib/es/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_types();
      init_parse();
      init_stringify();
    }
  });

  // node_modules/boolbase/index.js
  var require_boolbase = __commonJS({
    "node_modules/boolbase/index.js"(exports4, module2) {
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = {
        trueFunc: /* @__PURE__ */ __name(function trueFunc() {
          return true;
        }, "trueFunc"),
        falseFunc: /* @__PURE__ */ __name(function falseFunc() {
          return false;
        }, "falseFunc")
      };
    }
  });

  // node_modules/css-select/lib/sort.js
  var require_sort = __commonJS({
    "node_modules/css-select/lib/sort.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.isTraversal = void 0;
      var css_what_1 = (init_es(), __toCommonJS(es_exports));
      var procedure = /* @__PURE__ */ new Map([
        [css_what_1.SelectorType.Universal, 50],
        [css_what_1.SelectorType.Tag, 30],
        [css_what_1.SelectorType.Attribute, 1],
        [css_what_1.SelectorType.Pseudo, 0]
      ]);
      function isTraversal2(token) {
        return !procedure.has(token.type);
      }
      __name(isTraversal2, "isTraversal");
      exports4.isTraversal = isTraversal2;
      var attributes = /* @__PURE__ */ new Map([
        [css_what_1.AttributeAction.Exists, 10],
        [css_what_1.AttributeAction.Equals, 8],
        [css_what_1.AttributeAction.Not, 7],
        [css_what_1.AttributeAction.Start, 6],
        [css_what_1.AttributeAction.End, 6],
        [css_what_1.AttributeAction.Any, 5]
      ]);
      function sortByProcedure(arr) {
        var procs = arr.map(getProcedure);
        for (var i = 1; i < arr.length; i++) {
          var procNew = procs[i];
          if (procNew < 0)
            continue;
          for (var j = i - 1; j >= 0 && procNew < procs[j]; j--) {
            var token = arr[j + 1];
            arr[j + 1] = arr[j];
            arr[j] = token;
            procs[j + 1] = procs[j];
            procs[j] = procNew;
          }
        }
      }
      __name(sortByProcedure, "sortByProcedure");
      exports4.default = sortByProcedure;
      function getProcedure(token) {
        var _a, _b;
        var proc = (_a = procedure.get(token.type)) !== null && _a !== void 0 ? _a : -1;
        if (token.type === css_what_1.SelectorType.Attribute) {
          proc = (_b = attributes.get(token.action)) !== null && _b !== void 0 ? _b : 4;
          if (token.action === css_what_1.AttributeAction.Equals && token.name === "id") {
            proc = 9;
          }
          if (token.ignoreCase) {
            proc >>= 1;
          }
        } else if (token.type === css_what_1.SelectorType.Pseudo) {
          if (!token.data) {
            proc = 3;
          } else if (token.name === "has" || token.name === "contains") {
            proc = 0;
          } else if (Array.isArray(token.data)) {
            proc = Math.min.apply(Math, token.data.map(function(d) {
              return Math.min.apply(Math, d.map(getProcedure));
            }));
            if (proc < 0) {
              proc = 0;
            }
          } else {
            proc = 2;
          }
        }
        return proc;
      }
      __name(getProcedure, "getProcedure");
    }
  });

  // node_modules/css-select/lib/attributes.js
  var require_attributes = __commonJS({
    "node_modules/css-select/lib/attributes.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.attributeRules = void 0;
      var boolbase_1 = __importDefault(require_boolbase());
      var reChars = /[-[\]{}()*+?.,\\^$|#\s]/g;
      function escapeRegex(value) {
        return value.replace(reChars, "\\$&");
      }
      __name(escapeRegex, "escapeRegex");
      var caseInsensitiveAttributes = /* @__PURE__ */ new Set([
        "accept",
        "accept-charset",
        "align",
        "alink",
        "axis",
        "bgcolor",
        "charset",
        "checked",
        "clear",
        "codetype",
        "color",
        "compact",
        "declare",
        "defer",
        "dir",
        "direction",
        "disabled",
        "enctype",
        "face",
        "frame",
        "hreflang",
        "http-equiv",
        "lang",
        "language",
        "link",
        "media",
        "method",
        "multiple",
        "nohref",
        "noresize",
        "noshade",
        "nowrap",
        "readonly",
        "rel",
        "rev",
        "rules",
        "scope",
        "scrolling",
        "selected",
        "shape",
        "target",
        "text",
        "type",
        "valign",
        "valuetype",
        "vlink"
      ]);
      function shouldIgnoreCase(selector, options) {
        return typeof selector.ignoreCase === "boolean" ? selector.ignoreCase : selector.ignoreCase === "quirks" ? !!options.quirksMode : !options.xmlMode && caseInsensitiveAttributes.has(selector.name);
      }
      __name(shouldIgnoreCase, "shouldIgnoreCase");
      exports4.attributeRules = {
        equals: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name;
          var value = data2.value;
          if (shouldIgnoreCase(data2, options)) {
            value = value.toLowerCase();
            return function(elem) {
              var attr2 = adapter2.getAttributeValue(elem, name);
              return attr2 != null && attr2.length === value.length && attr2.toLowerCase() === value && next2(elem);
            };
          }
          return function(elem) {
            return adapter2.getAttributeValue(elem, name) === value && next2(elem);
          };
        }, "equals"),
        hyphen: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name;
          var value = data2.value;
          var len = value.length;
          if (shouldIgnoreCase(data2, options)) {
            value = value.toLowerCase();
            return /* @__PURE__ */ __name(function hyphenIC(elem) {
              var attr2 = adapter2.getAttributeValue(elem, name);
              return attr2 != null && (attr2.length === len || attr2.charAt(len) === "-") && attr2.substr(0, len).toLowerCase() === value && next2(elem);
            }, "hyphenIC");
          }
          return /* @__PURE__ */ __name(function hyphen(elem) {
            var attr2 = adapter2.getAttributeValue(elem, name);
            return attr2 != null && (attr2.length === len || attr2.charAt(len) === "-") && attr2.substr(0, len) === value && next2(elem);
          }, "hyphen");
        }, "hyphen"),
        element: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name, value = data2.value;
          if (/\s/.test(value)) {
            return boolbase_1.default.falseFunc;
          }
          var regex = new RegExp("(?:^|\\s)".concat(escapeRegex(value), "(?:$|\\s)"), shouldIgnoreCase(data2, options) ? "i" : "");
          return /* @__PURE__ */ __name(function element(elem) {
            var attr2 = adapter2.getAttributeValue(elem, name);
            return attr2 != null && attr2.length >= value.length && regex.test(attr2) && next2(elem);
          }, "element");
        }, "element"),
        exists: /* @__PURE__ */ __name(function(next2, _a, _b) {
          var name = _a.name;
          var adapter2 = _b.adapter;
          return function(elem) {
            return adapter2.hasAttrib(elem, name) && next2(elem);
          };
        }, "exists"),
        start: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name;
          var value = data2.value;
          var len = value.length;
          if (len === 0) {
            return boolbase_1.default.falseFunc;
          }
          if (shouldIgnoreCase(data2, options)) {
            value = value.toLowerCase();
            return function(elem) {
              var attr2 = adapter2.getAttributeValue(elem, name);
              return attr2 != null && attr2.length >= len && attr2.substr(0, len).toLowerCase() === value && next2(elem);
            };
          }
          return function(elem) {
            var _a;
            return !!((_a = adapter2.getAttributeValue(elem, name)) === null || _a === void 0 ? void 0 : _a.startsWith(value)) && next2(elem);
          };
        }, "start"),
        end: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name;
          var value = data2.value;
          var len = -value.length;
          if (len === 0) {
            return boolbase_1.default.falseFunc;
          }
          if (shouldIgnoreCase(data2, options)) {
            value = value.toLowerCase();
            return function(elem) {
              var _a;
              return ((_a = adapter2.getAttributeValue(elem, name)) === null || _a === void 0 ? void 0 : _a.substr(len).toLowerCase()) === value && next2(elem);
            };
          }
          return function(elem) {
            var _a;
            return !!((_a = adapter2.getAttributeValue(elem, name)) === null || _a === void 0 ? void 0 : _a.endsWith(value)) && next2(elem);
          };
        }, "end"),
        any: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name, value = data2.value;
          if (value === "") {
            return boolbase_1.default.falseFunc;
          }
          if (shouldIgnoreCase(data2, options)) {
            var regex_1 = new RegExp(escapeRegex(value), "i");
            return /* @__PURE__ */ __name(function anyIC(elem) {
              var attr2 = adapter2.getAttributeValue(elem, name);
              return attr2 != null && attr2.length >= value.length && regex_1.test(attr2) && next2(elem);
            }, "anyIC");
          }
          return function(elem) {
            var _a;
            return !!((_a = adapter2.getAttributeValue(elem, name)) === null || _a === void 0 ? void 0 : _a.includes(value)) && next2(elem);
          };
        }, "any"),
        not: /* @__PURE__ */ __name(function(next2, data2, options) {
          var adapter2 = options.adapter;
          var name = data2.name;
          var value = data2.value;
          if (value === "") {
            return function(elem) {
              return !!adapter2.getAttributeValue(elem, name) && next2(elem);
            };
          } else if (shouldIgnoreCase(data2, options)) {
            value = value.toLowerCase();
            return function(elem) {
              var attr2 = adapter2.getAttributeValue(elem, name);
              return (attr2 == null || attr2.length !== value.length || attr2.toLowerCase() !== value) && next2(elem);
            };
          }
          return function(elem) {
            return adapter2.getAttributeValue(elem, name) !== value && next2(elem);
          };
        }, "not")
      };
    }
  });

  // node_modules/nth-check/lib/parse.js
  var require_parse = __commonJS({
    "node_modules/nth-check/lib/parse.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.parse = void 0;
      var whitespace = /* @__PURE__ */ new Set([9, 10, 12, 13, 32]);
      var ZERO = "0".charCodeAt(0);
      var NINE = "9".charCodeAt(0);
      function parse5(formula) {
        formula = formula.trim().toLowerCase();
        if (formula === "even") {
          return [2, 0];
        } else if (formula === "odd") {
          return [2, 1];
        }
        var idx = 0;
        var a2 = 0;
        var sign = readSign();
        var number = readNumber();
        if (idx < formula.length && formula.charAt(idx) === "n") {
          idx++;
          a2 = sign * (number !== null && number !== void 0 ? number : 1);
          skipWhitespace();
          if (idx < formula.length) {
            sign = readSign();
            skipWhitespace();
            number = readNumber();
          } else {
            sign = number = 0;
          }
        }
        if (number === null || idx < formula.length) {
          throw new Error("n-th rule couldn't be parsed ('".concat(formula, "')"));
        }
        return [a2, sign * number];
        function readSign() {
          if (formula.charAt(idx) === "-") {
            idx++;
            return -1;
          }
          if (formula.charAt(idx) === "+") {
            idx++;
          }
          return 1;
        }
        __name(readSign, "readSign");
        function readNumber() {
          var start = idx;
          var value = 0;
          while (idx < formula.length && formula.charCodeAt(idx) >= ZERO && formula.charCodeAt(idx) <= NINE) {
            value = value * 10 + (formula.charCodeAt(idx) - ZERO);
            idx++;
          }
          return idx === start ? null : value;
        }
        __name(readNumber, "readNumber");
        function skipWhitespace() {
          while (idx < formula.length && whitespace.has(formula.charCodeAt(idx))) {
            idx++;
          }
        }
        __name(skipWhitespace, "skipWhitespace");
      }
      __name(parse5, "parse");
      exports4.parse = parse5;
    }
  });

  // node_modules/nth-check/lib/compile.js
  var require_compile = __commonJS({
    "node_modules/nth-check/lib/compile.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.generate = exports4.compile = void 0;
      var boolbase_1 = __importDefault(require_boolbase());
      function compile(parsed) {
        var a2 = parsed[0];
        var b = parsed[1] - 1;
        if (b < 0 && a2 <= 0)
          return boolbase_1.default.falseFunc;
        if (a2 === -1)
          return function(index2) {
            return index2 <= b;
          };
        if (a2 === 0)
          return function(index2) {
            return index2 === b;
          };
        if (a2 === 1)
          return b < 0 ? boolbase_1.default.trueFunc : function(index2) {
            return index2 >= b;
          };
        var absA = Math.abs(a2);
        var bMod = (b % absA + absA) % absA;
        return a2 > 1 ? function(index2) {
          return index2 >= b && index2 % absA === bMod;
        } : function(index2) {
          return index2 <= b && index2 % absA === bMod;
        };
      }
      __name(compile, "compile");
      exports4.compile = compile;
      function generate(parsed) {
        var a2 = parsed[0];
        var b = parsed[1] - 1;
        var n2 = 0;
        if (a2 < 0) {
          var aPos_1 = -a2;
          var minValue_1 = (b % aPos_1 + aPos_1) % aPos_1;
          return function() {
            var val2 = minValue_1 + aPos_1 * n2++;
            return val2 > b ? null : val2;
          };
        }
        if (a2 === 0)
          return b < 0 ? (
            // There are no result — always return `null`
            function() {
              return null;
            }
          ) : (
            // Return `b` exactly once
            function() {
              return n2++ === 0 ? b : null;
            }
          );
        if (b < 0) {
          b += a2 * Math.ceil(-b / a2);
        }
        return function() {
          return a2 * n2++ + b;
        };
      }
      __name(generate, "generate");
      exports4.generate = generate;
    }
  });

  // node_modules/nth-check/lib/index.js
  var require_lib6 = __commonJS({
    "node_modules/nth-check/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.sequence = exports4.generate = exports4.compile = exports4.parse = void 0;
      var parse_js_1 = require_parse();
      Object.defineProperty(exports4, "parse", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return parse_js_1.parse;
      }, "get") });
      var compile_js_1 = require_compile();
      Object.defineProperty(exports4, "compile", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return compile_js_1.compile;
      }, "get") });
      Object.defineProperty(exports4, "generate", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return compile_js_1.generate;
      }, "get") });
      function nthCheck(formula) {
        return (0, compile_js_1.compile)((0, parse_js_1.parse)(formula));
      }
      __name(nthCheck, "nthCheck");
      exports4.default = nthCheck;
      function sequence(formula) {
        return (0, compile_js_1.generate)((0, parse_js_1.parse)(formula));
      }
      __name(sequence, "sequence");
      exports4.sequence = sequence;
    }
  });

  // node_modules/css-select/lib/pseudo-selectors/filters.js
  var require_filters = __commonJS({
    "node_modules/css-select/lib/pseudo-selectors/filters.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.filters = void 0;
      var nth_check_1 = __importDefault(require_lib6());
      var boolbase_1 = __importDefault(require_boolbase());
      function getChildFunc(next2, adapter2) {
        return function(elem) {
          var parent2 = adapter2.getParent(elem);
          return parent2 != null && adapter2.isTag(parent2) && next2(elem);
        };
      }
      __name(getChildFunc, "getChildFunc");
      exports4.filters = {
        contains: /* @__PURE__ */ __name(function(next2, text3, _a) {
          var adapter2 = _a.adapter;
          return /* @__PURE__ */ __name(function contains2(elem) {
            return next2(elem) && adapter2.getText(elem).includes(text3);
          }, "contains");
        }, "contains"),
        icontains: /* @__PURE__ */ __name(function(next2, text3, _a) {
          var adapter2 = _a.adapter;
          var itext = text3.toLowerCase();
          return /* @__PURE__ */ __name(function icontains(elem) {
            return next2(elem) && adapter2.getText(elem).toLowerCase().includes(itext);
          }, "icontains");
        }, "icontains"),
        // Location specific methods
        "nth-child": /* @__PURE__ */ __name(function(next2, rule, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var func = (0, nth_check_1.default)(rule);
          if (func === boolbase_1.default.falseFunc)
            return boolbase_1.default.falseFunc;
          if (func === boolbase_1.default.trueFunc)
            return getChildFunc(next2, adapter2);
          return /* @__PURE__ */ __name(function nthChild(elem) {
            var siblings2 = adapter2.getSiblings(elem);
            var pos = 0;
            for (var i = 0; i < siblings2.length; i++) {
              if (equals(elem, siblings2[i]))
                break;
              if (adapter2.isTag(siblings2[i])) {
                pos++;
              }
            }
            return func(pos) && next2(elem);
          }, "nthChild");
        }, "nth-child"),
        "nth-last-child": /* @__PURE__ */ __name(function(next2, rule, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var func = (0, nth_check_1.default)(rule);
          if (func === boolbase_1.default.falseFunc)
            return boolbase_1.default.falseFunc;
          if (func === boolbase_1.default.trueFunc)
            return getChildFunc(next2, adapter2);
          return /* @__PURE__ */ __name(function nthLastChild(elem) {
            var siblings2 = adapter2.getSiblings(elem);
            var pos = 0;
            for (var i = siblings2.length - 1; i >= 0; i--) {
              if (equals(elem, siblings2[i]))
                break;
              if (adapter2.isTag(siblings2[i])) {
                pos++;
              }
            }
            return func(pos) && next2(elem);
          }, "nthLastChild");
        }, "nth-last-child"),
        "nth-of-type": /* @__PURE__ */ __name(function(next2, rule, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var func = (0, nth_check_1.default)(rule);
          if (func === boolbase_1.default.falseFunc)
            return boolbase_1.default.falseFunc;
          if (func === boolbase_1.default.trueFunc)
            return getChildFunc(next2, adapter2);
          return /* @__PURE__ */ __name(function nthOfType(elem) {
            var siblings2 = adapter2.getSiblings(elem);
            var pos = 0;
            for (var i = 0; i < siblings2.length; i++) {
              var currentSibling = siblings2[i];
              if (equals(elem, currentSibling))
                break;
              if (adapter2.isTag(currentSibling) && adapter2.getName(currentSibling) === adapter2.getName(elem)) {
                pos++;
              }
            }
            return func(pos) && next2(elem);
          }, "nthOfType");
        }, "nth-of-type"),
        "nth-last-of-type": /* @__PURE__ */ __name(function(next2, rule, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var func = (0, nth_check_1.default)(rule);
          if (func === boolbase_1.default.falseFunc)
            return boolbase_1.default.falseFunc;
          if (func === boolbase_1.default.trueFunc)
            return getChildFunc(next2, adapter2);
          return /* @__PURE__ */ __name(function nthLastOfType(elem) {
            var siblings2 = adapter2.getSiblings(elem);
            var pos = 0;
            for (var i = siblings2.length - 1; i >= 0; i--) {
              var currentSibling = siblings2[i];
              if (equals(elem, currentSibling))
                break;
              if (adapter2.isTag(currentSibling) && adapter2.getName(currentSibling) === adapter2.getName(elem)) {
                pos++;
              }
            }
            return func(pos) && next2(elem);
          }, "nthLastOfType");
        }, "nth-last-of-type"),
        // TODO determine the actual root element
        root: /* @__PURE__ */ __name(function(next2, _rule, _a) {
          var adapter2 = _a.adapter;
          return function(elem) {
            var parent2 = adapter2.getParent(elem);
            return (parent2 == null || !adapter2.isTag(parent2)) && next2(elem);
          };
        }, "root"),
        scope: /* @__PURE__ */ __name(function(next2, rule, options, context) {
          var equals = options.equals;
          if (!context || context.length === 0) {
            return exports4.filters["root"](next2, rule, options);
          }
          if (context.length === 1) {
            return function(elem) {
              return equals(context[0], elem) && next2(elem);
            };
          }
          return function(elem) {
            return context.includes(elem) && next2(elem);
          };
        }, "scope"),
        hover: dynamicStatePseudo("isHovered"),
        visited: dynamicStatePseudo("isVisited"),
        active: dynamicStatePseudo("isActive")
      };
      function dynamicStatePseudo(name) {
        return /* @__PURE__ */ __name(function dynamicPseudo(next2, _rule, _a) {
          var adapter2 = _a.adapter;
          var func = adapter2[name];
          if (typeof func !== "function") {
            return boolbase_1.default.falseFunc;
          }
          return /* @__PURE__ */ __name(function active(elem) {
            return func(elem) && next2(elem);
          }, "active");
        }, "dynamicPseudo");
      }
      __name(dynamicStatePseudo, "dynamicStatePseudo");
    }
  });

  // node_modules/css-select/lib/pseudo-selectors/pseudos.js
  var require_pseudos = __commonJS({
    "node_modules/css-select/lib/pseudo-selectors/pseudos.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.verifyPseudoArgs = exports4.pseudos = void 0;
      exports4.pseudos = {
        empty: /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter;
          return !adapter2.getChildren(elem).some(function(elem2) {
            return adapter2.isTag(elem2) || adapter2.getText(elem2) !== "";
          });
        }, "empty"),
        "first-child": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          if (adapter2.prevElementSibling) {
            return adapter2.prevElementSibling(elem) == null;
          }
          var firstChild = adapter2.getSiblings(elem).find(function(elem2) {
            return adapter2.isTag(elem2);
          });
          return firstChild != null && equals(elem, firstChild);
        }, "first-child"),
        "last-child": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var siblings2 = adapter2.getSiblings(elem);
          for (var i = siblings2.length - 1; i >= 0; i--) {
            if (equals(elem, siblings2[i]))
              return true;
            if (adapter2.isTag(siblings2[i]))
              break;
          }
          return false;
        }, "last-child"),
        "first-of-type": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var siblings2 = adapter2.getSiblings(elem);
          var elemName = adapter2.getName(elem);
          for (var i = 0; i < siblings2.length; i++) {
            var currentSibling = siblings2[i];
            if (equals(elem, currentSibling))
              return true;
            if (adapter2.isTag(currentSibling) && adapter2.getName(currentSibling) === elemName) {
              break;
            }
          }
          return false;
        }, "first-of-type"),
        "last-of-type": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var siblings2 = adapter2.getSiblings(elem);
          var elemName = adapter2.getName(elem);
          for (var i = siblings2.length - 1; i >= 0; i--) {
            var currentSibling = siblings2[i];
            if (equals(elem, currentSibling))
              return true;
            if (adapter2.isTag(currentSibling) && adapter2.getName(currentSibling) === elemName) {
              break;
            }
          }
          return false;
        }, "last-of-type"),
        "only-of-type": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          var elemName = adapter2.getName(elem);
          return adapter2.getSiblings(elem).every(function(sibling) {
            return equals(elem, sibling) || !adapter2.isTag(sibling) || adapter2.getName(sibling) !== elemName;
          });
        }, "only-of-type"),
        "only-child": /* @__PURE__ */ __name(function(elem, _a) {
          var adapter2 = _a.adapter, equals = _a.equals;
          return adapter2.getSiblings(elem).every(function(sibling) {
            return equals(elem, sibling) || !adapter2.isTag(sibling);
          });
        }, "only-child")
      };
      function verifyPseudoArgs(func, name, subselect, argIndex) {
        if (subselect === null) {
          if (func.length > argIndex) {
            throw new Error("Pseudo-class :".concat(name, " requires an argument"));
          }
        } else if (func.length === argIndex) {
          throw new Error("Pseudo-class :".concat(name, " doesn't have any arguments"));
        }
      }
      __name(verifyPseudoArgs, "verifyPseudoArgs");
      exports4.verifyPseudoArgs = verifyPseudoArgs;
    }
  });

  // node_modules/css-select/lib/pseudo-selectors/aliases.js
  var require_aliases = __commonJS({
    "node_modules/css-select/lib/pseudo-selectors/aliases.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.aliases = void 0;
      exports4.aliases = {
        // Links
        "any-link": ":is(a, area, link)[href]",
        link: ":any-link:not(:visited)",
        // Forms
        // https://html.spec.whatwg.org/multipage/scripting.html#disabled-elements
        disabled: ":is(\n        :is(button, input, select, textarea, optgroup, option)[disabled],\n        optgroup[disabled] > option,\n        fieldset[disabled]:not(fieldset[disabled] legend:first-of-type *)\n    )",
        enabled: ":not(:disabled)",
        checked: ":is(:is(input[type=radio], input[type=checkbox])[checked], option:selected)",
        required: ":is(input, select, textarea)[required]",
        optional: ":is(input, select, textarea):not([required])",
        // JQuery extensions
        // https://html.spec.whatwg.org/multipage/form-elements.html#concept-option-selectedness
        selected: "option:is([selected], select:not([multiple]):not(:has(> option[selected])) > :first-of-type)",
        checkbox: "[type=checkbox]",
        file: "[type=file]",
        password: "[type=password]",
        radio: "[type=radio]",
        reset: "[type=reset]",
        image: "[type=image]",
        submit: "[type=submit]",
        parent: ":not(:empty)",
        header: ":is(h1, h2, h3, h4, h5, h6)",
        button: ":is(button, input[type=button])",
        input: ":is(input, textarea, select, button)",
        text: "input:is(:not([type!='']), [type=text])"
      };
    }
  });

  // node_modules/css-select/lib/pseudo-selectors/subselects.js
  var require_subselects = __commonJS({
    "node_modules/css-select/lib/pseudo-selectors/subselects.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __spreadArray = exports4 && exports4.__spreadArray || function(to, from, pack) {
        if (pack || arguments.length === 2) for (var i = 0, l2 = from.length, ar; i < l2; i++) {
          if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
          }
        }
        return to.concat(ar || Array.prototype.slice.call(from));
      };
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.subselects = exports4.getNextSiblings = exports4.ensureIsTag = exports4.PLACEHOLDER_ELEMENT = void 0;
      var boolbase_1 = __importDefault(require_boolbase());
      var sort_js_1 = require_sort();
      exports4.PLACEHOLDER_ELEMENT = {};
      function ensureIsTag(next2, adapter2) {
        if (next2 === boolbase_1.default.falseFunc)
          return boolbase_1.default.falseFunc;
        return function(elem) {
          return adapter2.isTag(elem) && next2(elem);
        };
      }
      __name(ensureIsTag, "ensureIsTag");
      exports4.ensureIsTag = ensureIsTag;
      function getNextSiblings(elem, adapter2) {
        var siblings2 = adapter2.getSiblings(elem);
        if (siblings2.length <= 1)
          return [];
        var elemIndex = siblings2.indexOf(elem);
        if (elemIndex < 0 || elemIndex === siblings2.length - 1)
          return [];
        return siblings2.slice(elemIndex + 1).filter(adapter2.isTag);
      }
      __name(getNextSiblings, "getNextSiblings");
      exports4.getNextSiblings = getNextSiblings;
      function copyOptions(options) {
        return {
          xmlMode: !!options.xmlMode,
          lowerCaseAttributeNames: !!options.lowerCaseAttributeNames,
          lowerCaseTags: !!options.lowerCaseTags,
          quirksMode: !!options.quirksMode,
          cacheResults: !!options.cacheResults,
          pseudos: options.pseudos,
          adapter: options.adapter,
          equals: options.equals
        };
      }
      __name(copyOptions, "copyOptions");
      var is3 = /* @__PURE__ */ __name(function(next2, token, options, context, compileToken) {
        var func = compileToken(token, copyOptions(options), context);
        return func === boolbase_1.default.trueFunc ? next2 : func === boolbase_1.default.falseFunc ? boolbase_1.default.falseFunc : function(elem) {
          return func(elem) && next2(elem);
        };
      }, "is");
      exports4.subselects = {
        is: is3,
        /**
         * `:matches` and `:where` are aliases for `:is`.
         */
        matches: is3,
        where: is3,
        not: /* @__PURE__ */ __name(function(next2, token, options, context, compileToken) {
          var func = compileToken(token, copyOptions(options), context);
          return func === boolbase_1.default.falseFunc ? next2 : func === boolbase_1.default.trueFunc ? boolbase_1.default.falseFunc : function(elem) {
            return !func(elem) && next2(elem);
          };
        }, "not"),
        has: /* @__PURE__ */ __name(function(next2, subselect, options, _context, compileToken) {
          var adapter2 = options.adapter;
          var opts = copyOptions(options);
          opts.relativeSelector = true;
          var context = subselect.some(function(s) {
            return s.some(sort_js_1.isTraversal);
          }) ? (
            // Used as a placeholder. Will be replaced with the actual element.
            [exports4.PLACEHOLDER_ELEMENT]
          ) : void 0;
          var compiled = compileToken(subselect, opts, context);
          if (compiled === boolbase_1.default.falseFunc)
            return boolbase_1.default.falseFunc;
          var hasElement = ensureIsTag(compiled, adapter2);
          if (context && compiled !== boolbase_1.default.trueFunc) {
            var _a = compiled.shouldTestNextSiblings, shouldTestNextSiblings_1 = _a === void 0 ? false : _a;
            return function(elem) {
              if (!next2(elem))
                return false;
              context[0] = elem;
              var childs = adapter2.getChildren(elem);
              var nextElements = shouldTestNextSiblings_1 ? __spreadArray(__spreadArray([], childs, true), getNextSiblings(elem, adapter2), true) : childs;
              return adapter2.existsOne(hasElement, nextElements);
            };
          }
          return function(elem) {
            return next2(elem) && adapter2.existsOne(hasElement, adapter2.getChildren(elem));
          };
        }, "has")
      };
    }
  });

  // node_modules/css-select/lib/pseudo-selectors/index.js
  var require_pseudo_selectors = __commonJS({
    "node_modules/css-select/lib/pseudo-selectors/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.compilePseudoSelector = exports4.aliases = exports4.pseudos = exports4.filters = void 0;
      var css_what_1 = (init_es(), __toCommonJS(es_exports));
      var filters_js_1 = require_filters();
      Object.defineProperty(exports4, "filters", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return filters_js_1.filters;
      }, "get") });
      var pseudos_js_1 = require_pseudos();
      Object.defineProperty(exports4, "pseudos", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return pseudos_js_1.pseudos;
      }, "get") });
      var aliases_js_1 = require_aliases();
      Object.defineProperty(exports4, "aliases", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return aliases_js_1.aliases;
      }, "get") });
      var subselects_js_1 = require_subselects();
      function compilePseudoSelector(next2, selector, options, context, compileToken) {
        var _a;
        var name = selector.name, data2 = selector.data;
        if (Array.isArray(data2)) {
          if (!(name in subselects_js_1.subselects)) {
            throw new Error("Unknown pseudo-class :".concat(name, "(").concat(data2, ")"));
          }
          return subselects_js_1.subselects[name](next2, data2, options, context, compileToken);
        }
        var userPseudo = (_a = options.pseudos) === null || _a === void 0 ? void 0 : _a[name];
        var stringPseudo = typeof userPseudo === "string" ? userPseudo : aliases_js_1.aliases[name];
        if (typeof stringPseudo === "string") {
          if (data2 != null) {
            throw new Error("Pseudo ".concat(name, " doesn't have any arguments"));
          }
          var alias = (0, css_what_1.parse)(stringPseudo);
          return subselects_js_1.subselects["is"](next2, alias, options, context, compileToken);
        }
        if (typeof userPseudo === "function") {
          (0, pseudos_js_1.verifyPseudoArgs)(userPseudo, name, data2, 1);
          return function(elem) {
            return userPseudo(elem, data2) && next2(elem);
          };
        }
        if (name in filters_js_1.filters) {
          return filters_js_1.filters[name](next2, data2, options, context);
        }
        if (name in pseudos_js_1.pseudos) {
          var pseudo_1 = pseudos_js_1.pseudos[name];
          (0, pseudos_js_1.verifyPseudoArgs)(pseudo_1, name, data2, 2);
          return function(elem) {
            return pseudo_1(elem, options, data2) && next2(elem);
          };
        }
        throw new Error("Unknown pseudo-class :".concat(name));
      }
      __name(compilePseudoSelector, "compilePseudoSelector");
      exports4.compilePseudoSelector = compilePseudoSelector;
    }
  });

  // node_modules/css-select/lib/general.js
  var require_general = __commonJS({
    "node_modules/css-select/lib/general.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.compileGeneralSelector = void 0;
      var attributes_js_1 = require_attributes();
      var index_js_1 = require_pseudo_selectors();
      var css_what_1 = (init_es(), __toCommonJS(es_exports));
      function getElementParent(node, adapter2) {
        var parent2 = adapter2.getParent(node);
        if (parent2 && adapter2.isTag(parent2)) {
          return parent2;
        }
        return null;
      }
      __name(getElementParent, "getElementParent");
      function compileGeneralSelector(next2, selector, options, context, compileToken) {
        var adapter2 = options.adapter, equals = options.equals;
        switch (selector.type) {
          case css_what_1.SelectorType.PseudoElement: {
            throw new Error("Pseudo-elements are not supported by css-select");
          }
          case css_what_1.SelectorType.ColumnCombinator: {
            throw new Error("Column combinators are not yet supported by css-select");
          }
          case css_what_1.SelectorType.Attribute: {
            if (selector.namespace != null) {
              throw new Error("Namespaced attributes are not yet supported by css-select");
            }
            if (!options.xmlMode || options.lowerCaseAttributeNames) {
              selector.name = selector.name.toLowerCase();
            }
            return attributes_js_1.attributeRules[selector.action](next2, selector, options);
          }
          case css_what_1.SelectorType.Pseudo: {
            return (0, index_js_1.compilePseudoSelector)(next2, selector, options, context, compileToken);
          }
          // Tags
          case css_what_1.SelectorType.Tag: {
            if (selector.namespace != null) {
              throw new Error("Namespaced tag names are not yet supported by css-select");
            }
            var name_1 = selector.name;
            if (!options.xmlMode || options.lowerCaseTags) {
              name_1 = name_1.toLowerCase();
            }
            return /* @__PURE__ */ __name(function tag(elem) {
              return adapter2.getName(elem) === name_1 && next2(elem);
            }, "tag");
          }
          // Traversal
          case css_what_1.SelectorType.Descendant: {
            if (options.cacheResults === false || typeof WeakSet === "undefined") {
              return /* @__PURE__ */ __name(function descendant(elem) {
                var current = elem;
                while (current = getElementParent(current, adapter2)) {
                  if (next2(current)) {
                    return true;
                  }
                }
                return false;
              }, "descendant");
            }
            var isFalseCache_1 = /* @__PURE__ */ new WeakSet();
            return /* @__PURE__ */ __name(function cachedDescendant(elem) {
              var current = elem;
              while (current = getElementParent(current, adapter2)) {
                if (!isFalseCache_1.has(current)) {
                  if (adapter2.isTag(current) && next2(current)) {
                    return true;
                  }
                  isFalseCache_1.add(current);
                }
              }
              return false;
            }, "cachedDescendant");
          }
          case "_flexibleDescendant": {
            return /* @__PURE__ */ __name(function flexibleDescendant(elem) {
              var current = elem;
              do {
                if (next2(current))
                  return true;
              } while (current = getElementParent(current, adapter2));
              return false;
            }, "flexibleDescendant");
          }
          case css_what_1.SelectorType.Parent: {
            return /* @__PURE__ */ __name(function parent2(elem) {
              return adapter2.getChildren(elem).some(function(elem2) {
                return adapter2.isTag(elem2) && next2(elem2);
              });
            }, "parent");
          }
          case css_what_1.SelectorType.Child: {
            return /* @__PURE__ */ __name(function child(elem) {
              var parent2 = adapter2.getParent(elem);
              return parent2 != null && adapter2.isTag(parent2) && next2(parent2);
            }, "child");
          }
          case css_what_1.SelectorType.Sibling: {
            return /* @__PURE__ */ __name(function sibling(elem) {
              var siblings2 = adapter2.getSiblings(elem);
              for (var i = 0; i < siblings2.length; i++) {
                var currentSibling = siblings2[i];
                if (equals(elem, currentSibling))
                  break;
                if (adapter2.isTag(currentSibling) && next2(currentSibling)) {
                  return true;
                }
              }
              return false;
            }, "sibling");
          }
          case css_what_1.SelectorType.Adjacent: {
            if (adapter2.prevElementSibling) {
              return /* @__PURE__ */ __name(function adjacent(elem) {
                var previous = adapter2.prevElementSibling(elem);
                return previous != null && next2(previous);
              }, "adjacent");
            }
            return /* @__PURE__ */ __name(function adjacent(elem) {
              var siblings2 = adapter2.getSiblings(elem);
              var lastElement;
              for (var i = 0; i < siblings2.length; i++) {
                var currentSibling = siblings2[i];
                if (equals(elem, currentSibling))
                  break;
                if (adapter2.isTag(currentSibling)) {
                  lastElement = currentSibling;
                }
              }
              return !!lastElement && next2(lastElement);
            }, "adjacent");
          }
          case css_what_1.SelectorType.Universal: {
            if (selector.namespace != null && selector.namespace !== "*") {
              throw new Error("Namespaced universal selectors are not yet supported by css-select");
            }
            return next2;
          }
        }
      }
      __name(compileGeneralSelector, "compileGeneralSelector");
      exports4.compileGeneralSelector = compileGeneralSelector;
    }
  });

  // node_modules/css-select/lib/compile.js
  var require_compile2 = __commonJS({
    "node_modules/css-select/lib/compile.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.compileToken = exports4.compileUnsafe = exports4.compile = void 0;
      var css_what_1 = (init_es(), __toCommonJS(es_exports));
      var boolbase_1 = __importDefault(require_boolbase());
      var sort_js_1 = __importStar(require_sort());
      var general_js_1 = require_general();
      var subselects_js_1 = require_subselects();
      function compile(selector, options, context) {
        var next2 = compileUnsafe(selector, options, context);
        return (0, subselects_js_1.ensureIsTag)(next2, options.adapter);
      }
      __name(compile, "compile");
      exports4.compile = compile;
      function compileUnsafe(selector, options, context) {
        var token = typeof selector === "string" ? (0, css_what_1.parse)(selector) : selector;
        return compileToken(token, options, context);
      }
      __name(compileUnsafe, "compileUnsafe");
      exports4.compileUnsafe = compileUnsafe;
      function includesScopePseudo(t2) {
        return t2.type === css_what_1.SelectorType.Pseudo && (t2.name === "scope" || Array.isArray(t2.data) && t2.data.some(function(data2) {
          return data2.some(includesScopePseudo);
        }));
      }
      __name(includesScopePseudo, "includesScopePseudo");
      var DESCENDANT_TOKEN = { type: css_what_1.SelectorType.Descendant };
      var FLEXIBLE_DESCENDANT_TOKEN = {
        type: "_flexibleDescendant"
      };
      var SCOPE_TOKEN = {
        type: css_what_1.SelectorType.Pseudo,
        name: "scope",
        data: null
      };
      function absolutize(token, _a, context) {
        var adapter2 = _a.adapter;
        var hasContext = !!(context === null || context === void 0 ? void 0 : context.every(function(e2) {
          var parent2 = adapter2.isTag(e2) && adapter2.getParent(e2);
          return e2 === subselects_js_1.PLACEHOLDER_ELEMENT || parent2 && adapter2.isTag(parent2);
        }));
        for (var _i = 0, token_1 = token; _i < token_1.length; _i++) {
          var t2 = token_1[_i];
          if (t2.length > 0 && (0, sort_js_1.isTraversal)(t2[0]) && t2[0].type !== css_what_1.SelectorType.Descendant) {
          } else if (hasContext && !t2.some(includesScopePseudo)) {
            t2.unshift(DESCENDANT_TOKEN);
          } else {
            continue;
          }
          t2.unshift(SCOPE_TOKEN);
        }
      }
      __name(absolutize, "absolutize");
      function compileToken(token, options, context) {
        var _a;
        token.forEach(sort_js_1.default);
        context = (_a = options.context) !== null && _a !== void 0 ? _a : context;
        var isArrayContext = Array.isArray(context);
        var finalContext = context && (Array.isArray(context) ? context : [context]);
        if (options.relativeSelector !== false) {
          absolutize(token, options, finalContext);
        } else if (token.some(function(t2) {
          return t2.length > 0 && (0, sort_js_1.isTraversal)(t2[0]);
        })) {
          throw new Error("Relative selectors are not allowed when the `relativeSelector` option is disabled");
        }
        var shouldTestNextSiblings = false;
        var query = token.map(function(rules) {
          if (rules.length >= 2) {
            var first2 = rules[0], second = rules[1];
            if (first2.type !== css_what_1.SelectorType.Pseudo || first2.name !== "scope") {
            } else if (isArrayContext && second.type === css_what_1.SelectorType.Descendant) {
              rules[1] = FLEXIBLE_DESCENDANT_TOKEN;
            } else if (second.type === css_what_1.SelectorType.Adjacent || second.type === css_what_1.SelectorType.Sibling) {
              shouldTestNextSiblings = true;
            }
          }
          return compileRules(rules, options, finalContext);
        }).reduce(reduceRules, boolbase_1.default.falseFunc);
        query.shouldTestNextSiblings = shouldTestNextSiblings;
        return query;
      }
      __name(compileToken, "compileToken");
      exports4.compileToken = compileToken;
      function compileRules(rules, options, context) {
        var _a;
        return rules.reduce(function(previous, rule) {
          return previous === boolbase_1.default.falseFunc ? boolbase_1.default.falseFunc : (0, general_js_1.compileGeneralSelector)(previous, rule, options, context, compileToken);
        }, (_a = options.rootFunc) !== null && _a !== void 0 ? _a : boolbase_1.default.trueFunc);
      }
      __name(compileRules, "compileRules");
      function reduceRules(a2, b) {
        if (b === boolbase_1.default.falseFunc || a2 === boolbase_1.default.trueFunc) {
          return a2;
        }
        if (a2 === boolbase_1.default.falseFunc || b === boolbase_1.default.trueFunc) {
          return b;
        }
        return /* @__PURE__ */ __name(function combine(elem) {
          return a2(elem) || b(elem);
        }, "combine");
      }
      __name(reduceRules, "reduceRules");
    }
  });

  // node_modules/css-select/lib/index.js
  var require_lib7 = __commonJS({
    "node_modules/css-select/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.aliases = exports4.pseudos = exports4.filters = exports4.is = exports4.selectOne = exports4.selectAll = exports4.prepareContext = exports4._compileToken = exports4._compileUnsafe = exports4.compile = void 0;
      var DomUtils = __importStar(require_lib5());
      var boolbase_1 = __importDefault(require_boolbase());
      var compile_js_1 = require_compile2();
      var subselects_js_1 = require_subselects();
      var defaultEquals = /* @__PURE__ */ __name(function(a2, b) {
        return a2 === b;
      }, "defaultEquals");
      var defaultOptions = {
        adapter: DomUtils,
        equals: defaultEquals
      };
      function convertOptionFormats(options) {
        var _a, _b, _c, _d;
        var opts = options !== null && options !== void 0 ? options : defaultOptions;
        (_a = opts.adapter) !== null && _a !== void 0 ? _a : opts.adapter = DomUtils;
        (_b = opts.equals) !== null && _b !== void 0 ? _b : opts.equals = (_d = (_c = opts.adapter) === null || _c === void 0 ? void 0 : _c.equals) !== null && _d !== void 0 ? _d : defaultEquals;
        return opts;
      }
      __name(convertOptionFormats, "convertOptionFormats");
      function wrapCompile(func) {
        return /* @__PURE__ */ __name(function addAdapter(selector, options, context) {
          var opts = convertOptionFormats(options);
          return func(selector, opts, context);
        }, "addAdapter");
      }
      __name(wrapCompile, "wrapCompile");
      exports4.compile = wrapCompile(compile_js_1.compile);
      exports4._compileUnsafe = wrapCompile(compile_js_1.compileUnsafe);
      exports4._compileToken = wrapCompile(compile_js_1.compileToken);
      function getSelectorFunc(searchFunc) {
        return /* @__PURE__ */ __name(function select3(query, elements, options) {
          var opts = convertOptionFormats(options);
          if (typeof query !== "function") {
            query = (0, compile_js_1.compileUnsafe)(query, opts, elements);
          }
          var filteredElements = prepareContext(elements, opts.adapter, query.shouldTestNextSiblings);
          return searchFunc(query, filteredElements, opts);
        }, "select");
      }
      __name(getSelectorFunc, "getSelectorFunc");
      function prepareContext(elems, adapter2, shouldTestNextSiblings) {
        if (shouldTestNextSiblings === void 0) {
          shouldTestNextSiblings = false;
        }
        if (shouldTestNextSiblings) {
          elems = appendNextSiblings(elems, adapter2);
        }
        return Array.isArray(elems) ? adapter2.removeSubsets(elems) : adapter2.getChildren(elems);
      }
      __name(prepareContext, "prepareContext");
      exports4.prepareContext = prepareContext;
      function appendNextSiblings(elem, adapter2) {
        var elems = Array.isArray(elem) ? elem.slice(0) : [elem];
        var elemsLength = elems.length;
        for (var i = 0; i < elemsLength; i++) {
          var nextSiblings = (0, subselects_js_1.getNextSiblings)(elems[i], adapter2);
          elems.push.apply(elems, nextSiblings);
        }
        return elems;
      }
      __name(appendNextSiblings, "appendNextSiblings");
      exports4.selectAll = getSelectorFunc(function(query, elems, options) {
        return query === boolbase_1.default.falseFunc || !elems || elems.length === 0 ? [] : options.adapter.findAll(query, elems);
      });
      exports4.selectOne = getSelectorFunc(function(query, elems, options) {
        return query === boolbase_1.default.falseFunc || !elems || elems.length === 0 ? null : options.adapter.findOne(query, elems);
      });
      function is3(elem, query, options) {
        var opts = convertOptionFormats(options);
        return (typeof query === "function" ? query : (0, compile_js_1.compile)(query, opts))(elem);
      }
      __name(is3, "is");
      exports4.is = is3;
      exports4.default = exports4.selectAll;
      var index_js_1 = require_pseudo_selectors();
      Object.defineProperty(exports4, "filters", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return index_js_1.filters;
      }, "get") });
      Object.defineProperty(exports4, "pseudos", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return index_js_1.pseudos;
      }, "get") });
      Object.defineProperty(exports4, "aliases", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return index_js_1.aliases;
      }, "get") });
    }
  });

  // node_modules/cheerio-select/lib/positionals.js
  var require_positionals = __commonJS({
    "node_modules/cheerio-select/lib/positionals.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.getLimit = exports4.isFilter = exports4.filterNames = void 0;
      exports4.filterNames = /* @__PURE__ */ new Set([
        "first",
        "last",
        "eq",
        "gt",
        "nth",
        "lt",
        "even",
        "odd"
      ]);
      function isFilter(s) {
        if (s.type !== "pseudo")
          return false;
        if (exports4.filterNames.has(s.name))
          return true;
        if (s.name === "not" && Array.isArray(s.data)) {
          return s.data.some(function(s2) {
            return s2.some(isFilter);
          });
        }
        return false;
      }
      __name(isFilter, "isFilter");
      exports4.isFilter = isFilter;
      function getLimit(filter3, data2, partLimit) {
        var num = data2 != null ? parseInt(data2, 10) : NaN;
        switch (filter3) {
          case "first":
            return 1;
          case "nth":
          case "eq":
            return isFinite(num) ? num >= 0 ? num + 1 : Infinity : 0;
          case "lt":
            return isFinite(num) ? num >= 0 ? Math.min(num, partLimit) : Infinity : 0;
          case "gt":
            return isFinite(num) ? Infinity : 0;
          case "odd":
            return 2 * partLimit;
          case "even":
            return 2 * partLimit - 1;
          case "last":
          case "not":
            return Infinity;
        }
      }
      __name(getLimit, "getLimit");
      exports4.getLimit = getLimit;
    }
  });

  // node_modules/cheerio-select/lib/helpers.js
  var require_helpers2 = __commonJS({
    "node_modules/cheerio-select/lib/helpers.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.groupSelectors = exports4.getDocumentRoot = void 0;
      var positionals_js_1 = require_positionals();
      function getDocumentRoot(node) {
        while (node.parent)
          node = node.parent;
        return node;
      }
      __name(getDocumentRoot, "getDocumentRoot");
      exports4.getDocumentRoot = getDocumentRoot;
      function groupSelectors(selectors) {
        var filteredSelectors = [];
        var plainSelectors = [];
        for (var _i = 0, selectors_1 = selectors; _i < selectors_1.length; _i++) {
          var selector = selectors_1[_i];
          if (selector.some(positionals_js_1.isFilter)) {
            filteredSelectors.push(selector);
          } else {
            plainSelectors.push(selector);
          }
        }
        return [plainSelectors, filteredSelectors];
      }
      __name(groupSelectors, "groupSelectors");
      exports4.groupSelectors = groupSelectors;
    }
  });

  // node_modules/cheerio-select/lib/index.js
  var require_lib8 = __commonJS({
    "node_modules/cheerio-select/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __assign = exports4 && exports4.__assign || function() {
        __assign = Object.assign || function(t2) {
          for (var s, i = 1, n2 = arguments.length; i < n2; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
              t2[p] = s[p];
          }
          return t2;
        };
        return __assign.apply(this, arguments);
      };
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      var __spreadArray = exports4 && exports4.__spreadArray || function(to, from, pack) {
        if (pack || arguments.length === 2) for (var i = 0, l2 = from.length, ar; i < l2; i++) {
          if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
          }
        }
        return to.concat(ar || Array.prototype.slice.call(from));
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.select = exports4.filter = exports4.some = exports4.is = exports4.aliases = exports4.pseudos = exports4.filters = void 0;
      var css_what_1 = (init_es(), __toCommonJS(es_exports));
      var css_select_1 = require_lib7();
      var DomUtils = __importStar(require_lib5());
      var boolbase = __importStar(require_boolbase());
      var helpers_js_1 = require_helpers2();
      var positionals_js_1 = require_positionals();
      var css_select_2 = require_lib7();
      Object.defineProperty(exports4, "filters", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return css_select_2.filters;
      }, "get") });
      Object.defineProperty(exports4, "pseudos", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return css_select_2.pseudos;
      }, "get") });
      Object.defineProperty(exports4, "aliases", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return css_select_2.aliases;
      }, "get") });
      var UNIVERSAL_SELECTOR = {
        type: css_what_1.SelectorType.Universal,
        namespace: null
      };
      var SCOPE_PSEUDO = {
        type: css_what_1.SelectorType.Pseudo,
        name: "scope",
        data: null
      };
      function is3(element, selector, options) {
        if (options === void 0) {
          options = {};
        }
        return some2([element], selector, options);
      }
      __name(is3, "is");
      exports4.is = is3;
      function some2(elements, selector, options) {
        if (options === void 0) {
          options = {};
        }
        if (typeof selector === "function")
          return elements.some(selector);
        var _a = (0, helpers_js_1.groupSelectors)((0, css_what_1.parse)(selector)), plain = _a[0], filtered = _a[1];
        return plain.length > 0 && elements.some((0, css_select_1._compileToken)(plain, options)) || filtered.some(function(sel) {
          return filterBySelector(sel, elements, options).length > 0;
        });
      }
      __name(some2, "some");
      exports4.some = some2;
      function filterByPosition(filter4, elems, data2, options) {
        var num = typeof data2 === "string" ? parseInt(data2, 10) : NaN;
        switch (filter4) {
          case "first":
          case "lt":
            return elems;
          case "last":
            return elems.length > 0 ? [elems[elems.length - 1]] : elems;
          case "nth":
          case "eq":
            return isFinite(num) && Math.abs(num) < elems.length ? [num < 0 ? elems[elems.length + num] : elems[num]] : [];
          case "gt":
            return isFinite(num) ? elems.slice(num + 1) : [];
          case "even":
            return elems.filter(function(_, i) {
              return i % 2 === 0;
            });
          case "odd":
            return elems.filter(function(_, i) {
              return i % 2 === 1;
            });
          case "not": {
            var filtered_1 = new Set(filterParsed(data2, elems, options));
            return elems.filter(function(e2) {
              return !filtered_1.has(e2);
            });
          }
        }
      }
      __name(filterByPosition, "filterByPosition");
      function filter3(selector, elements, options) {
        if (options === void 0) {
          options = {};
        }
        return filterParsed((0, css_what_1.parse)(selector), elements, options);
      }
      __name(filter3, "filter");
      exports4.filter = filter3;
      function filterParsed(selector, elements, options) {
        if (elements.length === 0)
          return [];
        var _a = (0, helpers_js_1.groupSelectors)(selector), plainSelectors = _a[0], filteredSelectors = _a[1];
        var found;
        if (plainSelectors.length) {
          var filtered = filterElements(elements, plainSelectors, options);
          if (filteredSelectors.length === 0) {
            return filtered;
          }
          if (filtered.length) {
            found = new Set(filtered);
          }
        }
        for (var i = 0; i < filteredSelectors.length && (found === null || found === void 0 ? void 0 : found.size) !== elements.length; i++) {
          var filteredSelector = filteredSelectors[i];
          var missing = found ? elements.filter(function(e2) {
            return DomUtils.isTag(e2) && !found.has(e2);
          }) : elements;
          if (missing.length === 0)
            break;
          var filtered = filterBySelector(filteredSelector, elements, options);
          if (filtered.length) {
            if (!found) {
              if (i === filteredSelectors.length - 1) {
                return filtered;
              }
              found = new Set(filtered);
            } else {
              filtered.forEach(function(el) {
                return found.add(el);
              });
            }
          }
        }
        return typeof found !== "undefined" ? found.size === elements.length ? elements : (
          // Filter elements to preserve order
          elements.filter(function(el) {
            return found.has(el);
          })
        ) : [];
      }
      __name(filterParsed, "filterParsed");
      function filterBySelector(selector, elements, options) {
        var _a;
        if (selector.some(css_what_1.isTraversal)) {
          var root2 = (_a = options.root) !== null && _a !== void 0 ? _a : (0, helpers_js_1.getDocumentRoot)(elements[0]);
          var opts = __assign(__assign({}, options), { context: elements, relativeSelector: false });
          selector.push(SCOPE_PSEUDO);
          return findFilterElements(root2, selector, opts, true, elements.length);
        }
        return findFilterElements(elements, selector, options, false, elements.length);
      }
      __name(filterBySelector, "filterBySelector");
      function select3(selector, root2, options, limit) {
        if (options === void 0) {
          options = {};
        }
        if (limit === void 0) {
          limit = Infinity;
        }
        if (typeof selector === "function") {
          return find2(root2, selector);
        }
        var _a = (0, helpers_js_1.groupSelectors)((0, css_what_1.parse)(selector)), plain = _a[0], filtered = _a[1];
        var results = filtered.map(function(sel) {
          return findFilterElements(root2, sel, options, true, limit);
        });
        if (plain.length) {
          results.push(findElements(root2, plain, options, limit));
        }
        if (results.length === 0) {
          return [];
        }
        if (results.length === 1) {
          return results[0];
        }
        return DomUtils.uniqueSort(results.reduce(function(a2, b) {
          return __spreadArray(__spreadArray([], a2, true), b, true);
        }));
      }
      __name(select3, "select");
      exports4.select = select3;
      function findFilterElements(root2, selector, options, queryForSelector, totalLimit) {
        var filterIndex = selector.findIndex(positionals_js_1.isFilter);
        var sub = selector.slice(0, filterIndex);
        var filter4 = selector[filterIndex];
        var partLimit = selector.length - 1 === filterIndex ? totalLimit : Infinity;
        var limit = (0, positionals_js_1.getLimit)(filter4.name, filter4.data, partLimit);
        if (limit === 0)
          return [];
        var elemsNoLimit = sub.length === 0 && !Array.isArray(root2) ? DomUtils.getChildren(root2).filter(DomUtils.isTag) : sub.length === 0 ? (Array.isArray(root2) ? root2 : [root2]).filter(DomUtils.isTag) : queryForSelector || sub.some(css_what_1.isTraversal) ? findElements(root2, [sub], options, limit) : filterElements(root2, [sub], options);
        var elems = elemsNoLimit.slice(0, limit);
        var result = filterByPosition(filter4.name, elems, filter4.data, options);
        if (result.length === 0 || selector.length === filterIndex + 1) {
          return result;
        }
        var remainingSelector = selector.slice(filterIndex + 1);
        var remainingHasTraversal = remainingSelector.some(css_what_1.isTraversal);
        if (remainingHasTraversal) {
          if ((0, css_what_1.isTraversal)(remainingSelector[0])) {
            var type = remainingSelector[0].type;
            if (type === css_what_1.SelectorType.Sibling || type === css_what_1.SelectorType.Adjacent) {
              result = (0, css_select_1.prepareContext)(result, DomUtils, true);
            }
            remainingSelector.unshift(UNIVERSAL_SELECTOR);
          }
          options = __assign(__assign({}, options), {
            // Avoid absolutizing the selector
            relativeSelector: false,
            /*
             * Add a custom root func, to make sure traversals don't match elements
             * that aren't a part of the considered tree.
             */
            rootFunc: /* @__PURE__ */ __name(function(el) {
              return result.includes(el);
            }, "rootFunc")
          });
        } else if (options.rootFunc && options.rootFunc !== boolbase.trueFunc) {
          options = __assign(__assign({}, options), { rootFunc: boolbase.trueFunc });
        }
        return remainingSelector.some(positionals_js_1.isFilter) ? findFilterElements(result, remainingSelector, options, false, totalLimit) : remainingHasTraversal ? (
          // Query existing elements to resolve traversal.
          findElements(result, [remainingSelector], options, totalLimit)
        ) : (
          // If we don't have any more traversals, simply filter elements.
          filterElements(result, [remainingSelector], options)
        );
      }
      __name(findFilterElements, "findFilterElements");
      function findElements(root2, sel, options, limit) {
        var query = (0, css_select_1._compileToken)(sel, options, root2);
        return find2(root2, query, limit);
      }
      __name(findElements, "findElements");
      function find2(root2, query, limit) {
        if (limit === void 0) {
          limit = Infinity;
        }
        var elems = (0, css_select_1.prepareContext)(root2, DomUtils, query.shouldTestNextSiblings);
        return DomUtils.find(function(node) {
          return DomUtils.isTag(node) && query(node);
        }, elems, true, limit);
      }
      __name(find2, "find");
      function filterElements(elements, sel, options) {
        var els = (Array.isArray(elements) ? elements : [elements]).filter(DomUtils.isTag);
        if (els.length === 0)
          return els;
        var query = (0, css_select_1._compileToken)(sel, options);
        return query === boolbase.trueFunc ? els : els.filter(query);
      }
      __name(filterElements, "filterElements");
    }
  });

  // node_modules/cheerio/dist/browser/api/traversing.js
  var traversing_exports = {};
  __export(traversing_exports, {
    _findBySelector: () => _findBySelector,
    add: () => add,
    addBack: () => addBack,
    children: () => children,
    closest: () => closest,
    contents: () => contents,
    each: () => each,
    end: () => end,
    eq: () => eq,
    filter: () => filter,
    filterArray: () => filterArray,
    find: () => find,
    first: () => first,
    get: () => get,
    has: () => has,
    index: () => index,
    is: () => is,
    last: () => last,
    map: () => map,
    next: () => next,
    nextAll: () => nextAll,
    nextUntil: () => nextUntil,
    not: () => not,
    parent: () => parent,
    parents: () => parents,
    parentsUntil: () => parentsUntil,
    prev: () => prev,
    prevAll: () => prevAll,
    prevUntil: () => prevUntil,
    siblings: () => siblings,
    slice: () => slice,
    toArray: () => toArray
  });
  function find(selectorOrHaystack) {
    if (!selectorOrHaystack) {
      return this._make([]);
    }
    if (typeof selectorOrHaystack !== "string") {
      const haystack = isCheerio(selectorOrHaystack) ? selectorOrHaystack.toArray() : [selectorOrHaystack];
      const context = this.toArray();
      return this._make(haystack.filter((elem) => context.some((node) => contains(node, elem))));
    }
    return this._findBySelector(selectorOrHaystack, Number.POSITIVE_INFINITY);
  }
  function _findBySelector(selector, limit) {
    var _a;
    const context = this.toArray();
    const elems = reSiblingSelector.test(selector) ? context : this.children().toArray();
    const options = {
      context,
      root: (_a = this._root) === null || _a === void 0 ? void 0 : _a[0],
      // Pass options that are recognized by `cheerio-select`
      xmlMode: this.options.xmlMode,
      lowerCaseTags: this.options.lowerCaseTags,
      lowerCaseAttributeNames: this.options.lowerCaseAttributeNames,
      pseudos: this.options.pseudos,
      quirksMode: this.options.quirksMode
    };
    return this._make(select.select(selector, elems, options, limit));
  }
  function _getMatcher(matchMap) {
    return function(fn, ...postFns) {
      return function(selector) {
        var _a;
        let matched = matchMap(fn, this);
        if (selector) {
          matched = filterArray(matched, selector, this.options.xmlMode, (_a = this._root) === null || _a === void 0 ? void 0 : _a[0]);
        }
        return this._make(
          // Post processing is only necessary if there is more than one element.
          this.length > 1 && matched.length > 1 ? postFns.reduce((elems, fn2) => fn2(elems), matched) : matched
        );
      };
    };
  }
  function _matchUntil(nextElem, ...postFns) {
    let matches = null;
    const innerMatcher = _getMatcher((nextElem2, elems) => {
      const matched = [];
      domEach(elems, (elem) => {
        for (let next2; next2 = nextElem2(elem); elem = next2) {
          if (matches === null || matches === void 0 ? void 0 : matches(next2, matched.length))
            break;
          matched.push(next2);
        }
      });
      return matched;
    })(nextElem, ...postFns);
    return function(selector, filterSelector) {
      matches = typeof selector === "string" ? (elem) => select.is(elem, selector, this.options) : selector ? getFilterFn(selector) : null;
      const ret = innerMatcher.call(this, filterSelector);
      matches = null;
      return ret;
    };
  }
  function _removeDuplicates(elems) {
    return elems.length > 1 ? Array.from(new Set(elems)) : elems;
  }
  function closest(selector) {
    var _a;
    const set = [];
    if (!selector) {
      return this._make(set);
    }
    const selectOpts = {
      xmlMode: this.options.xmlMode,
      root: (_a = this._root) === null || _a === void 0 ? void 0 : _a[0]
    };
    const selectFn = typeof selector === "string" ? (elem) => select.is(elem, selector, selectOpts) : getFilterFn(selector);
    domEach(this, (elem) => {
      if (elem && !(0, import_domhandler2.isDocument)(elem) && !(0, import_domhandler2.isTag)(elem)) {
        elem = elem.parent;
      }
      while (elem && (0, import_domhandler2.isTag)(elem)) {
        if (selectFn(elem, 0)) {
          if (!set.includes(elem)) {
            set.push(elem);
          }
          break;
        }
        elem = elem.parent;
      }
    });
    return this._make(set);
  }
  function contents() {
    const elems = this.toArray().reduce((newElems, elem) => (0, import_domhandler2.hasChildren)(elem) ? newElems.concat(elem.children) : newElems, []);
    return this._make(elems);
  }
  function each(fn) {
    let i = 0;
    const len = this.length;
    while (i < len && fn.call(this[i], i, this[i]) !== false)
      ++i;
    return this;
  }
  function map(fn) {
    let elems = [];
    for (let i = 0; i < this.length; i++) {
      const el = this[i];
      const val2 = fn.call(el, i, el);
      if (val2 != null) {
        elems = elems.concat(val2);
      }
    }
    return this._make(elems);
  }
  function getFilterFn(match) {
    if (typeof match === "function") {
      return (el, i) => match.call(el, i, el);
    }
    if (isCheerio(match)) {
      return (el) => Array.prototype.includes.call(match, el);
    }
    return function(el) {
      return match === el;
    };
  }
  function filter(match) {
    var _a;
    return this._make(filterArray(this.toArray(), match, this.options.xmlMode, (_a = this._root) === null || _a === void 0 ? void 0 : _a[0]));
  }
  function filterArray(nodes, match, xmlMode, root2) {
    return typeof match === "string" ? select.filter(match, nodes, { xmlMode, root: root2 }) : nodes.filter(getFilterFn(match));
  }
  function is(selector) {
    const nodes = this.toArray();
    return typeof selector === "string" ? select.some(nodes.filter(import_domhandler2.isTag), selector, this.options) : selector ? nodes.some(getFilterFn(selector)) : false;
  }
  function not(match) {
    let nodes = this.toArray();
    if (typeof match === "string") {
      const matches = new Set(select.filter(match, nodes, this.options));
      nodes = nodes.filter((el) => !matches.has(el));
    } else {
      const filterFn = getFilterFn(match);
      nodes = nodes.filter((el, i) => !filterFn(el, i));
    }
    return this._make(nodes);
  }
  function has(selectorOrHaystack) {
    return this.filter(typeof selectorOrHaystack === "string" ? (
      // Using the `:has` selector here short-circuits searches.
      `:has(${selectorOrHaystack})`
    ) : (_, el) => this._make(el).find(selectorOrHaystack).length > 0);
  }
  function first() {
    return this.length > 1 ? this._make(this[0]) : this;
  }
  function last() {
    return this.length > 0 ? this._make(this[this.length - 1]) : this;
  }
  function eq(i) {
    var _a;
    i = +i;
    if (i === 0 && this.length <= 1)
      return this;
    if (i < 0)
      i = this.length + i;
    return this._make((_a = this[i]) !== null && _a !== void 0 ? _a : []);
  }
  function get(i) {
    if (i == null) {
      return this.toArray();
    }
    return this[i < 0 ? this.length + i : i];
  }
  function toArray() {
    return Array.prototype.slice.call(this);
  }
  function index(selectorOrNeedle) {
    let $haystack;
    let needle;
    if (selectorOrNeedle == null) {
      $haystack = this.parent().children();
      needle = this[0];
    } else if (typeof selectorOrNeedle === "string") {
      $haystack = this._make(selectorOrNeedle);
      needle = this[0];
    } else {
      $haystack = this;
      needle = isCheerio(selectorOrNeedle) ? selectorOrNeedle[0] : selectorOrNeedle;
    }
    return Array.prototype.indexOf.call($haystack, needle);
  }
  function slice(start, end2) {
    return this._make(Array.prototype.slice.call(this, start, end2));
  }
  function end() {
    var _a;
    return (_a = this.prevObject) !== null && _a !== void 0 ? _a : this._make([]);
  }
  function add(other, context) {
    const selection = this._make(other, context);
    const contents2 = (0, import_domutils3.uniqueSort)([...this.get(), ...selection.get()]);
    return this._make(contents2);
  }
  function addBack(selector) {
    return this.prevObject ? this.add(selector ? this.prevObject.filter(selector) : this.prevObject) : this;
  }
  var import_domhandler2, select, import_domutils3, reSiblingSelector, _matcher, _singleMatcher, parent, parents, parentsUntil, next, nextAll, nextUntil, prev, prevAll, prevUntil, siblings, children;
  var init_traversing = __esm({
    "node_modules/cheerio/dist/browser/api/traversing.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domhandler2 = __toESM(require_lib2(), 1);
      select = __toESM(require_lib8(), 1);
      init_utils();
      init_static();
      import_domutils3 = __toESM(require_lib5(), 1);
      reSiblingSelector = /^\s*[+~]/;
      __name(find, "find");
      __name(_findBySelector, "_findBySelector");
      __name(_getMatcher, "_getMatcher");
      _matcher = _getMatcher((fn, elems) => {
        let ret = [];
        for (let i = 0; i < elems.length; i++) {
          const value = fn(elems[i]);
          if (value.length > 0)
            ret = ret.concat(value);
        }
        return ret;
      });
      _singleMatcher = _getMatcher((fn, elems) => {
        const ret = [];
        for (let i = 0; i < elems.length; i++) {
          const value = fn(elems[i]);
          if (value !== null) {
            ret.push(value);
          }
        }
        return ret;
      });
      __name(_matchUntil, "_matchUntil");
      __name(_removeDuplicates, "_removeDuplicates");
      parent = _singleMatcher(({ parent: parent2 }) => parent2 && !(0, import_domhandler2.isDocument)(parent2) ? parent2 : null, _removeDuplicates);
      parents = _matcher((elem) => {
        const matched = [];
        while (elem.parent && !(0, import_domhandler2.isDocument)(elem.parent)) {
          matched.push(elem.parent);
          elem = elem.parent;
        }
        return matched;
      }, import_domutils3.uniqueSort, (elems) => elems.reverse());
      parentsUntil = _matchUntil(({ parent: parent2 }) => parent2 && !(0, import_domhandler2.isDocument)(parent2) ? parent2 : null, import_domutils3.uniqueSort, (elems) => elems.reverse());
      __name(closest, "closest");
      next = _singleMatcher((elem) => (0, import_domutils3.nextElementSibling)(elem));
      nextAll = _matcher((elem) => {
        const matched = [];
        while (elem.next) {
          elem = elem.next;
          if ((0, import_domhandler2.isTag)(elem))
            matched.push(elem);
        }
        return matched;
      }, _removeDuplicates);
      nextUntil = _matchUntil((el) => (0, import_domutils3.nextElementSibling)(el), _removeDuplicates);
      prev = _singleMatcher((elem) => (0, import_domutils3.prevElementSibling)(elem));
      prevAll = _matcher((elem) => {
        const matched = [];
        while (elem.prev) {
          elem = elem.prev;
          if ((0, import_domhandler2.isTag)(elem))
            matched.push(elem);
        }
        return matched;
      }, _removeDuplicates);
      prevUntil = _matchUntil((el) => (0, import_domutils3.prevElementSibling)(el), _removeDuplicates);
      siblings = _matcher((elem) => (0, import_domutils3.getSiblings)(elem).filter((el) => (0, import_domhandler2.isTag)(el) && el !== elem), import_domutils3.uniqueSort);
      children = _matcher((elem) => (0, import_domutils3.getChildren)(elem).filter(import_domhandler2.isTag), _removeDuplicates);
      __name(contents, "contents");
      __name(each, "each");
      __name(map, "map");
      __name(getFilterFn, "getFilterFn");
      __name(filter, "filter");
      __name(filterArray, "filterArray");
      __name(is, "is");
      __name(not, "not");
      __name(has, "has");
      __name(first, "first");
      __name(last, "last");
      __name(eq, "eq");
      __name(get, "get");
      __name(toArray, "toArray");
      __name(index, "index");
      __name(slice, "slice");
      __name(end, "end");
      __name(add, "add");
      __name(addBack, "addBack");
    }
  });

  // node_modules/cheerio/dist/browser/parse.js
  function getParse(parser) {
    return /* @__PURE__ */ __name(function parse5(content, options, isDocument3, context) {
      if (typeof Buffer !== "undefined" && Buffer.isBuffer(content)) {
        content = content.toString();
      }
      if (typeof content === "string") {
        return parser(content, options, isDocument3, context);
      }
      const doc = content;
      if (!Array.isArray(doc) && (0, import_domhandler3.isDocument)(doc)) {
        return doc;
      }
      const root2 = new import_domhandler3.Document([]);
      update(doc, root2);
      return root2;
    }, "parse");
  }
  function update(newChilds, parent2) {
    const arr = Array.isArray(newChilds) ? newChilds : [newChilds];
    if (parent2) {
      parent2.children = arr;
    } else {
      parent2 = null;
    }
    for (let i = 0; i < arr.length; i++) {
      const node = arr[i];
      if (node.parent && node.parent.children !== arr) {
        (0, import_domutils4.removeElement)(node);
      }
      if (parent2) {
        node.prev = arr[i - 1] || null;
        node.next = arr[i + 1] || null;
      } else {
        node.prev = node.next = null;
      }
      node.parent = parent2;
    }
    return parent2;
  }
  var import_domutils4, import_domhandler3;
  var init_parse2 = __esm({
    "node_modules/cheerio/dist/browser/parse.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domutils4 = __toESM(require_lib5(), 1);
      import_domhandler3 = __toESM(require_lib2(), 1);
      __name(getParse, "getParse");
      __name(update, "update");
    }
  });

  // node_modules/cheerio/dist/browser/api/manipulation.js
  var manipulation_exports = {};
  __export(manipulation_exports, {
    _makeDomArray: () => _makeDomArray,
    after: () => after,
    append: () => append,
    appendTo: () => appendTo,
    before: () => before,
    clone: () => clone,
    empty: () => empty,
    html: () => html2,
    insertAfter: () => insertAfter,
    insertBefore: () => insertBefore,
    prepend: () => prepend,
    prependTo: () => prependTo,
    remove: () => remove,
    replaceWith: () => replaceWith,
    text: () => text2,
    toString: () => toString,
    unwrap: () => unwrap,
    wrap: () => wrap,
    wrapAll: () => wrapAll,
    wrapInner: () => wrapInner
  });
  function _makeDomArray(elem, clone2) {
    if (elem == null) {
      return [];
    }
    if (typeof elem === "string") {
      return this._parse(elem, this.options, false, null).children.slice(0);
    }
    if ("length" in elem) {
      if (elem.length === 1) {
        return this._makeDomArray(elem[0], clone2);
      }
      const result = [];
      for (let i = 0; i < elem.length; i++) {
        const el = elem[i];
        if (typeof el === "object") {
          if (el == null) {
            continue;
          }
          if (!("length" in el)) {
            result.push(clone2 ? (0, import_domhandler4.cloneNode)(el, true) : el);
            continue;
          }
        }
        result.push(...this._makeDomArray(el, clone2));
      }
      return result;
    }
    return [clone2 ? (0, import_domhandler4.cloneNode)(elem, true) : elem];
  }
  function _insert(concatenator) {
    return function(...elems) {
      const lastIdx = this.length - 1;
      return domEach(this, (el, i) => {
        if (!(0, import_domhandler4.hasChildren)(el))
          return;
        const domSrc = typeof elems[0] === "function" ? elems[0].call(el, i, this._render(el.children)) : elems;
        const dom = this._makeDomArray(domSrc, i < lastIdx);
        concatenator(dom, el.children, el);
      });
    };
  }
  function uniqueSplice(array, spliceIdx, spliceCount, newElems, parent2) {
    var _a, _b;
    const spliceArgs = [
      spliceIdx,
      spliceCount,
      ...newElems
    ];
    const prev2 = spliceIdx === 0 ? null : array[spliceIdx - 1];
    const next2 = spliceIdx + spliceCount >= array.length ? null : array[spliceIdx + spliceCount];
    for (let idx = 0; idx < newElems.length; ++idx) {
      const node = newElems[idx];
      const oldParent = node.parent;
      if (oldParent) {
        const oldSiblings = oldParent.children;
        const prevIdx = oldSiblings.indexOf(node);
        if (prevIdx > -1) {
          oldParent.children.splice(prevIdx, 1);
          if (parent2 === oldParent && spliceIdx > prevIdx) {
            spliceArgs[0]--;
          }
        }
      }
      node.parent = parent2;
      if (node.prev) {
        node.prev.next = (_a = node.next) !== null && _a !== void 0 ? _a : null;
      }
      if (node.next) {
        node.next.prev = (_b = node.prev) !== null && _b !== void 0 ? _b : null;
      }
      node.prev = idx === 0 ? prev2 : newElems[idx - 1];
      node.next = idx === newElems.length - 1 ? next2 : newElems[idx + 1];
    }
    if (prev2) {
      prev2.next = newElems[0];
    }
    if (next2) {
      next2.prev = newElems[newElems.length - 1];
    }
    return array.splice(...spliceArgs);
  }
  function appendTo(target) {
    const appendTarget = isCheerio(target) ? target : this._make(target);
    appendTarget.append(this);
    return this;
  }
  function prependTo(target) {
    const prependTarget = isCheerio(target) ? target : this._make(target);
    prependTarget.prepend(this);
    return this;
  }
  function _wrap(insert) {
    return function(wrapper) {
      const lastIdx = this.length - 1;
      const lastParent = this.parents().last();
      for (let i = 0; i < this.length; i++) {
        const el = this[i];
        const wrap2 = typeof wrapper === "function" ? wrapper.call(el, i, el) : typeof wrapper === "string" && !isHtml(wrapper) ? lastParent.find(wrapper).clone() : wrapper;
        const [wrapperDom] = this._makeDomArray(wrap2, i < lastIdx);
        if (!wrapperDom || !(0, import_domhandler4.hasChildren)(wrapperDom))
          continue;
        let elInsertLocation = wrapperDom;
        let j = 0;
        while (j < elInsertLocation.children.length) {
          const child = elInsertLocation.children[j];
          if ((0, import_domhandler4.isTag)(child)) {
            elInsertLocation = child;
            j = 0;
          } else {
            j++;
          }
        }
        insert(el, elInsertLocation, [wrapperDom]);
      }
      return this;
    };
  }
  function unwrap(selector) {
    this.parent(selector).not("body").each((_, el) => {
      this._make(el).replaceWith(el.children);
    });
    return this;
  }
  function wrapAll(wrapper) {
    const el = this[0];
    if (el) {
      const wrap2 = this._make(typeof wrapper === "function" ? wrapper.call(el, 0, el) : wrapper).insertBefore(el);
      let elInsertLocation;
      for (let i = 0; i < wrap2.length; i++) {
        if (wrap2[i].type === "tag")
          elInsertLocation = wrap2[i];
      }
      let j = 0;
      while (elInsertLocation && j < elInsertLocation.children.length) {
        const child = elInsertLocation.children[j];
        if (child.type === "tag") {
          elInsertLocation = child;
          j = 0;
        } else {
          j++;
        }
      }
      if (elInsertLocation)
        this._make(elInsertLocation).append(this);
    }
    return this;
  }
  function after(...elems) {
    const lastIdx = this.length - 1;
    return domEach(this, (el, i) => {
      if (!(0, import_domhandler4.hasChildren)(el) || !el.parent) {
        return;
      }
      const siblings2 = el.parent.children;
      const index2 = siblings2.indexOf(el);
      if (index2 < 0)
        return;
      const domSrc = typeof elems[0] === "function" ? elems[0].call(el, i, this._render(el.children)) : elems;
      const dom = this._makeDomArray(domSrc, i < lastIdx);
      uniqueSplice(siblings2, index2 + 1, 0, dom, el.parent);
    });
  }
  function insertAfter(target) {
    if (typeof target === "string") {
      target = this._make(target);
    }
    this.remove();
    const clones = [];
    for (const el of this._makeDomArray(target)) {
      const clonedSelf = this.clone().toArray();
      const { parent: parent2 } = el;
      if (!parent2) {
        continue;
      }
      const siblings2 = parent2.children;
      const index2 = siblings2.indexOf(el);
      if (index2 < 0)
        continue;
      uniqueSplice(siblings2, index2 + 1, 0, clonedSelf, parent2);
      clones.push(...clonedSelf);
    }
    return this._make(clones);
  }
  function before(...elems) {
    const lastIdx = this.length - 1;
    return domEach(this, (el, i) => {
      if (!(0, import_domhandler4.hasChildren)(el) || !el.parent) {
        return;
      }
      const siblings2 = el.parent.children;
      const index2 = siblings2.indexOf(el);
      if (index2 < 0)
        return;
      const domSrc = typeof elems[0] === "function" ? elems[0].call(el, i, this._render(el.children)) : elems;
      const dom = this._makeDomArray(domSrc, i < lastIdx);
      uniqueSplice(siblings2, index2, 0, dom, el.parent);
    });
  }
  function insertBefore(target) {
    const targetArr = this._make(target);
    this.remove();
    const clones = [];
    domEach(targetArr, (el) => {
      const clonedSelf = this.clone().toArray();
      const { parent: parent2 } = el;
      if (!parent2) {
        return;
      }
      const siblings2 = parent2.children;
      const index2 = siblings2.indexOf(el);
      if (index2 < 0)
        return;
      uniqueSplice(siblings2, index2, 0, clonedSelf, parent2);
      clones.push(...clonedSelf);
    });
    return this._make(clones);
  }
  function remove(selector) {
    const elems = selector ? this.filter(selector) : this;
    domEach(elems, (el) => {
      (0, import_domutils5.removeElement)(el);
      el.prev = el.next = el.parent = null;
    });
    return this;
  }
  function replaceWith(content) {
    return domEach(this, (el, i) => {
      const { parent: parent2 } = el;
      if (!parent2) {
        return;
      }
      const siblings2 = parent2.children;
      const cont = typeof content === "function" ? content.call(el, i, el) : content;
      const dom = this._makeDomArray(cont);
      update(dom, null);
      const index2 = siblings2.indexOf(el);
      uniqueSplice(siblings2, index2, 1, dom, parent2);
      if (!dom.includes(el)) {
        el.parent = el.prev = el.next = null;
      }
    });
  }
  function empty() {
    return domEach(this, (el) => {
      if (!(0, import_domhandler4.hasChildren)(el))
        return;
      for (const child of el.children) {
        child.next = child.prev = child.parent = null;
      }
      el.children.length = 0;
    });
  }
  function html2(str) {
    if (str === void 0) {
      const el = this[0];
      if (!el || !(0, import_domhandler4.hasChildren)(el))
        return null;
      return this._render(el.children);
    }
    return domEach(this, (el) => {
      if (!(0, import_domhandler4.hasChildren)(el))
        return;
      for (const child of el.children) {
        child.next = child.prev = child.parent = null;
      }
      const content = isCheerio(str) ? str.toArray() : this._parse(`${str}`, this.options, false, el).children;
      update(content, el);
    });
  }
  function toString() {
    return this._render(this);
  }
  function text2(str) {
    if (str === void 0) {
      return text(this);
    }
    if (typeof str === "function") {
      return domEach(this, (el, i) => this._make(el).text(str.call(el, i, text([el]))));
    }
    return domEach(this, (el) => {
      if (!(0, import_domhandler4.hasChildren)(el))
        return;
      for (const child of el.children) {
        child.next = child.prev = child.parent = null;
      }
      const textNode = new import_domhandler4.Text(`${str}`);
      update(textNode, el);
    });
  }
  function clone() {
    const clone2 = Array.prototype.map.call(this.get(), (el) => (0, import_domhandler4.cloneNode)(el, true));
    const root2 = new import_domhandler4.Document(clone2);
    for (const node of clone2) {
      node.parent = root2;
    }
    return this._make(clone2);
  }
  var import_domhandler4, import_domutils5, append, prepend, wrap, wrapInner;
  var init_manipulation = __esm({
    "node_modules/cheerio/dist/browser/api/manipulation.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domhandler4 = __toESM(require_lib2(), 1);
      init_parse2();
      init_static();
      init_utils();
      import_domutils5 = __toESM(require_lib5(), 1);
      __name(_makeDomArray, "_makeDomArray");
      __name(_insert, "_insert");
      __name(uniqueSplice, "uniqueSplice");
      __name(appendTo, "appendTo");
      __name(prependTo, "prependTo");
      append = _insert((dom, children2, parent2) => {
        uniqueSplice(children2, children2.length, 0, dom, parent2);
      });
      prepend = _insert((dom, children2, parent2) => {
        uniqueSplice(children2, 0, 0, dom, parent2);
      });
      __name(_wrap, "_wrap");
      wrap = _wrap((el, elInsertLocation, wrapperDom) => {
        const { parent: parent2 } = el;
        if (!parent2)
          return;
        const siblings2 = parent2.children;
        const index2 = siblings2.indexOf(el);
        update([el], elInsertLocation);
        uniqueSplice(siblings2, index2, 0, wrapperDom, parent2);
      });
      wrapInner = _wrap((el, elInsertLocation, wrapperDom) => {
        if (!(0, import_domhandler4.hasChildren)(el))
          return;
        update(el.children, elInsertLocation);
        update(wrapperDom, el);
      });
      __name(unwrap, "unwrap");
      __name(wrapAll, "wrapAll");
      __name(after, "after");
      __name(insertAfter, "insertAfter");
      __name(before, "before");
      __name(insertBefore, "insertBefore");
      __name(remove, "remove");
      __name(replaceWith, "replaceWith");
      __name(empty, "empty");
      __name(html2, "html");
      __name(toString, "toString");
      __name(text2, "text");
      __name(clone, "clone");
    }
  });

  // node_modules/cheerio/dist/browser/api/css.js
  var css_exports = {};
  __export(css_exports, {
    css: () => css
  });
  function css(prop2, val2) {
    if (prop2 != null && val2 != null || // When `prop` is a "plain" object
    typeof prop2 === "object" && !Array.isArray(prop2)) {
      return domEach(this, (el, i) => {
        if ((0, import_domhandler5.isTag)(el)) {
          setCss(el, prop2, val2, i);
        }
      });
    }
    if (this.length === 0) {
      return void 0;
    }
    return getCss(this[0], prop2);
  }
  function setCss(el, prop2, value, idx) {
    if (typeof prop2 === "string") {
      const styles = getCss(el);
      const val2 = typeof value === "function" ? value.call(el, idx, styles[prop2]) : value;
      if (val2 === "") {
        delete styles[prop2];
      } else if (val2 != null) {
        styles[prop2] = val2;
      }
      el.attribs["style"] = stringify2(styles);
    } else if (typeof prop2 === "object") {
      const keys = Object.keys(prop2);
      for (let i = 0; i < keys.length; i++) {
        const k = keys[i];
        setCss(el, k, prop2[k], i);
      }
    }
  }
  function getCss(el, prop2) {
    if (!el || !(0, import_domhandler5.isTag)(el))
      return;
    const styles = parse2(el.attribs["style"]);
    if (typeof prop2 === "string") {
      return styles[prop2];
    }
    if (Array.isArray(prop2)) {
      const newStyles = {};
      for (const item of prop2) {
        if (styles[item] != null) {
          newStyles[item] = styles[item];
        }
      }
      return newStyles;
    }
    return styles;
  }
  function stringify2(obj) {
    return Object.keys(obj).reduce((str, prop2) => `${str}${str ? " " : ""}${prop2}: ${obj[prop2]};`, "");
  }
  function parse2(styles) {
    styles = (styles || "").trim();
    if (!styles)
      return {};
    const obj = {};
    let key;
    for (const str of styles.split(";")) {
      const n2 = str.indexOf(":");
      if (n2 < 1 || n2 === str.length - 1) {
        const trimmed = str.trimEnd();
        if (trimmed.length > 0 && key !== void 0) {
          obj[key] += `;${trimmed}`;
        }
      } else {
        key = str.slice(0, n2).trim();
        obj[key] = str.slice(n2 + 1).trim();
      }
    }
    return obj;
  }
  var import_domhandler5;
  var init_css = __esm({
    "node_modules/cheerio/dist/browser/api/css.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_utils();
      import_domhandler5 = __toESM(require_lib2(), 1);
      __name(css, "css");
      __name(setCss, "setCss");
      __name(getCss, "getCss");
      __name(stringify2, "stringify");
      __name(parse2, "parse");
    }
  });

  // node_modules/cheerio/dist/browser/api/forms.js
  var forms_exports = {};
  __export(forms_exports, {
    serialize: () => serialize,
    serializeArray: () => serializeArray
  });
  function serialize() {
    const arr = this.serializeArray();
    const retArr = arr.map((data2) => `${encodeURIComponent(data2.name)}=${encodeURIComponent(data2.value)}`);
    return retArr.join("&").replace(r20, "+");
  }
  function serializeArray() {
    return this.map((_, elem) => {
      const $elem = this._make(elem);
      if ((0, import_domhandler6.isTag)(elem) && elem.name === "form") {
        return $elem.find(submittableSelector).toArray();
      }
      return $elem.filter(submittableSelector).toArray();
    }).filter(
      // Verify elements have a name (`attr.name`) and are not disabled (`:enabled`)
      '[name!=""]:enabled:not(:submit, :button, :image, :reset, :file):matches([checked], :not(:checkbox, :radio))'
    ).map((_, elem) => {
      var _a;
      const $elem = this._make(elem);
      const name = $elem.attr("name");
      const value = (_a = $elem.val()) !== null && _a !== void 0 ? _a : "";
      if (Array.isArray(value)) {
        return value.map((val2) => (
          /*
           * We trim replace any line endings (e.g. `\r` or `\r\n` with `\r\n`) to guarantee consistency across platforms
           * These can occur inside of `<textarea>'s`
           */
          { name, value: val2.replace(rCRLF, "\r\n") }
        ));
      }
      return { name, value: value.replace(rCRLF, "\r\n") };
    }).toArray();
  }
  var import_domhandler6, submittableSelector, r20, rCRLF;
  var init_forms = __esm({
    "node_modules/cheerio/dist/browser/api/forms.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domhandler6 = __toESM(require_lib2(), 1);
      submittableSelector = "input,select,textarea,keygen";
      r20 = /%20/g;
      rCRLF = /\r?\n/g;
      __name(serialize, "serialize");
      __name(serializeArray, "serializeArray");
    }
  });

  // node_modules/cheerio/dist/browser/api/extract.js
  var extract_exports = {};
  __export(extract_exports, {
    extract: () => extract2
  });
  function getExtractDescr(descr) {
    var _a;
    if (typeof descr === "string") {
      return { selector: descr, value: "textContent" };
    }
    return {
      selector: descr.selector,
      value: (_a = descr.value) !== null && _a !== void 0 ? _a : "textContent"
    };
  }
  function extract2(map2) {
    const ret = {};
    for (const key in map2) {
      const descr = map2[key];
      const isArray = Array.isArray(descr);
      const { selector, value } = getExtractDescr(isArray ? descr[0] : descr);
      const fn = typeof value === "function" ? value : typeof value === "string" ? (el) => this._make(el).prop(value) : (el) => this._make(el).extract(value);
      if (isArray) {
        ret[key] = this._findBySelector(selector, Number.POSITIVE_INFINITY).map((_, el) => fn(el, key, ret)).get();
      } else {
        const $2 = this._findBySelector(selector, 1);
        ret[key] = $2.length > 0 ? fn($2[0], key, ret) : void 0;
      }
    }
    return ret;
  }
  var init_extract = __esm({
    "node_modules/cheerio/dist/browser/api/extract.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      __name(getExtractDescr, "getExtractDescr");
      __name(extract2, "extract");
    }
  });

  // node_modules/cheerio/dist/browser/cheerio.js
  var _Cheerio, Cheerio;
  var init_cheerio = __esm({
    "node_modules/cheerio/dist/browser/cheerio.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_attributes();
      init_traversing();
      init_manipulation();
      init_css();
      init_forms();
      init_extract();
      _Cheerio = class _Cheerio {
        /**
         * Instance of cheerio. Methods are specified in the modules. Usage of this
         * constructor is not recommended. Please use `$.load` instead.
         *
         * @private
         * @param elements - The new selection.
         * @param root - Sets the root node.
         * @param options - Options for the instance.
         */
        constructor(elements, root2, options) {
          this.length = 0;
          this.options = options;
          this._root = root2;
          if (elements) {
            for (let idx = 0; idx < elements.length; idx++) {
              this[idx] = elements[idx];
            }
            this.length = elements.length;
          }
        }
      };
      __name(_Cheerio, "Cheerio");
      Cheerio = _Cheerio;
      Cheerio.prototype.cheerio = "[cheerio object]";
      Cheerio.prototype.splice = Array.prototype.splice;
      Cheerio.prototype[Symbol.iterator] = Array.prototype[Symbol.iterator];
      Object.assign(Cheerio.prototype, attributes_exports, traversing_exports, manipulation_exports, css_exports, forms_exports, extract_exports);
    }
  });

  // node_modules/cheerio/dist/browser/load.js
  function getLoad(parse5, render2) {
    return /* @__PURE__ */ __name(function load2(content, options, isDocument3 = true) {
      if (content == null) {
        throw new Error("cheerio.load() expects a string");
      }
      const internalOpts = flattenOptions(options);
      const initialRoot = parse5(content, internalOpts, isDocument3, null);
      const _LoadedCheerio = class _LoadedCheerio extends Cheerio {
        _make(selector, context) {
          const cheerio = initialize(selector, context);
          cheerio.prevObject = this;
          return cheerio;
        }
        _parse(content2, options2, isDocument4, context) {
          return parse5(content2, options2, isDocument4, context);
        }
        _render(dom) {
          return render2(dom, this.options);
        }
      };
      __name(_LoadedCheerio, "LoadedCheerio");
      let LoadedCheerio = _LoadedCheerio;
      function initialize(selector, context, root2 = initialRoot, opts) {
        if (selector && isCheerio(selector))
          return selector;
        const options2 = flattenOptions(opts, internalOpts);
        const r2 = typeof root2 === "string" ? [parse5(root2, options2, false, null)] : "length" in root2 ? root2 : [root2];
        const rootInstance = isCheerio(r2) ? r2 : new LoadedCheerio(r2, null, options2);
        rootInstance._root = rootInstance;
        if (!selector) {
          return new LoadedCheerio(void 0, rootInstance, options2);
        }
        const elements = typeof selector === "string" && isHtml(selector) ? (
          // $(<html>)
          parse5(selector, options2, false, null).children
        ) : isNode(selector) ? (
          // $(dom)
          [selector]
        ) : Array.isArray(selector) ? (
          // $([dom])
          selector
        ) : void 0;
        const instance = new LoadedCheerio(elements, rootInstance, options2);
        if (elements) {
          return instance;
        }
        if (typeof selector !== "string") {
          throw new TypeError("Unexpected type of selector");
        }
        let search = selector;
        const searchContext = context ? (
          // If we don't have a context, maybe we have a root, from loading
          typeof context === "string" ? isHtml(context) ? (
            // $('li', '<ul>...</ul>')
            new LoadedCheerio([parse5(context, options2, false, null)], rootInstance, options2)
          ) : (
            // $('li', 'ul')
            (search = `${context} ${search}`, rootInstance)
          ) : isCheerio(context) ? (
            // $('li', $)
            context
          ) : (
            // $('li', node), $('li', [nodes])
            new LoadedCheerio(Array.isArray(context) ? context : [context], rootInstance, options2)
          )
        ) : rootInstance;
        if (!searchContext)
          return instance;
        return searchContext.find(search);
      }
      __name(initialize, "initialize");
      Object.assign(initialize, static_exports, {
        load: load2,
        // `_root` and `_options` are used in static methods.
        _root: initialRoot,
        _options: internalOpts,
        // Add `fn` for plugins
        fn: LoadedCheerio.prototype,
        // Add the prototype here to maintain `instanceof` behavior.
        prototype: LoadedCheerio.prototype
      });
      return initialize;
    }, "load");
  }
  function isNode(obj) {
    return !!obj.name || obj.type === "root" || obj.type === "text" || obj.type === "comment";
  }
  var init_load = __esm({
    "node_modules/cheerio/dist/browser/load.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_options();
      init_static();
      init_cheerio();
      init_utils();
      __name(getLoad, "getLoad");
      __name(isNode, "isNode");
    }
  });

  // node_modules/parse5/dist/common/unicode.js
  function isSurrogate(cp) {
    return cp >= 55296 && cp <= 57343;
  }
  function isSurrogatePair(cp) {
    return cp >= 56320 && cp <= 57343;
  }
  function getSurrogatePairCodePoint(cp1, cp2) {
    return (cp1 - 55296) * 1024 + 9216 + cp2;
  }
  function isControlCodePoint(cp) {
    return cp !== 32 && cp !== 10 && cp !== 13 && cp !== 9 && cp !== 12 && cp >= 1 && cp <= 31 || cp >= 127 && cp <= 159;
  }
  function isUndefinedCodePoint(cp) {
    return cp >= 64976 && cp <= 65007 || UNDEFINED_CODE_POINTS.has(cp);
  }
  var UNDEFINED_CODE_POINTS, REPLACEMENT_CHARACTER, CODE_POINTS, SEQUENCES;
  var init_unicode = __esm({
    "node_modules/parse5/dist/common/unicode.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      UNDEFINED_CODE_POINTS = /* @__PURE__ */ new Set([
        65534,
        65535,
        131070,
        131071,
        196606,
        196607,
        262142,
        262143,
        327678,
        327679,
        393214,
        393215,
        458750,
        458751,
        524286,
        524287,
        589822,
        589823,
        655358,
        655359,
        720894,
        720895,
        786430,
        786431,
        851966,
        851967,
        917502,
        917503,
        983038,
        983039,
        1048574,
        1048575,
        1114110,
        1114111
      ]);
      REPLACEMENT_CHARACTER = "\uFFFD";
      (function(CODE_POINTS2) {
        CODE_POINTS2[CODE_POINTS2["EOF"] = -1] = "EOF";
        CODE_POINTS2[CODE_POINTS2["NULL"] = 0] = "NULL";
        CODE_POINTS2[CODE_POINTS2["TABULATION"] = 9] = "TABULATION";
        CODE_POINTS2[CODE_POINTS2["CARRIAGE_RETURN"] = 13] = "CARRIAGE_RETURN";
        CODE_POINTS2[CODE_POINTS2["LINE_FEED"] = 10] = "LINE_FEED";
        CODE_POINTS2[CODE_POINTS2["FORM_FEED"] = 12] = "FORM_FEED";
        CODE_POINTS2[CODE_POINTS2["SPACE"] = 32] = "SPACE";
        CODE_POINTS2[CODE_POINTS2["EXCLAMATION_MARK"] = 33] = "EXCLAMATION_MARK";
        CODE_POINTS2[CODE_POINTS2["QUOTATION_MARK"] = 34] = "QUOTATION_MARK";
        CODE_POINTS2[CODE_POINTS2["AMPERSAND"] = 38] = "AMPERSAND";
        CODE_POINTS2[CODE_POINTS2["APOSTROPHE"] = 39] = "APOSTROPHE";
        CODE_POINTS2[CODE_POINTS2["HYPHEN_MINUS"] = 45] = "HYPHEN_MINUS";
        CODE_POINTS2[CODE_POINTS2["SOLIDUS"] = 47] = "SOLIDUS";
        CODE_POINTS2[CODE_POINTS2["DIGIT_0"] = 48] = "DIGIT_0";
        CODE_POINTS2[CODE_POINTS2["DIGIT_9"] = 57] = "DIGIT_9";
        CODE_POINTS2[CODE_POINTS2["SEMICOLON"] = 59] = "SEMICOLON";
        CODE_POINTS2[CODE_POINTS2["LESS_THAN_SIGN"] = 60] = "LESS_THAN_SIGN";
        CODE_POINTS2[CODE_POINTS2["EQUALS_SIGN"] = 61] = "EQUALS_SIGN";
        CODE_POINTS2[CODE_POINTS2["GREATER_THAN_SIGN"] = 62] = "GREATER_THAN_SIGN";
        CODE_POINTS2[CODE_POINTS2["QUESTION_MARK"] = 63] = "QUESTION_MARK";
        CODE_POINTS2[CODE_POINTS2["LATIN_CAPITAL_A"] = 65] = "LATIN_CAPITAL_A";
        CODE_POINTS2[CODE_POINTS2["LATIN_CAPITAL_Z"] = 90] = "LATIN_CAPITAL_Z";
        CODE_POINTS2[CODE_POINTS2["RIGHT_SQUARE_BRACKET"] = 93] = "RIGHT_SQUARE_BRACKET";
        CODE_POINTS2[CODE_POINTS2["GRAVE_ACCENT"] = 96] = "GRAVE_ACCENT";
        CODE_POINTS2[CODE_POINTS2["LATIN_SMALL_A"] = 97] = "LATIN_SMALL_A";
        CODE_POINTS2[CODE_POINTS2["LATIN_SMALL_Z"] = 122] = "LATIN_SMALL_Z";
      })(CODE_POINTS || (CODE_POINTS = {}));
      SEQUENCES = {
        DASH_DASH: "--",
        CDATA_START: "[CDATA[",
        DOCTYPE: "doctype",
        SCRIPT: "script",
        PUBLIC: "public",
        SYSTEM: "system"
      };
      __name(isSurrogate, "isSurrogate");
      __name(isSurrogatePair, "isSurrogatePair");
      __name(getSurrogatePairCodePoint, "getSurrogatePairCodePoint");
      __name(isControlCodePoint, "isControlCodePoint");
      __name(isUndefinedCodePoint, "isUndefinedCodePoint");
    }
  });

  // node_modules/parse5/dist/common/error-codes.js
  var ERR;
  var init_error_codes = __esm({
    "node_modules/parse5/dist/common/error-codes.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      (function(ERR2) {
        ERR2["controlCharacterInInputStream"] = "control-character-in-input-stream";
        ERR2["noncharacterInInputStream"] = "noncharacter-in-input-stream";
        ERR2["surrogateInInputStream"] = "surrogate-in-input-stream";
        ERR2["nonVoidHtmlElementStartTagWithTrailingSolidus"] = "non-void-html-element-start-tag-with-trailing-solidus";
        ERR2["endTagWithAttributes"] = "end-tag-with-attributes";
        ERR2["endTagWithTrailingSolidus"] = "end-tag-with-trailing-solidus";
        ERR2["unexpectedSolidusInTag"] = "unexpected-solidus-in-tag";
        ERR2["unexpectedNullCharacter"] = "unexpected-null-character";
        ERR2["unexpectedQuestionMarkInsteadOfTagName"] = "unexpected-question-mark-instead-of-tag-name";
        ERR2["invalidFirstCharacterOfTagName"] = "invalid-first-character-of-tag-name";
        ERR2["unexpectedEqualsSignBeforeAttributeName"] = "unexpected-equals-sign-before-attribute-name";
        ERR2["missingEndTagName"] = "missing-end-tag-name";
        ERR2["unexpectedCharacterInAttributeName"] = "unexpected-character-in-attribute-name";
        ERR2["unknownNamedCharacterReference"] = "unknown-named-character-reference";
        ERR2["missingSemicolonAfterCharacterReference"] = "missing-semicolon-after-character-reference";
        ERR2["unexpectedCharacterAfterDoctypeSystemIdentifier"] = "unexpected-character-after-doctype-system-identifier";
        ERR2["unexpectedCharacterInUnquotedAttributeValue"] = "unexpected-character-in-unquoted-attribute-value";
        ERR2["eofBeforeTagName"] = "eof-before-tag-name";
        ERR2["eofInTag"] = "eof-in-tag";
        ERR2["missingAttributeValue"] = "missing-attribute-value";
        ERR2["missingWhitespaceBetweenAttributes"] = "missing-whitespace-between-attributes";
        ERR2["missingWhitespaceAfterDoctypePublicKeyword"] = "missing-whitespace-after-doctype-public-keyword";
        ERR2["missingWhitespaceBetweenDoctypePublicAndSystemIdentifiers"] = "missing-whitespace-between-doctype-public-and-system-identifiers";
        ERR2["missingWhitespaceAfterDoctypeSystemKeyword"] = "missing-whitespace-after-doctype-system-keyword";
        ERR2["missingQuoteBeforeDoctypePublicIdentifier"] = "missing-quote-before-doctype-public-identifier";
        ERR2["missingQuoteBeforeDoctypeSystemIdentifier"] = "missing-quote-before-doctype-system-identifier";
        ERR2["missingDoctypePublicIdentifier"] = "missing-doctype-public-identifier";
        ERR2["missingDoctypeSystemIdentifier"] = "missing-doctype-system-identifier";
        ERR2["abruptDoctypePublicIdentifier"] = "abrupt-doctype-public-identifier";
        ERR2["abruptDoctypeSystemIdentifier"] = "abrupt-doctype-system-identifier";
        ERR2["cdataInHtmlContent"] = "cdata-in-html-content";
        ERR2["incorrectlyOpenedComment"] = "incorrectly-opened-comment";
        ERR2["eofInScriptHtmlCommentLikeText"] = "eof-in-script-html-comment-like-text";
        ERR2["eofInDoctype"] = "eof-in-doctype";
        ERR2["nestedComment"] = "nested-comment";
        ERR2["abruptClosingOfEmptyComment"] = "abrupt-closing-of-empty-comment";
        ERR2["eofInComment"] = "eof-in-comment";
        ERR2["incorrectlyClosedComment"] = "incorrectly-closed-comment";
        ERR2["eofInCdata"] = "eof-in-cdata";
        ERR2["absenceOfDigitsInNumericCharacterReference"] = "absence-of-digits-in-numeric-character-reference";
        ERR2["nullCharacterReference"] = "null-character-reference";
        ERR2["surrogateCharacterReference"] = "surrogate-character-reference";
        ERR2["characterReferenceOutsideUnicodeRange"] = "character-reference-outside-unicode-range";
        ERR2["controlCharacterReference"] = "control-character-reference";
        ERR2["noncharacterCharacterReference"] = "noncharacter-character-reference";
        ERR2["missingWhitespaceBeforeDoctypeName"] = "missing-whitespace-before-doctype-name";
        ERR2["missingDoctypeName"] = "missing-doctype-name";
        ERR2["invalidCharacterSequenceAfterDoctypeName"] = "invalid-character-sequence-after-doctype-name";
        ERR2["duplicateAttribute"] = "duplicate-attribute";
        ERR2["nonConformingDoctype"] = "non-conforming-doctype";
        ERR2["missingDoctype"] = "missing-doctype";
        ERR2["misplacedDoctype"] = "misplaced-doctype";
        ERR2["endTagWithoutMatchingOpenElement"] = "end-tag-without-matching-open-element";
        ERR2["closingOfElementWithOpenChildElements"] = "closing-of-element-with-open-child-elements";
        ERR2["disallowedContentInNoscriptInHead"] = "disallowed-content-in-noscript-in-head";
        ERR2["openElementsLeftAfterEof"] = "open-elements-left-after-eof";
        ERR2["abandonedHeadElementChild"] = "abandoned-head-element-child";
        ERR2["misplacedStartTagForHeadElement"] = "misplaced-start-tag-for-head-element";
        ERR2["nestedNoscriptInHead"] = "nested-noscript-in-head";
        ERR2["eofInElementThatCanContainOnlyText"] = "eof-in-element-that-can-contain-only-text";
      })(ERR || (ERR = {}));
    }
  });

  // node_modules/parse5/dist/tokenizer/preprocessor.js
  var DEFAULT_BUFFER_WATERLINE, _Preprocessor, Preprocessor;
  var init_preprocessor = __esm({
    "node_modules/parse5/dist/tokenizer/preprocessor.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_unicode();
      init_error_codes();
      DEFAULT_BUFFER_WATERLINE = 1 << 16;
      _Preprocessor = class _Preprocessor {
        constructor(handler) {
          this.handler = handler;
          this.html = "";
          this.pos = -1;
          this.lastGapPos = -2;
          this.gapStack = [];
          this.skipNextNewLine = false;
          this.lastChunkWritten = false;
          this.endOfChunkHit = false;
          this.bufferWaterline = DEFAULT_BUFFER_WATERLINE;
          this.isEol = false;
          this.lineStartPos = 0;
          this.droppedBufferSize = 0;
          this.line = 1;
          this.lastErrOffset = -1;
        }
        /** The column on the current line. If we just saw a gap (eg. a surrogate pair), return the index before. */
        get col() {
          return this.pos - this.lineStartPos + Number(this.lastGapPos !== this.pos);
        }
        get offset() {
          return this.droppedBufferSize + this.pos;
        }
        getError(code, cpOffset) {
          const { line, col, offset } = this;
          const startCol = col + cpOffset;
          const startOffset = offset + cpOffset;
          return {
            code,
            startLine: line,
            endLine: line,
            startCol,
            endCol: startCol,
            startOffset,
            endOffset: startOffset
          };
        }
        _err(code) {
          if (this.handler.onParseError && this.lastErrOffset !== this.offset) {
            this.lastErrOffset = this.offset;
            this.handler.onParseError(this.getError(code, 0));
          }
        }
        _addGap() {
          this.gapStack.push(this.lastGapPos);
          this.lastGapPos = this.pos;
        }
        _processSurrogate(cp) {
          if (this.pos !== this.html.length - 1) {
            const nextCp = this.html.charCodeAt(this.pos + 1);
            if (isSurrogatePair(nextCp)) {
              this.pos++;
              this._addGap();
              return getSurrogatePairCodePoint(cp, nextCp);
            }
          } else if (!this.lastChunkWritten) {
            this.endOfChunkHit = true;
            return CODE_POINTS.EOF;
          }
          this._err(ERR.surrogateInInputStream);
          return cp;
        }
        willDropParsedChunk() {
          return this.pos > this.bufferWaterline;
        }
        dropParsedChunk() {
          if (this.willDropParsedChunk()) {
            this.html = this.html.substring(this.pos);
            this.lineStartPos -= this.pos;
            this.droppedBufferSize += this.pos;
            this.pos = 0;
            this.lastGapPos = -2;
            this.gapStack.length = 0;
          }
        }
        write(chunk, isLastChunk) {
          if (this.html.length > 0) {
            this.html += chunk;
          } else {
            this.html = chunk;
          }
          this.endOfChunkHit = false;
          this.lastChunkWritten = isLastChunk;
        }
        insertHtmlAtCurrentPos(chunk) {
          this.html = this.html.substring(0, this.pos + 1) + chunk + this.html.substring(this.pos + 1);
          this.endOfChunkHit = false;
        }
        startsWith(pattern, caseSensitive) {
          if (this.pos + pattern.length > this.html.length) {
            this.endOfChunkHit = !this.lastChunkWritten;
            return false;
          }
          if (caseSensitive) {
            return this.html.startsWith(pattern, this.pos);
          }
          for (let i = 0; i < pattern.length; i++) {
            const cp = this.html.charCodeAt(this.pos + i) | 32;
            if (cp !== pattern.charCodeAt(i)) {
              return false;
            }
          }
          return true;
        }
        peek(offset) {
          const pos = this.pos + offset;
          if (pos >= this.html.length) {
            this.endOfChunkHit = !this.lastChunkWritten;
            return CODE_POINTS.EOF;
          }
          const code = this.html.charCodeAt(pos);
          return code === CODE_POINTS.CARRIAGE_RETURN ? CODE_POINTS.LINE_FEED : code;
        }
        advance() {
          this.pos++;
          if (this.isEol) {
            this.isEol = false;
            this.line++;
            this.lineStartPos = this.pos;
          }
          if (this.pos >= this.html.length) {
            this.endOfChunkHit = !this.lastChunkWritten;
            return CODE_POINTS.EOF;
          }
          let cp = this.html.charCodeAt(this.pos);
          if (cp === CODE_POINTS.CARRIAGE_RETURN) {
            this.isEol = true;
            this.skipNextNewLine = true;
            return CODE_POINTS.LINE_FEED;
          }
          if (cp === CODE_POINTS.LINE_FEED) {
            this.isEol = true;
            if (this.skipNextNewLine) {
              this.line--;
              this.skipNextNewLine = false;
              this._addGap();
              return this.advance();
            }
          }
          this.skipNextNewLine = false;
          if (isSurrogate(cp)) {
            cp = this._processSurrogate(cp);
          }
          const isCommonValidRange = this.handler.onParseError === null || cp > 31 && cp < 127 || cp === CODE_POINTS.LINE_FEED || cp === CODE_POINTS.CARRIAGE_RETURN || cp > 159 && cp < 64976;
          if (!isCommonValidRange) {
            this._checkForProblematicCharacters(cp);
          }
          return cp;
        }
        _checkForProblematicCharacters(cp) {
          if (isControlCodePoint(cp)) {
            this._err(ERR.controlCharacterInInputStream);
          } else if (isUndefinedCodePoint(cp)) {
            this._err(ERR.noncharacterInInputStream);
          }
        }
        retreat(count) {
          this.pos -= count;
          while (this.pos < this.lastGapPos) {
            this.lastGapPos = this.gapStack.pop();
            this.pos--;
          }
          this.isEol = false;
        }
      };
      __name(_Preprocessor, "Preprocessor");
      Preprocessor = _Preprocessor;
    }
  });

  // node_modules/parse5/dist/common/token.js
  function getTokenAttr(token, attrName) {
    for (let i = token.attrs.length - 1; i >= 0; i--) {
      if (token.attrs[i].name === attrName) {
        return token.attrs[i].value;
      }
    }
    return null;
  }
  var TokenType;
  var init_token = __esm({
    "node_modules/parse5/dist/common/token.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      (function(TokenType2) {
        TokenType2[TokenType2["CHARACTER"] = 0] = "CHARACTER";
        TokenType2[TokenType2["NULL_CHARACTER"] = 1] = "NULL_CHARACTER";
        TokenType2[TokenType2["WHITESPACE_CHARACTER"] = 2] = "WHITESPACE_CHARACTER";
        TokenType2[TokenType2["START_TAG"] = 3] = "START_TAG";
        TokenType2[TokenType2["END_TAG"] = 4] = "END_TAG";
        TokenType2[TokenType2["COMMENT"] = 5] = "COMMENT";
        TokenType2[TokenType2["DOCTYPE"] = 6] = "DOCTYPE";
        TokenType2[TokenType2["EOF"] = 7] = "EOF";
        TokenType2[TokenType2["HIBERNATION"] = 8] = "HIBERNATION";
      })(TokenType || (TokenType = {}));
      __name(getTokenAttr, "getTokenAttr");
    }
  });

  // node_modules/parse5/dist/common/html.js
  var html_exports = {};
  __export(html_exports, {
    ATTRS: () => ATTRS,
    DOCUMENT_MODE: () => DOCUMENT_MODE,
    NS: () => NS,
    NUMBERED_HEADERS: () => NUMBERED_HEADERS,
    SPECIAL_ELEMENTS: () => SPECIAL_ELEMENTS,
    TAG_ID: () => TAG_ID,
    TAG_NAMES: () => TAG_NAMES,
    getTagID: () => getTagID,
    hasUnescapedText: () => hasUnescapedText
  });
  function getTagID(tagName) {
    var _a;
    return (_a = TAG_NAME_TO_ID.get(tagName)) !== null && _a !== void 0 ? _a : TAG_ID.UNKNOWN;
  }
  function hasUnescapedText(tn, scriptingEnabled) {
    return UNESCAPED_TEXT.has(tn) || scriptingEnabled && tn === TAG_NAMES.NOSCRIPT;
  }
  var NS, ATTRS, DOCUMENT_MODE, TAG_NAMES, TAG_ID, TAG_NAME_TO_ID, $, SPECIAL_ELEMENTS, NUMBERED_HEADERS, UNESCAPED_TEXT;
  var init_html = __esm({
    "node_modules/parse5/dist/common/html.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      (function(NS2) {
        NS2["HTML"] = "http://www.w3.org/1999/xhtml";
        NS2["MATHML"] = "http://www.w3.org/1998/Math/MathML";
        NS2["SVG"] = "http://www.w3.org/2000/svg";
        NS2["XLINK"] = "http://www.w3.org/1999/xlink";
        NS2["XML"] = "http://www.w3.org/XML/1998/namespace";
        NS2["XMLNS"] = "http://www.w3.org/2000/xmlns/";
      })(NS || (NS = {}));
      (function(ATTRS2) {
        ATTRS2["TYPE"] = "type";
        ATTRS2["ACTION"] = "action";
        ATTRS2["ENCODING"] = "encoding";
        ATTRS2["PROMPT"] = "prompt";
        ATTRS2["NAME"] = "name";
        ATTRS2["COLOR"] = "color";
        ATTRS2["FACE"] = "face";
        ATTRS2["SIZE"] = "size";
      })(ATTRS || (ATTRS = {}));
      (function(DOCUMENT_MODE2) {
        DOCUMENT_MODE2["NO_QUIRKS"] = "no-quirks";
        DOCUMENT_MODE2["QUIRKS"] = "quirks";
        DOCUMENT_MODE2["LIMITED_QUIRKS"] = "limited-quirks";
      })(DOCUMENT_MODE || (DOCUMENT_MODE = {}));
      (function(TAG_NAMES2) {
        TAG_NAMES2["A"] = "a";
        TAG_NAMES2["ADDRESS"] = "address";
        TAG_NAMES2["ANNOTATION_XML"] = "annotation-xml";
        TAG_NAMES2["APPLET"] = "applet";
        TAG_NAMES2["AREA"] = "area";
        TAG_NAMES2["ARTICLE"] = "article";
        TAG_NAMES2["ASIDE"] = "aside";
        TAG_NAMES2["B"] = "b";
        TAG_NAMES2["BASE"] = "base";
        TAG_NAMES2["BASEFONT"] = "basefont";
        TAG_NAMES2["BGSOUND"] = "bgsound";
        TAG_NAMES2["BIG"] = "big";
        TAG_NAMES2["BLOCKQUOTE"] = "blockquote";
        TAG_NAMES2["BODY"] = "body";
        TAG_NAMES2["BR"] = "br";
        TAG_NAMES2["BUTTON"] = "button";
        TAG_NAMES2["CAPTION"] = "caption";
        TAG_NAMES2["CENTER"] = "center";
        TAG_NAMES2["CODE"] = "code";
        TAG_NAMES2["COL"] = "col";
        TAG_NAMES2["COLGROUP"] = "colgroup";
        TAG_NAMES2["DD"] = "dd";
        TAG_NAMES2["DESC"] = "desc";
        TAG_NAMES2["DETAILS"] = "details";
        TAG_NAMES2["DIALOG"] = "dialog";
        TAG_NAMES2["DIR"] = "dir";
        TAG_NAMES2["DIV"] = "div";
        TAG_NAMES2["DL"] = "dl";
        TAG_NAMES2["DT"] = "dt";
        TAG_NAMES2["EM"] = "em";
        TAG_NAMES2["EMBED"] = "embed";
        TAG_NAMES2["FIELDSET"] = "fieldset";
        TAG_NAMES2["FIGCAPTION"] = "figcaption";
        TAG_NAMES2["FIGURE"] = "figure";
        TAG_NAMES2["FONT"] = "font";
        TAG_NAMES2["FOOTER"] = "footer";
        TAG_NAMES2["FOREIGN_OBJECT"] = "foreignObject";
        TAG_NAMES2["FORM"] = "form";
        TAG_NAMES2["FRAME"] = "frame";
        TAG_NAMES2["FRAMESET"] = "frameset";
        TAG_NAMES2["H1"] = "h1";
        TAG_NAMES2["H2"] = "h2";
        TAG_NAMES2["H3"] = "h3";
        TAG_NAMES2["H4"] = "h4";
        TAG_NAMES2["H5"] = "h5";
        TAG_NAMES2["H6"] = "h6";
        TAG_NAMES2["HEAD"] = "head";
        TAG_NAMES2["HEADER"] = "header";
        TAG_NAMES2["HGROUP"] = "hgroup";
        TAG_NAMES2["HR"] = "hr";
        TAG_NAMES2["HTML"] = "html";
        TAG_NAMES2["I"] = "i";
        TAG_NAMES2["IMG"] = "img";
        TAG_NAMES2["IMAGE"] = "image";
        TAG_NAMES2["INPUT"] = "input";
        TAG_NAMES2["IFRAME"] = "iframe";
        TAG_NAMES2["KEYGEN"] = "keygen";
        TAG_NAMES2["LABEL"] = "label";
        TAG_NAMES2["LI"] = "li";
        TAG_NAMES2["LINK"] = "link";
        TAG_NAMES2["LISTING"] = "listing";
        TAG_NAMES2["MAIN"] = "main";
        TAG_NAMES2["MALIGNMARK"] = "malignmark";
        TAG_NAMES2["MARQUEE"] = "marquee";
        TAG_NAMES2["MATH"] = "math";
        TAG_NAMES2["MENU"] = "menu";
        TAG_NAMES2["META"] = "meta";
        TAG_NAMES2["MGLYPH"] = "mglyph";
        TAG_NAMES2["MI"] = "mi";
        TAG_NAMES2["MO"] = "mo";
        TAG_NAMES2["MN"] = "mn";
        TAG_NAMES2["MS"] = "ms";
        TAG_NAMES2["MTEXT"] = "mtext";
        TAG_NAMES2["NAV"] = "nav";
        TAG_NAMES2["NOBR"] = "nobr";
        TAG_NAMES2["NOFRAMES"] = "noframes";
        TAG_NAMES2["NOEMBED"] = "noembed";
        TAG_NAMES2["NOSCRIPT"] = "noscript";
        TAG_NAMES2["OBJECT"] = "object";
        TAG_NAMES2["OL"] = "ol";
        TAG_NAMES2["OPTGROUP"] = "optgroup";
        TAG_NAMES2["OPTION"] = "option";
        TAG_NAMES2["P"] = "p";
        TAG_NAMES2["PARAM"] = "param";
        TAG_NAMES2["PLAINTEXT"] = "plaintext";
        TAG_NAMES2["PRE"] = "pre";
        TAG_NAMES2["RB"] = "rb";
        TAG_NAMES2["RP"] = "rp";
        TAG_NAMES2["RT"] = "rt";
        TAG_NAMES2["RTC"] = "rtc";
        TAG_NAMES2["RUBY"] = "ruby";
        TAG_NAMES2["S"] = "s";
        TAG_NAMES2["SCRIPT"] = "script";
        TAG_NAMES2["SEARCH"] = "search";
        TAG_NAMES2["SECTION"] = "section";
        TAG_NAMES2["SELECT"] = "select";
        TAG_NAMES2["SOURCE"] = "source";
        TAG_NAMES2["SMALL"] = "small";
        TAG_NAMES2["SPAN"] = "span";
        TAG_NAMES2["STRIKE"] = "strike";
        TAG_NAMES2["STRONG"] = "strong";
        TAG_NAMES2["STYLE"] = "style";
        TAG_NAMES2["SUB"] = "sub";
        TAG_NAMES2["SUMMARY"] = "summary";
        TAG_NAMES2["SUP"] = "sup";
        TAG_NAMES2["TABLE"] = "table";
        TAG_NAMES2["TBODY"] = "tbody";
        TAG_NAMES2["TEMPLATE"] = "template";
        TAG_NAMES2["TEXTAREA"] = "textarea";
        TAG_NAMES2["TFOOT"] = "tfoot";
        TAG_NAMES2["TD"] = "td";
        TAG_NAMES2["TH"] = "th";
        TAG_NAMES2["THEAD"] = "thead";
        TAG_NAMES2["TITLE"] = "title";
        TAG_NAMES2["TR"] = "tr";
        TAG_NAMES2["TRACK"] = "track";
        TAG_NAMES2["TT"] = "tt";
        TAG_NAMES2["U"] = "u";
        TAG_NAMES2["UL"] = "ul";
        TAG_NAMES2["SVG"] = "svg";
        TAG_NAMES2["VAR"] = "var";
        TAG_NAMES2["WBR"] = "wbr";
        TAG_NAMES2["XMP"] = "xmp";
      })(TAG_NAMES || (TAG_NAMES = {}));
      (function(TAG_ID2) {
        TAG_ID2[TAG_ID2["UNKNOWN"] = 0] = "UNKNOWN";
        TAG_ID2[TAG_ID2["A"] = 1] = "A";
        TAG_ID2[TAG_ID2["ADDRESS"] = 2] = "ADDRESS";
        TAG_ID2[TAG_ID2["ANNOTATION_XML"] = 3] = "ANNOTATION_XML";
        TAG_ID2[TAG_ID2["APPLET"] = 4] = "APPLET";
        TAG_ID2[TAG_ID2["AREA"] = 5] = "AREA";
        TAG_ID2[TAG_ID2["ARTICLE"] = 6] = "ARTICLE";
        TAG_ID2[TAG_ID2["ASIDE"] = 7] = "ASIDE";
        TAG_ID2[TAG_ID2["B"] = 8] = "B";
        TAG_ID2[TAG_ID2["BASE"] = 9] = "BASE";
        TAG_ID2[TAG_ID2["BASEFONT"] = 10] = "BASEFONT";
        TAG_ID2[TAG_ID2["BGSOUND"] = 11] = "BGSOUND";
        TAG_ID2[TAG_ID2["BIG"] = 12] = "BIG";
        TAG_ID2[TAG_ID2["BLOCKQUOTE"] = 13] = "BLOCKQUOTE";
        TAG_ID2[TAG_ID2["BODY"] = 14] = "BODY";
        TAG_ID2[TAG_ID2["BR"] = 15] = "BR";
        TAG_ID2[TAG_ID2["BUTTON"] = 16] = "BUTTON";
        TAG_ID2[TAG_ID2["CAPTION"] = 17] = "CAPTION";
        TAG_ID2[TAG_ID2["CENTER"] = 18] = "CENTER";
        TAG_ID2[TAG_ID2["CODE"] = 19] = "CODE";
        TAG_ID2[TAG_ID2["COL"] = 20] = "COL";
        TAG_ID2[TAG_ID2["COLGROUP"] = 21] = "COLGROUP";
        TAG_ID2[TAG_ID2["DD"] = 22] = "DD";
        TAG_ID2[TAG_ID2["DESC"] = 23] = "DESC";
        TAG_ID2[TAG_ID2["DETAILS"] = 24] = "DETAILS";
        TAG_ID2[TAG_ID2["DIALOG"] = 25] = "DIALOG";
        TAG_ID2[TAG_ID2["DIR"] = 26] = "DIR";
        TAG_ID2[TAG_ID2["DIV"] = 27] = "DIV";
        TAG_ID2[TAG_ID2["DL"] = 28] = "DL";
        TAG_ID2[TAG_ID2["DT"] = 29] = "DT";
        TAG_ID2[TAG_ID2["EM"] = 30] = "EM";
        TAG_ID2[TAG_ID2["EMBED"] = 31] = "EMBED";
        TAG_ID2[TAG_ID2["FIELDSET"] = 32] = "FIELDSET";
        TAG_ID2[TAG_ID2["FIGCAPTION"] = 33] = "FIGCAPTION";
        TAG_ID2[TAG_ID2["FIGURE"] = 34] = "FIGURE";
        TAG_ID2[TAG_ID2["FONT"] = 35] = "FONT";
        TAG_ID2[TAG_ID2["FOOTER"] = 36] = "FOOTER";
        TAG_ID2[TAG_ID2["FOREIGN_OBJECT"] = 37] = "FOREIGN_OBJECT";
        TAG_ID2[TAG_ID2["FORM"] = 38] = "FORM";
        TAG_ID2[TAG_ID2["FRAME"] = 39] = "FRAME";
        TAG_ID2[TAG_ID2["FRAMESET"] = 40] = "FRAMESET";
        TAG_ID2[TAG_ID2["H1"] = 41] = "H1";
        TAG_ID2[TAG_ID2["H2"] = 42] = "H2";
        TAG_ID2[TAG_ID2["H3"] = 43] = "H3";
        TAG_ID2[TAG_ID2["H4"] = 44] = "H4";
        TAG_ID2[TAG_ID2["H5"] = 45] = "H5";
        TAG_ID2[TAG_ID2["H6"] = 46] = "H6";
        TAG_ID2[TAG_ID2["HEAD"] = 47] = "HEAD";
        TAG_ID2[TAG_ID2["HEADER"] = 48] = "HEADER";
        TAG_ID2[TAG_ID2["HGROUP"] = 49] = "HGROUP";
        TAG_ID2[TAG_ID2["HR"] = 50] = "HR";
        TAG_ID2[TAG_ID2["HTML"] = 51] = "HTML";
        TAG_ID2[TAG_ID2["I"] = 52] = "I";
        TAG_ID2[TAG_ID2["IMG"] = 53] = "IMG";
        TAG_ID2[TAG_ID2["IMAGE"] = 54] = "IMAGE";
        TAG_ID2[TAG_ID2["INPUT"] = 55] = "INPUT";
        TAG_ID2[TAG_ID2["IFRAME"] = 56] = "IFRAME";
        TAG_ID2[TAG_ID2["KEYGEN"] = 57] = "KEYGEN";
        TAG_ID2[TAG_ID2["LABEL"] = 58] = "LABEL";
        TAG_ID2[TAG_ID2["LI"] = 59] = "LI";
        TAG_ID2[TAG_ID2["LINK"] = 60] = "LINK";
        TAG_ID2[TAG_ID2["LISTING"] = 61] = "LISTING";
        TAG_ID2[TAG_ID2["MAIN"] = 62] = "MAIN";
        TAG_ID2[TAG_ID2["MALIGNMARK"] = 63] = "MALIGNMARK";
        TAG_ID2[TAG_ID2["MARQUEE"] = 64] = "MARQUEE";
        TAG_ID2[TAG_ID2["MATH"] = 65] = "MATH";
        TAG_ID2[TAG_ID2["MENU"] = 66] = "MENU";
        TAG_ID2[TAG_ID2["META"] = 67] = "META";
        TAG_ID2[TAG_ID2["MGLYPH"] = 68] = "MGLYPH";
        TAG_ID2[TAG_ID2["MI"] = 69] = "MI";
        TAG_ID2[TAG_ID2["MO"] = 70] = "MO";
        TAG_ID2[TAG_ID2["MN"] = 71] = "MN";
        TAG_ID2[TAG_ID2["MS"] = 72] = "MS";
        TAG_ID2[TAG_ID2["MTEXT"] = 73] = "MTEXT";
        TAG_ID2[TAG_ID2["NAV"] = 74] = "NAV";
        TAG_ID2[TAG_ID2["NOBR"] = 75] = "NOBR";
        TAG_ID2[TAG_ID2["NOFRAMES"] = 76] = "NOFRAMES";
        TAG_ID2[TAG_ID2["NOEMBED"] = 77] = "NOEMBED";
        TAG_ID2[TAG_ID2["NOSCRIPT"] = 78] = "NOSCRIPT";
        TAG_ID2[TAG_ID2["OBJECT"] = 79] = "OBJECT";
        TAG_ID2[TAG_ID2["OL"] = 80] = "OL";
        TAG_ID2[TAG_ID2["OPTGROUP"] = 81] = "OPTGROUP";
        TAG_ID2[TAG_ID2["OPTION"] = 82] = "OPTION";
        TAG_ID2[TAG_ID2["P"] = 83] = "P";
        TAG_ID2[TAG_ID2["PARAM"] = 84] = "PARAM";
        TAG_ID2[TAG_ID2["PLAINTEXT"] = 85] = "PLAINTEXT";
        TAG_ID2[TAG_ID2["PRE"] = 86] = "PRE";
        TAG_ID2[TAG_ID2["RB"] = 87] = "RB";
        TAG_ID2[TAG_ID2["RP"] = 88] = "RP";
        TAG_ID2[TAG_ID2["RT"] = 89] = "RT";
        TAG_ID2[TAG_ID2["RTC"] = 90] = "RTC";
        TAG_ID2[TAG_ID2["RUBY"] = 91] = "RUBY";
        TAG_ID2[TAG_ID2["S"] = 92] = "S";
        TAG_ID2[TAG_ID2["SCRIPT"] = 93] = "SCRIPT";
        TAG_ID2[TAG_ID2["SEARCH"] = 94] = "SEARCH";
        TAG_ID2[TAG_ID2["SECTION"] = 95] = "SECTION";
        TAG_ID2[TAG_ID2["SELECT"] = 96] = "SELECT";
        TAG_ID2[TAG_ID2["SOURCE"] = 97] = "SOURCE";
        TAG_ID2[TAG_ID2["SMALL"] = 98] = "SMALL";
        TAG_ID2[TAG_ID2["SPAN"] = 99] = "SPAN";
        TAG_ID2[TAG_ID2["STRIKE"] = 100] = "STRIKE";
        TAG_ID2[TAG_ID2["STRONG"] = 101] = "STRONG";
        TAG_ID2[TAG_ID2["STYLE"] = 102] = "STYLE";
        TAG_ID2[TAG_ID2["SUB"] = 103] = "SUB";
        TAG_ID2[TAG_ID2["SUMMARY"] = 104] = "SUMMARY";
        TAG_ID2[TAG_ID2["SUP"] = 105] = "SUP";
        TAG_ID2[TAG_ID2["TABLE"] = 106] = "TABLE";
        TAG_ID2[TAG_ID2["TBODY"] = 107] = "TBODY";
        TAG_ID2[TAG_ID2["TEMPLATE"] = 108] = "TEMPLATE";
        TAG_ID2[TAG_ID2["TEXTAREA"] = 109] = "TEXTAREA";
        TAG_ID2[TAG_ID2["TFOOT"] = 110] = "TFOOT";
        TAG_ID2[TAG_ID2["TD"] = 111] = "TD";
        TAG_ID2[TAG_ID2["TH"] = 112] = "TH";
        TAG_ID2[TAG_ID2["THEAD"] = 113] = "THEAD";
        TAG_ID2[TAG_ID2["TITLE"] = 114] = "TITLE";
        TAG_ID2[TAG_ID2["TR"] = 115] = "TR";
        TAG_ID2[TAG_ID2["TRACK"] = 116] = "TRACK";
        TAG_ID2[TAG_ID2["TT"] = 117] = "TT";
        TAG_ID2[TAG_ID2["U"] = 118] = "U";
        TAG_ID2[TAG_ID2["UL"] = 119] = "UL";
        TAG_ID2[TAG_ID2["SVG"] = 120] = "SVG";
        TAG_ID2[TAG_ID2["VAR"] = 121] = "VAR";
        TAG_ID2[TAG_ID2["WBR"] = 122] = "WBR";
        TAG_ID2[TAG_ID2["XMP"] = 123] = "XMP";
      })(TAG_ID || (TAG_ID = {}));
      TAG_NAME_TO_ID = /* @__PURE__ */ new Map([
        [TAG_NAMES.A, TAG_ID.A],
        [TAG_NAMES.ADDRESS, TAG_ID.ADDRESS],
        [TAG_NAMES.ANNOTATION_XML, TAG_ID.ANNOTATION_XML],
        [TAG_NAMES.APPLET, TAG_ID.APPLET],
        [TAG_NAMES.AREA, TAG_ID.AREA],
        [TAG_NAMES.ARTICLE, TAG_ID.ARTICLE],
        [TAG_NAMES.ASIDE, TAG_ID.ASIDE],
        [TAG_NAMES.B, TAG_ID.B],
        [TAG_NAMES.BASE, TAG_ID.BASE],
        [TAG_NAMES.BASEFONT, TAG_ID.BASEFONT],
        [TAG_NAMES.BGSOUND, TAG_ID.BGSOUND],
        [TAG_NAMES.BIG, TAG_ID.BIG],
        [TAG_NAMES.BLOCKQUOTE, TAG_ID.BLOCKQUOTE],
        [TAG_NAMES.BODY, TAG_ID.BODY],
        [TAG_NAMES.BR, TAG_ID.BR],
        [TAG_NAMES.BUTTON, TAG_ID.BUTTON],
        [TAG_NAMES.CAPTION, TAG_ID.CAPTION],
        [TAG_NAMES.CENTER, TAG_ID.CENTER],
        [TAG_NAMES.CODE, TAG_ID.CODE],
        [TAG_NAMES.COL, TAG_ID.COL],
        [TAG_NAMES.COLGROUP, TAG_ID.COLGROUP],
        [TAG_NAMES.DD, TAG_ID.DD],
        [TAG_NAMES.DESC, TAG_ID.DESC],
        [TAG_NAMES.DETAILS, TAG_ID.DETAILS],
        [TAG_NAMES.DIALOG, TAG_ID.DIALOG],
        [TAG_NAMES.DIR, TAG_ID.DIR],
        [TAG_NAMES.DIV, TAG_ID.DIV],
        [TAG_NAMES.DL, TAG_ID.DL],
        [TAG_NAMES.DT, TAG_ID.DT],
        [TAG_NAMES.EM, TAG_ID.EM],
        [TAG_NAMES.EMBED, TAG_ID.EMBED],
        [TAG_NAMES.FIELDSET, TAG_ID.FIELDSET],
        [TAG_NAMES.FIGCAPTION, TAG_ID.FIGCAPTION],
        [TAG_NAMES.FIGURE, TAG_ID.FIGURE],
        [TAG_NAMES.FONT, TAG_ID.FONT],
        [TAG_NAMES.FOOTER, TAG_ID.FOOTER],
        [TAG_NAMES.FOREIGN_OBJECT, TAG_ID.FOREIGN_OBJECT],
        [TAG_NAMES.FORM, TAG_ID.FORM],
        [TAG_NAMES.FRAME, TAG_ID.FRAME],
        [TAG_NAMES.FRAMESET, TAG_ID.FRAMESET],
        [TAG_NAMES.H1, TAG_ID.H1],
        [TAG_NAMES.H2, TAG_ID.H2],
        [TAG_NAMES.H3, TAG_ID.H3],
        [TAG_NAMES.H4, TAG_ID.H4],
        [TAG_NAMES.H5, TAG_ID.H5],
        [TAG_NAMES.H6, TAG_ID.H6],
        [TAG_NAMES.HEAD, TAG_ID.HEAD],
        [TAG_NAMES.HEADER, TAG_ID.HEADER],
        [TAG_NAMES.HGROUP, TAG_ID.HGROUP],
        [TAG_NAMES.HR, TAG_ID.HR],
        [TAG_NAMES.HTML, TAG_ID.HTML],
        [TAG_NAMES.I, TAG_ID.I],
        [TAG_NAMES.IMG, TAG_ID.IMG],
        [TAG_NAMES.IMAGE, TAG_ID.IMAGE],
        [TAG_NAMES.INPUT, TAG_ID.INPUT],
        [TAG_NAMES.IFRAME, TAG_ID.IFRAME],
        [TAG_NAMES.KEYGEN, TAG_ID.KEYGEN],
        [TAG_NAMES.LABEL, TAG_ID.LABEL],
        [TAG_NAMES.LI, TAG_ID.LI],
        [TAG_NAMES.LINK, TAG_ID.LINK],
        [TAG_NAMES.LISTING, TAG_ID.LISTING],
        [TAG_NAMES.MAIN, TAG_ID.MAIN],
        [TAG_NAMES.MALIGNMARK, TAG_ID.MALIGNMARK],
        [TAG_NAMES.MARQUEE, TAG_ID.MARQUEE],
        [TAG_NAMES.MATH, TAG_ID.MATH],
        [TAG_NAMES.MENU, TAG_ID.MENU],
        [TAG_NAMES.META, TAG_ID.META],
        [TAG_NAMES.MGLYPH, TAG_ID.MGLYPH],
        [TAG_NAMES.MI, TAG_ID.MI],
        [TAG_NAMES.MO, TAG_ID.MO],
        [TAG_NAMES.MN, TAG_ID.MN],
        [TAG_NAMES.MS, TAG_ID.MS],
        [TAG_NAMES.MTEXT, TAG_ID.MTEXT],
        [TAG_NAMES.NAV, TAG_ID.NAV],
        [TAG_NAMES.NOBR, TAG_ID.NOBR],
        [TAG_NAMES.NOFRAMES, TAG_ID.NOFRAMES],
        [TAG_NAMES.NOEMBED, TAG_ID.NOEMBED],
        [TAG_NAMES.NOSCRIPT, TAG_ID.NOSCRIPT],
        [TAG_NAMES.OBJECT, TAG_ID.OBJECT],
        [TAG_NAMES.OL, TAG_ID.OL],
        [TAG_NAMES.OPTGROUP, TAG_ID.OPTGROUP],
        [TAG_NAMES.OPTION, TAG_ID.OPTION],
        [TAG_NAMES.P, TAG_ID.P],
        [TAG_NAMES.PARAM, TAG_ID.PARAM],
        [TAG_NAMES.PLAINTEXT, TAG_ID.PLAINTEXT],
        [TAG_NAMES.PRE, TAG_ID.PRE],
        [TAG_NAMES.RB, TAG_ID.RB],
        [TAG_NAMES.RP, TAG_ID.RP],
        [TAG_NAMES.RT, TAG_ID.RT],
        [TAG_NAMES.RTC, TAG_ID.RTC],
        [TAG_NAMES.RUBY, TAG_ID.RUBY],
        [TAG_NAMES.S, TAG_ID.S],
        [TAG_NAMES.SCRIPT, TAG_ID.SCRIPT],
        [TAG_NAMES.SEARCH, TAG_ID.SEARCH],
        [TAG_NAMES.SECTION, TAG_ID.SECTION],
        [TAG_NAMES.SELECT, TAG_ID.SELECT],
        [TAG_NAMES.SOURCE, TAG_ID.SOURCE],
        [TAG_NAMES.SMALL, TAG_ID.SMALL],
        [TAG_NAMES.SPAN, TAG_ID.SPAN],
        [TAG_NAMES.STRIKE, TAG_ID.STRIKE],
        [TAG_NAMES.STRONG, TAG_ID.STRONG],
        [TAG_NAMES.STYLE, TAG_ID.STYLE],
        [TAG_NAMES.SUB, TAG_ID.SUB],
        [TAG_NAMES.SUMMARY, TAG_ID.SUMMARY],
        [TAG_NAMES.SUP, TAG_ID.SUP],
        [TAG_NAMES.TABLE, TAG_ID.TABLE],
        [TAG_NAMES.TBODY, TAG_ID.TBODY],
        [TAG_NAMES.TEMPLATE, TAG_ID.TEMPLATE],
        [TAG_NAMES.TEXTAREA, TAG_ID.TEXTAREA],
        [TAG_NAMES.TFOOT, TAG_ID.TFOOT],
        [TAG_NAMES.TD, TAG_ID.TD],
        [TAG_NAMES.TH, TAG_ID.TH],
        [TAG_NAMES.THEAD, TAG_ID.THEAD],
        [TAG_NAMES.TITLE, TAG_ID.TITLE],
        [TAG_NAMES.TR, TAG_ID.TR],
        [TAG_NAMES.TRACK, TAG_ID.TRACK],
        [TAG_NAMES.TT, TAG_ID.TT],
        [TAG_NAMES.U, TAG_ID.U],
        [TAG_NAMES.UL, TAG_ID.UL],
        [TAG_NAMES.SVG, TAG_ID.SVG],
        [TAG_NAMES.VAR, TAG_ID.VAR],
        [TAG_NAMES.WBR, TAG_ID.WBR],
        [TAG_NAMES.XMP, TAG_ID.XMP]
      ]);
      __name(getTagID, "getTagID");
      $ = TAG_ID;
      SPECIAL_ELEMENTS = {
        [NS.HTML]: /* @__PURE__ */ new Set([
          $.ADDRESS,
          $.APPLET,
          $.AREA,
          $.ARTICLE,
          $.ASIDE,
          $.BASE,
          $.BASEFONT,
          $.BGSOUND,
          $.BLOCKQUOTE,
          $.BODY,
          $.BR,
          $.BUTTON,
          $.CAPTION,
          $.CENTER,
          $.COL,
          $.COLGROUP,
          $.DD,
          $.DETAILS,
          $.DIR,
          $.DIV,
          $.DL,
          $.DT,
          $.EMBED,
          $.FIELDSET,
          $.FIGCAPTION,
          $.FIGURE,
          $.FOOTER,
          $.FORM,
          $.FRAME,
          $.FRAMESET,
          $.H1,
          $.H2,
          $.H3,
          $.H4,
          $.H5,
          $.H6,
          $.HEAD,
          $.HEADER,
          $.HGROUP,
          $.HR,
          $.HTML,
          $.IFRAME,
          $.IMG,
          $.INPUT,
          $.LI,
          $.LINK,
          $.LISTING,
          $.MAIN,
          $.MARQUEE,
          $.MENU,
          $.META,
          $.NAV,
          $.NOEMBED,
          $.NOFRAMES,
          $.NOSCRIPT,
          $.OBJECT,
          $.OL,
          $.P,
          $.PARAM,
          $.PLAINTEXT,
          $.PRE,
          $.SCRIPT,
          $.SECTION,
          $.SELECT,
          $.SOURCE,
          $.STYLE,
          $.SUMMARY,
          $.TABLE,
          $.TBODY,
          $.TD,
          $.TEMPLATE,
          $.TEXTAREA,
          $.TFOOT,
          $.TH,
          $.THEAD,
          $.TITLE,
          $.TR,
          $.TRACK,
          $.UL,
          $.WBR,
          $.XMP
        ]),
        [NS.MATHML]: /* @__PURE__ */ new Set([$.MI, $.MO, $.MN, $.MS, $.MTEXT, $.ANNOTATION_XML]),
        [NS.SVG]: /* @__PURE__ */ new Set([$.TITLE, $.FOREIGN_OBJECT, $.DESC]),
        [NS.XLINK]: /* @__PURE__ */ new Set(),
        [NS.XML]: /* @__PURE__ */ new Set(),
        [NS.XMLNS]: /* @__PURE__ */ new Set()
      };
      NUMBERED_HEADERS = /* @__PURE__ */ new Set([$.H1, $.H2, $.H3, $.H4, $.H5, $.H6]);
      UNESCAPED_TEXT = /* @__PURE__ */ new Set([
        TAG_NAMES.STYLE,
        TAG_NAMES.SCRIPT,
        TAG_NAMES.XMP,
        TAG_NAMES.IFRAME,
        TAG_NAMES.NOEMBED,
        TAG_NAMES.NOFRAMES,
        TAG_NAMES.PLAINTEXT
      ]);
      __name(hasUnescapedText, "hasUnescapedText");
    }
  });

  // node_modules/parse5/dist/tokenizer/index.js
  function isAsciiDigit(cp) {
    return cp >= CODE_POINTS.DIGIT_0 && cp <= CODE_POINTS.DIGIT_9;
  }
  function isAsciiUpper(cp) {
    return cp >= CODE_POINTS.LATIN_CAPITAL_A && cp <= CODE_POINTS.LATIN_CAPITAL_Z;
  }
  function isAsciiLower(cp) {
    return cp >= CODE_POINTS.LATIN_SMALL_A && cp <= CODE_POINTS.LATIN_SMALL_Z;
  }
  function isAsciiLetter(cp) {
    return isAsciiLower(cp) || isAsciiUpper(cp);
  }
  function isAsciiAlphaNumeric(cp) {
    return isAsciiLetter(cp) || isAsciiDigit(cp);
  }
  function toAsciiLower(cp) {
    return cp + 32;
  }
  function isWhitespace2(cp) {
    return cp === CODE_POINTS.SPACE || cp === CODE_POINTS.LINE_FEED || cp === CODE_POINTS.TABULATION || cp === CODE_POINTS.FORM_FEED;
  }
  function isScriptDataDoubleEscapeSequenceEnd(cp) {
    return isWhitespace2(cp) || cp === CODE_POINTS.SOLIDUS || cp === CODE_POINTS.GREATER_THAN_SIGN;
  }
  function getErrorForNumericCharacterReference(code) {
    if (code === CODE_POINTS.NULL) {
      return ERR.nullCharacterReference;
    } else if (code > 1114111) {
      return ERR.characterReferenceOutsideUnicodeRange;
    } else if (isSurrogate(code)) {
      return ERR.surrogateCharacterReference;
    } else if (isUndefinedCodePoint(code)) {
      return ERR.noncharacterCharacterReference;
    } else if (isControlCodePoint(code) || code === CODE_POINTS.CARRIAGE_RETURN) {
      return ERR.controlCharacterReference;
    }
    return null;
  }
  var import_decode, State, TokenizerMode, _Tokenizer, Tokenizer;
  var init_tokenizer = __esm({
    "node_modules/parse5/dist/tokenizer/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_preprocessor();
      init_unicode();
      init_token();
      import_decode = __toESM(require_decode(), 1);
      init_error_codes();
      init_html();
      (function(State2) {
        State2[State2["DATA"] = 0] = "DATA";
        State2[State2["RCDATA"] = 1] = "RCDATA";
        State2[State2["RAWTEXT"] = 2] = "RAWTEXT";
        State2[State2["SCRIPT_DATA"] = 3] = "SCRIPT_DATA";
        State2[State2["PLAINTEXT"] = 4] = "PLAINTEXT";
        State2[State2["TAG_OPEN"] = 5] = "TAG_OPEN";
        State2[State2["END_TAG_OPEN"] = 6] = "END_TAG_OPEN";
        State2[State2["TAG_NAME"] = 7] = "TAG_NAME";
        State2[State2["RCDATA_LESS_THAN_SIGN"] = 8] = "RCDATA_LESS_THAN_SIGN";
        State2[State2["RCDATA_END_TAG_OPEN"] = 9] = "RCDATA_END_TAG_OPEN";
        State2[State2["RCDATA_END_TAG_NAME"] = 10] = "RCDATA_END_TAG_NAME";
        State2[State2["RAWTEXT_LESS_THAN_SIGN"] = 11] = "RAWTEXT_LESS_THAN_SIGN";
        State2[State2["RAWTEXT_END_TAG_OPEN"] = 12] = "RAWTEXT_END_TAG_OPEN";
        State2[State2["RAWTEXT_END_TAG_NAME"] = 13] = "RAWTEXT_END_TAG_NAME";
        State2[State2["SCRIPT_DATA_LESS_THAN_SIGN"] = 14] = "SCRIPT_DATA_LESS_THAN_SIGN";
        State2[State2["SCRIPT_DATA_END_TAG_OPEN"] = 15] = "SCRIPT_DATA_END_TAG_OPEN";
        State2[State2["SCRIPT_DATA_END_TAG_NAME"] = 16] = "SCRIPT_DATA_END_TAG_NAME";
        State2[State2["SCRIPT_DATA_ESCAPE_START"] = 17] = "SCRIPT_DATA_ESCAPE_START";
        State2[State2["SCRIPT_DATA_ESCAPE_START_DASH"] = 18] = "SCRIPT_DATA_ESCAPE_START_DASH";
        State2[State2["SCRIPT_DATA_ESCAPED"] = 19] = "SCRIPT_DATA_ESCAPED";
        State2[State2["SCRIPT_DATA_ESCAPED_DASH"] = 20] = "SCRIPT_DATA_ESCAPED_DASH";
        State2[State2["SCRIPT_DATA_ESCAPED_DASH_DASH"] = 21] = "SCRIPT_DATA_ESCAPED_DASH_DASH";
        State2[State2["SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN"] = 22] = "SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN";
        State2[State2["SCRIPT_DATA_ESCAPED_END_TAG_OPEN"] = 23] = "SCRIPT_DATA_ESCAPED_END_TAG_OPEN";
        State2[State2["SCRIPT_DATA_ESCAPED_END_TAG_NAME"] = 24] = "SCRIPT_DATA_ESCAPED_END_TAG_NAME";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPE_START"] = 25] = "SCRIPT_DATA_DOUBLE_ESCAPE_START";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPED"] = 26] = "SCRIPT_DATA_DOUBLE_ESCAPED";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPED_DASH"] = 27] = "SCRIPT_DATA_DOUBLE_ESCAPED_DASH";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH"] = 28] = "SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN"] = 29] = "SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN";
        State2[State2["SCRIPT_DATA_DOUBLE_ESCAPE_END"] = 30] = "SCRIPT_DATA_DOUBLE_ESCAPE_END";
        State2[State2["BEFORE_ATTRIBUTE_NAME"] = 31] = "BEFORE_ATTRIBUTE_NAME";
        State2[State2["ATTRIBUTE_NAME"] = 32] = "ATTRIBUTE_NAME";
        State2[State2["AFTER_ATTRIBUTE_NAME"] = 33] = "AFTER_ATTRIBUTE_NAME";
        State2[State2["BEFORE_ATTRIBUTE_VALUE"] = 34] = "BEFORE_ATTRIBUTE_VALUE";
        State2[State2["ATTRIBUTE_VALUE_DOUBLE_QUOTED"] = 35] = "ATTRIBUTE_VALUE_DOUBLE_QUOTED";
        State2[State2["ATTRIBUTE_VALUE_SINGLE_QUOTED"] = 36] = "ATTRIBUTE_VALUE_SINGLE_QUOTED";
        State2[State2["ATTRIBUTE_VALUE_UNQUOTED"] = 37] = "ATTRIBUTE_VALUE_UNQUOTED";
        State2[State2["AFTER_ATTRIBUTE_VALUE_QUOTED"] = 38] = "AFTER_ATTRIBUTE_VALUE_QUOTED";
        State2[State2["SELF_CLOSING_START_TAG"] = 39] = "SELF_CLOSING_START_TAG";
        State2[State2["BOGUS_COMMENT"] = 40] = "BOGUS_COMMENT";
        State2[State2["MARKUP_DECLARATION_OPEN"] = 41] = "MARKUP_DECLARATION_OPEN";
        State2[State2["COMMENT_START"] = 42] = "COMMENT_START";
        State2[State2["COMMENT_START_DASH"] = 43] = "COMMENT_START_DASH";
        State2[State2["COMMENT"] = 44] = "COMMENT";
        State2[State2["COMMENT_LESS_THAN_SIGN"] = 45] = "COMMENT_LESS_THAN_SIGN";
        State2[State2["COMMENT_LESS_THAN_SIGN_BANG"] = 46] = "COMMENT_LESS_THAN_SIGN_BANG";
        State2[State2["COMMENT_LESS_THAN_SIGN_BANG_DASH"] = 47] = "COMMENT_LESS_THAN_SIGN_BANG_DASH";
        State2[State2["COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH"] = 48] = "COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH";
        State2[State2["COMMENT_END_DASH"] = 49] = "COMMENT_END_DASH";
        State2[State2["COMMENT_END"] = 50] = "COMMENT_END";
        State2[State2["COMMENT_END_BANG"] = 51] = "COMMENT_END_BANG";
        State2[State2["DOCTYPE"] = 52] = "DOCTYPE";
        State2[State2["BEFORE_DOCTYPE_NAME"] = 53] = "BEFORE_DOCTYPE_NAME";
        State2[State2["DOCTYPE_NAME"] = 54] = "DOCTYPE_NAME";
        State2[State2["AFTER_DOCTYPE_NAME"] = 55] = "AFTER_DOCTYPE_NAME";
        State2[State2["AFTER_DOCTYPE_PUBLIC_KEYWORD"] = 56] = "AFTER_DOCTYPE_PUBLIC_KEYWORD";
        State2[State2["BEFORE_DOCTYPE_PUBLIC_IDENTIFIER"] = 57] = "BEFORE_DOCTYPE_PUBLIC_IDENTIFIER";
        State2[State2["DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED"] = 58] = "DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED";
        State2[State2["DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED"] = 59] = "DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED";
        State2[State2["AFTER_DOCTYPE_PUBLIC_IDENTIFIER"] = 60] = "AFTER_DOCTYPE_PUBLIC_IDENTIFIER";
        State2[State2["BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS"] = 61] = "BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS";
        State2[State2["AFTER_DOCTYPE_SYSTEM_KEYWORD"] = 62] = "AFTER_DOCTYPE_SYSTEM_KEYWORD";
        State2[State2["BEFORE_DOCTYPE_SYSTEM_IDENTIFIER"] = 63] = "BEFORE_DOCTYPE_SYSTEM_IDENTIFIER";
        State2[State2["DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED"] = 64] = "DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED";
        State2[State2["DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED"] = 65] = "DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED";
        State2[State2["AFTER_DOCTYPE_SYSTEM_IDENTIFIER"] = 66] = "AFTER_DOCTYPE_SYSTEM_IDENTIFIER";
        State2[State2["BOGUS_DOCTYPE"] = 67] = "BOGUS_DOCTYPE";
        State2[State2["CDATA_SECTION"] = 68] = "CDATA_SECTION";
        State2[State2["CDATA_SECTION_BRACKET"] = 69] = "CDATA_SECTION_BRACKET";
        State2[State2["CDATA_SECTION_END"] = 70] = "CDATA_SECTION_END";
        State2[State2["CHARACTER_REFERENCE"] = 71] = "CHARACTER_REFERENCE";
        State2[State2["AMBIGUOUS_AMPERSAND"] = 72] = "AMBIGUOUS_AMPERSAND";
      })(State || (State = {}));
      TokenizerMode = {
        DATA: State.DATA,
        RCDATA: State.RCDATA,
        RAWTEXT: State.RAWTEXT,
        SCRIPT_DATA: State.SCRIPT_DATA,
        PLAINTEXT: State.PLAINTEXT,
        CDATA_SECTION: State.CDATA_SECTION
      };
      __name(isAsciiDigit, "isAsciiDigit");
      __name(isAsciiUpper, "isAsciiUpper");
      __name(isAsciiLower, "isAsciiLower");
      __name(isAsciiLetter, "isAsciiLetter");
      __name(isAsciiAlphaNumeric, "isAsciiAlphaNumeric");
      __name(toAsciiLower, "toAsciiLower");
      __name(isWhitespace2, "isWhitespace");
      __name(isScriptDataDoubleEscapeSequenceEnd, "isScriptDataDoubleEscapeSequenceEnd");
      __name(getErrorForNumericCharacterReference, "getErrorForNumericCharacterReference");
      _Tokenizer = class _Tokenizer {
        constructor(options, handler) {
          this.options = options;
          this.handler = handler;
          this.paused = false;
          this.inLoop = false;
          this.inForeignNode = false;
          this.lastStartTagName = "";
          this.active = false;
          this.state = State.DATA;
          this.returnState = State.DATA;
          this.entityStartPos = 0;
          this.consumedAfterSnapshot = -1;
          this.currentCharacterToken = null;
          this.currentToken = null;
          this.currentAttr = { name: "", value: "" };
          this.preprocessor = new Preprocessor(handler);
          this.currentLocation = this.getCurrentLocation(-1);
          this.entityDecoder = new import_decode.EntityDecoder(import_decode.htmlDecodeTree, (cp, consumed) => {
            this.preprocessor.pos = this.entityStartPos + consumed - 1;
            this._flushCodePointConsumedAsCharacterReference(cp);
          }, handler.onParseError ? {
            missingSemicolonAfterCharacterReference: /* @__PURE__ */ __name(() => {
              this._err(ERR.missingSemicolonAfterCharacterReference, 1);
            }, "missingSemicolonAfterCharacterReference"),
            absenceOfDigitsInNumericCharacterReference: /* @__PURE__ */ __name((consumed) => {
              this._err(ERR.absenceOfDigitsInNumericCharacterReference, this.entityStartPos - this.preprocessor.pos + consumed);
            }, "absenceOfDigitsInNumericCharacterReference"),
            validateNumericCharacterReference: /* @__PURE__ */ __name((code) => {
              const error = getErrorForNumericCharacterReference(code);
              if (error)
                this._err(error, 1);
            }, "validateNumericCharacterReference")
          } : void 0);
        }
        //Errors
        _err(code, cpOffset = 0) {
          var _a, _b;
          (_b = (_a = this.handler).onParseError) === null || _b === void 0 ? void 0 : _b.call(_a, this.preprocessor.getError(code, cpOffset));
        }
        // NOTE: `offset` may never run across line boundaries.
        getCurrentLocation(offset) {
          if (!this.options.sourceCodeLocationInfo) {
            return null;
          }
          return {
            startLine: this.preprocessor.line,
            startCol: this.preprocessor.col - offset,
            startOffset: this.preprocessor.offset - offset,
            endLine: -1,
            endCol: -1,
            endOffset: -1
          };
        }
        _runParsingLoop() {
          if (this.inLoop)
            return;
          this.inLoop = true;
          while (this.active && !this.paused) {
            this.consumedAfterSnapshot = 0;
            const cp = this._consume();
            if (!this._ensureHibernation()) {
              this._callState(cp);
            }
          }
          this.inLoop = false;
        }
        //API
        pause() {
          this.paused = true;
        }
        resume(writeCallback) {
          if (!this.paused) {
            throw new Error("Parser was already resumed");
          }
          this.paused = false;
          if (this.inLoop)
            return;
          this._runParsingLoop();
          if (!this.paused) {
            writeCallback === null || writeCallback === void 0 ? void 0 : writeCallback();
          }
        }
        write(chunk, isLastChunk, writeCallback) {
          this.active = true;
          this.preprocessor.write(chunk, isLastChunk);
          this._runParsingLoop();
          if (!this.paused) {
            writeCallback === null || writeCallback === void 0 ? void 0 : writeCallback();
          }
        }
        insertHtmlAtCurrentPos(chunk) {
          this.active = true;
          this.preprocessor.insertHtmlAtCurrentPos(chunk);
          this._runParsingLoop();
        }
        //Hibernation
        _ensureHibernation() {
          if (this.preprocessor.endOfChunkHit) {
            this.preprocessor.retreat(this.consumedAfterSnapshot);
            this.consumedAfterSnapshot = 0;
            this.active = false;
            return true;
          }
          return false;
        }
        //Consumption
        _consume() {
          this.consumedAfterSnapshot++;
          return this.preprocessor.advance();
        }
        _advanceBy(count) {
          this.consumedAfterSnapshot += count;
          for (let i = 0; i < count; i++) {
            this.preprocessor.advance();
          }
        }
        _consumeSequenceIfMatch(pattern, caseSensitive) {
          if (this.preprocessor.startsWith(pattern, caseSensitive)) {
            this._advanceBy(pattern.length - 1);
            return true;
          }
          return false;
        }
        //Token creation
        _createStartTagToken() {
          this.currentToken = {
            type: TokenType.START_TAG,
            tagName: "",
            tagID: TAG_ID.UNKNOWN,
            selfClosing: false,
            ackSelfClosing: false,
            attrs: [],
            location: this.getCurrentLocation(1)
          };
        }
        _createEndTagToken() {
          this.currentToken = {
            type: TokenType.END_TAG,
            tagName: "",
            tagID: TAG_ID.UNKNOWN,
            selfClosing: false,
            ackSelfClosing: false,
            attrs: [],
            location: this.getCurrentLocation(2)
          };
        }
        _createCommentToken(offset) {
          this.currentToken = {
            type: TokenType.COMMENT,
            data: "",
            location: this.getCurrentLocation(offset)
          };
        }
        _createDoctypeToken(initialName) {
          this.currentToken = {
            type: TokenType.DOCTYPE,
            name: initialName,
            forceQuirks: false,
            publicId: null,
            systemId: null,
            location: this.currentLocation
          };
        }
        _createCharacterToken(type, chars) {
          this.currentCharacterToken = {
            type,
            chars,
            location: this.currentLocation
          };
        }
        //Tag attributes
        _createAttr(attrNameFirstCh) {
          this.currentAttr = {
            name: attrNameFirstCh,
            value: ""
          };
          this.currentLocation = this.getCurrentLocation(0);
        }
        _leaveAttrName() {
          var _a;
          var _b;
          const token = this.currentToken;
          if (getTokenAttr(token, this.currentAttr.name) === null) {
            token.attrs.push(this.currentAttr);
            if (token.location && this.currentLocation) {
              const attrLocations = (_a = (_b = token.location).attrs) !== null && _a !== void 0 ? _a : _b.attrs = /* @__PURE__ */ Object.create(null);
              attrLocations[this.currentAttr.name] = this.currentLocation;
              this._leaveAttrValue();
            }
          } else {
            this._err(ERR.duplicateAttribute);
          }
        }
        _leaveAttrValue() {
          if (this.currentLocation) {
            this.currentLocation.endLine = this.preprocessor.line;
            this.currentLocation.endCol = this.preprocessor.col;
            this.currentLocation.endOffset = this.preprocessor.offset;
          }
        }
        //Token emission
        prepareToken(ct) {
          this._emitCurrentCharacterToken(ct.location);
          this.currentToken = null;
          if (ct.location) {
            ct.location.endLine = this.preprocessor.line;
            ct.location.endCol = this.preprocessor.col + 1;
            ct.location.endOffset = this.preprocessor.offset + 1;
          }
          this.currentLocation = this.getCurrentLocation(-1);
        }
        emitCurrentTagToken() {
          const ct = this.currentToken;
          this.prepareToken(ct);
          ct.tagID = getTagID(ct.tagName);
          if (ct.type === TokenType.START_TAG) {
            this.lastStartTagName = ct.tagName;
            this.handler.onStartTag(ct);
          } else {
            if (ct.attrs.length > 0) {
              this._err(ERR.endTagWithAttributes);
            }
            if (ct.selfClosing) {
              this._err(ERR.endTagWithTrailingSolidus);
            }
            this.handler.onEndTag(ct);
          }
          this.preprocessor.dropParsedChunk();
        }
        emitCurrentComment(ct) {
          this.prepareToken(ct);
          this.handler.onComment(ct);
          this.preprocessor.dropParsedChunk();
        }
        emitCurrentDoctype(ct) {
          this.prepareToken(ct);
          this.handler.onDoctype(ct);
          this.preprocessor.dropParsedChunk();
        }
        _emitCurrentCharacterToken(nextLocation) {
          if (this.currentCharacterToken) {
            if (nextLocation && this.currentCharacterToken.location) {
              this.currentCharacterToken.location.endLine = nextLocation.startLine;
              this.currentCharacterToken.location.endCol = nextLocation.startCol;
              this.currentCharacterToken.location.endOffset = nextLocation.startOffset;
            }
            switch (this.currentCharacterToken.type) {
              case TokenType.CHARACTER: {
                this.handler.onCharacter(this.currentCharacterToken);
                break;
              }
              case TokenType.NULL_CHARACTER: {
                this.handler.onNullCharacter(this.currentCharacterToken);
                break;
              }
              case TokenType.WHITESPACE_CHARACTER: {
                this.handler.onWhitespaceCharacter(this.currentCharacterToken);
                break;
              }
            }
            this.currentCharacterToken = null;
          }
        }
        _emitEOFToken() {
          const location = this.getCurrentLocation(0);
          if (location) {
            location.endLine = location.startLine;
            location.endCol = location.startCol;
            location.endOffset = location.startOffset;
          }
          this._emitCurrentCharacterToken(location);
          this.handler.onEof({ type: TokenType.EOF, location });
          this.active = false;
        }
        //Characters emission
        //OPTIMIZATION: The specification uses only one type of character token (one token per character).
        //This causes a huge memory overhead and a lot of unnecessary parser loops. parse5 uses 3 groups of characters.
        //If we have a sequence of characters that belong to the same group, the parser can process it
        //as a single solid character token.
        //So, there are 3 types of character tokens in parse5:
        //1)TokenType.NULL_CHARACTER - \u0000-character sequences (e.g. '\u0000\u0000\u0000')
        //2)TokenType.WHITESPACE_CHARACTER - any whitespace/new-line character sequences (e.g. '\n  \r\t   \f')
        //3)TokenType.CHARACTER - any character sequence which don't belong to groups 1 and 2 (e.g. 'abcdef1234@@#$%^')
        _appendCharToCurrentCharacterToken(type, ch) {
          if (this.currentCharacterToken) {
            if (this.currentCharacterToken.type === type) {
              this.currentCharacterToken.chars += ch;
              return;
            } else {
              this.currentLocation = this.getCurrentLocation(0);
              this._emitCurrentCharacterToken(this.currentLocation);
              this.preprocessor.dropParsedChunk();
            }
          }
          this._createCharacterToken(type, ch);
        }
        _emitCodePoint(cp) {
          const type = isWhitespace2(cp) ? TokenType.WHITESPACE_CHARACTER : cp === CODE_POINTS.NULL ? TokenType.NULL_CHARACTER : TokenType.CHARACTER;
          this._appendCharToCurrentCharacterToken(type, String.fromCodePoint(cp));
        }
        //NOTE: used when we emit characters explicitly.
        //This is always for non-whitespace and non-null characters, which allows us to avoid additional checks.
        _emitChars(ch) {
          this._appendCharToCurrentCharacterToken(TokenType.CHARACTER, ch);
        }
        // Character reference helpers
        _startCharacterReference() {
          this.returnState = this.state;
          this.state = State.CHARACTER_REFERENCE;
          this.entityStartPos = this.preprocessor.pos;
          this.entityDecoder.startEntity(this._isCharacterReferenceInAttribute() ? import_decode.DecodingMode.Attribute : import_decode.DecodingMode.Legacy);
        }
        _isCharacterReferenceInAttribute() {
          return this.returnState === State.ATTRIBUTE_VALUE_DOUBLE_QUOTED || this.returnState === State.ATTRIBUTE_VALUE_SINGLE_QUOTED || this.returnState === State.ATTRIBUTE_VALUE_UNQUOTED;
        }
        _flushCodePointConsumedAsCharacterReference(cp) {
          if (this._isCharacterReferenceInAttribute()) {
            this.currentAttr.value += String.fromCodePoint(cp);
          } else {
            this._emitCodePoint(cp);
          }
        }
        // Calling states this way turns out to be much faster than any other approach.
        _callState(cp) {
          switch (this.state) {
            case State.DATA: {
              this._stateData(cp);
              break;
            }
            case State.RCDATA: {
              this._stateRcdata(cp);
              break;
            }
            case State.RAWTEXT: {
              this._stateRawtext(cp);
              break;
            }
            case State.SCRIPT_DATA: {
              this._stateScriptData(cp);
              break;
            }
            case State.PLAINTEXT: {
              this._statePlaintext(cp);
              break;
            }
            case State.TAG_OPEN: {
              this._stateTagOpen(cp);
              break;
            }
            case State.END_TAG_OPEN: {
              this._stateEndTagOpen(cp);
              break;
            }
            case State.TAG_NAME: {
              this._stateTagName(cp);
              break;
            }
            case State.RCDATA_LESS_THAN_SIGN: {
              this._stateRcdataLessThanSign(cp);
              break;
            }
            case State.RCDATA_END_TAG_OPEN: {
              this._stateRcdataEndTagOpen(cp);
              break;
            }
            case State.RCDATA_END_TAG_NAME: {
              this._stateRcdataEndTagName(cp);
              break;
            }
            case State.RAWTEXT_LESS_THAN_SIGN: {
              this._stateRawtextLessThanSign(cp);
              break;
            }
            case State.RAWTEXT_END_TAG_OPEN: {
              this._stateRawtextEndTagOpen(cp);
              break;
            }
            case State.RAWTEXT_END_TAG_NAME: {
              this._stateRawtextEndTagName(cp);
              break;
            }
            case State.SCRIPT_DATA_LESS_THAN_SIGN: {
              this._stateScriptDataLessThanSign(cp);
              break;
            }
            case State.SCRIPT_DATA_END_TAG_OPEN: {
              this._stateScriptDataEndTagOpen(cp);
              break;
            }
            case State.SCRIPT_DATA_END_TAG_NAME: {
              this._stateScriptDataEndTagName(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPE_START: {
              this._stateScriptDataEscapeStart(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPE_START_DASH: {
              this._stateScriptDataEscapeStartDash(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED: {
              this._stateScriptDataEscaped(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED_DASH: {
              this._stateScriptDataEscapedDash(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED_DASH_DASH: {
              this._stateScriptDataEscapedDashDash(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN: {
              this._stateScriptDataEscapedLessThanSign(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED_END_TAG_OPEN: {
              this._stateScriptDataEscapedEndTagOpen(cp);
              break;
            }
            case State.SCRIPT_DATA_ESCAPED_END_TAG_NAME: {
              this._stateScriptDataEscapedEndTagName(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPE_START: {
              this._stateScriptDataDoubleEscapeStart(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPED: {
              this._stateScriptDataDoubleEscaped(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPED_DASH: {
              this._stateScriptDataDoubleEscapedDash(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH: {
              this._stateScriptDataDoubleEscapedDashDash(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN: {
              this._stateScriptDataDoubleEscapedLessThanSign(cp);
              break;
            }
            case State.SCRIPT_DATA_DOUBLE_ESCAPE_END: {
              this._stateScriptDataDoubleEscapeEnd(cp);
              break;
            }
            case State.BEFORE_ATTRIBUTE_NAME: {
              this._stateBeforeAttributeName(cp);
              break;
            }
            case State.ATTRIBUTE_NAME: {
              this._stateAttributeName(cp);
              break;
            }
            case State.AFTER_ATTRIBUTE_NAME: {
              this._stateAfterAttributeName(cp);
              break;
            }
            case State.BEFORE_ATTRIBUTE_VALUE: {
              this._stateBeforeAttributeValue(cp);
              break;
            }
            case State.ATTRIBUTE_VALUE_DOUBLE_QUOTED: {
              this._stateAttributeValueDoubleQuoted(cp);
              break;
            }
            case State.ATTRIBUTE_VALUE_SINGLE_QUOTED: {
              this._stateAttributeValueSingleQuoted(cp);
              break;
            }
            case State.ATTRIBUTE_VALUE_UNQUOTED: {
              this._stateAttributeValueUnquoted(cp);
              break;
            }
            case State.AFTER_ATTRIBUTE_VALUE_QUOTED: {
              this._stateAfterAttributeValueQuoted(cp);
              break;
            }
            case State.SELF_CLOSING_START_TAG: {
              this._stateSelfClosingStartTag(cp);
              break;
            }
            case State.BOGUS_COMMENT: {
              this._stateBogusComment(cp);
              break;
            }
            case State.MARKUP_DECLARATION_OPEN: {
              this._stateMarkupDeclarationOpen(cp);
              break;
            }
            case State.COMMENT_START: {
              this._stateCommentStart(cp);
              break;
            }
            case State.COMMENT_START_DASH: {
              this._stateCommentStartDash(cp);
              break;
            }
            case State.COMMENT: {
              this._stateComment(cp);
              break;
            }
            case State.COMMENT_LESS_THAN_SIGN: {
              this._stateCommentLessThanSign(cp);
              break;
            }
            case State.COMMENT_LESS_THAN_SIGN_BANG: {
              this._stateCommentLessThanSignBang(cp);
              break;
            }
            case State.COMMENT_LESS_THAN_SIGN_BANG_DASH: {
              this._stateCommentLessThanSignBangDash(cp);
              break;
            }
            case State.COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH: {
              this._stateCommentLessThanSignBangDashDash(cp);
              break;
            }
            case State.COMMENT_END_DASH: {
              this._stateCommentEndDash(cp);
              break;
            }
            case State.COMMENT_END: {
              this._stateCommentEnd(cp);
              break;
            }
            case State.COMMENT_END_BANG: {
              this._stateCommentEndBang(cp);
              break;
            }
            case State.DOCTYPE: {
              this._stateDoctype(cp);
              break;
            }
            case State.BEFORE_DOCTYPE_NAME: {
              this._stateBeforeDoctypeName(cp);
              break;
            }
            case State.DOCTYPE_NAME: {
              this._stateDoctypeName(cp);
              break;
            }
            case State.AFTER_DOCTYPE_NAME: {
              this._stateAfterDoctypeName(cp);
              break;
            }
            case State.AFTER_DOCTYPE_PUBLIC_KEYWORD: {
              this._stateAfterDoctypePublicKeyword(cp);
              break;
            }
            case State.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER: {
              this._stateBeforeDoctypePublicIdentifier(cp);
              break;
            }
            case State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED: {
              this._stateDoctypePublicIdentifierDoubleQuoted(cp);
              break;
            }
            case State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED: {
              this._stateDoctypePublicIdentifierSingleQuoted(cp);
              break;
            }
            case State.AFTER_DOCTYPE_PUBLIC_IDENTIFIER: {
              this._stateAfterDoctypePublicIdentifier(cp);
              break;
            }
            case State.BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS: {
              this._stateBetweenDoctypePublicAndSystemIdentifiers(cp);
              break;
            }
            case State.AFTER_DOCTYPE_SYSTEM_KEYWORD: {
              this._stateAfterDoctypeSystemKeyword(cp);
              break;
            }
            case State.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER: {
              this._stateBeforeDoctypeSystemIdentifier(cp);
              break;
            }
            case State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED: {
              this._stateDoctypeSystemIdentifierDoubleQuoted(cp);
              break;
            }
            case State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED: {
              this._stateDoctypeSystemIdentifierSingleQuoted(cp);
              break;
            }
            case State.AFTER_DOCTYPE_SYSTEM_IDENTIFIER: {
              this._stateAfterDoctypeSystemIdentifier(cp);
              break;
            }
            case State.BOGUS_DOCTYPE: {
              this._stateBogusDoctype(cp);
              break;
            }
            case State.CDATA_SECTION: {
              this._stateCdataSection(cp);
              break;
            }
            case State.CDATA_SECTION_BRACKET: {
              this._stateCdataSectionBracket(cp);
              break;
            }
            case State.CDATA_SECTION_END: {
              this._stateCdataSectionEnd(cp);
              break;
            }
            case State.CHARACTER_REFERENCE: {
              this._stateCharacterReference();
              break;
            }
            case State.AMBIGUOUS_AMPERSAND: {
              this._stateAmbiguousAmpersand(cp);
              break;
            }
            default: {
              throw new Error("Unknown state");
            }
          }
        }
        // State machine
        // Data state
        //------------------------------------------------------------------
        _stateData(cp) {
          switch (cp) {
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.TAG_OPEN;
              break;
            }
            case CODE_POINTS.AMPERSAND: {
              this._startCharacterReference();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitCodePoint(cp);
              break;
            }
            case CODE_POINTS.EOF: {
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        //  RCDATA state
        //------------------------------------------------------------------
        _stateRcdata(cp) {
          switch (cp) {
            case CODE_POINTS.AMPERSAND: {
              this._startCharacterReference();
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.RCDATA_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // RAWTEXT state
        //------------------------------------------------------------------
        _stateRawtext(cp) {
          switch (cp) {
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.RAWTEXT_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data state
        //------------------------------------------------------------------
        _stateScriptData(cp) {
          switch (cp) {
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // PLAINTEXT state
        //------------------------------------------------------------------
        _statePlaintext(cp) {
          switch (cp) {
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // Tag open state
        //------------------------------------------------------------------
        _stateTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this._createStartTagToken();
            this.state = State.TAG_NAME;
            this._stateTagName(cp);
          } else
            switch (cp) {
              case CODE_POINTS.EXCLAMATION_MARK: {
                this.state = State.MARKUP_DECLARATION_OPEN;
                break;
              }
              case CODE_POINTS.SOLIDUS: {
                this.state = State.END_TAG_OPEN;
                break;
              }
              case CODE_POINTS.QUESTION_MARK: {
                this._err(ERR.unexpectedQuestionMarkInsteadOfTagName);
                this._createCommentToken(1);
                this.state = State.BOGUS_COMMENT;
                this._stateBogusComment(cp);
                break;
              }
              case CODE_POINTS.EOF: {
                this._err(ERR.eofBeforeTagName);
                this._emitChars("<");
                this._emitEOFToken();
                break;
              }
              default: {
                this._err(ERR.invalidFirstCharacterOfTagName);
                this._emitChars("<");
                this.state = State.DATA;
                this._stateData(cp);
              }
            }
        }
        // End tag open state
        //------------------------------------------------------------------
        _stateEndTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this._createEndTagToken();
            this.state = State.TAG_NAME;
            this._stateTagName(cp);
          } else
            switch (cp) {
              case CODE_POINTS.GREATER_THAN_SIGN: {
                this._err(ERR.missingEndTagName);
                this.state = State.DATA;
                break;
              }
              case CODE_POINTS.EOF: {
                this._err(ERR.eofBeforeTagName);
                this._emitChars("</");
                this._emitEOFToken();
                break;
              }
              default: {
                this._err(ERR.invalidFirstCharacterOfTagName);
                this._createCommentToken(2);
                this.state = State.BOGUS_COMMENT;
                this._stateBogusComment(cp);
              }
            }
        }
        // Tag name state
        //------------------------------------------------------------------
        _stateTagName(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              break;
            }
            case CODE_POINTS.SOLIDUS: {
              this.state = State.SELF_CLOSING_START_TAG;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.tagName += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              token.tagName += String.fromCodePoint(isAsciiUpper(cp) ? toAsciiLower(cp) : cp);
            }
          }
        }
        // RCDATA less-than sign state
        //------------------------------------------------------------------
        _stateRcdataLessThanSign(cp) {
          if (cp === CODE_POINTS.SOLIDUS) {
            this.state = State.RCDATA_END_TAG_OPEN;
          } else {
            this._emitChars("<");
            this.state = State.RCDATA;
            this._stateRcdata(cp);
          }
        }
        // RCDATA end tag open state
        //------------------------------------------------------------------
        _stateRcdataEndTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this.state = State.RCDATA_END_TAG_NAME;
            this._stateRcdataEndTagName(cp);
          } else {
            this._emitChars("</");
            this.state = State.RCDATA;
            this._stateRcdata(cp);
          }
        }
        handleSpecialEndTag(_cp) {
          if (!this.preprocessor.startsWith(this.lastStartTagName, false)) {
            return !this._ensureHibernation();
          }
          this._createEndTagToken();
          const token = this.currentToken;
          token.tagName = this.lastStartTagName;
          const cp = this.preprocessor.peek(this.lastStartTagName.length);
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this._advanceBy(this.lastStartTagName.length);
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              return false;
            }
            case CODE_POINTS.SOLIDUS: {
              this._advanceBy(this.lastStartTagName.length);
              this.state = State.SELF_CLOSING_START_TAG;
              return false;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._advanceBy(this.lastStartTagName.length);
              this.emitCurrentTagToken();
              this.state = State.DATA;
              return false;
            }
            default: {
              return !this._ensureHibernation();
            }
          }
        }
        // RCDATA end tag name state
        //------------------------------------------------------------------
        _stateRcdataEndTagName(cp) {
          if (this.handleSpecialEndTag(cp)) {
            this._emitChars("</");
            this.state = State.RCDATA;
            this._stateRcdata(cp);
          }
        }
        // RAWTEXT less-than sign state
        //------------------------------------------------------------------
        _stateRawtextLessThanSign(cp) {
          if (cp === CODE_POINTS.SOLIDUS) {
            this.state = State.RAWTEXT_END_TAG_OPEN;
          } else {
            this._emitChars("<");
            this.state = State.RAWTEXT;
            this._stateRawtext(cp);
          }
        }
        // RAWTEXT end tag open state
        //------------------------------------------------------------------
        _stateRawtextEndTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this.state = State.RAWTEXT_END_TAG_NAME;
            this._stateRawtextEndTagName(cp);
          } else {
            this._emitChars("</");
            this.state = State.RAWTEXT;
            this._stateRawtext(cp);
          }
        }
        // RAWTEXT end tag name state
        //------------------------------------------------------------------
        _stateRawtextEndTagName(cp) {
          if (this.handleSpecialEndTag(cp)) {
            this._emitChars("</");
            this.state = State.RAWTEXT;
            this._stateRawtext(cp);
          }
        }
        // Script data less-than sign state
        //------------------------------------------------------------------
        _stateScriptDataLessThanSign(cp) {
          switch (cp) {
            case CODE_POINTS.SOLIDUS: {
              this.state = State.SCRIPT_DATA_END_TAG_OPEN;
              break;
            }
            case CODE_POINTS.EXCLAMATION_MARK: {
              this.state = State.SCRIPT_DATA_ESCAPE_START;
              this._emitChars("<!");
              break;
            }
            default: {
              this._emitChars("<");
              this.state = State.SCRIPT_DATA;
              this._stateScriptData(cp);
            }
          }
        }
        // Script data end tag open state
        //------------------------------------------------------------------
        _stateScriptDataEndTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this.state = State.SCRIPT_DATA_END_TAG_NAME;
            this._stateScriptDataEndTagName(cp);
          } else {
            this._emitChars("</");
            this.state = State.SCRIPT_DATA;
            this._stateScriptData(cp);
          }
        }
        // Script data end tag name state
        //------------------------------------------------------------------
        _stateScriptDataEndTagName(cp) {
          if (this.handleSpecialEndTag(cp)) {
            this._emitChars("</");
            this.state = State.SCRIPT_DATA;
            this._stateScriptData(cp);
          }
        }
        // Script data escape start state
        //------------------------------------------------------------------
        _stateScriptDataEscapeStart(cp) {
          if (cp === CODE_POINTS.HYPHEN_MINUS) {
            this.state = State.SCRIPT_DATA_ESCAPE_START_DASH;
            this._emitChars("-");
          } else {
            this.state = State.SCRIPT_DATA;
            this._stateScriptData(cp);
          }
        }
        // Script data escape start dash state
        //------------------------------------------------------------------
        _stateScriptDataEscapeStartDash(cp) {
          if (cp === CODE_POINTS.HYPHEN_MINUS) {
            this.state = State.SCRIPT_DATA_ESCAPED_DASH_DASH;
            this._emitChars("-");
          } else {
            this.state = State.SCRIPT_DATA;
            this._stateScriptData(cp);
          }
        }
        // Script data escaped state
        //------------------------------------------------------------------
        _stateScriptDataEscaped(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.SCRIPT_DATA_ESCAPED_DASH;
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data escaped dash state
        //------------------------------------------------------------------
        _stateScriptDataEscapedDash(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.SCRIPT_DATA_ESCAPED_DASH_DASH;
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.state = State.SCRIPT_DATA_ESCAPED;
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this.state = State.SCRIPT_DATA_ESCAPED;
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data escaped dash dash state
        //------------------------------------------------------------------
        _stateScriptDataEscapedDashDash(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.SCRIPT_DATA;
              this._emitChars(">");
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.state = State.SCRIPT_DATA_ESCAPED;
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this.state = State.SCRIPT_DATA_ESCAPED;
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data escaped less-than sign state
        //------------------------------------------------------------------
        _stateScriptDataEscapedLessThanSign(cp) {
          if (cp === CODE_POINTS.SOLIDUS) {
            this.state = State.SCRIPT_DATA_ESCAPED_END_TAG_OPEN;
          } else if (isAsciiLetter(cp)) {
            this._emitChars("<");
            this.state = State.SCRIPT_DATA_DOUBLE_ESCAPE_START;
            this._stateScriptDataDoubleEscapeStart(cp);
          } else {
            this._emitChars("<");
            this.state = State.SCRIPT_DATA_ESCAPED;
            this._stateScriptDataEscaped(cp);
          }
        }
        // Script data escaped end tag open state
        //------------------------------------------------------------------
        _stateScriptDataEscapedEndTagOpen(cp) {
          if (isAsciiLetter(cp)) {
            this.state = State.SCRIPT_DATA_ESCAPED_END_TAG_NAME;
            this._stateScriptDataEscapedEndTagName(cp);
          } else {
            this._emitChars("</");
            this.state = State.SCRIPT_DATA_ESCAPED;
            this._stateScriptDataEscaped(cp);
          }
        }
        // Script data escaped end tag name state
        //------------------------------------------------------------------
        _stateScriptDataEscapedEndTagName(cp) {
          if (this.handleSpecialEndTag(cp)) {
            this._emitChars("</");
            this.state = State.SCRIPT_DATA_ESCAPED;
            this._stateScriptDataEscaped(cp);
          }
        }
        // Script data double escape start state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscapeStart(cp) {
          if (this.preprocessor.startsWith(SEQUENCES.SCRIPT, false) && isScriptDataDoubleEscapeSequenceEnd(this.preprocessor.peek(SEQUENCES.SCRIPT.length))) {
            this._emitCodePoint(cp);
            for (let i = 0; i < SEQUENCES.SCRIPT.length; i++) {
              this._emitCodePoint(this._consume());
            }
            this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
          } else if (!this._ensureHibernation()) {
            this.state = State.SCRIPT_DATA_ESCAPED;
            this._stateScriptDataEscaped(cp);
          }
        }
        // Script data double escaped state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscaped(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED_DASH;
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN;
              this._emitChars("<");
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data double escaped dash state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscapedDash(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH;
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN;
              this._emitChars("<");
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data double escaped dash dash state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscapedDashDash(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this._emitChars("-");
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN;
              this._emitChars("<");
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.SCRIPT_DATA;
              this._emitChars(">");
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
              this._emitChars(REPLACEMENT_CHARACTER);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInScriptHtmlCommentLikeText);
              this._emitEOFToken();
              break;
            }
            default: {
              this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
              this._emitCodePoint(cp);
            }
          }
        }
        // Script data double escaped less-than sign state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscapedLessThanSign(cp) {
          if (cp === CODE_POINTS.SOLIDUS) {
            this.state = State.SCRIPT_DATA_DOUBLE_ESCAPE_END;
            this._emitChars("/");
          } else {
            this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
            this._stateScriptDataDoubleEscaped(cp);
          }
        }
        // Script data double escape end state
        //------------------------------------------------------------------
        _stateScriptDataDoubleEscapeEnd(cp) {
          if (this.preprocessor.startsWith(SEQUENCES.SCRIPT, false) && isScriptDataDoubleEscapeSequenceEnd(this.preprocessor.peek(SEQUENCES.SCRIPT.length))) {
            this._emitCodePoint(cp);
            for (let i = 0; i < SEQUENCES.SCRIPT.length; i++) {
              this._emitCodePoint(this._consume());
            }
            this.state = State.SCRIPT_DATA_ESCAPED;
          } else if (!this._ensureHibernation()) {
            this.state = State.SCRIPT_DATA_DOUBLE_ESCAPED;
            this._stateScriptDataDoubleEscaped(cp);
          }
        }
        // Before attribute name state
        //------------------------------------------------------------------
        _stateBeforeAttributeName(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.SOLIDUS:
            case CODE_POINTS.GREATER_THAN_SIGN:
            case CODE_POINTS.EOF: {
              this.state = State.AFTER_ATTRIBUTE_NAME;
              this._stateAfterAttributeName(cp);
              break;
            }
            case CODE_POINTS.EQUALS_SIGN: {
              this._err(ERR.unexpectedEqualsSignBeforeAttributeName);
              this._createAttr("=");
              this.state = State.ATTRIBUTE_NAME;
              break;
            }
            default: {
              this._createAttr("");
              this.state = State.ATTRIBUTE_NAME;
              this._stateAttributeName(cp);
            }
          }
        }
        // Attribute name state
        //------------------------------------------------------------------
        _stateAttributeName(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED:
            case CODE_POINTS.SOLIDUS:
            case CODE_POINTS.GREATER_THAN_SIGN:
            case CODE_POINTS.EOF: {
              this._leaveAttrName();
              this.state = State.AFTER_ATTRIBUTE_NAME;
              this._stateAfterAttributeName(cp);
              break;
            }
            case CODE_POINTS.EQUALS_SIGN: {
              this._leaveAttrName();
              this.state = State.BEFORE_ATTRIBUTE_VALUE;
              break;
            }
            case CODE_POINTS.QUOTATION_MARK:
            case CODE_POINTS.APOSTROPHE:
            case CODE_POINTS.LESS_THAN_SIGN: {
              this._err(ERR.unexpectedCharacterInAttributeName);
              this.currentAttr.name += String.fromCodePoint(cp);
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.currentAttr.name += REPLACEMENT_CHARACTER;
              break;
            }
            default: {
              this.currentAttr.name += String.fromCodePoint(isAsciiUpper(cp) ? toAsciiLower(cp) : cp);
            }
          }
        }
        // After attribute name state
        //------------------------------------------------------------------
        _stateAfterAttributeName(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.SOLIDUS: {
              this.state = State.SELF_CLOSING_START_TAG;
              break;
            }
            case CODE_POINTS.EQUALS_SIGN: {
              this.state = State.BEFORE_ATTRIBUTE_VALUE;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this._createAttr("");
              this.state = State.ATTRIBUTE_NAME;
              this._stateAttributeName(cp);
            }
          }
        }
        // Before attribute value state
        //------------------------------------------------------------------
        _stateBeforeAttributeValue(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              this.state = State.ATTRIBUTE_VALUE_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              this.state = State.ATTRIBUTE_VALUE_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.missingAttributeValue);
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            default: {
              this.state = State.ATTRIBUTE_VALUE_UNQUOTED;
              this._stateAttributeValueUnquoted(cp);
            }
          }
        }
        // Attribute value (double-quoted) state
        //------------------------------------------------------------------
        _stateAttributeValueDoubleQuoted(cp) {
          switch (cp) {
            case CODE_POINTS.QUOTATION_MARK: {
              this.state = State.AFTER_ATTRIBUTE_VALUE_QUOTED;
              break;
            }
            case CODE_POINTS.AMPERSAND: {
              this._startCharacterReference();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.currentAttr.value += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this.currentAttr.value += String.fromCodePoint(cp);
            }
          }
        }
        // Attribute value (single-quoted) state
        //------------------------------------------------------------------
        _stateAttributeValueSingleQuoted(cp) {
          switch (cp) {
            case CODE_POINTS.APOSTROPHE: {
              this.state = State.AFTER_ATTRIBUTE_VALUE_QUOTED;
              break;
            }
            case CODE_POINTS.AMPERSAND: {
              this._startCharacterReference();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.currentAttr.value += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this.currentAttr.value += String.fromCodePoint(cp);
            }
          }
        }
        // Attribute value (unquoted) state
        //------------------------------------------------------------------
        _stateAttributeValueUnquoted(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this._leaveAttrValue();
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              break;
            }
            case CODE_POINTS.AMPERSAND: {
              this._startCharacterReference();
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._leaveAttrValue();
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              this.currentAttr.value += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.QUOTATION_MARK:
            case CODE_POINTS.APOSTROPHE:
            case CODE_POINTS.LESS_THAN_SIGN:
            case CODE_POINTS.EQUALS_SIGN:
            case CODE_POINTS.GRAVE_ACCENT: {
              this._err(ERR.unexpectedCharacterInUnquotedAttributeValue);
              this.currentAttr.value += String.fromCodePoint(cp);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this.currentAttr.value += String.fromCodePoint(cp);
            }
          }
        }
        // After attribute value (quoted) state
        //------------------------------------------------------------------
        _stateAfterAttributeValueQuoted(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this._leaveAttrValue();
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              break;
            }
            case CODE_POINTS.SOLIDUS: {
              this._leaveAttrValue();
              this.state = State.SELF_CLOSING_START_TAG;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._leaveAttrValue();
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingWhitespaceBetweenAttributes);
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              this._stateBeforeAttributeName(cp);
            }
          }
        }
        // Self-closing start tag state
        //------------------------------------------------------------------
        _stateSelfClosingStartTag(cp) {
          switch (cp) {
            case CODE_POINTS.GREATER_THAN_SIGN: {
              const token = this.currentToken;
              token.selfClosing = true;
              this.state = State.DATA;
              this.emitCurrentTagToken();
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInTag);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.unexpectedSolidusInTag);
              this.state = State.BEFORE_ATTRIBUTE_NAME;
              this._stateBeforeAttributeName(cp);
            }
          }
        }
        // Bogus comment state
        //------------------------------------------------------------------
        _stateBogusComment(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentComment(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.data += REPLACEMENT_CHARACTER;
              break;
            }
            default: {
              token.data += String.fromCodePoint(cp);
            }
          }
        }
        // Markup declaration open state
        //------------------------------------------------------------------
        _stateMarkupDeclarationOpen(cp) {
          if (this._consumeSequenceIfMatch(SEQUENCES.DASH_DASH, true)) {
            this._createCommentToken(SEQUENCES.DASH_DASH.length + 1);
            this.state = State.COMMENT_START;
          } else if (this._consumeSequenceIfMatch(SEQUENCES.DOCTYPE, false)) {
            this.currentLocation = this.getCurrentLocation(SEQUENCES.DOCTYPE.length + 1);
            this.state = State.DOCTYPE;
          } else if (this._consumeSequenceIfMatch(SEQUENCES.CDATA_START, true)) {
            if (this.inForeignNode) {
              this.state = State.CDATA_SECTION;
            } else {
              this._err(ERR.cdataInHtmlContent);
              this._createCommentToken(SEQUENCES.CDATA_START.length + 1);
              this.currentToken.data = "[CDATA[";
              this.state = State.BOGUS_COMMENT;
            }
          } else if (!this._ensureHibernation()) {
            this._err(ERR.incorrectlyOpenedComment);
            this._createCommentToken(2);
            this.state = State.BOGUS_COMMENT;
            this._stateBogusComment(cp);
          }
        }
        // Comment start state
        //------------------------------------------------------------------
        _stateCommentStart(cp) {
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.COMMENT_START_DASH;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptClosingOfEmptyComment);
              this.state = State.DATA;
              const token = this.currentToken;
              this.emitCurrentComment(token);
              break;
            }
            default: {
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // Comment start dash state
        //------------------------------------------------------------------
        _stateCommentStartDash(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.COMMENT_END;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptClosingOfEmptyComment);
              this.state = State.DATA;
              this.emitCurrentComment(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInComment);
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.data += "-";
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // Comment state
        //------------------------------------------------------------------
        _stateComment(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.COMMENT_END_DASH;
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              token.data += "<";
              this.state = State.COMMENT_LESS_THAN_SIGN;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.data += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInComment);
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.data += String.fromCodePoint(cp);
            }
          }
        }
        // Comment less-than sign state
        //------------------------------------------------------------------
        _stateCommentLessThanSign(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.EXCLAMATION_MARK: {
              token.data += "!";
              this.state = State.COMMENT_LESS_THAN_SIGN_BANG;
              break;
            }
            case CODE_POINTS.LESS_THAN_SIGN: {
              token.data += "<";
              break;
            }
            default: {
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // Comment less-than sign bang state
        //------------------------------------------------------------------
        _stateCommentLessThanSignBang(cp) {
          if (cp === CODE_POINTS.HYPHEN_MINUS) {
            this.state = State.COMMENT_LESS_THAN_SIGN_BANG_DASH;
          } else {
            this.state = State.COMMENT;
            this._stateComment(cp);
          }
        }
        // Comment less-than sign bang dash state
        //------------------------------------------------------------------
        _stateCommentLessThanSignBangDash(cp) {
          if (cp === CODE_POINTS.HYPHEN_MINUS) {
            this.state = State.COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH;
          } else {
            this.state = State.COMMENT_END_DASH;
            this._stateCommentEndDash(cp);
          }
        }
        // Comment less-than sign bang dash dash state
        //------------------------------------------------------------------
        _stateCommentLessThanSignBangDashDash(cp) {
          if (cp !== CODE_POINTS.GREATER_THAN_SIGN && cp !== CODE_POINTS.EOF) {
            this._err(ERR.nestedComment);
          }
          this.state = State.COMMENT_END;
          this._stateCommentEnd(cp);
        }
        // Comment end dash state
        //------------------------------------------------------------------
        _stateCommentEndDash(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              this.state = State.COMMENT_END;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInComment);
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.data += "-";
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // Comment end state
        //------------------------------------------------------------------
        _stateCommentEnd(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentComment(token);
              break;
            }
            case CODE_POINTS.EXCLAMATION_MARK: {
              this.state = State.COMMENT_END_BANG;
              break;
            }
            case CODE_POINTS.HYPHEN_MINUS: {
              token.data += "-";
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInComment);
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.data += "--";
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // Comment end bang state
        //------------------------------------------------------------------
        _stateCommentEndBang(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.HYPHEN_MINUS: {
              token.data += "--!";
              this.state = State.COMMENT_END_DASH;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.incorrectlyClosedComment);
              this.state = State.DATA;
              this.emitCurrentComment(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInComment);
              this.emitCurrentComment(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.data += "--!";
              this.state = State.COMMENT;
              this._stateComment(cp);
            }
          }
        }
        // DOCTYPE state
        //------------------------------------------------------------------
        _stateDoctype(cp) {
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.BEFORE_DOCTYPE_NAME;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.BEFORE_DOCTYPE_NAME;
              this._stateBeforeDoctypeName(cp);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              this._createDoctypeToken(null);
              const token = this.currentToken;
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingWhitespaceBeforeDoctypeName);
              this.state = State.BEFORE_DOCTYPE_NAME;
              this._stateBeforeDoctypeName(cp);
            }
          }
        }
        // Before DOCTYPE name state
        //------------------------------------------------------------------
        _stateBeforeDoctypeName(cp) {
          if (isAsciiUpper(cp)) {
            this._createDoctypeToken(String.fromCharCode(toAsciiLower(cp)));
            this.state = State.DOCTYPE_NAME;
          } else
            switch (cp) {
              case CODE_POINTS.SPACE:
              case CODE_POINTS.LINE_FEED:
              case CODE_POINTS.TABULATION:
              case CODE_POINTS.FORM_FEED: {
                break;
              }
              case CODE_POINTS.NULL: {
                this._err(ERR.unexpectedNullCharacter);
                this._createDoctypeToken(REPLACEMENT_CHARACTER);
                this.state = State.DOCTYPE_NAME;
                break;
              }
              case CODE_POINTS.GREATER_THAN_SIGN: {
                this._err(ERR.missingDoctypeName);
                this._createDoctypeToken(null);
                const token = this.currentToken;
                token.forceQuirks = true;
                this.emitCurrentDoctype(token);
                this.state = State.DATA;
                break;
              }
              case CODE_POINTS.EOF: {
                this._err(ERR.eofInDoctype);
                this._createDoctypeToken(null);
                const token = this.currentToken;
                token.forceQuirks = true;
                this.emitCurrentDoctype(token);
                this._emitEOFToken();
                break;
              }
              default: {
                this._createDoctypeToken(String.fromCodePoint(cp));
                this.state = State.DOCTYPE_NAME;
              }
            }
        }
        // DOCTYPE name state
        //------------------------------------------------------------------
        _stateDoctypeName(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.AFTER_DOCTYPE_NAME;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.name += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.name += String.fromCodePoint(isAsciiUpper(cp) ? toAsciiLower(cp) : cp);
            }
          }
        }
        // After DOCTYPE name state
        //------------------------------------------------------------------
        _stateAfterDoctypeName(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              if (this._consumeSequenceIfMatch(SEQUENCES.PUBLIC, false)) {
                this.state = State.AFTER_DOCTYPE_PUBLIC_KEYWORD;
              } else if (this._consumeSequenceIfMatch(SEQUENCES.SYSTEM, false)) {
                this.state = State.AFTER_DOCTYPE_SYSTEM_KEYWORD;
              } else if (!this._ensureHibernation()) {
                this._err(ERR.invalidCharacterSequenceAfterDoctypeName);
                token.forceQuirks = true;
                this.state = State.BOGUS_DOCTYPE;
                this._stateBogusDoctype(cp);
              }
            }
          }
        }
        // After DOCTYPE public keyword state
        //------------------------------------------------------------------
        _stateAfterDoctypePublicKeyword(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER;
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              this._err(ERR.missingWhitespaceAfterDoctypePublicKeyword);
              token.publicId = "";
              this.state = State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              this._err(ERR.missingWhitespaceAfterDoctypePublicKeyword);
              token.publicId = "";
              this.state = State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.missingDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // Before DOCTYPE public identifier state
        //------------------------------------------------------------------
        _stateBeforeDoctypePublicIdentifier(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              token.publicId = "";
              this.state = State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              token.publicId = "";
              this.state = State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.missingDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // DOCTYPE public identifier (double-quoted) state
        //------------------------------------------------------------------
        _stateDoctypePublicIdentifierDoubleQuoted(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.QUOTATION_MARK: {
              this.state = State.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.publicId += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.publicId += String.fromCodePoint(cp);
            }
          }
        }
        // DOCTYPE public identifier (single-quoted) state
        //------------------------------------------------------------------
        _stateDoctypePublicIdentifierSingleQuoted(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.APOSTROPHE: {
              this.state = State.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.publicId += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptDoctypePublicIdentifier);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.publicId += String.fromCodePoint(cp);
            }
          }
        }
        // After DOCTYPE public identifier state
        //------------------------------------------------------------------
        _stateAfterDoctypePublicIdentifier(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              this._err(ERR.missingWhitespaceBetweenDoctypePublicAndSystemIdentifiers);
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              this._err(ERR.missingWhitespaceBetweenDoctypePublicAndSystemIdentifiers);
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // Between DOCTYPE public and system identifiers state
        //------------------------------------------------------------------
        _stateBetweenDoctypePublicAndSystemIdentifiers(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // After DOCTYPE system keyword state
        //------------------------------------------------------------------
        _stateAfterDoctypeSystemKeyword(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              this.state = State.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER;
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              this._err(ERR.missingWhitespaceAfterDoctypeSystemKeyword);
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              this._err(ERR.missingWhitespaceAfterDoctypeSystemKeyword);
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.missingDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // Before DOCTYPE system identifier state
        //------------------------------------------------------------------
        _stateBeforeDoctypeSystemIdentifier(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.QUOTATION_MARK: {
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
              break;
            }
            case CODE_POINTS.APOSTROPHE: {
              token.systemId = "";
              this.state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.missingDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.DATA;
              this.emitCurrentDoctype(token);
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.missingQuoteBeforeDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // DOCTYPE system identifier (double-quoted) state
        //------------------------------------------------------------------
        _stateDoctypeSystemIdentifierDoubleQuoted(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.QUOTATION_MARK: {
              this.state = State.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.systemId += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.systemId += String.fromCodePoint(cp);
            }
          }
        }
        // DOCTYPE system identifier (single-quoted) state
        //------------------------------------------------------------------
        _stateDoctypeSystemIdentifierSingleQuoted(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.APOSTROPHE: {
              this.state = State.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              token.systemId += REPLACEMENT_CHARACTER;
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this._err(ERR.abruptDoctypeSystemIdentifier);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              token.systemId += String.fromCodePoint(cp);
            }
          }
        }
        // After DOCTYPE system identifier state
        //------------------------------------------------------------------
        _stateAfterDoctypeSystemIdentifier(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.SPACE:
            case CODE_POINTS.LINE_FEED:
            case CODE_POINTS.TABULATION:
            case CODE_POINTS.FORM_FEED: {
              break;
            }
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInDoctype);
              token.forceQuirks = true;
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default: {
              this._err(ERR.unexpectedCharacterAfterDoctypeSystemIdentifier);
              this.state = State.BOGUS_DOCTYPE;
              this._stateBogusDoctype(cp);
            }
          }
        }
        // Bogus DOCTYPE state
        //------------------------------------------------------------------
        _stateBogusDoctype(cp) {
          const token = this.currentToken;
          switch (cp) {
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.emitCurrentDoctype(token);
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.NULL: {
              this._err(ERR.unexpectedNullCharacter);
              break;
            }
            case CODE_POINTS.EOF: {
              this.emitCurrentDoctype(token);
              this._emitEOFToken();
              break;
            }
            default:
          }
        }
        // CDATA section state
        //------------------------------------------------------------------
        _stateCdataSection(cp) {
          switch (cp) {
            case CODE_POINTS.RIGHT_SQUARE_BRACKET: {
              this.state = State.CDATA_SECTION_BRACKET;
              break;
            }
            case CODE_POINTS.EOF: {
              this._err(ERR.eofInCdata);
              this._emitEOFToken();
              break;
            }
            default: {
              this._emitCodePoint(cp);
            }
          }
        }
        // CDATA section bracket state
        //------------------------------------------------------------------
        _stateCdataSectionBracket(cp) {
          if (cp === CODE_POINTS.RIGHT_SQUARE_BRACKET) {
            this.state = State.CDATA_SECTION_END;
          } else {
            this._emitChars("]");
            this.state = State.CDATA_SECTION;
            this._stateCdataSection(cp);
          }
        }
        // CDATA section end state
        //------------------------------------------------------------------
        _stateCdataSectionEnd(cp) {
          switch (cp) {
            case CODE_POINTS.GREATER_THAN_SIGN: {
              this.state = State.DATA;
              break;
            }
            case CODE_POINTS.RIGHT_SQUARE_BRACKET: {
              this._emitChars("]");
              break;
            }
            default: {
              this._emitChars("]]");
              this.state = State.CDATA_SECTION;
              this._stateCdataSection(cp);
            }
          }
        }
        // Character reference state
        //------------------------------------------------------------------
        _stateCharacterReference() {
          let length = this.entityDecoder.write(this.preprocessor.html, this.preprocessor.pos);
          if (length < 0) {
            if (this.preprocessor.lastChunkWritten) {
              length = this.entityDecoder.end();
            } else {
              this.active = false;
              this.preprocessor.pos = this.preprocessor.html.length - 1;
              this.consumedAfterSnapshot = 0;
              this.preprocessor.endOfChunkHit = true;
              return;
            }
          }
          if (length === 0) {
            this.preprocessor.pos = this.entityStartPos;
            this._flushCodePointConsumedAsCharacterReference(CODE_POINTS.AMPERSAND);
            this.state = !this._isCharacterReferenceInAttribute() && isAsciiAlphaNumeric(this.preprocessor.peek(1)) ? State.AMBIGUOUS_AMPERSAND : this.returnState;
          } else {
            this.state = this.returnState;
          }
        }
        // Ambiguos ampersand state
        //------------------------------------------------------------------
        _stateAmbiguousAmpersand(cp) {
          if (isAsciiAlphaNumeric(cp)) {
            this._flushCodePointConsumedAsCharacterReference(cp);
          } else {
            if (cp === CODE_POINTS.SEMICOLON) {
              this._err(ERR.unknownNamedCharacterReference);
            }
            this.state = this.returnState;
            this._callState(cp);
          }
        }
      };
      __name(_Tokenizer, "Tokenizer");
      Tokenizer = _Tokenizer;
    }
  });

  // node_modules/parse5/dist/parser/open-element-stack.js
  var IMPLICIT_END_TAG_REQUIRED, IMPLICIT_END_TAG_REQUIRED_THOROUGHLY, SCOPING_ELEMENTS_HTML, SCOPING_ELEMENTS_HTML_LIST, SCOPING_ELEMENTS_HTML_BUTTON, SCOPING_ELEMENTS_MATHML, SCOPING_ELEMENTS_SVG, TABLE_ROW_CONTEXT, TABLE_BODY_CONTEXT, TABLE_CONTEXT, TABLE_CELLS, _OpenElementStack, OpenElementStack;
  var init_open_element_stack = __esm({
    "node_modules/parse5/dist/parser/open-element-stack.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_html();
      IMPLICIT_END_TAG_REQUIRED = /* @__PURE__ */ new Set([TAG_ID.DD, TAG_ID.DT, TAG_ID.LI, TAG_ID.OPTGROUP, TAG_ID.OPTION, TAG_ID.P, TAG_ID.RB, TAG_ID.RP, TAG_ID.RT, TAG_ID.RTC]);
      IMPLICIT_END_TAG_REQUIRED_THOROUGHLY = /* @__PURE__ */ new Set([
        ...IMPLICIT_END_TAG_REQUIRED,
        TAG_ID.CAPTION,
        TAG_ID.COLGROUP,
        TAG_ID.TBODY,
        TAG_ID.TD,
        TAG_ID.TFOOT,
        TAG_ID.TH,
        TAG_ID.THEAD,
        TAG_ID.TR
      ]);
      SCOPING_ELEMENTS_HTML = /* @__PURE__ */ new Set([
        TAG_ID.APPLET,
        TAG_ID.CAPTION,
        TAG_ID.HTML,
        TAG_ID.MARQUEE,
        TAG_ID.OBJECT,
        TAG_ID.TABLE,
        TAG_ID.TD,
        TAG_ID.TEMPLATE,
        TAG_ID.TH
      ]);
      SCOPING_ELEMENTS_HTML_LIST = /* @__PURE__ */ new Set([...SCOPING_ELEMENTS_HTML, TAG_ID.OL, TAG_ID.UL]);
      SCOPING_ELEMENTS_HTML_BUTTON = /* @__PURE__ */ new Set([...SCOPING_ELEMENTS_HTML, TAG_ID.BUTTON]);
      SCOPING_ELEMENTS_MATHML = /* @__PURE__ */ new Set([TAG_ID.ANNOTATION_XML, TAG_ID.MI, TAG_ID.MN, TAG_ID.MO, TAG_ID.MS, TAG_ID.MTEXT]);
      SCOPING_ELEMENTS_SVG = /* @__PURE__ */ new Set([TAG_ID.DESC, TAG_ID.FOREIGN_OBJECT, TAG_ID.TITLE]);
      TABLE_ROW_CONTEXT = /* @__PURE__ */ new Set([TAG_ID.TR, TAG_ID.TEMPLATE, TAG_ID.HTML]);
      TABLE_BODY_CONTEXT = /* @__PURE__ */ new Set([TAG_ID.TBODY, TAG_ID.TFOOT, TAG_ID.THEAD, TAG_ID.TEMPLATE, TAG_ID.HTML]);
      TABLE_CONTEXT = /* @__PURE__ */ new Set([TAG_ID.TABLE, TAG_ID.TEMPLATE, TAG_ID.HTML]);
      TABLE_CELLS = /* @__PURE__ */ new Set([TAG_ID.TD, TAG_ID.TH]);
      _OpenElementStack = class _OpenElementStack {
        get currentTmplContentOrNode() {
          return this._isInTemplate() ? this.treeAdapter.getTemplateContent(this.current) : this.current;
        }
        constructor(document, treeAdapter, handler) {
          this.treeAdapter = treeAdapter;
          this.handler = handler;
          this.items = [];
          this.tagIDs = [];
          this.stackTop = -1;
          this.tmplCount = 0;
          this.currentTagId = TAG_ID.UNKNOWN;
          this.current = document;
        }
        //Index of element
        _indexOf(element) {
          return this.items.lastIndexOf(element, this.stackTop);
        }
        //Update current element
        _isInTemplate() {
          return this.currentTagId === TAG_ID.TEMPLATE && this.treeAdapter.getNamespaceURI(this.current) === NS.HTML;
        }
        _updateCurrentElement() {
          this.current = this.items[this.stackTop];
          this.currentTagId = this.tagIDs[this.stackTop];
        }
        //Mutations
        push(element, tagID) {
          this.stackTop++;
          this.items[this.stackTop] = element;
          this.current = element;
          this.tagIDs[this.stackTop] = tagID;
          this.currentTagId = tagID;
          if (this._isInTemplate()) {
            this.tmplCount++;
          }
          this.handler.onItemPush(element, tagID, true);
        }
        pop() {
          const popped = this.current;
          if (this.tmplCount > 0 && this._isInTemplate()) {
            this.tmplCount--;
          }
          this.stackTop--;
          this._updateCurrentElement();
          this.handler.onItemPop(popped, true);
        }
        replace(oldElement, newElement) {
          const idx = this._indexOf(oldElement);
          this.items[idx] = newElement;
          if (idx === this.stackTop) {
            this.current = newElement;
          }
        }
        insertAfter(referenceElement, newElement, newElementID) {
          const insertionIdx = this._indexOf(referenceElement) + 1;
          this.items.splice(insertionIdx, 0, newElement);
          this.tagIDs.splice(insertionIdx, 0, newElementID);
          this.stackTop++;
          if (insertionIdx === this.stackTop) {
            this._updateCurrentElement();
          }
          this.handler.onItemPush(this.current, this.currentTagId, insertionIdx === this.stackTop);
        }
        popUntilTagNamePopped(tagName) {
          let targetIdx = this.stackTop + 1;
          do {
            targetIdx = this.tagIDs.lastIndexOf(tagName, targetIdx - 1);
          } while (targetIdx > 0 && this.treeAdapter.getNamespaceURI(this.items[targetIdx]) !== NS.HTML);
          this.shortenToLength(targetIdx < 0 ? 0 : targetIdx);
        }
        shortenToLength(idx) {
          while (this.stackTop >= idx) {
            const popped = this.current;
            if (this.tmplCount > 0 && this._isInTemplate()) {
              this.tmplCount -= 1;
            }
            this.stackTop--;
            this._updateCurrentElement();
            this.handler.onItemPop(popped, this.stackTop < idx);
          }
        }
        popUntilElementPopped(element) {
          const idx = this._indexOf(element);
          this.shortenToLength(idx < 0 ? 0 : idx);
        }
        popUntilPopped(tagNames, targetNS) {
          const idx = this._indexOfTagNames(tagNames, targetNS);
          this.shortenToLength(idx < 0 ? 0 : idx);
        }
        popUntilNumberedHeaderPopped() {
          this.popUntilPopped(NUMBERED_HEADERS, NS.HTML);
        }
        popUntilTableCellPopped() {
          this.popUntilPopped(TABLE_CELLS, NS.HTML);
        }
        popAllUpToHtmlElement() {
          this.tmplCount = 0;
          this.shortenToLength(1);
        }
        _indexOfTagNames(tagNames, namespace) {
          for (let i = this.stackTop; i >= 0; i--) {
            if (tagNames.has(this.tagIDs[i]) && this.treeAdapter.getNamespaceURI(this.items[i]) === namespace) {
              return i;
            }
          }
          return -1;
        }
        clearBackTo(tagNames, targetNS) {
          const idx = this._indexOfTagNames(tagNames, targetNS);
          this.shortenToLength(idx + 1);
        }
        clearBackToTableContext() {
          this.clearBackTo(TABLE_CONTEXT, NS.HTML);
        }
        clearBackToTableBodyContext() {
          this.clearBackTo(TABLE_BODY_CONTEXT, NS.HTML);
        }
        clearBackToTableRowContext() {
          this.clearBackTo(TABLE_ROW_CONTEXT, NS.HTML);
        }
        remove(element) {
          const idx = this._indexOf(element);
          if (idx >= 0) {
            if (idx === this.stackTop) {
              this.pop();
            } else {
              this.items.splice(idx, 1);
              this.tagIDs.splice(idx, 1);
              this.stackTop--;
              this._updateCurrentElement();
              this.handler.onItemPop(element, false);
            }
          }
        }
        //Search
        tryPeekProperlyNestedBodyElement() {
          return this.stackTop >= 1 && this.tagIDs[1] === TAG_ID.BODY ? this.items[1] : null;
        }
        contains(element) {
          return this._indexOf(element) > -1;
        }
        getCommonAncestor(element) {
          const elementIdx = this._indexOf(element) - 1;
          return elementIdx >= 0 ? this.items[elementIdx] : null;
        }
        isRootHtmlElementCurrent() {
          return this.stackTop === 0 && this.tagIDs[0] === TAG_ID.HTML;
        }
        //Element in scope
        hasInDynamicScope(tagName, htmlScope) {
          for (let i = this.stackTop; i >= 0; i--) {
            const tn = this.tagIDs[i];
            switch (this.treeAdapter.getNamespaceURI(this.items[i])) {
              case NS.HTML: {
                if (tn === tagName)
                  return true;
                if (htmlScope.has(tn))
                  return false;
                break;
              }
              case NS.SVG: {
                if (SCOPING_ELEMENTS_SVG.has(tn))
                  return false;
                break;
              }
              case NS.MATHML: {
                if (SCOPING_ELEMENTS_MATHML.has(tn))
                  return false;
                break;
              }
            }
          }
          return true;
        }
        hasInScope(tagName) {
          return this.hasInDynamicScope(tagName, SCOPING_ELEMENTS_HTML);
        }
        hasInListItemScope(tagName) {
          return this.hasInDynamicScope(tagName, SCOPING_ELEMENTS_HTML_LIST);
        }
        hasInButtonScope(tagName) {
          return this.hasInDynamicScope(tagName, SCOPING_ELEMENTS_HTML_BUTTON);
        }
        hasNumberedHeaderInScope() {
          for (let i = this.stackTop; i >= 0; i--) {
            const tn = this.tagIDs[i];
            switch (this.treeAdapter.getNamespaceURI(this.items[i])) {
              case NS.HTML: {
                if (NUMBERED_HEADERS.has(tn))
                  return true;
                if (SCOPING_ELEMENTS_HTML.has(tn))
                  return false;
                break;
              }
              case NS.SVG: {
                if (SCOPING_ELEMENTS_SVG.has(tn))
                  return false;
                break;
              }
              case NS.MATHML: {
                if (SCOPING_ELEMENTS_MATHML.has(tn))
                  return false;
                break;
              }
            }
          }
          return true;
        }
        hasInTableScope(tagName) {
          for (let i = this.stackTop; i >= 0; i--) {
            if (this.treeAdapter.getNamespaceURI(this.items[i]) !== NS.HTML) {
              continue;
            }
            switch (this.tagIDs[i]) {
              case tagName: {
                return true;
              }
              case TAG_ID.TABLE:
              case TAG_ID.HTML: {
                return false;
              }
            }
          }
          return true;
        }
        hasTableBodyContextInTableScope() {
          for (let i = this.stackTop; i >= 0; i--) {
            if (this.treeAdapter.getNamespaceURI(this.items[i]) !== NS.HTML) {
              continue;
            }
            switch (this.tagIDs[i]) {
              case TAG_ID.TBODY:
              case TAG_ID.THEAD:
              case TAG_ID.TFOOT: {
                return true;
              }
              case TAG_ID.TABLE:
              case TAG_ID.HTML: {
                return false;
              }
            }
          }
          return true;
        }
        hasInSelectScope(tagName) {
          for (let i = this.stackTop; i >= 0; i--) {
            if (this.treeAdapter.getNamespaceURI(this.items[i]) !== NS.HTML) {
              continue;
            }
            switch (this.tagIDs[i]) {
              case tagName: {
                return true;
              }
              case TAG_ID.OPTION:
              case TAG_ID.OPTGROUP: {
                break;
              }
              default: {
                return false;
              }
            }
          }
          return true;
        }
        //Implied end tags
        generateImpliedEndTags() {
          while (IMPLICIT_END_TAG_REQUIRED.has(this.currentTagId)) {
            this.pop();
          }
        }
        generateImpliedEndTagsThoroughly() {
          while (IMPLICIT_END_TAG_REQUIRED_THOROUGHLY.has(this.currentTagId)) {
            this.pop();
          }
        }
        generateImpliedEndTagsWithExclusion(exclusionId) {
          while (this.currentTagId !== exclusionId && IMPLICIT_END_TAG_REQUIRED_THOROUGHLY.has(this.currentTagId)) {
            this.pop();
          }
        }
      };
      __name(_OpenElementStack, "OpenElementStack");
      OpenElementStack = _OpenElementStack;
    }
  });

  // node_modules/parse5/dist/parser/formatting-element-list.js
  var NOAH_ARK_CAPACITY, EntryType, MARKER, _FormattingElementList, FormattingElementList;
  var init_formatting_element_list = __esm({
    "node_modules/parse5/dist/parser/formatting-element-list.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      NOAH_ARK_CAPACITY = 3;
      (function(EntryType2) {
        EntryType2[EntryType2["Marker"] = 0] = "Marker";
        EntryType2[EntryType2["Element"] = 1] = "Element";
      })(EntryType || (EntryType = {}));
      MARKER = { type: EntryType.Marker };
      _FormattingElementList = class _FormattingElementList {
        constructor(treeAdapter) {
          this.treeAdapter = treeAdapter;
          this.entries = [];
          this.bookmark = null;
        }
        //Noah Ark's condition
        //OPTIMIZATION: at first we try to find possible candidates for exclusion using
        //lightweight heuristics without thorough attributes check.
        _getNoahArkConditionCandidates(newElement, neAttrs) {
          const candidates = [];
          const neAttrsLength = neAttrs.length;
          const neTagName = this.treeAdapter.getTagName(newElement);
          const neNamespaceURI = this.treeAdapter.getNamespaceURI(newElement);
          for (let i = 0; i < this.entries.length; i++) {
            const entry = this.entries[i];
            if (entry.type === EntryType.Marker) {
              break;
            }
            const { element } = entry;
            if (this.treeAdapter.getTagName(element) === neTagName && this.treeAdapter.getNamespaceURI(element) === neNamespaceURI) {
              const elementAttrs = this.treeAdapter.getAttrList(element);
              if (elementAttrs.length === neAttrsLength) {
                candidates.push({ idx: i, attrs: elementAttrs });
              }
            }
          }
          return candidates;
        }
        _ensureNoahArkCondition(newElement) {
          if (this.entries.length < NOAH_ARK_CAPACITY)
            return;
          const neAttrs = this.treeAdapter.getAttrList(newElement);
          const candidates = this._getNoahArkConditionCandidates(newElement, neAttrs);
          if (candidates.length < NOAH_ARK_CAPACITY)
            return;
          const neAttrsMap = new Map(neAttrs.map((neAttr) => [neAttr.name, neAttr.value]));
          let validCandidates = 0;
          for (let i = 0; i < candidates.length; i++) {
            const candidate = candidates[i];
            if (candidate.attrs.every((cAttr) => neAttrsMap.get(cAttr.name) === cAttr.value)) {
              validCandidates += 1;
              if (validCandidates >= NOAH_ARK_CAPACITY) {
                this.entries.splice(candidate.idx, 1);
              }
            }
          }
        }
        //Mutations
        insertMarker() {
          this.entries.unshift(MARKER);
        }
        pushElement(element, token) {
          this._ensureNoahArkCondition(element);
          this.entries.unshift({
            type: EntryType.Element,
            element,
            token
          });
        }
        insertElementAfterBookmark(element, token) {
          const bookmarkIdx = this.entries.indexOf(this.bookmark);
          this.entries.splice(bookmarkIdx, 0, {
            type: EntryType.Element,
            element,
            token
          });
        }
        removeEntry(entry) {
          const entryIndex = this.entries.indexOf(entry);
          if (entryIndex >= 0) {
            this.entries.splice(entryIndex, 1);
          }
        }
        /**
         * Clears the list of formatting elements up to the last marker.
         *
         * @see https://html.spec.whatwg.org/multipage/parsing.html#clear-the-list-of-active-formatting-elements-up-to-the-last-marker
         */
        clearToLastMarker() {
          const markerIdx = this.entries.indexOf(MARKER);
          if (markerIdx >= 0) {
            this.entries.splice(0, markerIdx + 1);
          } else {
            this.entries.length = 0;
          }
        }
        //Search
        getElementEntryInScopeWithTagName(tagName) {
          const entry = this.entries.find((entry2) => entry2.type === EntryType.Marker || this.treeAdapter.getTagName(entry2.element) === tagName);
          return entry && entry.type === EntryType.Element ? entry : null;
        }
        getElementEntry(element) {
          return this.entries.find((entry) => entry.type === EntryType.Element && entry.element === element);
        }
      };
      __name(_FormattingElementList, "FormattingElementList");
      FormattingElementList = _FormattingElementList;
    }
  });

  // node_modules/parse5/dist/tree-adapters/default.js
  var defaultTreeAdapter;
  var init_default = __esm({
    "node_modules/parse5/dist/tree-adapters/default.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_html();
      defaultTreeAdapter = {
        //Node construction
        createDocument() {
          return {
            nodeName: "#document",
            mode: DOCUMENT_MODE.NO_QUIRKS,
            childNodes: []
          };
        },
        createDocumentFragment() {
          return {
            nodeName: "#document-fragment",
            childNodes: []
          };
        },
        createElement(tagName, namespaceURI, attrs) {
          return {
            nodeName: tagName,
            tagName,
            attrs,
            namespaceURI,
            childNodes: [],
            parentNode: null
          };
        },
        createCommentNode(data2) {
          return {
            nodeName: "#comment",
            data: data2,
            parentNode: null
          };
        },
        createTextNode(value) {
          return {
            nodeName: "#text",
            value,
            parentNode: null
          };
        },
        //Tree mutation
        appendChild(parentNode, newNode) {
          parentNode.childNodes.push(newNode);
          newNode.parentNode = parentNode;
        },
        insertBefore(parentNode, newNode, referenceNode) {
          const insertionIdx = parentNode.childNodes.indexOf(referenceNode);
          parentNode.childNodes.splice(insertionIdx, 0, newNode);
          newNode.parentNode = parentNode;
        },
        setTemplateContent(templateElement, contentElement) {
          templateElement.content = contentElement;
        },
        getTemplateContent(templateElement) {
          return templateElement.content;
        },
        setDocumentType(document, name, publicId, systemId) {
          const doctypeNode = document.childNodes.find((node) => node.nodeName === "#documentType");
          if (doctypeNode) {
            doctypeNode.name = name;
            doctypeNode.publicId = publicId;
            doctypeNode.systemId = systemId;
          } else {
            const node = {
              nodeName: "#documentType",
              name,
              publicId,
              systemId,
              parentNode: null
            };
            defaultTreeAdapter.appendChild(document, node);
          }
        },
        setDocumentMode(document, mode) {
          document.mode = mode;
        },
        getDocumentMode(document) {
          return document.mode;
        },
        detachNode(node) {
          if (node.parentNode) {
            const idx = node.parentNode.childNodes.indexOf(node);
            node.parentNode.childNodes.splice(idx, 1);
            node.parentNode = null;
          }
        },
        insertText(parentNode, text3) {
          if (parentNode.childNodes.length > 0) {
            const prevNode = parentNode.childNodes[parentNode.childNodes.length - 1];
            if (defaultTreeAdapter.isTextNode(prevNode)) {
              prevNode.value += text3;
              return;
            }
          }
          defaultTreeAdapter.appendChild(parentNode, defaultTreeAdapter.createTextNode(text3));
        },
        insertTextBefore(parentNode, text3, referenceNode) {
          const prevNode = parentNode.childNodes[parentNode.childNodes.indexOf(referenceNode) - 1];
          if (prevNode && defaultTreeAdapter.isTextNode(prevNode)) {
            prevNode.value += text3;
          } else {
            defaultTreeAdapter.insertBefore(parentNode, defaultTreeAdapter.createTextNode(text3), referenceNode);
          }
        },
        adoptAttributes(recipient, attrs) {
          const recipientAttrsMap = new Set(recipient.attrs.map((attr2) => attr2.name));
          for (let j = 0; j < attrs.length; j++) {
            if (!recipientAttrsMap.has(attrs[j].name)) {
              recipient.attrs.push(attrs[j]);
            }
          }
        },
        //Tree traversing
        getFirstChild(node) {
          return node.childNodes[0];
        },
        getChildNodes(node) {
          return node.childNodes;
        },
        getParentNode(node) {
          return node.parentNode;
        },
        getAttrList(element) {
          return element.attrs;
        },
        //Node data
        getTagName(element) {
          return element.tagName;
        },
        getNamespaceURI(element) {
          return element.namespaceURI;
        },
        getTextNodeContent(textNode) {
          return textNode.value;
        },
        getCommentNodeContent(commentNode) {
          return commentNode.data;
        },
        getDocumentTypeNodeName(doctypeNode) {
          return doctypeNode.name;
        },
        getDocumentTypeNodePublicId(doctypeNode) {
          return doctypeNode.publicId;
        },
        getDocumentTypeNodeSystemId(doctypeNode) {
          return doctypeNode.systemId;
        },
        //Node types
        isTextNode(node) {
          return node.nodeName === "#text";
        },
        isCommentNode(node) {
          return node.nodeName === "#comment";
        },
        isDocumentTypeNode(node) {
          return node.nodeName === "#documentType";
        },
        isElementNode(node) {
          return Object.prototype.hasOwnProperty.call(node, "tagName");
        },
        // Source code location
        setNodeSourceCodeLocation(node, location) {
          node.sourceCodeLocation = location;
        },
        getNodeSourceCodeLocation(node) {
          return node.sourceCodeLocation;
        },
        updateNodeSourceCodeLocation(node, endLocation) {
          node.sourceCodeLocation = { ...node.sourceCodeLocation, ...endLocation };
        }
      };
    }
  });

  // node_modules/parse5/dist/common/doctype.js
  function hasPrefix(publicId, prefixes) {
    return prefixes.some((prefix) => publicId.startsWith(prefix));
  }
  function isConforming(token) {
    return token.name === VALID_DOCTYPE_NAME && token.publicId === null && (token.systemId === null || token.systemId === VALID_SYSTEM_ID);
  }
  function getDocumentMode(token) {
    if (token.name !== VALID_DOCTYPE_NAME) {
      return DOCUMENT_MODE.QUIRKS;
    }
    const { systemId } = token;
    if (systemId && systemId.toLowerCase() === QUIRKS_MODE_SYSTEM_ID) {
      return DOCUMENT_MODE.QUIRKS;
    }
    let { publicId } = token;
    if (publicId !== null) {
      publicId = publicId.toLowerCase();
      if (QUIRKS_MODE_PUBLIC_IDS.has(publicId)) {
        return DOCUMENT_MODE.QUIRKS;
      }
      let prefixes = systemId === null ? QUIRKS_MODE_NO_SYSTEM_ID_PUBLIC_ID_PREFIXES : QUIRKS_MODE_PUBLIC_ID_PREFIXES;
      if (hasPrefix(publicId, prefixes)) {
        return DOCUMENT_MODE.QUIRKS;
      }
      prefixes = systemId === null ? LIMITED_QUIRKS_PUBLIC_ID_PREFIXES : LIMITED_QUIRKS_WITH_SYSTEM_ID_PUBLIC_ID_PREFIXES;
      if (hasPrefix(publicId, prefixes)) {
        return DOCUMENT_MODE.LIMITED_QUIRKS;
      }
    }
    return DOCUMENT_MODE.NO_QUIRKS;
  }
  var VALID_DOCTYPE_NAME, VALID_SYSTEM_ID, QUIRKS_MODE_SYSTEM_ID, QUIRKS_MODE_PUBLIC_ID_PREFIXES, QUIRKS_MODE_NO_SYSTEM_ID_PUBLIC_ID_PREFIXES, QUIRKS_MODE_PUBLIC_IDS, LIMITED_QUIRKS_PUBLIC_ID_PREFIXES, LIMITED_QUIRKS_WITH_SYSTEM_ID_PUBLIC_ID_PREFIXES;
  var init_doctype = __esm({
    "node_modules/parse5/dist/common/doctype.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_html();
      VALID_DOCTYPE_NAME = "html";
      VALID_SYSTEM_ID = "about:legacy-compat";
      QUIRKS_MODE_SYSTEM_ID = "http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd";
      QUIRKS_MODE_PUBLIC_ID_PREFIXES = [
        "+//silmaril//dtd html pro v0r11 19970101//",
        "-//as//dtd html 3.0 aswedit + extensions//",
        "-//advasoft ltd//dtd html 3.0 aswedit + extensions//",
        "-//ietf//dtd html 2.0 level 1//",
        "-//ietf//dtd html 2.0 level 2//",
        "-//ietf//dtd html 2.0 strict level 1//",
        "-//ietf//dtd html 2.0 strict level 2//",
        "-//ietf//dtd html 2.0 strict//",
        "-//ietf//dtd html 2.0//",
        "-//ietf//dtd html 2.1e//",
        "-//ietf//dtd html 3.0//",
        "-//ietf//dtd html 3.2 final//",
        "-//ietf//dtd html 3.2//",
        "-//ietf//dtd html 3//",
        "-//ietf//dtd html level 0//",
        "-//ietf//dtd html level 1//",
        "-//ietf//dtd html level 2//",
        "-//ietf//dtd html level 3//",
        "-//ietf//dtd html strict level 0//",
        "-//ietf//dtd html strict level 1//",
        "-//ietf//dtd html strict level 2//",
        "-//ietf//dtd html strict level 3//",
        "-//ietf//dtd html strict//",
        "-//ietf//dtd html//",
        "-//metrius//dtd metrius presentational//",
        "-//microsoft//dtd internet explorer 2.0 html strict//",
        "-//microsoft//dtd internet explorer 2.0 html//",
        "-//microsoft//dtd internet explorer 2.0 tables//",
        "-//microsoft//dtd internet explorer 3.0 html strict//",
        "-//microsoft//dtd internet explorer 3.0 html//",
        "-//microsoft//dtd internet explorer 3.0 tables//",
        "-//netscape comm. corp.//dtd html//",
        "-//netscape comm. corp.//dtd strict html//",
        "-//o'reilly and associates//dtd html 2.0//",
        "-//o'reilly and associates//dtd html extended 1.0//",
        "-//o'reilly and associates//dtd html extended relaxed 1.0//",
        "-//sq//dtd html 2.0 hotmetal + extensions//",
        "-//softquad software//dtd hotmetal pro 6.0::19990601::extensions to html 4.0//",
        "-//softquad//dtd hotmetal pro 4.0::19971010::extensions to html 4.0//",
        "-//spyglass//dtd html 2.0 extended//",
        "-//sun microsystems corp.//dtd hotjava html//",
        "-//sun microsystems corp.//dtd hotjava strict html//",
        "-//w3c//dtd html 3 1995-03-24//",
        "-//w3c//dtd html 3.2 draft//",
        "-//w3c//dtd html 3.2 final//",
        "-//w3c//dtd html 3.2//",
        "-//w3c//dtd html 3.2s draft//",
        "-//w3c//dtd html 4.0 frameset//",
        "-//w3c//dtd html 4.0 transitional//",
        "-//w3c//dtd html experimental 19960712//",
        "-//w3c//dtd html experimental 970421//",
        "-//w3c//dtd w3 html//",
        "-//w3o//dtd w3 html 3.0//",
        "-//webtechs//dtd mozilla html 2.0//",
        "-//webtechs//dtd mozilla html//"
      ];
      QUIRKS_MODE_NO_SYSTEM_ID_PUBLIC_ID_PREFIXES = [
        ...QUIRKS_MODE_PUBLIC_ID_PREFIXES,
        "-//w3c//dtd html 4.01 frameset//",
        "-//w3c//dtd html 4.01 transitional//"
      ];
      QUIRKS_MODE_PUBLIC_IDS = /* @__PURE__ */ new Set([
        "-//w3o//dtd w3 html strict 3.0//en//",
        "-/w3c/dtd html 4.0 transitional/en",
        "html"
      ]);
      LIMITED_QUIRKS_PUBLIC_ID_PREFIXES = ["-//w3c//dtd xhtml 1.0 frameset//", "-//w3c//dtd xhtml 1.0 transitional//"];
      LIMITED_QUIRKS_WITH_SYSTEM_ID_PUBLIC_ID_PREFIXES = [
        ...LIMITED_QUIRKS_PUBLIC_ID_PREFIXES,
        "-//w3c//dtd html 4.01 frameset//",
        "-//w3c//dtd html 4.01 transitional//"
      ];
      __name(hasPrefix, "hasPrefix");
      __name(isConforming, "isConforming");
      __name(getDocumentMode, "getDocumentMode");
    }
  });

  // node_modules/parse5/dist/common/foreign-content.js
  function causesExit(startTagToken) {
    const tn = startTagToken.tagID;
    const isFontWithAttrs = tn === TAG_ID.FONT && startTagToken.attrs.some(({ name }) => name === ATTRS.COLOR || name === ATTRS.SIZE || name === ATTRS.FACE);
    return isFontWithAttrs || EXITS_FOREIGN_CONTENT.has(tn);
  }
  function adjustTokenMathMLAttrs(token) {
    for (let i = 0; i < token.attrs.length; i++) {
      if (token.attrs[i].name === DEFINITION_URL_ATTR) {
        token.attrs[i].name = ADJUSTED_DEFINITION_URL_ATTR;
        break;
      }
    }
  }
  function adjustTokenSVGAttrs(token) {
    for (let i = 0; i < token.attrs.length; i++) {
      const adjustedAttrName = SVG_ATTRS_ADJUSTMENT_MAP.get(token.attrs[i].name);
      if (adjustedAttrName != null) {
        token.attrs[i].name = adjustedAttrName;
      }
    }
  }
  function adjustTokenXMLAttrs(token) {
    for (let i = 0; i < token.attrs.length; i++) {
      const adjustedAttrEntry = XML_ATTRS_ADJUSTMENT_MAP.get(token.attrs[i].name);
      if (adjustedAttrEntry) {
        token.attrs[i].prefix = adjustedAttrEntry.prefix;
        token.attrs[i].name = adjustedAttrEntry.name;
        token.attrs[i].namespace = adjustedAttrEntry.namespace;
      }
    }
  }
  function adjustTokenSVGTagName(token) {
    const adjustedTagName = SVG_TAG_NAMES_ADJUSTMENT_MAP.get(token.tagName);
    if (adjustedTagName != null) {
      token.tagName = adjustedTagName;
      token.tagID = getTagID(token.tagName);
    }
  }
  function isMathMLTextIntegrationPoint(tn, ns) {
    return ns === NS.MATHML && (tn === TAG_ID.MI || tn === TAG_ID.MO || tn === TAG_ID.MN || tn === TAG_ID.MS || tn === TAG_ID.MTEXT);
  }
  function isHtmlIntegrationPoint(tn, ns, attrs) {
    if (ns === NS.MATHML && tn === TAG_ID.ANNOTATION_XML) {
      for (let i = 0; i < attrs.length; i++) {
        if (attrs[i].name === ATTRS.ENCODING) {
          const value = attrs[i].value.toLowerCase();
          return value === MIME_TYPES.TEXT_HTML || value === MIME_TYPES.APPLICATION_XML;
        }
      }
    }
    return ns === NS.SVG && (tn === TAG_ID.FOREIGN_OBJECT || tn === TAG_ID.DESC || tn === TAG_ID.TITLE);
  }
  function isIntegrationPoint(tn, ns, attrs, foreignNS) {
    return (!foreignNS || foreignNS === NS.HTML) && isHtmlIntegrationPoint(tn, ns, attrs) || (!foreignNS || foreignNS === NS.MATHML) && isMathMLTextIntegrationPoint(tn, ns);
  }
  var MIME_TYPES, DEFINITION_URL_ATTR, ADJUSTED_DEFINITION_URL_ATTR, SVG_ATTRS_ADJUSTMENT_MAP, XML_ATTRS_ADJUSTMENT_MAP, SVG_TAG_NAMES_ADJUSTMENT_MAP, EXITS_FOREIGN_CONTENT;
  var init_foreign_content = __esm({
    "node_modules/parse5/dist/common/foreign-content.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_html();
      MIME_TYPES = {
        TEXT_HTML: "text/html",
        APPLICATION_XML: "application/xhtml+xml"
      };
      DEFINITION_URL_ATTR = "definitionurl";
      ADJUSTED_DEFINITION_URL_ATTR = "definitionURL";
      SVG_ATTRS_ADJUSTMENT_MAP = new Map([
        "attributeName",
        "attributeType",
        "baseFrequency",
        "baseProfile",
        "calcMode",
        "clipPathUnits",
        "diffuseConstant",
        "edgeMode",
        "filterUnits",
        "glyphRef",
        "gradientTransform",
        "gradientUnits",
        "kernelMatrix",
        "kernelUnitLength",
        "keyPoints",
        "keySplines",
        "keyTimes",
        "lengthAdjust",
        "limitingConeAngle",
        "markerHeight",
        "markerUnits",
        "markerWidth",
        "maskContentUnits",
        "maskUnits",
        "numOctaves",
        "pathLength",
        "patternContentUnits",
        "patternTransform",
        "patternUnits",
        "pointsAtX",
        "pointsAtY",
        "pointsAtZ",
        "preserveAlpha",
        "preserveAspectRatio",
        "primitiveUnits",
        "refX",
        "refY",
        "repeatCount",
        "repeatDur",
        "requiredExtensions",
        "requiredFeatures",
        "specularConstant",
        "specularExponent",
        "spreadMethod",
        "startOffset",
        "stdDeviation",
        "stitchTiles",
        "surfaceScale",
        "systemLanguage",
        "tableValues",
        "targetX",
        "targetY",
        "textLength",
        "viewBox",
        "viewTarget",
        "xChannelSelector",
        "yChannelSelector",
        "zoomAndPan"
      ].map((attr2) => [attr2.toLowerCase(), attr2]));
      XML_ATTRS_ADJUSTMENT_MAP = /* @__PURE__ */ new Map([
        ["xlink:actuate", { prefix: "xlink", name: "actuate", namespace: NS.XLINK }],
        ["xlink:arcrole", { prefix: "xlink", name: "arcrole", namespace: NS.XLINK }],
        ["xlink:href", { prefix: "xlink", name: "href", namespace: NS.XLINK }],
        ["xlink:role", { prefix: "xlink", name: "role", namespace: NS.XLINK }],
        ["xlink:show", { prefix: "xlink", name: "show", namespace: NS.XLINK }],
        ["xlink:title", { prefix: "xlink", name: "title", namespace: NS.XLINK }],
        ["xlink:type", { prefix: "xlink", name: "type", namespace: NS.XLINK }],
        ["xml:lang", { prefix: "xml", name: "lang", namespace: NS.XML }],
        ["xml:space", { prefix: "xml", name: "space", namespace: NS.XML }],
        ["xmlns", { prefix: "", name: "xmlns", namespace: NS.XMLNS }],
        ["xmlns:xlink", { prefix: "xmlns", name: "xlink", namespace: NS.XMLNS }]
      ]);
      SVG_TAG_NAMES_ADJUSTMENT_MAP = new Map([
        "altGlyph",
        "altGlyphDef",
        "altGlyphItem",
        "animateColor",
        "animateMotion",
        "animateTransform",
        "clipPath",
        "feBlend",
        "feColorMatrix",
        "feComponentTransfer",
        "feComposite",
        "feConvolveMatrix",
        "feDiffuseLighting",
        "feDisplacementMap",
        "feDistantLight",
        "feFlood",
        "feFuncA",
        "feFuncB",
        "feFuncG",
        "feFuncR",
        "feGaussianBlur",
        "feImage",
        "feMerge",
        "feMergeNode",
        "feMorphology",
        "feOffset",
        "fePointLight",
        "feSpecularLighting",
        "feSpotLight",
        "feTile",
        "feTurbulence",
        "foreignObject",
        "glyphRef",
        "linearGradient",
        "radialGradient",
        "textPath"
      ].map((tn) => [tn.toLowerCase(), tn]));
      EXITS_FOREIGN_CONTENT = /* @__PURE__ */ new Set([
        TAG_ID.B,
        TAG_ID.BIG,
        TAG_ID.BLOCKQUOTE,
        TAG_ID.BODY,
        TAG_ID.BR,
        TAG_ID.CENTER,
        TAG_ID.CODE,
        TAG_ID.DD,
        TAG_ID.DIV,
        TAG_ID.DL,
        TAG_ID.DT,
        TAG_ID.EM,
        TAG_ID.EMBED,
        TAG_ID.H1,
        TAG_ID.H2,
        TAG_ID.H3,
        TAG_ID.H4,
        TAG_ID.H5,
        TAG_ID.H6,
        TAG_ID.HEAD,
        TAG_ID.HR,
        TAG_ID.I,
        TAG_ID.IMG,
        TAG_ID.LI,
        TAG_ID.LISTING,
        TAG_ID.MENU,
        TAG_ID.META,
        TAG_ID.NOBR,
        TAG_ID.OL,
        TAG_ID.P,
        TAG_ID.PRE,
        TAG_ID.RUBY,
        TAG_ID.S,
        TAG_ID.SMALL,
        TAG_ID.SPAN,
        TAG_ID.STRONG,
        TAG_ID.STRIKE,
        TAG_ID.SUB,
        TAG_ID.SUP,
        TAG_ID.TABLE,
        TAG_ID.TT,
        TAG_ID.U,
        TAG_ID.UL,
        TAG_ID.VAR
      ]);
      __name(causesExit, "causesExit");
      __name(adjustTokenMathMLAttrs, "adjustTokenMathMLAttrs");
      __name(adjustTokenSVGAttrs, "adjustTokenSVGAttrs");
      __name(adjustTokenXMLAttrs, "adjustTokenXMLAttrs");
      __name(adjustTokenSVGTagName, "adjustTokenSVGTagName");
      __name(isMathMLTextIntegrationPoint, "isMathMLTextIntegrationPoint");
      __name(isHtmlIntegrationPoint, "isHtmlIntegrationPoint");
      __name(isIntegrationPoint, "isIntegrationPoint");
    }
  });

  // node_modules/parse5/dist/parser/index.js
  function aaObtainFormattingElementEntry(p, token) {
    let formattingElementEntry = p.activeFormattingElements.getElementEntryInScopeWithTagName(token.tagName);
    if (formattingElementEntry) {
      if (!p.openElements.contains(formattingElementEntry.element)) {
        p.activeFormattingElements.removeEntry(formattingElementEntry);
        formattingElementEntry = null;
      } else if (!p.openElements.hasInScope(token.tagID)) {
        formattingElementEntry = null;
      }
    } else {
      genericEndTagInBody(p, token);
    }
    return formattingElementEntry;
  }
  function aaObtainFurthestBlock(p, formattingElementEntry) {
    let furthestBlock = null;
    let idx = p.openElements.stackTop;
    for (; idx >= 0; idx--) {
      const element = p.openElements.items[idx];
      if (element === formattingElementEntry.element) {
        break;
      }
      if (p._isSpecialElement(element, p.openElements.tagIDs[idx])) {
        furthestBlock = element;
      }
    }
    if (!furthestBlock) {
      p.openElements.shortenToLength(idx < 0 ? 0 : idx);
      p.activeFormattingElements.removeEntry(formattingElementEntry);
    }
    return furthestBlock;
  }
  function aaInnerLoop(p, furthestBlock, formattingElement) {
    let lastElement = furthestBlock;
    let nextElement = p.openElements.getCommonAncestor(furthestBlock);
    for (let i = 0, element = nextElement; element !== formattingElement; i++, element = nextElement) {
      nextElement = p.openElements.getCommonAncestor(element);
      const elementEntry = p.activeFormattingElements.getElementEntry(element);
      const counterOverflow = elementEntry && i >= AA_INNER_LOOP_ITER;
      const shouldRemoveFromOpenElements = !elementEntry || counterOverflow;
      if (shouldRemoveFromOpenElements) {
        if (counterOverflow) {
          p.activeFormattingElements.removeEntry(elementEntry);
        }
        p.openElements.remove(element);
      } else {
        element = aaRecreateElementFromEntry(p, elementEntry);
        if (lastElement === furthestBlock) {
          p.activeFormattingElements.bookmark = elementEntry;
        }
        p.treeAdapter.detachNode(lastElement);
        p.treeAdapter.appendChild(element, lastElement);
        lastElement = element;
      }
    }
    return lastElement;
  }
  function aaRecreateElementFromEntry(p, elementEntry) {
    const ns = p.treeAdapter.getNamespaceURI(elementEntry.element);
    const newElement = p.treeAdapter.createElement(elementEntry.token.tagName, ns, elementEntry.token.attrs);
    p.openElements.replace(elementEntry.element, newElement);
    elementEntry.element = newElement;
    return newElement;
  }
  function aaInsertLastNodeInCommonAncestor(p, commonAncestor, lastElement) {
    const tn = p.treeAdapter.getTagName(commonAncestor);
    const tid = getTagID(tn);
    if (p._isElementCausesFosterParenting(tid)) {
      p._fosterParentElement(lastElement);
    } else {
      const ns = p.treeAdapter.getNamespaceURI(commonAncestor);
      if (tid === TAG_ID.TEMPLATE && ns === NS.HTML) {
        commonAncestor = p.treeAdapter.getTemplateContent(commonAncestor);
      }
      p.treeAdapter.appendChild(commonAncestor, lastElement);
    }
  }
  function aaReplaceFormattingElement(p, furthestBlock, formattingElementEntry) {
    const ns = p.treeAdapter.getNamespaceURI(formattingElementEntry.element);
    const { token } = formattingElementEntry;
    const newElement = p.treeAdapter.createElement(token.tagName, ns, token.attrs);
    p._adoptNodes(furthestBlock, newElement);
    p.treeAdapter.appendChild(furthestBlock, newElement);
    p.activeFormattingElements.insertElementAfterBookmark(newElement, token);
    p.activeFormattingElements.removeEntry(formattingElementEntry);
    p.openElements.remove(formattingElementEntry.element);
    p.openElements.insertAfter(furthestBlock, newElement, token.tagID);
  }
  function callAdoptionAgency(p, token) {
    for (let i = 0; i < AA_OUTER_LOOP_ITER; i++) {
      const formattingElementEntry = aaObtainFormattingElementEntry(p, token);
      if (!formattingElementEntry) {
        break;
      }
      const furthestBlock = aaObtainFurthestBlock(p, formattingElementEntry);
      if (!furthestBlock) {
        break;
      }
      p.activeFormattingElements.bookmark = formattingElementEntry;
      const lastElement = aaInnerLoop(p, furthestBlock, formattingElementEntry.element);
      const commonAncestor = p.openElements.getCommonAncestor(formattingElementEntry.element);
      p.treeAdapter.detachNode(lastElement);
      if (commonAncestor)
        aaInsertLastNodeInCommonAncestor(p, commonAncestor, lastElement);
      aaReplaceFormattingElement(p, furthestBlock, formattingElementEntry);
    }
  }
  function appendComment(p, token) {
    p._appendCommentNode(token, p.openElements.currentTmplContentOrNode);
  }
  function appendCommentToRootHtmlElement(p, token) {
    p._appendCommentNode(token, p.openElements.items[0]);
  }
  function appendCommentToDocument(p, token) {
    p._appendCommentNode(token, p.document);
  }
  function stopParsing(p, token) {
    p.stopped = true;
    if (token.location) {
      const target = p.fragmentContext ? 0 : 2;
      for (let i = p.openElements.stackTop; i >= target; i--) {
        p._setEndLocation(p.openElements.items[i], token);
      }
      if (!p.fragmentContext && p.openElements.stackTop >= 0) {
        const htmlElement = p.openElements.items[0];
        const htmlLocation = p.treeAdapter.getNodeSourceCodeLocation(htmlElement);
        if (htmlLocation && !htmlLocation.endTag) {
          p._setEndLocation(htmlElement, token);
          if (p.openElements.stackTop >= 1) {
            const bodyElement = p.openElements.items[1];
            const bodyLocation = p.treeAdapter.getNodeSourceCodeLocation(bodyElement);
            if (bodyLocation && !bodyLocation.endTag) {
              p._setEndLocation(bodyElement, token);
            }
          }
        }
      }
    }
  }
  function doctypeInInitialMode(p, token) {
    p._setDocumentType(token);
    const mode = token.forceQuirks ? DOCUMENT_MODE.QUIRKS : getDocumentMode(token);
    if (!isConforming(token)) {
      p._err(token, ERR.nonConformingDoctype);
    }
    p.treeAdapter.setDocumentMode(p.document, mode);
    p.insertionMode = InsertionMode.BEFORE_HTML;
  }
  function tokenInInitialMode(p, token) {
    p._err(token, ERR.missingDoctype, true);
    p.treeAdapter.setDocumentMode(p.document, DOCUMENT_MODE.QUIRKS);
    p.insertionMode = InsertionMode.BEFORE_HTML;
    p._processToken(token);
  }
  function startTagBeforeHtml(p, token) {
    if (token.tagID === TAG_ID.HTML) {
      p._insertElement(token, NS.HTML);
      p.insertionMode = InsertionMode.BEFORE_HEAD;
    } else {
      tokenBeforeHtml(p, token);
    }
  }
  function endTagBeforeHtml(p, token) {
    const tn = token.tagID;
    if (tn === TAG_ID.HTML || tn === TAG_ID.HEAD || tn === TAG_ID.BODY || tn === TAG_ID.BR) {
      tokenBeforeHtml(p, token);
    }
  }
  function tokenBeforeHtml(p, token) {
    p._insertFakeRootElement();
    p.insertionMode = InsertionMode.BEFORE_HEAD;
    p._processToken(token);
  }
  function startTagBeforeHead(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.HEAD: {
        p._insertElement(token, NS.HTML);
        p.headElement = p.openElements.current;
        p.insertionMode = InsertionMode.IN_HEAD;
        break;
      }
      default: {
        tokenBeforeHead(p, token);
      }
    }
  }
  function endTagBeforeHead(p, token) {
    const tn = token.tagID;
    if (tn === TAG_ID.HEAD || tn === TAG_ID.BODY || tn === TAG_ID.HTML || tn === TAG_ID.BR) {
      tokenBeforeHead(p, token);
    } else {
      p._err(token, ERR.endTagWithoutMatchingOpenElement);
    }
  }
  function tokenBeforeHead(p, token) {
    p._insertFakeElement(TAG_NAMES.HEAD, TAG_ID.HEAD);
    p.headElement = p.openElements.current;
    p.insertionMode = InsertionMode.IN_HEAD;
    p._processToken(token);
  }
  function startTagInHead(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.BASE:
      case TAG_ID.BASEFONT:
      case TAG_ID.BGSOUND:
      case TAG_ID.LINK:
      case TAG_ID.META: {
        p._appendElement(token, NS.HTML);
        token.ackSelfClosing = true;
        break;
      }
      case TAG_ID.TITLE: {
        p._switchToTextParsing(token, TokenizerMode.RCDATA);
        break;
      }
      case TAG_ID.NOSCRIPT: {
        if (p.options.scriptingEnabled) {
          p._switchToTextParsing(token, TokenizerMode.RAWTEXT);
        } else {
          p._insertElement(token, NS.HTML);
          p.insertionMode = InsertionMode.IN_HEAD_NO_SCRIPT;
        }
        break;
      }
      case TAG_ID.NOFRAMES:
      case TAG_ID.STYLE: {
        p._switchToTextParsing(token, TokenizerMode.RAWTEXT);
        break;
      }
      case TAG_ID.SCRIPT: {
        p._switchToTextParsing(token, TokenizerMode.SCRIPT_DATA);
        break;
      }
      case TAG_ID.TEMPLATE: {
        p._insertTemplate(token);
        p.activeFormattingElements.insertMarker();
        p.framesetOk = false;
        p.insertionMode = InsertionMode.IN_TEMPLATE;
        p.tmplInsertionModeStack.unshift(InsertionMode.IN_TEMPLATE);
        break;
      }
      case TAG_ID.HEAD: {
        p._err(token, ERR.misplacedStartTagForHeadElement);
        break;
      }
      default: {
        tokenInHead(p, token);
      }
    }
  }
  function endTagInHead(p, token) {
    switch (token.tagID) {
      case TAG_ID.HEAD: {
        p.openElements.pop();
        p.insertionMode = InsertionMode.AFTER_HEAD;
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.BR:
      case TAG_ID.HTML: {
        tokenInHead(p, token);
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      default: {
        p._err(token, ERR.endTagWithoutMatchingOpenElement);
      }
    }
  }
  function templateEndTagInHead(p, token) {
    if (p.openElements.tmplCount > 0) {
      p.openElements.generateImpliedEndTagsThoroughly();
      if (p.openElements.currentTagId !== TAG_ID.TEMPLATE) {
        p._err(token, ERR.closingOfElementWithOpenChildElements);
      }
      p.openElements.popUntilTagNamePopped(TAG_ID.TEMPLATE);
      p.activeFormattingElements.clearToLastMarker();
      p.tmplInsertionModeStack.shift();
      p._resetInsertionMode();
    } else {
      p._err(token, ERR.endTagWithoutMatchingOpenElement);
    }
  }
  function tokenInHead(p, token) {
    p.openElements.pop();
    p.insertionMode = InsertionMode.AFTER_HEAD;
    p._processToken(token);
  }
  function startTagInHeadNoScript(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.BASEFONT:
      case TAG_ID.BGSOUND:
      case TAG_ID.HEAD:
      case TAG_ID.LINK:
      case TAG_ID.META:
      case TAG_ID.NOFRAMES:
      case TAG_ID.STYLE: {
        startTagInHead(p, token);
        break;
      }
      case TAG_ID.NOSCRIPT: {
        p._err(token, ERR.nestedNoscriptInHead);
        break;
      }
      default: {
        tokenInHeadNoScript(p, token);
      }
    }
  }
  function endTagInHeadNoScript(p, token) {
    switch (token.tagID) {
      case TAG_ID.NOSCRIPT: {
        p.openElements.pop();
        p.insertionMode = InsertionMode.IN_HEAD;
        break;
      }
      case TAG_ID.BR: {
        tokenInHeadNoScript(p, token);
        break;
      }
      default: {
        p._err(token, ERR.endTagWithoutMatchingOpenElement);
      }
    }
  }
  function tokenInHeadNoScript(p, token) {
    const errCode = token.type === TokenType.EOF ? ERR.openElementsLeftAfterEof : ERR.disallowedContentInNoscriptInHead;
    p._err(token, errCode);
    p.openElements.pop();
    p.insertionMode = InsertionMode.IN_HEAD;
    p._processToken(token);
  }
  function startTagAfterHead(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.BODY: {
        p._insertElement(token, NS.HTML);
        p.framesetOk = false;
        p.insertionMode = InsertionMode.IN_BODY;
        break;
      }
      case TAG_ID.FRAMESET: {
        p._insertElement(token, NS.HTML);
        p.insertionMode = InsertionMode.IN_FRAMESET;
        break;
      }
      case TAG_ID.BASE:
      case TAG_ID.BASEFONT:
      case TAG_ID.BGSOUND:
      case TAG_ID.LINK:
      case TAG_ID.META:
      case TAG_ID.NOFRAMES:
      case TAG_ID.SCRIPT:
      case TAG_ID.STYLE:
      case TAG_ID.TEMPLATE:
      case TAG_ID.TITLE: {
        p._err(token, ERR.abandonedHeadElementChild);
        p.openElements.push(p.headElement, TAG_ID.HEAD);
        startTagInHead(p, token);
        p.openElements.remove(p.headElement);
        break;
      }
      case TAG_ID.HEAD: {
        p._err(token, ERR.misplacedStartTagForHeadElement);
        break;
      }
      default: {
        tokenAfterHead(p, token);
      }
    }
  }
  function endTagAfterHead(p, token) {
    switch (token.tagID) {
      case TAG_ID.BODY:
      case TAG_ID.HTML:
      case TAG_ID.BR: {
        tokenAfterHead(p, token);
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      default: {
        p._err(token, ERR.endTagWithoutMatchingOpenElement);
      }
    }
  }
  function tokenAfterHead(p, token) {
    p._insertFakeElement(TAG_NAMES.BODY, TAG_ID.BODY);
    p.insertionMode = InsertionMode.IN_BODY;
    modeInBody(p, token);
  }
  function modeInBody(p, token) {
    switch (token.type) {
      case TokenType.CHARACTER: {
        characterInBody(p, token);
        break;
      }
      case TokenType.WHITESPACE_CHARACTER: {
        whitespaceCharacterInBody(p, token);
        break;
      }
      case TokenType.COMMENT: {
        appendComment(p, token);
        break;
      }
      case TokenType.START_TAG: {
        startTagInBody(p, token);
        break;
      }
      case TokenType.END_TAG: {
        endTagInBody(p, token);
        break;
      }
      case TokenType.EOF: {
        eofInBody(p, token);
        break;
      }
      default:
    }
  }
  function whitespaceCharacterInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertCharacters(token);
  }
  function characterInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertCharacters(token);
    p.framesetOk = false;
  }
  function htmlStartTagInBody(p, token) {
    if (p.openElements.tmplCount === 0) {
      p.treeAdapter.adoptAttributes(p.openElements.items[0], token.attrs);
    }
  }
  function bodyStartTagInBody(p, token) {
    const bodyElement = p.openElements.tryPeekProperlyNestedBodyElement();
    if (bodyElement && p.openElements.tmplCount === 0) {
      p.framesetOk = false;
      p.treeAdapter.adoptAttributes(bodyElement, token.attrs);
    }
  }
  function framesetStartTagInBody(p, token) {
    const bodyElement = p.openElements.tryPeekProperlyNestedBodyElement();
    if (p.framesetOk && bodyElement) {
      p.treeAdapter.detachNode(bodyElement);
      p.openElements.popAllUpToHtmlElement();
      p._insertElement(token, NS.HTML);
      p.insertionMode = InsertionMode.IN_FRAMESET;
    }
  }
  function addressStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._insertElement(token, NS.HTML);
  }
  function numberedHeaderStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    if (NUMBERED_HEADERS.has(p.openElements.currentTagId)) {
      p.openElements.pop();
    }
    p._insertElement(token, NS.HTML);
  }
  function preStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._insertElement(token, NS.HTML);
    p.skipNextNewLine = true;
    p.framesetOk = false;
  }
  function formStartTagInBody(p, token) {
    const inTemplate = p.openElements.tmplCount > 0;
    if (!p.formElement || inTemplate) {
      if (p.openElements.hasInButtonScope(TAG_ID.P)) {
        p._closePElement();
      }
      p._insertElement(token, NS.HTML);
      if (!inTemplate) {
        p.formElement = p.openElements.current;
      }
    }
  }
  function listItemStartTagInBody(p, token) {
    p.framesetOk = false;
    const tn = token.tagID;
    for (let i = p.openElements.stackTop; i >= 0; i--) {
      const elementId = p.openElements.tagIDs[i];
      if (tn === TAG_ID.LI && elementId === TAG_ID.LI || (tn === TAG_ID.DD || tn === TAG_ID.DT) && (elementId === TAG_ID.DD || elementId === TAG_ID.DT)) {
        p.openElements.generateImpliedEndTagsWithExclusion(elementId);
        p.openElements.popUntilTagNamePopped(elementId);
        break;
      }
      if (elementId !== TAG_ID.ADDRESS && elementId !== TAG_ID.DIV && elementId !== TAG_ID.P && p._isSpecialElement(p.openElements.items[i], elementId)) {
        break;
      }
    }
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._insertElement(token, NS.HTML);
  }
  function plaintextStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._insertElement(token, NS.HTML);
    p.tokenizer.state = TokenizerMode.PLAINTEXT;
  }
  function buttonStartTagInBody(p, token) {
    if (p.openElements.hasInScope(TAG_ID.BUTTON)) {
      p.openElements.generateImpliedEndTags();
      p.openElements.popUntilTagNamePopped(TAG_ID.BUTTON);
    }
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
    p.framesetOk = false;
  }
  function aStartTagInBody(p, token) {
    const activeElementEntry = p.activeFormattingElements.getElementEntryInScopeWithTagName(TAG_NAMES.A);
    if (activeElementEntry) {
      callAdoptionAgency(p, token);
      p.openElements.remove(activeElementEntry.element);
      p.activeFormattingElements.removeEntry(activeElementEntry);
    }
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
    p.activeFormattingElements.pushElement(p.openElements.current, token);
  }
  function bStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
    p.activeFormattingElements.pushElement(p.openElements.current, token);
  }
  function nobrStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    if (p.openElements.hasInScope(TAG_ID.NOBR)) {
      callAdoptionAgency(p, token);
      p._reconstructActiveFormattingElements();
    }
    p._insertElement(token, NS.HTML);
    p.activeFormattingElements.pushElement(p.openElements.current, token);
  }
  function appletStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
    p.activeFormattingElements.insertMarker();
    p.framesetOk = false;
  }
  function tableStartTagInBody(p, token) {
    if (p.treeAdapter.getDocumentMode(p.document) !== DOCUMENT_MODE.QUIRKS && p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._insertElement(token, NS.HTML);
    p.framesetOk = false;
    p.insertionMode = InsertionMode.IN_TABLE;
  }
  function areaStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._appendElement(token, NS.HTML);
    p.framesetOk = false;
    token.ackSelfClosing = true;
  }
  function isHiddenInput(token) {
    const inputType = getTokenAttr(token, ATTRS.TYPE);
    return inputType != null && inputType.toLowerCase() === HIDDEN_INPUT_TYPE;
  }
  function inputStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._appendElement(token, NS.HTML);
    if (!isHiddenInput(token)) {
      p.framesetOk = false;
    }
    token.ackSelfClosing = true;
  }
  function paramStartTagInBody(p, token) {
    p._appendElement(token, NS.HTML);
    token.ackSelfClosing = true;
  }
  function hrStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._appendElement(token, NS.HTML);
    p.framesetOk = false;
    token.ackSelfClosing = true;
  }
  function imageStartTagInBody(p, token) {
    token.tagName = TAG_NAMES.IMG;
    token.tagID = TAG_ID.IMG;
    areaStartTagInBody(p, token);
  }
  function textareaStartTagInBody(p, token) {
    p._insertElement(token, NS.HTML);
    p.skipNextNewLine = true;
    p.tokenizer.state = TokenizerMode.RCDATA;
    p.originalInsertionMode = p.insertionMode;
    p.framesetOk = false;
    p.insertionMode = InsertionMode.TEXT;
  }
  function xmpStartTagInBody(p, token) {
    if (p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._closePElement();
    }
    p._reconstructActiveFormattingElements();
    p.framesetOk = false;
    p._switchToTextParsing(token, TokenizerMode.RAWTEXT);
  }
  function iframeStartTagInBody(p, token) {
    p.framesetOk = false;
    p._switchToTextParsing(token, TokenizerMode.RAWTEXT);
  }
  function rawTextStartTagInBody(p, token) {
    p._switchToTextParsing(token, TokenizerMode.RAWTEXT);
  }
  function selectStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
    p.framesetOk = false;
    p.insertionMode = p.insertionMode === InsertionMode.IN_TABLE || p.insertionMode === InsertionMode.IN_CAPTION || p.insertionMode === InsertionMode.IN_TABLE_BODY || p.insertionMode === InsertionMode.IN_ROW || p.insertionMode === InsertionMode.IN_CELL ? InsertionMode.IN_SELECT_IN_TABLE : InsertionMode.IN_SELECT;
  }
  function optgroupStartTagInBody(p, token) {
    if (p.openElements.currentTagId === TAG_ID.OPTION) {
      p.openElements.pop();
    }
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
  }
  function rbStartTagInBody(p, token) {
    if (p.openElements.hasInScope(TAG_ID.RUBY)) {
      p.openElements.generateImpliedEndTags();
    }
    p._insertElement(token, NS.HTML);
  }
  function rtStartTagInBody(p, token) {
    if (p.openElements.hasInScope(TAG_ID.RUBY)) {
      p.openElements.generateImpliedEndTagsWithExclusion(TAG_ID.RTC);
    }
    p._insertElement(token, NS.HTML);
  }
  function mathStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    adjustTokenMathMLAttrs(token);
    adjustTokenXMLAttrs(token);
    if (token.selfClosing) {
      p._appendElement(token, NS.MATHML);
    } else {
      p._insertElement(token, NS.MATHML);
    }
    token.ackSelfClosing = true;
  }
  function svgStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    adjustTokenSVGAttrs(token);
    adjustTokenXMLAttrs(token);
    if (token.selfClosing) {
      p._appendElement(token, NS.SVG);
    } else {
      p._insertElement(token, NS.SVG);
    }
    token.ackSelfClosing = true;
  }
  function genericStartTagInBody(p, token) {
    p._reconstructActiveFormattingElements();
    p._insertElement(token, NS.HTML);
  }
  function startTagInBody(p, token) {
    switch (token.tagID) {
      case TAG_ID.I:
      case TAG_ID.S:
      case TAG_ID.B:
      case TAG_ID.U:
      case TAG_ID.EM:
      case TAG_ID.TT:
      case TAG_ID.BIG:
      case TAG_ID.CODE:
      case TAG_ID.FONT:
      case TAG_ID.SMALL:
      case TAG_ID.STRIKE:
      case TAG_ID.STRONG: {
        bStartTagInBody(p, token);
        break;
      }
      case TAG_ID.A: {
        aStartTagInBody(p, token);
        break;
      }
      case TAG_ID.H1:
      case TAG_ID.H2:
      case TAG_ID.H3:
      case TAG_ID.H4:
      case TAG_ID.H5:
      case TAG_ID.H6: {
        numberedHeaderStartTagInBody(p, token);
        break;
      }
      case TAG_ID.P:
      case TAG_ID.DL:
      case TAG_ID.OL:
      case TAG_ID.UL:
      case TAG_ID.DIV:
      case TAG_ID.DIR:
      case TAG_ID.NAV:
      case TAG_ID.MAIN:
      case TAG_ID.MENU:
      case TAG_ID.ASIDE:
      case TAG_ID.CENTER:
      case TAG_ID.FIGURE:
      case TAG_ID.FOOTER:
      case TAG_ID.HEADER:
      case TAG_ID.HGROUP:
      case TAG_ID.DIALOG:
      case TAG_ID.DETAILS:
      case TAG_ID.ADDRESS:
      case TAG_ID.ARTICLE:
      case TAG_ID.SEARCH:
      case TAG_ID.SECTION:
      case TAG_ID.SUMMARY:
      case TAG_ID.FIELDSET:
      case TAG_ID.BLOCKQUOTE:
      case TAG_ID.FIGCAPTION: {
        addressStartTagInBody(p, token);
        break;
      }
      case TAG_ID.LI:
      case TAG_ID.DD:
      case TAG_ID.DT: {
        listItemStartTagInBody(p, token);
        break;
      }
      case TAG_ID.BR:
      case TAG_ID.IMG:
      case TAG_ID.WBR:
      case TAG_ID.AREA:
      case TAG_ID.EMBED:
      case TAG_ID.KEYGEN: {
        areaStartTagInBody(p, token);
        break;
      }
      case TAG_ID.HR: {
        hrStartTagInBody(p, token);
        break;
      }
      case TAG_ID.RB:
      case TAG_ID.RTC: {
        rbStartTagInBody(p, token);
        break;
      }
      case TAG_ID.RT:
      case TAG_ID.RP: {
        rtStartTagInBody(p, token);
        break;
      }
      case TAG_ID.PRE:
      case TAG_ID.LISTING: {
        preStartTagInBody(p, token);
        break;
      }
      case TAG_ID.XMP: {
        xmpStartTagInBody(p, token);
        break;
      }
      case TAG_ID.SVG: {
        svgStartTagInBody(p, token);
        break;
      }
      case TAG_ID.HTML: {
        htmlStartTagInBody(p, token);
        break;
      }
      case TAG_ID.BASE:
      case TAG_ID.LINK:
      case TAG_ID.META:
      case TAG_ID.STYLE:
      case TAG_ID.TITLE:
      case TAG_ID.SCRIPT:
      case TAG_ID.BGSOUND:
      case TAG_ID.BASEFONT:
      case TAG_ID.TEMPLATE: {
        startTagInHead(p, token);
        break;
      }
      case TAG_ID.BODY: {
        bodyStartTagInBody(p, token);
        break;
      }
      case TAG_ID.FORM: {
        formStartTagInBody(p, token);
        break;
      }
      case TAG_ID.NOBR: {
        nobrStartTagInBody(p, token);
        break;
      }
      case TAG_ID.MATH: {
        mathStartTagInBody(p, token);
        break;
      }
      case TAG_ID.TABLE: {
        tableStartTagInBody(p, token);
        break;
      }
      case TAG_ID.INPUT: {
        inputStartTagInBody(p, token);
        break;
      }
      case TAG_ID.PARAM:
      case TAG_ID.TRACK:
      case TAG_ID.SOURCE: {
        paramStartTagInBody(p, token);
        break;
      }
      case TAG_ID.IMAGE: {
        imageStartTagInBody(p, token);
        break;
      }
      case TAG_ID.BUTTON: {
        buttonStartTagInBody(p, token);
        break;
      }
      case TAG_ID.APPLET:
      case TAG_ID.OBJECT:
      case TAG_ID.MARQUEE: {
        appletStartTagInBody(p, token);
        break;
      }
      case TAG_ID.IFRAME: {
        iframeStartTagInBody(p, token);
        break;
      }
      case TAG_ID.SELECT: {
        selectStartTagInBody(p, token);
        break;
      }
      case TAG_ID.OPTION:
      case TAG_ID.OPTGROUP: {
        optgroupStartTagInBody(p, token);
        break;
      }
      case TAG_ID.NOEMBED:
      case TAG_ID.NOFRAMES: {
        rawTextStartTagInBody(p, token);
        break;
      }
      case TAG_ID.FRAMESET: {
        framesetStartTagInBody(p, token);
        break;
      }
      case TAG_ID.TEXTAREA: {
        textareaStartTagInBody(p, token);
        break;
      }
      case TAG_ID.NOSCRIPT: {
        if (p.options.scriptingEnabled) {
          rawTextStartTagInBody(p, token);
        } else {
          genericStartTagInBody(p, token);
        }
        break;
      }
      case TAG_ID.PLAINTEXT: {
        plaintextStartTagInBody(p, token);
        break;
      }
      case TAG_ID.COL:
      case TAG_ID.TH:
      case TAG_ID.TD:
      case TAG_ID.TR:
      case TAG_ID.HEAD:
      case TAG_ID.FRAME:
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD:
      case TAG_ID.CAPTION:
      case TAG_ID.COLGROUP: {
        break;
      }
      default: {
        genericStartTagInBody(p, token);
      }
    }
  }
  function bodyEndTagInBody(p, token) {
    if (p.openElements.hasInScope(TAG_ID.BODY)) {
      p.insertionMode = InsertionMode.AFTER_BODY;
      if (p.options.sourceCodeLocationInfo) {
        const bodyElement = p.openElements.tryPeekProperlyNestedBodyElement();
        if (bodyElement) {
          p._setEndLocation(bodyElement, token);
        }
      }
    }
  }
  function htmlEndTagInBody(p, token) {
    if (p.openElements.hasInScope(TAG_ID.BODY)) {
      p.insertionMode = InsertionMode.AFTER_BODY;
      endTagAfterBody(p, token);
    }
  }
  function addressEndTagInBody(p, token) {
    const tn = token.tagID;
    if (p.openElements.hasInScope(tn)) {
      p.openElements.generateImpliedEndTags();
      p.openElements.popUntilTagNamePopped(tn);
    }
  }
  function formEndTagInBody(p) {
    const inTemplate = p.openElements.tmplCount > 0;
    const { formElement } = p;
    if (!inTemplate) {
      p.formElement = null;
    }
    if ((formElement || inTemplate) && p.openElements.hasInScope(TAG_ID.FORM)) {
      p.openElements.generateImpliedEndTags();
      if (inTemplate) {
        p.openElements.popUntilTagNamePopped(TAG_ID.FORM);
      } else if (formElement) {
        p.openElements.remove(formElement);
      }
    }
  }
  function pEndTagInBody(p) {
    if (!p.openElements.hasInButtonScope(TAG_ID.P)) {
      p._insertFakeElement(TAG_NAMES.P, TAG_ID.P);
    }
    p._closePElement();
  }
  function liEndTagInBody(p) {
    if (p.openElements.hasInListItemScope(TAG_ID.LI)) {
      p.openElements.generateImpliedEndTagsWithExclusion(TAG_ID.LI);
      p.openElements.popUntilTagNamePopped(TAG_ID.LI);
    }
  }
  function ddEndTagInBody(p, token) {
    const tn = token.tagID;
    if (p.openElements.hasInScope(tn)) {
      p.openElements.generateImpliedEndTagsWithExclusion(tn);
      p.openElements.popUntilTagNamePopped(tn);
    }
  }
  function numberedHeaderEndTagInBody(p) {
    if (p.openElements.hasNumberedHeaderInScope()) {
      p.openElements.generateImpliedEndTags();
      p.openElements.popUntilNumberedHeaderPopped();
    }
  }
  function appletEndTagInBody(p, token) {
    const tn = token.tagID;
    if (p.openElements.hasInScope(tn)) {
      p.openElements.generateImpliedEndTags();
      p.openElements.popUntilTagNamePopped(tn);
      p.activeFormattingElements.clearToLastMarker();
    }
  }
  function brEndTagInBody(p) {
    p._reconstructActiveFormattingElements();
    p._insertFakeElement(TAG_NAMES.BR, TAG_ID.BR);
    p.openElements.pop();
    p.framesetOk = false;
  }
  function genericEndTagInBody(p, token) {
    const tn = token.tagName;
    const tid = token.tagID;
    for (let i = p.openElements.stackTop; i > 0; i--) {
      const element = p.openElements.items[i];
      const elementId = p.openElements.tagIDs[i];
      if (tid === elementId && (tid !== TAG_ID.UNKNOWN || p.treeAdapter.getTagName(element) === tn)) {
        p.openElements.generateImpliedEndTagsWithExclusion(tid);
        if (p.openElements.stackTop >= i)
          p.openElements.shortenToLength(i);
        break;
      }
      if (p._isSpecialElement(element, elementId)) {
        break;
      }
    }
  }
  function endTagInBody(p, token) {
    switch (token.tagID) {
      case TAG_ID.A:
      case TAG_ID.B:
      case TAG_ID.I:
      case TAG_ID.S:
      case TAG_ID.U:
      case TAG_ID.EM:
      case TAG_ID.TT:
      case TAG_ID.BIG:
      case TAG_ID.CODE:
      case TAG_ID.FONT:
      case TAG_ID.NOBR:
      case TAG_ID.SMALL:
      case TAG_ID.STRIKE:
      case TAG_ID.STRONG: {
        callAdoptionAgency(p, token);
        break;
      }
      case TAG_ID.P: {
        pEndTagInBody(p);
        break;
      }
      case TAG_ID.DL:
      case TAG_ID.UL:
      case TAG_ID.OL:
      case TAG_ID.DIR:
      case TAG_ID.DIV:
      case TAG_ID.NAV:
      case TAG_ID.PRE:
      case TAG_ID.MAIN:
      case TAG_ID.MENU:
      case TAG_ID.ASIDE:
      case TAG_ID.BUTTON:
      case TAG_ID.CENTER:
      case TAG_ID.FIGURE:
      case TAG_ID.FOOTER:
      case TAG_ID.HEADER:
      case TAG_ID.HGROUP:
      case TAG_ID.DIALOG:
      case TAG_ID.ADDRESS:
      case TAG_ID.ARTICLE:
      case TAG_ID.DETAILS:
      case TAG_ID.SEARCH:
      case TAG_ID.SECTION:
      case TAG_ID.SUMMARY:
      case TAG_ID.LISTING:
      case TAG_ID.FIELDSET:
      case TAG_ID.BLOCKQUOTE:
      case TAG_ID.FIGCAPTION: {
        addressEndTagInBody(p, token);
        break;
      }
      case TAG_ID.LI: {
        liEndTagInBody(p);
        break;
      }
      case TAG_ID.DD:
      case TAG_ID.DT: {
        ddEndTagInBody(p, token);
        break;
      }
      case TAG_ID.H1:
      case TAG_ID.H2:
      case TAG_ID.H3:
      case TAG_ID.H4:
      case TAG_ID.H5:
      case TAG_ID.H6: {
        numberedHeaderEndTagInBody(p);
        break;
      }
      case TAG_ID.BR: {
        brEndTagInBody(p);
        break;
      }
      case TAG_ID.BODY: {
        bodyEndTagInBody(p, token);
        break;
      }
      case TAG_ID.HTML: {
        htmlEndTagInBody(p, token);
        break;
      }
      case TAG_ID.FORM: {
        formEndTagInBody(p);
        break;
      }
      case TAG_ID.APPLET:
      case TAG_ID.OBJECT:
      case TAG_ID.MARQUEE: {
        appletEndTagInBody(p, token);
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      default: {
        genericEndTagInBody(p, token);
      }
    }
  }
  function eofInBody(p, token) {
    if (p.tmplInsertionModeStack.length > 0) {
      eofInTemplate(p, token);
    } else {
      stopParsing(p, token);
    }
  }
  function endTagInText(p, token) {
    var _a;
    if (token.tagID === TAG_ID.SCRIPT) {
      (_a = p.scriptHandler) === null || _a === void 0 ? void 0 : _a.call(p, p.openElements.current);
    }
    p.openElements.pop();
    p.insertionMode = p.originalInsertionMode;
  }
  function eofInText(p, token) {
    p._err(token, ERR.eofInElementThatCanContainOnlyText);
    p.openElements.pop();
    p.insertionMode = p.originalInsertionMode;
    p.onEof(token);
  }
  function characterInTable(p, token) {
    if (TABLE_STRUCTURE_TAGS.has(p.openElements.currentTagId)) {
      p.pendingCharacterTokens.length = 0;
      p.hasNonWhitespacePendingCharacterToken = false;
      p.originalInsertionMode = p.insertionMode;
      p.insertionMode = InsertionMode.IN_TABLE_TEXT;
      switch (token.type) {
        case TokenType.CHARACTER: {
          characterInTableText(p, token);
          break;
        }
        case TokenType.WHITESPACE_CHARACTER: {
          whitespaceCharacterInTableText(p, token);
          break;
        }
      }
    } else {
      tokenInTable(p, token);
    }
  }
  function captionStartTagInTable(p, token) {
    p.openElements.clearBackToTableContext();
    p.activeFormattingElements.insertMarker();
    p._insertElement(token, NS.HTML);
    p.insertionMode = InsertionMode.IN_CAPTION;
  }
  function colgroupStartTagInTable(p, token) {
    p.openElements.clearBackToTableContext();
    p._insertElement(token, NS.HTML);
    p.insertionMode = InsertionMode.IN_COLUMN_GROUP;
  }
  function colStartTagInTable(p, token) {
    p.openElements.clearBackToTableContext();
    p._insertFakeElement(TAG_NAMES.COLGROUP, TAG_ID.COLGROUP);
    p.insertionMode = InsertionMode.IN_COLUMN_GROUP;
    startTagInColumnGroup(p, token);
  }
  function tbodyStartTagInTable(p, token) {
    p.openElements.clearBackToTableContext();
    p._insertElement(token, NS.HTML);
    p.insertionMode = InsertionMode.IN_TABLE_BODY;
  }
  function tdStartTagInTable(p, token) {
    p.openElements.clearBackToTableContext();
    p._insertFakeElement(TAG_NAMES.TBODY, TAG_ID.TBODY);
    p.insertionMode = InsertionMode.IN_TABLE_BODY;
    startTagInTableBody(p, token);
  }
  function tableStartTagInTable(p, token) {
    if (p.openElements.hasInTableScope(TAG_ID.TABLE)) {
      p.openElements.popUntilTagNamePopped(TAG_ID.TABLE);
      p._resetInsertionMode();
      p._processStartTag(token);
    }
  }
  function inputStartTagInTable(p, token) {
    if (isHiddenInput(token)) {
      p._appendElement(token, NS.HTML);
    } else {
      tokenInTable(p, token);
    }
    token.ackSelfClosing = true;
  }
  function formStartTagInTable(p, token) {
    if (!p.formElement && p.openElements.tmplCount === 0) {
      p._insertElement(token, NS.HTML);
      p.formElement = p.openElements.current;
      p.openElements.pop();
    }
  }
  function startTagInTable(p, token) {
    switch (token.tagID) {
      case TAG_ID.TD:
      case TAG_ID.TH:
      case TAG_ID.TR: {
        tdStartTagInTable(p, token);
        break;
      }
      case TAG_ID.STYLE:
      case TAG_ID.SCRIPT:
      case TAG_ID.TEMPLATE: {
        startTagInHead(p, token);
        break;
      }
      case TAG_ID.COL: {
        colStartTagInTable(p, token);
        break;
      }
      case TAG_ID.FORM: {
        formStartTagInTable(p, token);
        break;
      }
      case TAG_ID.TABLE: {
        tableStartTagInTable(p, token);
        break;
      }
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD: {
        tbodyStartTagInTable(p, token);
        break;
      }
      case TAG_ID.INPUT: {
        inputStartTagInTable(p, token);
        break;
      }
      case TAG_ID.CAPTION: {
        captionStartTagInTable(p, token);
        break;
      }
      case TAG_ID.COLGROUP: {
        colgroupStartTagInTable(p, token);
        break;
      }
      default: {
        tokenInTable(p, token);
      }
    }
  }
  function endTagInTable(p, token) {
    switch (token.tagID) {
      case TAG_ID.TABLE: {
        if (p.openElements.hasInTableScope(TAG_ID.TABLE)) {
          p.openElements.popUntilTagNamePopped(TAG_ID.TABLE);
          p._resetInsertionMode();
        }
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.HTML:
      case TAG_ID.TBODY:
      case TAG_ID.TD:
      case TAG_ID.TFOOT:
      case TAG_ID.TH:
      case TAG_ID.THEAD:
      case TAG_ID.TR: {
        break;
      }
      default: {
        tokenInTable(p, token);
      }
    }
  }
  function tokenInTable(p, token) {
    const savedFosterParentingState = p.fosterParentingEnabled;
    p.fosterParentingEnabled = true;
    modeInBody(p, token);
    p.fosterParentingEnabled = savedFosterParentingState;
  }
  function whitespaceCharacterInTableText(p, token) {
    p.pendingCharacterTokens.push(token);
  }
  function characterInTableText(p, token) {
    p.pendingCharacterTokens.push(token);
    p.hasNonWhitespacePendingCharacterToken = true;
  }
  function tokenInTableText(p, token) {
    let i = 0;
    if (p.hasNonWhitespacePendingCharacterToken) {
      for (; i < p.pendingCharacterTokens.length; i++) {
        tokenInTable(p, p.pendingCharacterTokens[i]);
      }
    } else {
      for (; i < p.pendingCharacterTokens.length; i++) {
        p._insertCharacters(p.pendingCharacterTokens[i]);
      }
    }
    p.insertionMode = p.originalInsertionMode;
    p._processToken(token);
  }
  function startTagInCaption(p, token) {
    const tn = token.tagID;
    if (TABLE_VOID_ELEMENTS.has(tn)) {
      if (p.openElements.hasInTableScope(TAG_ID.CAPTION)) {
        p.openElements.generateImpliedEndTags();
        p.openElements.popUntilTagNamePopped(TAG_ID.CAPTION);
        p.activeFormattingElements.clearToLastMarker();
        p.insertionMode = InsertionMode.IN_TABLE;
        startTagInTable(p, token);
      }
    } else {
      startTagInBody(p, token);
    }
  }
  function endTagInCaption(p, token) {
    const tn = token.tagID;
    switch (tn) {
      case TAG_ID.CAPTION:
      case TAG_ID.TABLE: {
        if (p.openElements.hasInTableScope(TAG_ID.CAPTION)) {
          p.openElements.generateImpliedEndTags();
          p.openElements.popUntilTagNamePopped(TAG_ID.CAPTION);
          p.activeFormattingElements.clearToLastMarker();
          p.insertionMode = InsertionMode.IN_TABLE;
          if (tn === TAG_ID.TABLE) {
            endTagInTable(p, token);
          }
        }
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.HTML:
      case TAG_ID.TBODY:
      case TAG_ID.TD:
      case TAG_ID.TFOOT:
      case TAG_ID.TH:
      case TAG_ID.THEAD:
      case TAG_ID.TR: {
        break;
      }
      default: {
        endTagInBody(p, token);
      }
    }
  }
  function startTagInColumnGroup(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.COL: {
        p._appendElement(token, NS.HTML);
        token.ackSelfClosing = true;
        break;
      }
      case TAG_ID.TEMPLATE: {
        startTagInHead(p, token);
        break;
      }
      default: {
        tokenInColumnGroup(p, token);
      }
    }
  }
  function endTagInColumnGroup(p, token) {
    switch (token.tagID) {
      case TAG_ID.COLGROUP: {
        if (p.openElements.currentTagId === TAG_ID.COLGROUP) {
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE;
        }
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      case TAG_ID.COL: {
        break;
      }
      default: {
        tokenInColumnGroup(p, token);
      }
    }
  }
  function tokenInColumnGroup(p, token) {
    if (p.openElements.currentTagId === TAG_ID.COLGROUP) {
      p.openElements.pop();
      p.insertionMode = InsertionMode.IN_TABLE;
      p._processToken(token);
    }
  }
  function startTagInTableBody(p, token) {
    switch (token.tagID) {
      case TAG_ID.TR: {
        p.openElements.clearBackToTableBodyContext();
        p._insertElement(token, NS.HTML);
        p.insertionMode = InsertionMode.IN_ROW;
        break;
      }
      case TAG_ID.TH:
      case TAG_ID.TD: {
        p.openElements.clearBackToTableBodyContext();
        p._insertFakeElement(TAG_NAMES.TR, TAG_ID.TR);
        p.insertionMode = InsertionMode.IN_ROW;
        startTagInRow(p, token);
        break;
      }
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD: {
        if (p.openElements.hasTableBodyContextInTableScope()) {
          p.openElements.clearBackToTableBodyContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE;
          startTagInTable(p, token);
        }
        break;
      }
      default: {
        startTagInTable(p, token);
      }
    }
  }
  function endTagInTableBody(p, token) {
    const tn = token.tagID;
    switch (token.tagID) {
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD: {
        if (p.openElements.hasInTableScope(tn)) {
          p.openElements.clearBackToTableBodyContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE;
        }
        break;
      }
      case TAG_ID.TABLE: {
        if (p.openElements.hasTableBodyContextInTableScope()) {
          p.openElements.clearBackToTableBodyContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE;
          endTagInTable(p, token);
        }
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.HTML:
      case TAG_ID.TD:
      case TAG_ID.TH:
      case TAG_ID.TR: {
        break;
      }
      default: {
        endTagInTable(p, token);
      }
    }
  }
  function startTagInRow(p, token) {
    switch (token.tagID) {
      case TAG_ID.TH:
      case TAG_ID.TD: {
        p.openElements.clearBackToTableRowContext();
        p._insertElement(token, NS.HTML);
        p.insertionMode = InsertionMode.IN_CELL;
        p.activeFormattingElements.insertMarker();
        break;
      }
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD:
      case TAG_ID.TR: {
        if (p.openElements.hasInTableScope(TAG_ID.TR)) {
          p.openElements.clearBackToTableRowContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE_BODY;
          startTagInTableBody(p, token);
        }
        break;
      }
      default: {
        startTagInTable(p, token);
      }
    }
  }
  function endTagInRow(p, token) {
    switch (token.tagID) {
      case TAG_ID.TR: {
        if (p.openElements.hasInTableScope(TAG_ID.TR)) {
          p.openElements.clearBackToTableRowContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE_BODY;
        }
        break;
      }
      case TAG_ID.TABLE: {
        if (p.openElements.hasInTableScope(TAG_ID.TR)) {
          p.openElements.clearBackToTableRowContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE_BODY;
          endTagInTableBody(p, token);
        }
        break;
      }
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD: {
        if (p.openElements.hasInTableScope(token.tagID) || p.openElements.hasInTableScope(TAG_ID.TR)) {
          p.openElements.clearBackToTableRowContext();
          p.openElements.pop();
          p.insertionMode = InsertionMode.IN_TABLE_BODY;
          endTagInTableBody(p, token);
        }
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.HTML:
      case TAG_ID.TD:
      case TAG_ID.TH: {
        break;
      }
      default: {
        endTagInTable(p, token);
      }
    }
  }
  function startTagInCell(p, token) {
    const tn = token.tagID;
    if (TABLE_VOID_ELEMENTS.has(tn)) {
      if (p.openElements.hasInTableScope(TAG_ID.TD) || p.openElements.hasInTableScope(TAG_ID.TH)) {
        p._closeTableCell();
        startTagInRow(p, token);
      }
    } else {
      startTagInBody(p, token);
    }
  }
  function endTagInCell(p, token) {
    const tn = token.tagID;
    switch (tn) {
      case TAG_ID.TD:
      case TAG_ID.TH: {
        if (p.openElements.hasInTableScope(tn)) {
          p.openElements.generateImpliedEndTags();
          p.openElements.popUntilTagNamePopped(tn);
          p.activeFormattingElements.clearToLastMarker();
          p.insertionMode = InsertionMode.IN_ROW;
        }
        break;
      }
      case TAG_ID.TABLE:
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD:
      case TAG_ID.TR: {
        if (p.openElements.hasInTableScope(tn)) {
          p._closeTableCell();
          endTagInRow(p, token);
        }
        break;
      }
      case TAG_ID.BODY:
      case TAG_ID.CAPTION:
      case TAG_ID.COL:
      case TAG_ID.COLGROUP:
      case TAG_ID.HTML: {
        break;
      }
      default: {
        endTagInBody(p, token);
      }
    }
  }
  function startTagInSelect(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.OPTION: {
        if (p.openElements.currentTagId === TAG_ID.OPTION) {
          p.openElements.pop();
        }
        p._insertElement(token, NS.HTML);
        break;
      }
      case TAG_ID.OPTGROUP: {
        if (p.openElements.currentTagId === TAG_ID.OPTION) {
          p.openElements.pop();
        }
        if (p.openElements.currentTagId === TAG_ID.OPTGROUP) {
          p.openElements.pop();
        }
        p._insertElement(token, NS.HTML);
        break;
      }
      case TAG_ID.HR: {
        if (p.openElements.currentTagId === TAG_ID.OPTION) {
          p.openElements.pop();
        }
        if (p.openElements.currentTagId === TAG_ID.OPTGROUP) {
          p.openElements.pop();
        }
        p._appendElement(token, NS.HTML);
        token.ackSelfClosing = true;
        break;
      }
      case TAG_ID.INPUT:
      case TAG_ID.KEYGEN:
      case TAG_ID.TEXTAREA:
      case TAG_ID.SELECT: {
        if (p.openElements.hasInSelectScope(TAG_ID.SELECT)) {
          p.openElements.popUntilTagNamePopped(TAG_ID.SELECT);
          p._resetInsertionMode();
          if (token.tagID !== TAG_ID.SELECT) {
            p._processStartTag(token);
          }
        }
        break;
      }
      case TAG_ID.SCRIPT:
      case TAG_ID.TEMPLATE: {
        startTagInHead(p, token);
        break;
      }
      default:
    }
  }
  function endTagInSelect(p, token) {
    switch (token.tagID) {
      case TAG_ID.OPTGROUP: {
        if (p.openElements.stackTop > 0 && p.openElements.currentTagId === TAG_ID.OPTION && p.openElements.tagIDs[p.openElements.stackTop - 1] === TAG_ID.OPTGROUP) {
          p.openElements.pop();
        }
        if (p.openElements.currentTagId === TAG_ID.OPTGROUP) {
          p.openElements.pop();
        }
        break;
      }
      case TAG_ID.OPTION: {
        if (p.openElements.currentTagId === TAG_ID.OPTION) {
          p.openElements.pop();
        }
        break;
      }
      case TAG_ID.SELECT: {
        if (p.openElements.hasInSelectScope(TAG_ID.SELECT)) {
          p.openElements.popUntilTagNamePopped(TAG_ID.SELECT);
          p._resetInsertionMode();
        }
        break;
      }
      case TAG_ID.TEMPLATE: {
        templateEndTagInHead(p, token);
        break;
      }
      default:
    }
  }
  function startTagInSelectInTable(p, token) {
    const tn = token.tagID;
    if (tn === TAG_ID.CAPTION || tn === TAG_ID.TABLE || tn === TAG_ID.TBODY || tn === TAG_ID.TFOOT || tn === TAG_ID.THEAD || tn === TAG_ID.TR || tn === TAG_ID.TD || tn === TAG_ID.TH) {
      p.openElements.popUntilTagNamePopped(TAG_ID.SELECT);
      p._resetInsertionMode();
      p._processStartTag(token);
    } else {
      startTagInSelect(p, token);
    }
  }
  function endTagInSelectInTable(p, token) {
    const tn = token.tagID;
    if (tn === TAG_ID.CAPTION || tn === TAG_ID.TABLE || tn === TAG_ID.TBODY || tn === TAG_ID.TFOOT || tn === TAG_ID.THEAD || tn === TAG_ID.TR || tn === TAG_ID.TD || tn === TAG_ID.TH) {
      if (p.openElements.hasInTableScope(tn)) {
        p.openElements.popUntilTagNamePopped(TAG_ID.SELECT);
        p._resetInsertionMode();
        p.onEndTag(token);
      }
    } else {
      endTagInSelect(p, token);
    }
  }
  function startTagInTemplate(p, token) {
    switch (token.tagID) {
      // First, handle tags that can start without a mode change
      case TAG_ID.BASE:
      case TAG_ID.BASEFONT:
      case TAG_ID.BGSOUND:
      case TAG_ID.LINK:
      case TAG_ID.META:
      case TAG_ID.NOFRAMES:
      case TAG_ID.SCRIPT:
      case TAG_ID.STYLE:
      case TAG_ID.TEMPLATE:
      case TAG_ID.TITLE: {
        startTagInHead(p, token);
        break;
      }
      // Re-process the token in the appropriate mode
      case TAG_ID.CAPTION:
      case TAG_ID.COLGROUP:
      case TAG_ID.TBODY:
      case TAG_ID.TFOOT:
      case TAG_ID.THEAD: {
        p.tmplInsertionModeStack[0] = InsertionMode.IN_TABLE;
        p.insertionMode = InsertionMode.IN_TABLE;
        startTagInTable(p, token);
        break;
      }
      case TAG_ID.COL: {
        p.tmplInsertionModeStack[0] = InsertionMode.IN_COLUMN_GROUP;
        p.insertionMode = InsertionMode.IN_COLUMN_GROUP;
        startTagInColumnGroup(p, token);
        break;
      }
      case TAG_ID.TR: {
        p.tmplInsertionModeStack[0] = InsertionMode.IN_TABLE_BODY;
        p.insertionMode = InsertionMode.IN_TABLE_BODY;
        startTagInTableBody(p, token);
        break;
      }
      case TAG_ID.TD:
      case TAG_ID.TH: {
        p.tmplInsertionModeStack[0] = InsertionMode.IN_ROW;
        p.insertionMode = InsertionMode.IN_ROW;
        startTagInRow(p, token);
        break;
      }
      default: {
        p.tmplInsertionModeStack[0] = InsertionMode.IN_BODY;
        p.insertionMode = InsertionMode.IN_BODY;
        startTagInBody(p, token);
      }
    }
  }
  function endTagInTemplate(p, token) {
    if (token.tagID === TAG_ID.TEMPLATE) {
      templateEndTagInHead(p, token);
    }
  }
  function eofInTemplate(p, token) {
    if (p.openElements.tmplCount > 0) {
      p.openElements.popUntilTagNamePopped(TAG_ID.TEMPLATE);
      p.activeFormattingElements.clearToLastMarker();
      p.tmplInsertionModeStack.shift();
      p._resetInsertionMode();
      p.onEof(token);
    } else {
      stopParsing(p, token);
    }
  }
  function startTagAfterBody(p, token) {
    if (token.tagID === TAG_ID.HTML) {
      startTagInBody(p, token);
    } else {
      tokenAfterBody(p, token);
    }
  }
  function endTagAfterBody(p, token) {
    var _a;
    if (token.tagID === TAG_ID.HTML) {
      if (!p.fragmentContext) {
        p.insertionMode = InsertionMode.AFTER_AFTER_BODY;
      }
      if (p.options.sourceCodeLocationInfo && p.openElements.tagIDs[0] === TAG_ID.HTML) {
        p._setEndLocation(p.openElements.items[0], token);
        const bodyElement = p.openElements.items[1];
        if (bodyElement && !((_a = p.treeAdapter.getNodeSourceCodeLocation(bodyElement)) === null || _a === void 0 ? void 0 : _a.endTag)) {
          p._setEndLocation(bodyElement, token);
        }
      }
    } else {
      tokenAfterBody(p, token);
    }
  }
  function tokenAfterBody(p, token) {
    p.insertionMode = InsertionMode.IN_BODY;
    modeInBody(p, token);
  }
  function startTagInFrameset(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.FRAMESET: {
        p._insertElement(token, NS.HTML);
        break;
      }
      case TAG_ID.FRAME: {
        p._appendElement(token, NS.HTML);
        token.ackSelfClosing = true;
        break;
      }
      case TAG_ID.NOFRAMES: {
        startTagInHead(p, token);
        break;
      }
      default:
    }
  }
  function endTagInFrameset(p, token) {
    if (token.tagID === TAG_ID.FRAMESET && !p.openElements.isRootHtmlElementCurrent()) {
      p.openElements.pop();
      if (!p.fragmentContext && p.openElements.currentTagId !== TAG_ID.FRAMESET) {
        p.insertionMode = InsertionMode.AFTER_FRAMESET;
      }
    }
  }
  function startTagAfterFrameset(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.NOFRAMES: {
        startTagInHead(p, token);
        break;
      }
      default:
    }
  }
  function endTagAfterFrameset(p, token) {
    if (token.tagID === TAG_ID.HTML) {
      p.insertionMode = InsertionMode.AFTER_AFTER_FRAMESET;
    }
  }
  function startTagAfterAfterBody(p, token) {
    if (token.tagID === TAG_ID.HTML) {
      startTagInBody(p, token);
    } else {
      tokenAfterAfterBody(p, token);
    }
  }
  function tokenAfterAfterBody(p, token) {
    p.insertionMode = InsertionMode.IN_BODY;
    modeInBody(p, token);
  }
  function startTagAfterAfterFrameset(p, token) {
    switch (token.tagID) {
      case TAG_ID.HTML: {
        startTagInBody(p, token);
        break;
      }
      case TAG_ID.NOFRAMES: {
        startTagInHead(p, token);
        break;
      }
      default:
    }
  }
  function nullCharacterInForeignContent(p, token) {
    token.chars = REPLACEMENT_CHARACTER;
    p._insertCharacters(token);
  }
  function characterInForeignContent(p, token) {
    p._insertCharacters(token);
    p.framesetOk = false;
  }
  function popUntilHtmlOrIntegrationPoint(p) {
    while (p.treeAdapter.getNamespaceURI(p.openElements.current) !== NS.HTML && !p._isIntegrationPoint(p.openElements.currentTagId, p.openElements.current)) {
      p.openElements.pop();
    }
  }
  function startTagInForeignContent(p, token) {
    if (causesExit(token)) {
      popUntilHtmlOrIntegrationPoint(p);
      p._startTagOutsideForeignContent(token);
    } else {
      const current = p._getAdjustedCurrentElement();
      const currentNs = p.treeAdapter.getNamespaceURI(current);
      if (currentNs === NS.MATHML) {
        adjustTokenMathMLAttrs(token);
      } else if (currentNs === NS.SVG) {
        adjustTokenSVGTagName(token);
        adjustTokenSVGAttrs(token);
      }
      adjustTokenXMLAttrs(token);
      if (token.selfClosing) {
        p._appendElement(token, currentNs);
      } else {
        p._insertElement(token, currentNs);
      }
      token.ackSelfClosing = true;
    }
  }
  function endTagInForeignContent(p, token) {
    if (token.tagID === TAG_ID.P || token.tagID === TAG_ID.BR) {
      popUntilHtmlOrIntegrationPoint(p);
      p._endTagOutsideForeignContent(token);
      return;
    }
    for (let i = p.openElements.stackTop; i > 0; i--) {
      const element = p.openElements.items[i];
      if (p.treeAdapter.getNamespaceURI(element) === NS.HTML) {
        p._endTagOutsideForeignContent(token);
        break;
      }
      const tagName = p.treeAdapter.getTagName(element);
      if (tagName.toLowerCase() === token.tagName) {
        token.tagName = tagName;
        p.openElements.shortenToLength(i);
        break;
      }
    }
  }
  var HIDDEN_INPUT_TYPE, AA_OUTER_LOOP_ITER, AA_INNER_LOOP_ITER, InsertionMode, BASE_LOC, TABLE_STRUCTURE_TAGS, defaultParserOptions, _Parser, Parser, TABLE_VOID_ELEMENTS;
  var init_parser = __esm({
    "node_modules/parse5/dist/parser/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_tokenizer();
      init_open_element_stack();
      init_formatting_element_list();
      init_default();
      init_doctype();
      init_foreign_content();
      init_error_codes();
      init_unicode();
      init_html();
      init_token();
      HIDDEN_INPUT_TYPE = "hidden";
      AA_OUTER_LOOP_ITER = 8;
      AA_INNER_LOOP_ITER = 3;
      (function(InsertionMode2) {
        InsertionMode2[InsertionMode2["INITIAL"] = 0] = "INITIAL";
        InsertionMode2[InsertionMode2["BEFORE_HTML"] = 1] = "BEFORE_HTML";
        InsertionMode2[InsertionMode2["BEFORE_HEAD"] = 2] = "BEFORE_HEAD";
        InsertionMode2[InsertionMode2["IN_HEAD"] = 3] = "IN_HEAD";
        InsertionMode2[InsertionMode2["IN_HEAD_NO_SCRIPT"] = 4] = "IN_HEAD_NO_SCRIPT";
        InsertionMode2[InsertionMode2["AFTER_HEAD"] = 5] = "AFTER_HEAD";
        InsertionMode2[InsertionMode2["IN_BODY"] = 6] = "IN_BODY";
        InsertionMode2[InsertionMode2["TEXT"] = 7] = "TEXT";
        InsertionMode2[InsertionMode2["IN_TABLE"] = 8] = "IN_TABLE";
        InsertionMode2[InsertionMode2["IN_TABLE_TEXT"] = 9] = "IN_TABLE_TEXT";
        InsertionMode2[InsertionMode2["IN_CAPTION"] = 10] = "IN_CAPTION";
        InsertionMode2[InsertionMode2["IN_COLUMN_GROUP"] = 11] = "IN_COLUMN_GROUP";
        InsertionMode2[InsertionMode2["IN_TABLE_BODY"] = 12] = "IN_TABLE_BODY";
        InsertionMode2[InsertionMode2["IN_ROW"] = 13] = "IN_ROW";
        InsertionMode2[InsertionMode2["IN_CELL"] = 14] = "IN_CELL";
        InsertionMode2[InsertionMode2["IN_SELECT"] = 15] = "IN_SELECT";
        InsertionMode2[InsertionMode2["IN_SELECT_IN_TABLE"] = 16] = "IN_SELECT_IN_TABLE";
        InsertionMode2[InsertionMode2["IN_TEMPLATE"] = 17] = "IN_TEMPLATE";
        InsertionMode2[InsertionMode2["AFTER_BODY"] = 18] = "AFTER_BODY";
        InsertionMode2[InsertionMode2["IN_FRAMESET"] = 19] = "IN_FRAMESET";
        InsertionMode2[InsertionMode2["AFTER_FRAMESET"] = 20] = "AFTER_FRAMESET";
        InsertionMode2[InsertionMode2["AFTER_AFTER_BODY"] = 21] = "AFTER_AFTER_BODY";
        InsertionMode2[InsertionMode2["AFTER_AFTER_FRAMESET"] = 22] = "AFTER_AFTER_FRAMESET";
      })(InsertionMode || (InsertionMode = {}));
      BASE_LOC = {
        startLine: -1,
        startCol: -1,
        startOffset: -1,
        endLine: -1,
        endCol: -1,
        endOffset: -1
      };
      TABLE_STRUCTURE_TAGS = /* @__PURE__ */ new Set([TAG_ID.TABLE, TAG_ID.TBODY, TAG_ID.TFOOT, TAG_ID.THEAD, TAG_ID.TR]);
      defaultParserOptions = {
        scriptingEnabled: true,
        sourceCodeLocationInfo: false,
        treeAdapter: defaultTreeAdapter,
        onParseError: null
      };
      _Parser = class _Parser {
        constructor(options, document, fragmentContext = null, scriptHandler = null) {
          this.fragmentContext = fragmentContext;
          this.scriptHandler = scriptHandler;
          this.currentToken = null;
          this.stopped = false;
          this.insertionMode = InsertionMode.INITIAL;
          this.originalInsertionMode = InsertionMode.INITIAL;
          this.headElement = null;
          this.formElement = null;
          this.currentNotInHTML = false;
          this.tmplInsertionModeStack = [];
          this.pendingCharacterTokens = [];
          this.hasNonWhitespacePendingCharacterToken = false;
          this.framesetOk = true;
          this.skipNextNewLine = false;
          this.fosterParentingEnabled = false;
          this.options = {
            ...defaultParserOptions,
            ...options
          };
          this.treeAdapter = this.options.treeAdapter;
          this.onParseError = this.options.onParseError;
          if (this.onParseError) {
            this.options.sourceCodeLocationInfo = true;
          }
          this.document = document !== null && document !== void 0 ? document : this.treeAdapter.createDocument();
          this.tokenizer = new Tokenizer(this.options, this);
          this.activeFormattingElements = new FormattingElementList(this.treeAdapter);
          this.fragmentContextID = fragmentContext ? getTagID(this.treeAdapter.getTagName(fragmentContext)) : TAG_ID.UNKNOWN;
          this._setContextModes(fragmentContext !== null && fragmentContext !== void 0 ? fragmentContext : this.document, this.fragmentContextID);
          this.openElements = new OpenElementStack(this.document, this.treeAdapter, this);
        }
        // API
        static parse(html3, options) {
          const parser = new this(options);
          parser.tokenizer.write(html3, true);
          return parser.document;
        }
        static getFragmentParser(fragmentContext, options) {
          const opts = {
            ...defaultParserOptions,
            ...options
          };
          fragmentContext !== null && fragmentContext !== void 0 ? fragmentContext : fragmentContext = opts.treeAdapter.createElement(TAG_NAMES.TEMPLATE, NS.HTML, []);
          const documentMock = opts.treeAdapter.createElement("documentmock", NS.HTML, []);
          const parser = new this(opts, documentMock, fragmentContext);
          if (parser.fragmentContextID === TAG_ID.TEMPLATE) {
            parser.tmplInsertionModeStack.unshift(InsertionMode.IN_TEMPLATE);
          }
          parser._initTokenizerForFragmentParsing();
          parser._insertFakeRootElement();
          parser._resetInsertionMode();
          parser._findFormInFragmentContext();
          return parser;
        }
        getFragment() {
          const rootElement = this.treeAdapter.getFirstChild(this.document);
          const fragment = this.treeAdapter.createDocumentFragment();
          this._adoptNodes(rootElement, fragment);
          return fragment;
        }
        //Errors
        /** @internal */
        _err(token, code, beforeToken) {
          var _a;
          if (!this.onParseError)
            return;
          const loc = (_a = token.location) !== null && _a !== void 0 ? _a : BASE_LOC;
          const err = {
            code,
            startLine: loc.startLine,
            startCol: loc.startCol,
            startOffset: loc.startOffset,
            endLine: beforeToken ? loc.startLine : loc.endLine,
            endCol: beforeToken ? loc.startCol : loc.endCol,
            endOffset: beforeToken ? loc.startOffset : loc.endOffset
          };
          this.onParseError(err);
        }
        //Stack events
        /** @internal */
        onItemPush(node, tid, isTop) {
          var _a, _b;
          (_b = (_a = this.treeAdapter).onItemPush) === null || _b === void 0 ? void 0 : _b.call(_a, node);
          if (isTop && this.openElements.stackTop > 0)
            this._setContextModes(node, tid);
        }
        /** @internal */
        onItemPop(node, isTop) {
          var _a, _b;
          if (this.options.sourceCodeLocationInfo) {
            this._setEndLocation(node, this.currentToken);
          }
          (_b = (_a = this.treeAdapter).onItemPop) === null || _b === void 0 ? void 0 : _b.call(_a, node, this.openElements.current);
          if (isTop) {
            let current;
            let currentTagId;
            if (this.openElements.stackTop === 0 && this.fragmentContext) {
              current = this.fragmentContext;
              currentTagId = this.fragmentContextID;
            } else {
              ({ current, currentTagId } = this.openElements);
            }
            this._setContextModes(current, currentTagId);
          }
        }
        _setContextModes(current, tid) {
          const isHTML = current === this.document || this.treeAdapter.getNamespaceURI(current) === NS.HTML;
          this.currentNotInHTML = !isHTML;
          this.tokenizer.inForeignNode = !isHTML && !this._isIntegrationPoint(tid, current);
        }
        /** @protected */
        _switchToTextParsing(currentToken, nextTokenizerState) {
          this._insertElement(currentToken, NS.HTML);
          this.tokenizer.state = nextTokenizerState;
          this.originalInsertionMode = this.insertionMode;
          this.insertionMode = InsertionMode.TEXT;
        }
        switchToPlaintextParsing() {
          this.insertionMode = InsertionMode.TEXT;
          this.originalInsertionMode = InsertionMode.IN_BODY;
          this.tokenizer.state = TokenizerMode.PLAINTEXT;
        }
        //Fragment parsing
        /** @protected */
        _getAdjustedCurrentElement() {
          return this.openElements.stackTop === 0 && this.fragmentContext ? this.fragmentContext : this.openElements.current;
        }
        /** @protected */
        _findFormInFragmentContext() {
          let node = this.fragmentContext;
          while (node) {
            if (this.treeAdapter.getTagName(node) === TAG_NAMES.FORM) {
              this.formElement = node;
              break;
            }
            node = this.treeAdapter.getParentNode(node);
          }
        }
        _initTokenizerForFragmentParsing() {
          if (!this.fragmentContext || this.treeAdapter.getNamespaceURI(this.fragmentContext) !== NS.HTML) {
            return;
          }
          switch (this.fragmentContextID) {
            case TAG_ID.TITLE:
            case TAG_ID.TEXTAREA: {
              this.tokenizer.state = TokenizerMode.RCDATA;
              break;
            }
            case TAG_ID.STYLE:
            case TAG_ID.XMP:
            case TAG_ID.IFRAME:
            case TAG_ID.NOEMBED:
            case TAG_ID.NOFRAMES:
            case TAG_ID.NOSCRIPT: {
              this.tokenizer.state = TokenizerMode.RAWTEXT;
              break;
            }
            case TAG_ID.SCRIPT: {
              this.tokenizer.state = TokenizerMode.SCRIPT_DATA;
              break;
            }
            case TAG_ID.PLAINTEXT: {
              this.tokenizer.state = TokenizerMode.PLAINTEXT;
              break;
            }
            default:
          }
        }
        //Tree mutation
        /** @protected */
        _setDocumentType(token) {
          const name = token.name || "";
          const publicId = token.publicId || "";
          const systemId = token.systemId || "";
          this.treeAdapter.setDocumentType(this.document, name, publicId, systemId);
          if (token.location) {
            const documentChildren = this.treeAdapter.getChildNodes(this.document);
            const docTypeNode = documentChildren.find((node) => this.treeAdapter.isDocumentTypeNode(node));
            if (docTypeNode) {
              this.treeAdapter.setNodeSourceCodeLocation(docTypeNode, token.location);
            }
          }
        }
        /** @protected */
        _attachElementToTree(element, location) {
          if (this.options.sourceCodeLocationInfo) {
            const loc = location && {
              ...location,
              startTag: location
            };
            this.treeAdapter.setNodeSourceCodeLocation(element, loc);
          }
          if (this._shouldFosterParentOnInsertion()) {
            this._fosterParentElement(element);
          } else {
            const parent2 = this.openElements.currentTmplContentOrNode;
            this.treeAdapter.appendChild(parent2, element);
          }
        }
        /**
         * For self-closing tags. Add an element to the tree, but skip adding it
         * to the stack.
         */
        /** @protected */
        _appendElement(token, namespaceURI) {
          const element = this.treeAdapter.createElement(token.tagName, namespaceURI, token.attrs);
          this._attachElementToTree(element, token.location);
        }
        /** @protected */
        _insertElement(token, namespaceURI) {
          const element = this.treeAdapter.createElement(token.tagName, namespaceURI, token.attrs);
          this._attachElementToTree(element, token.location);
          this.openElements.push(element, token.tagID);
        }
        /** @protected */
        _insertFakeElement(tagName, tagID) {
          const element = this.treeAdapter.createElement(tagName, NS.HTML, []);
          this._attachElementToTree(element, null);
          this.openElements.push(element, tagID);
        }
        /** @protected */
        _insertTemplate(token) {
          const tmpl = this.treeAdapter.createElement(token.tagName, NS.HTML, token.attrs);
          const content = this.treeAdapter.createDocumentFragment();
          this.treeAdapter.setTemplateContent(tmpl, content);
          this._attachElementToTree(tmpl, token.location);
          this.openElements.push(tmpl, token.tagID);
          if (this.options.sourceCodeLocationInfo)
            this.treeAdapter.setNodeSourceCodeLocation(content, null);
        }
        /** @protected */
        _insertFakeRootElement() {
          const element = this.treeAdapter.createElement(TAG_NAMES.HTML, NS.HTML, []);
          if (this.options.sourceCodeLocationInfo)
            this.treeAdapter.setNodeSourceCodeLocation(element, null);
          this.treeAdapter.appendChild(this.openElements.current, element);
          this.openElements.push(element, TAG_ID.HTML);
        }
        /** @protected */
        _appendCommentNode(token, parent2) {
          const commentNode = this.treeAdapter.createCommentNode(token.data);
          this.treeAdapter.appendChild(parent2, commentNode);
          if (this.options.sourceCodeLocationInfo) {
            this.treeAdapter.setNodeSourceCodeLocation(commentNode, token.location);
          }
        }
        /** @protected */
        _insertCharacters(token) {
          let parent2;
          let beforeElement;
          if (this._shouldFosterParentOnInsertion()) {
            ({ parent: parent2, beforeElement } = this._findFosterParentingLocation());
            if (beforeElement) {
              this.treeAdapter.insertTextBefore(parent2, token.chars, beforeElement);
            } else {
              this.treeAdapter.insertText(parent2, token.chars);
            }
          } else {
            parent2 = this.openElements.currentTmplContentOrNode;
            this.treeAdapter.insertText(parent2, token.chars);
          }
          if (!token.location)
            return;
          const siblings2 = this.treeAdapter.getChildNodes(parent2);
          const textNodeIdx = beforeElement ? siblings2.lastIndexOf(beforeElement) : siblings2.length;
          const textNode = siblings2[textNodeIdx - 1];
          const tnLoc = this.treeAdapter.getNodeSourceCodeLocation(textNode);
          if (tnLoc) {
            const { endLine, endCol, endOffset } = token.location;
            this.treeAdapter.updateNodeSourceCodeLocation(textNode, { endLine, endCol, endOffset });
          } else if (this.options.sourceCodeLocationInfo) {
            this.treeAdapter.setNodeSourceCodeLocation(textNode, token.location);
          }
        }
        /** @protected */
        _adoptNodes(donor, recipient) {
          for (let child = this.treeAdapter.getFirstChild(donor); child; child = this.treeAdapter.getFirstChild(donor)) {
            this.treeAdapter.detachNode(child);
            this.treeAdapter.appendChild(recipient, child);
          }
        }
        /** @protected */
        _setEndLocation(element, closingToken) {
          if (this.treeAdapter.getNodeSourceCodeLocation(element) && closingToken.location) {
            const ctLoc = closingToken.location;
            const tn = this.treeAdapter.getTagName(element);
            const endLoc = (
              // NOTE: For cases like <p> <p> </p> - First 'p' closes without a closing
              // tag and for cases like <td> <p> </td> - 'p' closes without a closing tag.
              closingToken.type === TokenType.END_TAG && tn === closingToken.tagName ? {
                endTag: { ...ctLoc },
                endLine: ctLoc.endLine,
                endCol: ctLoc.endCol,
                endOffset: ctLoc.endOffset
              } : {
                endLine: ctLoc.startLine,
                endCol: ctLoc.startCol,
                endOffset: ctLoc.startOffset
              }
            );
            this.treeAdapter.updateNodeSourceCodeLocation(element, endLoc);
          }
        }
        //Token processing
        shouldProcessStartTagTokenInForeignContent(token) {
          if (!this.currentNotInHTML)
            return false;
          let current;
          let currentTagId;
          if (this.openElements.stackTop === 0 && this.fragmentContext) {
            current = this.fragmentContext;
            currentTagId = this.fragmentContextID;
          } else {
            ({ current, currentTagId } = this.openElements);
          }
          if (token.tagID === TAG_ID.SVG && this.treeAdapter.getTagName(current) === TAG_NAMES.ANNOTATION_XML && this.treeAdapter.getNamespaceURI(current) === NS.MATHML) {
            return false;
          }
          return (
            // Check that `current` is not an integration point for HTML or MathML elements.
            this.tokenizer.inForeignNode || // If it _is_ an integration point, then we might have to check that it is not an HTML
            // integration point.
            (token.tagID === TAG_ID.MGLYPH || token.tagID === TAG_ID.MALIGNMARK) && !this._isIntegrationPoint(currentTagId, current, NS.HTML)
          );
        }
        /** @protected */
        _processToken(token) {
          switch (token.type) {
            case TokenType.CHARACTER: {
              this.onCharacter(token);
              break;
            }
            case TokenType.NULL_CHARACTER: {
              this.onNullCharacter(token);
              break;
            }
            case TokenType.COMMENT: {
              this.onComment(token);
              break;
            }
            case TokenType.DOCTYPE: {
              this.onDoctype(token);
              break;
            }
            case TokenType.START_TAG: {
              this._processStartTag(token);
              break;
            }
            case TokenType.END_TAG: {
              this.onEndTag(token);
              break;
            }
            case TokenType.EOF: {
              this.onEof(token);
              break;
            }
            case TokenType.WHITESPACE_CHARACTER: {
              this.onWhitespaceCharacter(token);
              break;
            }
          }
        }
        //Integration points
        /** @protected */
        _isIntegrationPoint(tid, element, foreignNS) {
          const ns = this.treeAdapter.getNamespaceURI(element);
          const attrs = this.treeAdapter.getAttrList(element);
          return isIntegrationPoint(tid, ns, attrs, foreignNS);
        }
        //Active formatting elements reconstruction
        /** @protected */
        _reconstructActiveFormattingElements() {
          const listLength = this.activeFormattingElements.entries.length;
          if (listLength) {
            const endIndex = this.activeFormattingElements.entries.findIndex((entry) => entry.type === EntryType.Marker || this.openElements.contains(entry.element));
            const unopenIdx = endIndex < 0 ? listLength - 1 : endIndex - 1;
            for (let i = unopenIdx; i >= 0; i--) {
              const entry = this.activeFormattingElements.entries[i];
              this._insertElement(entry.token, this.treeAdapter.getNamespaceURI(entry.element));
              entry.element = this.openElements.current;
            }
          }
        }
        //Close elements
        /** @protected */
        _closeTableCell() {
          this.openElements.generateImpliedEndTags();
          this.openElements.popUntilTableCellPopped();
          this.activeFormattingElements.clearToLastMarker();
          this.insertionMode = InsertionMode.IN_ROW;
        }
        /** @protected */
        _closePElement() {
          this.openElements.generateImpliedEndTagsWithExclusion(TAG_ID.P);
          this.openElements.popUntilTagNamePopped(TAG_ID.P);
        }
        //Insertion modes
        /** @protected */
        _resetInsertionMode() {
          for (let i = this.openElements.stackTop; i >= 0; i--) {
            switch (i === 0 && this.fragmentContext ? this.fragmentContextID : this.openElements.tagIDs[i]) {
              case TAG_ID.TR: {
                this.insertionMode = InsertionMode.IN_ROW;
                return;
              }
              case TAG_ID.TBODY:
              case TAG_ID.THEAD:
              case TAG_ID.TFOOT: {
                this.insertionMode = InsertionMode.IN_TABLE_BODY;
                return;
              }
              case TAG_ID.CAPTION: {
                this.insertionMode = InsertionMode.IN_CAPTION;
                return;
              }
              case TAG_ID.COLGROUP: {
                this.insertionMode = InsertionMode.IN_COLUMN_GROUP;
                return;
              }
              case TAG_ID.TABLE: {
                this.insertionMode = InsertionMode.IN_TABLE;
                return;
              }
              case TAG_ID.BODY: {
                this.insertionMode = InsertionMode.IN_BODY;
                return;
              }
              case TAG_ID.FRAMESET: {
                this.insertionMode = InsertionMode.IN_FRAMESET;
                return;
              }
              case TAG_ID.SELECT: {
                this._resetInsertionModeForSelect(i);
                return;
              }
              case TAG_ID.TEMPLATE: {
                this.insertionMode = this.tmplInsertionModeStack[0];
                return;
              }
              case TAG_ID.HTML: {
                this.insertionMode = this.headElement ? InsertionMode.AFTER_HEAD : InsertionMode.BEFORE_HEAD;
                return;
              }
              case TAG_ID.TD:
              case TAG_ID.TH: {
                if (i > 0) {
                  this.insertionMode = InsertionMode.IN_CELL;
                  return;
                }
                break;
              }
              case TAG_ID.HEAD: {
                if (i > 0) {
                  this.insertionMode = InsertionMode.IN_HEAD;
                  return;
                }
                break;
              }
            }
          }
          this.insertionMode = InsertionMode.IN_BODY;
        }
        /** @protected */
        _resetInsertionModeForSelect(selectIdx) {
          if (selectIdx > 0) {
            for (let i = selectIdx - 1; i > 0; i--) {
              const tn = this.openElements.tagIDs[i];
              if (tn === TAG_ID.TEMPLATE) {
                break;
              } else if (tn === TAG_ID.TABLE) {
                this.insertionMode = InsertionMode.IN_SELECT_IN_TABLE;
                return;
              }
            }
          }
          this.insertionMode = InsertionMode.IN_SELECT;
        }
        //Foster parenting
        /** @protected */
        _isElementCausesFosterParenting(tn) {
          return TABLE_STRUCTURE_TAGS.has(tn);
        }
        /** @protected */
        _shouldFosterParentOnInsertion() {
          return this.fosterParentingEnabled && this._isElementCausesFosterParenting(this.openElements.currentTagId);
        }
        /** @protected */
        _findFosterParentingLocation() {
          for (let i = this.openElements.stackTop; i >= 0; i--) {
            const openElement = this.openElements.items[i];
            switch (this.openElements.tagIDs[i]) {
              case TAG_ID.TEMPLATE: {
                if (this.treeAdapter.getNamespaceURI(openElement) === NS.HTML) {
                  return { parent: this.treeAdapter.getTemplateContent(openElement), beforeElement: null };
                }
                break;
              }
              case TAG_ID.TABLE: {
                const parent2 = this.treeAdapter.getParentNode(openElement);
                if (parent2) {
                  return { parent: parent2, beforeElement: openElement };
                }
                return { parent: this.openElements.items[i - 1], beforeElement: null };
              }
              default:
            }
          }
          return { parent: this.openElements.items[0], beforeElement: null };
        }
        /** @protected */
        _fosterParentElement(element) {
          const location = this._findFosterParentingLocation();
          if (location.beforeElement) {
            this.treeAdapter.insertBefore(location.parent, element, location.beforeElement);
          } else {
            this.treeAdapter.appendChild(location.parent, element);
          }
        }
        //Special elements
        /** @protected */
        _isSpecialElement(element, id) {
          const ns = this.treeAdapter.getNamespaceURI(element);
          return SPECIAL_ELEMENTS[ns].has(id);
        }
        /** @internal */
        onCharacter(token) {
          this.skipNextNewLine = false;
          if (this.tokenizer.inForeignNode) {
            characterInForeignContent(this, token);
            return;
          }
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              tokenInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HTML: {
              tokenBeforeHtml(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD: {
              tokenBeforeHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD: {
              tokenInHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD_NO_SCRIPT: {
              tokenInHeadNoScript(this, token);
              break;
            }
            case InsertionMode.AFTER_HEAD: {
              tokenAfterHead(this, token);
              break;
            }
            case InsertionMode.IN_BODY:
            case InsertionMode.IN_CAPTION:
            case InsertionMode.IN_CELL:
            case InsertionMode.IN_TEMPLATE: {
              characterInBody(this, token);
              break;
            }
            case InsertionMode.TEXT:
            case InsertionMode.IN_SELECT:
            case InsertionMode.IN_SELECT_IN_TABLE: {
              this._insertCharacters(token);
              break;
            }
            case InsertionMode.IN_TABLE:
            case InsertionMode.IN_TABLE_BODY:
            case InsertionMode.IN_ROW: {
              characterInTable(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              characterInTableText(this, token);
              break;
            }
            case InsertionMode.IN_COLUMN_GROUP: {
              tokenInColumnGroup(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY: {
              tokenAfterBody(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_BODY: {
              tokenAfterAfterBody(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onNullCharacter(token) {
          this.skipNextNewLine = false;
          if (this.tokenizer.inForeignNode) {
            nullCharacterInForeignContent(this, token);
            return;
          }
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              tokenInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HTML: {
              tokenBeforeHtml(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD: {
              tokenBeforeHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD: {
              tokenInHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD_NO_SCRIPT: {
              tokenInHeadNoScript(this, token);
              break;
            }
            case InsertionMode.AFTER_HEAD: {
              tokenAfterHead(this, token);
              break;
            }
            case InsertionMode.TEXT: {
              this._insertCharacters(token);
              break;
            }
            case InsertionMode.IN_TABLE:
            case InsertionMode.IN_TABLE_BODY:
            case InsertionMode.IN_ROW: {
              characterInTable(this, token);
              break;
            }
            case InsertionMode.IN_COLUMN_GROUP: {
              tokenInColumnGroup(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY: {
              tokenAfterBody(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_BODY: {
              tokenAfterAfterBody(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onComment(token) {
          this.skipNextNewLine = false;
          if (this.currentNotInHTML) {
            appendComment(this, token);
            return;
          }
          switch (this.insertionMode) {
            case InsertionMode.INITIAL:
            case InsertionMode.BEFORE_HTML:
            case InsertionMode.BEFORE_HEAD:
            case InsertionMode.IN_HEAD:
            case InsertionMode.IN_HEAD_NO_SCRIPT:
            case InsertionMode.AFTER_HEAD:
            case InsertionMode.IN_BODY:
            case InsertionMode.IN_TABLE:
            case InsertionMode.IN_CAPTION:
            case InsertionMode.IN_COLUMN_GROUP:
            case InsertionMode.IN_TABLE_BODY:
            case InsertionMode.IN_ROW:
            case InsertionMode.IN_CELL:
            case InsertionMode.IN_SELECT:
            case InsertionMode.IN_SELECT_IN_TABLE:
            case InsertionMode.IN_TEMPLATE:
            case InsertionMode.IN_FRAMESET:
            case InsertionMode.AFTER_FRAMESET: {
              appendComment(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              tokenInTableText(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY: {
              appendCommentToRootHtmlElement(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_BODY:
            case InsertionMode.AFTER_AFTER_FRAMESET: {
              appendCommentToDocument(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onDoctype(token) {
          this.skipNextNewLine = false;
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              doctypeInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD:
            case InsertionMode.IN_HEAD:
            case InsertionMode.IN_HEAD_NO_SCRIPT:
            case InsertionMode.AFTER_HEAD: {
              this._err(token, ERR.misplacedDoctype);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              tokenInTableText(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onStartTag(token) {
          this.skipNextNewLine = false;
          this.currentToken = token;
          this._processStartTag(token);
          if (token.selfClosing && !token.ackSelfClosing) {
            this._err(token, ERR.nonVoidHtmlElementStartTagWithTrailingSolidus);
          }
        }
        /**
         * Processes a given start tag.
         *
         * `onStartTag` checks if a self-closing tag was recognized. When a token
         * is moved inbetween multiple insertion modes, this check for self-closing
         * could lead to false positives. To avoid this, `_processStartTag` is used
         * for nested calls.
         *
         * @param token The token to process.
         * @protected
         */
        _processStartTag(token) {
          if (this.shouldProcessStartTagTokenInForeignContent(token)) {
            startTagInForeignContent(this, token);
          } else {
            this._startTagOutsideForeignContent(token);
          }
        }
        /** @protected */
        _startTagOutsideForeignContent(token) {
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              tokenInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HTML: {
              startTagBeforeHtml(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD: {
              startTagBeforeHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD: {
              startTagInHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD_NO_SCRIPT: {
              startTagInHeadNoScript(this, token);
              break;
            }
            case InsertionMode.AFTER_HEAD: {
              startTagAfterHead(this, token);
              break;
            }
            case InsertionMode.IN_BODY: {
              startTagInBody(this, token);
              break;
            }
            case InsertionMode.IN_TABLE: {
              startTagInTable(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              tokenInTableText(this, token);
              break;
            }
            case InsertionMode.IN_CAPTION: {
              startTagInCaption(this, token);
              break;
            }
            case InsertionMode.IN_COLUMN_GROUP: {
              startTagInColumnGroup(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_BODY: {
              startTagInTableBody(this, token);
              break;
            }
            case InsertionMode.IN_ROW: {
              startTagInRow(this, token);
              break;
            }
            case InsertionMode.IN_CELL: {
              startTagInCell(this, token);
              break;
            }
            case InsertionMode.IN_SELECT: {
              startTagInSelect(this, token);
              break;
            }
            case InsertionMode.IN_SELECT_IN_TABLE: {
              startTagInSelectInTable(this, token);
              break;
            }
            case InsertionMode.IN_TEMPLATE: {
              startTagInTemplate(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY: {
              startTagAfterBody(this, token);
              break;
            }
            case InsertionMode.IN_FRAMESET: {
              startTagInFrameset(this, token);
              break;
            }
            case InsertionMode.AFTER_FRAMESET: {
              startTagAfterFrameset(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_BODY: {
              startTagAfterAfterBody(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_FRAMESET: {
              startTagAfterAfterFrameset(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onEndTag(token) {
          this.skipNextNewLine = false;
          this.currentToken = token;
          if (this.currentNotInHTML) {
            endTagInForeignContent(this, token);
          } else {
            this._endTagOutsideForeignContent(token);
          }
        }
        /** @protected */
        _endTagOutsideForeignContent(token) {
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              tokenInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HTML: {
              endTagBeforeHtml(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD: {
              endTagBeforeHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD: {
              endTagInHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD_NO_SCRIPT: {
              endTagInHeadNoScript(this, token);
              break;
            }
            case InsertionMode.AFTER_HEAD: {
              endTagAfterHead(this, token);
              break;
            }
            case InsertionMode.IN_BODY: {
              endTagInBody(this, token);
              break;
            }
            case InsertionMode.TEXT: {
              endTagInText(this, token);
              break;
            }
            case InsertionMode.IN_TABLE: {
              endTagInTable(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              tokenInTableText(this, token);
              break;
            }
            case InsertionMode.IN_CAPTION: {
              endTagInCaption(this, token);
              break;
            }
            case InsertionMode.IN_COLUMN_GROUP: {
              endTagInColumnGroup(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_BODY: {
              endTagInTableBody(this, token);
              break;
            }
            case InsertionMode.IN_ROW: {
              endTagInRow(this, token);
              break;
            }
            case InsertionMode.IN_CELL: {
              endTagInCell(this, token);
              break;
            }
            case InsertionMode.IN_SELECT: {
              endTagInSelect(this, token);
              break;
            }
            case InsertionMode.IN_SELECT_IN_TABLE: {
              endTagInSelectInTable(this, token);
              break;
            }
            case InsertionMode.IN_TEMPLATE: {
              endTagInTemplate(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY: {
              endTagAfterBody(this, token);
              break;
            }
            case InsertionMode.IN_FRAMESET: {
              endTagInFrameset(this, token);
              break;
            }
            case InsertionMode.AFTER_FRAMESET: {
              endTagAfterFrameset(this, token);
              break;
            }
            case InsertionMode.AFTER_AFTER_BODY: {
              tokenAfterAfterBody(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onEof(token) {
          switch (this.insertionMode) {
            case InsertionMode.INITIAL: {
              tokenInInitialMode(this, token);
              break;
            }
            case InsertionMode.BEFORE_HTML: {
              tokenBeforeHtml(this, token);
              break;
            }
            case InsertionMode.BEFORE_HEAD: {
              tokenBeforeHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD: {
              tokenInHead(this, token);
              break;
            }
            case InsertionMode.IN_HEAD_NO_SCRIPT: {
              tokenInHeadNoScript(this, token);
              break;
            }
            case InsertionMode.AFTER_HEAD: {
              tokenAfterHead(this, token);
              break;
            }
            case InsertionMode.IN_BODY:
            case InsertionMode.IN_TABLE:
            case InsertionMode.IN_CAPTION:
            case InsertionMode.IN_COLUMN_GROUP:
            case InsertionMode.IN_TABLE_BODY:
            case InsertionMode.IN_ROW:
            case InsertionMode.IN_CELL:
            case InsertionMode.IN_SELECT:
            case InsertionMode.IN_SELECT_IN_TABLE: {
              eofInBody(this, token);
              break;
            }
            case InsertionMode.TEXT: {
              eofInText(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              tokenInTableText(this, token);
              break;
            }
            case InsertionMode.IN_TEMPLATE: {
              eofInTemplate(this, token);
              break;
            }
            case InsertionMode.AFTER_BODY:
            case InsertionMode.IN_FRAMESET:
            case InsertionMode.AFTER_FRAMESET:
            case InsertionMode.AFTER_AFTER_BODY:
            case InsertionMode.AFTER_AFTER_FRAMESET: {
              stopParsing(this, token);
              break;
            }
            default:
          }
        }
        /** @internal */
        onWhitespaceCharacter(token) {
          if (this.skipNextNewLine) {
            this.skipNextNewLine = false;
            if (token.chars.charCodeAt(0) === CODE_POINTS.LINE_FEED) {
              if (token.chars.length === 1) {
                return;
              }
              token.chars = token.chars.substr(1);
            }
          }
          if (this.tokenizer.inForeignNode) {
            this._insertCharacters(token);
            return;
          }
          switch (this.insertionMode) {
            case InsertionMode.IN_HEAD:
            case InsertionMode.IN_HEAD_NO_SCRIPT:
            case InsertionMode.AFTER_HEAD:
            case InsertionMode.TEXT:
            case InsertionMode.IN_COLUMN_GROUP:
            case InsertionMode.IN_SELECT:
            case InsertionMode.IN_SELECT_IN_TABLE:
            case InsertionMode.IN_FRAMESET:
            case InsertionMode.AFTER_FRAMESET: {
              this._insertCharacters(token);
              break;
            }
            case InsertionMode.IN_BODY:
            case InsertionMode.IN_CAPTION:
            case InsertionMode.IN_CELL:
            case InsertionMode.IN_TEMPLATE:
            case InsertionMode.AFTER_BODY:
            case InsertionMode.AFTER_AFTER_BODY:
            case InsertionMode.AFTER_AFTER_FRAMESET: {
              whitespaceCharacterInBody(this, token);
              break;
            }
            case InsertionMode.IN_TABLE:
            case InsertionMode.IN_TABLE_BODY:
            case InsertionMode.IN_ROW: {
              characterInTable(this, token);
              break;
            }
            case InsertionMode.IN_TABLE_TEXT: {
              whitespaceCharacterInTableText(this, token);
              break;
            }
            default:
          }
        }
      };
      __name(_Parser, "Parser");
      Parser = _Parser;
      __name(aaObtainFormattingElementEntry, "aaObtainFormattingElementEntry");
      __name(aaObtainFurthestBlock, "aaObtainFurthestBlock");
      __name(aaInnerLoop, "aaInnerLoop");
      __name(aaRecreateElementFromEntry, "aaRecreateElementFromEntry");
      __name(aaInsertLastNodeInCommonAncestor, "aaInsertLastNodeInCommonAncestor");
      __name(aaReplaceFormattingElement, "aaReplaceFormattingElement");
      __name(callAdoptionAgency, "callAdoptionAgency");
      __name(appendComment, "appendComment");
      __name(appendCommentToRootHtmlElement, "appendCommentToRootHtmlElement");
      __name(appendCommentToDocument, "appendCommentToDocument");
      __name(stopParsing, "stopParsing");
      __name(doctypeInInitialMode, "doctypeInInitialMode");
      __name(tokenInInitialMode, "tokenInInitialMode");
      __name(startTagBeforeHtml, "startTagBeforeHtml");
      __name(endTagBeforeHtml, "endTagBeforeHtml");
      __name(tokenBeforeHtml, "tokenBeforeHtml");
      __name(startTagBeforeHead, "startTagBeforeHead");
      __name(endTagBeforeHead, "endTagBeforeHead");
      __name(tokenBeforeHead, "tokenBeforeHead");
      __name(startTagInHead, "startTagInHead");
      __name(endTagInHead, "endTagInHead");
      __name(templateEndTagInHead, "templateEndTagInHead");
      __name(tokenInHead, "tokenInHead");
      __name(startTagInHeadNoScript, "startTagInHeadNoScript");
      __name(endTagInHeadNoScript, "endTagInHeadNoScript");
      __name(tokenInHeadNoScript, "tokenInHeadNoScript");
      __name(startTagAfterHead, "startTagAfterHead");
      __name(endTagAfterHead, "endTagAfterHead");
      __name(tokenAfterHead, "tokenAfterHead");
      __name(modeInBody, "modeInBody");
      __name(whitespaceCharacterInBody, "whitespaceCharacterInBody");
      __name(characterInBody, "characterInBody");
      __name(htmlStartTagInBody, "htmlStartTagInBody");
      __name(bodyStartTagInBody, "bodyStartTagInBody");
      __name(framesetStartTagInBody, "framesetStartTagInBody");
      __name(addressStartTagInBody, "addressStartTagInBody");
      __name(numberedHeaderStartTagInBody, "numberedHeaderStartTagInBody");
      __name(preStartTagInBody, "preStartTagInBody");
      __name(formStartTagInBody, "formStartTagInBody");
      __name(listItemStartTagInBody, "listItemStartTagInBody");
      __name(plaintextStartTagInBody, "plaintextStartTagInBody");
      __name(buttonStartTagInBody, "buttonStartTagInBody");
      __name(aStartTagInBody, "aStartTagInBody");
      __name(bStartTagInBody, "bStartTagInBody");
      __name(nobrStartTagInBody, "nobrStartTagInBody");
      __name(appletStartTagInBody, "appletStartTagInBody");
      __name(tableStartTagInBody, "tableStartTagInBody");
      __name(areaStartTagInBody, "areaStartTagInBody");
      __name(isHiddenInput, "isHiddenInput");
      __name(inputStartTagInBody, "inputStartTagInBody");
      __name(paramStartTagInBody, "paramStartTagInBody");
      __name(hrStartTagInBody, "hrStartTagInBody");
      __name(imageStartTagInBody, "imageStartTagInBody");
      __name(textareaStartTagInBody, "textareaStartTagInBody");
      __name(xmpStartTagInBody, "xmpStartTagInBody");
      __name(iframeStartTagInBody, "iframeStartTagInBody");
      __name(rawTextStartTagInBody, "rawTextStartTagInBody");
      __name(selectStartTagInBody, "selectStartTagInBody");
      __name(optgroupStartTagInBody, "optgroupStartTagInBody");
      __name(rbStartTagInBody, "rbStartTagInBody");
      __name(rtStartTagInBody, "rtStartTagInBody");
      __name(mathStartTagInBody, "mathStartTagInBody");
      __name(svgStartTagInBody, "svgStartTagInBody");
      __name(genericStartTagInBody, "genericStartTagInBody");
      __name(startTagInBody, "startTagInBody");
      __name(bodyEndTagInBody, "bodyEndTagInBody");
      __name(htmlEndTagInBody, "htmlEndTagInBody");
      __name(addressEndTagInBody, "addressEndTagInBody");
      __name(formEndTagInBody, "formEndTagInBody");
      __name(pEndTagInBody, "pEndTagInBody");
      __name(liEndTagInBody, "liEndTagInBody");
      __name(ddEndTagInBody, "ddEndTagInBody");
      __name(numberedHeaderEndTagInBody, "numberedHeaderEndTagInBody");
      __name(appletEndTagInBody, "appletEndTagInBody");
      __name(brEndTagInBody, "brEndTagInBody");
      __name(genericEndTagInBody, "genericEndTagInBody");
      __name(endTagInBody, "endTagInBody");
      __name(eofInBody, "eofInBody");
      __name(endTagInText, "endTagInText");
      __name(eofInText, "eofInText");
      __name(characterInTable, "characterInTable");
      __name(captionStartTagInTable, "captionStartTagInTable");
      __name(colgroupStartTagInTable, "colgroupStartTagInTable");
      __name(colStartTagInTable, "colStartTagInTable");
      __name(tbodyStartTagInTable, "tbodyStartTagInTable");
      __name(tdStartTagInTable, "tdStartTagInTable");
      __name(tableStartTagInTable, "tableStartTagInTable");
      __name(inputStartTagInTable, "inputStartTagInTable");
      __name(formStartTagInTable, "formStartTagInTable");
      __name(startTagInTable, "startTagInTable");
      __name(endTagInTable, "endTagInTable");
      __name(tokenInTable, "tokenInTable");
      __name(whitespaceCharacterInTableText, "whitespaceCharacterInTableText");
      __name(characterInTableText, "characterInTableText");
      __name(tokenInTableText, "tokenInTableText");
      TABLE_VOID_ELEMENTS = /* @__PURE__ */ new Set([TAG_ID.CAPTION, TAG_ID.COL, TAG_ID.COLGROUP, TAG_ID.TBODY, TAG_ID.TD, TAG_ID.TFOOT, TAG_ID.TH, TAG_ID.THEAD, TAG_ID.TR]);
      __name(startTagInCaption, "startTagInCaption");
      __name(endTagInCaption, "endTagInCaption");
      __name(startTagInColumnGroup, "startTagInColumnGroup");
      __name(endTagInColumnGroup, "endTagInColumnGroup");
      __name(tokenInColumnGroup, "tokenInColumnGroup");
      __name(startTagInTableBody, "startTagInTableBody");
      __name(endTagInTableBody, "endTagInTableBody");
      __name(startTagInRow, "startTagInRow");
      __name(endTagInRow, "endTagInRow");
      __name(startTagInCell, "startTagInCell");
      __name(endTagInCell, "endTagInCell");
      __name(startTagInSelect, "startTagInSelect");
      __name(endTagInSelect, "endTagInSelect");
      __name(startTagInSelectInTable, "startTagInSelectInTable");
      __name(endTagInSelectInTable, "endTagInSelectInTable");
      __name(startTagInTemplate, "startTagInTemplate");
      __name(endTagInTemplate, "endTagInTemplate");
      __name(eofInTemplate, "eofInTemplate");
      __name(startTagAfterBody, "startTagAfterBody");
      __name(endTagAfterBody, "endTagAfterBody");
      __name(tokenAfterBody, "tokenAfterBody");
      __name(startTagInFrameset, "startTagInFrameset");
      __name(endTagInFrameset, "endTagInFrameset");
      __name(startTagAfterFrameset, "startTagAfterFrameset");
      __name(endTagAfterFrameset, "endTagAfterFrameset");
      __name(startTagAfterAfterBody, "startTagAfterAfterBody");
      __name(tokenAfterAfterBody, "tokenAfterAfterBody");
      __name(startTagAfterAfterFrameset, "startTagAfterAfterFrameset");
      __name(nullCharacterInForeignContent, "nullCharacterInForeignContent");
      __name(characterInForeignContent, "characterInForeignContent");
      __name(popUntilHtmlOrIntegrationPoint, "popUntilHtmlOrIntegrationPoint");
      __name(startTagInForeignContent, "startTagInForeignContent");
      __name(endTagInForeignContent, "endTagInForeignContent");
    }
  });

  // node_modules/parse5/dist/serializer/index.js
  function isVoidElement(node, options) {
    return options.treeAdapter.isElementNode(node) && options.treeAdapter.getNamespaceURI(node) === NS.HTML && VOID_ELEMENTS.has(options.treeAdapter.getTagName(node));
  }
  function serialize2(node, options) {
    const opts = { ...defaultOpts2, ...options };
    if (isVoidElement(node, opts)) {
      return "";
    }
    return serializeChildNodes(node, opts);
  }
  function serializeOuter(node, options) {
    const opts = { ...defaultOpts2, ...options };
    return serializeNode(node, opts);
  }
  function serializeChildNodes(parentNode, options) {
    let html3 = "";
    const container = options.treeAdapter.isElementNode(parentNode) && options.treeAdapter.getTagName(parentNode) === TAG_NAMES.TEMPLATE && options.treeAdapter.getNamespaceURI(parentNode) === NS.HTML ? options.treeAdapter.getTemplateContent(parentNode) : parentNode;
    const childNodes = options.treeAdapter.getChildNodes(container);
    if (childNodes) {
      for (const currentNode of childNodes) {
        html3 += serializeNode(currentNode, options);
      }
    }
    return html3;
  }
  function serializeNode(node, options) {
    if (options.treeAdapter.isElementNode(node)) {
      return serializeElement(node, options);
    }
    if (options.treeAdapter.isTextNode(node)) {
      return serializeTextNode(node, options);
    }
    if (options.treeAdapter.isCommentNode(node)) {
      return serializeCommentNode(node, options);
    }
    if (options.treeAdapter.isDocumentTypeNode(node)) {
      return serializeDocumentTypeNode(node, options);
    }
    return "";
  }
  function serializeElement(node, options) {
    const tn = options.treeAdapter.getTagName(node);
    return `<${tn}${serializeAttributes(node, options)}>${isVoidElement(node, options) ? "" : `${serializeChildNodes(node, options)}</${tn}>`}`;
  }
  function serializeAttributes(node, { treeAdapter }) {
    let html3 = "";
    for (const attr2 of treeAdapter.getAttrList(node)) {
      html3 += " ";
      if (attr2.namespace) {
        switch (attr2.namespace) {
          case NS.XML: {
            html3 += `xml:${attr2.name}`;
            break;
          }
          case NS.XMLNS: {
            if (attr2.name !== "xmlns") {
              html3 += "xmlns:";
            }
            html3 += attr2.name;
            break;
          }
          case NS.XLINK: {
            html3 += `xlink:${attr2.name}`;
            break;
          }
          default: {
            html3 += `${attr2.prefix}:${attr2.name}`;
          }
        }
      } else {
        html3 += attr2.name;
      }
      html3 += `="${(0, import_escape.escapeAttribute)(attr2.value)}"`;
    }
    return html3;
  }
  function serializeTextNode(node, options) {
    const { treeAdapter } = options;
    const content = treeAdapter.getTextNodeContent(node);
    const parent2 = treeAdapter.getParentNode(node);
    const parentTn = parent2 && treeAdapter.isElementNode(parent2) && treeAdapter.getTagName(parent2);
    return parentTn && treeAdapter.getNamespaceURI(parent2) === NS.HTML && hasUnescapedText(parentTn, options.scriptingEnabled) ? content : (0, import_escape.escapeText)(content);
  }
  function serializeCommentNode(node, { treeAdapter }) {
    return `<!--${treeAdapter.getCommentNodeContent(node)}-->`;
  }
  function serializeDocumentTypeNode(node, { treeAdapter }) {
    return `<!DOCTYPE ${treeAdapter.getDocumentTypeNodeName(node)}>`;
  }
  var import_escape, VOID_ELEMENTS, defaultOpts2;
  var init_serializer = __esm({
    "node_modules/parse5/dist/serializer/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_html();
      import_escape = __toESM(require_escape(), 1);
      init_default();
      VOID_ELEMENTS = /* @__PURE__ */ new Set([
        TAG_NAMES.AREA,
        TAG_NAMES.BASE,
        TAG_NAMES.BASEFONT,
        TAG_NAMES.BGSOUND,
        TAG_NAMES.BR,
        TAG_NAMES.COL,
        TAG_NAMES.EMBED,
        TAG_NAMES.FRAME,
        TAG_NAMES.HR,
        TAG_NAMES.IMG,
        TAG_NAMES.INPUT,
        TAG_NAMES.KEYGEN,
        TAG_NAMES.LINK,
        TAG_NAMES.META,
        TAG_NAMES.PARAM,
        TAG_NAMES.SOURCE,
        TAG_NAMES.TRACK,
        TAG_NAMES.WBR
      ]);
      __name(isVoidElement, "isVoidElement");
      defaultOpts2 = { treeAdapter: defaultTreeAdapter, scriptingEnabled: true };
      __name(serialize2, "serialize");
      __name(serializeOuter, "serializeOuter");
      __name(serializeChildNodes, "serializeChildNodes");
      __name(serializeNode, "serializeNode");
      __name(serializeElement, "serializeElement");
      __name(serializeAttributes, "serializeAttributes");
      __name(serializeTextNode, "serializeTextNode");
      __name(serializeCommentNode, "serializeCommentNode");
      __name(serializeDocumentTypeNode, "serializeDocumentTypeNode");
    }
  });

  // node_modules/parse5/dist/index.js
  function parse3(html3, options) {
    return Parser.parse(html3, options);
  }
  function parseFragment(fragmentContext, html3, options) {
    if (typeof fragmentContext === "string") {
      options = html3;
      html3 = fragmentContext;
      fragmentContext = null;
    }
    const parser = Parser.getFragmentParser(fragmentContext, options);
    parser.tokenizer.write(html3, true);
    return parser.getFragment();
  }
  var init_dist = __esm({
    "node_modules/parse5/dist/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_parser();
      init_default();
      init_parser();
      init_serializer();
      init_error_codes();
      init_foreign_content();
      init_html();
      init_token();
      init_tokenizer();
      __name(parse3, "parse");
      __name(parseFragment, "parseFragment");
    }
  });

  // node_modules/parse5-htmlparser2-tree-adapter/dist/index.js
  function enquoteDoctypeId(id) {
    const quote = id.includes('"') ? "'" : '"';
    return quote + id + quote;
  }
  function serializeDoctypeContent(name, publicId, systemId) {
    let str = "!DOCTYPE ";
    if (name) {
      str += name;
    }
    if (publicId) {
      str += ` PUBLIC ${enquoteDoctypeId(publicId)}`;
    } else if (systemId) {
      str += " SYSTEM";
    }
    if (systemId) {
      str += ` ${enquoteDoctypeId(systemId)}`;
    }
    return str;
  }
  var import_domhandler7, adapter;
  var init_dist2 = __esm({
    "node_modules/parse5-htmlparser2-tree-adapter/dist/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_dist();
      import_domhandler7 = __toESM(require_lib2(), 1);
      __name(enquoteDoctypeId, "enquoteDoctypeId");
      __name(serializeDoctypeContent, "serializeDoctypeContent");
      adapter = {
        // Re-exports from domhandler
        isCommentNode: import_domhandler7.isComment,
        isElementNode: import_domhandler7.isTag,
        isTextNode: import_domhandler7.isText,
        //Node construction
        createDocument() {
          const node = new import_domhandler7.Document([]);
          node["x-mode"] = html_exports.DOCUMENT_MODE.NO_QUIRKS;
          return node;
        },
        createDocumentFragment() {
          return new import_domhandler7.Document([]);
        },
        createElement(tagName, namespaceURI, attrs) {
          const attribs = /* @__PURE__ */ Object.create(null);
          const attribsNamespace = /* @__PURE__ */ Object.create(null);
          const attribsPrefix = /* @__PURE__ */ Object.create(null);
          for (let i = 0; i < attrs.length; i++) {
            const attrName = attrs[i].name;
            attribs[attrName] = attrs[i].value;
            attribsNamespace[attrName] = attrs[i].namespace;
            attribsPrefix[attrName] = attrs[i].prefix;
          }
          const node = new import_domhandler7.Element(tagName, attribs, []);
          node.namespace = namespaceURI;
          node["x-attribsNamespace"] = attribsNamespace;
          node["x-attribsPrefix"] = attribsPrefix;
          return node;
        },
        createCommentNode(data2) {
          return new import_domhandler7.Comment(data2);
        },
        createTextNode(value) {
          return new import_domhandler7.Text(value);
        },
        //Tree mutation
        appendChild(parentNode, newNode) {
          const prev2 = parentNode.children[parentNode.children.length - 1];
          if (prev2) {
            prev2.next = newNode;
            newNode.prev = prev2;
          }
          parentNode.children.push(newNode);
          newNode.parent = parentNode;
        },
        insertBefore(parentNode, newNode, referenceNode) {
          const insertionIdx = parentNode.children.indexOf(referenceNode);
          const { prev: prev2 } = referenceNode;
          if (prev2) {
            prev2.next = newNode;
            newNode.prev = prev2;
          }
          referenceNode.prev = newNode;
          newNode.next = referenceNode;
          parentNode.children.splice(insertionIdx, 0, newNode);
          newNode.parent = parentNode;
        },
        setTemplateContent(templateElement, contentElement) {
          adapter.appendChild(templateElement, contentElement);
        },
        getTemplateContent(templateElement) {
          return templateElement.children[0];
        },
        setDocumentType(document, name, publicId, systemId) {
          const data2 = serializeDoctypeContent(name, publicId, systemId);
          let doctypeNode = document.children.find((node) => (0, import_domhandler7.isDirective)(node) && node.name === "!doctype");
          if (doctypeNode) {
            doctypeNode.data = data2 !== null && data2 !== void 0 ? data2 : null;
          } else {
            doctypeNode = new import_domhandler7.ProcessingInstruction("!doctype", data2);
            adapter.appendChild(document, doctypeNode);
          }
          doctypeNode["x-name"] = name;
          doctypeNode["x-publicId"] = publicId;
          doctypeNode["x-systemId"] = systemId;
        },
        setDocumentMode(document, mode) {
          document["x-mode"] = mode;
        },
        getDocumentMode(document) {
          return document["x-mode"];
        },
        detachNode(node) {
          if (node.parent) {
            const idx = node.parent.children.indexOf(node);
            const { prev: prev2, next: next2 } = node;
            node.prev = null;
            node.next = null;
            if (prev2) {
              prev2.next = next2;
            }
            if (next2) {
              next2.prev = prev2;
            }
            node.parent.children.splice(idx, 1);
            node.parent = null;
          }
        },
        insertText(parentNode, text3) {
          const lastChild = parentNode.children[parentNode.children.length - 1];
          if (lastChild && (0, import_domhandler7.isText)(lastChild)) {
            lastChild.data += text3;
          } else {
            adapter.appendChild(parentNode, adapter.createTextNode(text3));
          }
        },
        insertTextBefore(parentNode, text3, referenceNode) {
          const prevNode = parentNode.children[parentNode.children.indexOf(referenceNode) - 1];
          if (prevNode && (0, import_domhandler7.isText)(prevNode)) {
            prevNode.data += text3;
          } else {
            adapter.insertBefore(parentNode, adapter.createTextNode(text3), referenceNode);
          }
        },
        adoptAttributes(recipient, attrs) {
          for (let i = 0; i < attrs.length; i++) {
            const attrName = attrs[i].name;
            if (recipient.attribs[attrName] === void 0) {
              recipient.attribs[attrName] = attrs[i].value;
              recipient["x-attribsNamespace"][attrName] = attrs[i].namespace;
              recipient["x-attribsPrefix"][attrName] = attrs[i].prefix;
            }
          }
        },
        //Tree traversing
        getFirstChild(node) {
          return node.children[0];
        },
        getChildNodes(node) {
          return node.children;
        },
        getParentNode(node) {
          return node.parent;
        },
        getAttrList(element) {
          return element.attributes;
        },
        //Node data
        getTagName(element) {
          return element.name;
        },
        getNamespaceURI(element) {
          return element.namespace;
        },
        getTextNodeContent(textNode) {
          return textNode.data;
        },
        getCommentNodeContent(commentNode) {
          return commentNode.data;
        },
        getDocumentTypeNodeName(doctypeNode) {
          var _a;
          return (_a = doctypeNode["x-name"]) !== null && _a !== void 0 ? _a : "";
        },
        getDocumentTypeNodePublicId(doctypeNode) {
          var _a;
          return (_a = doctypeNode["x-publicId"]) !== null && _a !== void 0 ? _a : "";
        },
        getDocumentTypeNodeSystemId(doctypeNode) {
          var _a;
          return (_a = doctypeNode["x-systemId"]) !== null && _a !== void 0 ? _a : "";
        },
        //Node types
        isDocumentTypeNode(node) {
          return (0, import_domhandler7.isDirective)(node) && node.name === "!doctype";
        },
        // Source code location
        setNodeSourceCodeLocation(node, location) {
          if (location) {
            node.startIndex = location.startOffset;
            node.endIndex = location.endOffset;
          }
          node.sourceCodeLocation = location;
        },
        getNodeSourceCodeLocation(node) {
          return node.sourceCodeLocation;
        },
        updateNodeSourceCodeLocation(node, endLocation) {
          if (endLocation.endOffset != null)
            node.endIndex = endLocation.endOffset;
          node.sourceCodeLocation = {
            ...node.sourceCodeLocation,
            ...endLocation
          };
        }
      };
    }
  });

  // node_modules/cheerio/dist/browser/parsers/parse5-adapter.js
  function parseWithParse5(content, options, isDocument3, context) {
    var _a;
    (_a = options.treeAdapter) !== null && _a !== void 0 ? _a : options.treeAdapter = adapter;
    if (options.scriptingEnabled !== false) {
      options.scriptingEnabled = true;
    }
    return isDocument3 ? parse3(content, options) : parseFragment(context, content, options);
  }
  function renderWithParse5(dom) {
    const nodes = "length" in dom ? dom : [dom];
    for (let index2 = 0; index2 < nodes.length; index2 += 1) {
      const node = nodes[index2];
      if ((0, import_domhandler8.isDocument)(node)) {
        Array.prototype.splice.call(nodes, index2, 1, ...node.children);
      }
    }
    let result = "";
    for (let index2 = 0; index2 < nodes.length; index2 += 1) {
      const node = nodes[index2];
      result += serializeOuter(node, renderOpts);
    }
    return result;
  }
  var import_domhandler8, renderOpts;
  var init_parse5_adapter = __esm({
    "node_modules/cheerio/dist/browser/parsers/parse5-adapter.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      import_domhandler8 = __toESM(require_lib2(), 1);
      init_dist();
      init_dist2();
      __name(parseWithParse5, "parseWithParse5");
      renderOpts = { treeAdapter: adapter };
      __name(renderWithParse5, "renderWithParse5");
    }
  });

  // node_modules/htmlparser2/lib/Tokenizer.js
  var require_Tokenizer = __commonJS({
    "node_modules/htmlparser2/lib/Tokenizer.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.QuoteType = void 0;
      var decode_js_1 = require_decode();
      var CharCodes;
      (function(CharCodes2) {
        CharCodes2[CharCodes2["Tab"] = 9] = "Tab";
        CharCodes2[CharCodes2["NewLine"] = 10] = "NewLine";
        CharCodes2[CharCodes2["FormFeed"] = 12] = "FormFeed";
        CharCodes2[CharCodes2["CarriageReturn"] = 13] = "CarriageReturn";
        CharCodes2[CharCodes2["Space"] = 32] = "Space";
        CharCodes2[CharCodes2["ExclamationMark"] = 33] = "ExclamationMark";
        CharCodes2[CharCodes2["Number"] = 35] = "Number";
        CharCodes2[CharCodes2["Amp"] = 38] = "Amp";
        CharCodes2[CharCodes2["SingleQuote"] = 39] = "SingleQuote";
        CharCodes2[CharCodes2["DoubleQuote"] = 34] = "DoubleQuote";
        CharCodes2[CharCodes2["Dash"] = 45] = "Dash";
        CharCodes2[CharCodes2["Slash"] = 47] = "Slash";
        CharCodes2[CharCodes2["Zero"] = 48] = "Zero";
        CharCodes2[CharCodes2["Nine"] = 57] = "Nine";
        CharCodes2[CharCodes2["Semi"] = 59] = "Semi";
        CharCodes2[CharCodes2["Lt"] = 60] = "Lt";
        CharCodes2[CharCodes2["Eq"] = 61] = "Eq";
        CharCodes2[CharCodes2["Gt"] = 62] = "Gt";
        CharCodes2[CharCodes2["Questionmark"] = 63] = "Questionmark";
        CharCodes2[CharCodes2["UpperA"] = 65] = "UpperA";
        CharCodes2[CharCodes2["LowerA"] = 97] = "LowerA";
        CharCodes2[CharCodes2["UpperF"] = 70] = "UpperF";
        CharCodes2[CharCodes2["LowerF"] = 102] = "LowerF";
        CharCodes2[CharCodes2["UpperZ"] = 90] = "UpperZ";
        CharCodes2[CharCodes2["LowerZ"] = 122] = "LowerZ";
        CharCodes2[CharCodes2["LowerX"] = 120] = "LowerX";
        CharCodes2[CharCodes2["OpeningSquareBracket"] = 91] = "OpeningSquareBracket";
      })(CharCodes || (CharCodes = {}));
      var State2;
      (function(State3) {
        State3[State3["Text"] = 1] = "Text";
        State3[State3["BeforeTagName"] = 2] = "BeforeTagName";
        State3[State3["InTagName"] = 3] = "InTagName";
        State3[State3["InSelfClosingTag"] = 4] = "InSelfClosingTag";
        State3[State3["BeforeClosingTagName"] = 5] = "BeforeClosingTagName";
        State3[State3["InClosingTagName"] = 6] = "InClosingTagName";
        State3[State3["AfterClosingTagName"] = 7] = "AfterClosingTagName";
        State3[State3["BeforeAttributeName"] = 8] = "BeforeAttributeName";
        State3[State3["InAttributeName"] = 9] = "InAttributeName";
        State3[State3["AfterAttributeName"] = 10] = "AfterAttributeName";
        State3[State3["BeforeAttributeValue"] = 11] = "BeforeAttributeValue";
        State3[State3["InAttributeValueDq"] = 12] = "InAttributeValueDq";
        State3[State3["InAttributeValueSq"] = 13] = "InAttributeValueSq";
        State3[State3["InAttributeValueNq"] = 14] = "InAttributeValueNq";
        State3[State3["BeforeDeclaration"] = 15] = "BeforeDeclaration";
        State3[State3["InDeclaration"] = 16] = "InDeclaration";
        State3[State3["InProcessingInstruction"] = 17] = "InProcessingInstruction";
        State3[State3["BeforeComment"] = 18] = "BeforeComment";
        State3[State3["CDATASequence"] = 19] = "CDATASequence";
        State3[State3["InSpecialComment"] = 20] = "InSpecialComment";
        State3[State3["InCommentLike"] = 21] = "InCommentLike";
        State3[State3["BeforeSpecialS"] = 22] = "BeforeSpecialS";
        State3[State3["BeforeSpecialT"] = 23] = "BeforeSpecialT";
        State3[State3["SpecialStartSequence"] = 24] = "SpecialStartSequence";
        State3[State3["InSpecialTag"] = 25] = "InSpecialTag";
        State3[State3["InEntity"] = 26] = "InEntity";
      })(State2 || (State2 = {}));
      function isWhitespace3(c) {
        return c === CharCodes.Space || c === CharCodes.NewLine || c === CharCodes.Tab || c === CharCodes.FormFeed || c === CharCodes.CarriageReturn;
      }
      __name(isWhitespace3, "isWhitespace");
      function isEndOfTagSection(c) {
        return c === CharCodes.Slash || c === CharCodes.Gt || isWhitespace3(c);
      }
      __name(isEndOfTagSection, "isEndOfTagSection");
      function isASCIIAlpha(c) {
        return c >= CharCodes.LowerA && c <= CharCodes.LowerZ || c >= CharCodes.UpperA && c <= CharCodes.UpperZ;
      }
      __name(isASCIIAlpha, "isASCIIAlpha");
      var QuoteType;
      (function(QuoteType2) {
        QuoteType2[QuoteType2["NoValue"] = 0] = "NoValue";
        QuoteType2[QuoteType2["Unquoted"] = 1] = "Unquoted";
        QuoteType2[QuoteType2["Single"] = 2] = "Single";
        QuoteType2[QuoteType2["Double"] = 3] = "Double";
      })(QuoteType || (exports4.QuoteType = QuoteType = {}));
      var Sequences = {
        Cdata: new Uint8Array([67, 68, 65, 84, 65, 91]),
        // CDATA[
        CdataEnd: new Uint8Array([93, 93, 62]),
        // ]]>
        CommentEnd: new Uint8Array([45, 45, 62]),
        // `-->`
        ScriptEnd: new Uint8Array([60, 47, 115, 99, 114, 105, 112, 116]),
        // `<\/script`
        StyleEnd: new Uint8Array([60, 47, 115, 116, 121, 108, 101]),
        // `</style`
        TitleEnd: new Uint8Array([60, 47, 116, 105, 116, 108, 101]),
        // `</title`
        TextareaEnd: new Uint8Array([
          60,
          47,
          116,
          101,
          120,
          116,
          97,
          114,
          101,
          97
        ])
        // `</textarea`
      };
      var Tokenizer2 = (
        /** @class */
        function() {
          function Tokenizer3(_a, cbs) {
            var _b = _a.xmlMode, xmlMode = _b === void 0 ? false : _b, _c = _a.decodeEntities, decodeEntities = _c === void 0 ? true : _c;
            var _this = this;
            this.cbs = cbs;
            this.state = State2.Text;
            this.buffer = "";
            this.sectionStart = 0;
            this.index = 0;
            this.entityStart = 0;
            this.baseState = State2.Text;
            this.isSpecial = false;
            this.running = true;
            this.offset = 0;
            this.currentSequence = void 0;
            this.sequenceIndex = 0;
            this.xmlMode = xmlMode;
            this.decodeEntities = decodeEntities;
            this.entityDecoder = new decode_js_1.EntityDecoder(xmlMode ? decode_js_1.xmlDecodeTree : decode_js_1.htmlDecodeTree, function(cp, consumed) {
              return _this.emitCodePoint(cp, consumed);
            });
          }
          __name(Tokenizer3, "Tokenizer");
          Tokenizer3.prototype.reset = function() {
            this.state = State2.Text;
            this.buffer = "";
            this.sectionStart = 0;
            this.index = 0;
            this.baseState = State2.Text;
            this.currentSequence = void 0;
            this.running = true;
            this.offset = 0;
          };
          Tokenizer3.prototype.write = function(chunk) {
            this.offset += this.buffer.length;
            this.buffer = chunk;
            this.parse();
          };
          Tokenizer3.prototype.end = function() {
            if (this.running)
              this.finish();
          };
          Tokenizer3.prototype.pause = function() {
            this.running = false;
          };
          Tokenizer3.prototype.resume = function() {
            this.running = true;
            if (this.index < this.buffer.length + this.offset) {
              this.parse();
            }
          };
          Tokenizer3.prototype.stateText = function(c) {
            if (c === CharCodes.Lt || !this.decodeEntities && this.fastForwardTo(CharCodes.Lt)) {
              if (this.index > this.sectionStart) {
                this.cbs.ontext(this.sectionStart, this.index);
              }
              this.state = State2.BeforeTagName;
              this.sectionStart = this.index;
            } else if (this.decodeEntities && c === CharCodes.Amp) {
              this.startEntity();
            }
          };
          Tokenizer3.prototype.stateSpecialStartSequence = function(c) {
            var isEnd = this.sequenceIndex === this.currentSequence.length;
            var isMatch = isEnd ? (
              // If we are at the end of the sequence, make sure the tag name has ended
              isEndOfTagSection(c)
            ) : (
              // Otherwise, do a case-insensitive comparison
              (c | 32) === this.currentSequence[this.sequenceIndex]
            );
            if (!isMatch) {
              this.isSpecial = false;
            } else if (!isEnd) {
              this.sequenceIndex++;
              return;
            }
            this.sequenceIndex = 0;
            this.state = State2.InTagName;
            this.stateInTagName(c);
          };
          Tokenizer3.prototype.stateInSpecialTag = function(c) {
            if (this.sequenceIndex === this.currentSequence.length) {
              if (c === CharCodes.Gt || isWhitespace3(c)) {
                var endOfText = this.index - this.currentSequence.length;
                if (this.sectionStart < endOfText) {
                  var actualIndex = this.index;
                  this.index = endOfText;
                  this.cbs.ontext(this.sectionStart, endOfText);
                  this.index = actualIndex;
                }
                this.isSpecial = false;
                this.sectionStart = endOfText + 2;
                this.stateInClosingTagName(c);
                return;
              }
              this.sequenceIndex = 0;
            }
            if ((c | 32) === this.currentSequence[this.sequenceIndex]) {
              this.sequenceIndex += 1;
            } else if (this.sequenceIndex === 0) {
              if (this.currentSequence === Sequences.TitleEnd) {
                if (this.decodeEntities && c === CharCodes.Amp) {
                  this.startEntity();
                }
              } else if (this.fastForwardTo(CharCodes.Lt)) {
                this.sequenceIndex = 1;
              }
            } else {
              this.sequenceIndex = Number(c === CharCodes.Lt);
            }
          };
          Tokenizer3.prototype.stateCDATASequence = function(c) {
            if (c === Sequences.Cdata[this.sequenceIndex]) {
              if (++this.sequenceIndex === Sequences.Cdata.length) {
                this.state = State2.InCommentLike;
                this.currentSequence = Sequences.CdataEnd;
                this.sequenceIndex = 0;
                this.sectionStart = this.index + 1;
              }
            } else {
              this.sequenceIndex = 0;
              this.state = State2.InDeclaration;
              this.stateInDeclaration(c);
            }
          };
          Tokenizer3.prototype.fastForwardTo = function(c) {
            while (++this.index < this.buffer.length + this.offset) {
              if (this.buffer.charCodeAt(this.index - this.offset) === c) {
                return true;
              }
            }
            this.index = this.buffer.length + this.offset - 1;
            return false;
          };
          Tokenizer3.prototype.stateInCommentLike = function(c) {
            if (c === this.currentSequence[this.sequenceIndex]) {
              if (++this.sequenceIndex === this.currentSequence.length) {
                if (this.currentSequence === Sequences.CdataEnd) {
                  this.cbs.oncdata(this.sectionStart, this.index, 2);
                } else {
                  this.cbs.oncomment(this.sectionStart, this.index, 2);
                }
                this.sequenceIndex = 0;
                this.sectionStart = this.index + 1;
                this.state = State2.Text;
              }
            } else if (this.sequenceIndex === 0) {
              if (this.fastForwardTo(this.currentSequence[0])) {
                this.sequenceIndex = 1;
              }
            } else if (c !== this.currentSequence[this.sequenceIndex - 1]) {
              this.sequenceIndex = 0;
            }
          };
          Tokenizer3.prototype.isTagStartChar = function(c) {
            return this.xmlMode ? !isEndOfTagSection(c) : isASCIIAlpha(c);
          };
          Tokenizer3.prototype.startSpecial = function(sequence, offset) {
            this.isSpecial = true;
            this.currentSequence = sequence;
            this.sequenceIndex = offset;
            this.state = State2.SpecialStartSequence;
          };
          Tokenizer3.prototype.stateBeforeTagName = function(c) {
            if (c === CharCodes.ExclamationMark) {
              this.state = State2.BeforeDeclaration;
              this.sectionStart = this.index + 1;
            } else if (c === CharCodes.Questionmark) {
              this.state = State2.InProcessingInstruction;
              this.sectionStart = this.index + 1;
            } else if (this.isTagStartChar(c)) {
              var lower = c | 32;
              this.sectionStart = this.index;
              if (this.xmlMode) {
                this.state = State2.InTagName;
              } else if (lower === Sequences.ScriptEnd[2]) {
                this.state = State2.BeforeSpecialS;
              } else if (lower === Sequences.TitleEnd[2]) {
                this.state = State2.BeforeSpecialT;
              } else {
                this.state = State2.InTagName;
              }
            } else if (c === CharCodes.Slash) {
              this.state = State2.BeforeClosingTagName;
            } else {
              this.state = State2.Text;
              this.stateText(c);
            }
          };
          Tokenizer3.prototype.stateInTagName = function(c) {
            if (isEndOfTagSection(c)) {
              this.cbs.onopentagname(this.sectionStart, this.index);
              this.sectionStart = -1;
              this.state = State2.BeforeAttributeName;
              this.stateBeforeAttributeName(c);
            }
          };
          Tokenizer3.prototype.stateBeforeClosingTagName = function(c) {
            if (isWhitespace3(c)) {
            } else if (c === CharCodes.Gt) {
              this.state = State2.Text;
            } else {
              this.state = this.isTagStartChar(c) ? State2.InClosingTagName : State2.InSpecialComment;
              this.sectionStart = this.index;
            }
          };
          Tokenizer3.prototype.stateInClosingTagName = function(c) {
            if (c === CharCodes.Gt || isWhitespace3(c)) {
              this.cbs.onclosetag(this.sectionStart, this.index);
              this.sectionStart = -1;
              this.state = State2.AfterClosingTagName;
              this.stateAfterClosingTagName(c);
            }
          };
          Tokenizer3.prototype.stateAfterClosingTagName = function(c) {
            if (c === CharCodes.Gt || this.fastForwardTo(CharCodes.Gt)) {
              this.state = State2.Text;
              this.sectionStart = this.index + 1;
            }
          };
          Tokenizer3.prototype.stateBeforeAttributeName = function(c) {
            if (c === CharCodes.Gt) {
              this.cbs.onopentagend(this.index);
              if (this.isSpecial) {
                this.state = State2.InSpecialTag;
                this.sequenceIndex = 0;
              } else {
                this.state = State2.Text;
              }
              this.sectionStart = this.index + 1;
            } else if (c === CharCodes.Slash) {
              this.state = State2.InSelfClosingTag;
            } else if (!isWhitespace3(c)) {
              this.state = State2.InAttributeName;
              this.sectionStart = this.index;
            }
          };
          Tokenizer3.prototype.stateInSelfClosingTag = function(c) {
            if (c === CharCodes.Gt) {
              this.cbs.onselfclosingtag(this.index);
              this.state = State2.Text;
              this.sectionStart = this.index + 1;
              this.isSpecial = false;
            } else if (!isWhitespace3(c)) {
              this.state = State2.BeforeAttributeName;
              this.stateBeforeAttributeName(c);
            }
          };
          Tokenizer3.prototype.stateInAttributeName = function(c) {
            if (c === CharCodes.Eq || isEndOfTagSection(c)) {
              this.cbs.onattribname(this.sectionStart, this.index);
              this.sectionStart = this.index;
              this.state = State2.AfterAttributeName;
              this.stateAfterAttributeName(c);
            }
          };
          Tokenizer3.prototype.stateAfterAttributeName = function(c) {
            if (c === CharCodes.Eq) {
              this.state = State2.BeforeAttributeValue;
            } else if (c === CharCodes.Slash || c === CharCodes.Gt) {
              this.cbs.onattribend(QuoteType.NoValue, this.sectionStart);
              this.sectionStart = -1;
              this.state = State2.BeforeAttributeName;
              this.stateBeforeAttributeName(c);
            } else if (!isWhitespace3(c)) {
              this.cbs.onattribend(QuoteType.NoValue, this.sectionStart);
              this.state = State2.InAttributeName;
              this.sectionStart = this.index;
            }
          };
          Tokenizer3.prototype.stateBeforeAttributeValue = function(c) {
            if (c === CharCodes.DoubleQuote) {
              this.state = State2.InAttributeValueDq;
              this.sectionStart = this.index + 1;
            } else if (c === CharCodes.SingleQuote) {
              this.state = State2.InAttributeValueSq;
              this.sectionStart = this.index + 1;
            } else if (!isWhitespace3(c)) {
              this.sectionStart = this.index;
              this.state = State2.InAttributeValueNq;
              this.stateInAttributeValueNoQuotes(c);
            }
          };
          Tokenizer3.prototype.handleInAttributeValue = function(c, quote) {
            if (c === quote || !this.decodeEntities && this.fastForwardTo(quote)) {
              this.cbs.onattribdata(this.sectionStart, this.index);
              this.sectionStart = -1;
              this.cbs.onattribend(quote === CharCodes.DoubleQuote ? QuoteType.Double : QuoteType.Single, this.index + 1);
              this.state = State2.BeforeAttributeName;
            } else if (this.decodeEntities && c === CharCodes.Amp) {
              this.startEntity();
            }
          };
          Tokenizer3.prototype.stateInAttributeValueDoubleQuotes = function(c) {
            this.handleInAttributeValue(c, CharCodes.DoubleQuote);
          };
          Tokenizer3.prototype.stateInAttributeValueSingleQuotes = function(c) {
            this.handleInAttributeValue(c, CharCodes.SingleQuote);
          };
          Tokenizer3.prototype.stateInAttributeValueNoQuotes = function(c) {
            if (isWhitespace3(c) || c === CharCodes.Gt) {
              this.cbs.onattribdata(this.sectionStart, this.index);
              this.sectionStart = -1;
              this.cbs.onattribend(QuoteType.Unquoted, this.index);
              this.state = State2.BeforeAttributeName;
              this.stateBeforeAttributeName(c);
            } else if (this.decodeEntities && c === CharCodes.Amp) {
              this.startEntity();
            }
          };
          Tokenizer3.prototype.stateBeforeDeclaration = function(c) {
            if (c === CharCodes.OpeningSquareBracket) {
              this.state = State2.CDATASequence;
              this.sequenceIndex = 0;
            } else {
              this.state = c === CharCodes.Dash ? State2.BeforeComment : State2.InDeclaration;
            }
          };
          Tokenizer3.prototype.stateInDeclaration = function(c) {
            if (c === CharCodes.Gt || this.fastForwardTo(CharCodes.Gt)) {
              this.cbs.ondeclaration(this.sectionStart, this.index);
              this.state = State2.Text;
              this.sectionStart = this.index + 1;
            }
          };
          Tokenizer3.prototype.stateInProcessingInstruction = function(c) {
            if (c === CharCodes.Gt || this.fastForwardTo(CharCodes.Gt)) {
              this.cbs.onprocessinginstruction(this.sectionStart, this.index);
              this.state = State2.Text;
              this.sectionStart = this.index + 1;
            }
          };
          Tokenizer3.prototype.stateBeforeComment = function(c) {
            if (c === CharCodes.Dash) {
              this.state = State2.InCommentLike;
              this.currentSequence = Sequences.CommentEnd;
              this.sequenceIndex = 2;
              this.sectionStart = this.index + 1;
            } else {
              this.state = State2.InDeclaration;
            }
          };
          Tokenizer3.prototype.stateInSpecialComment = function(c) {
            if (c === CharCodes.Gt || this.fastForwardTo(CharCodes.Gt)) {
              this.cbs.oncomment(this.sectionStart, this.index, 0);
              this.state = State2.Text;
              this.sectionStart = this.index + 1;
            }
          };
          Tokenizer3.prototype.stateBeforeSpecialS = function(c) {
            var lower = c | 32;
            if (lower === Sequences.ScriptEnd[3]) {
              this.startSpecial(Sequences.ScriptEnd, 4);
            } else if (lower === Sequences.StyleEnd[3]) {
              this.startSpecial(Sequences.StyleEnd, 4);
            } else {
              this.state = State2.InTagName;
              this.stateInTagName(c);
            }
          };
          Tokenizer3.prototype.stateBeforeSpecialT = function(c) {
            var lower = c | 32;
            if (lower === Sequences.TitleEnd[3]) {
              this.startSpecial(Sequences.TitleEnd, 4);
            } else if (lower === Sequences.TextareaEnd[3]) {
              this.startSpecial(Sequences.TextareaEnd, 4);
            } else {
              this.state = State2.InTagName;
              this.stateInTagName(c);
            }
          };
          Tokenizer3.prototype.startEntity = function() {
            this.baseState = this.state;
            this.state = State2.InEntity;
            this.entityStart = this.index;
            this.entityDecoder.startEntity(this.xmlMode ? decode_js_1.DecodingMode.Strict : this.baseState === State2.Text || this.baseState === State2.InSpecialTag ? decode_js_1.DecodingMode.Legacy : decode_js_1.DecodingMode.Attribute);
          };
          Tokenizer3.prototype.stateInEntity = function() {
            var length = this.entityDecoder.write(this.buffer, this.index - this.offset);
            if (length >= 0) {
              this.state = this.baseState;
              if (length === 0) {
                this.index = this.entityStart;
              }
            } else {
              this.index = this.offset + this.buffer.length - 1;
            }
          };
          Tokenizer3.prototype.cleanup = function() {
            if (this.running && this.sectionStart !== this.index) {
              if (this.state === State2.Text || this.state === State2.InSpecialTag && this.sequenceIndex === 0) {
                this.cbs.ontext(this.sectionStart, this.index);
                this.sectionStart = this.index;
              } else if (this.state === State2.InAttributeValueDq || this.state === State2.InAttributeValueSq || this.state === State2.InAttributeValueNq) {
                this.cbs.onattribdata(this.sectionStart, this.index);
                this.sectionStart = this.index;
              }
            }
          };
          Tokenizer3.prototype.shouldContinue = function() {
            return this.index < this.buffer.length + this.offset && this.running;
          };
          Tokenizer3.prototype.parse = function() {
            while (this.shouldContinue()) {
              var c = this.buffer.charCodeAt(this.index - this.offset);
              switch (this.state) {
                case State2.Text: {
                  this.stateText(c);
                  break;
                }
                case State2.SpecialStartSequence: {
                  this.stateSpecialStartSequence(c);
                  break;
                }
                case State2.InSpecialTag: {
                  this.stateInSpecialTag(c);
                  break;
                }
                case State2.CDATASequence: {
                  this.stateCDATASequence(c);
                  break;
                }
                case State2.InAttributeValueDq: {
                  this.stateInAttributeValueDoubleQuotes(c);
                  break;
                }
                case State2.InAttributeName: {
                  this.stateInAttributeName(c);
                  break;
                }
                case State2.InCommentLike: {
                  this.stateInCommentLike(c);
                  break;
                }
                case State2.InSpecialComment: {
                  this.stateInSpecialComment(c);
                  break;
                }
                case State2.BeforeAttributeName: {
                  this.stateBeforeAttributeName(c);
                  break;
                }
                case State2.InTagName: {
                  this.stateInTagName(c);
                  break;
                }
                case State2.InClosingTagName: {
                  this.stateInClosingTagName(c);
                  break;
                }
                case State2.BeforeTagName: {
                  this.stateBeforeTagName(c);
                  break;
                }
                case State2.AfterAttributeName: {
                  this.stateAfterAttributeName(c);
                  break;
                }
                case State2.InAttributeValueSq: {
                  this.stateInAttributeValueSingleQuotes(c);
                  break;
                }
                case State2.BeforeAttributeValue: {
                  this.stateBeforeAttributeValue(c);
                  break;
                }
                case State2.BeforeClosingTagName: {
                  this.stateBeforeClosingTagName(c);
                  break;
                }
                case State2.AfterClosingTagName: {
                  this.stateAfterClosingTagName(c);
                  break;
                }
                case State2.BeforeSpecialS: {
                  this.stateBeforeSpecialS(c);
                  break;
                }
                case State2.BeforeSpecialT: {
                  this.stateBeforeSpecialT(c);
                  break;
                }
                case State2.InAttributeValueNq: {
                  this.stateInAttributeValueNoQuotes(c);
                  break;
                }
                case State2.InSelfClosingTag: {
                  this.stateInSelfClosingTag(c);
                  break;
                }
                case State2.InDeclaration: {
                  this.stateInDeclaration(c);
                  break;
                }
                case State2.BeforeDeclaration: {
                  this.stateBeforeDeclaration(c);
                  break;
                }
                case State2.BeforeComment: {
                  this.stateBeforeComment(c);
                  break;
                }
                case State2.InProcessingInstruction: {
                  this.stateInProcessingInstruction(c);
                  break;
                }
                case State2.InEntity: {
                  this.stateInEntity();
                  break;
                }
              }
              this.index++;
            }
            this.cleanup();
          };
          Tokenizer3.prototype.finish = function() {
            if (this.state === State2.InEntity) {
              this.entityDecoder.end();
              this.state = this.baseState;
            }
            this.handleTrailingData();
            this.cbs.onend();
          };
          Tokenizer3.prototype.handleTrailingData = function() {
            var endIndex = this.buffer.length + this.offset;
            if (this.sectionStart >= endIndex) {
              return;
            }
            if (this.state === State2.InCommentLike) {
              if (this.currentSequence === Sequences.CdataEnd) {
                this.cbs.oncdata(this.sectionStart, endIndex, 0);
              } else {
                this.cbs.oncomment(this.sectionStart, endIndex, 0);
              }
            } else if (this.state === State2.InTagName || this.state === State2.BeforeAttributeName || this.state === State2.BeforeAttributeValue || this.state === State2.AfterAttributeName || this.state === State2.InAttributeName || this.state === State2.InAttributeValueSq || this.state === State2.InAttributeValueDq || this.state === State2.InAttributeValueNq || this.state === State2.InClosingTagName) {
            } else {
              this.cbs.ontext(this.sectionStart, endIndex);
            }
          };
          Tokenizer3.prototype.emitCodePoint = function(cp, consumed) {
            if (this.baseState !== State2.Text && this.baseState !== State2.InSpecialTag) {
              if (this.sectionStart < this.entityStart) {
                this.cbs.onattribdata(this.sectionStart, this.entityStart);
              }
              this.sectionStart = this.entityStart + consumed;
              this.index = this.sectionStart - 1;
              this.cbs.onattribentity(cp);
            } else {
              if (this.sectionStart < this.entityStart) {
                this.cbs.ontext(this.sectionStart, this.entityStart);
              }
              this.sectionStart = this.entityStart + consumed;
              this.index = this.sectionStart - 1;
              this.cbs.ontextentity(cp, this.sectionStart);
            }
          };
          return Tokenizer3;
        }()
      );
      exports4.default = Tokenizer2;
    }
  });

  // node_modules/htmlparser2/lib/Parser.js
  var require_Parser = __commonJS({
    "node_modules/htmlparser2/lib/Parser.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.Parser = void 0;
      var Tokenizer_js_1 = __importStar(require_Tokenizer());
      var decode_js_1 = require_decode();
      var formTags = /* @__PURE__ */ new Set([
        "input",
        "option",
        "optgroup",
        "select",
        "button",
        "datalist",
        "textarea"
      ]);
      var pTag = /* @__PURE__ */ new Set(["p"]);
      var tableSectionTags = /* @__PURE__ */ new Set(["thead", "tbody"]);
      var ddtTags = /* @__PURE__ */ new Set(["dd", "dt"]);
      var rtpTags = /* @__PURE__ */ new Set(["rt", "rp"]);
      var openImpliesClose = /* @__PURE__ */ new Map([
        ["tr", /* @__PURE__ */ new Set(["tr", "th", "td"])],
        ["th", /* @__PURE__ */ new Set(["th"])],
        ["td", /* @__PURE__ */ new Set(["thead", "th", "td"])],
        ["body", /* @__PURE__ */ new Set(["head", "link", "script"])],
        ["li", /* @__PURE__ */ new Set(["li"])],
        ["p", pTag],
        ["h1", pTag],
        ["h2", pTag],
        ["h3", pTag],
        ["h4", pTag],
        ["h5", pTag],
        ["h6", pTag],
        ["select", formTags],
        ["input", formTags],
        ["output", formTags],
        ["button", formTags],
        ["datalist", formTags],
        ["textarea", formTags],
        ["option", /* @__PURE__ */ new Set(["option"])],
        ["optgroup", /* @__PURE__ */ new Set(["optgroup", "option"])],
        ["dd", ddtTags],
        ["dt", ddtTags],
        ["address", pTag],
        ["article", pTag],
        ["aside", pTag],
        ["blockquote", pTag],
        ["details", pTag],
        ["div", pTag],
        ["dl", pTag],
        ["fieldset", pTag],
        ["figcaption", pTag],
        ["figure", pTag],
        ["footer", pTag],
        ["form", pTag],
        ["header", pTag],
        ["hr", pTag],
        ["main", pTag],
        ["nav", pTag],
        ["ol", pTag],
        ["pre", pTag],
        ["section", pTag],
        ["table", pTag],
        ["ul", pTag],
        ["rt", rtpTags],
        ["rp", rtpTags],
        ["tbody", tableSectionTags],
        ["tfoot", tableSectionTags]
      ]);
      var voidElements = /* @__PURE__ */ new Set([
        "area",
        "base",
        "basefont",
        "br",
        "col",
        "command",
        "embed",
        "frame",
        "hr",
        "img",
        "input",
        "isindex",
        "keygen",
        "link",
        "meta",
        "param",
        "source",
        "track",
        "wbr"
      ]);
      var foreignContextElements = /* @__PURE__ */ new Set(["math", "svg"]);
      var htmlIntegrationElements = /* @__PURE__ */ new Set([
        "mi",
        "mo",
        "mn",
        "ms",
        "mtext",
        "annotation-xml",
        "foreignobject",
        "desc",
        "title"
      ]);
      var reNameEnd = /\s|\//;
      var Parser2 = (
        /** @class */
        function() {
          function Parser3(cbs, options) {
            if (options === void 0) {
              options = {};
            }
            var _a, _b, _c, _d, _e, _f;
            this.options = options;
            this.startIndex = 0;
            this.endIndex = 0;
            this.openTagStart = 0;
            this.tagname = "";
            this.attribname = "";
            this.attribvalue = "";
            this.attribs = null;
            this.stack = [];
            this.buffers = [];
            this.bufferOffset = 0;
            this.writeIndex = 0;
            this.ended = false;
            this.cbs = cbs !== null && cbs !== void 0 ? cbs : {};
            this.htmlMode = !this.options.xmlMode;
            this.lowerCaseTagNames = (_a = options.lowerCaseTags) !== null && _a !== void 0 ? _a : this.htmlMode;
            this.lowerCaseAttributeNames = (_b = options.lowerCaseAttributeNames) !== null && _b !== void 0 ? _b : this.htmlMode;
            this.recognizeSelfClosing = (_c = options.recognizeSelfClosing) !== null && _c !== void 0 ? _c : !this.htmlMode;
            this.tokenizer = new ((_d = options.Tokenizer) !== null && _d !== void 0 ? _d : Tokenizer_js_1.default)(this.options, this);
            this.foreignContext = [!this.htmlMode];
            (_f = (_e = this.cbs).onparserinit) === null || _f === void 0 ? void 0 : _f.call(_e, this);
          }
          __name(Parser3, "Parser");
          Parser3.prototype.ontext = function(start, endIndex) {
            var _a, _b;
            var data2 = this.getSlice(start, endIndex);
            this.endIndex = endIndex - 1;
            (_b = (_a = this.cbs).ontext) === null || _b === void 0 ? void 0 : _b.call(_a, data2);
            this.startIndex = endIndex;
          };
          Parser3.prototype.ontextentity = function(cp, endIndex) {
            var _a, _b;
            this.endIndex = endIndex - 1;
            (_b = (_a = this.cbs).ontext) === null || _b === void 0 ? void 0 : _b.call(_a, (0, decode_js_1.fromCodePoint)(cp));
            this.startIndex = endIndex;
          };
          Parser3.prototype.isVoidElement = function(name) {
            return this.htmlMode && voidElements.has(name);
          };
          Parser3.prototype.onopentagname = function(start, endIndex) {
            this.endIndex = endIndex;
            var name = this.getSlice(start, endIndex);
            if (this.lowerCaseTagNames) {
              name = name.toLowerCase();
            }
            this.emitOpenTag(name);
          };
          Parser3.prototype.emitOpenTag = function(name) {
            var _a, _b, _c, _d;
            this.openTagStart = this.startIndex;
            this.tagname = name;
            var impliesClose = this.htmlMode && openImpliesClose.get(name);
            if (impliesClose) {
              while (this.stack.length > 0 && impliesClose.has(this.stack[0])) {
                var element = this.stack.shift();
                (_b = (_a = this.cbs).onclosetag) === null || _b === void 0 ? void 0 : _b.call(_a, element, true);
              }
            }
            if (!this.isVoidElement(name)) {
              this.stack.unshift(name);
              if (this.htmlMode) {
                if (foreignContextElements.has(name)) {
                  this.foreignContext.unshift(true);
                } else if (htmlIntegrationElements.has(name)) {
                  this.foreignContext.unshift(false);
                }
              }
            }
            (_d = (_c = this.cbs).onopentagname) === null || _d === void 0 ? void 0 : _d.call(_c, name);
            if (this.cbs.onopentag)
              this.attribs = {};
          };
          Parser3.prototype.endOpenTag = function(isImplied) {
            var _a, _b;
            this.startIndex = this.openTagStart;
            if (this.attribs) {
              (_b = (_a = this.cbs).onopentag) === null || _b === void 0 ? void 0 : _b.call(_a, this.tagname, this.attribs, isImplied);
              this.attribs = null;
            }
            if (this.cbs.onclosetag && this.isVoidElement(this.tagname)) {
              this.cbs.onclosetag(this.tagname, true);
            }
            this.tagname = "";
          };
          Parser3.prototype.onopentagend = function(endIndex) {
            this.endIndex = endIndex;
            this.endOpenTag(false);
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.onclosetag = function(start, endIndex) {
            var _a, _b, _c, _d, _e, _f, _g, _h;
            this.endIndex = endIndex;
            var name = this.getSlice(start, endIndex);
            if (this.lowerCaseTagNames) {
              name = name.toLowerCase();
            }
            if (this.htmlMode && (foreignContextElements.has(name) || htmlIntegrationElements.has(name))) {
              this.foreignContext.shift();
            }
            if (!this.isVoidElement(name)) {
              var pos = this.stack.indexOf(name);
              if (pos !== -1) {
                for (var index2 = 0; index2 <= pos; index2++) {
                  var element = this.stack.shift();
                  (_b = (_a = this.cbs).onclosetag) === null || _b === void 0 ? void 0 : _b.call(_a, element, index2 !== pos);
                }
              } else if (this.htmlMode && name === "p") {
                this.emitOpenTag("p");
                this.closeCurrentTag(true);
              }
            } else if (this.htmlMode && name === "br") {
              (_d = (_c = this.cbs).onopentagname) === null || _d === void 0 ? void 0 : _d.call(_c, "br");
              (_f = (_e = this.cbs).onopentag) === null || _f === void 0 ? void 0 : _f.call(_e, "br", {}, true);
              (_h = (_g = this.cbs).onclosetag) === null || _h === void 0 ? void 0 : _h.call(_g, "br", false);
            }
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.onselfclosingtag = function(endIndex) {
            this.endIndex = endIndex;
            if (this.recognizeSelfClosing || this.foreignContext[0]) {
              this.closeCurrentTag(false);
              this.startIndex = endIndex + 1;
            } else {
              this.onopentagend(endIndex);
            }
          };
          Parser3.prototype.closeCurrentTag = function(isOpenImplied) {
            var _a, _b;
            var name = this.tagname;
            this.endOpenTag(isOpenImplied);
            if (this.stack[0] === name) {
              (_b = (_a = this.cbs).onclosetag) === null || _b === void 0 ? void 0 : _b.call(_a, name, !isOpenImplied);
              this.stack.shift();
            }
          };
          Parser3.prototype.onattribname = function(start, endIndex) {
            this.startIndex = start;
            var name = this.getSlice(start, endIndex);
            this.attribname = this.lowerCaseAttributeNames ? name.toLowerCase() : name;
          };
          Parser3.prototype.onattribdata = function(start, endIndex) {
            this.attribvalue += this.getSlice(start, endIndex);
          };
          Parser3.prototype.onattribentity = function(cp) {
            this.attribvalue += (0, decode_js_1.fromCodePoint)(cp);
          };
          Parser3.prototype.onattribend = function(quote, endIndex) {
            var _a, _b;
            this.endIndex = endIndex;
            (_b = (_a = this.cbs).onattribute) === null || _b === void 0 ? void 0 : _b.call(_a, this.attribname, this.attribvalue, quote === Tokenizer_js_1.QuoteType.Double ? '"' : quote === Tokenizer_js_1.QuoteType.Single ? "'" : quote === Tokenizer_js_1.QuoteType.NoValue ? void 0 : null);
            if (this.attribs && !Object.prototype.hasOwnProperty.call(this.attribs, this.attribname)) {
              this.attribs[this.attribname] = this.attribvalue;
            }
            this.attribvalue = "";
          };
          Parser3.prototype.getInstructionName = function(value) {
            var index2 = value.search(reNameEnd);
            var name = index2 < 0 ? value : value.substr(0, index2);
            if (this.lowerCaseTagNames) {
              name = name.toLowerCase();
            }
            return name;
          };
          Parser3.prototype.ondeclaration = function(start, endIndex) {
            this.endIndex = endIndex;
            var value = this.getSlice(start, endIndex);
            if (this.cbs.onprocessinginstruction) {
              var name = this.getInstructionName(value);
              this.cbs.onprocessinginstruction("!".concat(name), "!".concat(value));
            }
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.onprocessinginstruction = function(start, endIndex) {
            this.endIndex = endIndex;
            var value = this.getSlice(start, endIndex);
            if (this.cbs.onprocessinginstruction) {
              var name = this.getInstructionName(value);
              this.cbs.onprocessinginstruction("?".concat(name), "?".concat(value));
            }
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.oncomment = function(start, endIndex, offset) {
            var _a, _b, _c, _d;
            this.endIndex = endIndex;
            (_b = (_a = this.cbs).oncomment) === null || _b === void 0 ? void 0 : _b.call(_a, this.getSlice(start, endIndex - offset));
            (_d = (_c = this.cbs).oncommentend) === null || _d === void 0 ? void 0 : _d.call(_c);
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.oncdata = function(start, endIndex, offset) {
            var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
            this.endIndex = endIndex;
            var value = this.getSlice(start, endIndex - offset);
            if (!this.htmlMode || this.options.recognizeCDATA) {
              (_b = (_a = this.cbs).oncdatastart) === null || _b === void 0 ? void 0 : _b.call(_a);
              (_d = (_c = this.cbs).ontext) === null || _d === void 0 ? void 0 : _d.call(_c, value);
              (_f = (_e = this.cbs).oncdataend) === null || _f === void 0 ? void 0 : _f.call(_e);
            } else {
              (_h = (_g = this.cbs).oncomment) === null || _h === void 0 ? void 0 : _h.call(_g, "[CDATA[".concat(value, "]]"));
              (_k = (_j = this.cbs).oncommentend) === null || _k === void 0 ? void 0 : _k.call(_j);
            }
            this.startIndex = endIndex + 1;
          };
          Parser3.prototype.onend = function() {
            var _a, _b;
            if (this.cbs.onclosetag) {
              this.endIndex = this.startIndex;
              for (var index2 = 0; index2 < this.stack.length; index2++) {
                this.cbs.onclosetag(this.stack[index2], true);
              }
            }
            (_b = (_a = this.cbs).onend) === null || _b === void 0 ? void 0 : _b.call(_a);
          };
          Parser3.prototype.reset = function() {
            var _a, _b, _c, _d;
            (_b = (_a = this.cbs).onreset) === null || _b === void 0 ? void 0 : _b.call(_a);
            this.tokenizer.reset();
            this.tagname = "";
            this.attribname = "";
            this.attribs = null;
            this.stack.length = 0;
            this.startIndex = 0;
            this.endIndex = 0;
            (_d = (_c = this.cbs).onparserinit) === null || _d === void 0 ? void 0 : _d.call(_c, this);
            this.buffers.length = 0;
            this.foreignContext.length = 0;
            this.foreignContext.unshift(!this.htmlMode);
            this.bufferOffset = 0;
            this.writeIndex = 0;
            this.ended = false;
          };
          Parser3.prototype.parseComplete = function(data2) {
            this.reset();
            this.end(data2);
          };
          Parser3.prototype.getSlice = function(start, end2) {
            while (start - this.bufferOffset >= this.buffers[0].length) {
              this.shiftBuffer();
            }
            var slice2 = this.buffers[0].slice(start - this.bufferOffset, end2 - this.bufferOffset);
            while (end2 - this.bufferOffset > this.buffers[0].length) {
              this.shiftBuffer();
              slice2 += this.buffers[0].slice(0, end2 - this.bufferOffset);
            }
            return slice2;
          };
          Parser3.prototype.shiftBuffer = function() {
            this.bufferOffset += this.buffers[0].length;
            this.writeIndex--;
            this.buffers.shift();
          };
          Parser3.prototype.write = function(chunk) {
            var _a, _b;
            if (this.ended) {
              (_b = (_a = this.cbs).onerror) === null || _b === void 0 ? void 0 : _b.call(_a, new Error(".write() after done!"));
              return;
            }
            this.buffers.push(chunk);
            if (this.tokenizer.running) {
              this.tokenizer.write(chunk);
              this.writeIndex++;
            }
          };
          Parser3.prototype.end = function(chunk) {
            var _a, _b;
            if (this.ended) {
              (_b = (_a = this.cbs).onerror) === null || _b === void 0 ? void 0 : _b.call(_a, new Error(".end() after done!"));
              return;
            }
            if (chunk)
              this.write(chunk);
            this.ended = true;
            this.tokenizer.end();
          };
          Parser3.prototype.pause = function() {
            this.tokenizer.pause();
          };
          Parser3.prototype.resume = function() {
            this.tokenizer.resume();
            while (this.tokenizer.running && this.writeIndex < this.buffers.length) {
              this.tokenizer.write(this.buffers[this.writeIndex++]);
            }
            if (this.ended)
              this.tokenizer.end();
          };
          Parser3.prototype.parseChunk = function(chunk) {
            this.write(chunk);
          };
          Parser3.prototype.done = function(chunk) {
            this.end(chunk);
          };
          return Parser3;
        }()
      );
      exports4.Parser = Parser2;
    }
  });

  // node_modules/htmlparser2/lib/index.js
  var require_lib9 = __commonJS({
    "node_modules/htmlparser2/lib/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var __createBinding = exports4 && exports4.__createBinding || (Object.create ? function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        var desc = Object.getOwnPropertyDescriptor(m, k);
        if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
          desc = { enumerable: true, get: /* @__PURE__ */ __name(function() {
            return m[k];
          }, "get") };
        }
        Object.defineProperty(o, k2, desc);
      } : function(o, m, k, k2) {
        if (k2 === void 0) k2 = k;
        o[k2] = m[k];
      });
      var __setModuleDefault = exports4 && exports4.__setModuleDefault || (Object.create ? function(o, v) {
        Object.defineProperty(o, "default", { enumerable: true, value: v });
      } : function(o, v) {
        o["default"] = v;
      });
      var __importStar = exports4 && exports4.__importStar || function(mod2) {
        if (mod2 && mod2.__esModule) return mod2;
        var result = {};
        if (mod2 != null) {
          for (var k in mod2) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod2, k)) __createBinding(result, mod2, k);
        }
        __setModuleDefault(result, mod2);
        return result;
      };
      var __importDefault = exports4 && exports4.__importDefault || function(mod2) {
        return mod2 && mod2.__esModule ? mod2 : { "default": mod2 };
      };
      Object.defineProperty(exports4, "__esModule", { value: true });
      exports4.DomUtils = exports4.parseFeed = exports4.getFeed = exports4.ElementType = exports4.QuoteType = exports4.Tokenizer = exports4.createDomStream = exports4.createDocumentStream = exports4.parseDOM = exports4.parseDocument = exports4.DefaultHandler = exports4.DomHandler = exports4.Parser = void 0;
      var Parser_js_1 = require_Parser();
      var Parser_js_2 = require_Parser();
      Object.defineProperty(exports4, "Parser", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return Parser_js_2.Parser;
      }, "get") });
      var domhandler_1 = require_lib2();
      var domhandler_2 = require_lib2();
      Object.defineProperty(exports4, "DomHandler", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_2.DomHandler;
      }, "get") });
      Object.defineProperty(exports4, "DefaultHandler", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domhandler_2.DomHandler;
      }, "get") });
      function parseDocument(data2, options) {
        var handler = new domhandler_1.DomHandler(void 0, options);
        new Parser_js_1.Parser(handler, options).end(data2);
        return handler.root;
      }
      __name(parseDocument, "parseDocument");
      exports4.parseDocument = parseDocument;
      function parseDOM(data2, options) {
        return parseDocument(data2, options).children;
      }
      __name(parseDOM, "parseDOM");
      exports4.parseDOM = parseDOM;
      function createDocumentStream(callback, options, elementCallback) {
        var handler = new domhandler_1.DomHandler(function(error) {
          return callback(error, handler.root);
        }, options, elementCallback);
        return new Parser_js_1.Parser(handler, options);
      }
      __name(createDocumentStream, "createDocumentStream");
      exports4.createDocumentStream = createDocumentStream;
      function createDomStream(callback, options, elementCallback) {
        var handler = new domhandler_1.DomHandler(callback, options, elementCallback);
        return new Parser_js_1.Parser(handler, options);
      }
      __name(createDomStream, "createDomStream");
      exports4.createDomStream = createDomStream;
      var Tokenizer_js_1 = require_Tokenizer();
      Object.defineProperty(exports4, "Tokenizer", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return __importDefault(Tokenizer_js_1).default;
      }, "get") });
      Object.defineProperty(exports4, "QuoteType", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return Tokenizer_js_1.QuoteType;
      }, "get") });
      exports4.ElementType = __importStar(require_lib());
      var domutils_1 = require_lib5();
      var domutils_2 = require_lib5();
      Object.defineProperty(exports4, "getFeed", { enumerable: true, get: /* @__PURE__ */ __name(function() {
        return domutils_2.getFeed;
      }, "get") });
      var parseFeedDefaultOptions = { xmlMode: true };
      function parseFeed(feed, options) {
        if (options === void 0) {
          options = parseFeedDefaultOptions;
        }
        return (0, domutils_1.getFeed)(parseDOM(feed, options));
      }
      __name(parseFeed, "parseFeed");
      exports4.parseFeed = parseFeed;
      exports4.DomUtils = __importStar(require_lib5());
    }
  });

  // node_modules/cheerio/dist/browser/load-parse.js
  var import_dom_serializer, import_htmlparser2, parse4, load;
  var init_load_parse = __esm({
    "node_modules/cheerio/dist/browser/load-parse.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_load();
      init_parse2();
      init_parse5_adapter();
      import_dom_serializer = __toESM(require_lib4(), 1);
      import_htmlparser2 = __toESM(require_lib9(), 1);
      parse4 = getParse((content, options, isDocument3, context) => options._useHtmlParser2 ? (0, import_htmlparser2.parseDocument)(content, options) : parseWithParse5(content, options, isDocument3, context));
      load = getLoad(parse4, (dom, options) => options._useHtmlParser2 ? (0, import_dom_serializer.default)(dom, options) : renderWithParse5(dom));
    }
  });

  // node_modules/cheerio/dist/browser/index.js
  var browser_exports = {};
  __export(browser_exports, {
    contains: () => contains,
    load: () => load,
    merge: () => merge
  });
  var init_browser = __esm({
    "node_modules/cheerio/dist/browser/index.js"() {
      init_dirname();
      init_buffer2();
      init_process2();
      init_static();
      init_load_parse();
    }
  });

  // node_modules/@protobufjs/aspromise/index.js
  var require_aspromise = __commonJS({
    "node_modules/@protobufjs/aspromise/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = asPromise;
      function asPromise(fn, ctx) {
        var params = new Array(arguments.length - 1), offset = 0, index2 = 2, pending = true;
        while (index2 < arguments.length)
          params[offset++] = arguments[index2++];
        return new Promise(/* @__PURE__ */ __name(function executor(resolve, reject) {
          params[offset] = /* @__PURE__ */ __name(function callback(err) {
            if (pending) {
              pending = false;
              if (err)
                reject(err);
              else {
                var params2 = new Array(arguments.length - 1), offset2 = 0;
                while (offset2 < params2.length)
                  params2[offset2++] = arguments[offset2];
                resolve.apply(null, params2);
              }
            }
          }, "callback");
          try {
            fn.apply(ctx || null, params);
          } catch (err) {
            if (pending) {
              pending = false;
              reject(err);
            }
          }
        }, "executor"));
      }
      __name(asPromise, "asPromise");
    }
  });

  // node_modules/@protobufjs/base64/index.js
  var require_base64 = __commonJS({
    "node_modules/@protobufjs/base64/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var base64 = exports4;
      base64.length = /* @__PURE__ */ __name(function length(string) {
        var p = string.length;
        if (!p)
          return 0;
        var n2 = 0;
        while (--p % 4 > 1 && string.charAt(p) === "=")
          ++n2;
        return Math.ceil(string.length * 3) / 4 - n2;
      }, "length");
      var b64 = new Array(64);
      var s64 = new Array(123);
      for (i = 0; i < 64; )
        s64[b64[i] = i < 26 ? i + 65 : i < 52 ? i + 71 : i < 62 ? i - 4 : i - 59 | 43] = i++;
      base64.encode = /* @__PURE__ */ __name(function encode(buffer, start, end2) {
        var parts = null, chunk = [];
        var i2 = 0, j = 0, t2;
        while (start < end2) {
          var b = buffer[start++];
          switch (j) {
            case 0:
              chunk[i2++] = b64[b >> 2];
              t2 = (b & 3) << 4;
              j = 1;
              break;
            case 1:
              chunk[i2++] = b64[t2 | b >> 4];
              t2 = (b & 15) << 2;
              j = 2;
              break;
            case 2:
              chunk[i2++] = b64[t2 | b >> 6];
              chunk[i2++] = b64[b & 63];
              j = 0;
              break;
          }
          if (i2 > 8191) {
            (parts || (parts = [])).push(String.fromCharCode.apply(String, chunk));
            i2 = 0;
          }
        }
        if (j) {
          chunk[i2++] = b64[t2];
          chunk[i2++] = 61;
          if (j === 1)
            chunk[i2++] = 61;
        }
        if (parts) {
          if (i2)
            parts.push(String.fromCharCode.apply(String, chunk.slice(0, i2)));
          return parts.join("");
        }
        return String.fromCharCode.apply(String, chunk.slice(0, i2));
      }, "encode");
      var invalidEncoding = "invalid encoding";
      base64.decode = /* @__PURE__ */ __name(function decode(string, buffer, offset) {
        var start = offset;
        var j = 0, t2;
        for (var i2 = 0; i2 < string.length; ) {
          var c = string.charCodeAt(i2++);
          if (c === 61 && j > 1)
            break;
          if ((c = s64[c]) === void 0)
            throw Error(invalidEncoding);
          switch (j) {
            case 0:
              t2 = c;
              j = 1;
              break;
            case 1:
              buffer[offset++] = t2 << 2 | (c & 48) >> 4;
              t2 = c;
              j = 2;
              break;
            case 2:
              buffer[offset++] = (t2 & 15) << 4 | (c & 60) >> 2;
              t2 = c;
              j = 3;
              break;
            case 3:
              buffer[offset++] = (t2 & 3) << 6 | c;
              j = 0;
              break;
          }
        }
        if (j === 1)
          throw Error(invalidEncoding);
        return offset - start;
      }, "decode");
      base64.test = /* @__PURE__ */ __name(function test(string) {
        return /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(string);
      }, "test");
      var i;
    }
  });

  // node_modules/@protobufjs/eventemitter/index.js
  var require_eventemitter = __commonJS({
    "node_modules/@protobufjs/eventemitter/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = EventEmitter;
      function EventEmitter() {
        this._listeners = {};
      }
      __name(EventEmitter, "EventEmitter");
      EventEmitter.prototype.on = /* @__PURE__ */ __name(function on2(evt, fn, ctx) {
        (this._listeners[evt] || (this._listeners[evt] = [])).push({
          fn,
          ctx: ctx || this
        });
        return this;
      }, "on");
      EventEmitter.prototype.off = /* @__PURE__ */ __name(function off2(evt, fn) {
        if (evt === void 0)
          this._listeners = {};
        else {
          if (fn === void 0)
            this._listeners[evt] = [];
          else {
            var listeners2 = this._listeners[evt];
            for (var i = 0; i < listeners2.length; )
              if (listeners2[i].fn === fn)
                listeners2.splice(i, 1);
              else
                ++i;
          }
        }
        return this;
      }, "off");
      EventEmitter.prototype.emit = /* @__PURE__ */ __name(function emit2(evt) {
        var listeners2 = this._listeners[evt];
        if (listeners2) {
          var args = [], i = 1;
          for (; i < arguments.length; )
            args.push(arguments[i++]);
          for (i = 0; i < listeners2.length; )
            listeners2[i].fn.apply(listeners2[i++].ctx, args);
        }
        return this;
      }, "emit");
    }
  });

  // node_modules/@protobufjs/float/index.js
  var require_float = __commonJS({
    "node_modules/@protobufjs/float/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = factory(factory);
      function factory(exports5) {
        if (typeof Float32Array !== "undefined") (function() {
          var f32 = new Float32Array([-0]), f8b = new Uint8Array(f32.buffer), le = f8b[3] === 128;
          function writeFloat_f32_cpy(val2, buf, pos) {
            f32[0] = val2;
            buf[pos] = f8b[0];
            buf[pos + 1] = f8b[1];
            buf[pos + 2] = f8b[2];
            buf[pos + 3] = f8b[3];
          }
          __name(writeFloat_f32_cpy, "writeFloat_f32_cpy");
          function writeFloat_f32_rev(val2, buf, pos) {
            f32[0] = val2;
            buf[pos] = f8b[3];
            buf[pos + 1] = f8b[2];
            buf[pos + 2] = f8b[1];
            buf[pos + 3] = f8b[0];
          }
          __name(writeFloat_f32_rev, "writeFloat_f32_rev");
          exports5.writeFloatLE = le ? writeFloat_f32_cpy : writeFloat_f32_rev;
          exports5.writeFloatBE = le ? writeFloat_f32_rev : writeFloat_f32_cpy;
          function readFloat_f32_cpy(buf, pos) {
            f8b[0] = buf[pos];
            f8b[1] = buf[pos + 1];
            f8b[2] = buf[pos + 2];
            f8b[3] = buf[pos + 3];
            return f32[0];
          }
          __name(readFloat_f32_cpy, "readFloat_f32_cpy");
          function readFloat_f32_rev(buf, pos) {
            f8b[3] = buf[pos];
            f8b[2] = buf[pos + 1];
            f8b[1] = buf[pos + 2];
            f8b[0] = buf[pos + 3];
            return f32[0];
          }
          __name(readFloat_f32_rev, "readFloat_f32_rev");
          exports5.readFloatLE = le ? readFloat_f32_cpy : readFloat_f32_rev;
          exports5.readFloatBE = le ? readFloat_f32_rev : readFloat_f32_cpy;
        })();
        else (function() {
          function writeFloat_ieee754(writeUint, val2, buf, pos) {
            var sign = val2 < 0 ? 1 : 0;
            if (sign)
              val2 = -val2;
            if (val2 === 0)
              writeUint(1 / val2 > 0 ? (
                /* positive */
                0
              ) : (
                /* negative 0 */
                2147483648
              ), buf, pos);
            else if (isNaN(val2))
              writeUint(2143289344, buf, pos);
            else if (val2 > 34028234663852886e22)
              writeUint((sign << 31 | 2139095040) >>> 0, buf, pos);
            else if (val2 < 11754943508222875e-54)
              writeUint((sign << 31 | Math.round(val2 / 1401298464324817e-60)) >>> 0, buf, pos);
            else {
              var exponent = Math.floor(Math.log(val2) / Math.LN2), mantissa = Math.round(val2 * Math.pow(2, -exponent) * 8388608) & 8388607;
              writeUint((sign << 31 | exponent + 127 << 23 | mantissa) >>> 0, buf, pos);
            }
          }
          __name(writeFloat_ieee754, "writeFloat_ieee754");
          exports5.writeFloatLE = writeFloat_ieee754.bind(null, writeUintLE);
          exports5.writeFloatBE = writeFloat_ieee754.bind(null, writeUintBE);
          function readFloat_ieee754(readUint, buf, pos) {
            var uint = readUint(buf, pos), sign = (uint >> 31) * 2 + 1, exponent = uint >>> 23 & 255, mantissa = uint & 8388607;
            return exponent === 255 ? mantissa ? NaN : sign * Infinity : exponent === 0 ? sign * 1401298464324817e-60 * mantissa : sign * Math.pow(2, exponent - 150) * (mantissa + 8388608);
          }
          __name(readFloat_ieee754, "readFloat_ieee754");
          exports5.readFloatLE = readFloat_ieee754.bind(null, readUintLE);
          exports5.readFloatBE = readFloat_ieee754.bind(null, readUintBE);
        })();
        if (typeof Float64Array !== "undefined") (function() {
          var f64 = new Float64Array([-0]), f8b = new Uint8Array(f64.buffer), le = f8b[7] === 128;
          function writeDouble_f64_cpy(val2, buf, pos) {
            f64[0] = val2;
            buf[pos] = f8b[0];
            buf[pos + 1] = f8b[1];
            buf[pos + 2] = f8b[2];
            buf[pos + 3] = f8b[3];
            buf[pos + 4] = f8b[4];
            buf[pos + 5] = f8b[5];
            buf[pos + 6] = f8b[6];
            buf[pos + 7] = f8b[7];
          }
          __name(writeDouble_f64_cpy, "writeDouble_f64_cpy");
          function writeDouble_f64_rev(val2, buf, pos) {
            f64[0] = val2;
            buf[pos] = f8b[7];
            buf[pos + 1] = f8b[6];
            buf[pos + 2] = f8b[5];
            buf[pos + 3] = f8b[4];
            buf[pos + 4] = f8b[3];
            buf[pos + 5] = f8b[2];
            buf[pos + 6] = f8b[1];
            buf[pos + 7] = f8b[0];
          }
          __name(writeDouble_f64_rev, "writeDouble_f64_rev");
          exports5.writeDoubleLE = le ? writeDouble_f64_cpy : writeDouble_f64_rev;
          exports5.writeDoubleBE = le ? writeDouble_f64_rev : writeDouble_f64_cpy;
          function readDouble_f64_cpy(buf, pos) {
            f8b[0] = buf[pos];
            f8b[1] = buf[pos + 1];
            f8b[2] = buf[pos + 2];
            f8b[3] = buf[pos + 3];
            f8b[4] = buf[pos + 4];
            f8b[5] = buf[pos + 5];
            f8b[6] = buf[pos + 6];
            f8b[7] = buf[pos + 7];
            return f64[0];
          }
          __name(readDouble_f64_cpy, "readDouble_f64_cpy");
          function readDouble_f64_rev(buf, pos) {
            f8b[7] = buf[pos];
            f8b[6] = buf[pos + 1];
            f8b[5] = buf[pos + 2];
            f8b[4] = buf[pos + 3];
            f8b[3] = buf[pos + 4];
            f8b[2] = buf[pos + 5];
            f8b[1] = buf[pos + 6];
            f8b[0] = buf[pos + 7];
            return f64[0];
          }
          __name(readDouble_f64_rev, "readDouble_f64_rev");
          exports5.readDoubleLE = le ? readDouble_f64_cpy : readDouble_f64_rev;
          exports5.readDoubleBE = le ? readDouble_f64_rev : readDouble_f64_cpy;
        })();
        else (function() {
          function writeDouble_ieee754(writeUint, off0, off1, val2, buf, pos) {
            var sign = val2 < 0 ? 1 : 0;
            if (sign)
              val2 = -val2;
            if (val2 === 0) {
              writeUint(0, buf, pos + off0);
              writeUint(1 / val2 > 0 ? (
                /* positive */
                0
              ) : (
                /* negative 0 */
                2147483648
              ), buf, pos + off1);
            } else if (isNaN(val2)) {
              writeUint(0, buf, pos + off0);
              writeUint(2146959360, buf, pos + off1);
            } else if (val2 > 17976931348623157e292) {
              writeUint(0, buf, pos + off0);
              writeUint((sign << 31 | 2146435072) >>> 0, buf, pos + off1);
            } else {
              var mantissa;
              if (val2 < 22250738585072014e-324) {
                mantissa = val2 / 5e-324;
                writeUint(mantissa >>> 0, buf, pos + off0);
                writeUint((sign << 31 | mantissa / 4294967296) >>> 0, buf, pos + off1);
              } else {
                var exponent = Math.floor(Math.log(val2) / Math.LN2);
                if (exponent === 1024)
                  exponent = 1023;
                mantissa = val2 * Math.pow(2, -exponent);
                writeUint(mantissa * 4503599627370496 >>> 0, buf, pos + off0);
                writeUint((sign << 31 | exponent + 1023 << 20 | mantissa * 1048576 & 1048575) >>> 0, buf, pos + off1);
              }
            }
          }
          __name(writeDouble_ieee754, "writeDouble_ieee754");
          exports5.writeDoubleLE = writeDouble_ieee754.bind(null, writeUintLE, 0, 4);
          exports5.writeDoubleBE = writeDouble_ieee754.bind(null, writeUintBE, 4, 0);
          function readDouble_ieee754(readUint, off0, off1, buf, pos) {
            var lo = readUint(buf, pos + off0), hi = readUint(buf, pos + off1);
            var sign = (hi >> 31) * 2 + 1, exponent = hi >>> 20 & 2047, mantissa = 4294967296 * (hi & 1048575) + lo;
            return exponent === 2047 ? mantissa ? NaN : sign * Infinity : exponent === 0 ? sign * 5e-324 * mantissa : sign * Math.pow(2, exponent - 1075) * (mantissa + 4503599627370496);
          }
          __name(readDouble_ieee754, "readDouble_ieee754");
          exports5.readDoubleLE = readDouble_ieee754.bind(null, readUintLE, 0, 4);
          exports5.readDoubleBE = readDouble_ieee754.bind(null, readUintBE, 4, 0);
        })();
        return exports5;
      }
      __name(factory, "factory");
      function writeUintLE(val2, buf, pos) {
        buf[pos] = val2 & 255;
        buf[pos + 1] = val2 >>> 8 & 255;
        buf[pos + 2] = val2 >>> 16 & 255;
        buf[pos + 3] = val2 >>> 24;
      }
      __name(writeUintLE, "writeUintLE");
      function writeUintBE(val2, buf, pos) {
        buf[pos] = val2 >>> 24;
        buf[pos + 1] = val2 >>> 16 & 255;
        buf[pos + 2] = val2 >>> 8 & 255;
        buf[pos + 3] = val2 & 255;
      }
      __name(writeUintBE, "writeUintBE");
      function readUintLE(buf, pos) {
        return (buf[pos] | buf[pos + 1] << 8 | buf[pos + 2] << 16 | buf[pos + 3] << 24) >>> 0;
      }
      __name(readUintLE, "readUintLE");
      function readUintBE(buf, pos) {
        return (buf[pos] << 24 | buf[pos + 1] << 16 | buf[pos + 2] << 8 | buf[pos + 3]) >>> 0;
      }
      __name(readUintBE, "readUintBE");
    }
  });

  // node_modules/@protobufjs/inquire/index.js
  var require_inquire = __commonJS({
    "node_modules/@protobufjs/inquire/index.js"(exports, module) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module.exports = inquire;
      function inquire(moduleName) {
        try {
          var mod = eval("quire".replace(/^/, "re"))(moduleName);
          if (mod && (mod.length || Object.keys(mod).length))
            return mod;
        } catch (e2) {
        }
        return null;
      }
      __name(inquire, "inquire");
    }
  });

  // node_modules/@protobufjs/utf8/index.js
  var require_utf8 = __commonJS({
    "node_modules/@protobufjs/utf8/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var utf8 = exports4;
      utf8.length = /* @__PURE__ */ __name(function utf8_length(string) {
        var len = 0, c = 0;
        for (var i = 0; i < string.length; ++i) {
          c = string.charCodeAt(i);
          if (c < 128)
            len += 1;
          else if (c < 2048)
            len += 2;
          else if ((c & 64512) === 55296 && (string.charCodeAt(i + 1) & 64512) === 56320) {
            ++i;
            len += 4;
          } else
            len += 3;
        }
        return len;
      }, "utf8_length");
      utf8.read = /* @__PURE__ */ __name(function utf8_read(buffer, start, end2) {
        var len = end2 - start;
        if (len < 1)
          return "";
        var parts = null, chunk = [], i = 0, t2;
        while (start < end2) {
          t2 = buffer[start++];
          if (t2 < 128)
            chunk[i++] = t2;
          else if (t2 > 191 && t2 < 224)
            chunk[i++] = (t2 & 31) << 6 | buffer[start++] & 63;
          else if (t2 > 239 && t2 < 365) {
            t2 = ((t2 & 7) << 18 | (buffer[start++] & 63) << 12 | (buffer[start++] & 63) << 6 | buffer[start++] & 63) - 65536;
            chunk[i++] = 55296 + (t2 >> 10);
            chunk[i++] = 56320 + (t2 & 1023);
          } else
            chunk[i++] = (t2 & 15) << 12 | (buffer[start++] & 63) << 6 | buffer[start++] & 63;
          if (i > 8191) {
            (parts || (parts = [])).push(String.fromCharCode.apply(String, chunk));
            i = 0;
          }
        }
        if (parts) {
          if (i)
            parts.push(String.fromCharCode.apply(String, chunk.slice(0, i)));
          return parts.join("");
        }
        return String.fromCharCode.apply(String, chunk.slice(0, i));
      }, "utf8_read");
      utf8.write = /* @__PURE__ */ __name(function utf8_write(string, buffer, offset) {
        var start = offset, c1, c2;
        for (var i = 0; i < string.length; ++i) {
          c1 = string.charCodeAt(i);
          if (c1 < 128) {
            buffer[offset++] = c1;
          } else if (c1 < 2048) {
            buffer[offset++] = c1 >> 6 | 192;
            buffer[offset++] = c1 & 63 | 128;
          } else if ((c1 & 64512) === 55296 && ((c2 = string.charCodeAt(i + 1)) & 64512) === 56320) {
            c1 = 65536 + ((c1 & 1023) << 10) + (c2 & 1023);
            ++i;
            buffer[offset++] = c1 >> 18 | 240;
            buffer[offset++] = c1 >> 12 & 63 | 128;
            buffer[offset++] = c1 >> 6 & 63 | 128;
            buffer[offset++] = c1 & 63 | 128;
          } else {
            buffer[offset++] = c1 >> 12 | 224;
            buffer[offset++] = c1 >> 6 & 63 | 128;
            buffer[offset++] = c1 & 63 | 128;
          }
        }
        return offset - start;
      }, "utf8_write");
    }
  });

  // node_modules/@protobufjs/pool/index.js
  var require_pool = __commonJS({
    "node_modules/@protobufjs/pool/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = pool;
      function pool(alloc, slice2, size) {
        var SIZE = size || 8192;
        var MAX = SIZE >>> 1;
        var slab = null;
        var offset = SIZE;
        return /* @__PURE__ */ __name(function pool_alloc(size2) {
          if (size2 < 1 || size2 > MAX)
            return alloc(size2);
          if (offset + size2 > SIZE) {
            slab = alloc(SIZE);
            offset = 0;
          }
          var buf = slice2.call(slab, offset, offset += size2);
          if (offset & 7)
            offset = (offset | 7) + 1;
          return buf;
        }, "pool_alloc");
      }
      __name(pool, "pool");
    }
  });

  // node_modules/protobufjs/src/util/longbits.js
  var require_longbits = __commonJS({
    "node_modules/protobufjs/src/util/longbits.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = LongBits;
      var util = require_minimal();
      function LongBits(lo, hi) {
        this.lo = lo >>> 0;
        this.hi = hi >>> 0;
      }
      __name(LongBits, "LongBits");
      var zero = LongBits.zero = new LongBits(0, 0);
      zero.toNumber = function() {
        return 0;
      };
      zero.zzEncode = zero.zzDecode = function() {
        return this;
      };
      zero.length = function() {
        return 1;
      };
      var zeroHash = LongBits.zeroHash = "\0\0\0\0\0\0\0\0";
      LongBits.fromNumber = /* @__PURE__ */ __name(function fromNumber(value) {
        if (value === 0)
          return zero;
        var sign = value < 0;
        if (sign)
          value = -value;
        var lo = value >>> 0, hi = (value - lo) / 4294967296 >>> 0;
        if (sign) {
          hi = ~hi >>> 0;
          lo = ~lo >>> 0;
          if (++lo > 4294967295) {
            lo = 0;
            if (++hi > 4294967295)
              hi = 0;
          }
        }
        return new LongBits(lo, hi);
      }, "fromNumber");
      LongBits.from = /* @__PURE__ */ __name(function from(value) {
        if (typeof value === "number")
          return LongBits.fromNumber(value);
        if (util.isString(value)) {
          if (util.Long)
            value = util.Long.fromString(value);
          else
            return LongBits.fromNumber(parseInt(value, 10));
        }
        return value.low || value.high ? new LongBits(value.low >>> 0, value.high >>> 0) : zero;
      }, "from");
      LongBits.prototype.toNumber = /* @__PURE__ */ __name(function toNumber(unsigned) {
        if (!unsigned && this.hi >>> 31) {
          var lo = ~this.lo + 1 >>> 0, hi = ~this.hi >>> 0;
          if (!lo)
            hi = hi + 1 >>> 0;
          return -(lo + hi * 4294967296);
        }
        return this.lo + this.hi * 4294967296;
      }, "toNumber");
      LongBits.prototype.toLong = /* @__PURE__ */ __name(function toLong(unsigned) {
        return util.Long ? new util.Long(this.lo | 0, this.hi | 0, Boolean(unsigned)) : { low: this.lo | 0, high: this.hi | 0, unsigned: Boolean(unsigned) };
      }, "toLong");
      var charCodeAt = String.prototype.charCodeAt;
      LongBits.fromHash = /* @__PURE__ */ __name(function fromHash(hash) {
        if (hash === zeroHash)
          return zero;
        return new LongBits(
          (charCodeAt.call(hash, 0) | charCodeAt.call(hash, 1) << 8 | charCodeAt.call(hash, 2) << 16 | charCodeAt.call(hash, 3) << 24) >>> 0,
          (charCodeAt.call(hash, 4) | charCodeAt.call(hash, 5) << 8 | charCodeAt.call(hash, 6) << 16 | charCodeAt.call(hash, 7) << 24) >>> 0
        );
      }, "fromHash");
      LongBits.prototype.toHash = /* @__PURE__ */ __name(function toHash() {
        return String.fromCharCode(
          this.lo & 255,
          this.lo >>> 8 & 255,
          this.lo >>> 16 & 255,
          this.lo >>> 24,
          this.hi & 255,
          this.hi >>> 8 & 255,
          this.hi >>> 16 & 255,
          this.hi >>> 24
        );
      }, "toHash");
      LongBits.prototype.zzEncode = /* @__PURE__ */ __name(function zzEncode() {
        var mask = this.hi >> 31;
        this.hi = ((this.hi << 1 | this.lo >>> 31) ^ mask) >>> 0;
        this.lo = (this.lo << 1 ^ mask) >>> 0;
        return this;
      }, "zzEncode");
      LongBits.prototype.zzDecode = /* @__PURE__ */ __name(function zzDecode() {
        var mask = -(this.lo & 1);
        this.lo = ((this.lo >>> 1 | this.hi << 31) ^ mask) >>> 0;
        this.hi = (this.hi >>> 1 ^ mask) >>> 0;
        return this;
      }, "zzDecode");
      LongBits.prototype.length = /* @__PURE__ */ __name(function length() {
        var part0 = this.lo, part1 = (this.lo >>> 28 | this.hi << 4) >>> 0, part2 = this.hi >>> 24;
        return part2 === 0 ? part1 === 0 ? part0 < 16384 ? part0 < 128 ? 1 : 2 : part0 < 2097152 ? 3 : 4 : part1 < 16384 ? part1 < 128 ? 5 : 6 : part1 < 2097152 ? 7 : 8 : part2 < 128 ? 9 : 10;
      }, "length");
    }
  });

  // node_modules/protobufjs/src/util/minimal.js
  var require_minimal = __commonJS({
    "node_modules/protobufjs/src/util/minimal.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var util = exports4;
      util.asPromise = require_aspromise();
      util.base64 = require_base64();
      util.EventEmitter = require_eventemitter();
      util.float = require_float();
      util.inquire = require_inquire();
      util.utf8 = require_utf8();
      util.pool = require_pool();
      util.LongBits = require_longbits();
      util.isNode = Boolean(typeof global !== "undefined" && global && global.process && global.process.versions && global.process.versions.node);
      util.global = util.isNode && global || typeof window !== "undefined" && window || typeof self !== "undefined" && self || exports4;
      util.emptyArray = Object.freeze ? Object.freeze([]) : (
        /* istanbul ignore next */
        []
      );
      util.emptyObject = Object.freeze ? Object.freeze({}) : (
        /* istanbul ignore next */
        {}
      );
      util.isInteger = Number.isInteger || /* istanbul ignore next */
      /* @__PURE__ */ __name(function isInteger(value) {
        return typeof value === "number" && isFinite(value) && Math.floor(value) === value;
      }, "isInteger");
      util.isString = /* @__PURE__ */ __name(function isString(value) {
        return typeof value === "string" || value instanceof String;
      }, "isString");
      util.isObject = /* @__PURE__ */ __name(function isObject(value) {
        return value && typeof value === "object";
      }, "isObject");
      util.isset = /**
       * Checks if a property on a message is considered to be present.
       * @param {Object} obj Plain object or message instance
       * @param {string} prop Property name
       * @returns {boolean} `true` if considered to be present, otherwise `false`
       */
      util.isSet = /* @__PURE__ */ __name(function isSet(obj, prop2) {
        var value = obj[prop2];
        if (value != null && obj.hasOwnProperty(prop2))
          return typeof value !== "object" || (Array.isArray(value) ? value.length : Object.keys(value).length) > 0;
        return false;
      }, "isSet");
      util.Buffer = function() {
        try {
          var Buffer2 = util.inquire("buffer").Buffer;
          return Buffer2.prototype.utf8Write ? Buffer2 : (
            /* istanbul ignore next */
            null
          );
        } catch (e2) {
          return null;
        }
      }();
      util._Buffer_from = null;
      util._Buffer_allocUnsafe = null;
      util.newBuffer = /* @__PURE__ */ __name(function newBuffer(sizeOrArray) {
        return typeof sizeOrArray === "number" ? util.Buffer ? util._Buffer_allocUnsafe(sizeOrArray) : new util.Array(sizeOrArray) : util.Buffer ? util._Buffer_from(sizeOrArray) : typeof Uint8Array === "undefined" ? sizeOrArray : new Uint8Array(sizeOrArray);
      }, "newBuffer");
      util.Array = typeof Uint8Array !== "undefined" ? Uint8Array : Array;
      util.Long = /* istanbul ignore next */
      util.global.dcodeIO && /* istanbul ignore next */
      util.global.dcodeIO.Long || /* istanbul ignore next */
      util.global.Long || util.inquire("long");
      util.key2Re = /^true|false|0|1$/;
      util.key32Re = /^-?(?:0|[1-9][0-9]*)$/;
      util.key64Re = /^(?:[\\x00-\\xff]{8}|-?(?:0|[1-9][0-9]*))$/;
      util.longToHash = /* @__PURE__ */ __name(function longToHash(value) {
        return value ? util.LongBits.from(value).toHash() : util.LongBits.zeroHash;
      }, "longToHash");
      util.longFromHash = /* @__PURE__ */ __name(function longFromHash(hash, unsigned) {
        var bits = util.LongBits.fromHash(hash);
        if (util.Long)
          return util.Long.fromBits(bits.lo, bits.hi, unsigned);
        return bits.toNumber(Boolean(unsigned));
      }, "longFromHash");
      function merge2(dst, src, ifNotSet) {
        for (var keys = Object.keys(src), i = 0; i < keys.length; ++i)
          if (dst[keys[i]] === void 0 || !ifNotSet)
            dst[keys[i]] = src[keys[i]];
        return dst;
      }
      __name(merge2, "merge");
      util.merge = merge2;
      util.lcFirst = /* @__PURE__ */ __name(function lcFirst(str) {
        return str.charAt(0).toLowerCase() + str.substring(1);
      }, "lcFirst");
      function newError(name) {
        function CustomError(message, properties) {
          if (!(this instanceof CustomError))
            return new CustomError(message, properties);
          Object.defineProperty(this, "message", { get: /* @__PURE__ */ __name(function() {
            return message;
          }, "get") });
          if (Error.captureStackTrace)
            Error.captureStackTrace(this, CustomError);
          else
            Object.defineProperty(this, "stack", { value: new Error().stack || "" });
          if (properties)
            merge2(this, properties);
        }
        __name(CustomError, "CustomError");
        CustomError.prototype = Object.create(Error.prototype, {
          constructor: {
            value: CustomError,
            writable: true,
            enumerable: false,
            configurable: true
          },
          name: {
            get: /* @__PURE__ */ __name(function get2() {
              return name;
            }, "get"),
            set: void 0,
            enumerable: false,
            // configurable: false would accurately preserve the behavior of
            // the original, but I'm guessing that was not intentional.
            // For an actual error subclass, this property would
            // be configurable.
            configurable: true
          },
          toString: {
            value: /* @__PURE__ */ __name(function value() {
              return this.name + ": " + this.message;
            }, "value"),
            writable: true,
            enumerable: false,
            configurable: true
          }
        });
        return CustomError;
      }
      __name(newError, "newError");
      util.newError = newError;
      util.ProtocolError = newError("ProtocolError");
      util.oneOfGetter = /* @__PURE__ */ __name(function getOneOf(fieldNames) {
        var fieldMap = {};
        for (var i = 0; i < fieldNames.length; ++i)
          fieldMap[fieldNames[i]] = 1;
        return function() {
          for (var keys = Object.keys(this), i2 = keys.length - 1; i2 > -1; --i2)
            if (fieldMap[keys[i2]] === 1 && this[keys[i2]] !== void 0 && this[keys[i2]] !== null)
              return keys[i2];
        };
      }, "getOneOf");
      util.oneOfSetter = /* @__PURE__ */ __name(function setOneOf(fieldNames) {
        return function(name) {
          for (var i = 0; i < fieldNames.length; ++i)
            if (fieldNames[i] !== name)
              delete this[fieldNames[i]];
        };
      }, "setOneOf");
      util.toJSONOptions = {
        longs: String,
        enums: String,
        bytes: String,
        json: true
      };
      util._configure = function() {
        var Buffer2 = util.Buffer;
        if (!Buffer2) {
          util._Buffer_from = util._Buffer_allocUnsafe = null;
          return;
        }
        util._Buffer_from = Buffer2.from !== Uint8Array.from && Buffer2.from || /* istanbul ignore next */
        /* @__PURE__ */ __name(function Buffer_from(value, encoding) {
          return new Buffer2(value, encoding);
        }, "Buffer_from");
        util._Buffer_allocUnsafe = Buffer2.allocUnsafe || /* istanbul ignore next */
        /* @__PURE__ */ __name(function Buffer_allocUnsafe(size) {
          return new Buffer2(size);
        }, "Buffer_allocUnsafe");
      };
    }
  });

  // node_modules/protobufjs/src/writer.js
  var require_writer = __commonJS({
    "node_modules/protobufjs/src/writer.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Writer;
      var util = require_minimal();
      var BufferWriter;
      var LongBits = util.LongBits, base64 = util.base64, utf8 = util.utf8;
      function Op(fn, len, val2) {
        this.fn = fn;
        this.len = len;
        this.next = void 0;
        this.val = val2;
      }
      __name(Op, "Op");
      function noop2() {
      }
      __name(noop2, "noop");
      function State2(writer) {
        this.head = writer.head;
        this.tail = writer.tail;
        this.len = writer.len;
        this.next = writer.states;
      }
      __name(State2, "State");
      function Writer() {
        this.len = 0;
        this.head = new Op(noop2, 0, 0);
        this.tail = this.head;
        this.states = null;
      }
      __name(Writer, "Writer");
      var create = /* @__PURE__ */ __name(function create2() {
        return util.Buffer ? /* @__PURE__ */ __name(function create_buffer_setup() {
          return (Writer.create = /* @__PURE__ */ __name(function create_buffer() {
            return new BufferWriter();
          }, "create_buffer"))();
        }, "create_buffer_setup") : /* @__PURE__ */ __name(function create_array() {
          return new Writer();
        }, "create_array");
      }, "create");
      Writer.create = create();
      Writer.alloc = /* @__PURE__ */ __name(function alloc(size) {
        return new util.Array(size);
      }, "alloc");
      if (util.Array !== Array)
        Writer.alloc = util.pool(Writer.alloc, util.Array.prototype.subarray);
      Writer.prototype._push = /* @__PURE__ */ __name(function push(fn, len, val2) {
        this.tail = this.tail.next = new Op(fn, len, val2);
        this.len += len;
        return this;
      }, "push");
      function writeByte(val2, buf, pos) {
        buf[pos] = val2 & 255;
      }
      __name(writeByte, "writeByte");
      function writeVarint32(val2, buf, pos) {
        while (val2 > 127) {
          buf[pos++] = val2 & 127 | 128;
          val2 >>>= 7;
        }
        buf[pos] = val2;
      }
      __name(writeVarint32, "writeVarint32");
      function VarintOp(len, val2) {
        this.len = len;
        this.next = void 0;
        this.val = val2;
      }
      __name(VarintOp, "VarintOp");
      VarintOp.prototype = Object.create(Op.prototype);
      VarintOp.prototype.fn = writeVarint32;
      Writer.prototype.uint32 = /* @__PURE__ */ __name(function write_uint32(value) {
        this.len += (this.tail = this.tail.next = new VarintOp(
          (value = value >>> 0) < 128 ? 1 : value < 16384 ? 2 : value < 2097152 ? 3 : value < 268435456 ? 4 : 5,
          value
        )).len;
        return this;
      }, "write_uint32");
      Writer.prototype.int32 = /* @__PURE__ */ __name(function write_int32(value) {
        return value < 0 ? this._push(writeVarint64, 10, LongBits.fromNumber(value)) : this.uint32(value);
      }, "write_int32");
      Writer.prototype.sint32 = /* @__PURE__ */ __name(function write_sint32(value) {
        return this.uint32((value << 1 ^ value >> 31) >>> 0);
      }, "write_sint32");
      function writeVarint64(val2, buf, pos) {
        while (val2.hi) {
          buf[pos++] = val2.lo & 127 | 128;
          val2.lo = (val2.lo >>> 7 | val2.hi << 25) >>> 0;
          val2.hi >>>= 7;
        }
        while (val2.lo > 127) {
          buf[pos++] = val2.lo & 127 | 128;
          val2.lo = val2.lo >>> 7;
        }
        buf[pos++] = val2.lo;
      }
      __name(writeVarint64, "writeVarint64");
      Writer.prototype.uint64 = /* @__PURE__ */ __name(function write_uint64(value) {
        var bits = LongBits.from(value);
        return this._push(writeVarint64, bits.length(), bits);
      }, "write_uint64");
      Writer.prototype.int64 = Writer.prototype.uint64;
      Writer.prototype.sint64 = /* @__PURE__ */ __name(function write_sint64(value) {
        var bits = LongBits.from(value).zzEncode();
        return this._push(writeVarint64, bits.length(), bits);
      }, "write_sint64");
      Writer.prototype.bool = /* @__PURE__ */ __name(function write_bool(value) {
        return this._push(writeByte, 1, value ? 1 : 0);
      }, "write_bool");
      function writeFixed32(val2, buf, pos) {
        buf[pos] = val2 & 255;
        buf[pos + 1] = val2 >>> 8 & 255;
        buf[pos + 2] = val2 >>> 16 & 255;
        buf[pos + 3] = val2 >>> 24;
      }
      __name(writeFixed32, "writeFixed32");
      Writer.prototype.fixed32 = /* @__PURE__ */ __name(function write_fixed32(value) {
        return this._push(writeFixed32, 4, value >>> 0);
      }, "write_fixed32");
      Writer.prototype.sfixed32 = Writer.prototype.fixed32;
      Writer.prototype.fixed64 = /* @__PURE__ */ __name(function write_fixed64(value) {
        var bits = LongBits.from(value);
        return this._push(writeFixed32, 4, bits.lo)._push(writeFixed32, 4, bits.hi);
      }, "write_fixed64");
      Writer.prototype.sfixed64 = Writer.prototype.fixed64;
      Writer.prototype.float = /* @__PURE__ */ __name(function write_float(value) {
        return this._push(util.float.writeFloatLE, 4, value);
      }, "write_float");
      Writer.prototype.double = /* @__PURE__ */ __name(function write_double(value) {
        return this._push(util.float.writeDoubleLE, 8, value);
      }, "write_double");
      var writeBytes = util.Array.prototype.set ? /* @__PURE__ */ __name(function writeBytes_set(val2, buf, pos) {
        buf.set(val2, pos);
      }, "writeBytes_set") : /* @__PURE__ */ __name(function writeBytes_for(val2, buf, pos) {
        for (var i = 0; i < val2.length; ++i)
          buf[pos + i] = val2[i];
      }, "writeBytes_for");
      Writer.prototype.bytes = /* @__PURE__ */ __name(function write_bytes(value) {
        var len = value.length >>> 0;
        if (!len)
          return this._push(writeByte, 1, 0);
        if (util.isString(value)) {
          var buf = Writer.alloc(len = base64.length(value));
          base64.decode(value, buf, 0);
          value = buf;
        }
        return this.uint32(len)._push(writeBytes, len, value);
      }, "write_bytes");
      Writer.prototype.string = /* @__PURE__ */ __name(function write_string(value) {
        var len = utf8.length(value);
        return len ? this.uint32(len)._push(utf8.write, len, value) : this._push(writeByte, 1, 0);
      }, "write_string");
      Writer.prototype.fork = /* @__PURE__ */ __name(function fork() {
        this.states = new State2(this);
        this.head = this.tail = new Op(noop2, 0, 0);
        this.len = 0;
        return this;
      }, "fork");
      Writer.prototype.reset = /* @__PURE__ */ __name(function reset() {
        if (this.states) {
          this.head = this.states.head;
          this.tail = this.states.tail;
          this.len = this.states.len;
          this.states = this.states.next;
        } else {
          this.head = this.tail = new Op(noop2, 0, 0);
          this.len = 0;
        }
        return this;
      }, "reset");
      Writer.prototype.ldelim = /* @__PURE__ */ __name(function ldelim() {
        var head = this.head, tail = this.tail, len = this.len;
        this.reset().uint32(len);
        if (len) {
          this.tail.next = head.next;
          this.tail = tail;
          this.len += len;
        }
        return this;
      }, "ldelim");
      Writer.prototype.finish = /* @__PURE__ */ __name(function finish() {
        var head = this.head.next, buf = this.constructor.alloc(this.len), pos = 0;
        while (head) {
          head.fn(head.val, buf, pos);
          pos += head.len;
          head = head.next;
        }
        return buf;
      }, "finish");
      Writer._configure = function(BufferWriter_) {
        BufferWriter = BufferWriter_;
        Writer.create = create();
        BufferWriter._configure();
      };
    }
  });

  // node_modules/protobufjs/src/writer_buffer.js
  var require_writer_buffer = __commonJS({
    "node_modules/protobufjs/src/writer_buffer.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = BufferWriter;
      var Writer = require_writer();
      (BufferWriter.prototype = Object.create(Writer.prototype)).constructor = BufferWriter;
      var util = require_minimal();
      function BufferWriter() {
        Writer.call(this);
      }
      __name(BufferWriter, "BufferWriter");
      BufferWriter._configure = function() {
        BufferWriter.alloc = util._Buffer_allocUnsafe;
        BufferWriter.writeBytesBuffer = util.Buffer && util.Buffer.prototype instanceof Uint8Array && util.Buffer.prototype.set.name === "set" ? /* @__PURE__ */ __name(function writeBytesBuffer_set(val2, buf, pos) {
          buf.set(val2, pos);
        }, "writeBytesBuffer_set") : /* @__PURE__ */ __name(function writeBytesBuffer_copy(val2, buf, pos) {
          if (val2.copy)
            val2.copy(buf, pos, 0, val2.length);
          else for (var i = 0; i < val2.length; )
            buf[pos++] = val2[i++];
        }, "writeBytesBuffer_copy");
      };
      BufferWriter.prototype.bytes = /* @__PURE__ */ __name(function write_bytes_buffer(value) {
        if (util.isString(value))
          value = util._Buffer_from(value, "base64");
        var len = value.length >>> 0;
        this.uint32(len);
        if (len)
          this._push(BufferWriter.writeBytesBuffer, len, value);
        return this;
      }, "write_bytes_buffer");
      function writeStringBuffer(val2, buf, pos) {
        if (val2.length < 40)
          util.utf8.write(val2, buf, pos);
        else if (buf.utf8Write)
          buf.utf8Write(val2, pos);
        else
          buf.write(val2, pos);
      }
      __name(writeStringBuffer, "writeStringBuffer");
      BufferWriter.prototype.string = /* @__PURE__ */ __name(function write_string_buffer(value) {
        var len = util.Buffer.byteLength(value);
        this.uint32(len);
        if (len)
          this._push(writeStringBuffer, len, value);
        return this;
      }, "write_string_buffer");
      BufferWriter._configure();
    }
  });

  // node_modules/protobufjs/src/reader.js
  var require_reader = __commonJS({
    "node_modules/protobufjs/src/reader.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Reader;
      var util = require_minimal();
      var BufferReader;
      var LongBits = util.LongBits, utf8 = util.utf8;
      function indexOutOfRange(reader, writeLength) {
        return RangeError("index out of range: " + reader.pos + " + " + (writeLength || 1) + " > " + reader.len);
      }
      __name(indexOutOfRange, "indexOutOfRange");
      function Reader(buffer) {
        this.buf = buffer;
        this.pos = 0;
        this.len = buffer.length;
      }
      __name(Reader, "Reader");
      var create_array = typeof Uint8Array !== "undefined" ? /* @__PURE__ */ __name(function create_typed_array(buffer) {
        if (buffer instanceof Uint8Array || Array.isArray(buffer))
          return new Reader(buffer);
        throw Error("illegal buffer");
      }, "create_typed_array") : /* @__PURE__ */ __name(function create_array2(buffer) {
        if (Array.isArray(buffer))
          return new Reader(buffer);
        throw Error("illegal buffer");
      }, "create_array");
      var create = /* @__PURE__ */ __name(function create2() {
        return util.Buffer ? /* @__PURE__ */ __name(function create_buffer_setup(buffer) {
          return (Reader.create = /* @__PURE__ */ __name(function create_buffer(buffer2) {
            return util.Buffer.isBuffer(buffer2) ? new BufferReader(buffer2) : create_array(buffer2);
          }, "create_buffer"))(buffer);
        }, "create_buffer_setup") : create_array;
      }, "create");
      Reader.create = create();
      Reader.prototype._slice = util.Array.prototype.subarray || /* istanbul ignore next */
      util.Array.prototype.slice;
      Reader.prototype.uint32 = (/* @__PURE__ */ __name(function read_uint32_setup() {
        var value = 4294967295;
        return /* @__PURE__ */ __name(function read_uint32() {
          value = (this.buf[this.pos] & 127) >>> 0;
          if (this.buf[this.pos++] < 128) return value;
          value = (value | (this.buf[this.pos] & 127) << 7) >>> 0;
          if (this.buf[this.pos++] < 128) return value;
          value = (value | (this.buf[this.pos] & 127) << 14) >>> 0;
          if (this.buf[this.pos++] < 128) return value;
          value = (value | (this.buf[this.pos] & 127) << 21) >>> 0;
          if (this.buf[this.pos++] < 128) return value;
          value = (value | (this.buf[this.pos] & 15) << 28) >>> 0;
          if (this.buf[this.pos++] < 128) return value;
          if ((this.pos += 5) > this.len) {
            this.pos = this.len;
            throw indexOutOfRange(this, 10);
          }
          return value;
        }, "read_uint32");
      }, "read_uint32_setup"))();
      Reader.prototype.int32 = /* @__PURE__ */ __name(function read_int32() {
        return this.uint32() | 0;
      }, "read_int32");
      Reader.prototype.sint32 = /* @__PURE__ */ __name(function read_sint32() {
        var value = this.uint32();
        return value >>> 1 ^ -(value & 1) | 0;
      }, "read_sint32");
      function readLongVarint() {
        var bits = new LongBits(0, 0);
        var i = 0;
        if (this.len - this.pos > 4) {
          for (; i < 4; ++i) {
            bits.lo = (bits.lo | (this.buf[this.pos] & 127) << i * 7) >>> 0;
            if (this.buf[this.pos++] < 128)
              return bits;
          }
          bits.lo = (bits.lo | (this.buf[this.pos] & 127) << 28) >>> 0;
          bits.hi = (bits.hi | (this.buf[this.pos] & 127) >> 4) >>> 0;
          if (this.buf[this.pos++] < 128)
            return bits;
          i = 0;
        } else {
          for (; i < 3; ++i) {
            if (this.pos >= this.len)
              throw indexOutOfRange(this);
            bits.lo = (bits.lo | (this.buf[this.pos] & 127) << i * 7) >>> 0;
            if (this.buf[this.pos++] < 128)
              return bits;
          }
          bits.lo = (bits.lo | (this.buf[this.pos++] & 127) << i * 7) >>> 0;
          return bits;
        }
        if (this.len - this.pos > 4) {
          for (; i < 5; ++i) {
            bits.hi = (bits.hi | (this.buf[this.pos] & 127) << i * 7 + 3) >>> 0;
            if (this.buf[this.pos++] < 128)
              return bits;
          }
        } else {
          for (; i < 5; ++i) {
            if (this.pos >= this.len)
              throw indexOutOfRange(this);
            bits.hi = (bits.hi | (this.buf[this.pos] & 127) << i * 7 + 3) >>> 0;
            if (this.buf[this.pos++] < 128)
              return bits;
          }
        }
        throw Error("invalid varint encoding");
      }
      __name(readLongVarint, "readLongVarint");
      Reader.prototype.bool = /* @__PURE__ */ __name(function read_bool() {
        return this.uint32() !== 0;
      }, "read_bool");
      function readFixed32_end(buf, end2) {
        return (buf[end2 - 4] | buf[end2 - 3] << 8 | buf[end2 - 2] << 16 | buf[end2 - 1] << 24) >>> 0;
      }
      __name(readFixed32_end, "readFixed32_end");
      Reader.prototype.fixed32 = /* @__PURE__ */ __name(function read_fixed32() {
        if (this.pos + 4 > this.len)
          throw indexOutOfRange(this, 4);
        return readFixed32_end(this.buf, this.pos += 4);
      }, "read_fixed32");
      Reader.prototype.sfixed32 = /* @__PURE__ */ __name(function read_sfixed32() {
        if (this.pos + 4 > this.len)
          throw indexOutOfRange(this, 4);
        return readFixed32_end(this.buf, this.pos += 4) | 0;
      }, "read_sfixed32");
      function readFixed64() {
        if (this.pos + 8 > this.len)
          throw indexOutOfRange(this, 8);
        return new LongBits(readFixed32_end(this.buf, this.pos += 4), readFixed32_end(this.buf, this.pos += 4));
      }
      __name(readFixed64, "readFixed64");
      Reader.prototype.float = /* @__PURE__ */ __name(function read_float() {
        if (this.pos + 4 > this.len)
          throw indexOutOfRange(this, 4);
        var value = util.float.readFloatLE(this.buf, this.pos);
        this.pos += 4;
        return value;
      }, "read_float");
      Reader.prototype.double = /* @__PURE__ */ __name(function read_double() {
        if (this.pos + 8 > this.len)
          throw indexOutOfRange(this, 4);
        var value = util.float.readDoubleLE(this.buf, this.pos);
        this.pos += 8;
        return value;
      }, "read_double");
      Reader.prototype.bytes = /* @__PURE__ */ __name(function read_bytes() {
        var length = this.uint32(), start = this.pos, end2 = this.pos + length;
        if (end2 > this.len)
          throw indexOutOfRange(this, length);
        this.pos += length;
        if (Array.isArray(this.buf))
          return this.buf.slice(start, end2);
        if (start === end2) {
          var nativeBuffer = util.Buffer;
          return nativeBuffer ? nativeBuffer.alloc(0) : new this.buf.constructor(0);
        }
        return this._slice.call(this.buf, start, end2);
      }, "read_bytes");
      Reader.prototype.string = /* @__PURE__ */ __name(function read_string() {
        var bytes = this.bytes();
        return utf8.read(bytes, 0, bytes.length);
      }, "read_string");
      Reader.prototype.skip = /* @__PURE__ */ __name(function skip(length) {
        if (typeof length === "number") {
          if (this.pos + length > this.len)
            throw indexOutOfRange(this, length);
          this.pos += length;
        } else {
          do {
            if (this.pos >= this.len)
              throw indexOutOfRange(this);
          } while (this.buf[this.pos++] & 128);
        }
        return this;
      }, "skip");
      Reader.prototype.skipType = function(wireType) {
        switch (wireType) {
          case 0:
            this.skip();
            break;
          case 1:
            this.skip(8);
            break;
          case 2:
            this.skip(this.uint32());
            break;
          case 3:
            while ((wireType = this.uint32() & 7) !== 4) {
              this.skipType(wireType);
            }
            break;
          case 5:
            this.skip(4);
            break;
          /* istanbul ignore next */
          default:
            throw Error("invalid wire type " + wireType + " at offset " + this.pos);
        }
        return this;
      };
      Reader._configure = function(BufferReader_) {
        BufferReader = BufferReader_;
        Reader.create = create();
        BufferReader._configure();
        var fn = util.Long ? "toLong" : (
          /* istanbul ignore next */
          "toNumber"
        );
        util.merge(Reader.prototype, {
          int64: /* @__PURE__ */ __name(function read_int64() {
            return readLongVarint.call(this)[fn](false);
          }, "read_int64"),
          uint64: /* @__PURE__ */ __name(function read_uint64() {
            return readLongVarint.call(this)[fn](true);
          }, "read_uint64"),
          sint64: /* @__PURE__ */ __name(function read_sint64() {
            return readLongVarint.call(this).zzDecode()[fn](false);
          }, "read_sint64"),
          fixed64: /* @__PURE__ */ __name(function read_fixed64() {
            return readFixed64.call(this)[fn](true);
          }, "read_fixed64"),
          sfixed64: /* @__PURE__ */ __name(function read_sfixed64() {
            return readFixed64.call(this)[fn](false);
          }, "read_sfixed64")
        });
      };
    }
  });

  // node_modules/protobufjs/src/reader_buffer.js
  var require_reader_buffer = __commonJS({
    "node_modules/protobufjs/src/reader_buffer.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = BufferReader;
      var Reader = require_reader();
      (BufferReader.prototype = Object.create(Reader.prototype)).constructor = BufferReader;
      var util = require_minimal();
      function BufferReader(buffer) {
        Reader.call(this, buffer);
      }
      __name(BufferReader, "BufferReader");
      BufferReader._configure = function() {
        if (util.Buffer)
          BufferReader.prototype._slice = util.Buffer.prototype.slice;
      };
      BufferReader.prototype.string = /* @__PURE__ */ __name(function read_string_buffer() {
        var len = this.uint32();
        return this.buf.utf8Slice ? this.buf.utf8Slice(this.pos, this.pos = Math.min(this.pos + len, this.len)) : this.buf.toString("utf-8", this.pos, this.pos = Math.min(this.pos + len, this.len));
      }, "read_string_buffer");
      BufferReader._configure();
    }
  });

  // node_modules/protobufjs/src/rpc/service.js
  var require_service = __commonJS({
    "node_modules/protobufjs/src/rpc/service.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Service;
      var util = require_minimal();
      (Service.prototype = Object.create(util.EventEmitter.prototype)).constructor = Service;
      function Service(rpcImpl, requestDelimited, responseDelimited) {
        if (typeof rpcImpl !== "function")
          throw TypeError("rpcImpl must be a function");
        util.EventEmitter.call(this);
        this.rpcImpl = rpcImpl;
        this.requestDelimited = Boolean(requestDelimited);
        this.responseDelimited = Boolean(responseDelimited);
      }
      __name(Service, "Service");
      Service.prototype.rpcCall = /* @__PURE__ */ __name(function rpcCall(method, requestCtor, responseCtor, request, callback) {
        if (!request)
          throw TypeError("request must be specified");
        var self2 = this;
        if (!callback)
          return util.asPromise(rpcCall, self2, method, requestCtor, responseCtor, request);
        if (!self2.rpcImpl) {
          setTimeout(function() {
            callback(Error("already ended"));
          }, 0);
          return void 0;
        }
        try {
          return self2.rpcImpl(
            method,
            requestCtor[self2.requestDelimited ? "encodeDelimited" : "encode"](request).finish(),
            /* @__PURE__ */ __name(function rpcCallback(err, response) {
              if (err) {
                self2.emit("error", err, method);
                return callback(err);
              }
              if (response === null) {
                self2.end(
                  /* endedByRPC */
                  true
                );
                return void 0;
              }
              if (!(response instanceof responseCtor)) {
                try {
                  response = responseCtor[self2.responseDelimited ? "decodeDelimited" : "decode"](response);
                } catch (err2) {
                  self2.emit("error", err2, method);
                  return callback(err2);
                }
              }
              self2.emit("data", response, method);
              return callback(null, response);
            }, "rpcCallback")
          );
        } catch (err) {
          self2.emit("error", err, method);
          setTimeout(function() {
            callback(err);
          }, 0);
          return void 0;
        }
      }, "rpcCall");
      Service.prototype.end = /* @__PURE__ */ __name(function end2(endedByRPC) {
        if (this.rpcImpl) {
          if (!endedByRPC)
            this.rpcImpl(null, null, null);
          this.rpcImpl = null;
          this.emit("end").off();
        }
        return this;
      }, "end");
    }
  });

  // node_modules/protobufjs/src/rpc.js
  var require_rpc = __commonJS({
    "node_modules/protobufjs/src/rpc.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var rpc = exports4;
      rpc.Service = require_service();
    }
  });

  // node_modules/protobufjs/src/roots.js
  var require_roots = __commonJS({
    "node_modules/protobufjs/src/roots.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = {};
    }
  });

  // node_modules/protobufjs/src/index-minimal.js
  var require_index_minimal = __commonJS({
    "node_modules/protobufjs/src/index-minimal.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var protobuf = exports4;
      protobuf.build = "minimal";
      protobuf.Writer = require_writer();
      protobuf.BufferWriter = require_writer_buffer();
      protobuf.Reader = require_reader();
      protobuf.BufferReader = require_reader_buffer();
      protobuf.util = require_minimal();
      protobuf.rpc = require_rpc();
      protobuf.roots = require_roots();
      protobuf.configure = configure;
      function configure() {
        protobuf.util._configure();
        protobuf.Writer._configure(protobuf.BufferWriter);
        protobuf.Reader._configure(protobuf.BufferReader);
      }
      __name(configure, "configure");
      configure();
    }
  });

  // node_modules/@protobufjs/codegen/index.js
  var require_codegen = __commonJS({
    "node_modules/@protobufjs/codegen/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = codegen;
      function codegen(functionParams, functionName) {
        if (typeof functionParams === "string") {
          functionName = functionParams;
          functionParams = void 0;
        }
        var body = [];
        function Codegen(formatStringOrScope) {
          if (typeof formatStringOrScope !== "string") {
            var source = toString2();
            if (codegen.verbose)
              console.log("codegen: " + source);
            source = "return " + source;
            if (formatStringOrScope) {
              var scopeKeys = Object.keys(formatStringOrScope), scopeParams = new Array(scopeKeys.length + 1), scopeValues = new Array(scopeKeys.length), scopeOffset = 0;
              while (scopeOffset < scopeKeys.length) {
                scopeParams[scopeOffset] = scopeKeys[scopeOffset];
                scopeValues[scopeOffset] = formatStringOrScope[scopeKeys[scopeOffset++]];
              }
              scopeParams[scopeOffset] = source;
              return Function.apply(null, scopeParams).apply(null, scopeValues);
            }
            return Function(source)();
          }
          var formatParams = new Array(arguments.length - 1), formatOffset = 0;
          while (formatOffset < formatParams.length)
            formatParams[formatOffset] = arguments[++formatOffset];
          formatOffset = 0;
          formatStringOrScope = formatStringOrScope.replace(/%([%dfijs])/g, /* @__PURE__ */ __name(function replace($0, $1) {
            var value = formatParams[formatOffset++];
            switch ($1) {
              case "d":
              case "f":
                return String(Number(value));
              case "i":
                return String(Math.floor(value));
              case "j":
                return JSON.stringify(value);
              case "s":
                return String(value);
            }
            return "%";
          }, "replace"));
          if (formatOffset !== formatParams.length)
            throw Error("parameter count mismatch");
          body.push(formatStringOrScope);
          return Codegen;
        }
        __name(Codegen, "Codegen");
        function toString2(functionNameOverride) {
          return "function " + (functionNameOverride || functionName || "") + "(" + (functionParams && functionParams.join(",") || "") + "){\n  " + body.join("\n  ") + "\n}";
        }
        __name(toString2, "toString");
        Codegen.toString = toString2;
        return Codegen;
      }
      __name(codegen, "codegen");
      codegen.verbose = false;
    }
  });

  // node_modules/@protobufjs/fetch/index.js
  var require_fetch = __commonJS({
    "node_modules/@protobufjs/fetch/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = fetch2;
      var asPromise = require_aspromise(), inquire2 = require_inquire();
      var fs = inquire2("fs");
      function fetch2(filename, options, callback) {
        if (typeof options === "function") {
          callback = options;
          options = {};
        } else if (!options)
          options = {};
        if (!callback)
          return asPromise(fetch2, this, filename, options);
        if (!options.xhr && fs && fs.readFile)
          return fs.readFile(filename, /* @__PURE__ */ __name(function fetchReadFileCallback(err, contents2) {
            return err && typeof XMLHttpRequest !== "undefined" ? fetch2.xhr(filename, options, callback) : err ? callback(err) : callback(null, options.binary ? contents2 : contents2.toString("utf8"));
          }, "fetchReadFileCallback"));
        return fetch2.xhr(filename, options, callback);
      }
      __name(fetch2, "fetch");
      fetch2.xhr = /* @__PURE__ */ __name(function fetch_xhr(filename, options, callback) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = /* @__PURE__ */ __name(function fetchOnReadyStateChange() {
          if (xhr.readyState !== 4)
            return void 0;
          if (xhr.status !== 0 && xhr.status !== 200)
            return callback(Error("status " + xhr.status));
          if (options.binary) {
            var buffer = xhr.response;
            if (!buffer) {
              buffer = [];
              for (var i = 0; i < xhr.responseText.length; ++i)
                buffer.push(xhr.responseText.charCodeAt(i) & 255);
            }
            return callback(null, typeof Uint8Array !== "undefined" ? new Uint8Array(buffer) : buffer);
          }
          return callback(null, xhr.responseText);
        }, "fetchOnReadyStateChange");
        if (options.binary) {
          if ("overrideMimeType" in xhr)
            xhr.overrideMimeType("text/plain; charset=x-user-defined");
          xhr.responseType = "arraybuffer";
        }
        xhr.open("GET", filename);
        xhr.send();
      }, "fetch_xhr");
    }
  });

  // node_modules/@protobufjs/path/index.js
  var require_path = __commonJS({
    "node_modules/@protobufjs/path/index.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var path = exports4;
      var isAbsolute = (
        /**
         * Tests if the specified path is absolute.
         * @param {string} path Path to test
         * @returns {boolean} `true` if path is absolute
         */
        path.isAbsolute = /* @__PURE__ */ __name(function isAbsolute2(path2) {
          return /^(?:\/|\w+:)/.test(path2);
        }, "isAbsolute")
      );
      var normalize = (
        /**
         * Normalizes the specified path.
         * @param {string} path Path to normalize
         * @returns {string} Normalized path
         */
        path.normalize = /* @__PURE__ */ __name(function normalize2(path2) {
          path2 = path2.replace(/\\/g, "/").replace(/\/{2,}/g, "/");
          var parts = path2.split("/"), absolute = isAbsolute(path2), prefix = "";
          if (absolute)
            prefix = parts.shift() + "/";
          for (var i = 0; i < parts.length; ) {
            if (parts[i] === "..") {
              if (i > 0 && parts[i - 1] !== "..")
                parts.splice(--i, 2);
              else if (absolute)
                parts.splice(i, 1);
              else
                ++i;
            } else if (parts[i] === ".")
              parts.splice(i, 1);
            else
              ++i;
          }
          return prefix + parts.join("/");
        }, "normalize")
      );
      path.resolve = /* @__PURE__ */ __name(function resolve(originPath, includePath, alreadyNormalized) {
        if (!alreadyNormalized)
          includePath = normalize(includePath);
        if (isAbsolute(includePath))
          return includePath;
        if (!alreadyNormalized)
          originPath = normalize(originPath);
        return (originPath = originPath.replace(/(?:\/|^)[^/]+$/, "")).length ? normalize(originPath + "/" + includePath) : includePath;
      }, "resolve");
    }
  });

  // node_modules/protobufjs/src/types.js
  var require_types = __commonJS({
    "node_modules/protobufjs/src/types.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var types = exports4;
      var util = require_util();
      var s = [
        "double",
        // 0
        "float",
        // 1
        "int32",
        // 2
        "uint32",
        // 3
        "sint32",
        // 4
        "fixed32",
        // 5
        "sfixed32",
        // 6
        "int64",
        // 7
        "uint64",
        // 8
        "sint64",
        // 9
        "fixed64",
        // 10
        "sfixed64",
        // 11
        "bool",
        // 12
        "string",
        // 13
        "bytes"
        // 14
      ];
      function bake(values, offset) {
        var i = 0, o = {};
        offset |= 0;
        while (i < values.length) o[s[i + offset]] = values[i++];
        return o;
      }
      __name(bake, "bake");
      types.basic = bake([
        /* double   */
        1,
        /* float    */
        5,
        /* int32    */
        0,
        /* uint32   */
        0,
        /* sint32   */
        0,
        /* fixed32  */
        5,
        /* sfixed32 */
        5,
        /* int64    */
        0,
        /* uint64   */
        0,
        /* sint64   */
        0,
        /* fixed64  */
        1,
        /* sfixed64 */
        1,
        /* bool     */
        0,
        /* string   */
        2,
        /* bytes    */
        2
      ]);
      types.defaults = bake([
        /* double   */
        0,
        /* float    */
        0,
        /* int32    */
        0,
        /* uint32   */
        0,
        /* sint32   */
        0,
        /* fixed32  */
        0,
        /* sfixed32 */
        0,
        /* int64    */
        0,
        /* uint64   */
        0,
        /* sint64   */
        0,
        /* fixed64  */
        0,
        /* sfixed64 */
        0,
        /* bool     */
        false,
        /* string   */
        "",
        /* bytes    */
        util.emptyArray,
        /* message  */
        null
      ]);
      types.long = bake([
        /* int64    */
        0,
        /* uint64   */
        0,
        /* sint64   */
        0,
        /* fixed64  */
        1,
        /* sfixed64 */
        1
      ], 7);
      types.mapKey = bake([
        /* int32    */
        0,
        /* uint32   */
        0,
        /* sint32   */
        0,
        /* fixed32  */
        5,
        /* sfixed32 */
        5,
        /* int64    */
        0,
        /* uint64   */
        0,
        /* sint64   */
        0,
        /* fixed64  */
        1,
        /* sfixed64 */
        1,
        /* bool     */
        0,
        /* string   */
        2
      ], 2);
      types.packed = bake([
        /* double   */
        1,
        /* float    */
        5,
        /* int32    */
        0,
        /* uint32   */
        0,
        /* sint32   */
        0,
        /* fixed32  */
        5,
        /* sfixed32 */
        5,
        /* int64    */
        0,
        /* uint64   */
        0,
        /* sint64   */
        0,
        /* fixed64  */
        1,
        /* sfixed64 */
        1,
        /* bool     */
        0
      ]);
    }
  });

  // node_modules/protobufjs/src/field.js
  var require_field = __commonJS({
    "node_modules/protobufjs/src/field.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Field;
      var ReflectionObject = require_object();
      ((Field.prototype = Object.create(ReflectionObject.prototype)).constructor = Field).className = "Field";
      var Enum = require_enum(), types = require_types(), util = require_util();
      var Type;
      var ruleRe = /^required|optional|repeated$/;
      Field.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        return new Field(name, json.id, json.type, json.rule, json.extend, json.options, json.comment);
      }, "fromJSON");
      function Field(name, id, type, rule, extend, options, comment) {
        if (util.isObject(rule)) {
          comment = extend;
          options = rule;
          rule = extend = void 0;
        } else if (util.isObject(extend)) {
          comment = options;
          options = extend;
          extend = void 0;
        }
        ReflectionObject.call(this, name, options);
        if (!util.isInteger(id) || id < 0)
          throw TypeError("id must be a non-negative integer");
        if (!util.isString(type))
          throw TypeError("type must be a string");
        if (rule !== void 0 && !ruleRe.test(rule = rule.toString().toLowerCase()))
          throw TypeError("rule must be a string rule");
        if (extend !== void 0 && !util.isString(extend))
          throw TypeError("extend must be a string");
        if (rule === "proto3_optional") {
          rule = "optional";
        }
        this.rule = rule && rule !== "optional" ? rule : void 0;
        this.type = type;
        this.id = id;
        this.extend = extend || void 0;
        this.required = rule === "required";
        this.optional = !this.required;
        this.repeated = rule === "repeated";
        this.map = false;
        this.message = null;
        this.partOf = null;
        this.typeDefault = null;
        this.defaultValue = null;
        this.long = util.Long ? types.long[type] !== void 0 : (
          /* istanbul ignore next */
          false
        );
        this.bytes = type === "bytes";
        this.resolvedType = null;
        this.extensionField = null;
        this.declaringField = null;
        this._packed = null;
        this.comment = comment;
      }
      __name(Field, "Field");
      Object.defineProperty(Field.prototype, "packed", {
        get: /* @__PURE__ */ __name(function() {
          if (this._packed === null)
            this._packed = this.getOption("packed") !== false;
          return this._packed;
        }, "get")
      });
      Field.prototype.setOption = /* @__PURE__ */ __name(function setOption(name, value, ifNotSet) {
        if (name === "packed")
          this._packed = null;
        return ReflectionObject.prototype.setOption.call(this, name, value, ifNotSet);
      }, "setOption");
      Field.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "rule",
          this.rule !== "optional" && this.rule || void 0,
          "type",
          this.type,
          "id",
          this.id,
          "extend",
          this.extend,
          "options",
          this.options,
          "comment",
          keepComments ? this.comment : void 0
        ]);
      }, "toJSON");
      Field.prototype.resolve = /* @__PURE__ */ __name(function resolve() {
        if (this.resolved)
          return this;
        if ((this.typeDefault = types.defaults[this.type]) === void 0) {
          this.resolvedType = (this.declaringField ? this.declaringField.parent : this.parent).lookupTypeOrEnum(this.type);
          if (this.resolvedType instanceof Type)
            this.typeDefault = null;
          else
            this.typeDefault = this.resolvedType.values[Object.keys(this.resolvedType.values)[0]];
        } else if (this.options && this.options.proto3_optional) {
          this.typeDefault = null;
        }
        if (this.options && this.options["default"] != null) {
          this.typeDefault = this.options["default"];
          if (this.resolvedType instanceof Enum && typeof this.typeDefault === "string")
            this.typeDefault = this.resolvedType.values[this.typeDefault];
        }
        if (this.options) {
          if (this.options.packed === true || this.options.packed !== void 0 && this.resolvedType && !(this.resolvedType instanceof Enum))
            delete this.options.packed;
          if (!Object.keys(this.options).length)
            this.options = void 0;
        }
        if (this.long) {
          this.typeDefault = util.Long.fromNumber(this.typeDefault, this.type.charAt(0) === "u");
          if (Object.freeze)
            Object.freeze(this.typeDefault);
        } else if (this.bytes && typeof this.typeDefault === "string") {
          var buf;
          if (util.base64.test(this.typeDefault))
            util.base64.decode(this.typeDefault, buf = util.newBuffer(util.base64.length(this.typeDefault)), 0);
          else
            util.utf8.write(this.typeDefault, buf = util.newBuffer(util.utf8.length(this.typeDefault)), 0);
          this.typeDefault = buf;
        }
        if (this.map)
          this.defaultValue = util.emptyObject;
        else if (this.repeated)
          this.defaultValue = util.emptyArray;
        else
          this.defaultValue = this.typeDefault;
        if (this.parent instanceof Type)
          this.parent.ctor.prototype[this.name] = this.defaultValue;
        return ReflectionObject.prototype.resolve.call(this);
      }, "resolve");
      Field.d = /* @__PURE__ */ __name(function decorateField(fieldId, fieldType, fieldRule, defaultValue) {
        if (typeof fieldType === "function")
          fieldType = util.decorateType(fieldType).name;
        else if (fieldType && typeof fieldType === "object")
          fieldType = util.decorateEnum(fieldType).name;
        return /* @__PURE__ */ __name(function fieldDecorator(prototype, fieldName) {
          util.decorateType(prototype.constructor).add(new Field(fieldName, fieldId, fieldType, fieldRule, { "default": defaultValue }));
        }, "fieldDecorator");
      }, "decorateField");
      Field._configure = /* @__PURE__ */ __name(function configure(Type_) {
        Type = Type_;
      }, "configure");
    }
  });

  // node_modules/protobufjs/src/oneof.js
  var require_oneof = __commonJS({
    "node_modules/protobufjs/src/oneof.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = OneOf;
      var ReflectionObject = require_object();
      ((OneOf.prototype = Object.create(ReflectionObject.prototype)).constructor = OneOf).className = "OneOf";
      var Field = require_field(), util = require_util();
      function OneOf(name, fieldNames, options, comment) {
        if (!Array.isArray(fieldNames)) {
          options = fieldNames;
          fieldNames = void 0;
        }
        ReflectionObject.call(this, name, options);
        if (!(fieldNames === void 0 || Array.isArray(fieldNames)))
          throw TypeError("fieldNames must be an Array");
        this.oneof = fieldNames || [];
        this.fieldsArray = [];
        this.comment = comment;
      }
      __name(OneOf, "OneOf");
      OneOf.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        return new OneOf(name, json.oneof, json.options, json.comment);
      }, "fromJSON");
      OneOf.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "options",
          this.options,
          "oneof",
          this.oneof,
          "comment",
          keepComments ? this.comment : void 0
        ]);
      }, "toJSON");
      function addFieldsToParent(oneof) {
        if (oneof.parent) {
          for (var i = 0; i < oneof.fieldsArray.length; ++i)
            if (!oneof.fieldsArray[i].parent)
              oneof.parent.add(oneof.fieldsArray[i]);
        }
      }
      __name(addFieldsToParent, "addFieldsToParent");
      OneOf.prototype.add = /* @__PURE__ */ __name(function add2(field) {
        if (!(field instanceof Field))
          throw TypeError("field must be a Field");
        if (field.parent && field.parent !== this.parent)
          field.parent.remove(field);
        this.oneof.push(field.name);
        this.fieldsArray.push(field);
        field.partOf = this;
        addFieldsToParent(this);
        return this;
      }, "add");
      OneOf.prototype.remove = /* @__PURE__ */ __name(function remove2(field) {
        if (!(field instanceof Field))
          throw TypeError("field must be a Field");
        var index2 = this.fieldsArray.indexOf(field);
        if (index2 < 0)
          throw Error(field + " is not a member of " + this);
        this.fieldsArray.splice(index2, 1);
        index2 = this.oneof.indexOf(field.name);
        if (index2 > -1)
          this.oneof.splice(index2, 1);
        field.partOf = null;
        return this;
      }, "remove");
      OneOf.prototype.onAdd = /* @__PURE__ */ __name(function onAdd(parent2) {
        ReflectionObject.prototype.onAdd.call(this, parent2);
        var self2 = this;
        for (var i = 0; i < this.oneof.length; ++i) {
          var field = parent2.get(this.oneof[i]);
          if (field && !field.partOf) {
            field.partOf = self2;
            self2.fieldsArray.push(field);
          }
        }
        addFieldsToParent(this);
      }, "onAdd");
      OneOf.prototype.onRemove = /* @__PURE__ */ __name(function onRemove(parent2) {
        for (var i = 0, field; i < this.fieldsArray.length; ++i)
          if ((field = this.fieldsArray[i]).parent)
            field.parent.remove(field);
        ReflectionObject.prototype.onRemove.call(this, parent2);
      }, "onRemove");
      OneOf.d = /* @__PURE__ */ __name(function decorateOneOf() {
        var fieldNames = new Array(arguments.length), index2 = 0;
        while (index2 < arguments.length)
          fieldNames[index2] = arguments[index2++];
        return /* @__PURE__ */ __name(function oneOfDecorator(prototype, oneofName) {
          util.decorateType(prototype.constructor).add(new OneOf(oneofName, fieldNames));
          Object.defineProperty(prototype, oneofName, {
            get: util.oneOfGetter(fieldNames),
            set: util.oneOfSetter(fieldNames)
          });
        }, "oneOfDecorator");
      }, "decorateOneOf");
    }
  });

  // node_modules/protobufjs/src/namespace.js
  var require_namespace = __commonJS({
    "node_modules/protobufjs/src/namespace.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Namespace;
      var ReflectionObject = require_object();
      ((Namespace.prototype = Object.create(ReflectionObject.prototype)).constructor = Namespace).className = "Namespace";
      var Field = require_field(), util = require_util(), OneOf = require_oneof();
      var Type, Service, Enum;
      Namespace.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        return new Namespace(name, json.options).addJSON(json.nested);
      }, "fromJSON");
      function arrayToJSON(array, toJSONOptions) {
        if (!(array && array.length))
          return void 0;
        var obj = {};
        for (var i = 0; i < array.length; ++i)
          obj[array[i].name] = array[i].toJSON(toJSONOptions);
        return obj;
      }
      __name(arrayToJSON, "arrayToJSON");
      Namespace.arrayToJSON = arrayToJSON;
      Namespace.isReservedId = /* @__PURE__ */ __name(function isReservedId(reserved, id) {
        if (reserved) {
          for (var i = 0; i < reserved.length; ++i)
            if (typeof reserved[i] !== "string" && reserved[i][0] <= id && reserved[i][1] > id)
              return true;
        }
        return false;
      }, "isReservedId");
      Namespace.isReservedName = /* @__PURE__ */ __name(function isReservedName(reserved, name) {
        if (reserved) {
          for (var i = 0; i < reserved.length; ++i)
            if (reserved[i] === name)
              return true;
        }
        return false;
      }, "isReservedName");
      function Namespace(name, options) {
        ReflectionObject.call(this, name, options);
        this.nested = void 0;
        this._nestedArray = null;
      }
      __name(Namespace, "Namespace");
      function clearCache(namespace) {
        namespace._nestedArray = null;
        return namespace;
      }
      __name(clearCache, "clearCache");
      Object.defineProperty(Namespace.prototype, "nestedArray", {
        get: /* @__PURE__ */ __name(function() {
          return this._nestedArray || (this._nestedArray = util.toArray(this.nested));
        }, "get")
      });
      Namespace.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        return util.toObject([
          "options",
          this.options,
          "nested",
          arrayToJSON(this.nestedArray, toJSONOptions)
        ]);
      }, "toJSON");
      Namespace.prototype.addJSON = /* @__PURE__ */ __name(function addJSON(nestedJson) {
        var ns = this;
        if (nestedJson) {
          for (var names = Object.keys(nestedJson), i = 0, nested; i < names.length; ++i) {
            nested = nestedJson[names[i]];
            ns.add(
              // most to least likely
              (nested.fields !== void 0 ? Type.fromJSON : nested.values !== void 0 ? Enum.fromJSON : nested.methods !== void 0 ? Service.fromJSON : nested.id !== void 0 ? Field.fromJSON : Namespace.fromJSON)(names[i], nested)
            );
          }
        }
        return this;
      }, "addJSON");
      Namespace.prototype.get = /* @__PURE__ */ __name(function get2(name) {
        return this.nested && this.nested[name] || null;
      }, "get");
      Namespace.prototype.getEnum = /* @__PURE__ */ __name(function getEnum(name) {
        if (this.nested && this.nested[name] instanceof Enum)
          return this.nested[name].values;
        throw Error("no such enum: " + name);
      }, "getEnum");
      Namespace.prototype.add = /* @__PURE__ */ __name(function add2(object) {
        if (!(object instanceof Field && object.extend !== void 0 || object instanceof Type || object instanceof OneOf || object instanceof Enum || object instanceof Service || object instanceof Namespace))
          throw TypeError("object must be a valid nested object");
        if (!this.nested)
          this.nested = {};
        else {
          var prev2 = this.get(object.name);
          if (prev2) {
            if (prev2 instanceof Namespace && object instanceof Namespace && !(prev2 instanceof Type || prev2 instanceof Service)) {
              var nested = prev2.nestedArray;
              for (var i = 0; i < nested.length; ++i)
                object.add(nested[i]);
              this.remove(prev2);
              if (!this.nested)
                this.nested = {};
              object.setOptions(prev2.options, true);
            } else
              throw Error("duplicate name '" + object.name + "' in " + this);
          }
        }
        this.nested[object.name] = object;
        object.onAdd(this);
        return clearCache(this);
      }, "add");
      Namespace.prototype.remove = /* @__PURE__ */ __name(function remove2(object) {
        if (!(object instanceof ReflectionObject))
          throw TypeError("object must be a ReflectionObject");
        if (object.parent !== this)
          throw Error(object + " is not a member of " + this);
        delete this.nested[object.name];
        if (!Object.keys(this.nested).length)
          this.nested = void 0;
        object.onRemove(this);
        return clearCache(this);
      }, "remove");
      Namespace.prototype.define = /* @__PURE__ */ __name(function define(path, json) {
        if (util.isString(path))
          path = path.split(".");
        else if (!Array.isArray(path))
          throw TypeError("illegal path");
        if (path && path.length && path[0] === "")
          throw Error("path must be relative");
        var ptr = this;
        while (path.length > 0) {
          var part = path.shift();
          if (ptr.nested && ptr.nested[part]) {
            ptr = ptr.nested[part];
            if (!(ptr instanceof Namespace))
              throw Error("path conflicts with non-namespace objects");
          } else
            ptr.add(ptr = new Namespace(part));
        }
        if (json)
          ptr.addJSON(json);
        return ptr;
      }, "define");
      Namespace.prototype.resolveAll = /* @__PURE__ */ __name(function resolveAll() {
        var nested = this.nestedArray, i = 0;
        while (i < nested.length)
          if (nested[i] instanceof Namespace)
            nested[i++].resolveAll();
          else
            nested[i++].resolve();
        return this.resolve();
      }, "resolveAll");
      Namespace.prototype.lookup = /* @__PURE__ */ __name(function lookup(path, filterTypes, parentAlreadyChecked) {
        if (typeof filterTypes === "boolean") {
          parentAlreadyChecked = filterTypes;
          filterTypes = void 0;
        } else if (filterTypes && !Array.isArray(filterTypes))
          filterTypes = [filterTypes];
        if (util.isString(path) && path.length) {
          if (path === ".")
            return this.root;
          path = path.split(".");
        } else if (!path.length)
          return this;
        if (path[0] === "")
          return this.root.lookup(path.slice(1), filterTypes);
        var found = this.get(path[0]);
        if (found) {
          if (path.length === 1) {
            if (!filterTypes || filterTypes.indexOf(found.constructor) > -1)
              return found;
          } else if (found instanceof Namespace && (found = found.lookup(path.slice(1), filterTypes, true)))
            return found;
        } else
          for (var i = 0; i < this.nestedArray.length; ++i)
            if (this._nestedArray[i] instanceof Namespace && (found = this._nestedArray[i].lookup(path, filterTypes, true)))
              return found;
        if (this.parent === null || parentAlreadyChecked)
          return null;
        return this.parent.lookup(path, filterTypes);
      }, "lookup");
      Namespace.prototype.lookupType = /* @__PURE__ */ __name(function lookupType(path) {
        var found = this.lookup(path, [Type]);
        if (!found)
          throw Error("no such type: " + path);
        return found;
      }, "lookupType");
      Namespace.prototype.lookupEnum = /* @__PURE__ */ __name(function lookupEnum(path) {
        var found = this.lookup(path, [Enum]);
        if (!found)
          throw Error("no such Enum '" + path + "' in " + this);
        return found;
      }, "lookupEnum");
      Namespace.prototype.lookupTypeOrEnum = /* @__PURE__ */ __name(function lookupTypeOrEnum(path) {
        var found = this.lookup(path, [Type, Enum]);
        if (!found)
          throw Error("no such Type or Enum '" + path + "' in " + this);
        return found;
      }, "lookupTypeOrEnum");
      Namespace.prototype.lookupService = /* @__PURE__ */ __name(function lookupService(path) {
        var found = this.lookup(path, [Service]);
        if (!found)
          throw Error("no such Service '" + path + "' in " + this);
        return found;
      }, "lookupService");
      Namespace._configure = function(Type_, Service_, Enum_) {
        Type = Type_;
        Service = Service_;
        Enum = Enum_;
      };
    }
  });

  // node_modules/protobufjs/src/mapfield.js
  var require_mapfield = __commonJS({
    "node_modules/protobufjs/src/mapfield.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = MapField;
      var Field = require_field();
      ((MapField.prototype = Object.create(Field.prototype)).constructor = MapField).className = "MapField";
      var types = require_types(), util = require_util();
      function MapField(name, id, keyType, type, options, comment) {
        Field.call(this, name, id, type, void 0, void 0, options, comment);
        if (!util.isString(keyType))
          throw TypeError("keyType must be a string");
        this.keyType = keyType;
        this.resolvedKeyType = null;
        this.map = true;
      }
      __name(MapField, "MapField");
      MapField.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        return new MapField(name, json.id, json.keyType, json.type, json.options, json.comment);
      }, "fromJSON");
      MapField.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "keyType",
          this.keyType,
          "type",
          this.type,
          "id",
          this.id,
          "extend",
          this.extend,
          "options",
          this.options,
          "comment",
          keepComments ? this.comment : void 0
        ]);
      }, "toJSON");
      MapField.prototype.resolve = /* @__PURE__ */ __name(function resolve() {
        if (this.resolved)
          return this;
        if (types.mapKey[this.keyType] === void 0)
          throw Error("invalid key type: " + this.keyType);
        return Field.prototype.resolve.call(this);
      }, "resolve");
      MapField.d = /* @__PURE__ */ __name(function decorateMapField(fieldId, fieldKeyType, fieldValueType) {
        if (typeof fieldValueType === "function")
          fieldValueType = util.decorateType(fieldValueType).name;
        else if (fieldValueType && typeof fieldValueType === "object")
          fieldValueType = util.decorateEnum(fieldValueType).name;
        return /* @__PURE__ */ __name(function mapFieldDecorator(prototype, fieldName) {
          util.decorateType(prototype.constructor).add(new MapField(fieldName, fieldId, fieldKeyType, fieldValueType));
        }, "mapFieldDecorator");
      }, "decorateMapField");
    }
  });

  // node_modules/protobufjs/src/method.js
  var require_method = __commonJS({
    "node_modules/protobufjs/src/method.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Method;
      var ReflectionObject = require_object();
      ((Method.prototype = Object.create(ReflectionObject.prototype)).constructor = Method).className = "Method";
      var util = require_util();
      function Method(name, type, requestType, responseType, requestStream, responseStream, options, comment, parsedOptions) {
        if (util.isObject(requestStream)) {
          options = requestStream;
          requestStream = responseStream = void 0;
        } else if (util.isObject(responseStream)) {
          options = responseStream;
          responseStream = void 0;
        }
        if (!(type === void 0 || util.isString(type)))
          throw TypeError("type must be a string");
        if (!util.isString(requestType))
          throw TypeError("requestType must be a string");
        if (!util.isString(responseType))
          throw TypeError("responseType must be a string");
        ReflectionObject.call(this, name, options);
        this.type = type || "rpc";
        this.requestType = requestType;
        this.requestStream = requestStream ? true : void 0;
        this.responseType = responseType;
        this.responseStream = responseStream ? true : void 0;
        this.resolvedRequestType = null;
        this.resolvedResponseType = null;
        this.comment = comment;
        this.parsedOptions = parsedOptions;
      }
      __name(Method, "Method");
      Method.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        return new Method(name, json.type, json.requestType, json.responseType, json.requestStream, json.responseStream, json.options, json.comment, json.parsedOptions);
      }, "fromJSON");
      Method.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "type",
          this.type !== "rpc" && /* istanbul ignore next */
          this.type || void 0,
          "requestType",
          this.requestType,
          "requestStream",
          this.requestStream,
          "responseType",
          this.responseType,
          "responseStream",
          this.responseStream,
          "options",
          this.options,
          "comment",
          keepComments ? this.comment : void 0,
          "parsedOptions",
          this.parsedOptions
        ]);
      }, "toJSON");
      Method.prototype.resolve = /* @__PURE__ */ __name(function resolve() {
        if (this.resolved)
          return this;
        this.resolvedRequestType = this.parent.lookupType(this.requestType);
        this.resolvedResponseType = this.parent.lookupType(this.responseType);
        return ReflectionObject.prototype.resolve.call(this);
      }, "resolve");
    }
  });

  // node_modules/protobufjs/src/service.js
  var require_service2 = __commonJS({
    "node_modules/protobufjs/src/service.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Service;
      var Namespace = require_namespace();
      ((Service.prototype = Object.create(Namespace.prototype)).constructor = Service).className = "Service";
      var Method = require_method(), util = require_util(), rpc = require_rpc();
      function Service(name, options) {
        Namespace.call(this, name, options);
        this.methods = {};
        this._methodsArray = null;
      }
      __name(Service, "Service");
      Service.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        var service = new Service(name, json.options);
        if (json.methods)
          for (var names = Object.keys(json.methods), i = 0; i < names.length; ++i)
            service.add(Method.fromJSON(names[i], json.methods[names[i]]));
        if (json.nested)
          service.addJSON(json.nested);
        service.comment = json.comment;
        return service;
      }, "fromJSON");
      Service.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var inherited = Namespace.prototype.toJSON.call(this, toJSONOptions);
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "options",
          inherited && inherited.options || void 0,
          "methods",
          Namespace.arrayToJSON(this.methodsArray, toJSONOptions) || /* istanbul ignore next */
          {},
          "nested",
          inherited && inherited.nested || void 0,
          "comment",
          keepComments ? this.comment : void 0
        ]);
      }, "toJSON");
      Object.defineProperty(Service.prototype, "methodsArray", {
        get: /* @__PURE__ */ __name(function() {
          return this._methodsArray || (this._methodsArray = util.toArray(this.methods));
        }, "get")
      });
      function clearCache(service) {
        service._methodsArray = null;
        return service;
      }
      __name(clearCache, "clearCache");
      Service.prototype.get = /* @__PURE__ */ __name(function get2(name) {
        return this.methods[name] || Namespace.prototype.get.call(this, name);
      }, "get");
      Service.prototype.resolveAll = /* @__PURE__ */ __name(function resolveAll() {
        var methods = this.methodsArray;
        for (var i = 0; i < methods.length; ++i)
          methods[i].resolve();
        return Namespace.prototype.resolve.call(this);
      }, "resolveAll");
      Service.prototype.add = /* @__PURE__ */ __name(function add2(object) {
        if (this.get(object.name))
          throw Error("duplicate name '" + object.name + "' in " + this);
        if (object instanceof Method) {
          this.methods[object.name] = object;
          object.parent = this;
          return clearCache(this);
        }
        return Namespace.prototype.add.call(this, object);
      }, "add");
      Service.prototype.remove = /* @__PURE__ */ __name(function remove2(object) {
        if (object instanceof Method) {
          if (this.methods[object.name] !== object)
            throw Error(object + " is not a member of " + this);
          delete this.methods[object.name];
          object.parent = null;
          return clearCache(this);
        }
        return Namespace.prototype.remove.call(this, object);
      }, "remove");
      Service.prototype.create = /* @__PURE__ */ __name(function create(rpcImpl, requestDelimited, responseDelimited) {
        var rpcService = new rpc.Service(rpcImpl, requestDelimited, responseDelimited);
        for (var i = 0, method; i < /* initializes */
        this.methodsArray.length; ++i) {
          var methodName = util.lcFirst((method = this._methodsArray[i]).resolve().name).replace(/[^$\w_]/g, "");
          rpcService[methodName] = util.codegen(["r", "c"], util.isReserved(methodName) ? methodName + "_" : methodName)("return this.rpcCall(m,q,s,r,c)")({
            m: method,
            q: method.resolvedRequestType.ctor,
            s: method.resolvedResponseType.ctor
          });
        }
        return rpcService;
      }, "create");
    }
  });

  // node_modules/protobufjs/src/message.js
  var require_message = __commonJS({
    "node_modules/protobufjs/src/message.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Message;
      var util = require_minimal();
      function Message(properties) {
        if (properties)
          for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
            this[keys[i]] = properties[keys[i]];
      }
      __name(Message, "Message");
      Message.create = /* @__PURE__ */ __name(function create(properties) {
        return this.$type.create(properties);
      }, "create");
      Message.encode = /* @__PURE__ */ __name(function encode(message, writer) {
        return this.$type.encode(message, writer);
      }, "encode");
      Message.encodeDelimited = /* @__PURE__ */ __name(function encodeDelimited(message, writer) {
        return this.$type.encodeDelimited(message, writer);
      }, "encodeDelimited");
      Message.decode = /* @__PURE__ */ __name(function decode(reader) {
        return this.$type.decode(reader);
      }, "decode");
      Message.decodeDelimited = /* @__PURE__ */ __name(function decodeDelimited(reader) {
        return this.$type.decodeDelimited(reader);
      }, "decodeDelimited");
      Message.verify = /* @__PURE__ */ __name(function verify(message) {
        return this.$type.verify(message);
      }, "verify");
      Message.fromObject = /* @__PURE__ */ __name(function fromObject(object) {
        return this.$type.fromObject(object);
      }, "fromObject");
      Message.toObject = /* @__PURE__ */ __name(function toObject(message, options) {
        return this.$type.toObject(message, options);
      }, "toObject");
      Message.prototype.toJSON = /* @__PURE__ */ __name(function toJSON() {
        return this.$type.toObject(this, util.toJSONOptions);
      }, "toJSON");
    }
  });

  // node_modules/protobufjs/src/decoder.js
  var require_decoder = __commonJS({
    "node_modules/protobufjs/src/decoder.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = decoder;
      var Enum = require_enum(), types = require_types(), util = require_util();
      function missing(field) {
        return "missing required '" + field.name + "'";
      }
      __name(missing, "missing");
      function decoder(mtype) {
        var gen = util.codegen(["r", "l"], mtype.name + "$decode")("if(!(r instanceof Reader))")("r=Reader.create(r)")("var c=l===undefined?r.len:r.pos+l,m=new this.ctor" + (mtype.fieldsArray.filter(function(field2) {
          return field2.map;
        }).length ? ",k,value" : ""))("while(r.pos<c){")("var t=r.uint32()");
        if (mtype.group) gen("if((t&7)===4)")("break");
        gen("switch(t>>>3){");
        var i = 0;
        for (; i < /* initializes */
        mtype.fieldsArray.length; ++i) {
          var field = mtype._fieldsArray[i].resolve(), type = field.resolvedType instanceof Enum ? "int32" : field.type, ref = "m" + util.safeProp(field.name);
          gen("case %i: {", field.id);
          if (field.map) {
            gen("if(%s===util.emptyObject)", ref)("%s={}", ref)("var c2 = r.uint32()+r.pos");
            if (types.defaults[field.keyType] !== void 0) gen("k=%j", types.defaults[field.keyType]);
            else gen("k=null");
            if (types.defaults[type] !== void 0) gen("value=%j", types.defaults[type]);
            else gen("value=null");
            gen("while(r.pos<c2){")("var tag2=r.uint32()")("switch(tag2>>>3){")("case 1: k=r.%s(); break", field.keyType)("case 2:");
            if (types.basic[type] === void 0) gen("value=types[%i].decode(r,r.uint32())", i);
            else gen("value=r.%s()", type);
            gen("break")("default:")("r.skipType(tag2&7)")("break")("}")("}");
            if (types.long[field.keyType] !== void 0) gen('%s[typeof k==="object"?util.longToHash(k):k]=value', ref);
            else gen("%s[k]=value", ref);
          } else if (field.repeated) {
            gen("if(!(%s&&%s.length))", ref, ref)("%s=[]", ref);
            if (types.packed[type] !== void 0) gen("if((t&7)===2){")("var c2=r.uint32()+r.pos")("while(r.pos<c2)")("%s.push(r.%s())", ref, type)("}else");
            if (types.basic[type] === void 0) gen(field.resolvedType.group ? "%s.push(types[%i].decode(r))" : "%s.push(types[%i].decode(r,r.uint32()))", ref, i);
            else gen("%s.push(r.%s())", ref, type);
          } else if (types.basic[type] === void 0) gen(field.resolvedType.group ? "%s=types[%i].decode(r)" : "%s=types[%i].decode(r,r.uint32())", ref, i);
          else gen("%s=r.%s()", ref, type);
          gen("break")("}");
        }
        gen("default:")("r.skipType(t&7)")("break")("}")("}");
        for (i = 0; i < mtype._fieldsArray.length; ++i) {
          var rfield = mtype._fieldsArray[i];
          if (rfield.required) gen("if(!m.hasOwnProperty(%j))", rfield.name)("throw util.ProtocolError(%j,{instance:m})", missing(rfield));
        }
        return gen("return m");
      }
      __name(decoder, "decoder");
    }
  });

  // node_modules/protobufjs/src/verifier.js
  var require_verifier = __commonJS({
    "node_modules/protobufjs/src/verifier.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = verifier;
      var Enum = require_enum(), util = require_util();
      function invalid(field, expected) {
        return field.name + ": " + expected + (field.repeated && expected !== "array" ? "[]" : field.map && expected !== "object" ? "{k:" + field.keyType + "}" : "") + " expected";
      }
      __name(invalid, "invalid");
      function genVerifyValue(gen, field, fieldIndex, ref) {
        if (field.resolvedType) {
          if (field.resolvedType instanceof Enum) {
            gen("switch(%s){", ref)("default:")("return%j", invalid(field, "enum value"));
            for (var keys = Object.keys(field.resolvedType.values), j = 0; j < keys.length; ++j) gen("case %i:", field.resolvedType.values[keys[j]]);
            gen("break")("}");
          } else {
            gen("{")("var e=types[%i].verify(%s);", fieldIndex, ref)("if(e)")("return%j+e", field.name + ".")("}");
          }
        } else {
          switch (field.type) {
            case "int32":
            case "uint32":
            case "sint32":
            case "fixed32":
            case "sfixed32":
              gen("if(!util.isInteger(%s))", ref)("return%j", invalid(field, "integer"));
              break;
            case "int64":
            case "uint64":
            case "sint64":
            case "fixed64":
            case "sfixed64":
              gen("if(!util.isInteger(%s)&&!(%s&&util.isInteger(%s.low)&&util.isInteger(%s.high)))", ref, ref, ref, ref)("return%j", invalid(field, "integer|Long"));
              break;
            case "float":
            case "double":
              gen('if(typeof %s!=="number")', ref)("return%j", invalid(field, "number"));
              break;
            case "bool":
              gen('if(typeof %s!=="boolean")', ref)("return%j", invalid(field, "boolean"));
              break;
            case "string":
              gen("if(!util.isString(%s))", ref)("return%j", invalid(field, "string"));
              break;
            case "bytes":
              gen('if(!(%s&&typeof %s.length==="number"||util.isString(%s)))', ref, ref, ref)("return%j", invalid(field, "buffer"));
              break;
          }
        }
        return gen;
      }
      __name(genVerifyValue, "genVerifyValue");
      function genVerifyKey(gen, field, ref) {
        switch (field.keyType) {
          case "int32":
          case "uint32":
          case "sint32":
          case "fixed32":
          case "sfixed32":
            gen("if(!util.key32Re.test(%s))", ref)("return%j", invalid(field, "integer key"));
            break;
          case "int64":
          case "uint64":
          case "sint64":
          case "fixed64":
          case "sfixed64":
            gen("if(!util.key64Re.test(%s))", ref)("return%j", invalid(field, "integer|Long key"));
            break;
          case "bool":
            gen("if(!util.key2Re.test(%s))", ref)("return%j", invalid(field, "boolean key"));
            break;
        }
        return gen;
      }
      __name(genVerifyKey, "genVerifyKey");
      function verifier(mtype) {
        var gen = util.codegen(["m"], mtype.name + "$verify")('if(typeof m!=="object"||m===null)')("return%j", "object expected");
        var oneofs = mtype.oneofsArray, seenFirstField = {};
        if (oneofs.length) gen("var p={}");
        for (var i = 0; i < /* initializes */
        mtype.fieldsArray.length; ++i) {
          var field = mtype._fieldsArray[i].resolve(), ref = "m" + util.safeProp(field.name);
          if (field.optional) gen("if(%s!=null&&m.hasOwnProperty(%j)){", ref, field.name);
          if (field.map) {
            gen("if(!util.isObject(%s))", ref)("return%j", invalid(field, "object"))("var k=Object.keys(%s)", ref)("for(var i=0;i<k.length;++i){");
            genVerifyKey(gen, field, "k[i]");
            genVerifyValue(gen, field, i, ref + "[k[i]]")("}");
          } else if (field.repeated) {
            gen("if(!Array.isArray(%s))", ref)("return%j", invalid(field, "array"))("for(var i=0;i<%s.length;++i){", ref);
            genVerifyValue(gen, field, i, ref + "[i]")("}");
          } else {
            if (field.partOf) {
              var oneofProp = util.safeProp(field.partOf.name);
              if (seenFirstField[field.partOf.name] === 1) gen("if(p%s===1)", oneofProp)("return%j", field.partOf.name + ": multiple values");
              seenFirstField[field.partOf.name] = 1;
              gen("p%s=1", oneofProp);
            }
            genVerifyValue(gen, field, i, ref);
          }
          if (field.optional) gen("}");
        }
        return gen("return null");
      }
      __name(verifier, "verifier");
    }
  });

  // node_modules/protobufjs/src/converter.js
  var require_converter = __commonJS({
    "node_modules/protobufjs/src/converter.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var converter = exports4;
      var Enum = require_enum(), util = require_util();
      function genValuePartial_fromObject(gen, field, fieldIndex, prop2) {
        var defaultAlreadyEmitted = false;
        if (field.resolvedType) {
          if (field.resolvedType instanceof Enum) {
            gen("switch(d%s){", prop2);
            for (var values = field.resolvedType.values, keys = Object.keys(values), i = 0; i < keys.length; ++i) {
              if (values[keys[i]] === field.typeDefault && !defaultAlreadyEmitted) {
                gen("default:")('if(typeof(d%s)==="number"){m%s=d%s;break}', prop2, prop2, prop2);
                if (!field.repeated) gen("break");
                defaultAlreadyEmitted = true;
              }
              gen("case%j:", keys[i])("case %i:", values[keys[i]])("m%s=%j", prop2, values[keys[i]])("break");
            }
            gen("}");
          } else gen('if(typeof d%s!=="object")', prop2)("throw TypeError(%j)", field.fullName + ": object expected")("m%s=types[%i].fromObject(d%s)", prop2, fieldIndex, prop2);
        } else {
          var isUnsigned = false;
          switch (field.type) {
            case "double":
            case "float":
              gen("m%s=Number(d%s)", prop2, prop2);
              break;
            case "uint32":
            case "fixed32":
              gen("m%s=d%s>>>0", prop2, prop2);
              break;
            case "int32":
            case "sint32":
            case "sfixed32":
              gen("m%s=d%s|0", prop2, prop2);
              break;
            case "uint64":
              isUnsigned = true;
            // eslint-disable-next-line no-fallthrough
            case "int64":
            case "sint64":
            case "fixed64":
            case "sfixed64":
              gen("if(util.Long)")("(m%s=util.Long.fromValue(d%s)).unsigned=%j", prop2, prop2, isUnsigned)('else if(typeof d%s==="string")', prop2)("m%s=parseInt(d%s,10)", prop2, prop2)('else if(typeof d%s==="number")', prop2)("m%s=d%s", prop2, prop2)('else if(typeof d%s==="object")', prop2)("m%s=new util.LongBits(d%s.low>>>0,d%s.high>>>0).toNumber(%s)", prop2, prop2, prop2, isUnsigned ? "true" : "");
              break;
            case "bytes":
              gen('if(typeof d%s==="string")', prop2)("util.base64.decode(d%s,m%s=util.newBuffer(util.base64.length(d%s)),0)", prop2, prop2, prop2)("else if(d%s.length >= 0)", prop2)("m%s=d%s", prop2, prop2);
              break;
            case "string":
              gen("m%s=String(d%s)", prop2, prop2);
              break;
            case "bool":
              gen("m%s=Boolean(d%s)", prop2, prop2);
              break;
          }
        }
        return gen;
      }
      __name(genValuePartial_fromObject, "genValuePartial_fromObject");
      converter.fromObject = /* @__PURE__ */ __name(function fromObject(mtype) {
        var fields = mtype.fieldsArray;
        var gen = util.codegen(["d"], mtype.name + "$fromObject")("if(d instanceof this.ctor)")("return d");
        if (!fields.length) return gen("return new this.ctor");
        gen("var m=new this.ctor");
        for (var i = 0; i < fields.length; ++i) {
          var field = fields[i].resolve(), prop2 = util.safeProp(field.name);
          if (field.map) {
            gen("if(d%s){", prop2)('if(typeof d%s!=="object")', prop2)("throw TypeError(%j)", field.fullName + ": object expected")("m%s={}", prop2)("for(var ks=Object.keys(d%s),i=0;i<ks.length;++i){", prop2);
            genValuePartial_fromObject(
              gen,
              field,
              /* not sorted */
              i,
              prop2 + "[ks[i]]"
            )("}")("}");
          } else if (field.repeated) {
            gen("if(d%s){", prop2)("if(!Array.isArray(d%s))", prop2)("throw TypeError(%j)", field.fullName + ": array expected")("m%s=[]", prop2)("for(var i=0;i<d%s.length;++i){", prop2);
            genValuePartial_fromObject(
              gen,
              field,
              /* not sorted */
              i,
              prop2 + "[i]"
            )("}")("}");
          } else {
            if (!(field.resolvedType instanceof Enum)) gen("if(d%s!=null){", prop2);
            genValuePartial_fromObject(
              gen,
              field,
              /* not sorted */
              i,
              prop2
            );
            if (!(field.resolvedType instanceof Enum)) gen("}");
          }
        }
        return gen("return m");
      }, "fromObject");
      function genValuePartial_toObject(gen, field, fieldIndex, prop2) {
        if (field.resolvedType) {
          if (field.resolvedType instanceof Enum) gen("d%s=o.enums===String?(types[%i].values[m%s]===undefined?m%s:types[%i].values[m%s]):m%s", prop2, fieldIndex, prop2, prop2, fieldIndex, prop2, prop2);
          else gen("d%s=types[%i].toObject(m%s,o)", prop2, fieldIndex, prop2);
        } else {
          var isUnsigned = false;
          switch (field.type) {
            case "double":
            case "float":
              gen("d%s=o.json&&!isFinite(m%s)?String(m%s):m%s", prop2, prop2, prop2, prop2);
              break;
            case "uint64":
              isUnsigned = true;
            // eslint-disable-next-line no-fallthrough
            case "int64":
            case "sint64":
            case "fixed64":
            case "sfixed64":
              gen('if(typeof m%s==="number")', prop2)("d%s=o.longs===String?String(m%s):m%s", prop2, prop2, prop2)("else")("d%s=o.longs===String?util.Long.prototype.toString.call(m%s):o.longs===Number?new util.LongBits(m%s.low>>>0,m%s.high>>>0).toNumber(%s):m%s", prop2, prop2, prop2, prop2, isUnsigned ? "true" : "", prop2);
              break;
            case "bytes":
              gen("d%s=o.bytes===String?util.base64.encode(m%s,0,m%s.length):o.bytes===Array?Array.prototype.slice.call(m%s):m%s", prop2, prop2, prop2, prop2, prop2);
              break;
            default:
              gen("d%s=m%s", prop2, prop2);
              break;
          }
        }
        return gen;
      }
      __name(genValuePartial_toObject, "genValuePartial_toObject");
      converter.toObject = /* @__PURE__ */ __name(function toObject(mtype) {
        var fields = mtype.fieldsArray.slice().sort(util.compareFieldsById);
        if (!fields.length)
          return util.codegen()("return {}");
        var gen = util.codegen(["m", "o"], mtype.name + "$toObject")("if(!o)")("o={}")("var d={}");
        var repeatedFields = [], mapFields = [], normalFields = [], i = 0;
        for (; i < fields.length; ++i)
          if (!fields[i].partOf)
            (fields[i].resolve().repeated ? repeatedFields : fields[i].map ? mapFields : normalFields).push(fields[i]);
        if (repeatedFields.length) {
          gen("if(o.arrays||o.defaults){");
          for (i = 0; i < repeatedFields.length; ++i) gen("d%s=[]", util.safeProp(repeatedFields[i].name));
          gen("}");
        }
        if (mapFields.length) {
          gen("if(o.objects||o.defaults){");
          for (i = 0; i < mapFields.length; ++i) gen("d%s={}", util.safeProp(mapFields[i].name));
          gen("}");
        }
        if (normalFields.length) {
          gen("if(o.defaults){");
          for (i = 0; i < normalFields.length; ++i) {
            var field = normalFields[i], prop2 = util.safeProp(field.name);
            if (field.resolvedType instanceof Enum) gen("d%s=o.enums===String?%j:%j", prop2, field.resolvedType.valuesById[field.typeDefault], field.typeDefault);
            else if (field.long) gen("if(util.Long){")("var n=new util.Long(%i,%i,%j)", field.typeDefault.low, field.typeDefault.high, field.typeDefault.unsigned)("d%s=o.longs===String?n.toString():o.longs===Number?n.toNumber():n", prop2)("}else")("d%s=o.longs===String?%j:%i", prop2, field.typeDefault.toString(), field.typeDefault.toNumber());
            else if (field.bytes) {
              var arrayDefault = "[" + Array.prototype.slice.call(field.typeDefault).join(",") + "]";
              gen("if(o.bytes===String)d%s=%j", prop2, String.fromCharCode.apply(String, field.typeDefault))("else{")("d%s=%s", prop2, arrayDefault)("if(o.bytes!==Array)d%s=util.newBuffer(d%s)", prop2, prop2)("}");
            } else gen("d%s=%j", prop2, field.typeDefault);
          }
          gen("}");
        }
        var hasKs2 = false;
        for (i = 0; i < fields.length; ++i) {
          var field = fields[i], index2 = mtype._fieldsArray.indexOf(field), prop2 = util.safeProp(field.name);
          if (field.map) {
            if (!hasKs2) {
              hasKs2 = true;
              gen("var ks2");
            }
            gen("if(m%s&&(ks2=Object.keys(m%s)).length){", prop2, prop2)("d%s={}", prop2)("for(var j=0;j<ks2.length;++j){");
            genValuePartial_toObject(
              gen,
              field,
              /* sorted */
              index2,
              prop2 + "[ks2[j]]"
            )("}");
          } else if (field.repeated) {
            gen("if(m%s&&m%s.length){", prop2, prop2)("d%s=[]", prop2)("for(var j=0;j<m%s.length;++j){", prop2);
            genValuePartial_toObject(
              gen,
              field,
              /* sorted */
              index2,
              prop2 + "[j]"
            )("}");
          } else {
            gen("if(m%s!=null&&m.hasOwnProperty(%j)){", prop2, field.name);
            genValuePartial_toObject(
              gen,
              field,
              /* sorted */
              index2,
              prop2
            );
            if (field.partOf) gen("if(o.oneofs)")("d%s=%j", util.safeProp(field.partOf.name), field.name);
          }
          gen("}");
        }
        return gen("return d");
      }, "toObject");
    }
  });

  // node_modules/protobufjs/src/wrappers.js
  var require_wrappers = __commonJS({
    "node_modules/protobufjs/src/wrappers.js"(exports4) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var wrappers = exports4;
      var Message = require_message();
      wrappers[".google.protobuf.Any"] = {
        fromObject: /* @__PURE__ */ __name(function(object) {
          if (object && object["@type"]) {
            var name = object["@type"].substring(object["@type"].lastIndexOf("/") + 1);
            var type = this.lookup(name);
            if (type) {
              var type_url = object["@type"].charAt(0) === "." ? object["@type"].slice(1) : object["@type"];
              if (type_url.indexOf("/") === -1) {
                type_url = "/" + type_url;
              }
              return this.create({
                type_url,
                value: type.encode(type.fromObject(object)).finish()
              });
            }
          }
          return this.fromObject(object);
        }, "fromObject"),
        toObject: /* @__PURE__ */ __name(function(message, options) {
          var googleApi = "type.googleapis.com/";
          var prefix = "";
          var name = "";
          if (options && options.json && message.type_url && message.value) {
            name = message.type_url.substring(message.type_url.lastIndexOf("/") + 1);
            prefix = message.type_url.substring(0, message.type_url.lastIndexOf("/") + 1);
            var type = this.lookup(name);
            if (type)
              message = type.decode(message.value);
          }
          if (!(message instanceof this.ctor) && message instanceof Message) {
            var object = message.$type.toObject(message, options);
            var messageName = message.$type.fullName[0] === "." ? message.$type.fullName.slice(1) : message.$type.fullName;
            if (prefix === "") {
              prefix = googleApi;
            }
            name = prefix + messageName;
            object["@type"] = name;
            return object;
          }
          return this.toObject(message, options);
        }, "toObject")
      };
    }
  });

  // node_modules/protobufjs/src/type.js
  var require_type = __commonJS({
    "node_modules/protobufjs/src/type.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Type;
      var Namespace = require_namespace();
      ((Type.prototype = Object.create(Namespace.prototype)).constructor = Type).className = "Type";
      var Enum = require_enum(), OneOf = require_oneof(), Field = require_field(), MapField = require_mapfield(), Service = require_service2(), Message = require_message(), Reader = require_reader(), Writer = require_writer(), util = require_util(), encoder = require_encoder(), decoder = require_decoder(), verifier = require_verifier(), converter = require_converter(), wrappers = require_wrappers();
      function Type(name, options) {
        Namespace.call(this, name, options);
        this.fields = {};
        this.oneofs = void 0;
        this.extensions = void 0;
        this.reserved = void 0;
        this.group = void 0;
        this._fieldsById = null;
        this._fieldsArray = null;
        this._oneofsArray = null;
        this._ctor = null;
      }
      __name(Type, "Type");
      Object.defineProperties(Type.prototype, {
        /**
         * Message fields by id.
         * @name Type#fieldsById
         * @type {Object.<number,Field>}
         * @readonly
         */
        fieldsById: {
          get: /* @__PURE__ */ __name(function() {
            if (this._fieldsById)
              return this._fieldsById;
            this._fieldsById = {};
            for (var names = Object.keys(this.fields), i = 0; i < names.length; ++i) {
              var field = this.fields[names[i]], id = field.id;
              if (this._fieldsById[id])
                throw Error("duplicate id " + id + " in " + this);
              this._fieldsById[id] = field;
            }
            return this._fieldsById;
          }, "get")
        },
        /**
         * Fields of this message as an array for iteration.
         * @name Type#fieldsArray
         * @type {Field[]}
         * @readonly
         */
        fieldsArray: {
          get: /* @__PURE__ */ __name(function() {
            return this._fieldsArray || (this._fieldsArray = util.toArray(this.fields));
          }, "get")
        },
        /**
         * Oneofs of this message as an array for iteration.
         * @name Type#oneofsArray
         * @type {OneOf[]}
         * @readonly
         */
        oneofsArray: {
          get: /* @__PURE__ */ __name(function() {
            return this._oneofsArray || (this._oneofsArray = util.toArray(this.oneofs));
          }, "get")
        },
        /**
         * The registered constructor, if any registered, otherwise a generic constructor.
         * Assigning a function replaces the internal constructor. If the function does not extend {@link Message} yet, its prototype will be setup accordingly and static methods will be populated. If it already extends {@link Message}, it will just replace the internal constructor.
         * @name Type#ctor
         * @type {Constructor<{}>}
         */
        ctor: {
          get: /* @__PURE__ */ __name(function() {
            return this._ctor || (this.ctor = Type.generateConstructor(this)());
          }, "get"),
          set: /* @__PURE__ */ __name(function(ctor) {
            var prototype = ctor.prototype;
            if (!(prototype instanceof Message)) {
              (ctor.prototype = new Message()).constructor = ctor;
              util.merge(ctor.prototype, prototype);
            }
            ctor.$type = ctor.prototype.$type = this;
            util.merge(ctor, Message, true);
            this._ctor = ctor;
            var i = 0;
            for (; i < /* initializes */
            this.fieldsArray.length; ++i)
              this._fieldsArray[i].resolve();
            var ctorProperties = {};
            for (i = 0; i < /* initializes */
            this.oneofsArray.length; ++i)
              ctorProperties[this._oneofsArray[i].resolve().name] = {
                get: util.oneOfGetter(this._oneofsArray[i].oneof),
                set: util.oneOfSetter(this._oneofsArray[i].oneof)
              };
            if (i)
              Object.defineProperties(ctor.prototype, ctorProperties);
          }, "set")
        }
      });
      Type.generateConstructor = /* @__PURE__ */ __name(function generateConstructor(mtype) {
        var gen = util.codegen(["p"], mtype.name);
        for (var i = 0, field; i < mtype.fieldsArray.length; ++i)
          if ((field = mtype._fieldsArray[i]).map) gen("this%s={}", util.safeProp(field.name));
          else if (field.repeated) gen("this%s=[]", util.safeProp(field.name));
        return gen("if(p)for(var ks=Object.keys(p),i=0;i<ks.length;++i)if(p[ks[i]]!=null)")("this[ks[i]]=p[ks[i]]");
      }, "generateConstructor");
      function clearCache(type) {
        type._fieldsById = type._fieldsArray = type._oneofsArray = null;
        delete type.encode;
        delete type.decode;
        delete type.verify;
        return type;
      }
      __name(clearCache, "clearCache");
      Type.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        var type = new Type(name, json.options);
        type.extensions = json.extensions;
        type.reserved = json.reserved;
        var names = Object.keys(json.fields), i = 0;
        for (; i < names.length; ++i)
          type.add(
            (typeof json.fields[names[i]].keyType !== "undefined" ? MapField.fromJSON : Field.fromJSON)(names[i], json.fields[names[i]])
          );
        if (json.oneofs)
          for (names = Object.keys(json.oneofs), i = 0; i < names.length; ++i)
            type.add(OneOf.fromJSON(names[i], json.oneofs[names[i]]));
        if (json.nested)
          for (names = Object.keys(json.nested), i = 0; i < names.length; ++i) {
            var nested = json.nested[names[i]];
            type.add(
              // most to least likely
              (nested.id !== void 0 ? Field.fromJSON : nested.fields !== void 0 ? Type.fromJSON : nested.values !== void 0 ? Enum.fromJSON : nested.methods !== void 0 ? Service.fromJSON : Namespace.fromJSON)(names[i], nested)
            );
          }
        if (json.extensions && json.extensions.length)
          type.extensions = json.extensions;
        if (json.reserved && json.reserved.length)
          type.reserved = json.reserved;
        if (json.group)
          type.group = true;
        if (json.comment)
          type.comment = json.comment;
        return type;
      }, "fromJSON");
      Type.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var inherited = Namespace.prototype.toJSON.call(this, toJSONOptions);
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "options",
          inherited && inherited.options || void 0,
          "oneofs",
          Namespace.arrayToJSON(this.oneofsArray, toJSONOptions),
          "fields",
          Namespace.arrayToJSON(this.fieldsArray.filter(function(obj) {
            return !obj.declaringField;
          }), toJSONOptions) || {},
          "extensions",
          this.extensions && this.extensions.length ? this.extensions : void 0,
          "reserved",
          this.reserved && this.reserved.length ? this.reserved : void 0,
          "group",
          this.group || void 0,
          "nested",
          inherited && inherited.nested || void 0,
          "comment",
          keepComments ? this.comment : void 0
        ]);
      }, "toJSON");
      Type.prototype.resolveAll = /* @__PURE__ */ __name(function resolveAll() {
        var fields = this.fieldsArray, i = 0;
        while (i < fields.length)
          fields[i++].resolve();
        var oneofs = this.oneofsArray;
        i = 0;
        while (i < oneofs.length)
          oneofs[i++].resolve();
        return Namespace.prototype.resolveAll.call(this);
      }, "resolveAll");
      Type.prototype.get = /* @__PURE__ */ __name(function get2(name) {
        return this.fields[name] || this.oneofs && this.oneofs[name] || this.nested && this.nested[name] || null;
      }, "get");
      Type.prototype.add = /* @__PURE__ */ __name(function add2(object) {
        if (this.get(object.name))
          throw Error("duplicate name '" + object.name + "' in " + this);
        if (object instanceof Field && object.extend === void 0) {
          if (this._fieldsById ? (
            /* istanbul ignore next */
            this._fieldsById[object.id]
          ) : this.fieldsById[object.id])
            throw Error("duplicate id " + object.id + " in " + this);
          if (this.isReservedId(object.id))
            throw Error("id " + object.id + " is reserved in " + this);
          if (this.isReservedName(object.name))
            throw Error("name '" + object.name + "' is reserved in " + this);
          if (object.parent)
            object.parent.remove(object);
          this.fields[object.name] = object;
          object.message = this;
          object.onAdd(this);
          return clearCache(this);
        }
        if (object instanceof OneOf) {
          if (!this.oneofs)
            this.oneofs = {};
          this.oneofs[object.name] = object;
          object.onAdd(this);
          return clearCache(this);
        }
        return Namespace.prototype.add.call(this, object);
      }, "add");
      Type.prototype.remove = /* @__PURE__ */ __name(function remove2(object) {
        if (object instanceof Field && object.extend === void 0) {
          if (!this.fields || this.fields[object.name] !== object)
            throw Error(object + " is not a member of " + this);
          delete this.fields[object.name];
          object.parent = null;
          object.onRemove(this);
          return clearCache(this);
        }
        if (object instanceof OneOf) {
          if (!this.oneofs || this.oneofs[object.name] !== object)
            throw Error(object + " is not a member of " + this);
          delete this.oneofs[object.name];
          object.parent = null;
          object.onRemove(this);
          return clearCache(this);
        }
        return Namespace.prototype.remove.call(this, object);
      }, "remove");
      Type.prototype.isReservedId = /* @__PURE__ */ __name(function isReservedId(id) {
        return Namespace.isReservedId(this.reserved, id);
      }, "isReservedId");
      Type.prototype.isReservedName = /* @__PURE__ */ __name(function isReservedName(name) {
        return Namespace.isReservedName(this.reserved, name);
      }, "isReservedName");
      Type.prototype.create = /* @__PURE__ */ __name(function create(properties) {
        return new this.ctor(properties);
      }, "create");
      Type.prototype.setup = /* @__PURE__ */ __name(function setup() {
        var fullName = this.fullName, types = [];
        for (var i = 0; i < /* initializes */
        this.fieldsArray.length; ++i)
          types.push(this._fieldsArray[i].resolve().resolvedType);
        this.encode = encoder(this)({
          Writer,
          types,
          util
        });
        this.decode = decoder(this)({
          Reader,
          types,
          util
        });
        this.verify = verifier(this)({
          types,
          util
        });
        this.fromObject = converter.fromObject(this)({
          types,
          util
        });
        this.toObject = converter.toObject(this)({
          types,
          util
        });
        var wrapper = wrappers[fullName];
        if (wrapper) {
          var originalThis = Object.create(this);
          originalThis.fromObject = this.fromObject;
          this.fromObject = wrapper.fromObject.bind(originalThis);
          originalThis.toObject = this.toObject;
          this.toObject = wrapper.toObject.bind(originalThis);
        }
        return this;
      }, "setup");
      Type.prototype.encode = /* @__PURE__ */ __name(function encode_setup(message, writer) {
        return this.setup().encode(message, writer);
      }, "encode_setup");
      Type.prototype.encodeDelimited = /* @__PURE__ */ __name(function encodeDelimited(message, writer) {
        return this.encode(message, writer && writer.len ? writer.fork() : writer).ldelim();
      }, "encodeDelimited");
      Type.prototype.decode = /* @__PURE__ */ __name(function decode_setup(reader, length) {
        return this.setup().decode(reader, length);
      }, "decode_setup");
      Type.prototype.decodeDelimited = /* @__PURE__ */ __name(function decodeDelimited(reader) {
        if (!(reader instanceof Reader))
          reader = Reader.create(reader);
        return this.decode(reader, reader.uint32());
      }, "decodeDelimited");
      Type.prototype.verify = /* @__PURE__ */ __name(function verify_setup(message) {
        return this.setup().verify(message);
      }, "verify_setup");
      Type.prototype.fromObject = /* @__PURE__ */ __name(function fromObject(object) {
        return this.setup().fromObject(object);
      }, "fromObject");
      Type.prototype.toObject = /* @__PURE__ */ __name(function toObject(message, options) {
        return this.setup().toObject(message, options);
      }, "toObject");
      Type.d = /* @__PURE__ */ __name(function decorateType(typeName) {
        return /* @__PURE__ */ __name(function typeDecorator(target) {
          util.decorateType(target, typeName);
        }, "typeDecorator");
      }, "decorateType");
    }
  });

  // node_modules/protobufjs/src/root.js
  var require_root = __commonJS({
    "node_modules/protobufjs/src/root.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Root;
      var Namespace = require_namespace();
      ((Root.prototype = Object.create(Namespace.prototype)).constructor = Root).className = "Root";
      var Field = require_field(), Enum = require_enum(), OneOf = require_oneof(), util = require_util();
      var Type, parse5, common;
      function Root(options) {
        Namespace.call(this, "", options);
        this.deferred = [];
        this.files = [];
      }
      __name(Root, "Root");
      Root.fromJSON = /* @__PURE__ */ __name(function fromJSON(json, root2) {
        if (!root2)
          root2 = new Root();
        if (json.options)
          root2.setOptions(json.options);
        return root2.addJSON(json.nested);
      }, "fromJSON");
      Root.prototype.resolvePath = util.path.resolve;
      Root.prototype.fetch = util.fetch;
      function SYNC() {
      }
      __name(SYNC, "SYNC");
      Root.prototype.load = /* @__PURE__ */ __name(function load2(filename, options, callback) {
        if (typeof options === "function") {
          callback = options;
          options = void 0;
        }
        var self2 = this;
        if (!callback)
          return util.asPromise(load2, self2, filename, options);
        var sync = callback === SYNC;
        function finish(err, root2) {
          if (!callback)
            return;
          if (sync)
            throw err;
          var cb = callback;
          callback = null;
          cb(err, root2);
        }
        __name(finish, "finish");
        function getBundledFileName(filename2) {
          var idx = filename2.lastIndexOf("google/protobuf/");
          if (idx > -1) {
            var altname = filename2.substring(idx);
            if (altname in common) return altname;
          }
          return null;
        }
        __name(getBundledFileName, "getBundledFileName");
        function process3(filename2, source) {
          try {
            if (util.isString(source) && source.charAt(0) === "{")
              source = JSON.parse(source);
            if (!util.isString(source))
              self2.setOptions(source.options).addJSON(source.nested);
            else {
              parse5.filename = filename2;
              var parsed = parse5(source, self2, options), resolved2, i2 = 0;
              if (parsed.imports) {
                for (; i2 < parsed.imports.length; ++i2)
                  if (resolved2 = getBundledFileName(parsed.imports[i2]) || self2.resolvePath(filename2, parsed.imports[i2]))
                    fetch2(resolved2);
              }
              if (parsed.weakImports) {
                for (i2 = 0; i2 < parsed.weakImports.length; ++i2)
                  if (resolved2 = getBundledFileName(parsed.weakImports[i2]) || self2.resolvePath(filename2, parsed.weakImports[i2]))
                    fetch2(resolved2, true);
              }
            }
          } catch (err) {
            finish(err);
          }
          if (!sync && !queued)
            finish(null, self2);
        }
        __name(process3, "process");
        function fetch2(filename2, weak) {
          filename2 = getBundledFileName(filename2) || filename2;
          if (self2.files.indexOf(filename2) > -1)
            return;
          self2.files.push(filename2);
          if (filename2 in common) {
            if (sync)
              process3(filename2, common[filename2]);
            else {
              ++queued;
              setTimeout(function() {
                --queued;
                process3(filename2, common[filename2]);
              });
            }
            return;
          }
          if (sync) {
            var source;
            try {
              source = util.fs.readFileSync(filename2).toString("utf8");
            } catch (err) {
              if (!weak)
                finish(err);
              return;
            }
            process3(filename2, source);
          } else {
            ++queued;
            self2.fetch(filename2, function(err, source2) {
              --queued;
              if (!callback)
                return;
              if (err) {
                if (!weak)
                  finish(err);
                else if (!queued)
                  finish(null, self2);
                return;
              }
              process3(filename2, source2);
            });
          }
        }
        __name(fetch2, "fetch");
        var queued = 0;
        if (util.isString(filename))
          filename = [filename];
        for (var i = 0, resolved; i < filename.length; ++i)
          if (resolved = self2.resolvePath("", filename[i]))
            fetch2(resolved);
        if (sync)
          return self2;
        if (!queued)
          finish(null, self2);
        return void 0;
      }, "load");
      Root.prototype.loadSync = /* @__PURE__ */ __name(function loadSync(filename, options) {
        if (!util.isNode)
          throw Error("not supported");
        return this.load(filename, options, SYNC);
      }, "loadSync");
      Root.prototype.resolveAll = /* @__PURE__ */ __name(function resolveAll() {
        if (this.deferred.length)
          throw Error("unresolvable extensions: " + this.deferred.map(function(field) {
            return "'extend " + field.extend + "' in " + field.parent.fullName;
          }).join(", "));
        return Namespace.prototype.resolveAll.call(this);
      }, "resolveAll");
      var exposeRe = /^[A-Z]/;
      function tryHandleExtension(root2, field) {
        var extendedType = field.parent.lookup(field.extend);
        if (extendedType) {
          var sisterField = new Field(field.fullName, field.id, field.type, field.rule, void 0, field.options);
          if (extendedType.get(sisterField.name)) {
            return true;
          }
          sisterField.declaringField = field;
          field.extensionField = sisterField;
          extendedType.add(sisterField);
          return true;
        }
        return false;
      }
      __name(tryHandleExtension, "tryHandleExtension");
      Root.prototype._handleAdd = /* @__PURE__ */ __name(function _handleAdd(object) {
        if (object instanceof Field) {
          if (
            /* an extension field (implies not part of a oneof) */
            object.extend !== void 0 && /* not already handled */
            !object.extensionField
          ) {
            if (!tryHandleExtension(this, object))
              this.deferred.push(object);
          }
        } else if (object instanceof Enum) {
          if (exposeRe.test(object.name))
            object.parent[object.name] = object.values;
        } else if (!(object instanceof OneOf)) {
          if (object instanceof Type)
            for (var i = 0; i < this.deferred.length; )
              if (tryHandleExtension(this, this.deferred[i]))
                this.deferred.splice(i, 1);
              else
                ++i;
          for (var j = 0; j < /* initializes */
          object.nestedArray.length; ++j)
            this._handleAdd(object._nestedArray[j]);
          if (exposeRe.test(object.name))
            object.parent[object.name] = object;
        }
      }, "_handleAdd");
      Root.prototype._handleRemove = /* @__PURE__ */ __name(function _handleRemove(object) {
        if (object instanceof Field) {
          if (
            /* an extension field */
            object.extend !== void 0
          ) {
            if (
              /* already handled */
              object.extensionField
            ) {
              object.extensionField.parent.remove(object.extensionField);
              object.extensionField = null;
            } else {
              var index2 = this.deferred.indexOf(object);
              if (index2 > -1)
                this.deferred.splice(index2, 1);
            }
          }
        } else if (object instanceof Enum) {
          if (exposeRe.test(object.name))
            delete object.parent[object.name];
        } else if (object instanceof Namespace) {
          for (var i = 0; i < /* initializes */
          object.nestedArray.length; ++i)
            this._handleRemove(object._nestedArray[i]);
          if (exposeRe.test(object.name))
            delete object.parent[object.name];
        }
      }, "_handleRemove");
      Root._configure = function(Type_, parse_, common_) {
        Type = Type_;
        parse5 = parse_;
        common = common_;
      };
    }
  });

  // node_modules/protobufjs/src/util.js
  var require_util = __commonJS({
    "node_modules/protobufjs/src/util.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var util = module2.exports = require_minimal();
      var roots = require_roots();
      var Type, Enum;
      util.codegen = require_codegen();
      util.fetch = require_fetch();
      util.path = require_path();
      util.fs = util.inquire("fs");
      util.toArray = /* @__PURE__ */ __name(function toArray2(object) {
        if (object) {
          var keys = Object.keys(object), array = new Array(keys.length), index2 = 0;
          while (index2 < keys.length)
            array[index2] = object[keys[index2++]];
          return array;
        }
        return [];
      }, "toArray");
      util.toObject = /* @__PURE__ */ __name(function toObject(array) {
        var object = {}, index2 = 0;
        while (index2 < array.length) {
          var key = array[index2++], val2 = array[index2++];
          if (val2 !== void 0)
            object[key] = val2;
        }
        return object;
      }, "toObject");
      var safePropBackslashRe = /\\/g, safePropQuoteRe = /"/g;
      util.isReserved = /* @__PURE__ */ __name(function isReserved(name) {
        return /^(?:do|if|in|for|let|new|try|var|case|else|enum|eval|false|null|this|true|void|with|break|catch|class|const|super|throw|while|yield|delete|export|import|public|return|static|switch|typeof|default|extends|finally|package|private|continue|debugger|function|arguments|interface|protected|implements|instanceof)$/.test(name);
      }, "isReserved");
      util.safeProp = /* @__PURE__ */ __name(function safeProp(prop2) {
        if (!/^[$\w_]+$/.test(prop2) || util.isReserved(prop2))
          return '["' + prop2.replace(safePropBackslashRe, "\\\\").replace(safePropQuoteRe, '\\"') + '"]';
        return "." + prop2;
      }, "safeProp");
      util.ucFirst = /* @__PURE__ */ __name(function ucFirst(str) {
        return str.charAt(0).toUpperCase() + str.substring(1);
      }, "ucFirst");
      var camelCaseRe = /_([a-z])/g;
      util.camelCase = /* @__PURE__ */ __name(function camelCase2(str) {
        return str.substring(0, 1) + str.substring(1).replace(camelCaseRe, function($0, $1) {
          return $1.toUpperCase();
        });
      }, "camelCase");
      util.compareFieldsById = /* @__PURE__ */ __name(function compareFieldsById(a2, b) {
        return a2.id - b.id;
      }, "compareFieldsById");
      util.decorateType = /* @__PURE__ */ __name(function decorateType(ctor, typeName) {
        if (ctor.$type) {
          if (typeName && ctor.$type.name !== typeName) {
            util.decorateRoot.remove(ctor.$type);
            ctor.$type.name = typeName;
            util.decorateRoot.add(ctor.$type);
          }
          return ctor.$type;
        }
        if (!Type)
          Type = require_type();
        var type = new Type(typeName || ctor.name);
        util.decorateRoot.add(type);
        type.ctor = ctor;
        Object.defineProperty(ctor, "$type", { value: type, enumerable: false });
        Object.defineProperty(ctor.prototype, "$type", { value: type, enumerable: false });
        return type;
      }, "decorateType");
      var decorateEnumIndex = 0;
      util.decorateEnum = /* @__PURE__ */ __name(function decorateEnum(object) {
        if (object.$type)
          return object.$type;
        if (!Enum)
          Enum = require_enum();
        var enm = new Enum("Enum" + decorateEnumIndex++, object);
        util.decorateRoot.add(enm);
        Object.defineProperty(object, "$type", { value: enm, enumerable: false });
        return enm;
      }, "decorateEnum");
      util.setProperty = /* @__PURE__ */ __name(function setProperty(dst, path, value) {
        function setProp2(dst2, path2, value2) {
          var part = path2.shift();
          if (part === "__proto__" || part === "prototype") {
            return dst2;
          }
          if (path2.length > 0) {
            dst2[part] = setProp2(dst2[part] || {}, path2, value2);
          } else {
            var prevValue = dst2[part];
            if (prevValue)
              value2 = [].concat(prevValue).concat(value2);
            dst2[part] = value2;
          }
          return dst2;
        }
        __name(setProp2, "setProp");
        if (typeof dst !== "object")
          throw TypeError("dst must be an object");
        if (!path)
          throw TypeError("path must be specified");
        path = path.split(".");
        return setProp2(dst, path, value);
      }, "setProperty");
      Object.defineProperty(util, "decorateRoot", {
        get: /* @__PURE__ */ __name(function() {
          return roots["decorated"] || (roots["decorated"] = new (require_root())());
        }, "get")
      });
    }
  });

  // node_modules/protobufjs/src/object.js
  var require_object = __commonJS({
    "node_modules/protobufjs/src/object.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = ReflectionObject;
      ReflectionObject.className = "ReflectionObject";
      var util = require_util();
      var Root;
      function ReflectionObject(name, options) {
        if (!util.isString(name))
          throw TypeError("name must be a string");
        if (options && !util.isObject(options))
          throw TypeError("options must be an object");
        this.options = options;
        this.parsedOptions = null;
        this.name = name;
        this.parent = null;
        this.resolved = false;
        this.comment = null;
        this.filename = null;
      }
      __name(ReflectionObject, "ReflectionObject");
      Object.defineProperties(ReflectionObject.prototype, {
        /**
         * Reference to the root namespace.
         * @name ReflectionObject#root
         * @type {Root}
         * @readonly
         */
        root: {
          get: /* @__PURE__ */ __name(function() {
            var ptr = this;
            while (ptr.parent !== null)
              ptr = ptr.parent;
            return ptr;
          }, "get")
        },
        /**
         * Full name including leading dot.
         * @name ReflectionObject#fullName
         * @type {string}
         * @readonly
         */
        fullName: {
          get: /* @__PURE__ */ __name(function() {
            var path = [this.name], ptr = this.parent;
            while (ptr) {
              path.unshift(ptr.name);
              ptr = ptr.parent;
            }
            return path.join(".");
          }, "get")
        }
      });
      ReflectionObject.prototype.toJSON = /* istanbul ignore next */
      /* @__PURE__ */ __name(function toJSON() {
        throw Error();
      }, "toJSON");
      ReflectionObject.prototype.onAdd = /* @__PURE__ */ __name(function onAdd(parent2) {
        if (this.parent && this.parent !== parent2)
          this.parent.remove(this);
        this.parent = parent2;
        this.resolved = false;
        var root2 = parent2.root;
        if (root2 instanceof Root)
          root2._handleAdd(this);
      }, "onAdd");
      ReflectionObject.prototype.onRemove = /* @__PURE__ */ __name(function onRemove(parent2) {
        var root2 = parent2.root;
        if (root2 instanceof Root)
          root2._handleRemove(this);
        this.parent = null;
        this.resolved = false;
      }, "onRemove");
      ReflectionObject.prototype.resolve = /* @__PURE__ */ __name(function resolve() {
        if (this.resolved)
          return this;
        if (this.root instanceof Root)
          this.resolved = true;
        return this;
      }, "resolve");
      ReflectionObject.prototype.getOption = /* @__PURE__ */ __name(function getOption(name) {
        if (this.options)
          return this.options[name];
        return void 0;
      }, "getOption");
      ReflectionObject.prototype.setOption = /* @__PURE__ */ __name(function setOption(name, value, ifNotSet) {
        if (!ifNotSet || !this.options || this.options[name] === void 0)
          (this.options || (this.options = {}))[name] = value;
        return this;
      }, "setOption");
      ReflectionObject.prototype.setParsedOption = /* @__PURE__ */ __name(function setParsedOption(name, value, propName) {
        if (!this.parsedOptions) {
          this.parsedOptions = [];
        }
        var parsedOptions = this.parsedOptions;
        if (propName) {
          var opt = parsedOptions.find(function(opt2) {
            return Object.prototype.hasOwnProperty.call(opt2, name);
          });
          if (opt) {
            var newValue = opt[name];
            util.setProperty(newValue, propName, value);
          } else {
            opt = {};
            opt[name] = util.setProperty({}, propName, value);
            parsedOptions.push(opt);
          }
        } else {
          var newOpt = {};
          newOpt[name] = value;
          parsedOptions.push(newOpt);
        }
        return this;
      }, "setParsedOption");
      ReflectionObject.prototype.setOptions = /* @__PURE__ */ __name(function setOptions(options, ifNotSet) {
        if (options)
          for (var keys = Object.keys(options), i = 0; i < keys.length; ++i)
            this.setOption(keys[i], options[keys[i]], ifNotSet);
        return this;
      }, "setOptions");
      ReflectionObject.prototype.toString = /* @__PURE__ */ __name(function toString2() {
        var className = this.constructor.className, fullName = this.fullName;
        if (fullName.length)
          return className + " " + fullName;
        return className;
      }, "toString");
      ReflectionObject._configure = function(Root_) {
        Root = Root_;
      };
    }
  });

  // node_modules/protobufjs/src/enum.js
  var require_enum = __commonJS({
    "node_modules/protobufjs/src/enum.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = Enum;
      var ReflectionObject = require_object();
      ((Enum.prototype = Object.create(ReflectionObject.prototype)).constructor = Enum).className = "Enum";
      var Namespace = require_namespace(), util = require_util();
      function Enum(name, values, options, comment, comments, valuesOptions) {
        ReflectionObject.call(this, name, options);
        if (values && typeof values !== "object")
          throw TypeError("values must be an object");
        this.valuesById = {};
        this.values = Object.create(this.valuesById);
        this.comment = comment;
        this.comments = comments || {};
        this.valuesOptions = valuesOptions;
        this.reserved = void 0;
        if (values) {
          for (var keys = Object.keys(values), i = 0; i < keys.length; ++i)
            if (typeof values[keys[i]] === "number")
              this.valuesById[this.values[keys[i]] = values[keys[i]]] = keys[i];
        }
      }
      __name(Enum, "Enum");
      Enum.fromJSON = /* @__PURE__ */ __name(function fromJSON(name, json) {
        var enm = new Enum(name, json.values, json.options, json.comment, json.comments);
        enm.reserved = json.reserved;
        return enm;
      }, "fromJSON");
      Enum.prototype.toJSON = /* @__PURE__ */ __name(function toJSON(toJSONOptions) {
        var keepComments = toJSONOptions ? Boolean(toJSONOptions.keepComments) : false;
        return util.toObject([
          "options",
          this.options,
          "valuesOptions",
          this.valuesOptions,
          "values",
          this.values,
          "reserved",
          this.reserved && this.reserved.length ? this.reserved : void 0,
          "comment",
          keepComments ? this.comment : void 0,
          "comments",
          keepComments ? this.comments : void 0
        ]);
      }, "toJSON");
      Enum.prototype.add = /* @__PURE__ */ __name(function add2(name, id, comment, options) {
        if (!util.isString(name))
          throw TypeError("name must be a string");
        if (!util.isInteger(id))
          throw TypeError("id must be an integer");
        if (this.values[name] !== void 0)
          throw Error("duplicate name '" + name + "' in " + this);
        if (this.isReservedId(id))
          throw Error("id " + id + " is reserved in " + this);
        if (this.isReservedName(name))
          throw Error("name '" + name + "' is reserved in " + this);
        if (this.valuesById[id] !== void 0) {
          if (!(this.options && this.options.allow_alias))
            throw Error("duplicate id " + id + " in " + this);
          this.values[name] = id;
        } else
          this.valuesById[this.values[name] = id] = name;
        if (options) {
          if (this.valuesOptions === void 0)
            this.valuesOptions = {};
          this.valuesOptions[name] = options || null;
        }
        this.comments[name] = comment || null;
        return this;
      }, "add");
      Enum.prototype.remove = /* @__PURE__ */ __name(function remove2(name) {
        if (!util.isString(name))
          throw TypeError("name must be a string");
        var val2 = this.values[name];
        if (val2 == null)
          throw Error("name '" + name + "' does not exist in " + this);
        delete this.valuesById[val2];
        delete this.values[name];
        delete this.comments[name];
        if (this.valuesOptions)
          delete this.valuesOptions[name];
        return this;
      }, "remove");
      Enum.prototype.isReservedId = /* @__PURE__ */ __name(function isReservedId(id) {
        return Namespace.isReservedId(this.reserved, id);
      }, "isReservedId");
      Enum.prototype.isReservedName = /* @__PURE__ */ __name(function isReservedName(name) {
        return Namespace.isReservedName(this.reserved, name);
      }, "isReservedName");
    }
  });

  // node_modules/protobufjs/src/encoder.js
  var require_encoder = __commonJS({
    "node_modules/protobufjs/src/encoder.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = encoder;
      var Enum = require_enum(), types = require_types(), util = require_util();
      function genTypePartial(gen, field, fieldIndex, ref) {
        return field.resolvedType.group ? gen("types[%i].encode(%s,w.uint32(%i)).uint32(%i)", fieldIndex, ref, (field.id << 3 | 3) >>> 0, (field.id << 3 | 4) >>> 0) : gen("types[%i].encode(%s,w.uint32(%i).fork()).ldelim()", fieldIndex, ref, (field.id << 3 | 2) >>> 0);
      }
      __name(genTypePartial, "genTypePartial");
      function encoder(mtype) {
        var gen = util.codegen(["m", "w"], mtype.name + "$encode")("if(!w)")("w=Writer.create()");
        var i, ref;
        var fields = (
          /* initializes */
          mtype.fieldsArray.slice().sort(util.compareFieldsById)
        );
        for (var i = 0; i < fields.length; ++i) {
          var field = fields[i].resolve(), index2 = mtype._fieldsArray.indexOf(field), type = field.resolvedType instanceof Enum ? "int32" : field.type, wireType = types.basic[type];
          ref = "m" + util.safeProp(field.name);
          if (field.map) {
            gen("if(%s!=null&&Object.hasOwnProperty.call(m,%j)){", ref, field.name)("for(var ks=Object.keys(%s),i=0;i<ks.length;++i){", ref)("w.uint32(%i).fork().uint32(%i).%s(ks[i])", (field.id << 3 | 2) >>> 0, 8 | types.mapKey[field.keyType], field.keyType);
            if (wireType === void 0) gen("types[%i].encode(%s[ks[i]],w.uint32(18).fork()).ldelim().ldelim()", index2, ref);
            else gen(".uint32(%i).%s(%s[ks[i]]).ldelim()", 16 | wireType, type, ref);
            gen("}")("}");
          } else if (field.repeated) {
            gen("if(%s!=null&&%s.length){", ref, ref);
            if (field.packed && types.packed[type] !== void 0) {
              gen("w.uint32(%i).fork()", (field.id << 3 | 2) >>> 0)("for(var i=0;i<%s.length;++i)", ref)("w.%s(%s[i])", type, ref)("w.ldelim()");
            } else {
              gen("for(var i=0;i<%s.length;++i)", ref);
              if (wireType === void 0)
                genTypePartial(gen, field, index2, ref + "[i]");
              else gen("w.uint32(%i).%s(%s[i])", (field.id << 3 | wireType) >>> 0, type, ref);
            }
            gen("}");
          } else {
            if (field.optional) gen("if(%s!=null&&Object.hasOwnProperty.call(m,%j))", ref, field.name);
            if (wireType === void 0)
              genTypePartial(gen, field, index2, ref);
            else gen("w.uint32(%i).%s(%s)", (field.id << 3 | wireType) >>> 0, type, ref);
          }
        }
        return gen("return w");
      }
      __name(encoder, "encoder");
    }
  });

  // node_modules/protobufjs/src/index-light.js
  var require_index_light = __commonJS({
    "node_modules/protobufjs/src/index-light.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var protobuf = module2.exports = require_index_minimal();
      protobuf.build = "light";
      function load2(filename, root2, callback) {
        if (typeof root2 === "function") {
          callback = root2;
          root2 = new protobuf.Root();
        } else if (!root2)
          root2 = new protobuf.Root();
        return root2.load(filename, callback);
      }
      __name(load2, "load");
      protobuf.load = load2;
      function loadSync(filename, root2) {
        if (!root2)
          root2 = new protobuf.Root();
        return root2.loadSync(filename);
      }
      __name(loadSync, "loadSync");
      protobuf.loadSync = loadSync;
      protobuf.encoder = require_encoder();
      protobuf.decoder = require_decoder();
      protobuf.verifier = require_verifier();
      protobuf.converter = require_converter();
      protobuf.ReflectionObject = require_object();
      protobuf.Namespace = require_namespace();
      protobuf.Root = require_root();
      protobuf.Enum = require_enum();
      protobuf.Type = require_type();
      protobuf.Field = require_field();
      protobuf.OneOf = require_oneof();
      protobuf.MapField = require_mapfield();
      protobuf.Service = require_service2();
      protobuf.Method = require_method();
      protobuf.Message = require_message();
      protobuf.wrappers = require_wrappers();
      protobuf.types = require_types();
      protobuf.util = require_util();
      protobuf.ReflectionObject._configure(protobuf.Root);
      protobuf.Namespace._configure(protobuf.Type, protobuf.Service, protobuf.Enum);
      protobuf.Root._configure(protobuf.Type);
      protobuf.Field._configure(protobuf.Type);
    }
  });

  // node_modules/protobufjs/src/tokenize.js
  var require_tokenize = __commonJS({
    "node_modules/protobufjs/src/tokenize.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = tokenize;
      var delimRe = /[\s{}=;:[\],'"()<>]/g, stringDoubleRe = /(?:"([^"\\]*(?:\\.[^"\\]*)*)")/g, stringSingleRe = /(?:'([^'\\]*(?:\\.[^'\\]*)*)')/g;
      var setCommentRe = /^ *[*/]+ */, setCommentAltRe = /^\s*\*?\/*/, setCommentSplitRe = /\n/g, whitespaceRe = /\s/, unescapeRe = /\\(.?)/g;
      var unescapeMap = {
        "0": "\0",
        "r": "\r",
        "n": "\n",
        "t": "	"
      };
      function unescape(str) {
        return str.replace(unescapeRe, function($0, $1) {
          switch ($1) {
            case "\\":
            case "":
              return $1;
            default:
              return unescapeMap[$1] || "";
          }
        });
      }
      __name(unescape, "unescape");
      tokenize.unescape = unescape;
      function tokenize(source, alternateCommentMode) {
        source = source.toString();
        var offset = 0, length = source.length, line = 1, lastCommentLine = 0, comments = {};
        var stack = [];
        var stringDelim = null;
        function illegal(subject) {
          return Error("illegal " + subject + " (line " + line + ")");
        }
        __name(illegal, "illegal");
        function readString() {
          var re = stringDelim === "'" ? stringSingleRe : stringDoubleRe;
          re.lastIndex = offset - 1;
          var match = re.exec(source);
          if (!match)
            throw illegal("string");
          offset = re.lastIndex;
          push(stringDelim);
          stringDelim = null;
          return unescape(match[1]);
        }
        __name(readString, "readString");
        function charAt(pos) {
          return source.charAt(pos);
        }
        __name(charAt, "charAt");
        function setComment(start, end2, isLeading) {
          var comment = {
            type: source.charAt(start++),
            lineEmpty: false,
            leading: isLeading
          };
          var lookback;
          if (alternateCommentMode) {
            lookback = 2;
          } else {
            lookback = 3;
          }
          var commentOffset = start - lookback, c;
          do {
            if (--commentOffset < 0 || (c = source.charAt(commentOffset)) === "\n") {
              comment.lineEmpty = true;
              break;
            }
          } while (c === " " || c === "	");
          var lines = source.substring(start, end2).split(setCommentSplitRe);
          for (var i = 0; i < lines.length; ++i)
            lines[i] = lines[i].replace(alternateCommentMode ? setCommentAltRe : setCommentRe, "").trim();
          comment.text = lines.join("\n").trim();
          comments[line] = comment;
          lastCommentLine = line;
        }
        __name(setComment, "setComment");
        function isDoubleSlashCommentLine(startOffset) {
          var endOffset = findEndOfLine(startOffset);
          var lineText = source.substring(startOffset, endOffset);
          var isComment2 = /^\s*\/\//.test(lineText);
          return isComment2;
        }
        __name(isDoubleSlashCommentLine, "isDoubleSlashCommentLine");
        function findEndOfLine(cursor) {
          var endOffset = cursor;
          while (endOffset < length && charAt(endOffset) !== "\n") {
            endOffset++;
          }
          return endOffset;
        }
        __name(findEndOfLine, "findEndOfLine");
        function next2() {
          if (stack.length > 0)
            return stack.shift();
          if (stringDelim)
            return readString();
          var repeat, prev2, curr, start, isDoc, isLeadingComment = offset === 0;
          do {
            if (offset === length)
              return null;
            repeat = false;
            while (whitespaceRe.test(curr = charAt(offset))) {
              if (curr === "\n") {
                isLeadingComment = true;
                ++line;
              }
              if (++offset === length)
                return null;
            }
            if (charAt(offset) === "/") {
              if (++offset === length) {
                throw illegal("comment");
              }
              if (charAt(offset) === "/") {
                if (!alternateCommentMode) {
                  isDoc = charAt(start = offset + 1) === "/";
                  while (charAt(++offset) !== "\n") {
                    if (offset === length) {
                      return null;
                    }
                  }
                  ++offset;
                  if (isDoc) {
                    setComment(start, offset - 1, isLeadingComment);
                    isLeadingComment = true;
                  }
                  ++line;
                  repeat = true;
                } else {
                  start = offset;
                  isDoc = false;
                  if (isDoubleSlashCommentLine(offset - 1)) {
                    isDoc = true;
                    do {
                      offset = findEndOfLine(offset);
                      if (offset === length) {
                        break;
                      }
                      offset++;
                      if (!isLeadingComment) {
                        break;
                      }
                    } while (isDoubleSlashCommentLine(offset));
                  } else {
                    offset = Math.min(length, findEndOfLine(offset) + 1);
                  }
                  if (isDoc) {
                    setComment(start, offset, isLeadingComment);
                    isLeadingComment = true;
                  }
                  line++;
                  repeat = true;
                }
              } else if ((curr = charAt(offset)) === "*") {
                start = offset + 1;
                isDoc = alternateCommentMode || charAt(start) === "*";
                do {
                  if (curr === "\n") {
                    ++line;
                  }
                  if (++offset === length) {
                    throw illegal("comment");
                  }
                  prev2 = curr;
                  curr = charAt(offset);
                } while (prev2 !== "*" || curr !== "/");
                ++offset;
                if (isDoc) {
                  setComment(start, offset - 2, isLeadingComment);
                  isLeadingComment = true;
                }
                repeat = true;
              } else {
                return "/";
              }
            }
          } while (repeat);
          var end2 = offset;
          delimRe.lastIndex = 0;
          var delim = delimRe.test(charAt(end2++));
          if (!delim)
            while (end2 < length && !delimRe.test(charAt(end2)))
              ++end2;
          var token = source.substring(offset, offset = end2);
          if (token === '"' || token === "'")
            stringDelim = token;
          return token;
        }
        __name(next2, "next");
        function push(token) {
          stack.push(token);
        }
        __name(push, "push");
        function peek() {
          if (!stack.length) {
            var token = next2();
            if (token === null)
              return null;
            push(token);
          }
          return stack[0];
        }
        __name(peek, "peek");
        function skip(expected, optional) {
          var actual = peek(), equals = actual === expected;
          if (equals) {
            next2();
            return true;
          }
          if (!optional)
            throw illegal("token '" + actual + "', '" + expected + "' expected");
          return false;
        }
        __name(skip, "skip");
        function cmnt(trailingLine) {
          var ret = null;
          var comment;
          if (trailingLine === void 0) {
            comment = comments[line - 1];
            delete comments[line - 1];
            if (comment && (alternateCommentMode || comment.type === "*" || comment.lineEmpty)) {
              ret = comment.leading ? comment.text : null;
            }
          } else {
            if (lastCommentLine < trailingLine) {
              peek();
            }
            comment = comments[trailingLine];
            delete comments[trailingLine];
            if (comment && !comment.lineEmpty && (alternateCommentMode || comment.type === "/")) {
              ret = comment.leading ? null : comment.text;
            }
          }
          return ret;
        }
        __name(cmnt, "cmnt");
        return Object.defineProperty({
          next: next2,
          peek,
          push,
          skip,
          cmnt
        }, "line", {
          get: /* @__PURE__ */ __name(function() {
            return line;
          }, "get")
        });
      }
      __name(tokenize, "tokenize");
    }
  });

  // node_modules/protobufjs/src/parse.js
  var require_parse2 = __commonJS({
    "node_modules/protobufjs/src/parse.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = parse5;
      parse5.filename = null;
      parse5.defaults = { keepCase: false };
      var tokenize = require_tokenize(), Root = require_root(), Type = require_type(), Field = require_field(), MapField = require_mapfield(), OneOf = require_oneof(), Enum = require_enum(), Service = require_service2(), Method = require_method(), types = require_types(), util = require_util();
      var base10Re = /^[1-9][0-9]*$/, base10NegRe = /^-?[1-9][0-9]*$/, base16Re = /^0[x][0-9a-fA-F]+$/, base16NegRe = /^-?0[x][0-9a-fA-F]+$/, base8Re = /^0[0-7]+$/, base8NegRe = /^-?0[0-7]+$/, numberRe = /^(?![eE])[0-9]*(?:\.[0-9]*)?(?:[eE][+-]?[0-9]+)?$/, nameRe = /^[a-zA-Z_][a-zA-Z_0-9]*$/, typeRefRe = /^(?:\.?[a-zA-Z_][a-zA-Z_0-9]*)(?:\.[a-zA-Z_][a-zA-Z_0-9]*)*$/, fqTypeRefRe = /^(?:\.[a-zA-Z_][a-zA-Z_0-9]*)+$/;
      function parse5(source, root2, options) {
        if (!(root2 instanceof Root)) {
          options = root2;
          root2 = new Root();
        }
        if (!options)
          options = parse5.defaults;
        var preferTrailingComment = options.preferTrailingComment || false;
        var tn = tokenize(source, options.alternateCommentMode || false), next2 = tn.next, push = tn.push, peek = tn.peek, skip = tn.skip, cmnt = tn.cmnt;
        var head = true, pkg, imports, weakImports, syntax, isProto3 = false;
        var ptr = root2;
        var applyCase = options.keepCase ? function(name) {
          return name;
        } : util.camelCase;
        function illegal(token2, name, insideTryCatch) {
          var filename = parse5.filename;
          if (!insideTryCatch)
            parse5.filename = null;
          return Error("illegal " + (name || "token") + " '" + token2 + "' (" + (filename ? filename + ", " : "") + "line " + tn.line + ")");
        }
        __name(illegal, "illegal");
        function readString() {
          var values = [], token2;
          do {
            if ((token2 = next2()) !== '"' && token2 !== "'")
              throw illegal(token2);
            values.push(next2());
            skip(token2);
            token2 = peek();
          } while (token2 === '"' || token2 === "'");
          return values.join("");
        }
        __name(readString, "readString");
        function readValue(acceptTypeRef) {
          var token2 = next2();
          switch (token2) {
            case "'":
            case '"':
              push(token2);
              return readString();
            case "true":
            case "TRUE":
              return true;
            case "false":
            case "FALSE":
              return false;
          }
          try {
            return parseNumber(
              token2,
              /* insideTryCatch */
              true
            );
          } catch (e2) {
            if (acceptTypeRef && typeRefRe.test(token2))
              return token2;
            throw illegal(token2, "value");
          }
        }
        __name(readValue, "readValue");
        function readRanges(target, acceptStrings) {
          var token2, start;
          do {
            if (acceptStrings && ((token2 = peek()) === '"' || token2 === "'"))
              target.push(readString());
            else
              target.push([start = parseId(next2()), skip("to", true) ? parseId(next2()) : start]);
          } while (skip(",", true));
          var dummy = { options: void 0 };
          dummy.setOption = function(name, value) {
            if (this.options === void 0) this.options = {};
            this.options[name] = value;
          };
          ifBlock(
            dummy,
            /* @__PURE__ */ __name(function parseRange_block(token3) {
              if (token3 === "option") {
                parseOption(dummy, token3);
                skip(";");
              } else
                throw illegal(token3);
            }, "parseRange_block"),
            /* @__PURE__ */ __name(function parseRange_line() {
              parseInlineOptions(dummy);
            }, "parseRange_line")
          );
        }
        __name(readRanges, "readRanges");
        function parseNumber(token2, insideTryCatch) {
          var sign = 1;
          if (token2.charAt(0) === "-") {
            sign = -1;
            token2 = token2.substring(1);
          }
          switch (token2) {
            case "inf":
            case "INF":
            case "Inf":
              return sign * Infinity;
            case "nan":
            case "NAN":
            case "Nan":
            case "NaN":
              return NaN;
            case "0":
              return 0;
          }
          if (base10Re.test(token2))
            return sign * parseInt(token2, 10);
          if (base16Re.test(token2))
            return sign * parseInt(token2, 16);
          if (base8Re.test(token2))
            return sign * parseInt(token2, 8);
          if (numberRe.test(token2))
            return sign * parseFloat(token2);
          throw illegal(token2, "number", insideTryCatch);
        }
        __name(parseNumber, "parseNumber");
        function parseId(token2, acceptNegative) {
          switch (token2) {
            case "max":
            case "MAX":
            case "Max":
              return 536870911;
            case "0":
              return 0;
          }
          if (!acceptNegative && token2.charAt(0) === "-")
            throw illegal(token2, "id");
          if (base10NegRe.test(token2))
            return parseInt(token2, 10);
          if (base16NegRe.test(token2))
            return parseInt(token2, 16);
          if (base8NegRe.test(token2))
            return parseInt(token2, 8);
          throw illegal(token2, "id");
        }
        __name(parseId, "parseId");
        function parsePackage() {
          if (pkg !== void 0)
            throw illegal("package");
          pkg = next2();
          if (!typeRefRe.test(pkg))
            throw illegal(pkg, "name");
          ptr = ptr.define(pkg);
          skip(";");
        }
        __name(parsePackage, "parsePackage");
        function parseImport() {
          var token2 = peek();
          var whichImports;
          switch (token2) {
            case "weak":
              whichImports = weakImports || (weakImports = []);
              next2();
              break;
            case "public":
              next2();
            // eslint-disable-next-line no-fallthrough
            default:
              whichImports = imports || (imports = []);
              break;
          }
          token2 = readString();
          skip(";");
          whichImports.push(token2);
        }
        __name(parseImport, "parseImport");
        function parseSyntax() {
          skip("=");
          syntax = readString();
          isProto3 = syntax === "proto3";
          if (!isProto3 && syntax !== "proto2")
            throw illegal(syntax, "syntax");
          root2.setOption("syntax", syntax);
          skip(";");
        }
        __name(parseSyntax, "parseSyntax");
        function parseCommon(parent2, token2) {
          switch (token2) {
            case "option":
              parseOption(parent2, token2);
              skip(";");
              return true;
            case "message":
              parseType(parent2, token2);
              return true;
            case "enum":
              parseEnum(parent2, token2);
              return true;
            case "service":
              parseService(parent2, token2);
              return true;
            case "extend":
              parseExtension(parent2, token2);
              return true;
          }
          return false;
        }
        __name(parseCommon, "parseCommon");
        function ifBlock(obj, fnIf, fnElse) {
          var trailingLine = tn.line;
          if (obj) {
            if (typeof obj.comment !== "string") {
              obj.comment = cmnt();
            }
            obj.filename = parse5.filename;
          }
          if (skip("{", true)) {
            var token2;
            while ((token2 = next2()) !== "}")
              fnIf(token2);
            skip(";", true);
          } else {
            if (fnElse)
              fnElse();
            skip(";");
            if (obj && (typeof obj.comment !== "string" || preferTrailingComment))
              obj.comment = cmnt(trailingLine) || obj.comment;
          }
        }
        __name(ifBlock, "ifBlock");
        function parseType(parent2, token2) {
          if (!nameRe.test(token2 = next2()))
            throw illegal(token2, "type name");
          var type = new Type(token2);
          ifBlock(type, /* @__PURE__ */ __name(function parseType_block(token3) {
            if (parseCommon(type, token3))
              return;
            switch (token3) {
              case "map":
                parseMapField(type, token3);
                break;
              case "required":
              case "repeated":
                parseField(type, token3);
                break;
              case "optional":
                if (isProto3) {
                  parseField(type, "proto3_optional");
                } else {
                  parseField(type, "optional");
                }
                break;
              case "oneof":
                parseOneOf(type, token3);
                break;
              case "extensions":
                readRanges(type.extensions || (type.extensions = []));
                break;
              case "reserved":
                readRanges(type.reserved || (type.reserved = []), true);
                break;
              default:
                if (!isProto3 || !typeRefRe.test(token3))
                  throw illegal(token3);
                push(token3);
                parseField(type, "optional");
                break;
            }
          }, "parseType_block"));
          parent2.add(type);
        }
        __name(parseType, "parseType");
        function parseField(parent2, rule, extend) {
          var type = next2();
          if (type === "group") {
            parseGroup(parent2, rule);
            return;
          }
          while (type.endsWith(".") || peek().startsWith(".")) {
            type += next2();
          }
          if (!typeRefRe.test(type))
            throw illegal(type, "type");
          var name = next2();
          if (!nameRe.test(name))
            throw illegal(name, "name");
          name = applyCase(name);
          skip("=");
          var field = new Field(name, parseId(next2()), type, rule, extend);
          ifBlock(field, /* @__PURE__ */ __name(function parseField_block(token2) {
            if (token2 === "option") {
              parseOption(field, token2);
              skip(";");
            } else
              throw illegal(token2);
          }, "parseField_block"), /* @__PURE__ */ __name(function parseField_line() {
            parseInlineOptions(field);
          }, "parseField_line"));
          if (rule === "proto3_optional") {
            var oneof = new OneOf("_" + name);
            field.setOption("proto3_optional", true);
            oneof.add(field);
            parent2.add(oneof);
          } else {
            parent2.add(field);
          }
          if (!isProto3 && field.repeated && (types.packed[type] !== void 0 || types.basic[type] === void 0))
            field.setOption(
              "packed",
              false,
              /* ifNotSet */
              true
            );
        }
        __name(parseField, "parseField");
        function parseGroup(parent2, rule) {
          var name = next2();
          if (!nameRe.test(name))
            throw illegal(name, "name");
          var fieldName = util.lcFirst(name);
          if (name === fieldName)
            name = util.ucFirst(name);
          skip("=");
          var id = parseId(next2());
          var type = new Type(name);
          type.group = true;
          var field = new Field(fieldName, id, name, rule);
          field.filename = parse5.filename;
          ifBlock(type, /* @__PURE__ */ __name(function parseGroup_block(token2) {
            switch (token2) {
              case "option":
                parseOption(type, token2);
                skip(";");
                break;
              case "required":
              case "repeated":
                parseField(type, token2);
                break;
              case "optional":
                if (isProto3) {
                  parseField(type, "proto3_optional");
                } else {
                  parseField(type, "optional");
                }
                break;
              case "message":
                parseType(type, token2);
                break;
              case "enum":
                parseEnum(type, token2);
                break;
              /* istanbul ignore next */
              default:
                throw illegal(token2);
            }
          }, "parseGroup_block"));
          parent2.add(type).add(field);
        }
        __name(parseGroup, "parseGroup");
        function parseMapField(parent2) {
          skip("<");
          var keyType = next2();
          if (types.mapKey[keyType] === void 0)
            throw illegal(keyType, "type");
          skip(",");
          var valueType = next2();
          if (!typeRefRe.test(valueType))
            throw illegal(valueType, "type");
          skip(">");
          var name = next2();
          if (!nameRe.test(name))
            throw illegal(name, "name");
          skip("=");
          var field = new MapField(applyCase(name), parseId(next2()), keyType, valueType);
          ifBlock(field, /* @__PURE__ */ __name(function parseMapField_block(token2) {
            if (token2 === "option") {
              parseOption(field, token2);
              skip(";");
            } else
              throw illegal(token2);
          }, "parseMapField_block"), /* @__PURE__ */ __name(function parseMapField_line() {
            parseInlineOptions(field);
          }, "parseMapField_line"));
          parent2.add(field);
        }
        __name(parseMapField, "parseMapField");
        function parseOneOf(parent2, token2) {
          if (!nameRe.test(token2 = next2()))
            throw illegal(token2, "name");
          var oneof = new OneOf(applyCase(token2));
          ifBlock(oneof, /* @__PURE__ */ __name(function parseOneOf_block(token3) {
            if (token3 === "option") {
              parseOption(oneof, token3);
              skip(";");
            } else {
              push(token3);
              parseField(oneof, "optional");
            }
          }, "parseOneOf_block"));
          parent2.add(oneof);
        }
        __name(parseOneOf, "parseOneOf");
        function parseEnum(parent2, token2) {
          if (!nameRe.test(token2 = next2()))
            throw illegal(token2, "name");
          var enm = new Enum(token2);
          ifBlock(enm, /* @__PURE__ */ __name(function parseEnum_block(token3) {
            switch (token3) {
              case "option":
                parseOption(enm, token3);
                skip(";");
                break;
              case "reserved":
                readRanges(enm.reserved || (enm.reserved = []), true);
                break;
              default:
                parseEnumValue(enm, token3);
            }
          }, "parseEnum_block"));
          parent2.add(enm);
        }
        __name(parseEnum, "parseEnum");
        function parseEnumValue(parent2, token2) {
          if (!nameRe.test(token2))
            throw illegal(token2, "name");
          skip("=");
          var value = parseId(next2(), true), dummy = {
            options: void 0
          };
          dummy.setOption = function(name, value2) {
            if (this.options === void 0)
              this.options = {};
            this.options[name] = value2;
          };
          ifBlock(dummy, /* @__PURE__ */ __name(function parseEnumValue_block(token3) {
            if (token3 === "option") {
              parseOption(dummy, token3);
              skip(";");
            } else
              throw illegal(token3);
          }, "parseEnumValue_block"), /* @__PURE__ */ __name(function parseEnumValue_line() {
            parseInlineOptions(dummy);
          }, "parseEnumValue_line"));
          parent2.add(token2, value, dummy.comment, dummy.options);
        }
        __name(parseEnumValue, "parseEnumValue");
        function parseOption(parent2, token2) {
          var isCustom = skip("(", true);
          if (!typeRefRe.test(token2 = next2()))
            throw illegal(token2, "name");
          var name = token2;
          var option = name;
          var propName;
          if (isCustom) {
            skip(")");
            name = "(" + name + ")";
            option = name;
            token2 = peek();
            if (fqTypeRefRe.test(token2)) {
              propName = token2.slice(1);
              name += token2;
              next2();
            }
          }
          skip("=");
          var optionValue = parseOptionValue(parent2, name);
          setParsedOption(parent2, option, optionValue, propName);
        }
        __name(parseOption, "parseOption");
        function parseOptionValue(parent2, name) {
          if (skip("{", true)) {
            var objectResult = {};
            while (!skip("}", true)) {
              if (!nameRe.test(token = next2())) {
                throw illegal(token, "name");
              }
              if (token === null) {
                throw illegal(token, "end of input");
              }
              var value;
              var propName = token;
              skip(":", true);
              if (peek() === "{")
                value = parseOptionValue(parent2, name + "." + token);
              else if (peek() === "[") {
                value = [];
                var lastValue;
                if (skip("[", true)) {
                  do {
                    lastValue = readValue(true);
                    value.push(lastValue);
                  } while (skip(",", true));
                  skip("]");
                  if (typeof lastValue !== "undefined") {
                    setOption(parent2, name + "." + token, lastValue);
                  }
                }
              } else {
                value = readValue(true);
                setOption(parent2, name + "." + token, value);
              }
              var prevValue = objectResult[propName];
              if (prevValue)
                value = [].concat(prevValue).concat(value);
              objectResult[propName] = value;
              skip(",", true);
              skip(";", true);
            }
            return objectResult;
          }
          var simpleValue = readValue(true);
          setOption(parent2, name, simpleValue);
          return simpleValue;
        }
        __name(parseOptionValue, "parseOptionValue");
        function setOption(parent2, name, value) {
          if (parent2.setOption)
            parent2.setOption(name, value);
        }
        __name(setOption, "setOption");
        function setParsedOption(parent2, name, value, propName) {
          if (parent2.setParsedOption)
            parent2.setParsedOption(name, value, propName);
        }
        __name(setParsedOption, "setParsedOption");
        function parseInlineOptions(parent2) {
          if (skip("[", true)) {
            do {
              parseOption(parent2, "option");
            } while (skip(",", true));
            skip("]");
          }
          return parent2;
        }
        __name(parseInlineOptions, "parseInlineOptions");
        function parseService(parent2, token2) {
          if (!nameRe.test(token2 = next2()))
            throw illegal(token2, "service name");
          var service = new Service(token2);
          ifBlock(service, /* @__PURE__ */ __name(function parseService_block(token3) {
            if (parseCommon(service, token3))
              return;
            if (token3 === "rpc")
              parseMethod(service, token3);
            else
              throw illegal(token3);
          }, "parseService_block"));
          parent2.add(service);
        }
        __name(parseService, "parseService");
        function parseMethod(parent2, token2) {
          var commentText = cmnt();
          var type = token2;
          if (!nameRe.test(token2 = next2()))
            throw illegal(token2, "name");
          var name = token2, requestType, requestStream, responseType, responseStream;
          skip("(");
          if (skip("stream", true))
            requestStream = true;
          if (!typeRefRe.test(token2 = next2()))
            throw illegal(token2);
          requestType = token2;
          skip(")");
          skip("returns");
          skip("(");
          if (skip("stream", true))
            responseStream = true;
          if (!typeRefRe.test(token2 = next2()))
            throw illegal(token2);
          responseType = token2;
          skip(")");
          var method = new Method(name, type, requestType, responseType, requestStream, responseStream);
          method.comment = commentText;
          ifBlock(method, /* @__PURE__ */ __name(function parseMethod_block(token3) {
            if (token3 === "option") {
              parseOption(method, token3);
              skip(";");
            } else
              throw illegal(token3);
          }, "parseMethod_block"));
          parent2.add(method);
        }
        __name(parseMethod, "parseMethod");
        function parseExtension(parent2, token2) {
          if (!typeRefRe.test(token2 = next2()))
            throw illegal(token2, "reference");
          var reference = token2;
          ifBlock(null, /* @__PURE__ */ __name(function parseExtension_block(token3) {
            switch (token3) {
              case "required":
              case "repeated":
                parseField(parent2, token3, reference);
                break;
              case "optional":
                if (isProto3) {
                  parseField(parent2, "proto3_optional", reference);
                } else {
                  parseField(parent2, "optional", reference);
                }
                break;
              default:
                if (!isProto3 || !typeRefRe.test(token3))
                  throw illegal(token3);
                push(token3);
                parseField(parent2, "optional", reference);
                break;
            }
          }, "parseExtension_block"));
        }
        __name(parseExtension, "parseExtension");
        var token;
        while ((token = next2()) !== null) {
          switch (token) {
            case "package":
              if (!head)
                throw illegal(token);
              parsePackage();
              break;
            case "import":
              if (!head)
                throw illegal(token);
              parseImport();
              break;
            case "syntax":
              if (!head)
                throw illegal(token);
              parseSyntax();
              break;
            case "option":
              parseOption(ptr, token);
              skip(";");
              break;
            default:
              if (parseCommon(ptr, token)) {
                head = false;
                continue;
              }
              throw illegal(token);
          }
        }
        parse5.filename = null;
        return {
          "package": pkg,
          "imports": imports,
          weakImports,
          syntax,
          root: root2
        };
      }
      __name(parse5, "parse");
    }
  });

  // node_modules/protobufjs/src/common.js
  var require_common = __commonJS({
    "node_modules/protobufjs/src/common.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = common;
      var commonRe = /\/|\./;
      function common(name, json) {
        if (!commonRe.test(name)) {
          name = "google/protobuf/" + name + ".proto";
          json = { nested: { google: { nested: { protobuf: { nested: json } } } } };
        }
        common[name] = json;
      }
      __name(common, "common");
      common("any", {
        /**
         * Properties of a google.protobuf.Any message.
         * @interface IAny
         * @type {Object}
         * @property {string} [typeUrl]
         * @property {Uint8Array} [bytes]
         * @memberof common
         */
        Any: {
          fields: {
            type_url: {
              type: "string",
              id: 1
            },
            value: {
              type: "bytes",
              id: 2
            }
          }
        }
      });
      var timeType;
      common("duration", {
        /**
         * Properties of a google.protobuf.Duration message.
         * @interface IDuration
         * @type {Object}
         * @property {number|Long} [seconds]
         * @property {number} [nanos]
         * @memberof common
         */
        Duration: timeType = {
          fields: {
            seconds: {
              type: "int64",
              id: 1
            },
            nanos: {
              type: "int32",
              id: 2
            }
          }
        }
      });
      common("timestamp", {
        /**
         * Properties of a google.protobuf.Timestamp message.
         * @interface ITimestamp
         * @type {Object}
         * @property {number|Long} [seconds]
         * @property {number} [nanos]
         * @memberof common
         */
        Timestamp: timeType
      });
      common("empty", {
        /**
         * Properties of a google.protobuf.Empty message.
         * @interface IEmpty
         * @memberof common
         */
        Empty: {
          fields: {}
        }
      });
      common("struct", {
        /**
         * Properties of a google.protobuf.Struct message.
         * @interface IStruct
         * @type {Object}
         * @property {Object.<string,IValue>} [fields]
         * @memberof common
         */
        Struct: {
          fields: {
            fields: {
              keyType: "string",
              type: "Value",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.Value message.
         * @interface IValue
         * @type {Object}
         * @property {string} [kind]
         * @property {0} [nullValue]
         * @property {number} [numberValue]
         * @property {string} [stringValue]
         * @property {boolean} [boolValue]
         * @property {IStruct} [structValue]
         * @property {IListValue} [listValue]
         * @memberof common
         */
        Value: {
          oneofs: {
            kind: {
              oneof: [
                "nullValue",
                "numberValue",
                "stringValue",
                "boolValue",
                "structValue",
                "listValue"
              ]
            }
          },
          fields: {
            nullValue: {
              type: "NullValue",
              id: 1
            },
            numberValue: {
              type: "double",
              id: 2
            },
            stringValue: {
              type: "string",
              id: 3
            },
            boolValue: {
              type: "bool",
              id: 4
            },
            structValue: {
              type: "Struct",
              id: 5
            },
            listValue: {
              type: "ListValue",
              id: 6
            }
          }
        },
        NullValue: {
          values: {
            NULL_VALUE: 0
          }
        },
        /**
         * Properties of a google.protobuf.ListValue message.
         * @interface IListValue
         * @type {Object}
         * @property {Array.<IValue>} [values]
         * @memberof common
         */
        ListValue: {
          fields: {
            values: {
              rule: "repeated",
              type: "Value",
              id: 1
            }
          }
        }
      });
      common("wrappers", {
        /**
         * Properties of a google.protobuf.DoubleValue message.
         * @interface IDoubleValue
         * @type {Object}
         * @property {number} [value]
         * @memberof common
         */
        DoubleValue: {
          fields: {
            value: {
              type: "double",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.FloatValue message.
         * @interface IFloatValue
         * @type {Object}
         * @property {number} [value]
         * @memberof common
         */
        FloatValue: {
          fields: {
            value: {
              type: "float",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.Int64Value message.
         * @interface IInt64Value
         * @type {Object}
         * @property {number|Long} [value]
         * @memberof common
         */
        Int64Value: {
          fields: {
            value: {
              type: "int64",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.UInt64Value message.
         * @interface IUInt64Value
         * @type {Object}
         * @property {number|Long} [value]
         * @memberof common
         */
        UInt64Value: {
          fields: {
            value: {
              type: "uint64",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.Int32Value message.
         * @interface IInt32Value
         * @type {Object}
         * @property {number} [value]
         * @memberof common
         */
        Int32Value: {
          fields: {
            value: {
              type: "int32",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.UInt32Value message.
         * @interface IUInt32Value
         * @type {Object}
         * @property {number} [value]
         * @memberof common
         */
        UInt32Value: {
          fields: {
            value: {
              type: "uint32",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.BoolValue message.
         * @interface IBoolValue
         * @type {Object}
         * @property {boolean} [value]
         * @memberof common
         */
        BoolValue: {
          fields: {
            value: {
              type: "bool",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.StringValue message.
         * @interface IStringValue
         * @type {Object}
         * @property {string} [value]
         * @memberof common
         */
        StringValue: {
          fields: {
            value: {
              type: "string",
              id: 1
            }
          }
        },
        /**
         * Properties of a google.protobuf.BytesValue message.
         * @interface IBytesValue
         * @type {Object}
         * @property {Uint8Array} [value]
         * @memberof common
         */
        BytesValue: {
          fields: {
            value: {
              type: "bytes",
              id: 1
            }
          }
        }
      });
      common("field_mask", {
        /**
         * Properties of a google.protobuf.FieldMask message.
         * @interface IDoubleValue
         * @type {Object}
         * @property {number} [value]
         * @memberof common
         */
        FieldMask: {
          fields: {
            paths: {
              rule: "repeated",
              type: "string",
              id: 1
            }
          }
        }
      });
      common.get = /* @__PURE__ */ __name(function get2(file) {
        return common[file] || null;
      }, "get");
    }
  });

  // node_modules/protobufjs/src/index.js
  var require_src = __commonJS({
    "node_modules/protobufjs/src/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      var protobuf = module2.exports = require_index_light();
      protobuf.build = "full";
      protobuf.tokenize = require_tokenize();
      protobuf.parse = require_parse2();
      protobuf.common = require_common();
      protobuf.Root._configure(protobuf.Type, protobuf.parse, protobuf.common);
    }
  });

  // node_modules/protobufjs/index.js
  var require_protobufjs = __commonJS({
    "node_modules/protobufjs/index.js"(exports4, module2) {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      module2.exports = require_src();
    }
  });

  // src/lib/fetch.ts
  async function fetchApi(url, init) {
    init = await makeInit(init);
    console.log(url, init);
    return await fetch(url, init);
  }
  var import_protobufjs, makeInit, fetchFile, fetchText, BYTE_MARK, fetchProto;
  var init_fetch = __esm({
    "src/lib/fetch.ts"() {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      import_protobufjs = __toESM(require_protobufjs(), 1);
      makeInit = /* @__PURE__ */ __name(async (init) => {
        const defaultHeaders = {
          "Connection": "keep-alive",
          "Accept": "*/*",
          "Accept-Language": "*",
          "Sec-Fetch-Mode": "cors",
          "Accept-Encoding": "gzip, deflate"
        };
        if (init?.headers) {
          if (init.headers instanceof Headers) {
            for (const [name, value] of Object.entries(defaultHeaders)) {
              if (!init.headers.get(name)) init.headers.set(name, value);
            }
          } else {
            init.headers = {
              ...defaultHeaders,
              ...init.headers
            };
          }
        } else {
          init = {
            ...init,
            headers: defaultHeaders
          };
        }
        return init;
      }, "makeInit");
      __name(fetchApi, "fetchApi");
      fetchFile = /* @__PURE__ */ __name(async function(url, init) {
        init = await makeInit(init);
        console.log(url, init);
        try {
          const res = await fetch(url, init);
          if (!res.ok) return "";
          const arrayBuffer = await res.arrayBuffer();
          return Buffer.from(arrayBuffer).toString("base64");
        } catch (e2) {
          return "";
        }
      }, "fetchFile");
      fetchText = /* @__PURE__ */ __name(async function(url, init, encoding) {
        init = await makeInit(init);
        console.log(url, init);
        try {
          const res = await fetch(url, init);
          if (!res.ok) return "";
          const arrayBuffer = await res.arrayBuffer();
          const decoder = new TextDecoder(encoding);
          return decoder.decode(arrayBuffer);
        } catch (e2) {
          return "";
        }
      }, "fetchText");
      BYTE_MARK = BigInt((1 << 8) - 1);
      fetchProto = /* @__PURE__ */ __name(async function(protoInit, url, init) {
        const protoRoot = (0, import_protobufjs.parse)(protoInit.proto).root;
        const RequestMessge = protoRoot.lookupType(protoInit.requestType);
        if (RequestMessge.verify(protoInit.requestData)) {
          throw new Error("Invalid Proto");
        }
        const encodedrequest = RequestMessge.encode(protoInit.requestData).finish();
        const requestLength = BigInt(encodedrequest.length);
        const headers = new Uint8Array(
          Array(5).fill(0).map((v, idx) => {
            if (idx === 0) return 0;
            return Number(requestLength >> BigInt(8 * (5 - idx - 1)) & BYTE_MARK);
          })
        );
        init = await makeInit(init);
        const bodyArray = new Uint8Array(headers.length + encodedrequest.length);
        bodyArray.set(headers, 0);
        bodyArray.set(encodedrequest, headers.length);
        return fetch(url, {
          method: "POST",
          ...init,
          body: bodyArray
        }).then((r2) => r2.arrayBuffer()).then((arr) => {
          const payload = new Uint8Array(arr);
          const length = Number(
            BigInt(payload[1] << 24) | BigInt(payload[2] << 16) | BigInt(payload[3] << 8) | BigInt(payload[4])
          );
          const ResponseMessage = protoRoot.lookupType(protoInit.responseType);
          return ResponseMessage.decode(payload.slice(5, 5 + length));
        });
      }, "fetchProto");
    }
  });

  // src/libs/fetch.ts
  var fetch_exports = {};
  __export(fetch_exports, {
    fetchApi: () => fetchApi,
    fetchFile: () => fetchFile,
    fetchProto: () => fetchProto,
    fetchText: () => fetchText
  });
  var init_fetch2 = __esm({
    "src/libs/fetch.ts"() {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      init_fetch();
    }
  });

  // src/types/filters.ts
  var FilterTypes;
  var init_filters = __esm({
    "src/types/filters.ts"() {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      FilterTypes = /* @__PURE__ */ ((FilterTypes2) => {
        FilterTypes2["TextInput"] = "Text";
        FilterTypes2["Picker"] = "Picker";
        FilterTypes2["CheckboxGroup"] = "Checkbox";
        FilterTypes2["Switch"] = "Switch";
        FilterTypes2["ExcludableCheckboxGroup"] = "XCheckbox";
        return FilterTypes2;
      })(FilterTypes || {});
    }
  });

  // src/libs/filterInputs.ts
  var filterInputs_exports = {};
  __export(filterInputs_exports, {
    FilterTypes: () => FilterTypes
  });
  var init_filterInputs = __esm({
    "src/libs/filterInputs.ts"() {
      "use strict";
      init_dirname();
      init_buffer2();
      init_process2();
      init_filters();
    }
  });

  // .js/plugins/english/earlynovel.js
  init_dirname();
  init_buffer2();
  init_process2();
  var e = function(e2, t2, a2, r2) {
    return new (a2 || (a2 = Promise))(function(n2, l2) {
      function i(e3) {
        try {
          u(r2.next(e3));
        } catch (e4) {
          l2(e4);
        }
      }
      __name(i, "i");
      function o(e3) {
        try {
          u(r2.throw(e3));
        } catch (e4) {
          l2(e4);
        }
      }
      __name(o, "o");
      function u(e3) {
        var t3;
        e3.done ? n2(e3.value) : (t3 = e3.value, t3 instanceof a2 ? t3 : new a2(function(e4) {
          e4(t3);
        })).then(i, o);
      }
      __name(u, "u");
      u((r2 = r2.apply(e2, t2 || [])).next());
    });
  }, t = function(e2, t2) {
    var a2, r2, n2, l2 = { label: 0, sent: /* @__PURE__ */ __name(function() {
      if (1 & n2[0]) throw n2[1];
      return n2[1];
    }, "sent"), trys: [], ops: [] }, i = Object.create(("function" == typeof Iterator ? Iterator : Object).prototype);
    return i.next = o(0), i.throw = o(1), i.return = o(2), "function" == typeof Symbol && (i[Symbol.iterator] = function() {
      return this;
    }), i;
    function o(o2) {
      return function(u) {
        return function(o3) {
          if (a2) throw new TypeError("Generator is already executing.");
          for (; i && (i = 0, o3[0] && (l2 = 0)), l2; ) try {
            if (a2 = 1, r2 && (n2 = 2 & o3[0] ? r2.return : o3[0] ? r2.throw || ((n2 = r2.return) && n2.call(r2), 0) : r2.next) && !(n2 = n2.call(r2, o3[1])).done) return n2;
            switch (r2 = 0, n2 && (o3 = [2 & o3[0], n2.value]), o3[0]) {
              case 0:
              case 1:
                n2 = o3;
                break;
              case 4:
                return l2.label++, { value: o3[1], done: false };
              case 5:
                l2.label++, r2 = o3[1], o3 = [0];
                continue;
              case 7:
                o3 = l2.ops.pop(), l2.trys.pop();
                continue;
              default:
                if (!(n2 = l2.trys, (n2 = n2.length > 0 && n2[n2.length - 1]) || 6 !== o3[0] && 2 !== o3[0])) {
                  l2 = 0;
                  continue;
                }
                if (3 === o3[0] && (!n2 || o3[1] > n2[0] && o3[1] < n2[3])) {
                  l2.label = o3[1];
                  break;
                }
                if (6 === o3[0] && l2.label < n2[1]) {
                  l2.label = n2[1], n2 = o3;
                  break;
                }
                if (n2 && l2.label < n2[2]) {
                  l2.label = n2[2], l2.ops.push(o3);
                  break;
                }
                n2[2] && l2.ops.pop(), l2.trys.pop();
                continue;
            }
            o3 = t2.call(e2, l2);
          } catch (e3) {
            o3 = [6, e3], r2 = 0;
          } finally {
            a2 = n2 = 0;
          }
          if (5 & o3[0]) throw o3[1];
          return { value: o3[0] ? o3[1] : void 0, done: true };
        }([o2, u]);
      };
    }
    __name(o, "o");
  };
  Object.defineProperty(exports, "__esModule", { value: true });
  var a = (init_browser(), __toCommonJS(browser_exports)), r = (init_fetch2(), __toCommonJS(fetch_exports)), n = (init_filterInputs(), __toCommonJS(filterInputs_exports)), l = function() {
    function l2() {
      this.id = "earlynovel", this.name = "Early Novel", this.version = "1.0.1", this.icon = "src/en/earlynovel/icon.png", this.site = "https://earlynovel.net/", this.filters = { order: { value: "/most-popular", label: "Order by", options: [{ label: "Latest Release", value: "/latest-release-novel" }, { label: "Hot Novel", value: "/hot-novel" }, { label: "Completed Novel", value: "/completed-novel" }, { label: "Most Popular", value: "/most-popular" }], type: n.FilterTypes.Picker }, genres: { value: "", label: "Genre", options: [{ label: "None", value: "" }, { label: "Action", value: "/genre/action-1" }, { label: "Adult", value: "/genre/adult-2" }, { label: "Adventure", value: "/genre/adventure-3" }, { label: "Comedy", value: "/genre/comedy-4" }, { label: "Drama", value: "/genre/drama-5" }, { label: "Ecchi", value: "/genre/ecchi-6" }, { label: "Fantasy", value: "/genre/fantasy-7" }, { label: "Gender Bender", value: "/genre/gender-bender-8" }, { label: "Harem", value: "/genre/harem-9" }, { label: "Historical", value: "/genre/historical-10" }, { label: "Horror", value: "/genre/horror-11" }, { label: "Josei", value: "/genre/josei-12" }, { label: "Martial Arts", value: "/genre/martial-arts-13" }, { label: "Mature", value: "/genre/mature-14" }, { label: "Mecha", value: "/genre/mecha-15" }, { label: "Mystery", value: "/genre/mystery-16" }, { label: "Psychological", value: "/genre/psychological-17" }, { label: "Romance", value: "/genre/romance-18" }, { label: "School Life", value: "/genre/school-life-19" }, { label: "Sci-fi", value: "/genre/sci-fi-20" }, { label: "Seinen", value: "/genre/seinen-21" }, { label: "Shoujo", value: "/genre/shoujo-22" }, { label: "Shoujo Ai", value: "/genre/shoujo-ai-23" }, { label: "Shounen", value: "/genre/shounen-24" }, { label: "Shounen Ai", value: "/genre/shounen-ai-25" }, { label: "Slice of Life", value: "/genre/slice-of-life-26" }, { label: "Smut", value: "/genre/smut-27" }, { label: "Sports", value: "/genre/sports-28" }, { label: "Supernatural", value: "/genre/supernatural-29" }, { label: "Tragedy", value: "/genre/tragedy-30" }, { label: "Wuxia", value: "/genre/wuxia-31" }, { label: "Xianxia", value: "/genre/xianxia-32" }, { label: "Xuanhuan", value: "/genre/xuanhuan-33" }, { label: "Yaoi", value: "/genre/yaoi-34" }, { label: "Yuri", value: "/genre/yuri-35" }, { label: "Video Games", value: "/genre/video-games-36" }, { label: "Magical Realism", value: "/genre/magical-realism-37" }], type: n.FilterTypes.Picker } };
    }
    __name(l2, "l");
    return l2.prototype.parseNovels = function(e2) {
      var t2 = [];
      return e2(".col-truyen-main > .list-truyen > .row").each(function(a2, r2) {
        var n2 = e2(r2).find("h3.truyen-title > a").attr("href"), l3 = e2(r2).find("h3.truyen-title > a").text(), i = e2(r2).find(".lazyimg").attr("data-image");
        n2 && t2.push({ path: n2, name: l3, cover: i });
      }), t2;
    }, l2.prototype.parseChapters = function(e2) {
      var t2 = [];
      return e2("ul.list-chapter > li").each(function(a2, r2) {
        var n2, l3 = e2(r2).find(".chapter-text").text().trim(), i = null === (n2 = e2(r2).find("a").attr("href")) || void 0 === n2 ? void 0 : n2.slice(1);
        i && t2.push({ name: l3, path: i });
      }), t2;
    }, l2.prototype.popularNovels = function(n2, l3) {
      return e(this, arguments, void 0, function(e2, n3) {
        var l4, i, o, u = n3.filters;
        return t(this, function(t2) {
          switch (t2.label) {
            case 0:
              return l4 = this.site, u.genres.value.length ? l4 += u.genres.value : l4 += u.order.value, l4 += "?page=".concat(e2), [4, (0, r.fetchApi)(l4).then(function(e3) {
                return e3.text();
              })];
            case 1:
              return i = t2.sent(), o = (0, a.load)(i), [2, this.parseNovels(o)];
          }
        });
      });
    }, l2.prototype.parseNovel = function(n2) {
      return e(this, void 0, void 0, function() {
        var e2, l3, i, o, u, s, c;
        return t(this, function(t2) {
          switch (t2.label) {
            case 0:
              return [4, (0, r.fetchApi)(this.site + n2)];
            case 1:
              return [4, t2.sent().text()];
            case 2:
              return e2 = t2.sent(), (l3 = (0, a.load)(e2))(".glyphicon-menu-right").closest("li").remove(), i = l3(".page-nav").prev().find("a"), o = null === (c = i.attr("title")) || void 0 === c ? void 0 : c.match(/(\d+)/), u = Number((null == o ? void 0 : o[1]) || "0"), s = { path: n2, name: l3(".book > img").attr("alt") || "Untitled", cover: l3(".book > img").attr("src"), summary: l3(".desc-text").text().trim(), chapters: [], totalPages: u }, l3(".info > div > h3").each(function() {
                var e3 = l3(this).text(), t3 = l3(this).siblings().map(function(e4, t4) {
                  return l3(t4).text();
                }).toArray().join(",");
                switch (e3) {
                  case "Author:":
                    s.author = t3;
                    break;
                  case "Status:":
                    s.status = t3;
                    break;
                  case "Genre:":
                    s.genres = t3;
                }
              }), s.chapters = this.parseChapters(l3), [2, s];
          }
        });
      });
    }, l2.prototype.parsePage = function(n2, l3) {
      return e(this, void 0, void 0, function() {
        var e2, i, o;
        return t(this, function(t2) {
          switch (t2.label) {
            case 0:
              return e2 = this.site + n2 + "?page=" + l3, [4, (0, r.fetchApi)(e2).then(function(e3) {
                return e3.text();
              })];
            case 1:
              return i = t2.sent(), o = (0, a.load)(i), [2, { chapters: this.parseChapters(o) }];
          }
        });
      });
    }, l2.prototype.parseChapter = function(n2) {
      return e(this, void 0, void 0, function() {
        var e2, l3;
        return t(this, function(t2) {
          switch (t2.label) {
            case 0:
              return [4, (0, r.fetchApi)(this.site + n2)];
            case 1:
              return [4, t2.sent().text()];
            case 2:
              return e2 = t2.sent(), l3 = (0, a.load)(e2), [2, l3("#chapter-c").html() || ""];
          }
        });
      });
    }, l2.prototype.searchNovels = function(n2) {
      return e(this, void 0, void 0, function() {
        var e2, l3, i;
        return t(this, function(t2) {
          switch (t2.label) {
            case 0:
              return e2 = "".concat(this.site, "search?keyword=").concat(encodeURIComponent(n2)), [4, (0, r.fetchApi)(e2)];
            case 1:
              return [4, t2.sent().text()];
            case 2:
              return l3 = t2.sent(), i = (0, a.load)(l3), [2, this.parseNovels(i)];
          }
        });
      });
    }, l2;
  }();
  exports.default = new l();
})();

// Export for compatibility
if (typeof module !== "undefined" && module.exports) { module.exports = this; }

