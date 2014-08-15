#include <ZumoMotors.h>

namespace MOTORS {
  ZumoMotors controller;
  
  int leftSpeed = 0;
  int rightSpeed = 0;
  
  void setup() {
    // uncomment one or both of the following lines if your motors' directions need to be flipped
    // controller.flipLeftMotor(true);
    // controller.flipRightMotor(true);
  }
  
  void setSpeeds(int left, int right) {
    leftSpeed = left;
    rightSpeed = right;
    controller.setSpeeds(left, right);
  }
  
  void transitionToSpeeds(int left, int right) {
    int deltaLeft = leftSpeed - left;
    int deltaRight = rightSpeed - right;
    
    int absLeft = abs(deltaLeft);
    int absRight = abs(deltaRight);
    
    int speed = absLeft > absRight ? absLeft : absRight;
    
    for(int i = 0 ; i < speed ; ++i) {
      if(i < absLeft) {
        if(deltaLeft < 0) {
          ++deltaLeft;
          ++leftSpeed;
        } else if(deltaLeft > 0) {
          --deltaLeft;
          --leftSpeed;
        }
      }
      
      if(i < absRight) {
        if(deltaRight < 0) {
          ++deltaRight;
          ++rightSpeed;          
        } else if(deltaRight > 0) {
          --deltaRight;
          --rightSpeed;          
        }
      }
      
      controller.setSpeeds(leftSpeed, rightSpeed);
      
      delay(2);
    }
}
  
  void stop() {
    // Slow halt.
    transitionToSpeeds(0,0);
  }
}


