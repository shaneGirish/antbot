#include <CmdMessenger.h>

CmdMessenger messenger = CmdMessenger(Serial);

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP,
    ANDROID_TEST
  };
  
  void setup() {
    messenger.printLfCr();
  }
}



