#define LED_PIN 13

namespace LED {
  unsigned long last_toggle_timestamp = 0;
  bool state = 0;
  
  void setup() {
    pinMode(LED_PIN, OUTPUT);
  }
  
  void toggle() {
    last_toggle_timestamp = millis();
    state = !state;
    digitalWrite(LED_PIN, state?HIGH:LOW);
  }
}
