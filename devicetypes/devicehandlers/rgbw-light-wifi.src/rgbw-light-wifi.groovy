/**
 *  Copyright 2015 SmartThings
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
 *  Z-Wave RGBW Light
 *
 *  Author: SmartThings
 *  Date: 2015-7-12
 */

metadata {
	definition (name: "RGBW Light - WIFI", namespace: "deviceHandlers", author: "mparentes") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"

		//command "reset"

	}
    
    preferences {
    input("serverIP", "string", title:"Server IP Address", description: "Please enter your server's IP Address", required: true, displayDuringSetup: true)
    input("serverPort", "string", title:"Server Port", description: "Please enter your server's Port", defaultValue: 80 , required: true, displayDuringSetup: true)
    input("lightID", "string", title:"Light ID", description: "Please enter your light's ID", required: false, displayDuringSetup: true)

	}

	simulator {
	}

	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"off"
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"on"

	}
	standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat") {
		state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
		state "level", action:"switch level.setLevel"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setColor"
	}
	valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}
	controlTile("colorTempControl", "device.colorTemperature", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "colorTemperature", action:"setColorTemperature"
	}
	valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
		state "hue", label: 'Hue ${currentValue}   '
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "reset", "colorTempControl", "refresh"])
}

def updated() {
	//response(refresh())
}

def parse(description) {

	def msg = parseLanMessage(description)
	def headerString = msg.header
    def bodyString = msg.body
    
    def result = []
    
	//log.debug("PARSE: ${bodyString}")
    
    if (bodyString) {
		def json = msg.json;
        log.debug "Response: ${json}"
        
        if( json?.switch ) {
            
        	log.debug "switch value ${json.switch}"
            
			result << createEvent(name: "switch", value: json.switch)
		}
        
        if( json?.brightness ) {
            
        	log.debug "brightness value ${json.brightness}"
            
			result << createEvent(name: "level", value: json.brightness)
		}
        
        
    }
    
	result
}
def on() {
	poll('/white/on')
	
}

def off() {
    poll('/white/off')
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	if(level > 99) level = 99
    
    log.debug("SET LEVEL...... $level - $duration")
    
    poll("/brightness/$level")
	
}

def refresh() {

	log.debug("Refresh...")
	
}

def setSaturation(percent) {
	log.debug "setSaturation($percent)"
	setColor(saturation: percent)
}

def setHue(value) {
	log.debug "setHue($value)"
	setColor(hue: value)
}

def setColor(value) {
	def result = []
	log.debug "setColor: ${value}"
    
		
    def r = value.red
    def g = value.green
    def b = value.blue
    def level = device.currentValue("level")
    //console.log("HUE: $hue")
    poll("/rgb/${r}/${g}/${b}/${level}")


	//else if (value.hex) {
	//	def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
	//	//result << zwave.switchColorV3.switchColorSet(red:c[0], green:c[1], blue:c[2], warmWhite:0, coldWhite:0)
	//} else {
//		def hue = value.hue ?: device.currentValue("hue")
//		def saturation = value.saturation ?: device.currentValue("saturation")
//		if(hue == null) hue = 13
//		if(saturation == null) saturation = 13
//		def rgb = huesatToRGB(hue, saturation)
//		//result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
//	}

//	if(value.hue) sendEvent(name: "hue", value: value.hue)
//	if(value.hex) sendEvent(name: "color", value: value.hex)
//	if(value.switch) sendEvent(name: "switch", value: value.switch)
//	if(value.saturation) sendEvent(name: "saturation", value: value.saturation)

//	commands(result)
}

def setColorTemperature(percent) {
	if(percent > 99) percent = 99
	int warmValue = percent * 255 / 99
	//command(zwave.switchColorV3.switchColorSet(red:0, green:0, blue:0, warmWhite:warmValue, coldWhite:(255 - warmValue)))
}

//Connection
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    return ip + ":" + port
}

def poll(path) {
	
    //def userpassascii = "${CameraUser}:${CameraPassword}"
	//def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = serverIP
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(serverPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def headers = [:] 
    headers.put("HOST", "$host:$serverPort")
    //headers.put("Authorization", userpass)
    
    //log.debug "The Header is $headers"
    
    
 //def path = "/motion.cgi?MotionDetectionEnable=${motion}&ConfigReboot=No"
 log.debug "path is: $path"
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
    
}

//End Connection
def reset() {
	log.debug "reset()"
	sendEvent(name: "color", value: "#ffffff")
	setColorTemperature(99)
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		//zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		//cmd.format()
	}
}

private commands(commands, delay=200) {
	//delayBetween(commands.collect{ command(it) }, delay)
}

def rgbToHSV(red, green, blue) {
	float r = red / 255f
	float g = green / 255f
	float b = blue / 255f
	float max = [r, g, b].max()
	float delta = max - [r, g, b].min()
	def hue = 13
	def saturation = 0
	if (max && delta) {
		saturation = 100 * delta / max
		if (r == max) {
			hue = ((g - b) / delta) * 100 / 6
		} else if (g == max) {
			hue = (2 + (b - r) / delta) * 100 / 6
		} else {
			hue = (4 + (r - g) / delta) * 100 / 6
		}
	}
	[hue: hue, saturation: saturation, value: max * 100]
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}