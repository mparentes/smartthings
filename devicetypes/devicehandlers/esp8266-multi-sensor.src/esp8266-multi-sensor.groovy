/**
 *  ESP8266 Temperature/Humidity Sensor
 *
 *  Copyright 2016 Marcos Parentes
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
 
preferences {
	input("ip", "text", title: "IP Address", description: "IP", required: true)
    input("port", "text", title: "Port", description: "Port", required: true)
    input("mac", "text", title: "MAC Addr", description: "MAC")
}

metadata {
	definition (name: "ESP8266 Multi Sensor", namespace: "devicehandlers", author: "Marcos Parentes") {
		capability "Polling"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Sensor"
		capability "Temperature Measurement"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		valueTile("temperature", "device.temperature", decoration: "flat", width: 1, height: 1) {
        	state "temperature", label:'${currentValue}°', unit: "F",
              	backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
    	}
        valueTile("humidity", "device.humidity", width: 1, height: 1) {
            state "humidity", label:'${currentValue}%', unit: "",
            	backgroundColors:[
            		[value: 16, color: "#5600A3"],
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
        }
        standardTile("refresh", "device.backdoor", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        main "temperature"
		details("temperature", "humidity", "refresh")
	}
}

// parse events into attributes
def parse(String description) {

	def msg = parseLanMessage(description)
	def headerString = msg.header
    def bodyString = msg.body
    
    // log.debug "body string: ${bodyString}"
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
    
    def result = []
    
    if (bodyString) {
		def json = msg.json;
        log.debug "Response: ${json}"
             
		if( json?.temperature ) {
			//if(getTemperatureScale() == "F"){
			//	value = (celsiusToFahrenheit(json.value) as Float).round(0) as Integer
			//} else {
			//	value = json.value
			//}
            
			log.debug "temperature value ${json.temperature}"
			result << createEvent(name: "temperature", value: json.temperature)
		}
        
        if( json?.humidity ) {
            
			log.debug "humidity value ${json.humidity}"
			result << createEvent(name: "humidity", value: json.humidity)
		}
	}
	result

}

// handle commands
def poll() {
	log.debug "Executing 'poll' ${getHostAddress()} /getMyData"
	new physicalgraph.device.HubAction(
    	method: "GET",
    	path: "/",
    	headers: [
        	HOST: "${getHostAddress()}"
    	]
	)
}

def refresh() {
	log.debug "Executing 'refresh' ${getHostAddress()}"
	if(!settings.mac) {
		// if mac address is blank in settings, then use ip:port, but ST will not get updates it will only get Poll results.
		log.debug "setting device network id to ip:port"
		def hosthex = convertIPtoHex(settings.ip)
		def porthex = convertPortToHex(settings.port)
		device.deviceNetworkId = "$hosthex:$porthex" 
    } else {
		if(device.deviceNetworkId!=settings.mac) {
    		log.debug "setting device network id to mac"
    		device.deviceNetworkId = settings.mac;
    	}
	}
	poll()
}

private getHostAddress() {
	def ip = settings.ip
	def port = settings.port

	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
	return ip + ":" + port
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}