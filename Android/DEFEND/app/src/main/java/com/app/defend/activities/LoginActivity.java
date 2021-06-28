package com.app.defend.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.defend.R;
import com.app.defend.Utils;
import com.app.defend.model.User;
import com.app.defend.rsa.RSAKeyPairGenerator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

	TextInputLayout phoneno, otp;
	TextInputEditText textPhoneNo;
	MaterialButton verify;
	FirebaseAuth mAuth;
	PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
	boolean first = false;
	FirebaseFirestore db;
	private String verificationId;
	private String countryCode;

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mAuth = FirebaseAuth.getInstance();
		FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
				.setPersistenceEnabled(true)
				.build();
		db = FirebaseFirestore.getInstance();
		db.setFirestoreSettings(settings);

		//FirebaseUser user = mAuth.getCurrentUser();
		//if (user != null) {
		if (Utils.getPrivateKey(this) != null) {
			Intent i = new Intent(this, ChatsDashboardActivity.class);
			startActivity(i);
			finish();
		}

		phoneno = findViewById(R.id.phoneno);
		verify = findViewById(R.id.verify);
		otp = findViewById(R.id.otp);
		textPhoneNo = findViewById(R.id.editTextPhoneNo);

		textPhoneNo.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				verify.setEnabled(s.length() == 10);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});


		verify.setOnClickListener(v -> {
			if (!first) {
				if (TextUtils.isEmpty(phoneno.getEditText().getText().toString())) {
					Toast.makeText(LoginActivity.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
				} else {
					// Adding country code with plus sign
					String phone = "+" + findViewById(R.id.ccp) + phoneno.getEditText().getText().toString();
					//sendVerificationCode(phone);
					createNewUser();  //for testing disabled authentication
				}
			} else {
				Log.e("123", "verify otp");
				if (TextUtils.isEmpty(otp.getEditText().getText().toString())) {
					Toast.makeText(LoginActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
				} else {
					verifyCode(otp.getEditText().getText().toString());
				}
			}
		});


		callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

			@Override
			public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
				super.onCodeSent(s, forceResendingToken);
				verificationId = s;
				otp.setVisibility(View.VISIBLE);
				verify.setText("Verify OTP");
				first = true;
			}

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

				final String code = phoneAuthCredential.getSmsCode();

				if (code != null) {
					otp.getEditText().setText(code);
					verifyCode(code);
				}
			}

			@Override
			public void onVerificationFailed(FirebaseException e) {
				Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		};

	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void verifyCode(String code) {
		PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
		signInWithCredential(credential);
	}

	private void sendVerificationCode(String number) {

		PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
				.setPhoneNumber(number)
				.setTimeout(60L, TimeUnit.SECONDS)
				.setActivity(this)
				.setCallbacks(callbacks)
				.build();
		PhoneAuthProvider.verifyPhoneNumber(options);
	}


	@RequiresApi(api = Build.VERSION_CODES.O)
	private void signInWithCredential(PhoneAuthCredential credential) {
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						createNewUser();
					} else {
						Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void createNewUser() {

		if (Utils.getPrivateKey(this) != null) {
			Intent i = new Intent(LoginActivity.this, ChatsDashboardActivity.class);
			startActivity(i);
			finish();
			return;
		}


		Log.e("123", "creating new user");
		String publicKey = null;
		String privateKey = null;
		try {
			RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
			privateKey = rsaKeyPairGenerator.getPrivateKey();
			publicKey = rsaKeyPairGenerator.getPublicKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Utils.saveKeysToSP(privateKey, publicKey, this);
		User user = new User();
		user.setUID(Utils.getAlphaNumericString(20));
		user.setName("Omkar");
		user.setPhoneNo(phoneno.getEditText().getText().toString());
		user.setPublicKey(publicKey);
		Utils.saveUserToSP(user, this);

		user.setChats(new ArrayList<String>());
		db.collection("Users").document(user.getUID()).set(user)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Intent i = new Intent(LoginActivity.this, ChatsDashboardActivity.class);
						startActivity(i);
						finish();
					}
				}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}
}