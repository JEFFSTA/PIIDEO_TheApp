package ru.crew.motley.piideo.piideo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.piideo.BitmapSingleton;
import ru.crew.motley.piideo.piideo.service.Recorder;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by vas on 12/22/17.
 */

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = PhotoActivity.class.getSimpleName();
    private static final String EXTRA_MESSAGE = "fcm_message";
    private static final String EXTRA_MESSAGE_ID = "local_db_id";

    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_WRITE_SD = 3;

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    ImageButton pictureButton;

    private TextView debugPhoto;
    private TextView photoSizes;

    private String mPiideoName;

    private Parcelable mMessage;
    private String mMessageId;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;

    public static Intent getIntent(String dbMessageId, Parcelable message, Context context) {
        Intent i = new Intent(context, PhotoActivity.class);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_MESSAGE_ID, dbMessageId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo);
        mMessage = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        mMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        pictureButton = findViewById(R.id.btnTakePicture);
        sv = findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        debugPhoto = findViewById(R.id.photo_debug);
        photoSizes = findViewById(R.id.photo_size);

        holderCallback = new HolderCallback();

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissionAndLoad();
    }

    private void prepareCamera() {
        camera = Camera.open(CAMERA_ID);
        setCameraDisplayOrientation(CAMERA_ID);

        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(100);
        params.setRotation(90);
        camera.setParameters(params);
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
        List<Camera.Size> pictureSizes = p.getSupportedPictureSizes();
//        for (Size size : pictureSizes) {
//            Log.d("Picture sizes", " w " + size.width + " h " + size.height);
//        }
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPreviewWidth = displaySize.y;
        int maxPreviewHeight = displaySize.x;

        Size size = getOptimalPreviewSize(previewsizes, maxPreviewWidth, maxPreviewHeight);
        p.setPreviewSize(size.width, size.height);
        p.setJpegQuality(100);
        Camera.Size pictureSize = getPictureSize(pictureSizes);
        p.setPictureSize(pictureSize.width, pictureSize.height);
        for (Camera.Size size1 : pictureSizes) {
            photoSizes.append("s w/s " + size1.width + " " + size1.height + "\n");
        }
        camera.setParameters(p);
        setMyPreviewSize(size.width, size.height);
    }

    private Camera.Size getPictureSize(List<Camera.Size> pictureSizes) {
        Camera.Size result = pictureSizes.get(0);
        if (result.width < result.height) {
            for (Camera.Size size : pictureSizes) {
                if (size.width > result.width) {
                    result = size;
                }
            }
        } else {
            for (Camera.Size size : pictureSizes) {
                if (size.height > result.height) {
                    result = size;
                }
            }
        }
        return result;
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        photoSizes.append("DD r - " + rotation + "\n");
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
        pictureButton.setEnabled(false);
        File piideoFolder = new File(Recorder.HOME_PATH);
        if (!piideoFolder.exists()) {
            piideoFolder.mkdir();
        }
        mPiideoName = UUID.randomUUID().toString();
//        File photoFile = new File(piideoFolder, mPiideoName + ".jpg");
        camera.takePicture(null, null, (data, camera) -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.d(TAG, " w " + bitmap.getWidth() + "  h " + bitmap.getHeight());
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    Matrix mat = new Matrix();
                    mat.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                }
                saveFile(bitmap);
                BitmapSingleton.save(bitmap);
//
//                FileOutputStream out = null;
//                try {
//                    out = new FileOutputStream(photoFile);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
//                    // PNG is a lossless format, the compression factor (100) is ignored
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        if (out != null) {
//                            out.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }


                Intent i = PiideoActivity.getIntent(this, mPiideoName, mMessage, mMessageId);
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

        int targetHeight = h;
        int targetWidth = w;

        debugPhoto.append("tR " + targetRatio);

//        if (  you want ratio as closed to what i asked for) {
        for (Size size : sizes) {
            Log.d("Camera", "Checking size " + size.width + "w " + size.height
                    + "h\n");

            debugPhoto.append("Ch.s " + size.width + "  w " + size.height + "h\n");
            double ratio = (double) size.width / size.height;
            Log.d("Camera", "Ch.s " + (Math.abs(ratio - targetRatio)));
            debugPhoto.append("Ch.s " + (Math.abs(ratio - targetRatio)) + "\n");
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                Log.d("Camera", " Opt.s " + optimalSize.width + " " + optimalSize.height);
                debugPhoto.append(" Opt.s " + optimalSize.width + " " + optimalSize.height + "\n");
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        Log.d(TAG, "Opt size w" + optimalSize.width + " h " + optimalSize.height);
        return optimalSize;
    }

    private void setMyPreviewSize(int width, int height) {
        // Get the set dimensions
        float newProportion = (float) width / (float) height;

        // Get the width of the screen

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) maxPreviewHeight / (float) maxPreviewWidth;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = sv.getLayoutParams();
        if (newProportion < screenProportion) {
            lp.width = maxPreviewWidth;
            lp.height = (int) ((float) maxPreviewHeight / newProportion);
        } else {
            lp.width = (int) (newProportion * (float) maxPreviewHeight);
            lp.height = maxPreviewHeight;
        }
        // Commit the layout parameters
        sv.setLayoutParams(lp);
    }

//    private Size getOptimalPictureSize(List<Size> sizes) {
//        int targetRatio = 0;
//        float tollerance = 0.2F;
//        if (sizes.get(0).height < sizes.get(0).width) {
//            targetRatio = 3 / 4;
//        } else {
//            targetRatio = 4 / 3;
//        }
//        int maxHeight = 0;
//        for (Size size : sizes) {
//            if (maxHeight < size.height) {
//                maxHeight = maxHeight;
//            }
//        }
//
//    }


    public void saveFile(Bitmap forSave) {
        Single.create(e -> {
            try {
                File piideoFolder = new File(Recorder.HOME_PATH);
                if (!piideoFolder.exists()) {
                    piideoFolder.mkdir();
                }
                File photoFile = new File(piideoFolder, mPiideoName + ".jpg");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(photoFile);
                    forSave.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                    e.onSuccess(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();


    }

}
