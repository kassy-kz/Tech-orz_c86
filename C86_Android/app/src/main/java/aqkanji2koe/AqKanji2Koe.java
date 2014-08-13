package aqkanji2koe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AqKanji2Koe {
    static {
        Log.i("AqKanji2Koe", "load library AqKanji2Koe.so");
        System.loadLibrary("AqKanji2Koe");
    }

    public static String conv(String dirDic, String kanjiStr) {
        return new AqKanji2Koe().Convert(dirDic, kanjiStr);
    }

    public synchronized native String Convert(String dirDic, String kanjiStr);


    /**
     * 初回起動時に辞書データを展開する
     * 別スレッドで処理を行う。
     * /data/data/<app>/files/copyed.datが存在しなかったら
     * /assets/aq_dic.zip を /data/data/<app>/files/に ファイルを展開
     * AqKanji2Koeのサンプルコードを流用（Aquest社許諾済）
     * @param context
     */
    public static void copyDic(final Context context){
        final ProgressDialog dialog = new ProgressDialog(context);
        try {
            // すでに展開済みか
            String filepath = context.getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
            File file = new File(filepath);
            boolean isExists = file.exists();

            // 展開済みでなかったら（初期起動時）
            if(!isExists){
                initDialog(context, dialog);
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            AssetManager am  = context.getResources().getAssets();
                            InputStream is  = am.open("aq_dic.zip", AssetManager.ACCESS_STREAMING);
                            ZipInputStream zis = new ZipInputStream(is);
                            ZipEntry ze  = zis.getNextEntry();

                            int totalSize=0;
                            for(;ze != null;) {
                                String path = context.getFilesDir().toString() + "/" + ze.getName();
                                FileOutputStream fos = new FileOutputStream(path, false);
                                byte[] buf = new byte[8192];
                                int size = 0;
                                int posLast=0;
                                while ((size = zis.read(buf, 0, buf.length)) > -1) {
                                    fos.write(buf, 0, size);
                                    totalSize += size;
                                    int pos = totalSize*100/27220452+1;
                                    if(posLast!=pos){
                                        dialog.setProgress(pos);
                                        posLast=pos;
                                    }
                                }
                                fos.close();
                                zis.closeEntry();
                                ze  = zis.getNextEntry();
                            }
                            zis.close();

                            // コピー完了のマークとして、copyed.datを作成
                            {
                                String filepath = context.getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
                                FileOutputStream fos = new FileOutputStream(filepath, false);
                                byte[] buf = new byte[1];
                                buf[0]='*';
                                fos.write(buf, 0, 1);
                                fos.close();
                            }

                        } catch(Exception e){
                            e.printStackTrace();

                        }
                        dialog.dismiss();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 辞書コピー中を示すダイアログを表示
     */
    private static void initDialog(Context context, ProgressDialog dialog) {
        dialog.setTitle("初回起動初期化中");
        dialog.setMessage("数分かかることがあります");
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.show();
    }


}
