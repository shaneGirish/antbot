void setup() {
  Serial.begin(115200);
  
  LED::setup();
  MOTORS::setup();
  MESSAGES::setup();
  WHEEL_ENCODERS::setup();
  
  messenger.attach(OnUnknownCommand);
  messenger.attach(MESSAGES::SET_SPEEDS, OnSetSpeedsCommand);
  messenger.attach(MESSAGES::STOP, OnStopCommand);
  messenger.attach(MESSAGES::TRANSITION_TO_SPEEDS, OnTransitionToSpeedsCommand);
  
  // Send the status to the PC that says the Arduino has booted
  // messenger.sendCmd(MESSAGES::ACKNOWLEDGE, "Arduino has started!");
}

void OnUnknownCommand() {
  messenger.sendCmd(MESSAGES::ERROR, "Command not recognized");
}

void OnTransitionToSpeedsCommand() {
  MOTORS::transitionToSpeeds(messenger.readInt16Arg(), messenger.readInt16Arg());
}

void OnSetSpeedsCommand() {
  MOTORS::setSpeeds(messenger.readInt16Arg(), messenger.readInt16Arg());
}

void OnStopCommand() {
  MOTORS::stop();
}


