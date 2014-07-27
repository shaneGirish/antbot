#include <ZumoMotors.h>

namespace MOTORS {
  ZumoMotors controller;
  
  float leftSpeed = 0;
  float rightSpeed = 0;
  
  void setup() {
    // uncomment one or both of the following lines if your motors' directions need to be flipped
    // controller.flipLeftMotor(true);
    // controller.flipRightMotor(true);
  }
  
  void setSpeeds(float left, float right) {
    leftSpeed = left;
    rightSpeed = right;
    controller.setSpeeds(left, right);
  }
  
  void stop() {
    // Slow halt.
  }
}


