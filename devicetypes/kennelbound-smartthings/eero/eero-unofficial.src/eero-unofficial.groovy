/**
 * Metadata / UX
 */
metadata {
    definition(name: "eero Unofficial", namespace: "kennelbound-smartthings/eero-unofficial", author: "Kennelbound") {
        capability "Actuator"
        capability "Sensor"

        attribute "name", "string"
        attribute "version", "string"
        attribute "speedTestDateTime", "number"
        attribute "uploadSpeedMbps", "number"
        attribute "downloadSpeedMbps", "number"
        attribute "eeroCount", "number"
        attribute "connectionType", "string"
        attribute "connectionMode", "string"
        attribute "status", "string"
        attribute "wanip", "string"
        attribute "gatewayip", "string"
        attribute "dns1", "string"
        attribute "dns2", "string"
        attribute "dns3", "string"
        attribute "upnp", "boolean"
    }

    preferences {
        input name: 'eeroToken', type: 'text', title: 'eero token', required: true, displayDuringSetup: true
        input name: 'eeroNetworkId', type: 'text', title: 'eero network id', required: true, displayDuringSetup: true
        input name: 'enableDebugLogging', type: 'boolean', title: 'Enable Debug Logging', description: 'Show Debug Logging in the Live Logs?', required: false, displayDuringSetup: false
    }


    tiles(scale: 2) {
        valueTile("uploadSpeed", "device.uploadSpeedMbps", width: 3, height: 2) {
            state "uploadSpeedMbps", label: '${currentValue} Up Mbps'
        }
        valueTile("downloadSpeed", "device.downloadSpeedMbps", width: 3, height: 2) {
            state "downloadSpeedMbps", label: '${currentValue} Down Mbps'
        }
        valueTile("name", "device.name", width: 3, height: 2) {
            state "name", label: 'Network Name: ${currentValue}'
        }
        valueTile("status", "device.status", width: 3, height: 2) {
            state "status", label: 'Status: ${currentValue}'
        }
        valueTile("wanip", "device.wanip", width: 3, height: 2) {
            state "wanip", label: 'WAN: ${currentValue}'
        }
        valueTile("gatewayip", "device.gatewayip", width: 3, height: 2) {
            state "gatewayip", label: 'Gateway: ${currentValue}'
        }
        valueTile("dns1", "device.dns1", width: 3, height: 2) {
            state "dns1", label: 'DNS1: ${currentValue}'
        }
        valueTile("dns2", "device.dns2", width: 3, height: 2) {
            state "dns2", label: 'DNS2: ${currentValue}'
        }
        valueTile("dns3", "device.dns3", width: 3, height: 2) {
            state "dns3", label: 'DNS3: ${currentValue}'
        }
        valueTile("eeroCount", "device.eeroCount", width: 3, height: 2) {
            state "eeroCount", label: 'Units: ${currentValue}'
        }
        valueTile("upnp", "device.upnp", width: 3, height: 2) {
            state "upnp", label: 'UPNP: ${currentValue}'
        }
        standardTile("refresh", "device", inactiveLabel: false, decoration: "flat", width: 6, height: 1) {
            state "default", label: 'Refresh', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "downloadSpeed"
        details([
                "uploadSpeed",
                "downloadSpeed",
                "name",
                "status",
                "wanip",
                "gatewayip",
                "dns1",
                "dns2",
                "dns3",
                "eeroCount",
                "upnp",
                "refresh"
        ])
    }
}

/**
 * EERO Integration and Event Listeners
 */
/* You must define the following methods to use this:

w_http_client_base_uri('post' or 'get', path, body, callback, contentType, passthru)
w_http_client_update_token(response)
w_http_client_post_callback(data.get('callback'), response, data)
w_http_client_get_callback(data.get('callback'), response, data)
w_http_client_referrer(path, body, callback, contentType)

*/
include 'asynchttp_v1'

def w_http_post(path, body, callback, contentType, passthru = null) {
    log.trace("w_http_post(path:$path, body:$body, callback:$callback, contentType:$contentType, headers: $headers")
    String stringBody = body?.collect { k, v -> "$k=$v" }?.join("&")?.toString() ?: ""

    def params = [
            uri               : w_http_client_base_uri('post', path, body, callback, contentType, passthru),
            path              : path,
            body              : stringBody,
            headers           : w_http_get_headers(path, body, callback, contentType),
            requestContentType: "application/x-www-form-urlencoded",
            contentType       : contentType
    ]

    def data = [
            path       : path,
            passthru   : passthru,
            contentType: contentType,
            callback   : callback
    ] //Data for Async Command.  Params to retry, handler to handle, and retry count if needed

    try {
//        log.debug "Attempting to post: $data"
        asynchttp_v1.post('w_http_post_response', params, data)
        state.referer = "${params['uri']}$path"
    } catch (e) {
        log.error "Something unexpected went wrong in eeroCommandPost: ${e}"
    }//try / catch for asynchttpPost
}//async post command

def w_http_post_response(response, data) {
    log.trace "w_http_post_response:$response.status->$response.headers"
    w_http_client_update_token(response, data)
    w_http_client_post_callback(data.get('callback'), response, data)
}

def w_http_get(path, query, callback, contentType, passthru = null) {
    log.trace "w_http_get(path:$path, query:$query, contentType:$contentType, callback:$callback, headers: $headers)"

    def params = [
            uri               : w_http_client_base_uri('get', path, body, callback, contentType, passthru),
            path              : path,
            query             : query,
            headers           : w_http_get_headers(path, body, callback, contentType),
            requestContentType: contentType
    ]

    def data = [
            path       : path,
            passthru   : passthru,
            contentType: contentType,
            callback   : callback,
    ]

    try {
//        log.debug("Attempting to get: $data")
        asynchttp_v1.get('w_http_get_response', params, data)
        state.referer = "${params['uri']}$path"
    } catch (e) {
        log.error "Something unexpected went wrong in eeroCommandGet: ${e}: $e.stackTrace"
    }
}

def w_http_get_response(response, data) {
    log.trace "w_http_get_response:$response.status->$response.headers"
    w_http_client_update_token(response, data)
    w_http_client_get_callback(data.get('callback'), response, data)
}

def w_http_no_callback(response, data) {
    log.error "Couldn't find response for callback ${data.get('callback')}"
}

def w_http_default_token_cookie(response, data) {
    try {
        state.cookie = response?.headers?.'Set-Cookie'?.split(';')?.getAt(0) ?: state.cookie ?: state.cookie
        state.token = (response.data =~ /meta content="(.*?)" name="csrf-token"/)[0][1]
        state.tokenRefresh = now()
    } catch (Exception e) {
//        log.warn "Couldn't fetch cookie and token from response, $e"
    }
}

def w_http_get_default_headers(path, body, callback, contentType) {
    def headers = [
            "Cookie"       : state.cookie,
            "User-Agent"   : "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Connection"   : "keep-alive",
            "Cache-Control": "no-cache"
    ]
    if (state.token) {
        headers["Referer"] = state.referer ?: w_http_client_referrer(path, body, callback, contentType)
        headers["X-CSRF-TOKEN"] = state.token
    }
    return headers
}


String w_http_client_base_uri(method, path, body, callback, contentType, passthru) {
    'https://api-user.e2ro.com/2.2/'
}

String w_http_client_referrer(path, body, callback, contentType) {
    'https://api-user.e2ro.com/2.2/'
}

void w_http_client_update_token(response, data) {
    w_http_default_token_cookie(response, data)
    updateToken(response, data)
}

void w_http_client_post_callback(callback, response, data) {
    switch (callback) {
        case "loginTest": loginTest(response, data); break
        case "refreshSendCommand": refreshSendCommand(response, data); break
        case "refreshVerifyCommand": refreshVerifyCommand(response, data); break
        default: noCallback(response, data); break
    }

    log.info "POST Callback with callback: $callback, and data: $data"
}

void w_http_client_get_callback(callback, response, data) {
    switch (callback) {
        case "loginTest": loginTest(response, data); break
        case "refreshSendCommand": refreshSendCommand(response, data); break
        case "refreshVerifyCommand": refreshVerifyCommand(response, data); break
        default: noCallback(response, data); break
    }

    log.info "GET Callback with callback: $callback, and data: $data"
}

def w_http_get_headers(path, body, callback, contentType) {
    return w_http_get_default_headers(path, body, callback, contentType)
}


def login(callback) {
    log_debug "login:$callback"
    w_http_post 'https://api-user.e2ro.com/2.2/login/refresh', null, "loginTest", "multipart/form-data", callback
}

def loginTest(response, data) {
    log_debug "loginTest:$response.status:${data.get('passthru')}"
    if (response.status == 200 && !response.hasError()) {
        // logged in, continue to passthru method
        log_debug "already logged in, calling passthru callback handler: ${data.get('passthru')}:${data.get('callback')}"
        w_http_client_get_callback(data.passthru, response, data)
    } else {
        if (!eeroToken) {
            log.error "EERO is not logged in, please follow instructions to get code", data.get('passthru')
        } else {
            state.cookie = "s=${eeroToken}; Path=/; HttpOnly; Domain=api-user.e2ro.com"
            state.token = null
            log_debug "Assigning the eeroToken we know, and retrying"
            login(data.get('passthru'))
        }
    }
}

def refresh() {
    state.retry_count = 0
    sendEvent name: 'updown', value: 'refreshing'
    executeRefresh()
}

def autoRefresh() {
    state.retry_count = 0
    executeRefresh()
}

def executeRefresh() {
    log_debug "refresh:$state.retry_count"
    if (state.retry_count <= MAX_REFRESH_RETRIES) {
        state.retry_count = state.retry_count + 1
        login 'refreshSendCommand'
    } else {
        log.error "Too many attempts to retry refresh"
    }
}

def refreshSendCommand(response, data) {
    log_debug "refreshSendCommand:$response.status"
    w_http_get("networks/$eeroNetworkId", null, "refreshVerifyCommand", "text/json")
}


def refreshVerifyCommand(response, data) {
    if (response.status != 200) {
        log.error "couldn't determine lock status after refreshing, $e"
        sendEvent name: 'updown', value: 'unknown'
        return
    }
    // valid response, get the json out of the response object
    try {
        def json = response.json.data
        log_debug "JSON:$json"
        sendEvent name: 'name', value: json.name
        sendEvent name: 'status', value: json.status
        sendEvent name: 'gatewayip', value: json.gateway_ip
        sendEvent name: 'wanip', value: json.wan_ip
        sendEvent name: 'connectionMode', value: json.connection.mode
        sendEvent name: 'eeroCount', value: json.eeros.count
        sendEvent name: 'uploadSpeedMbps', value: json.speed.up.value
        sendEvent name: 'downloadSpeedMbps', value: json.speed.down.value
        sendEvent name: 'speedTestDateTime', value: json.speed.date
        sendEvent name: 'dns1', value: json.dns?.parent?.ips[0]
        sendEvent name: 'dns2', value: json.dns?.parent?.ips[1]
        sendEvent name: 'dns3', value: json.dns?.parent?.ips[2]


    } catch (e) {
        log_debug "Can't update json"
    }
    log_debug "updating state from json: $json"
}

void updateToken(response, data) {
//    try {
//        state.token = (response.data =~ /meta content="(.*?)" name="csrf-token"/)[0][1]
//        state.tokenRefresh = now()
//    } catch (Exception e) {
//        log_debug "WARN: Couldn't fetch cookie and token from response, $e"
//    }
}


/**
 * Health and Lifecycle
 */
def ping() {
    refresh()
}

// Standard Hooks
def installed() {
    log.trace "installed()"
    initialize()
}

def updated() {
    log.trace "updated()"
    sendEvent(name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "cloud", scheme: "untracked"])
    refresh()
}

def initialize() {
    log.trace "initialize()"
    sendEvent(name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "cloud", scheme: "untracked"])
}

def unknown() {
    log.trace "unknown"
}


/**
 * Utility Functions and Constants
 */
boolean getIS_DEBUG_LOGGING_ENABLED() {
    return enableDebugLogging
}

void log_debug(...args) {
    if(IS_DEBUG_LOGGING_ENABLED) {
        log.debug(args)
    }
}
// Constants must be expressed as methods, by prefacing with get we can use it like a normal static constant
int getLOCK_REFRESH_WAIT() {
    2 // in seconds seconds
}

int getMAX_REFRESH_RETRIES() {
    5 // retries when the bolt state hasn't changed
}
