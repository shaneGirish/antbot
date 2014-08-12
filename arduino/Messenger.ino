#include <CmdMessenger.h>

CmdMessenger messenger = CmdMessenger(Serial);

namespace MESSAGES {
  enum {
    ACKNOWLEDGE,
    ERROR,
    SET_SPEEDS,
    TRANSITION_TO_SPEEDS,
    STOP,
    LEFT_WHEEL_DATA,
    RIGHT_WHEEL_DATA
  };
  
  void setup() {
    messenger.printLfCr();
  }
}



