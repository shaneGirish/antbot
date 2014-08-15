void setup() {
  Serial.begin(115200);
  
  LED::setup();
  MOTORS::setup();
  MESSAGES::setup();
  
  messenger.attach(OnUnknownCommand);
  messenger.attach(MESSAGES::MOTOR, OnMotorCommand);
  messenger.attach(MESSAGES::STOP, OnStopCommand);
  messenger.attach(MESSAGES::ANDROID_TEST, OnTestCommand);
  
  // Send the status to the PC that says the Arduino has booted
  // messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Arduino has started!");
}

void OnUnknownCommand() {
  messenger.sendCmd(MESSAGES::ERROR, "Command not recognized");
}

void OnTestCommand() {
  messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Android Test Ack");
}

void OnMotorCommand() {
  MOTORS::setSpeeds(messenger.readInt16Arg(), messenger.readInt16Arg());
  // messenger.sendCmdStart(MESSAGES::ACKNOWLEDGE);
  // messenger.sendCmdArg("Motor Speeds : ");
  // messenger.sendCmdArg(l);
  // messenger.sendCmdArg(r);
  // messenger.sendCmdEnd();
}

void OnStopCommand() {
  MOTORS::stop();
}


