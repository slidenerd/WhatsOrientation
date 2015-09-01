package slidenerd.vivz.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "VIVZ";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private static final double GRAVITY_THRESHOLD =
            SensorManager.STANDARD_GRAVITY / 2;
    private SensorManager sensorManager;
    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;
    private RadioGroup sensorSelector;
    private TextView selectedSensorValue;
    private TextView orientationValue;
    private TextView sensorXLabel;
    private TextView sensorXValue;
    private TextView sensorYLabel;
    private TextView sensorYValue;
    private TextView sensorZLabel;
    private TextView sensorZValue;
    private TextView statusMessage;
    private int selectedSensorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep the screen on so that changes in orientation can be easily observed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
// Set the volume control to use the same stream as TTS which allows // the user to easily adjust the TTS volume this.setVolumeControlStream(TTS_STREAM);
// Get a reference to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
// Initialize references to the UI views that will be updated in the
// code
        sensorSelector = (RadioGroup) findViewById(R.id.sensorSelector);
        selectedSensorValue = (TextView) findViewById(R.id.selectedSensorValue);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXLabel = (TextView) findViewById(R.id.sensorXLabel);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYLabel = (TextView) findViewById(R.id.sensorYLabel);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZLabel = (TextView) findViewById(R.id.sensorZLabel);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);
        statusMessage = (TextView) findViewById(R.id.status_message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasGravitySensor() {
        List<Sensor> listSensors = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        return !listSensors.isEmpty();
    }

    public boolean hasMagnetometer() {
        List<Sensor> listSensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        return !listSensors.isEmpty();
    }

    public boolean hasAccelerometer() {
        List<Sensor> listSensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        return !listSensors.isEmpty();
    }

    public boolean hasRotationVector() {
        List<Sensor> listSensors = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        return !listSensors.isEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSelectedSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
// Unregister updates from sensors
        sensorManager.unregisterListener(this);
    }

    private void updateSelectedSensor() {
        // Clear any current registrations sensorManager.unregisterListener(this);
        // Determine which radio button is currently selected and enable the // appropriate sensors
        selectedSensorId = sensorSelector.getCheckedRadioButtonId();
        if (selectedSensorId == R.id.accelerometerMagnetometer) {
            if (hasAccelerometer() && hasMagnetometer()) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), RATE);
            } else {
                statusMessage.setText(R.string.no_accelerometer_magnetometer);
            }

        } else if (selectedSensorId == R.id.gravityMagnetometer) {
            if (hasGravitySensor() && hasMagnetometer()) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), RATE);
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), RATE);
            } else {
                statusMessage.setText(R.string.no_gravitometer_magnetometer);
            }
        } else if ((selectedSensorId == R.id.gravitySensor)) {
            if (hasGravitySensor()) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), RATE);
            } else {
                statusMessage.setText(R.string.no_gravitometer);
            }
        } else {
            if (hasRotationVector()) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), RATE);
            } else {
                statusMessage.setText(R.string.no_rotation_vector);
            }
        }
        // Update the label with the currently selected sensor
        RadioButton selectedSensorRadioButton = (RadioButton) findViewById(selectedSensorId);
        selectedSensorValue.setText(selectedSensorRadioButton.getText());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                sensorXLabel.setText(R.string.xAxisLabel);
                sensorXValue.setText(String.valueOf(event.values[0]));
                sensorYLabel.setText(R.string.yAxisLabel);
                sensorYValue.setText(String.valueOf(event.values[1]));
                sensorZLabel.setText(R.string.zAxisLabel);
                sensorZValue.setText(String.valueOf(event.values[2]));
                sensorYLabel.setVisibility(View.VISIBLE);
                sensorYValue.setVisibility(View.VISIBLE);
                sensorZLabel.setVisibility(View.VISIBLE);
                sensorZValue.setVisibility(View.VISIBLE);
                if (selectedSensorId == R.id.gravitySensor) {
                    if (event.values[2] >= GRAVITY_THRESHOLD) {
                        onFaceUp();
                    } else if (event.values[2] <= (GRAVITY_THRESHOLD * -1)) {
                        onFaceDown();
                    }
                } else {
                    accelerationValues = event.values.clone();
                    rotationMatrix = generateRotationMatrix();
                    if (rotationMatrix != null) {
                        determineOrientation(rotationMatrix);
                    }
                }
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerationValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                if (rotationMatrix != null) {
                    determineOrientation(rotationMatrix);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                if (rotationMatrix != null) {
                    determineOrientation(rotationMatrix);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix,
                        event.values);
                determineOrientation(rotationMatrix);
                break;

        }

    }

    private void determineOrientation(float[] rotationMatrix) {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);
        double azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);

        sensorXLabel.setText(R.string.azimuthLabel);
        sensorXValue.setText(String.valueOf(azimuth));
        sensorYLabel.setText(R.string.pitchLabel);
        sensorYValue.setText(String.valueOf(pitch));
        sensorZLabel.setText(R.string.rollLabel);
        sensorZValue.setText(String.valueOf(roll));
        sensorYLabel.setVisibility(View.VISIBLE);
        sensorYValue.setVisibility(View.VISIBLE);
        sensorZLabel.setVisibility(View.VISIBLE);
        sensorZValue.setVisibility(View.VISIBLE);
        if (pitch <= 10) {
            if (Math.abs(roll) >= 170) {
                onFaceDown();
            } else if (Math.abs(roll) <= 10) {
                onFaceUp();
            } else {
                onSomeWhere();
            }
        }
    }

    private void onSomeWhere() {
        statusMessage.setText("Your Phone Is Spinning Somewhere, Lol");
    }

    private float[] generateRotationMatrix() {
        float[] rotationMatrix = null;
        if (accelerationValues != null && magneticValues != null) {
            rotationMatrix = new float[16];
            boolean rotationMatrixGenerated;
            rotationMatrixGenerated =
                    SensorManager.getRotationMatrix(rotationMatrix, null,
                            accelerationValues,
                            magneticValues);
            if (!rotationMatrixGenerated) {
                Log.w(TAG, getString(R.string.rotationMatrixGenFailureMessage));
                rotationMatrix = null;
            }
        }
        return rotationMatrix;
    }

    private void onFaceDown() {
        if (isFaceUp) {
            statusMessage.setText("Your Phone Has Its Face Down");
            orientationValue.setText(R.string.faceDownText);
            isFaceUp = false;
        }
    }

    private void onFaceUp() {
        if (!isFaceUp) {
            statusMessage.setText("Your Phone Has Its Face Up");
            orientationValue.setText(R.string.faceUpText);
            isFaceUp = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, String.format("Accuracy for sensor %s = %d", sensor.getName(), accuracy));
    }

    public void onSensorSelectorClick(View view) {
        updateSelectedSensor();
    }
}
