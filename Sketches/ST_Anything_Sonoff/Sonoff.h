#define SONOFF
//#define SONOFF_DUAL


#define LEDoff1 digitalWrite(PIN_LED_1,HIGH)
#define LEDon1 digitalWrite(PIN_LED_1,LOW)


#if defined SONOFF
#define Relayoff1 {\
  digitalWrite(PIN_SWITCH_1,LOW); \
  LEDoff1; \
}
#define Relayon1 {\
  digitalWrite(PIN_SWITCH_1,HIGH); \
  LEDon1; \
}
#elif defined SONOFF_DUAL
#define Relayoff1 {\
  byte byteValue; \
  
  if (Settings.currentState2) byteValue = 0x02; \
  else byteValue = 0x00; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState1 = false; \
}
#define Relayon1 {\
  byte byteValue; \
  if (!Settings.currentState1) needUpdate1 = true; \
  if (Settings.currentState2) byteValue = 0x03; \
  else byteValue = 0x01; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState1 = true; \
}
#define Relayoff2 {\
  byte byteValue; \
  if (Settings.currentState2) needUpdate2 = true; \
  if (Settings.currentState1) byteValue = 0x01; \
  else byteValue = 0x00; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState2 = false; \
}
#define Relayon2 {\
  byte byteValue; \
  if (!Settings.currentState2) needUpdate2 = true; \
  if (Settings.currentState1) byteValue = 0x03; \
  else byteValue = 0x02; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState2 = true; \
}
#endif
