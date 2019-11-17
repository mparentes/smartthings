#include <SmartThingsESP8266WiFi.h>

#include "Sonoff.h";

//******************************************************************************************
// ST_Anything Library 
//******************************************************************************************
#include <Constants.h>       //Constants.h is designed to be modified by the end user to adjust behavior of the ST_Anything library
#include <Device.h>          //Generic Device Class, inherited by Sensor and Executor classes
#include <Sensor.h>          //Generic Sensor Class, typically provides data to ST Cloud (e.g. Temperature, Motion, etc...)
#include <Executor.h>        //Generic Executor Class, typically receives data from ST Cloud (e.g. Switch)
#include <InterruptSensor.h> //Generic Interrupt "Sensor" Class, waits for change of state on digital input 
#include <PollingSensor.h>   //Generic Polling "Sensor" Class, polls Arduino pins periodically
#include <Everything.h>      //Master Brain of ST_Anything library that ties everything together and performs ST Shield communications

#include <PS_Illuminance.h>  //Implements a Polling Sensor (PS) to measure light levels via a photo resistor

#include <PS_TemperatureHumidity.h>  //Implements a Polling Sensor (PS) to measure Temperature and Humidity via DHT library
//#include <PS_DS18B20_Temperature.h>  //Implements a Polling Sesnor (PS) to measure Temperature via DS18B20 libraries 
//#include <PS_Water.h>        //Implements a Polling Sensor (PS) to measure presence of water (i.e. leak detector)
#include <IS_Motion.h>       //Implements an Interrupt Sensor (IS) to detect motion via a PIR sensor
#include <IS_Contact.h>      //Implements an Interrupt Sensor (IS) to monitor the status of a digital input pin
//#include <IS_Smoke.h>        //Implements an Interrupt Sensor (IS) to monitor the status of a digital input pin
//#include <IS_DoorControl.h>  //Implements an Interrupt Sensor (IS) and Executor to monitor the status of a digital input pin and control a digital output pin
#include <IS_Button.h>       //Implements an Interrupt Sensor (IS) to monitor the status of a digital input pin for button presses
#include <EX_Switch.h>       //Implements an Executor (EX) via a digital output to a relay
//#include <EX_Alarm.h>        //Implements Executor (EX)as an Alarm Siren capability via a digital output to a relay
//#include <S_TimedRelay.h>    //Implements a Sensor to control a digital output pin with timing capabilities

//*************************************************************************************************
//NodeMCU v1.0 ESP8266-12e Pin Definitions (makes it much easier as these match the board markings)
//*************************************************************************************************
//#define LED_BUILTIN 16
//#define BUILTIN_LED 16
//
#define D0 16  //no internal pullup resistor
#define D1  5
#define D2  4
#define D3  0  //must not be pulled low during power on/reset, toggles value during boot
#define D4  2  //must not be pulled low during power on/reset, toggles value during boot
#define D5 14
#define D6 12
#define D7 13
#define D8 15  //must not be pulled high during power on/reset

//******************************************************************************************
//Define which Arduino Pins will be used for each device
//******************************************************************************************


#define PIN_SWITCH_1                D6  //Relay
#define PIN_BUTTON_1                D3  //Button
#define PIN_LED_1                   D7  //LED Indicator




//******************************************************************************************
//ESP8266 WiFi Information
//******************************************************************************************
String str_ssid     = "net_iot";                           //  <---You must edit this line!
String str_password = "netiot05101520";                   //  <---You must edit this line!
IPAddress ip(192, 168, 10, 42);         //Device IP Address       //  <---You must edit this line!
IPAddress gateway(192, 168, 10, 1);     //Router gateway          //  <---You must edit this line!
IPAddress subnet(255, 255, 255, 0);     //LAN subnet mask         //  <---You must edit this line!
IPAddress dnsserver(192, 168, 10, 1);   //DNS server              //  <---You must edit this line!
const unsigned int serverPort = 8090;   // port to run the http server on

// Smarthings Hub Information
IPAddress hubIp(192, 168, 10, 20);  // smartthings hub ip       //  <---You must edit this line!
const unsigned int hubPort = 39500; // smartthings hub port

// Hubitat Hub Information
//IPAddress hubIp(192, 168, 1, 143);    // hubitat hub ip         //  <---You must edit this line!
//const unsigned int hubPort = 39501;   // hubitat hub port

//******************************************************************************************
//st::Everything::callOnMsgSend() optional callback routine.  This is a sniffer to monitor 
//    data being sent to ST.  This allows a user to act on data changes locally within the 
//    Arduino sktech.
//******************************************************************************************
void callback(const String &msg)
{
  Serial.print(F("ST_Anything Callback: Sniffed data = "));
  Serial.println(msg);

  #ifdef SONOFF
    if (msg == "switch1 on") LEDon1;
    if (msg == "switch1 off") LEDoff1;
  #endif
  
  #ifdef SONOFF_DUAL
    if (msg == "switch1 on") {
      
    }
    if (msg == "switch1 off") {
      
    }
    if (msg == "switch2 on") {
      
    }
    if (msg == "switch2 off") {
      
    }
  #endif
  
  
  //Masquerade as the ThingShield to send data to the Arduino, as if from the ST Cloud (uncomment and edit following line)
  //st::receiveSmartString("Put your command here!");  //use same strings that the Device Handler would send
}

//******************************************************************************************
//Arduino Setup() routine
//******************************************************************************************
void setup()
{

  //Custom code
  pinMode(PIN_LED_1, OUTPUT);
  
  
  //Executors
  static st::EX_Switch executor1(F("switch1"), PIN_SWITCH_1, LOW, false);  //Inverted logic for "Active Low" Relay Board
  
  //*****************************************************************************
  //  Configure debug print output from each main class 
  //  -Note: Set these to "false" if using Hardware Serial on pins 0 & 1
  //         to prevent communication conflicts with the ST Shield communications
  //*****************************************************************************
  st::Everything::debug=true;
  st::Executor::debug=true;
  st::Device::debug=true;
  st::PollingSensor::debug=true;
  st::InterruptSensor::debug=true;

  //*****************************************************************************
  //Initialize the "Everything" Class
  //*****************************************************************************

  //Initialize the optional local callback routine (safe to comment out if not desired)
  st::Everything::callOnMsgSend = callback;
  
  //Create the SmartThings ESP8266WiFi Communications Object
    //STATIC IP Assignment - Recommended
    st::Everything::SmartThing = new st::SmartThingsESP8266WiFi(str_ssid, str_password, ip, gateway, subnet, dnsserver, serverPort, hubIp, hubPort, st::receiveSmartString);
 
    //DHCP IP Assigment - Must set your router's DHCP server to provice a static IP address for this device's MAC address
    //st::Everything::SmartThing = new st::SmartThingsESP8266WiFi(str_ssid, str_password, serverPort, hubIp, hubPort, st::receiveSmartString);

  //Run the Everything class' init() routine which establishes WiFi communications with SmartThings Hub
  st::Everything::init();
  
  //*****************************************************************************
  //Add each sensor to the "Everything" Class
  //*****************************************************************************
//  st::Everything::addSensor(&sensor1);
      
  //*****************************************************************************
  //Add each executor to the "Everything" Class
  //*****************************************************************************
  st::Everything::addExecutor(&executor1);
    
  //*****************************************************************************
  //Initialize each of the devices which were added to the Everything Class
  //*****************************************************************************
  st::Everything::initDevices();

  
  
}

//******************************************************************************************
//Arduino Loop() routine
//******************************************************************************************
void loop()
{
  //*****************************************************************************
  //Execute the Everything run method which takes care of "Everything"
  //*****************************************************************************
  st::Everything::run();
}
