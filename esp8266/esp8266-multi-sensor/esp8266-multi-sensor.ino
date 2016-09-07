
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <DHT.h>;
#include <ArduinoJson.h>

MDNSResponder mdns;

// Replace with your network credentials
const char* ssid = "SSID";
const char* password = "PASS";

#define DHTPIN 2     // what pin we're connected to
#define SWITCH 13

#define DHTTYPE DHT22   // DHT 22  (AM2302)
DHT dht(DHTPIN, DHTTYPE); //// Initialize DHT sensor for normal 16mhz Arduino

float currentHumidity;  //Stores humidity value
float currentTemperature; //Stores temperature value 
float temp;
float hum;

String response;

ESP8266WebServer server(80);

WiFiClient client;

// Smartthings hub information
IPAddress hubIp(192, 168, 1, 107); // smartthings hub ip
const unsigned int hubPort = 39500; // smartthings hub port

int reportInterval = 10 * 60; //in seconds
int rptCnt;

void setup(void){
 
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.println("");

  pinMode(SWITCH, OUTPUT);
  digitalWrite(SWITCH, LOW);

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  
  if (mdns.begin("esp8266", WiFi.localIP())) {
    Serial.println("MDNS responder started");
  }
  

  server.on("/", [](){ 
    Serial.println("Get Status");
    response = printStatus();
    server.send(200, "application/json", response);
    });

  server.on("/switch/on", [](){ 
    Serial.println("Set Switch On");
    digitalWrite(SWITCH, HIGH);
    response = printStatus();
    server.send(200, "application/json", response);
  });

  server.on("/switch/off", [](){ 
    Serial.println("Set Switch Off");
    digitalWrite(SWITCH, LOW);
    response = printStatus();
    server.send(200, "application/json", response);
  });
  
  server.begin();
  Serial.println("HTTP server started");
}
 
void loop(void){
  server.handleClient();

  if(rptCnt<=0) {
    sendNotify();
    rptCnt=reportInterval;
  }

  if(rptCnt>0) {
    delay(1000);
    rptCnt--;
  }
} 

String printStatus(){

  String str;    

  temp = dht.readTemperature(true);
  hum = dht.readHumidity();

  StaticJsonBuffer<500> jsonBuffer;

  JsonObject& root = jsonBuffer.createObject();

  int switchStatus = 0;

  switchStatus = digitalRead(SWITCH);

  Serial.println("SWITCH STATUS: " + switchStatus);
  

  root["humidity"] = hum;
  root["temperature"] = temp;
  root["switch"] = (switchStatus == 1) ? "on" : "off";
  root["motion"] = "inactive";
  root["lum"] = 50;

  root.printTo(str);

  root.printTo(Serial);
  
  return str;
    
}


int sendNotify() //client function to send/receieve POST data.
{
    
  String str;

  int returnStatus = 1;

  str = printStatus();

  if (temp != currentTemperature || hum != currentHumidity) {

    currentTemperature = temp;
    currentHumidity = hum;
    
    if (client.connect(hubIp, hubPort)) {
       
      client.println(F("POST / HTTP/1.1"));
      client.print(F("HOST: "));
      client.print(hubIp);
      client.print(F(":"));
      client.println(hubPort);
      client.println("Connection: close");
      client.println("Content-Type: application/json;");
      client.print("Content-Length: ");
      client.println(str.length());
      client.println();
      client.println(str);
       
      Serial.println("Sending POST to ST HUB");     
      
    } 
    else {

      Serial.println("Failed to connect to Hub!");
      //connection failed
      returnStatus = 0;
           
    }
  
  }
  else {
    Serial.println("No changes detected");

  }

  // read any data returned from the POST
  while(client.connected() && !client.available()) delay(1); //waits for data
  while (client.connected() || client.available()) { //connected or data available
    char c = client.read();
  }

  delay(1);
  client.stop();
  return returnStatus;
}
