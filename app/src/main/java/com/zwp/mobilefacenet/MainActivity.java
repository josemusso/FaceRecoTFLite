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
import java.util.Arrays;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private MTCNN mtcnn; // Face Detection
    private FaceAntiSpoofing fas; // Live detection
    private MobileFaceNet mfn; // Face matching

    public static Bitmap bitmap1;
    public static Bitmap bitmap2;
    public static Bitmap bitmap3;
    private Bitmap bitmapCrop1;
    private Bitmap bitmapCrop2;
    private Bitmap bitmapCrop3;

    private ImageButton imageButton1;
    private ImageButton imageButton2;
    private ImageButton imageButton3;
    private ImageView imageViewCrop1;
    private ImageView imageViewCrop2;
    private ImageView imageViewCrop3;
    private TextView resultTextView;
    private TextView resultTextView2;
    private TextView resultTextView3;

    // para el boton hacia atras
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Enrolamiento");
        getSupportActionBar().setSubtitle("Control de Acceso");

        imageButton1 = findViewById(R.id.image_button1);
        imageButton2 = findViewById(R.id.image_button2);
        imageButton3 = findViewById(R.id.image_button3);
        imageViewCrop1 = findViewById(R.id.image_view_crop1);
        imageViewCrop2 = findViewById(R.id.image_view_crop2);
        imageViewCrop3 = findViewById(R.id.image_view_crop3);
        Button cropBtn = findViewById(R.id.crop_btn);
        Button compareBtn = findViewById(R.id.compare_btn);
        resultTextView = findViewById(R.id.result_text_view);
        resultTextView2 = findViewById(R.id.result_text_view2);
        resultTextView3 = findViewById(R.id.result_text_view3);


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
        if (bitmap1 == null || bitmap2 == null || bitmap3 == null) {
            Toast.makeText(this, "Por favor tomate dos fotos", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap bitmapTemp1 = bitmap1.copy(bitmap1.getConfig(), false);
        Bitmap bitmapTemp2 = bitmap2.copy(bitmap1.getConfig(), false);
        Bitmap bitmapTemp3 = bitmap3.copy(bitmap1.getConfig(), false);


        // 检测出人脸数据
        Vector<Box> boxes1 = null;
        Vector<Box> boxes2 = null;
        Vector<Box> boxes3 = null;
        // 如果没检测到人脸会抛出异常
        try {
            long start = System.currentTimeMillis();
            boxes1 = mtcnn.detectFaces(bitmapTemp1, bitmapTemp1.getWidth() / 5); // 只有这句代码检测人脸，下面都是根据Box在图片中裁减出人脸
            boxes2 = mtcnn.detectFaces(bitmapTemp2, bitmapTemp2.getWidth() / 5); // 只有这句代码检测人脸，下面都是根据Box在图片中裁减出人脸
            boxes3 = mtcnn.detectFaces(bitmapTemp3, bitmapTemp3.getWidth() / 5); // 只有这句代码检测人脸，下面都是根据Box在图片中裁减出人脸
            long end = System.currentTimeMillis();
            resultTextView.setText("Tiempo de detección：" + (end - start*1.0)/1000 + " Seg");
            resultTextView2.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (boxes1 == null || boxes2 == null || boxes3 == null) {
            Toast.makeText(MainActivity.this, "No se detectaron rostros", Toast.LENGTH_LONG).show();
            return;
        }
        // Because there is only one face in each photo used here, the first value is used to crop the face
        Box box1 = boxes1.get(0);
        Box box2 = boxes2.get(0);
        Box box3 = boxes3.get(0);

        // increase margin
        Rect rect1 = box1.transform2Rect();
        Rect rect2 = box2.transform2Rect();
        Rect rect3 = box3.transform2Rect();
        MyUtil.rectExtend(bitmapTemp1, rect1);
        MyUtil.rectExtend(bitmapTemp1, rect2);
        MyUtil.rectExtend(bitmapTemp1, rect3);

        // Crop face
        bitmapCrop1 = MyUtil.crop(bitmapTemp1, rect1);
        bitmapCrop2 = MyUtil.crop(bitmapTemp2, rect2);
        bitmapCrop3 = MyUtil.crop(bitmapTemp3, rect3);

        // Draw face frame and five points
        //Utils.drawBox(bitmapTemp1, box1, 10);
        //Utils.drawBox(bitmapTemp2, box2, 10);

//        bitmapCrop2 = MyUtil.readFromAssets(this, "1.png");
        imageViewCrop1.setImageBitmap(bitmapCrop1);
        imageViewCrop2.setImageBitmap(bitmapCrop2);
        imageViewCrop3.setImageBitmap(bitmapCrop3);

    }

    /**
     * Live detection
     */
    private void antiSpoofing() {
        if (bitmapCrop1 == null || bitmapCrop2 == null) {
            Toast.makeText(this, "Por favor detectar rostros primero", Toast.LENGTH_LONG).show();
            return;
        }

        long start = System.currentTimeMillis();
        float score1 = fas.antiSpoofing(bitmapCrop1); // 就这一句有用代码，其他都是UI
        long end = System.currentTimeMillis();
        float score2 = fas.antiSpoofing(bitmapCrop2); // 就这一句有用代码，其他都是UI

        String text = "antiSpoofing test results left：" + score1;
        if (score1 < FaceAntiSpoofing.THRESHOLD) {
            text = text + " => " + "True";
            resultTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            text = text + " => " + "False";
            resultTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        resultTextView.setText(text);
        String textTime = "Tiempo de ejecucion: " + (end - start*1.0)/1000 + " Seg";
        resultTextView2.setText(textTime);


        String text2 = "antiSpoofing test results right：" + score2;
        if (score2 < FaceAntiSpoofing.THRESHOLD) {
            text2 = text2 + " => " + "True";
            resultTextView3.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            text2 = text2 + " => " + "False";
            resultTextView3.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        resultTextView3.setText(text2);
    }

    /**
     * Face matching
     */
    private void faceCompare() {
        if (bitmapCrop1 == null || bitmapCrop2 == null || bitmapCrop3 == null) {
            Toast.makeText(this, "Por favor detectar rostros primero", Toast.LENGTH_LONG).show();
            return;
        }

        // eliminar despues
        float same = mfn.compare(bitmapCrop1, bitmapCrop2); // 就这一句有用代码，其他都是UI

        long start = System.currentTimeMillis();

        float[][] embeddings12 = mfn.getEmedding(bitmapCrop1, bitmapCrop2);
        float[][] embeddings33 = mfn.getEmedding(bitmapCrop3, bitmapCrop3); // 1 embed extra

        float[] embedding1 = embeddings12[0];
        float[] embedding2 = embeddings12[1];
        float[] embedding3 = embeddings33[0];

        Log.d("ENROL", "EMBEDDING 1: " + Arrays.toString(embedding1));
        Log.d("ENROL", "EMBEDDING 2: " + Arrays.toString(embedding2));
        Log.d("ENROL", "EMBEDDING 3: " + Arrays.toString(embedding3));
        long end = System.currentTimeMillis();

        String textTime = "Tiempo de Procesamiento: " + (end - start*1.0)/1000 + " Seg";
        resultTextView.setText(textTime);


        String text = "Comparación imágenes 1 y 2：" + same;
        if (same > MobileFaceNet.THRESHOLD) {
            text = text + " => True";
            resultTextView2.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            text = text + " => False";
            resultTextView2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        resultTextView2.setText(text);
        resultTextView3.setText("");

    }

    /*********************************** Here is the camera part ***********************************/
    public static ImageButton currentBtn;

    private void initCamera() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentBtn = (ImageButton) v;
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("activity","MainActivity");
                startActivity(intent);
            }
        };
        imageButton1.setOnClickListener(listener);
        imageButton2.setOnClickListener(listener);
        imageButton3.setOnClickListener(listener);
    }
}
