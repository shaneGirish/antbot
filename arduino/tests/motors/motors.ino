#include <CmdMessenger.h>
#include <ZumoMotors.h>

#define LED_PIN 13

unsigned long last_led_toggle_timestamp = 0;
bool led_state = 0;

CmdMessenger messenger = CmdMessenger(Serial);
ZumoMotors motors;

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP
  };
}

void setup() {
  Serial.begin(115200);

  // uncomment one or both of the following lines if your motors' directions need to be flipped
  // motors.flipLeftMotor(true);
  // motors.flipRightMotor(true);
  
  messenger.printLfCr(); // Adds newline to every command
  attachCommandCallbacks(); // Attach user-defined callback methods
  messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Arduino has started!"); // Send the status to the PC that says the Arduino has booted

  pinMode(LED_PIN, OUTPUT);
}

void loop() {
  // Process incoming serial data, and perform callbacks
  messenger.feedinSerialData();

  // Toggle LED periodically. If the LED does not toggle every 2000 ms,
  // this means that messenger are taking a longer time than this
  if (hasExpired(last_led_toggle_timestamp,2000)) {
    toggleLED();
  } 
}

void toggleLED() {
  led_state = !led_state;
  digitalWrite(LED_PIN, led_state?HIGH:LOW);
}

void attachCommandCallbacks() {
  messenger.attach(OnUnknownCommand);
  messenger.attach(MESSAGES::MOTOR, OnMotorCommand);
  messenger.attach(MESSAGES::STOP, OnStopCommand);
}

void OnUnknownCommand() {
  messenger.sendCmd(MESSAGES::ERROR, "Command not recognized");
}

void OnMotorCommand() {
  motors.setSpeeds(messenger.readFloatArg(), messenger.readFloatArg());
}

void OnStopCommand() {
  motors.setSpeeds(0,0);
}


// Returns if it has been more than interval (in ms) ago.
bool hasExpired(unsigned long &prevTime, unsigned long interval) {
  if ( millis() - prevTime > interval ) {
    prevTime = millis();
    return true;
  } else {
    return false;
  }
}
