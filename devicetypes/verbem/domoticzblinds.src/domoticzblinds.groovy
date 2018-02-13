/**
 *  domoticzBlinds
 *
 *  Copyright 2016 Martin Verbeek
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
 *  2.1 2016-11-21 Rework of setColor 
 *	2.2 2016-12-01 added calibration of the closing time, now you can use setlevel or ask alexa to dim to a percentage
 *	3.0 2016-12-24 cleanup of DTH statuses
 *  3.1 2017-05-10 Adding end of day scheduling for blinds as an offset to sunset, these are set in the Smart Screens app. 
 *  3.2 2017-07-12 Adding HC and parent check
 *	4.0 2018-02-12 Add windowShade capability
 */
import groovy.time.TimeCategory 
import groovy.time.TimeDuration

preferences {
    input(name:"stopSupported", type:"bool", title: "Stop command supported?", description:"Does your blind use the STOP command to halt the blind. NOT to be confused with the Somfy Stop/My command!", defaultValue:false)
}   
metadata {
	definition (name: "domoticzBlinds", namespace: "verbem", author: "Martin Verbeek") {
    
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Switch Level"
        capability "Refresh"
        capability "Polling"
        capability "Door Control"
        capability "Signal Strength"
		capability "Health Check"
        capability "Window Shade"

        // custom attributes
        attribute "networkId", "string"
        attribute "calibrationInProgress", "string" 
        attribute "startCalibrationTime", "number"
        attribute "endCalibrationTime", "number"
        attribute "blindClosingTime", "number"

        attribute "windBearing", "string"
        attribute "windSpeed", "number"
		attribute "cloudCover", "number"
		attribute "sunBearing", "string"
        attribute "somfySupported", "enum", [true, false]
        attribute "eodAction", "string"
        attribute "eodTime", "string"
        attribute "eodDone", "enum" , [true, false]


        // custom commands
        command "parse"     // (String "<attribute>:<value>[,<attribute>:<value>]")
        //command "close"
        //command "open"
        command "stop"
        command "setLevel"
        command "calibrate"
        command "generateEvent"
        command "eodRunOnce"

    }

    tiles (scale: 2) {
	    multiAttributeTile(name:"richDomoticzBlind", type:"generic",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
        	tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", 				label:"Open", 			backgroundColor:"#e86d13", nextState:"closing", action:"close"
                attributeState "opening", 			label:"Opening", 		backgroundColor:"#e86d13", nextState:""
				attributeState "partially open", 	label:"Partly Open", 	backgroundColor:"#11A81C", action:"open"               
                attributeState "closed", 			label:"Closed",  		backgroundColor:"#00a0dc", nextState:"opening", action:"open"
                attributeState "closing", 			label:"Closing",  		backgroundColor:"#00a0dc", nextState:""
				attributeState "Error", 			label:"Install Error", 	backgroundColor:"#bc2323"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL", range:"0..16") {
            	attributeState "level", action:"setLevel" 
            }
        }
 
        
        standardTile("Up", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Up', icon:"st.doors.garage.garage-opening",
                action:"open"
        }

        standardTile("Stop", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Stop', icon:"st.doors.garage.garage-open",
                action:"stop"
        }

        standardTile("Down", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Down', icon:"st.doors.garage.garage-closing",
                action:"close"
        }

		standardTile("Cal", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Calibrate', icon:"st.doors.garage.garage-closing",
                action:"calibrate"
        }
/*
 		standardTile("windBearing", "device.windBearing",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "windBearing", label:'${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/windBearing.png"            
        }

 		standardTile("windSpeed", "device.windSpeed",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "windSpeed", label:'${currentValue} km/h', unit:"km/h", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/windSpeed.png",            
							backgroundColors:[
							// km/h -> bft
							[value: 0, color: "#bef9b8"], 	//bft0 
							[value: 1, color: "#a8f99f"], 	//bft1 
							[value: 6, color: "#90f984"],	//bft2 
							[value: 12, color: "#72fc62"],	//bft3 
							[value: 20, color: "#4efc3a"],	//bft4 
							[value: 29, color: "#1efc05"],	//bft5 
							[value: 39, color: "#f6f7e8"],	//bft6 
							[value: 50, color: "#f1f7a5"],	//bft7
							[value: 62, color: "#fafc74"],	//bft8 
							[value: 75, color: "#f9fc20"],	//bft9 
							[value: 89, color: "#f7ae60"],	//bft10 
							[value: 103, color: "#fc8a11"],	//bft11 
							[value: 117, color: "#f9260e"]	//bft12 
							]        
        }

 		standardTile("sunBearing", "device.sunBearing",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "sunBearing", label:'${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/sunBearing.png"            
        }

 		standardTile("cloudCover", "device.cloudCover",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "cloudCover", label:'${currentValue}%', unit:"%", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/cloudCover.png"            
        }
		
		standardTile("eodAction", "device.eodAction", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "eodAction", label:'${currentValue} EoD Action', unit:""
		}

		standardTile("eodTime", "device.eodTime", inactiveLabel: false, width: 4, height: 1, decoration:"flat") {
			state "eodTime", label:'${currentValue}', unit:""
		}

		standardTile("eodDone", "device.eodDone", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "eodDone", label:'${currentValue}', unit:""
		}
*/
        standardTile("Refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "refresh", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
		standardTile("rssi", "device.rssi", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "rssi", label:'Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
		}
        
		standardTile("somfy", "device.somfySupported", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "somfySupported", label:'${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/Somfy.png"
		}
        
		childDeviceTile("windBearing", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "windBearing")   
        childDeviceTile("windSpeed", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "windSpeed")
        childDeviceTile("sunBearing", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "sunBearing")
        childDeviceTile("cloudCover", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "cloudCover")
        
        main(["richDomoticzBlind"])
	    details(["richDomoticzBlind", "Up", "Stop", "Down", "Cal", "rssi", "Refresh", "windBearing", "windSpeed", "sunBearing", "cloudCover"])

    }    
}

// parse events into attributes
def parse(String message) {
    log.debug "parse(${message})"

    Map msg = stringToMap(message)
    if (msg?.size() == 0) {
        log.error "Parse- Invalid message: ${message}"
        return null
    }


    if (msg.containsKey("switch")) {
    	log.debug "Parse- value  ${msg.switch}"
        switch (msg.switch.toUpperCase()) {
        case "ON": close(); break
        case "STOPPED": stop(); break
        case "OFF": open(); break
        sendPush( "Parse- Invalid message: ${message}")
        return null
        }
    }
//    STATE()
    return null
}

// handle commands

def on() {
	log.debug "Close()"
    if (parent) {
		sendEvent(name:'windowShade', value:"closed" as String)
		parent.domoticz_on(getIDXAddress())
    }

}

def off() {
	log.debug "Open()"
    if (parent) {
		sendEvent(name:'windowShade', value:"open" as String)
        parent.domoticz_off(getIDXAddress())
    }
}

def close() {
	log.debug "Close()"
    if (parent) {
		sendEvent(name:'windowShade', value:"closed" as String)
        parent.domoticz_on(getIDXAddress())
    }
}

def refresh() {
	log.debug "Refresh()"
    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }
}

def poll() {
	log.debug "Poll()"
    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }
}

def open() {
	log.debug "Open()"
    if (parent) {
		sendEvent(name:'windowShade', value:"open" as String)
        parent.domoticz_off(getIDXAddress())
    }
}

def stop() {
	log.debug "Stop()"
    if (parent) {
        sendEvent(name:'switch', value:"Stopped" as String)
        presetPosition()
    }

}

def presetPosition() {
	log.debug "presetPosition()"
    if (parent) {
        sendEvent(name:'windowShade', value:"partially open" as String)
        parent.domoticz_stop(getIDXAddress())
    }

}

def handlerEod(data) {
	sendEvent(name:'eodDone', value:true)
    switch (data.eodAction) {
    case "Up":
    	open()	
        break;
    case "Down":
    	close()
        break;
    case "Stop":
    	stop()
        break;
    default:
        log.error "Non handled eodAction(${tempEodAction})"
        break;
    }
}

def setLevel(level) {
	log.debug "setLevel() level ${level}"
	state.level = level   
    if (parent) {
    	if (device.currentValue("blindClosingTime")) {
        	if (device.currentValue("blindClosingTime") > 0 && device.currentValue("blindClosingTime") < 100000) {
        		parent.domoticz_off(getIDXAddress())
				def Sec = Math.round(device.currentValue("blindClosingTime").toInteger()/1000)
				runIn(Sec, setLevelCloseAgain)           
				log.debug "setLevel() ON in ${Sec} s"         		
            }
        }
        else {
            sendEvent(name:'switch', value:"Stopped" as String)
            sendEvent(name:'windowShade', value:"partially open" as String)
            parent.domoticz_stop(getIDXAddress())
        }
    }
}

def setLevelCloseAgain() {
    parent.domoticz_on(getIDXAddress())
    def Sec = Math.round(device.currentValue("blindClosingTime").toInteger()/1000)
	Sec = Math.round(Sec*state.level.toInteger()/100) - 1
    log.debug "setLevel() Stop in ${Sec} s"
    runIn(Sec, setLevelStopAgain)
}

def setLevelStopAgain() {

    if (stopSupported) {
        parent.domoticz_stop(getIDXAddress())
        log.debug "setLevel() STOP"
    	}
    else {
        parent.domoticz_on(getIDXAddress())
        log.debug "setLevel() second ON"
    	}
}


def calibrate() {
    
    if (device.currentValue("calibrationInProgress") == "yes")
    	{
        sendEvent(name: "calibrationInProgress", value: "no")
        def eTime = new Date().time
        sendEvent(name: "endCalibrationTime", value: eTime)
        def eT = device.currentValue("endCalibrationTime")
        def sT = device.currentValue("startCalibrationTime")
        def blindClosingTime = (eT - sT)
        sendEvent(name: "blindClosingTime", value: blindClosingTime)
		log.debug "Calibrate End() - blindClosingTime ${device.currentValue("blindClosingTime")} ms"       
        parent.domoticz_off(getIDXAddress())
        }
    else
    	{
		log.debug "Calibrate Start()"       
        sendEvent(name: "calibrationInProgress", value: "yes")
        def sTime = new Date().time
        sendEvent(name: "startCalibrationTime", value: sTime)
        parent.domoticz_on(getIDXAddress())
        }
}

def eodRunOnce(tempTime) {
	createComponent()
    
    def tempEodAction = device.currentValue("eodAction")
    runOnce(tempTime, handlerEod, [overwrite: false, data: ["eodAction": tempEodAction]])

}

private def createComponent() {
	// this will be first call Smart Screens does to the device. If no component child exists, create it now.
    log.info "CreateComponents"
    def children = getChildDevices()
    if (!children) {
    	try {
        	addChildDevice("verbem", "domoticzBlindsSmart", "${device.deviceNetworkId}-SmartScreen", null,
                            [completedSetup: true, label: "${device.displayName} (SmartScreens)",
                             isComponent: true, componentName: "SmartScreens", componentLabel: "SmartScreens"])
            }
        catch (e) {log.error e}
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

/*----------------------------------------------------*/
/*			execute event can be called from the service manager!!!
/*----------------------------------------------------*/
def generateEvent (Map results) {
    def children = getChildDevices()
    if (children) {
    	children.each { dev ->
            def passedResults = results
	    	dev.generateEvent(passedResults)
     	}
    }

return
results.each { name, value ->
	log.info "generateEvent " + name + " " + value
    
    if (name == "eodTime")	{
    	log.info "sendevent eodDone"
    	sendEvent(name:'eodDone', value:false)
        }
	if (name == "cloudCover") {
    	def cloudIcon = "http://icons.wxug.com/i/c/k/cloudy.gif"
        if (value != null) {
    	switch (value.toInteger()) {
        	case 0..20:
            	cloudIcon = "http://icons.wxug.com/i/c/k/clear.gif"
            	break
            case 21..50:
            	cloudIcon = "http://icons.wxug.com/i/c/k/partlycloudy.gif"
            	break
            case 51..80:
            	cloudIcon = "http://icons.wxug.com/i/c/k/mostlycloudy.gif"
            	break
            default:
            	break
        	}
        }
    	sendEvent(name:"${name}", value:"${value}", data:[icon:cloudIcon])}
		
	else if(name == "windBearing" || name == "sunBearing") {
    	def directionIcon = "https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/WindDirN.PNG"
        if (value != null) {
        	directionIcon = "https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/WindDir${value}.PNG"
        	}
       	log.info directionIcon
    	sendEvent(name:"${name}", value:"${value}", data:[icon:directionIcon])}    	
    	
    else {sendEvent(name:"${name}", value:"${value}")}
	
    }
    return null
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
        createComponent()
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "switch", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}