/**
 *  Aeon Smart Energy Switch gen-1
 *  Aeon Micro Smart Switch (G2)
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
 *  02-16-2016 : Removed posting to the Activity Feed (Recently tab) in the phone app and event log.
 *  02-17-2016 : Added the ability to change the icon.
 *  02-20-2016 : Fixed to use the right parameters for changed/timed reporting, and documented the parameters better.
 *  02-21-2016 : Made certain configuration parameters changeable via device preferences instead of having to tweak code all the time.
 *  02-27-2016 : Changed date formats to be MM-dd-yyyy h:mm a
 *  02-29-2016 : Changed reportType variable from 0 to 1.
 *  03-11-2016 : Due to ST's v2.1.0 app totally hosing up SECONDARY_CONTROL, implemented a workaround to display that info in a separate tile.
 *  03-19-2016 : Changed tile layout, added clarity for preferences, and removed rounding (line 171)
 *  07-07-2016 : Check for wildly large watts value coming from the switch and do not process them.
 *  08-22-2016 : Tile format changes, specifically statusText.
 *  08-27-2016 : Modified the device handler for my liking, primarly for looks and feel.
 *  01-08-2017 : Added code for Health Check capabilities/functions, and cleaned up code.
 *  01-18-2017 : Removed code no longer needed, and added another parameter in Preference to enable or disable the display of values in the Recently tab and device's event log (not Live Logs).  Enabling may be required for some SmartApps.
 *  01-19-2017 : Added code similar to the HEM v1 to display energy and cost.
 *  02-11-2017 : Cleaned up code and added an icon to the secondary_control section of the main tile.
 *  03-11-2017 : Changed from valueTile to standardTile for a few tiles since ST's mobile app v2.3.x changed something between the two.
 *  03-24-2017 : Changed color schema to match ST's new format.
 *  03-26-2017 : Added a new device Preference that allows for selecting how many decimal positions should be used to display for WATTS and kWh.  What's stored for the actual meter reading that's seen in the IDE for Power, and what's sent to SmartApps, did not change.
 *  05-28-2017 : Sometimes the HEM will send a super low reading, like 0.04672386; which in that case the decimal position setting would not get applied if you used 3.  I fixed that.
 *  06-12-2017 : Updated code to make sure kWh or kVAh readings from the reader are larger that the previous reading.  There should never be a smaller reading from the previous reading.
 *  09-06-2017 : Removed fingerprint.  Checking to see if this helps in joining the device since it doesn't have to go through all the configuration steps when included.
 *  09-15-2017 : Changed tile layout, made tiles smaller, and removed the reset info messages since you can find them in the Recently tab or the device's event log.
 *
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "My Aeon Metering Switch", namespace: "jscgs350", author: "SmartThings") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
        capability "Outlet"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"

        attribute "energyDisp", "string"
        attribute "energyOne", "string"
        attribute "energyTwo", "string"

        attribute "powerDisp", "string"
        attribute "powerOne", "string"
        attribute "powerTwo", "string"

        command "resetenergy"
        command "resetmax"
        command "configure"
        command "resetMeter"

	}

    preferences {
        input "disableOnOff", "boolean", 
            title: "Disable On/Off switch?", 
            defaultValue: false, 
            displayDuringSetup: true
        input "debugOutput", "boolean", 
            title: "Enable debug logging?", 
            defaultValue: false, 
            displayDuringSetup: true
        input "displayEvents", "boolean",
            title: "Display all events in the Recently tab and the device's event log?", 
            defaultValue: false,
            required: false,
            displayDuringSetup: true
        input "kWhCost", "string",
            title: "Enter your cost per kWh (or just use the default, or use 0 to not calculate):",
            defaultValue: 0.16,
            required: false,                
            displayDuringSetup: true            
        input "reportType", "number", 
            title: "ReportType: Send watts/kWh data on a time interval (0), or on a change in wattage (1)? Enter a 0 or 1:",  
            defaultValue: 1,
            range: "0..1",
            required: false, 
            displayDuringSetup: true
        input "wattsChanged", "number", 
            title: "For ReportType = 1, Don't send unless watts have changed by this many watts: (range 0 - 32,000W)",  
            defaultValue: 50,
            range: "0..32000",
            required: false, 
            displayDuringSetup: true
        input "wattsPercent", "number", 
            title: "For ReportType = 1, Don't send unless watts have changed by this percent: (range 0 - 99%)",  
            defaultValue: 10,
            range: "0..99",
            required: false, 
            displayDuringSetup: true
        input "secondsWatts", "number", 
            title: "For ReportType = 0, Send Watts data every how many seconds? (range 0 - 65,000 seconds)",  
            defaultValue: 10,
            range: "0..65000",
            required: false, 
            displayDuringSetup: true
        input "secondsKwh", "number", 
            title: "For ReportType = 0, Send kWh data every how many seconds? (range 0 - 65,000 seconds)",  
            defaultValue: 60,
            range: "0..65000",
            required: false, 
            displayDuringSetup: true 
        input "decimalPositions", "number", 
            title: "How many decimal positions do you want watts AND kWh to display? (range 0 - 3)",  
            defaultValue: 3,
            range: "0..3",
            required: false, 
            displayDuringSetup: true
    }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, decoration: "flat"){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
            tileAttribute ("secondaryText", key: "SECONDARY_CONTROL") {
           		attributeState "secondaryText", label:'${currentValue}', icon: "https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/device-activity-tile@2x.png"
            }
		}

// Watts row

        valueTile("powerDisp", "device.powerDisp", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state ("default", icon: "st.secondary.activity", label:'${currentValue}W')
        }
        standardTile("powerOne", "device.powerOne", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("default", label:'Low ${currentValue}')
        }
        standardTile("powerTwo", "device.powerTwo", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state("default", label:'High ${currentValue}')
        }
        
        standardTile("iconTile", "statusText", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon:"http://cdn.device-icons.smartthings.com/secondary/device-activity-tile@2x.png"
		}

        valueTile("energyDisp", "device.energyDisp", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: '${currentValue}kWh', backgroundColor:"#ffffff")
        }
        standardTile("energyOne", "device.energyOne", width: 5, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: '${currentValue}', backgroundColor:"#ffffff")
        }
        valueTile("energyTwo", "device.energyTwo", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: 'Cost $${currentValue}', backgroundColor:"#ffffff")
        }    
        
        standardTile("resetenergy", "device.energy", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Reset Energy Use', action:"resetenergy", icon:"st.secondary.refresh-icon"
		}        
        standardTile("resetmax", "device.energy", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Reset Maximum', action:"resetmax", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "device.power", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
        standardTile("statusText", "statusText", inactiveLabel: false, decoration: "flat", width: 5, height: 1) {
			state "statusText", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		main "powerDisp"
		details(["switch", "energyDisp", "energyTwo", "iconTile", "statusText", "resetmax", "resetenergy", "refresh","configure"])
	}
}

def updated() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    state.onOffDisabled = ("true" == disableOnOff)
    state.debug = ("true" == debugOutput)
    state.displayDisabled = ("true" == displayEvents)
    log.debug "updated(disableOnOff: ${disableOnOff}(${state.onOffDisabled}), debugOutput: ${debugOutput}(${state.debug}), reportType: ${reportType}, wattsChanged: ${wattsChanged}, wattsPercent: ${wattsPercent}, secondsWatts: ${secondsWatts}, secondsKwh: ${secondsKwh}, decimalPositions: ${decimalPositions})"
    response(configure())
}

def parse(String description) {
	if (state.debug) log.debug "Incoming to parse: ${description}"
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
        
    def statusTextmsg = ""
    statusTextmsg = "Max of ${device.currentState('powerTwo')?.value}"
    sendEvent(name: "statusText", value: statusTextmsg)
    if (state.debug) log.debug statusTextmsg
    
    def secondaryTextmsg = ""
    secondaryTextmsg = "Currently using ${device.currentState('powerDisp')?.value}W"
    sendEvent(name: "secondaryText", value: secondaryTextmsg, displayed: false)
    if (state.debug) log.debug secondaryTextmsg    

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    if (state.debug) log.debug "zwaveEvent received ${cmd}"
    def dispValue
    def dispLowValue
    def dispHighValue
    def newValue
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
	if (cmd.scale == 0) {
        newValue = cmd.scaledMeterValue
        if (newValue > state.energyValue) {
            dispValue = newValue
            if (decimalPositions == 3) {
            	def decimalDisplay = String.format("%3.3f",newValue)
                dispValue = decimalDisplay
            } else if (decimalPositions == 2) {
                def decimalDisplay = String.format("%3.2f",newValue)
                dispValue = decimalDisplay
            } else if (decimalPositions == 1) {
                def decimalDisplay = String.format("%3.1f",newValue)
                dispValue = decimalDisplay
            } else if (decimalPositions == 0) {
                dispValue = Math.round(cmd.scaledMeterValue)
            } else {
                dispValue = newValue
            }
            sendEvent(name: "energyDisp", value: dispValue as String, unit: "", displayed: false)
            state.energyValue = newValue
            BigDecimal costDecimal = newValue * ( kWhCost as BigDecimal)
            def costDisplay = String.format("%3.2f",costDecimal)
            sendEvent(name: "energyTwo", value: "${costDisplay}", unit: "", displayed: false)
            if (state.displayDisabled) {
                [name: "energy", value: newValue, unit: "kWh", displayed: true]
            } else {
                [name: "energy", value: newValue, unit: "kWh", displayed: false]
            }
		}
	} else if (cmd.scale == 1) {
        newValue = cmd.scaledMeterValue
        if (newValue > state.energyValue) {
            dispValue = newValue+"\nkVAh"
            sendEvent(name: "energyDisp", value: dispValue as String, unit: "", displayed: false)
            state.energyValue = newValue
            if (state.displayDisabled) {
                [name: "energy", value: newValue, unit: "kVAh", displayed: true]
            } else {
                [name: "energy", value: newValue, unit: "kVAh", displayed: false]
            }
        }
    } else {
			newValue = cmd.scaledMeterValue
            if (newValue < 3000) {								  // don't handle any wildly large readings due to firmware issues
	            if (newValue != state.powerValue) {
	                dispValue = newValue
	                if (decimalPositions == 3) {
		            	def decimalDisplay = String.format("%3.3f",newValue)
		                dispValue = decimalDisplay
                    } else if (decimalPositions == 2) {
                    	def decimalDisplay = String.format("%3.2f",newValue)
                    	dispValue = decimalDisplay
                    } else if (decimalPositions == 1) {
						def decimalDisplay = String.format("%3.1f",newValue)
                    	dispValue = decimalDisplay
                    } else if (decimalPositions == 0) {
                    	dispValue = Math.round(cmd.scaledMeterValue)
                    } else {
                    	dispValue = newValue
                    }
	                sendEvent(name: "powerDisp", value: dispValue, unit: "", displayed: false)
	                if (newValue < state.powerLow) {
	                    dispLowValue = dispValue+"w"+"on "+timeString
	                    sendEvent(name: "powerOne", value: dispLowValue as String, unit: "", displayed: false)
	                    state.powerLow = newValue
	                }
	                if (newValue > state.powerHigh) {
	                    dispHighValue = dispValue+"w "+"on "+timeString
	                    sendEvent(name: "powerTwo", value: dispHighValue as String, unit: "", displayed: false)
	                    state.powerHigh = newValue
	                }
	                state.powerValue = newValue
                    if (state.displayDisabled) {
                        [name: "power", value: newValue, unit: "W", displayed: true]
                    } else {
                        [name: "power", value: newValue, unit: "W", displayed: false]
                    }                    
	            }
			}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "physical"
	]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "digital"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
    if (state.onOffDisabled) {
        if (state.debug) log.debug "On/Off disabled"
        delayBetween([
            zwave.basicV1.basicGet().format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ], 5)
    }
    else {
        delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    }
}


def off() {
    if (state.onOffDisabled) {
        if (state.debug) log.debug "On/Off disabled"
        delayBetween([
            zwave.basicV1.basicGet().format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ], 5)
    }
    else {
        delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    }
}

def poll() {
    refresh()
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() {
	refresh()
}

def refresh() {
    if (state.debug) log.debug "${device.name} refresh"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def resetmax() {
    if (state.debug) log.debug "${device.name} reset"
    state.powerHigh = 0   
	def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    sendEvent(name: "energyOne", value: "Watts Data Maximum Value Reset On:\n"+timeString, unit: "")    
    sendEvent(name: "powerTwo", value: "", unit: "")
    def cmd = delayBetween( [
        zwave.meterV2.meterGet(scale: 0).format()
    ])
    cmd
}

def resetenergy() {
    log.debug "${device.name} reset kWh/Cost values"
	def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    sendEvent(name: "energyOne", value: "Energy Data (kWh/Cost) Reset On:\n"+timeString, unit: "")       
    sendEvent(name: "energyDisp", value: "", unit: "")
    sendEvent(name: "energyTwo", value: "Cost\n--", unit: "")
    state.energyValue = 0
    def cmd = delayBetween( [
        zwave.meterV2.meterReset().format(),
        zwave.meterV2.meterGet(scale: 0).format()
    ])
    cmd
}

def resetMeter() {
	log.debug "Resetting all metering switch values..."
    state.powerHigh = 0
    state.energyValue = 0
    sendEvent(name: "powerOne", value: "", unit: "")
	sendEvent(name: "powerTwo", value: "", unit: "")
    sendEvent(name: "energyDisp", value: "", unit: "")
    sendEvent(name: "energyTwo", value: "Cost\n--", unit: "")
	def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    sendEvent(name: "energyOne", value: "Metering switch was reset on "+timeString, unit: "")
    def cmd = delayBetween( [
        zwave.meterV2.meterReset().format(),
        zwave.meterV2.meterGet(scale: 0).format(),
    	zwave.meterV2.meterGet(scale: 2).format()
    ])
    cmd
}

def configure() {
    log.debug "${device.name} configuring..."
    
    if (reportType == 0) {
		log.debug "Setting reportType to ${reportType} per user request."
	} else if (reportType == 1) {
		log.debug "Setting reportType to ${reportType} per user request."
	}
    else {
        def reportType = 1
        log.debug "Setting reportType to ${reportType} because an invalid value was provided."
    }
    
    if (wattsChanged < 0) {
        def wattsChanged = 50
        log.debug "Setting wattsChanged to ${wattsChanged} (device default) because an invalid value was provided."
	} else if (wattsChanged < 32001) {
		log.debug "Setting wattsChanged to ${wattsChanged} per user request."
	}
    else {
        def wattsChanged = 50
        log.debug "Setting wattsChanged to ${wattsChanged} (device default) because an invalid value was provided."
    }    

    if (wattsPercent < 0) {
        def wattsPercent = 10
        log.debug "Setting wattsPercent to ${wattsPercent} (device default) because an invalid value was provided."
	} else if (wattsPercent < 100) {
		log.debug "Setting wattsPercent to ${wattsPercent} per user request."
	}
    else {
        def wattsPercent = 10
        log.debug "Setting wattsPercent to ${wattsPercent} (device default) because an invalid value was provided."
    } 

    if (secondsWatts < 0) {
        def secondsWatts = 600
        log.debug "Setting secondsWatts to ${secondsWatts} (device default) because an invalid value was provided."
	} else if (secondsWatts < 65000) {
		log.debug "Setting secondsWatts to ${secondsWatts} per user request."
	}
    else {
        def secondsWatts = 600
        log.debug "Setting secondsWatts to ${secondsWatts} (device default) because an invalid value was provided."
    } 

    if (secondsKwh < 0) {
        def secondsKwh = 600
        log.debug "Setting secondsKwh to ${secondsKwh} (device default) because an invalid value was provided."
	} else if (secondsKwh < 65000) {
		log.debug "Setting secondsKwh to ${secondsKwh} per user request."
	}
    else {
        def secondsKwh = 600
        log.debug "Setting secondsKwh to ${secondsKwh} (device default) because an invalid value was provided."
    }

	delayBetween([
    
    // Send data based on a time interval (0), or based on a change in wattage (1).  0 is default. 1 enables parameters 91 and 92.
    zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: reportType).format(),
    
    // If parameter 90 is 1, don't send unless watts have changed by 50 <default>
    zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: wattsChanged).format(),
    
    // If parameter 90 is 1, don't send unless watts have changed by 10% <default>
    zwave.configurationV1.configurationSet(parameterNumber: 92, size: 1, scaledConfigurationValue: wattsPercent).format(),
    
	// Defines the type of report sent for Reporting Group 1.  2->MultiSensor Report, 4->Meter Report for Watt, 8->Meter Report for kWh
	zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),
    
    // If parameter 90 is 0, report every XX Seconds (for Watts) for Reporting Group 1.
    zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: secondsWatts).format(),
    
    // Defines the type of report sent for Reporting Group 2.  2->MultiSensor Report, 4->Meter Report for Watt, 8->Meter Report for kWh
    zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),
    
    // If parameter 90 is 0, report every XX seconds (for kWh) for Reporting Group 2.
    zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: secondsKwh).format(),
    
    // Disable Reporting Group 3 parameters
    zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),
    zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 0).format()
	])
}