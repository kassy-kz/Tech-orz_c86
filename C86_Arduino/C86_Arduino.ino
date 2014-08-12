#include <AquesTalk.h>
#include <Wire.h> 

AquesTalk atp; 

// BLESerialの受信データ
int rcvData = 0;


void setup(){
  // デバッグシリアル
  Serial.begin(9600);

  // for BLESerial
  // Serial2を使用する
  pinMode(16, OUTPUT);
  // シリアル通信セットアップ
  Serial2.begin(9600);

}

void loop(){
  // こんにちは と発声（無限ループ）
  atp.Synthe("konnnichiwa.");  
  
  // BLESerial受信処理
  char buf[20];
  if(Serial2.available() > 0){
    // シリアルから読み取り
    rcvData = Serial2.readBytes(buf, 20);
    Serial.print("BLESerial receive ");
    Serial.println(buf);

    // BLESerialで返答を送信する
    Serial2.write("success receive");
  }
}


