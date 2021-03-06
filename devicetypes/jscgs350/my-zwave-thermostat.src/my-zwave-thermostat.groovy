/*
 * This device type can be used with any typical zwave thermostat with little, if any, modifications.  It was modeled after the default ST device type.
 * If used with an Evolve T100R, just enable cycler mode on the thermo under advanced settings to allow ** the Circulate tile to work, otherwise 
 * you'll just be able to only use Fan Auto and Fan On.
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
 *  Updates:
 *  -------
 *  03-20-2016 : Initial release.  Starts to use the multiAttributeTile type: "thermostat", and the controls/features it brings.
 *             : NOTE: range values for sliders are only in F, so be sure to adjust for C if needed.
 *  08-27-2016 : Modified the device handler for my liking, primarly for looks and feel.
 *  08-30-2016 : Added 1x1 Activity tile next to the statusText tile, and changed that to 5x1.  Removed heat and cool level sliders.
 *  10-12-2016 : Added the capability Thermostat Fan Mode so CoRE and other SmartApps can find the thermostat needing that capability
 *  10-27-2016 : Completely changed layout to use all of the multiAttributeTile type: "thermostat", reflects unit/fan modes better, added the e-heat mode tile,
 *			   : added a single slider (does not function in auto mode), and added temperature control arrows that are usefull when in auto mode.
 *  11-19-2016 : Changed color of slider from green to a lighter grey.
 *  01-08-2017 : Added code for Health Check capabilities/functions, and cleaned up code.
 *  01-13-2017 : Added a couple debug lines in the Configure section.
 *  03-21-2017 : Added THERMOSTAT_SETPOINT to the multiAttributeTile to properly reflect mode and setpoint.
 *  06-05-2017 : Added "switch" capablity to allow automations to manipulate the fan from/to auto or circulate.
 *  08-18-2017 : Fixed value inbetween the up and down arrows on the main tile.
 *  09-09-2017 : Changed the UI for ST's new slider format (went back to individuals sliders for cool/heat vs one slider), changed icons, colors, and format.
 *  09-12-2017 : Added another tile specifically used for the Things view for the device (temperature2).
 *
*/
metadata {
	// Automatically generated. Make future change here.
	definition (name: "My ZWave Thermostat", namespace: "jscgs350", author: "SmartThings")
    { 
		capability "Refresh"
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
        capability "Thermostat Fan Mode"
		capability "Thermostat"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Health Check"
        capability "Switch"
       
		command "setLevelUp"
		command "setLevelDown"
		command "heatLevelUp"
		command "heatLevelDown"
        command "quickSetHeat"
		command "coolLevelUp"
		command "coolLevelDown"
        command "quickSetCool"
        command "offmode"
        
		attribute "thermostatFanState", "string"
        attribute "currentState", "string"
        attribute "currentMode", "string"
        attribute "currentfanMode", "string"
	}

//Thermostat Temp and State
	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type: "thermostat", width: 6, height: 4, decoration: "flat"){
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}°', unit:"dF")
            }
			tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "setLevelUp")
				attributeState("VALUE_DOWN", action: "setLevelDown")
			}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}%', unit:"%")
            }            
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", backgroundColor:"#44b621")
                attributeState("heating", backgroundColor:"#ea5462")
                attributeState("cooling", backgroundColor:"#269bd2")
				attributeState("fan only", backgroundColor:"#145D78")
				attributeState("pending heat", backgroundColor:"#B27515")
				attributeState("pending cool", backgroundColor:"#197090")
				attributeState("vent economizer", backgroundColor:"#8000FF")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("emergency heat", label:'${name}')
                attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}°', unit:"dF")
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label:'${currentValue}°', unit:"dF")
            }
            tileAttribute("device.thermostatSetpoint", key: "THERMOSTAT_SETPOINT") {
                attributeState("default", label:'${currentValue}°', unit:"dF")
            } 
		}       

//Thermostat Mode Control
        standardTile("modeheat", "device.thermostatMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "heat", label:'Heat Mode', action:"heat", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/heat@2x.png"
        }
        standardTile("modecool", "device.thermostatMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "cool", label:'Cool Mode', action:"cool", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/cool@2x.png"
        }
        standardTile("modeauto", "device.thermostatMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "auto", label:'Auto Mode', action:"auto", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/auto@2x.png"
        }
        standardTile("modeheatemrgcy", "device.thermostatMode", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state "heatemrgcy", label:'', action:"emergencyHeat", icon:"st.thermostat.emergency-heat"
        }         
        standardTile("modeoff", "device.thermostatMode", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state "off", label: '', action:"offmode", icon:"st.thermostat.heating-cooling-off"
        }        

//Heating Set Point Controls
        standardTile("heatTile", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"heat", icon:"st.thermostat.heat", backgroundColor: "#ea5462"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", width: 1, height: 2, inactiveLabel: false, range:"(60..80)") {
			state "default", label:'${currentValue}', action:"quickSetHeat", backgroundColor: "#ea5462"
		}

//Cooling Set Point Controls
        standardTile("coolTile", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"cool", icon:"st.thermostat.cool", backgroundColor: "#269bd2"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", width: 1, height: 2, inactiveLabel: false, range:"(60..80)") {
			state "default", label:'${currentValue}', action:"quickSetCool", backgroundColor: "#269bd2"
		}

//Fan Mode Control        
        standardTile("fanauto", "device.thermostatFanMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "fanauto", label:'Set Fan To Auto', action:"fanAuto", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/fan-auto@2x.png"
        }
        standardTile("fanon", "device.thermostatFanMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "fanon", label:'Turn On Fan', action:"fanOn", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/fan-on@2x.png"
        }
        standardTile("fancir", "device.thermostatFanMode", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "fancir", label:'Circulate Fan', action:"fanCirculate", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/fan-on@2x.png"
        }

//Refresh and Config Controls
		standardTile("refresh", "device.refresh", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"polling.poll", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "device.configure", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

//Miscellaneous tiles used in this DH
        standardTile("statusL1Text", "statusL1Text", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", label:'${currentValue}', icon:"st.Home.home1"
		}
        standardTile("statusL2Text", "statusL2Text", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", label:'${currentValue}', icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/fan-on@2x.png"
		}
        valueTile("temperature2", "device.temperature", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}°', icon:"st.thermostat.ac.air-conditioning",
				backgroundColors:[
					[value: 32, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
            ]
        }
        
		main (["temperature2"])
		details(["temperature", "heatTile", "heatSliderControl", "coolSliderControl", "coolTile", "statusL1Text", "statusL2Text", "fanon", "fanauto", "fancir", "modeheat", "modecool", "modeauto", "modeheatemrgcy", "refresh", "modeoff", "configure"])
	}
}

def updated(){
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description)
{

	def map = createEvent(zwaveEvent(zwave.parse(description, [0x42:1, 0x43:2, 0x31: 3])))
	if (!map) {
		return null
	}

	if (map.name == "thermostatFanMode"){
		if (map.value == "fanAuto") {sendEvent(name: "currentfanMode", value: "Auto Mode" as String)}
	    if (map.value == "fanOn") {sendEvent(name: "currentfanMode", value: "On Mode" as String)}
	    if (map.value == "fanCirculate") {sendEvent(name: "currentfanMode", value: "Cycle Mode" as String)}
	}

	def result = [map]
	if (map.isStateChange && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: getTemperatureScale()
		]
		if (map.name == "thermostatMode") {
			state.lastTriedMode = map.value
			if (map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
			}
			else {
				map2.value = device.latestValue("heatingSetpoint")
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			if ((map.name == "heatingSetpoint" && mode == "heat") || (map.name == "coolingSetpoint" && mode == "cool")) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if (map2.value != null) {
			result << createEvent(map2)
		}
	} else if (map.name == "thermostatFanMode" && map.isStateChange) {
		state.lastTriedFanMode = map.value
	}

	def statusL1Textmsg = ""
    def statusL2Textmsg = ""

	if (device.currentState('currentMode').value == "Off") {
    	statusL1Textmsg = "Unit is Off"
    } else {
    	statusL1Textmsg = "Unit is in ${device.currentState('currentMode').value} mode and is ${device.currentState('currentState').value}"
    }
    sendEvent("name":"statusL1Text", "value":statusL1Textmsg)

    statusL2Textmsg = "Fan is in ${device.currentState('currentfanMode').value} and is ${device.currentState('thermostatFanState').value}"
    sendEvent("name":"statusL2Text", "value":statusL2Textmsg)
    
	log.debug "Parse returned $result"
	result
}

//
//Receive updates from the thermostat and update the app
//

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false
	switch (cmd.setpointType) {
		case 1:
			map.name = "heatingSetpoint"
			break;
		case 2:
			map.name = "coolingSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 1) {
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport cmd)
{
	def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
            sendEvent(name: "currentState", value: "Idle" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
           	sendEvent(name: "currentState", value: "running" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
            sendEvent(name: "currentState", value: "running" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
			sendEvent(name: "currentState", value: "fan only" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
            sendEvent(name: "currentState", value: "pending heat" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
            sendEvent(name: "currentState", value: "pending cool" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
            sendEvent(name: "currentState", value: "vent economizer" as String)
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def map = [name: "thermostatFanState", unit: ""]
	switch (cmd.fanOperatingState) {
		case 0:
			map.value = "idle"
            sendEvent(name: "thermostatFanState", value: "idle")
			break
		case 1:
			map.value = "running"
            sendEvent(name: "thermostatFanState", value: "running")
			break
		case 2:
			map.value = "running high"
            sendEvent(name: "thermostatFanState", value: "running high")
			break
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
            sendEvent(name: "currentMode", value: "Off" as String)
            break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
            sendEvent(name: "currentMode", value: "Heat" as String)
            break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergencyHeat"
            sendEvent(name: "currentMode", value: "E-Heat" as String)
            break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
            sendEvent(name: "currentMode", value: "Cool" as String)
            break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
            sendEvent(name: "currentMode", value: "Auto" as String)
            break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [:]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "fanAuto"
            sendEvent(name: "currentfanMode", value: "Auto Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "fanOn"
            sendEvent(name: "currentfanMode", value: "On Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "fanCirculate"
            sendEvent(name: "currentfanMode", value: "Cycle Mode" as String)
			break
	}
	map.name = "thermostatFanMode"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergencyHeat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }
	state.supportedModes = supportedModes
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = "fanAuto fanOn fanCirculate "
	state.supportedFanModes = supportedFanModes
}

def updateState(String name, String value) {
	state[name] = value
	device.updateDataValue(name, value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

//
//Send commands to the thermostat
//

def setLevelUp(){
	log.debug "Setting the setpoint UP a degree..."
    if (device.latestValue("thermostatMode") == "heat") {
    	log.debug "...for heat..."
    	int nextLevel = device.currentValue("heatingSetpoint") + 1
    	setHeatingSetpoint(nextLevel)
	} else if (device.latestValue("thermostatMode") == "cool") {
    	log.debug "...for cool..."    
        int nextLevel = device.currentValue("coolingSetpoint") + 1
        setCoolingSetpoint(nextLevel)
	} else if (device.latestValue("thermostatMode") == "auto") {
        int nextHeatLevel = device.currentValue("heatingSetpoint") + 1
        int nextCoolLevel = device.currentValue("coolingSetpoint") + 1
        if (device.latestValue("thermostatOperatingState") == "heating") {
        	log.debug "...for auto heat..."
        	setHeatingSetpoint(nextHeatLevel)
        } else if (device.latestValue("thermostatOperatingState") == "cooling") {
        	log.debug "...for auto cool..."
        	setCoolingSetpoint(nextCoolLevel)
        } else {
            log.debug "...for auto heat AND cool..."
	    	delayBetween([setHeatingSetpoint(nextHeatLevel), setCoolingSetpoint(nextCoolLevel)], 3000)
        }
	}    
}

def setLevelDown(){
	log.debug "Setting the setpoint DOWN a degree..."
    if (device.latestValue("thermostatMode") == "heat") {
    	log.debug "...for heat..."
    	int nextLevel = device.currentValue("heatingSetpoint") - 1
    	setHeatingSetpoint(nextLevel)
	} else if (device.latestValue("thermostatMode") == "cool") {
    	log.debug "...for cool..."    
        int nextLevel = device.currentValue("coolingSetpoint") - 1
        setCoolingSetpoint(nextLevel)
	} else if (device.latestValue("thermostatMode") == "auto") {
        int nextHeatLevel = device.currentValue("heatingSetpoint") - 1
        int nextCoolLevel = device.currentValue("coolingSetpoint") - 1
        if (device.latestValue("thermostatOperatingState") == "heating") {
        	log.debug "...for auto heat..."
        	setHeatingSetpoint(nextHeatLevel)
        } else if (device.latestValue("thermostatOperatingState") == "cooling") {
        	log.debug "...for auto cool..."
        	setCoolingSetpoint(nextCoolLevel)
        } else {
            log.debug "...for auto heat AND cool..."
	    	delayBetween([setHeatingSetpoint(nextHeatLevel), setCoolingSetpoint(nextCoolLevel)], 3000)
        }
	}    
}

def heatLevelUp(){
    int nextLevel = device.currentValue("heatingSetpoint") + 1
    setHeatingSetpoint(nextLevel)
}

def heatLevelDown(){
    int nextLevel = device.currentValue("heatingSetpoint") - 1
    setHeatingSetpoint(nextLevel)
}

def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees, 1000)
}

def setHeatingSetpoint(degrees, delay = 5000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 5000) {
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }
	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], delay)
}

def coolLevelUp(){
    int nextLevel = device.currentValue("coolingSetpoint") + 1
    setCoolingSetpoint(nextLevel)
}

def coolLevelDown(){
    int nextLevel = device.currentValue("coolingSetpoint") - 1
    setCoolingSetpoint(nextLevel)
}

def quickSetCool(degrees) {
	setCoolingSetpoint(degrees, 1000)
}

def setCoolingSetpoint(degrees, delay = 5000) {
	setCoolingSetpoint(degrees.toDouble(), delay)
}

def setCoolingSetpoint(Double degrees, Integer delay = 5000) {
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }
	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p,  scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
	], delay)
}

def offmode() {
	log.debug "Switching to off mode..."
    sendEvent(name: "currentMode", value: "Off" as String)
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)
}

def on() {
	log.debug "Setting thermostat fan mode to circulate..."
    fanCirculate()
}

def off() {
	log.debug "Setting thermostat fan mode to auto..."
    fanAuto()
}

def heat() {
	log.debug "Switching to heat mode..."
    sendEvent(name: "currentMode", value: "Heat" as String)
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)   
}

def cool() {
	log.debug "Switching to cool mode..."
    sendEvent(name: "currentMode", value: "Cool" as String)
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)
}

def auto() {
	log.debug "Switching to auto mode..."
    sendEvent(name: "currentMode", value: "Auto" as String)
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)
}

def emergencyHeat() {
	log.debug "Switching to emergency heat mode..."
    sendEvent(name: "currentMode", value: "E-Heat" as String)
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 4).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)
}

def fanOn() {
	log.debug "Switching fan to on mode..."
    sendEvent(name: "currentfanMode", value: "On Mode" as String)
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 1).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
        zwave.thermostatFanStateV1.thermostatFanStateGet().format()
	], 3000)   
}

def fanAuto() {
	log.debug "Switching fan to auto mode..."
    sendEvent(name: "currentfanMode", value: "Auto Mode" as String)
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 0).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
        zwave.thermostatFanStateV1.thermostatFanStateGet().format()
	], 3000)
}

def fanCirculate() {
	log.debug "Switching fan to circulate mode..."
    sendEvent(name: "currentfanMode", value: "Cycle Mode" as String)
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
        zwave.thermostatFanStateV1.thermostatFanStateGet().format()
	], 3000)
}

def poll() {
	log.debug "Executing poll/refresh...."
	delayBetween([
		zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
		zwave.thermostatFanStateV1.thermostatFanStateGet().format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
		zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 3000)
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() {
	poll()
}

def configure() {
	log.debug "Executing configure...."
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
		zwave.thermostatFanModeV3.thermostatFanModeSupportedGet().format(),
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	], 2300)
    log.debug "....done executing configure"
}

private getStandardDelay() {
	1000
}