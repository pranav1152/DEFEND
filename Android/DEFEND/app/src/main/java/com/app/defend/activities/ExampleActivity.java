package com.app.defend.activities;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.app.defend.R;
import com.app.defend.ml.Nmodel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExampleActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_example);


		AssetManager am = getAssets();
		InputStream is = null;
		try {
			is = am.open("mapping.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, Integer> mp = new HashMap<>();
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
			//System.out.println(read);

		}

		Nmodel model = null;
		try {
			model = Nmodel.newInstance(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Creates inputs for reference.
		TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);

		int[][] a = new int[1][10];
		a[0][0] = mp.get("the");
		for (int i = 1; i < 10; i++) {
			a[0][i] = 0;
		}

		inputFeature0.loadArray(a[0]);
		// Runs model inference and gets result.
		Nmodel.Outputs outputs = model.process(inputFeature0);
		TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

		Log.e("123", outputFeature0.getBuffer().toString());

		float[] farr = outputFeature0.getFloatArray();
		/*Log.e("123", "" + farr.length);
		for(int i=0;i<farr.length; i++)
			Log.e("123", ""+farr[i]);
*/
		Log.e("123", "eueue" + Arrays.toString(farr));

		// Releases model resources if no longer used.
		model.close();


	}
}