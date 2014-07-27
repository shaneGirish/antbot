#include <CmdMessenger.h>

CmdMessenger messenger = CmdMessenger(Serial);

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP,
    LEFT_WHEEL,
    RIGHT_WHEEL
  };
  
  void setup() {
    messenger.printLfCr();
  }
}



