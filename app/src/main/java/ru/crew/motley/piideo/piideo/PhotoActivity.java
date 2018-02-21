package ru.crew.motley.piideo.piideo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.piideo.service.Recorder;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by vas on 12/22/17.
 */

public class PhotoActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_WRITE_SD = 3;

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    private String mPiideoName;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;

    public static Intent getIntent(Context context) {
        Intent i = new Intent(context, PhotoActivity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo);

        sv = findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissionAndLoad();
    }

    private void prepareCamera() {
        camera = Camera.open(CAMERA_ID);

        Camera.Parameters params = camera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        params.setRotation(90);
        camera.setParameters(params);
        setCameraDisplayOrientation(CAMERA_ID);
        setPreviewSize();

        holder.addCallback(holderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    void setPreviewSize() {
        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> previewsizes = p.getSupportedPreviewSizes();
        List<Camera.Size> videosizes = p.getSupportedVideoSizes();
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        Size size = getOptimalPreviewSize(previewsizes, maxPreviewWidth, maxPreviewHeight);
        p.setPreviewSize(size.width, size.height);
        camera.setParameters(p);
    }

    void setPreviewSize(boolean fullScreen) {

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;


        // получаем размеры экрана
//        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = maxPreviewWidth > maxPreviewHeight;

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, displaySize.x, displaySize.y);

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    private void requestPermissionAndLoad() {
        int cameraCheck = ContextCompat.checkSelfPermission(this,
                CAMERA);
        int sdCardCheck = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        if (cameraCheck == PackageManager.PERMISSION_GRANTED && sdCardCheck == PackageManager.PERMISSION_GRANTED) {
            prepareCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CAMERA, WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareCamera();
                }
                break;
        }
    }

    public void onClickPicture(View view) {
        File piideoFolder = new File(Recorder.HOME_PATH);
        if (!piideoFolder.exists()) {
            piideoFolder.mkdir();
        }
        mPiideoName = UUID.randomUUID().toString();
        File photoFile = new File(piideoFolder, mPiideoName + ".jpg");
        camera.takePicture(null, null, (data, camera) -> {
            try {
                FileOutputStream fos = new FileOutputStream(photoFile);
                fos.write(data);
                fos.close();
                Intent i = PiideoActivity.getIntent(this, mPiideoName);
                startActivity(i);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) 4 / (double) 3;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetWidth = w;

//        if (  you want ratio as closed to what i asked for) {
        for (Size size : sizes) {
            Log.d("Camera", "Checking size " + size.width + "w " + size.height
                    + "h");
            double ratio = (double) size.width / size.height;
            Log.d("Camera", "Checking size " + (Math.abs(ratio - targetRatio)));
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetWidth) < minDiff) {
                optimalSize = size;
                Log.d("Camera", " + + " + optimalSize.width + " " + optimalSize.height);
                minDiff = Math.abs(size.height - targetWidth);
            }
        }
//        }

//        if (you want height as closed to what i asked for) { //you can do other for width
//            minDiff = Double.MAX_VALUE;
//            for (Size size : sizes) {
//                if (Math.abs(size.height - targetHeight) < minDiff) {
//                    optimalSize = size;
//                    minDiff = Math.abs(size.height - targetHeight);
//                }
//            }
//        }

//        if (you want the bigest one) {
//            minDiff = 0;
//            for (Size size : sizes) {
//                if (  size.height * size.width > minDiff ) {
//                    optimalSize = size;
//                    minDiff = size.height * size.width ;
//                }
//            }
//        }
        return optimalSize;
    }

}
