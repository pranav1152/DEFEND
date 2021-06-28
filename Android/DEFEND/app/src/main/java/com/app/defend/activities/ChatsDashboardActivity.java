package com.app.defend.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.defend.R;
import com.app.defend.Utils;
import com.app.defend.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatsDashboardActivity extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
	ArrayList<String> Uids;
	ProgressBar pbar;
	//	ChatAdapter adapter;
	User receiver;
	FloatingActionButton fab;
	FirebaseFirestore db;
	RecyclerView rvchats;
	ArrayList<String> chatUser;
	ChatAdapter adapter;
	private FirebaseAuth mAuth;
	private boolean storagePermissionGranted;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats_dashboard);

		mAuth = FirebaseAuth.getInstance();
		fab = findViewById(R.id.newconversation);
		rvchats = findViewById(R.id.rvChat);
		//chatUser = new ArrayList<>();
		//adapter = new ChatAdapter();
		//rvchats.setAdapter(adapter);
		//rvchats.setLayoutManager(new LinearLayoutManager(this));

		db = FirebaseFirestore.getInstance();


		getChatArray();
		retrieveMessages();

//		chatUser = db.collection("Users").document(Utils.getUID(ChatsDashboardActivity.this)).get("");


		fab.setOnClickListener(v -> {

//			getExternalStoragePermission();


			// Check the SDK version and whether the permission is already granted or not.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
				//After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
			} else {
				// Android version is lesser than 6.0 or the permission is already granted.
				Intent i = new Intent(ChatsDashboardActivity.this, ContactsActivity.class);
				startActivity(i);
			}

//			if (storagePermissionGranted) {
//				Intent i = new Intent(ChatsDashboardActivity.this, ContactsActivity.class);
//				startActivity(i);
//			}
		});
	}


	private void retrieveMessages() {
		FirebaseFirestore.getInstance().collection("Users").document("Messages").collection("Messages").get()
				.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
					@Override
					public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
						for (DocumentSnapshot doc : queryDocumentSnapshots) {

						}
					}
				}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

			}
		});
	}

	private void getChatArray() {
		db.collection("Users").document(Utils.getUID(ChatsDashboardActivity.this)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if (task.isSuccessful()) {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						chatUser = (ArrayList<String>) document.get("chats");
						Log.d("list", chatUser.toString());
					}
				}
			}

		});
	}

	private void getExternalStoragePermission() {
		if (ContextCompat.checkSelfPermission(getApplicationContext(),
				Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			// Permission not granted
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.READ_CONTACTS)) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
						PERMISSIONS_REQUEST_READ_CONTACTS);
			} else {
				if (!storagePermissionGranted) {
					// If asked for permission before but denied
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

					alertDialogBuilder.setTitle("Permission Needed");
					alertDialogBuilder.setMessage("Contacts permission needed");
					alertDialogBuilder.setPositiveButton("Open Setting", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							Intent intent = new Intent();
							intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							Uri uri = Uri.fromParts("package", ChatsDashboardActivity.this.getPackageName(), null);
							intent.setData(uri);
							startActivity(intent);
						}
					});
					alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

						}
					});

					AlertDialog dialog = alertDialogBuilder.create();
					dialog.show();
				} else {
					// If asked permission for the first time
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
							PERMISSIONS_REQUEST_READ_CONTACTS);
				}
			}
		} else {
			storagePermissionGranted = true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				storagePermissionGranted = true;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_chat_menu, menu);
		return true;
	}

//	@Override
//	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//		if (item.getItemId() == R.id.logout) {
//			FirebaseAuth.getInstance().signOut();
//			startActivity(new Intent(ChatsDashboardActivity.this, LoginActivity.class));
//			finish();
//			Log.e("logout check", "logout pass");
//			return true;
//		}
//		Log.e("logout check", "logout failed");
//		return false;
//	}

	// On Pressing the navigation up (back button)
	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	public static interface ClickListener {
		public void onClick(View view, int position);

		public void onLongClick(View view, int position);
	}

	public static class ChatViewHolder extends RecyclerView.ViewHolder {
		TextView name;

		public ChatViewHolder(@NonNull View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.name);
//			phone = itemView.findViewById(R.id.phone);
		}
	}

	static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

		private ClickListener clicklistener;
		private GestureDetector gestureDetector;

		public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
			this.clicklistener = clickListener;
			gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
					if (child != null && clickListener != null) {
						clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
					}
				}
			});
		}

		@Override
		public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
			View child = rv.findChildViewUnder(e.getX(), e.getY());
			if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
				clicklistener.onClick(child, rv.getChildAdapterPosition(child));
			}
			return false;
		}

		@Override
		public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

		}

		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

		}
	}

	public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

		@NonNull
		@Override
		public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(ChatsDashboardActivity.this).inflate(R.layout.dashboard_list_item, parent, false);
			return new ChatViewHolder(v);
		}

		@Override
		public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
			//holder.name.setText(chatUser.get(position));
			holder.name.setText(chatUser.get(position));
//			holder.name.setText(_names.get(idx));
		}

		@Override
		public int getItemCount() {
			return chatUser.size();
		}
	}

}
