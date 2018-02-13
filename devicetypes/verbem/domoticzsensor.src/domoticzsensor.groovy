/**
 *  domoticzSensor
 *
 *  Copyright 2017 Martin Verbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "domoticzSensor", namespace: "verbem", author: "SmartThings") {
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Battery"
		capability "Actuator"
		capability "Refresh"
        capability "Signal Strength"
        capability "Relative Humidity Measurement"
		capability "Health Check"
		capability "Illuminance Measurement"
		capability "Power Meter"
        capability "Motion Sensor"
        capability "Thermostat"
        
        attribute "pressure", "number"
        attribute "powerToday", "string"
        }

	tiles(scale: 2) {
        
		standardTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/battery.png"
		}
        
		standardTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2, canChangeIcon: true) {
			state "temparature", label:'${currentValue} C', unit:"", icon:"st.Weather.weather2",
							backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]		
            state "Error", label:"Install Error", backgroundColor: "#bc2323"
        }

		standardTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label:'${currentValue}% humidity', unit:"", icon:"st.Weather.weather12"
		}

		standardTile("motion", "device.motion", inactiveLabel: false, width: 2, height: 2) {
			state "motion", label:'${currentValue}', unit:"", icon:""
		}
        
 		standardTile("power", "device.powerToday",  inactiveLabel: false, width: 4, height: 2) {
			state "powerToday", label:'${currentValue}', unit:"", icon:""
		}
        
 		standardTile("illuminance", "device.illuminance",  inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} Lux', unit:"", icon:""
		}
                
 		standardTile("pressure", "device.pressure",  inactiveLabel: false, width: 2, height: 2) {
			state "pressure", label:'${currentValue} hPa', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/barometer-icon-png-5.png"
		}

		standardTile("rssi", "device.rssi", inactiveLabel: false,  width: 2, height: 2) {
            state "rssi", label:'Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
        }

		standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false,  width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        carouselTile("graph", "device.image", width: 6, height: 4)
		
		main "temperature"
		details(["temperature", "humidity", "pressure", "power", "illuminance", "motion", "battery", "rssi", "refresh", "graph"])
	}
}

def refresh() {
	log.debug "Executing 'refresh'"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }

}


// gets the IDX address of the device
private getIDXAddress() {
	
    def idx = getDataValue("idx")
        
    if (!idx) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            idx = parts[2]
        } else {
            log.warn "Can't figure out idx for device: ${device.id}"
        }
    }

    //log.debug "Using IDX: $idx for device: ${device.id}"
    return idx
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {

	if (parent) {
        sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked"]), displayed: false)
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "temperature", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}