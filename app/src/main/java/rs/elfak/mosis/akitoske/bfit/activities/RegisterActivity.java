package rs.elfak.mosis.akitoske.bfit.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;
import rs.elfak.mosis.akitoske.bfit.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    private static final int REQUEST_CHOOSE_IMAGE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private Context mContext = this;
    private boolean storagePermissionRationaleShown = false;

    //UI elements
    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatPassword;
    private EditText mDisplayName;
    private EditText mFullName;
    private EditText mPhone;
    private ImageView mAvatar;
    private TextView mAvatarError;

    // New avatar image path, used to check whether we need to delete the old and upload the new image
    private String mNewAvatarLocalPath;
    // Temporary field for storing the path of the generated camera image
    private String mGeneratedLocalPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = findViewById(R.id.register_email_text);
        mPassword = findViewById(R.id.register_password_text);
        mRepeatPassword = findViewById(R.id.register_repeat_password_text);
        mDisplayName = findViewById(R.id.display_name_text);
        mFullName = findViewById(R.id.full_name_text);
        mPhone = findViewById(R.id.phone_text);
        mAvatar = findViewById(R.id.avatar_image);
        mAvatarError = findViewById(R.id.avatar_tv);
        Button chooseAvatarButton = findViewById(R.id.choose_avatar_btn);

        mEmail.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);
        mRepeatPassword.setOnFocusChangeListener(this);
        mDisplayName.setOnFocusChangeListener(this);
        mFullName.setOnFocusChangeListener(this);
        mPhone.setOnFocusChangeListener(this);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAvatarError.requestFocus();
            }
        });

        chooseAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkReadStoragePermission();
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            EditText editText = (EditText) view;
            int editTextId = editText.getId();

            switch (editTextId) {
                case R.id.register_email_text:
                    Validator.validateEmail(editText);
                    break;
                case R.id.register_password_text:
                    Validator.validatePassword(editText);
                    break;
                case R.id.register_repeat_password_text:
                    Validator.validateRepeatPassword(mPassword, editText);
                    break;
                case R.id.display_name_text:
                    Validator.validateDisplayName(editText);
                    break;
                case R.id.full_name_text:
                    Validator.validateFullName(editText);
                    break;
                case R.id.phone_text:
                    Validator.validatePhone(editText);
                    break;
            }
        }
    }

    private void checkReadStoragePermission() {
        final String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int userPermission = ContextCompat.checkSelfPermission(this, storagePermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            requestPermissions(new String[]{storagePermission}, REQUEST_STORAGE_PERMISSION);
        } else {
            onStoragePermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                // If request is granted, the result arrays won't be empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStoragePermissionGranted();
                } else {
                    final String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
                    // Explain the user why the app requires the storage permission and ask for it
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission)
                            && !storagePermissionRationaleShown) {
                        storagePermissionRationaleShown = true;
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.register_storage_permission_title))
                                .setMessage(getString(R.string.register_storage_permission_message))
                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(
                                                new String[]{storagePermission},
                                                REQUEST_STORAGE_PERMISSION);
                                    }
                                }).create().show();
                    } else {
                        onStoragePermissionDenied();
                    }
                }
            }
        }
    }

    private void onStoragePermissionGranted() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        Intent cameraIntent = getCameraIntent();

        if (cameraIntent != null) {
            Intent chooseIntent = Intent.createChooser(galleryIntent, "Select image from");
            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

            startActivityForResult(chooseIntent, REQUEST_CHOOSE_IMAGE);
        } else {
            Toast.makeText(mContext, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
        }
    }

    // If storage access is denied, we can only offer the Camera app if it's available
    private void onStoragePermissionDenied() {
        Intent cameraIntent = getCameraIntent();

        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, REQUEST_CHOOSE_IMAGE);
        } else {
            Toast.makeText(mContext, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // If there's a camera activity, we let the user choose Gallery or Camera
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create a file to internally store the new camera image
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(
                        mContext,
                        "rs.elfak.mosis.akitoske.bfit.fileprovider",
                        imageFile
                );
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                return cameraIntent;
            }
        }

        // If no camera available or File failed to create, we return null
        return null;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mGeneratedLocalPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {

            if (data != null && data.getData() != null) {
                // If an Uri is found in getData(), we need to get the real file path
                // from gallery, because imageUri is a "content://..." Uri
                mNewAvatarLocalPath = getRealPathFromURI(mContext, data.getData());
            } else {
                // If no Uri is found in getData(), camera was used and we point to the generated file path
                mNewAvatarLocalPath = mGeneratedLocalPath;
            }

            mAvatar.setBackground(null);
            Glide.with(mContext)
                    .load(mNewAvatarLocalPath)
                    .into(mAvatar);
            mAvatarError.setError(null);
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        Validator.validateRepeatPassword(mPassword, mRepeatPassword);
        Validator.validateDisplayName(mDisplayName);
        Validator.validateFullName(mFullName);
        Validator.validatePhone(mPhone);
        Validator.validateAvatar(mAvatar, mAvatarError);

        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null &&
                mRepeatPassword.length() > 0 && mRepeatPassword.getError() == null &&
                mDisplayName.length() > 0 && mDisplayName.getError() == null &&
                mFullName.length() > 0 && mFullName.getError() == null &&
                mPhone.length() > 0 && mPhone.getError() == null &&
                mAvatar.getDrawable() != null && mAvatarError.getError() == null;
    }

    private UserModel getUserModel(String newUserId, String storageImgUrl) {
        return new UserModel(
                newUserId,
                mEmail.getText().toString().trim(),
                mDisplayName.getText().toString().trim(),
                mFullName.getText().toString().trim(),
                mPhone.getText().toString().trim(),
                storageImgUrl
        );
    }

    public String getAvatarFileName() {
        return FirebaseProvider.getInstance().getCurrentFirebaseUser().getUid() + ".jpg";
    }

    public void onRegisterClick(View v) {
        if (!allFieldsValid()) return;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.register_progress_dialog_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();

        // First we create the user using FirebaseAuth so he/she's authenticated
        firebaseProvider.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Then we store the avatar in Storage and get its downloadUrl
                    // from the snapshot, and save all user data in realtime database
                    if (task.isSuccessful()) {
                        final String imageFileName = getAvatarFileName();
                        String localImageUri = mNewAvatarLocalPath;
                        firebaseProvider.uploadAvatarImage(imageFileName).putFile(Uri.fromFile(new File(localImageUri))).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return firebaseProvider.uploadAvatarImage(imageFileName).getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri taskResult = task.getResult();
                                    String newUserId = firebaseProvider.getCurrentFirebaseUser().getUid();
                                    Uri downloadUrl = taskResult;
                                    String storageImageUri = String.valueOf(downloadUrl);
                                    UserModel newUser = getUserModel(newUserId, storageImageUri);
                                    Map<String, Object> userInfoValues = newUser.toMap();
                                    firebaseProvider.updateUserInfo(newUserId, userInfoValues);

                                    progressDialog.dismiss();
                                    Intent profileIntent = new Intent(RegisterActivity.this,
                                            MainActivity.class);
                                    startActivity(profileIntent);
                                    finish();
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Exception: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            mEmail.setError(getString(R.string.email_bad_format_error));
                            mEmail.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) {
                            mEmail.setError(getString(R.string.register_email_exists_error));
                            mEmail.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Exception: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
