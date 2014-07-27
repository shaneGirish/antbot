#include <CmdMessenger.h>
#include <ZumoMotors.h>
#include <PololuWheelEncoders.h>
#include <PinChangeInt.h>

#define LED_PIN 13

unsigned long last_led_toggle_timestamp = 0;
bool led_state = 0;

CmdMessenger messenger = CmdMessenger(Serial);
ZumoMotors motors;
PololuWheelEncoders wheel_encoders;

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP,
    WHEEL_ENCODER_DATA
  };
}

void setup() {
  Serial.begin(115200);
  
  wheel_encoders.init(0, 1, 2, 3);

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
  
  messenger.sendCmdStart(MESSAGES::WHEEL_ENCODER_DATA);
  messenger.sendCmdArg(analogRead(0));
  messenger.sendCmdArg(analogRead(1));
  messenger.sendCmdArg(analogRead(2));
  messenger.sendCmdArg(analogRead(3));
  messenger.sendCmdEnd();
  
  // delay(50);
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
  
  // messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Motor speeds updated");
  
  // messenger.sendCmd(kFloatAdditionResult,a + b);
  // or
  // messenger.sendCmdStart(kFloatAdditionResult);
  // messenger.sendCmdArg(a+b);
  // messenger.sendCmdArg(a-b);
  // messenger.sendCmdEnd();
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
