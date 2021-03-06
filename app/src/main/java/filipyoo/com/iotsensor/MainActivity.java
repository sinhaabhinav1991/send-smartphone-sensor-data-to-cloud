package filipyoo.com.iotsensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.net.Socket;



public class MainActivity extends AppCompatActivity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;

    private SensorManager sensorManager;
    private Sensor sensor_temp, sensor_accelero;
    private float temperature;
    private float x, y, z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		editTextAddress = (EditText)findViewById(R.id.address);
		editTextPort    = (EditText)findViewById(R.id.port);
		buttonConnect   = (Button)findViewById(R.id.connect);
		buttonClear     = (Button)findViewById(R.id.clear);
		textResponse    = (TextView)findViewById(R.id.response);
		
		sensorManager   = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor_temp     = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).get(0);
		sensor_accelero = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);


        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});
    }

    OnClickListener buttonConnectOnClickListener =  new OnClickListener(){
        @Override
        public void onClick(View arg0) {
            MyClientTask myClientTask = new MyClientTask(
                    editTextAddress.getText().toString(),
                    Integer.parseInt(editTextPort.getText().toString()));
            myClientTask.execute();
        }};


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(temp_listener, sensor_temp, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(accelero_listener, sensor_accelero, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(temp_listener);
        sensorManager.unregisterListener(accelero_listener);
        super.onStop();
    }

    private SensorEventListener temp_listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {}
        @Override
        public void onSensorChanged(SensorEvent event) {
            temperature = event.values[0];
        }
    };

    private SensorEventListener accelero_listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {}
        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        }
    };



    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        String response = "";
        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            Socket socket = null;
            String sensorValues;
            int readData_idx = 1;
            DataOutputStream dataOutputStream = null;
            try {
            	socket = new Socket(dstAddress, dstPort);            	
            	while (true) {
                	dataOutputStream = new DataOutputStream(socket.getOutputStream());
                	sensorValues = Float.toString(temperature) + "," + Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z) + "," + Integer.toString(readData_idx);
                	readData_idx = readData_idx + 1;
                	dataOutputStream.writeBytes(sensorValues);

                	response = "temperature = " + Float.toString(temperature) + "\nx = " + Float.toString(x) + "\ny = " + Float.toString(y) + "\nz = " + Float.toString(z) + "\nreadData_idx = " + Float.toString(readData_idx);
                	runOnUiThread(new Runnable() {
                	            @Override
                	            public void run() {
                	                textResponse.setText(response);
                	            }
                	        });
                	Thread.sleep(1000);
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }
    }

}