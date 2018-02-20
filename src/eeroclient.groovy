__HTTP__

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
