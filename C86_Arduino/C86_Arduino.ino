#include <AquesTalk.h>
#include <Wire.h> 

AquesTalk atp; 

// BLESerialの受信データ
int rcvData = 0;
int sendCount = 0;

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
  
  // BLESerial受信処理（分割結合あり）
  char buf[127];
  if(Serial2.available() > 0){
    // シリアルから読み取り
    rcvData = Serial2.readBytesUntil('¥0', buf, 127);
    buf[(int)rcvData] = 0;
    Serial.println("BLESerial receive ");
    Serial.println(buf);

    // 受信したのはローマ字のはずなので、そのままAquestalkに喋らせる
    atp.Synthe(buf);    

    // BLESerialで返答を送信する
    sendCount++;
    Serial2.write("success receive "+ sendCount);
  }
}


