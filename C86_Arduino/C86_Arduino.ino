#include <AquesTalk.h>
#include <Wire.h> 
#include <SFE_TSL2561.h>

AquesTalk atp; 

// BLESerialの受信データ
int rcvData = 0;
int sendCount = 0;

// 照度センサー用のパラメータ
SFE_TSL2561 light;
boolean gain;     // Gain setting, 0 = X1, 1 = X16;
unsigned int ms;  // Integration ("shutter") time in milliseconds
boolean isDayTime = true; // 昼か夜かのフラグ

// 洗濯タイマー（TWE-LITE）
boolean isTimerOn = false;
unsigned long time;
const int TWE=22;

/**
 * メイン関数１ setup
 **/
void setup(){
  // デバッグシリアル
  Serial.begin(9600);

  // for BLESerial
  // Serial2を使用する
  pinMode(16, OUTPUT);
  // シリアル通信セットアップ
  Serial2.begin(9600);
  
  // 照度センサー（TSL2561）
  light.begin();
  unsigned char ID;
  if (light.getID(ID)) {
  } else {
    byte error = light.getError();
  }
  unsigned char time = 2;
  light.setTiming(gain,time,ms);
  light.setPowerUp();

  // 洗濯タイマー用input(TWE=LITE)
  pinMode(TWE, INPUT); 

}


/**
 * メイン関数2 loop
 **/
void loop(){
  
  // BLESerial受信処理（分割結合あり）
  char buf[127];
  if(Serial2.available() > 0){
    // シリアルから読み取り
    rcvData = Serial2.readBytesUntil('¥0', buf, 127);
    buf[(int)rcvData] = 0;
    Serial.println("BLESerial receive ");
    Serial.println(buf);

    // CONNECTを検出（スマホとの接続を検知）
    String str = String(buf);
    if(str.equals("CONNECT\n")) {
      // おかえりなさいを言ってもらう
      atp.Synthe("okaerinasai");
    } 
    else {
      // 受信したのはローマ字のはずなので、そのままAquestalkに喋らせる
      atp.Synthe(buf);    
    }

    // BLESerialで返答を送信する
    sendCount++;
    Serial2.write("success receive "+ sendCount);
  }
  
  
    // 照度センサー（TSL2561）
  unsigned int data0, data1;
  if (light.getData(data0,data1)) {
    double lux;    // Resulting lux value
    boolean good;  // True if neither sensor is saturated
    good = light.getLux(gain,ms,data0,data1,lux);

    // 昼夜を設定する
    // 昼の場合
    if(isDayTime) {
      // 夜になった
      if(lux < 30) {
        isDayTime = false;
        atp.Synthe("oyasuminasai.");  
      }
    // 夜の場合
    } else {
      // 昼になった
      if(lux > 30) {
        isDayTime = true;
        atp.Synthe("ohayo-gozaima_su.");
      }      
    }

  } else {
    byte error = light.getError();
  }
  
  
  // 洗濯タイマー（TWE-LITE）
  if(digitalRead(TWE)) {
    // スイッチ話された
    Serial.println("dip 1");
  } else {
    // スイッチ押された
    Serial.println("dip 0");
    if(isTimerOn == false) {
      Serial.println("timer start");
      time = millis();
      Serial.println(time);
      isTimerOn = true;
    } 
  }

  if(isTimerOn) {
    unsigned long cur;
    cur = millis();
    if(cur > time + 2000 ) {
       Serial.println("setaku-complete");
       atp.Synthe("sentakuga,owa'rimashita.");          
       isTimerOn = false;
    }
  }

}


