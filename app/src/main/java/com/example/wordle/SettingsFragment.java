package com.example.wordle;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author: Rey Sanayei
 * @description: The Settings Fragment's java code. Responsible for displaying the username and
 * profile picture, as well as the options of changing the username, changing the profile picture
 * through the choices of camera or gallery browsing, and finally choosing the theme between light
 * and dark.
 *
 */

public class SettingsFragment  extends androidx.fragment.app.DialogFragment  {

    public Activity containerActivity = null;
    private Player player;
    private ImageView closeBtn;
    private ImageView profilePicture;
    private EditText usernameField;
    private Button changeUsernameBtn;
    private Button takePictureBtn;
    private Button browseGalleryBtn;
    private SwitchCompat themeSwitch;
    private Intent data;

    private static String currentPhotoPath;

    ActivityResultLauncher<Intent> CameraResultLauncher;
    ActivityResultLauncher<Intent> GallaryLauncher;
    SharedPreferences sharedPref;

    /**
     * Required empty public constructor
     */
    public SettingsFragment() {
    }

    /**
     * Sets the container Activity for this fragment.
     *
     * @param containerActivity The container Activity for this fragment.
     */
    public void setContainerActivity(Activity containerActivity) {
        this.containerActivity = containerActivity;
    }

    /**
     * The method called on the creation of the fragment's instance. This method also defines
     * the OnClick event-handling for the readOn Button.
     *
     * @param inflater           The LayoutInflater
     * @param container          The ViewGroup that contains this fragment
     * @param savedInstanceState The Bundle that holds saved information for recovering this instance
     * @return View The view that gets inflated is returned.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        player = ((MainActivity) containerActivity).getPlayer();
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        profilePicture = v.findViewById(R.id.settings_profile_img_view);
        sharedPref = containerActivity.getPreferences(Context.MODE_PRIVATE);
        String profilePictureSource = sharedPref.getString(getString(R.string.profile_picte_source), "NA");
        currentPhotoPath = sharedPref.getString(getString(R.string.profile_picture_path), "NA");
        if (!(currentPhotoPath.equals("NA"))) {
            if (profilePictureSource.equals(getString(R.string.camura_roll))) {
                Cursor cursor = getContext().getContentResolver().query(
                        MediaStore.Images

                                .Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                String searchName = new File(currentPhotoPath).getName();



                while (cursor.moveToNext()) {
                    // Use an ID column from the projection to get
                    // a URI representing the media item itself.
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    if (name.equals(searchName)) {
                        profilePicture.setImageURI(contentUri);
                        break;
                    } else profilePicture.setImageResource(R.drawable.defualt_profile);

                }
            } else {
                BitmapFactory.Options bmOptions =
                        new BitmapFactory.Options();
                File file = new File(containerActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), currentPhotoPath);

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
                profilePicture.setImageBitmap(bitmap);

            }
        }


        CameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    //This is the onActivityResult method:  gets called after user takes picture:
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK)
                            data = result.getData();
                        BitmapFactory.Options bmOptions =
                                new BitmapFactory.Options();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.profile_picture_path), currentPhotoPath);
                        editor.putString(getString(R.string.profile_picte_source), getString(R.string.profile_picture_path));
                        editor.apply();
                        File file = new File(containerActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), currentPhotoPath);
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
                        profilePicture.setImageBitmap(bitmap);
                    }
                }

        );

        GallaryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    //This is the onActivityResult method:  gets called after user selects picture
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK)
                            data = result.getData();
                            try {
                                Uri selectedImageUri = data.getData();
                                String path = getPathFromURI(selectedImageUri);

                                File file = new File(containerActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), path);
                                Toast.makeText(containerActivity, "" + path, Toast.LENGTH_SHORT).show();
                                currentPhotoPath = file.getPath();

                                Bitmap bitmap = BitmapFactory.decodeStream(containerActivity.getContentResolver().openInputStream(selectedImageUri));

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.profile_picture_path), currentPhotoPath);
                                editor.putString(getString(R.string.profile_picte_source), getString(R.string.camura_roll));
                                editor.apply();

                                profilePicture.setImageBitmap(bitmap);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                    }
                }

        );

        themeSwitch = v.findViewById(R.id.theme_toggle_btn);
        if(!player.isThemeLight())
            themeSwitch.setChecked(true);
        else
            themeSwitch.setChecked(false);

        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                player.setThemeLight(!isChecked);
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                dismissAllowingStateLoss();
            }
        });
        closeBtn = v.findViewById(R.id.close_button);
        closeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAllowingStateLoss();
            }
        });
        usernameField = v.findViewById(R.id.settings_name_edit_text);
        usernameField.setHint(player.getName());
        changeUsernameBtn = v.findViewById(R.id.change_name_btn);
        changeUsernameBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String toUpdate = String.valueOf(usernameField.getText());
                if(!toUpdate.equals("")) {
                    if (!player.getName().equals(toUpdate))
                        player.setName(toUpdate);
                    usernameField.setHint(player.getName());
                    usernameField.setText("");
                    ((MainActivity)containerActivity).sendUserData();
                }

            }
        });

        takePictureBtn = v.findViewById(R.id.camera_btn);
        takePictureBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentPhotoPath = "image" + profilePicture.getId();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(containerActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), currentPhotoPath);
                Uri photoURI = FileProvider.getUriForFile(containerActivity,
                        getString(R.string.authority), file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                CameraResultLauncher.launch(intent);
            }
        });

        browseGalleryBtn = v.findViewById(R.id.browse_btn);
        browseGalleryBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentPhotoPath = "imagegal" + profilePicture.getId() + ".png";
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                GallaryLauncher.launch(intent);
            }
        });

        return v;
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = containerActivity.getContentResolver().query(contentUri, proj,
                null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}