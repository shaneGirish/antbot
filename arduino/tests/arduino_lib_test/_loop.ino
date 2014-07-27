void loop() {
  // Process incoming serial data, and perform callbacks
  messenger.feedinSerialData();

  // Toggle LED periodically. If the LED does not toggle every 2000 ms,
  // this means that messenger are taking a longer time than this
  if ((millis() - LED::last_toggle_timestamp) > 500) {
    LED::toggle();
  }
  
  //messenger.sendCmd(MESSAGES::ACKNOWLEDGE, analogRead(A3));
}
