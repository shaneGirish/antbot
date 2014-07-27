void setup() {
  Serial.begin(115200);
  
  LED::setup();
  MOTORS::setup();
  MESSAGES::setup();
  WHEEL_ENCODERS::setup();
  
  messenger.attach(OnUnknownCommand);
  messenger.attach(MESSAGES::MOTOR, OnMotorCommand);
  messenger.attach(MESSAGES::STOP, OnStopCommand);
  
  messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Arduino has started!"); // Send the status to the PC that says the Arduino has booted
}

void OnUnknownCommand() {
  messenger.sendCmd(MESSAGES::ERROR, "Command not recognized");
}

void OnMotorCommand() {
  MOTORS::setSpeeds(messenger.readFloatArg(), messenger.readFloatArg());
}

void OnStopCommand() {
  MOTORS::setSpeeds(0,0);
}


