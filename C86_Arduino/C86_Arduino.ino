#include <AquesTalk.h>
#include <Wire.h> 

AquesTalk atp; 

void setup(){
}

void loop(){
  // こんにちは と発声（無限ループ）
  atp.Synthe("konnnichiwa.");  
}


