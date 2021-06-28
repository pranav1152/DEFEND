package com.app.defend.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.defend.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

	ArrayList<String> userContacts, _phones, _names;
	RecyclerView rv;
	ProgressBar pbar;
	ContactsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Select Contact");

		rv = findViewById(R.id.recyclerView);
		pbar = findViewById(R.id.contactProgressBar);

		getPhoneNumbers();


		rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {
			@Override
			public void onClick(View view, int position) {
				Toast.makeText(ContactsActivity.this, "Single Click on position :" + position,
						Toast.LENGTH_SHORT).show();

				//Getting name and phone number of the recipient chat user
				TextView receiverName = view.findViewById(R.id.name);
				TextView phoneNumber = view.findViewById(R.id.phone);

				// Sending data to Chat Activity
				Intent chat = new Intent(ContactsActivity.this, UserChats.class);
				chat.putExtra("receiver_name", receiverName.getText().toString());
				chat.putExtra("phone_number", phoneNumber.getText().toString());
				startActivity(chat);
				finish();
			}

			// TODO: Long-Click left to implement
			@Override
			public void onLongClick(View view, int position) {
				Toast.makeText(ContactsActivity.this, "Long press on position : " + position,
						Toast.LENGTH_SHORT).show();
			}
		}));

	}

	private void retrieveUsers() {
		// Start the indeterminate progress bar
		pbar.setVisibility(View.VISIBLE);
		pbar.setIndeterminate(true);
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection("Users")
				.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
			@Override
			public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
				userContacts = new ArrayList<>();
				for (DocumentSnapshot doc : queryDocumentSnapshots) {
					if (_phones.contains("+91" + doc.getString("phoneNo")))
						userContacts.add(doc.getString("phoneNo"));
				}
				updateUI();

			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

			}
		});
	}

	private void updateUI() {
		adapter = new ContactsAdapter();
		rv.setLayoutManager(new LinearLayoutManager(this));
		rv.setAdapter(adapter);

		// To stop the progress bar and hide it
		pbar.setVisibility(View.GONE);
		pbar.setIndeterminate(false);
	}

	private void getPhoneNumbers() {
		_phones = new ArrayList<>();
		_names = new ArrayList<>();
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (phones.moveToNext()) {

			String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

			_phones.add(phoneNumber);
			_names.add(name);

		}
		phones.close();
		retrieveUsers();

	}

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

	public static class ContactViewHolder extends RecyclerView.ViewHolder {
		TextView name, phone;

		public ContactViewHolder(@NonNull View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.name);
			phone = itemView.findViewById(R.id.phone);
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

	public class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> {

		@NonNull
		@Override
		public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(ContactsActivity.this).inflate(R.layout.contacts_list_item, parent, false);
			return new ContactViewHolder(v);
		}

		@Override
		public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
			holder.phone.setText(userContacts.get(position));
			int idx = _phones.indexOf("+91" + userContacts.get(position));
			holder.name.setText(_names.get(idx));
		}

		@Override
		public int getItemCount() {
			return userContacts.size();
		}
	}
}