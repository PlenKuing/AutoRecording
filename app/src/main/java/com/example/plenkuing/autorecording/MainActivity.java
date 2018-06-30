package com.example.plenkuing.autorecording;


import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button record,stop;
    //音频文件
    File soundFile;
    MediaRecorder mediaRecorder;
    //状态码
    boolean status=false;
    //电话管理器
    TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取界面中的两个按钮
        record = findViewById(R.id.record);
        stop = findViewById(R.id.stop);
        record.setOnClickListener(this);
        stop.setOnClickListener(this);

        //实现摇动录音
        SensorManagerHelper sensorHelper = new SensorManagerHelper(this);
        sensorHelper.setOnShakeListener(new SensorManagerHelper.OnShakeListener() {

            @Override
            public void onShake() {
                // TODO Auto-generated method stub
                if (!status) {//如果此时没有进入录音，摇动后将进入录音  此处添加录音代码
                     Toast.makeText(MainActivity.this, "你在摇哦，录音开始", Toast.LENGTH_SHORT).show();
                     //启用录音功能
                    record();
                     //改变状态值
                    status=!status;
                }else{//此时正在录音，摇动后停止录音 此处结束录音
                    Toast.makeText(MainActivity.this, "你又摇了，录音结束", Toast.LENGTH_SHORT).show();
                    //停止录音功能
                    stop();
                    //改变状态值
                    status=!status;
                }
            }
        });


        //实现监听录音
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //创建通话状态监听器
        PhoneStateListener listener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String number) {
                switch (state){
                    case TelephonyManager.CALL_STATE_IDLE://空闲状态
                        stop();
                        Log.v("record","空闲中");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                        Log.v("record","电话来了");
                        record();
                        break;
                    //来电铃响 进行录音
                    case TelephonyManager.CALL_STATE_RINGING://铃响状态
                        Log.v("record","铃响了");
                        break;
                    default:
                        break;
                }
                super.onCallStateChanged(state, number);
            }
        };

        //监听电话状态的改变
        telephonyManager.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);

}

    @Override
    protected void onDestroy() {
        if(soundFile!=null && soundFile.exists()){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder=null;
        }
        super.onDestroy();
    }

    //进行录音函数 代码复用
    public void record(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(MainActivity.this,"SD卡不存在，请插入SD卡！",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String time =String.valueOf(new Date().getTime());
            soundFile = new File(Environment.getExternalStorageDirectory().getCanonicalFile()+"/"+time+"sound1.amr");
            mediaRecorder = new MediaRecorder();
            //设置录音的声音来源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置录音的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //设置声音的编码
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(soundFile.getAbsolutePath());
            Log.v("record",String.valueOf(soundFile.length()));
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(MainActivity.this,"开始录音",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //停止录音函数 代码复用
    public  void stop(){
        if(soundFile!=null && soundFile.exists()){
            //停止录音
            mediaRecorder.stop();
            Log.v("record",String.valueOf(soundFile.length()));
            Toast.makeText(MainActivity.this,"录音结束",Toast.LENGTH_SHORT).show();
            //释放资源
            mediaRecorder.release();
            mediaRecorder=null;
        }

    }

    //设置按钮的点击事件，开启和停止录音
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.record:
                record();
                break;
            case R.id.stop:
                stop();
                break;
        }
    }
}
