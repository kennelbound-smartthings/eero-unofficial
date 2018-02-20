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
