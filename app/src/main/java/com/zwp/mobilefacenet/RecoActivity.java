package com.zwp.mobilefacenet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zwp.mobilefacenet.faceantispoofing.FaceAntiSpoofing;
import com.zwp.mobilefacenet.mobilefacenet.MobileFaceNet;
import com.zwp.mobilefacenet.mtcnn.Box;
import com.zwp.mobilefacenet.mtcnn.MTCNN;
import com.zwp.mobilefacenet.mtcnn.Utils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

public class RecoActivity extends AppCompatActivity{

    private MTCNN mtcnn; // Face Detection
    private FaceAntiSpoofing fas; // Live detection
    private MobileFaceNet mfn; // Face matching

    public static Bitmap bitmap1;
    private Bitmap bitmapCrop1;


    private ImageButton imageButton1;

    private ImageView imageViewCrop1;

    private TextView resultTextView;
    private TextView resultTextView2;


    // para el boton hacia atras
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reconocimiento");
        getSupportActionBar().setSubtitle("Control de Acceso");



        imageButton1 = findViewById(R.id.image_button_reco);
        imageViewCrop1 = findViewById(R.id.image_view_crop_reco);
        Button cropBtn = findViewById(R.id.crop_btn_reco);
        Button compareBtn = findViewById(R.id.compare_btn_reco);
        resultTextView = findViewById(R.id.result_text_view_reco);
        resultTextView2 = findViewById(R.id.result_text_view_reco2);


        try {
            mtcnn = new MTCNN(getAssets());
            fas = new FaceAntiSpoofing(getAssets());
            mfn = new MobileFaceNet(getAssets());
        } catch (IOException e) {
            e.printStackTrace();
        }

        initCamera();
        cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faceCrop();
            }
        });
        compareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faceCompare();
            }
        });
    }

    /**
     * Face detection and cropping
     */
    private void faceCrop() {
        if (bitmap1 == null) {
            Toast.makeText(this, "Por favor tomate dos fotos", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap bitmapTemp1 = bitmap1.copy(bitmap1.getConfig(), false);


        // 检测出人脸数据
        Vector<Box> boxes1 = null;

        // 如果没检测到人脸会抛出异常
        try {
            long start = System.currentTimeMillis();
            boxes1 = mtcnn.detectFaces(bitmapTemp1, bitmapTemp1.getWidth() / 5); // 只有这句代码检测人脸，下面都是根据Box在图片中裁减出人脸
            long end = System.currentTimeMillis();
            resultTextView.setText("Tiempo de detección：" + (end - start*1.0)/1000 + " Seg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (boxes1 == null) {
            Toast.makeText(RecoActivity.this, "No se detectaron rostros", Toast.LENGTH_LONG).show();
            return;
        }
        // Because there is only one face in each photo used here, the first value is used to crop the face
        Box box1 = boxes1.get(0);

        // increase margin
        Rect rect1 = box1.transform2Rect();
        MyUtil.rectExtend(bitmapTemp1, rect1);

        // Crop face
        bitmapCrop1 = MyUtil.crop(bitmapTemp1, rect1);

        // Draw face frame and five points
        //Utils.drawBox(bitmapTemp1, box1, 10);
        //Utils.drawBox(bitmapTemp2, box2, 10);

//        bitmapCrop2 = MyUtil.readFromAssets(this, "1.png");
        imageViewCrop1.setImageBitmap(bitmapCrop1);

    }

    /**
     * Face matching
     */
    private void faceCompare() {
        if (bitmapCrop1 == null) {
            Toast.makeText(this, "Por favor detectar rostros primero", Toast.LENGTH_LONG).show();
            return;
        }

        long start = System.currentTimeMillis();

        float[][] embeddings12 = mfn.getEmedding(bitmapCrop1, bitmapCrop1);

        float[] embedding1 = embeddings12[0];

        Log.d("RECO", "EMBEDDING 1: " + Arrays.toString(embedding1));
        long end = System.currentTimeMillis();

        String textTime = "Tiempo de Procesamiento: " + (end - start*1.0)/1000 + " Seg";
        resultTextView.setText(textTime);


        String text = "Embedding：[" + Array.get(embedding1, 0) + "," +
                Array.get(embedding1, 1) + "," + Array.get(embedding1, 2) + "...]";
        resultTextView2.setText(text);

    }

    /*********************************** Here is the camera part ***********************************/
    public static ImageButton currentBtn;

    private void initCamera() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentBtn = (ImageButton) v;
                Intent intent = new Intent(RecoActivity.this, CameraActivity.class);
                intent.putExtra("activity","RecoActivity");
                startActivity(intent);
            }
        };
        imageButton1.setOnClickListener(listener);
    }
}
