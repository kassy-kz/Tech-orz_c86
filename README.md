Tech-orz_c86
============

***

# これなに

コミックマーケットC86 ３日目西か41b Tech-orzの頒布する同人誌
Tech-orz 2014 SUMMER 第一部で紹介したガジェットの設計とソースコードです。

# 動作環境

以下の環境で動作を確認しています。

## 開発環境

* Mac OS X 10.9
* Android Studio 0.8.0
* Arduino 1.0.2

## 動作環境

* Nexus 5
* Arduino MEGA ADK


# 動かし方

## タグ
同人誌の各節とtagが対応しています。
（例）3.3節はsection_3_3のタグが対応

## 外部ライブラリ
本プロジェクトは以下の外部ライブラリを使用しています。
動作させるためには各サイトからダウンロードして組み込んでください。


### Arduino AquesTalk Library

[http://www.a-quest.com/download/package/Arduino_AquesTalk_Library.zip](http://www.a-quest.com/download/package/Arduino_AquesTalk_Library.zip)

ダウンロードして解凍し、
(Arduinoのインストールディレクトリ)/library/
配下に置きます。

### AqKanji2Koe for Android

[http://a-quest.com/download/index.html](http://a-quest.com/download/index.html)

上記サイトから評価版をダウンロードできます。
継続使用する場合には製品版を申請する必要があります。（個人利用なら無償、ライセンス発行手数料1800円）

ダウンロードして解凍し、中のlibAqKanji2Koe.soを
C86_Android/app/src/main/jniLibs/armeabi/
配下に

aq_dic.zipを
C86_Android/app/src/main/assets/
配下に置きます。



