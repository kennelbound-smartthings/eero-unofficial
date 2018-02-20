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