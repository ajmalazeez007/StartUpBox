package com.greycodes.startupbox;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.File;
import java.io.IOException;

import static org.opencv.core.Core.NORM_L2;
import static org.opencv.core.Core.norm;


public class MainActivitty extends ActionBarActivity {
   final String TAG = "Imae";
    Button image1,image2;
    TextView compare;
    ImageView img1,img2;
    Bitmap btm1,btm2;
    boolean set1,set2;
    EditText et1,et2;
    String url1,url2;
    private long enqueue1,enqueue2;
    private DownloadManager dm;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Create and set View




                    img1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            startActivityForResult(i, 1);
                        }
                    });
                    img2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            startActivityForResult(i, 2);
                        }
                    });

                    compare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (set1==true&&set2==true){
                               compare();
                            }else if (set1){
                              url2=  et2.getText().toString();
                                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url2));
                                enqueue1 = dm.enqueue(request);
                            }else if (set2){
                                url1=et1.getText().toString();
                                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url1));
                                enqueue1 = dm.enqueue(request);
                            }else{
                                url1=et1.getText().toString();
                                url2=  et2.getText().toString();
                                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url1));
                                enqueue1 = dm.enqueue(request);
                                request = new DownloadManager.Request(
                                        Uri.parse(url2));
                                enqueue2 = dm.enqueue(request);

                            }
                        }
                    });


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        set1=false;
        set2=false;
        setContentView(R.layout.activity_main);
        Log.i(TAG, "Trying to load OpenCV library");

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
            Toast.makeText(getApplicationContext(),"Cannot connect to OpenCV Manager",Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue1,enqueue2);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {

                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            if (enqueue1==downloadId){
                                    try {
                                        btm1 = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uriString));
                                        set1=true;
                                        img1.setImageBitmap(btm1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                            }else  if (enqueue2==downloadId){

                                    try {
                                        btm2 = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uriString));
                                        set2=true;
                                        img2.setImageBitmap(btm2);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                            }
                        }
                    }
                    if (set1==true&&set2==true){
                       compare();
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_activitty, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File imgFile = new  File(picturePath);
            if(imgFile.exists()){
                btm1 = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //Drawable d = new BitmapDrawable(getResources(), myBitmap);
            img1.setImageBitmap(btm1);
                set1=true;
            }

        }
        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File imgFile = new  File(picturePath);
            if(imgFile.exists()){
                btm2 = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //Drawable d = new BitmapDrawable(getResources(), myBitmap);
                img2.setImageBitmap(btm2);
                set2=true;
            }

        }


    }


    double getSimilarity(  Mat A,  Mat B ) {

        if (A.rows() > 0 && A.rows() == B.rows() && A.cols() > 0 && A.cols() == B.cols()) {
            // Calculate the L2 relative error between images.
            double errorL2 = norm(A, B, NORM_L2);
            // Convert to a reasonable scale, since L2 error is summed across all pixels of the image.
            double similarity = errorL2 / (double) (A.rows() * A.cols());
            return similarity;
        } else {
            //Images have a different size
            return 100000000.0;  // Return a bad value
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        image1 = (Button) findViewById(R.id.image1);
        image2 = (Button) findViewById(R.id.image2);
        compare = (TextView) findViewById(R.id.compare);
        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);


    }

    void compare(){
        btm1= getResizedBitmap(btm1,300,300);
        btm2= getResizedBitmap(btm2,300,300);
        Mat mat1 = new Mat ( img1.getHeight(), img1.getWidth(), CvType.CV_8U, new Scalar(4));
        Mat mat2 = new Mat ( img2.getHeight(), img2.getWidth(), CvType.CV_8U, new Scalar(4));

        Utils.bitmapToMat(btm1, mat1);
        Utils.bitmapToMat(btm2, mat2);
        double val=     getSimilarity(mat1, mat2);
        if (val>=100000000.0){
            Toast.makeText(getApplicationContext(),"Images are of different size",Toast.LENGTH_LONG).show();

        }else {
            int per = 100 -(int)(val*100);
            Intent intent = new Intent(MainActivitty.this,ResultActivity.class);
            intent.putExtra("result",per);
            set1=false;
            set2=false;
            et1.setText("");
            et2.setText("");
            img1.setImageResource(R.drawable.img);
            img2.setImageResource(R.drawable.img);
            startActivity(intent);
            overridePendingTransition(R.anim.fadeinright,R.anim.fadeoutleft);

        }
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


}

