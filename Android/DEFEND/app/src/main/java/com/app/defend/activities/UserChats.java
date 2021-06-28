package com.app.defend.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.defend.R;
import com.app.defend.Utils;
import com.app.defend.ml.Nmodel;
import com.app.defend.model.Message;
import com.app.defend.model.User;
import com.app.defend.retrofit.RetroInterface;
import com.app.defend.retrofit.Retrofi;
import com.app.defend.rsa.RSAUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserChats extends AppCompatActivity {

	User receiver;
	FirebaseFirestore db;
	RecyclerView ll;
	ArrayList<Message> messages;
	ArrayList<String> uids;
	Adapter adapter;
	EditText et;
	ImageButton send;
	RetroInterface ri;
	HashMap<String, Integer> mp;
	Nmodel model = null;
	TensorBuffer inputFeature0;
	Callback<String> callback;
	Message msg;

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_chats);
		uids = new ArrayList<>();

		callback = new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.isSuccessful()) {
					Log.e("123", "response success");
					String[] _t = response.body().split(" ");
					ArrayList<String> flags = new ArrayList<>();
					Collections.addAll(flags, _t);
					msg.setFlags(flags);
					postMessage(msg);
				} else {
					Toast.makeText(UserChats.this, "Failed to post msg" + response.errorBody() + response.message() + response.code(), Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {

			}
		};


		try {
			model = Nmodel.newInstance(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);

		loadTokens();

		ri = Retrofi.init().create(RetroInterface.class);


		Intent intent = getIntent();
		String receiverName = intent.getStringExtra("receiver_name");
		String phoneNumber = intent.getStringExtra("phone_number");

		db = FirebaseFirestore.getInstance();


		ll = findViewById(R.id.rvChat);
		messages = new ArrayList<>();
		adapter = new Adapter(Utils.getUID(this));
		et = findViewById(R.id.etMessage);
		send = findViewById(R.id.btnSend);

		ll.setLayoutManager(new LinearLayoutManager(this));
		ll.setAdapter(adapter);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.custom_chat_toolbar);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		View view = actionBar.getCustomView();
		TextView rName = view.findViewById(R.id.receiverUserName);
		rName.setText(receiverName);

		retriveUser(phoneNumber);
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					send.setEnabled(true);
				} else {
					send.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		send.setOnClickListener(v -> {
			msg = new Message();
			Log.e("123", "send ");
			msg.setUID(Utils.getAlphaNumericString(20));
			String origtypedMsg = et.getText().toString();

			Utils.putMessage(msg.getUID(), origtypedMsg, this);
			String typedMsg = null;
			try {
				typedMsg = Base64.getEncoder().encodeToString(RSAUtils.encrypt(origtypedMsg, receiver.getPublicKey()));
			} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException |
					NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			msg.setDate(new Date());
			msg.setEncryptedText(typedMsg);

			msg.setFrom(Utils.getUID(this));
			msg.setTo(receiver.getUID());


			int[][] tokensarray = preprocessText(origtypedMsg);
			msg.setEncodings(inferModel(tokensarray));

			getFlagsAndPost(msg);
		});
	}

	private void getFlagsAndPost(Message msg) {

		Call<String> call = ri.get_flags(msg.getEncodings());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.isSuccessful()) {
					Log.e("123", "response success");
					Log.e("123", response.body());
					String[] _t = response.body().split(" ");
					ArrayList<String> flags = new ArrayList<>();
					String[] labels = new String[]{"toxic", "severe_toxic", "obscene", "threat", "insult", "identity_hate"};
					for (int i = 0; i < 6; i++) {
						if (Float.parseFloat(_t[i]) >= 0.5f) {
							flags.add(labels[i]);
						}
					}
					msg.setFlags(flags);
					postMessage(msg);
				} else {
					Toast.makeText(UserChats.this, "Failed to post msg" + response.errorBody() + response.message() + response.code(), Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {

			}
		});
	}

	private void postMessage(Message msg) {
		db.collection(uids.get(0) + uids.get(1)).document(msg.getUID())
				.set(msg)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(UserChats.this, "Failed to post msg", Toast.LENGTH_LONG).show();
					}
				});

		et.setText("");
	}

	private String inferModel(int[][] tokensarray) {

		inputFeature0.loadArray(tokensarray[0]);
		Nmodel.Outputs outputs = model.process(inputFeature0);
		TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

		Log.e("123", "inferred");
		return Arrays.toString(outputFeature0.getFloatArray());

	}

	private void loadTokens() {
		AssetManager am = getAssets();
		InputStream is = null;
		try {
			is = am.open("mapping.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String read;
		while (true) {
			try {
				if (((read = br.readLine()) == null)) break;
				String[] arr = read.split(" ");
				mp.put(arr[0], Integer.parseInt(arr[1]));

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private int[][] preprocessText(String origtypedMsg) {

		String[] tokens = origtypedMsg.split(" ");
		int[][] a = new int[1][10];

		for (int i = 0; i < tokens.length; i++) {
			if (mp.get(tokens[i]) == null)
				a[0][i] = 0;
			else a[0][i] = mp.get(tokens[i]);
		}

		for (int i = tokens.length; i < 10; i++)
			a[0][i] = 0;

		Log.e("123", "prepocessed");
		return a;
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	public void retriveUser(String phoneNumber) {
		db.collection("Users").whereEqualTo("phoneNo", phoneNumber)
				.get()
				.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
					@Override
					public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
						receiver = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
						retrieveMessages();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {

					}
				});
	}

	private void retrieveMessages() {
		uids.add(receiver.getUID());
		uids.add(Utils.getUID(this));
		Collections.sort(uids);
		Log.e("user1", uids.get(0));
		Log.e("user2", uids.get(1));

		db.collection(uids.get(0) + uids.get(1)).get()
				.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
					@Override
					public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
						for (DocumentSnapshot doc : queryDocumentSnapshots) {
							Message msg = doc.toObject(Message.class);
							messages.add(msg);
						}
						adapter.notifyDataSetChanged();
						setUpListeners();

						db.collection("Users").document(Utils.getUID(UserChats.this)).update("chats", FieldValue.arrayUnion(receiver.getUID()));

					}

				}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

			}
		});
	}

	private void setUpListeners() {

		db.collection(uids.get(0) + uids.get(1)).addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
				List<DocumentSnapshot> val = value.getDocuments();
				if (val.size() != 0) {
					messages.clear();
					for (DocumentSnapshot doc : val) {
						Message msg = doc.toObject(Message.class);
						messages.add(msg);
					}
					Collections.sort(messages);
					adapter.notifyDataSetChanged();
				}
			}
		});

	}

	public abstract static class TextViewHolder extends RecyclerView.ViewHolder {

		TextView tv;

		public TextViewHolder(@NonNull View itemView) {
			super(itemView);
//			tv = itemView.findViewById(R.id.chattv);
		}

		abstract void bindMessage(Message message);
	}

	public class Adapter extends RecyclerView.Adapter<TextViewHolder> {

		private static final int MESSAGE_OUTGOING = 123;
		private static final int MESSAGE_INCOMING = 321;
		String mUserId;

		public Adapter(String userId) {
			this.mUserId = userId;
		}


		@NonNull
		@Override
		public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			Context context = parent.getContext();
			LayoutInflater inflater = LayoutInflater.from(context);

			if (viewType == MESSAGE_INCOMING) {
				View view = inflater.inflate(R.layout.receiver_msg_chat, parent, false);
				return new IncomingMessageViewHolder(view);
			} else if (viewType == MESSAGE_OUTGOING) {
				View view = inflater.inflate(R.layout.sender_msg_chat, parent, false);
				return new OutgoingMessageViewHolder(view);
			} else {
				throw new IllegalArgumentException("Unknown view type");
			}
		}

		private boolean isMe(int position) {
			Message message = messages.get(position);
			return message.getUID() != null && message.getFrom().equals(mUserId);
		}

		@Override
		public int getItemViewType(int position) {
			if (isMe(position)) {
				return MESSAGE_OUTGOING;
			} else {
				return MESSAGE_INCOMING;
			}
		}

		@RequiresApi(api = Build.VERSION_CODES.O)
		@Override
		public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
			Message message = messages.get(position);
			holder.bindMessage(message);
		}

		@Override
		public int getItemCount() {
			return messages.size();
		}
	}

	public class IncomingMessageViewHolder extends TextViewHolder {
		TextView textMessage;
		TextView chatTime;
		TextView reportText;

		public IncomingMessageViewHolder(@NonNull View itemView) {
			super(itemView);
			textMessage = itemView.findViewById(R.id.receiver_chat);
			chatTime = itemView.findViewById(R.id.chat_timestamp_receiver);
			reportText = itemView.findViewById(R.id.reportText);

		}

		@RequiresApi(api = Build.VERSION_CODES.O)
		@Override
		void bindMessage(Message message) {
			try {
				textMessage.setText(RSAUtils.decrypt(message.getEncryptedText(), Utils.getPrivateKey(UserChats.this)));
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
				chatTime.setText(sdf.format(message.getDate()));
				String flags = "";
				for (String f : message.getFlags())
					flags += f + ", ";
				if (message.getFlags().size() == 0) {
					reportText.setText(flags);
				} else
					reportText.setText("This text has been identified as: " + flags);
			} catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
					NoSuchPaddingException e) {
				e.printStackTrace();
			}
		}
	}

	public class OutgoingMessageViewHolder extends TextViewHolder {
		TextView textMessage;
		TextView chatTime;
		TextView reportText;

		public OutgoingMessageViewHolder(@NonNull View itemView) {
			super(itemView);
			textMessage = itemView.findViewById(R.id.sender_chat);
			chatTime = itemView.findViewById(R.id.chat_timestamp_sender);
			reportText = itemView.findViewById(R.id.reportText);
		}

		@Override
		void bindMessage(Message message) {
			textMessage.setText(Utils.getMessage(message.getUID(), UserChats.this));
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
			chatTime.setText(sdf.format(message.getDate()));
			String flags = "";
			for (String f : message.getFlags())
				flags += f + ", ";
			if (message.getFlags().size() == 0) {
				reportText.setText(flags);
			} else
				reportText.setText("This text has been identified as: " + flags);
		}
	}

}
