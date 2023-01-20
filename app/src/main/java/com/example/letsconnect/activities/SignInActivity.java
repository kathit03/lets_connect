package com.example.letsconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.letsconnect.databinding.ActivitySignInBinding;
import com.example.letsconnect.utilites.Constants;
import com.example.letsconnect.utilites.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners() {
        binding.textcreatenewaccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));

        binding.buttonSignin.setOnClickListener(view -> {
            if (isValidSignInDetails()){
                SignIn();
            }
        });

    }

    private void SignIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputemail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputpassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful() && task.getResult() != null
                   && task.getResult().getDocuments().size() > 0) {
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                       preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }else{
                       loading(false);
                       showToast("unable to sign in");
                   }
                });
    }

    public void loading (Boolean isLoading){
        if (isLoading){
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignin.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message ){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
        if (binding.inputemail.getText().toString().trim().isEmpty()){
            showToast("enter email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputemail.getText().toString()).matches()){
            showToast("enter valid email");
            return false;
        }else if (binding.inputpassword.getText().toString().trim().isEmpty()){
            showToast("enter password");
            return false;
        }else{
            return true;
        }
    }
}