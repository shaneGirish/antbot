#include <CmdMessenger.h>

CmdMessenger messenger = CmdMessenger(Serial);

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP,
    LEFT_WHEEL,
    RIGHT_WHEEL,
    ANDROID_TEST
  };
  
  void setup() {
    messenger.printLfCr();
  }
}



