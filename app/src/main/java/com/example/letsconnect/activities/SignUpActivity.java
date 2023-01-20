package com.example.letsconnect.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsconnect.databinding.ActivitySignUpBinding;
import com.example.letsconnect.utilites.Constants;
import com.example.letsconnect.utilites.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener(){
        binding.textSignin.setOnClickListener(view -> onBackPressed());
        binding.buttonSignup.setOnClickListener(view -> {
            if (isValidSignUpDetails()){
                Signup();
            }
        });
        binding.layoutimage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImages.launch(intent);
        });
    }

    private void  showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void Signup(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputname.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputemail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputpassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputname.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encodedImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight= bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImages = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
               if (result.getResultCode()== RESULT_OK){
                   if (result.getData() != null){
                       Uri imageuri = result.getData().getData();
                       try {
                           InputStream inputStream = getContentResolver().openInputStream(imageuri);
                           Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                           binding.imageprofile.setImageBitmap(bitmap);
                           binding.textAddImage.setVisibility(View.GONE);
                           encodedImage = encodedImage(bitmap);
                       }catch (FileNotFoundException e) {
                         e.printStackTrace();
                       }
                   }
               }
            }
    );

    private Boolean isValidSignUpDetails(){
        if (encodedImage==null){
            showToast("select profile picture ");
            return false;
        }else if (binding.inputname.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }else if (binding.inputemail.getText().toString().trim().isEmpty()){
            showToast("enter email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputemail.getText().toString()).matches()){
            showToast("enter valid image");
            return false;
        }
        else if (binding.inputpassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if (binding.inputconfirmpassword.getText().toString().trim().isEmpty()){
            showToast("confirm your password");
            return false;
        }
        else if (!binding.inputpassword.getText().toString().equals(binding.inputconfirmpassword.getText().toString())){
            showToast("password and confirm password must be same");
            return false;
        }
        else {
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignup.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignup.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


}