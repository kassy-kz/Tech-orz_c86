#include <AquesTalk.h>
#include <Wire.h> 
#include <SFE_TSL2561.h>

// EasyVR関連
#if defined(ARDUINO) && ARDUINO >= 100
  #include "Arduino.h"
  #include "Platform.h"
  #include "SoftwareSerial.h"
#ifndef CDC_ENABLED
  // Shield Jumper on SW
  SoftwareSerial port(12,13);
#else
  // Shield Jumper on HW (for Leonardo)
  #define port Serial1
#endif
#else // Arduino 0022 - use modified NewSoftSerial
  #include "WProgram.h"
  #include "NewSoftSerial.h"
  NewSoftSerial port(12,13);
#endif
#include "EasyVR.h"

// AquesTalk
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

// 赤外線リモコン検知
const int IR_OUT1=9;
const int IR_OUT2=8;


// EasyVR（音声認識）関連
EasyVR easyvr(port);
//Groups and Commands
enum Groups {
  GROUP_0  = 0,
  GROUP_1  = 1,
};

enum Group0 {
  G0_TANIGAWA = 0,
};

enum Group1 {
  G1_KONNICHIHA = 0,
  G1_OHAYO = 1,
  G1_TASUKETE = 2,
  G1_TSUKARETA = 3,
  G1_KAWAIINE = 4,
};

EasyVRBridge bridge;
int8_t group, idx;


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

  // EasyVR関連
  #ifndef CDC_ENABLED
  // bridge mode?
  if (bridge.check())
  {
    cli();
    bridge.loop(0, 1, 12, 13);
  }
  // run normally
  Serial.begin(9600);
  Serial.println("Bridge not started!");
#else
  // bridge mode?
  if (bridge.check())
  {
    port.begin(9600);
    bridge.loop(port);
  }
  Serial.println("Bridge connection aborted!");
#endif
  port.begin(9600);

  while (!easyvr.detect())
  {
    Serial.println("EasyVR not detected!");
    delay(1000);
  }

  easyvr.setPinOutput(EasyVR::IO1, LOW);
  Serial.println("EasyVR detected!");
  easyvr.setTimeout(1);
  easyvr.setLanguage(0);

  group = EasyVR::TRIGGER; //<-- start group (customize)

}

void action();


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
    
    // 赤外線リモコン検知
    pinMode(IR_OUT1, INPUT); 
    pinMode(IR_OUT2, INPUT); 
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
//    Serial.println("dip 1");
  } else {
    // スイッチ押された
//    Serial.println("dip 0");
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
  
  // 赤外線リモコン検知
  int val_ir1 = 0;
  int val_ir2 = 0;
  // 赤外線
  val_ir1 = digitalRead(IR_OUT1);  // 入力ピンを読む
  val_ir2 = digitalRead(IR_OUT2);  // 入力ピンを読む
  //delay(300);
//  Serial.print("IR1:");
//  Serial.println(val_ir1);
//  Serial.print("IR2:");
//  Serial.println(val_ir2);
  
  
  // EasyVR関連
  easyvr.setPinOutput(EasyVR::IO1, HIGH); // LED on (listening)

  Serial.print("Say a command in Group ");
  Serial.println(group);
  easyvr.recognizeCommand(group);

  do {
    // can do some processing while waiting for a spoken command
  }
  while (!easyvr.hasFinished());
  
  easyvr.setPinOutput(EasyVR::IO1, LOW); // LED off

  idx = easyvr.getWord();
  if (idx >= 0) {
    // built-in trigger (ROBOT)
    // group = GROUP_X; <-- jump to another group X
    return;
  }
  idx = easyvr.getCommand();
  if (idx >= 0) {
    // print debug message
    uint8_t train = 0;
    char name[32];
    Serial.print("Command: ");
    Serial.print(idx);
    if (easyvr.dumpCommand(group, idx, name, train)) {
      Serial.print(" = ");
      Serial.println(name);
    }
    else
      Serial.println();
    easyvr.playSound(0, EasyVR::VOL_FULL);
    // perform some action
    action();
  }
  else { // errors or timeout
    if (easyvr.isTimeout())
      Serial.println("Timed out, try again...");
    int16_t err = easyvr.getError();
    if (err >= 0) {
      // マイクに音が入ったけど学習済み単語と合致しなかった場合
      if(group == GROUP_1) {
        atp.Synthe("e'e.so'ude_sune.");
        group = GROUP_0;
      }
      Serial.print("Error ");
      Serial.println(err, HEX);
    }
  }

}


void action() {
    switch (group) {
    case GROUP_0:
      switch (idx) {
      case G0_TANIGAWA:
        // write your action code here
        // group = GROUP_X; <-- or jump to another group X for composite commands
        group = GROUP_1;
        break;
      }
      break;
    case GROUP_1:
      switch (idx) {
      case G1_KONNICHIHA:
        // こんにちはと返す
        atp.Synthe("konnichiwa");
        // 初期フェーズへ
        group = GROUP_0;
        break;
      case G1_OHAYO:
        // おはようございますと返す
        atp.Synthe("ohayo-gozaima_su");
        // 初期フェーズへ
        group = GROUP_0;
        break;
      case G1_TASUKETE:
        // いかがなさいましたと返す
        atp.Synthe("ikaganasaimasita.");
        // 初期フェーズへ
        group = GROUP_0;
        break;
      case G1_TSUKARETA:
        // お疲れ様です、と返す
        atp.Synthe("otsukaresamade_su");
        // 初期フェーズへ
        group = GROUP_0;
        break;
      case G1_KAWAIINE:
        // ありがとうございますと返す
        atp.Synthe("arigato-gozaima_su");
        // 初期フェーズへ
        group = GROUP_0;
        break;
      }
      break;
    }
}
