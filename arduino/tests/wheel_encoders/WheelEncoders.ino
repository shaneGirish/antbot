#include <PinChangeInt.h>

#define sensor1a A0
#define sensor1b A1
#define sensor2a A2
#define sensor2b A3

int sensor1a_value = 0;
int sensor1b_value = 0;

int sensor2a_value = 0;
int sensor2b_value = 0;

int isPinHigh(int pin) {
  return analogRead(pin) > 1000;
}

void listener() {
  int new_sensorA, new_sensorB, positive, negative, command, *sensorA, *sensorB;
  char* errorString;
  
  if(PCintPort::arduinoPin == sensor1a || PCintPort::arduinoPin == sensor1b) {
    new_sensorA = isPinHigh(sensor1a);
    new_sensorB = isPinHigh(sensor1b);
    sensorA = &sensor1a_value;
    sensorB = &sensor1b_value;
    
    command = MESSAGES::LEFT_WHEEL_DATA;
    errorString = "WheelEncoder sensor 1 error.";
  } else {
    new_sensorA = isPinHigh(sensor2a);
    new_sensorB = isPinHigh(sensor2b);
    sensorA = &sensor2a_value;
    sensorB = &sensor2b_value;
    
    command = MESSAGES::RIGHT_WHEEL_DATA;
    errorString = "WheelEncoder sensor 2 error.";
  }
  
  positive = new_sensorA ^ *sensorB;
  negative = new_sensorB ^ *sensorA;

  if(positive) {
    messenger.sendCmd(command, 1);
  }
  if(negative) {
    messenger.sendCmd(command, -1);
  }

  if(new_sensorA != *sensorA && new_sensorB != *sensorB) {
    messenger.sendCmd(MESSAGES::ERROR, errorString);
  }

  *sensorA = new_sensorA;
  *sensorB = new_sensorB;
};

namespace WHEEL_ENCODERS {  
  void setup() {
    pinMode(sensor1a, INPUT);
    pinMode(sensor1b, INPUT);
    pinMode(sensor2a, INPUT);
    pinMode(sensor2b, INPUT);
    
    sensor1a_value = isPinHigh(sensor1a);
    sensor1b_value = isPinHigh(sensor1b);
    sensor2a_value = isPinHigh(sensor2a);
    sensor2b_value = isPinHigh(sensor2b);
    
    PCintPort::attachInterrupt(sensor1a, &listener, CHANGE);
    PCintPort::attachInterrupt(sensor1b, &listener, CHANGE);
    PCintPort::attachInterrupt(sensor2a, &listener, CHANGE);
    PCintPort::attachInterrupt(sensor2b, &listener, CHANGE);
  }
}
