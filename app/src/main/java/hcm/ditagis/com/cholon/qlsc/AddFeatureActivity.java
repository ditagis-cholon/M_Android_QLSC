package hcm.ditagis.com.cholon.qlsc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.FeatureLayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import hcm.ditagis.com.cholon.qlsc.async.AddFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.async.LoadingDataFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.ImageFile;

public class AddFeatureActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.btn_add_feature_add)
    Button mBtnAdd;
    @BindView(R.id.btn_add_feature_capture)
    Button mBtnCapture;
    @BindView(R.id.btn_add_feature_pick_photo)
    Button mBtnPickPhoto;
    @BindView(R.id.llayout_add_feature_main)
    LinearLayout mLLayoutMain;
    @BindView(R.id.llayout_add_feature_field)
    LinearLayout mLLayoutField;
    @BindView(R.id.llayout_add_feature_progress)
    LinearLayout mLLayoutProgress;
    @BindView(R.id.txt_add_feature_progress)
    TextView mTxtProgress;
    @BindView(R.id.llayout_add_feature_image)
    LinearLayout mLLayoutImage;

    private DApplication mApplication;

    private List<byte[]> mImages;
    private Uri mUri;
    private ArrayAdapter<String> mAdapterLayer;
    private FeatureLayer mFeatureLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feature);
        ButterKnife.bind(this);
        mApplication = (DApplication) getApplication();
        initViews();
    }

    private void initViews() {
        mImages = new ArrayList<>();
        mBtnCapture.setOnClickListener(this::onClick);
        mBtnAdd.setOnClickListener(this::onClick);
        mBtnPickPhoto.setOnClickListener(this::onClick);
        (Objects.requireNonNull(getSupportActionBar())).setDisplayHomeAsUpEnabled(true);
        (Objects.requireNonNull(getSupportActionBar())).setDisplayShowHomeEnabled(true);
        mTxtProgress.setText("Đang khởi tạo thuộc tính...");
        mLLayoutProgress.setVisibility(View.VISIBLE);
        mLLayoutMain.setVisibility(View.GONE);

        mFeatureLayer = mApplication.getDFeatureLayer().getLayer();
        new LoadingDataFeatureAsync(AddFeatureActivity.this,
                AddFeatureActivity.this, mFeatureLayer.getFeatureTable().getFields(), views -> {
            if (views != null)
                for (View view1 : views) {

                    mLLayoutField.addView(view1);
                }
            mLLayoutProgress.setVisibility(View.GONE);
            mLLayoutMain.setVisibility(View.VISIBLE);
        }).execute(true);
    }


    private boolean hadPoint() {
        return mApplication.getAddFeaturePoint() != null;
    }

    public void capture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());

        File photo = ImageFile.getFile(AddFeatureActivity.this);
//        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        mUri = Uri.fromFile(photo);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
//        this.mUri = Uri.fromFile(photo);
        try {
            this.startActivityForResult(cameraIntent, Constant.RequestCode.ADD_FEATURE_ATTACHMENT);
        } catch (Exception e) {
            Log.e("Lỗi chụp ảnh", e.toString());
        }

    }

    private void pickPhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, Constant.RequestCode.PICK_PHOTO);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_add_feature_add:
                if (!hadPoint()) {
                    Toast.makeText(AddFeatureActivity.this, R.string.message_add_feature_had_not_point, Toast.LENGTH_LONG).show();
                } else if (mFeatureLayer == null) {
                    Toast.makeText(AddFeatureActivity.this, R.string.message_add_feature_had_not_feature, Toast.LENGTH_LONG).show();
                } else {
                    mLLayoutProgress.setVisibility(View.VISIBLE);
                    mLLayoutMain.setVisibility(View.GONE);
                    mTxtProgress.setText("Đang lưu...");
                    new AddFeatureAsync(AddFeatureActivity.this, mApplication.getDFeatureLayer().getServiceFeatureTable(), mLLayoutField, output -> {
                        if (output != null) {
                            mApplication.getDiemSuCo().setObjectID(Long.parseLong(output.getAttributes().get(Constant.Field.OBJECTID).toString()));

                            goHome();
                        }
                        mLLayoutProgress.setVisibility(View.GONE);
                        mLLayoutMain.setVisibility(View.VISIBLE);
                    }).execute();


                }
                break;
            case R.id.btn_add_feature_capture:
                capture();
                break;
            case R.id.btn_add_feature_pick_photo:
                pickPhoto();
                break;
        }

    }

    @Nullable
    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in;
        try {
            final int IMAGE_MAX_SIZE = 1000000; // 1.0MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            assert in != null;
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            assert in != null;
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

    private void handlingImage(Bitmap bitmap, boolean isFromCamera) {
        try {
            if (bitmap != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageView imageView = new ImageView(mLLayoutImage.getContext());
                imageView.setPadding(0, 0, 0, 10);
                if (isFromCamera) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    imageView.setImageBitmap(rotatedBitmap);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    imageView.setImageBitmap(bitmap);
                }
                byte[] image = outputStream.toByteArray();
                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show();
                mLLayoutImage.addView(imageView);

                mImages.add(image);
                mApplication.setImages(mImages);
            }
        } catch (Exception ignored) {
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.RequestCode.ADD_FEATURE_ATTACHMENT:
                if (resultCode == RESULT_OK) {
                    if (this.mUri != null) {
                        Bitmap bitmap = getBitmap(this.mUri.getPath());
                        handlingImage(bitmap, true);
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT);
                }
                break;
            case Constant.RequestCode.PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri contentURI = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                            handlingImage(bitmap, false);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AddFeatureActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        goHomeCancel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void goHome() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void goHomeCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
