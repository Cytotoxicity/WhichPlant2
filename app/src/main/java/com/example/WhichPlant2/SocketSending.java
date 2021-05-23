package com.example.WhichPlant2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketSending extends AppCompatActivity {


    Button connect_btn;                 // ip 받아오는 버튼

    EditText ip_edit;               // ip 에디트
    TextView show_text;             // 서버에서온거 보여주는 에디트
    // 소켓통신에 필요한것

    private static int BUF_SIZE = 100;
    private String html = "";
    private Handler mHandler;

    private Socket socket;

    private DataOutputStream dos;
    private DataInputStream dis;
    private ByteArrayOutputStream bos;
    private Bitmap bitmap;
    private int exifDegree;

    private String ip = "127.0.0.1";            // IP 번호(유선 통신, 로컬호스트)
    private int port = 8080;                          // port 번호

    private boolean background_omit;
    private String plant_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //MainActivity에서 사진 받아옴
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        Bundle extras = getIntent().getExtras();

        final String filepath = extras.getString("filepath");
        exifDegree = extras.getInt("exifDegree");
        File imgFile = new File(filepath);
        bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ((ImageView) findViewById(R.id.image_cam)).setImageBitmap(rotate(bitmap, exifDegree));

        final CheckBox checkbox = (CheckBox) findViewById(R.id.ckb_background_omit);
        checkbox.setChecked(false);
        checkbox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkbox.isChecked()) background_omit = true;
                else background_omit = false;
            }
        });

        Button btn_connection = (Button) findViewById(R.id.btn_connection);
        btn_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(view.getId() == R.id.btn_connection) connect();
            }
        });

        Button btn_gto_res = (Button) findViewById(R.id.btn_gto_res);
        btn_gto_res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), Result.class);
                intent.putExtra("plant_name", plant_name);
                intent.putExtra("filepath", filepath);
                intent.putExtra("exifDegree", exifDegree);
                startActivity(intent);
            }
        });

    };

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    // 로그인 정보 db에 넣어주고 연결시켜야 함.
    void connect(){

        mHandler = new Handler();
        Log.w("connect","연결 하는중");
        // 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
                // 서버 접속
                try {
                    socket = new Socket(ip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output
                    dis = new DataInputStream(socket.getInputStream());     // input
                    bos = new ByteArrayOutputStream();

                    Bitmap rotated_bitmap = rotate(bitmap, exifDegree);
                    Bitmap scaled_bitmap = Bitmap.createScaledBitmap(rotated_bitmap, rotated_bitmap.getWidth()/4, rotated_bitmap.getHeight()/4, true);
                    scaled_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    byte[] array = bos.toByteArray();
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    //dos.writeInt(scaled_bitmap.getWidth());
                    //dos.writeInt(scaled_bitmap.getHeight());
                    dos.writeBoolean(background_omit);
                    dos.write(array);
                    dos.flush();

                    //dos.writeUTF("안드로이드에서 서버로 연결요청");



                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼","버퍼생성 잘됨");


                while(true) {
                    try {
                        byte[] buf = new byte[BUF_SIZE];
                        int read_Byte = dis.read(buf);
                        plant_name = new String(buf, 0, read_Byte);
                        Log.w("서버에서 받아온 값 ",""+ plant_name);
                        if (read_Byte > 0) {
                            break;
                        }

                        Thread.sleep(2);
                    } catch (IOException | InterruptedException e) {
                        Log.w("String 오류남", "확인해보슈");
                    }
                }
            }
        };
        // 소켓 접속 시도, 버퍼생성
        checkUpdate.start();
    }

}