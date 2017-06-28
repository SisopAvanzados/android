package com.example.brian.Estacionamiento;

import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;




public class elegir_Lugar extends AppCompatActivity implements SensorEventListener{

    int estadoLugar1=0; //1 libre 2 reservado 3 ocupado
    int estadoLugar2=0;
    int estadoLugar3=0;
    int estadoLugar4=0;
    int cantReservas=0;
    private long last_update = 0, last_movement = 0;
    private float prevX = 0, prevY = 0, prevZ = 0;
    private float curX = 0, curY = 0, curZ = 0;


    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //servicio UUID
    private static String address = "20:17:02:15:44:13"; //MAC de nuestra placa bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir__lugar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_elegir__lugar);

        final Button Lugar1 = (Button)findViewById(R.id.Lugar1);
        final Button Lugar2 = (Button)findViewById(R.id.Lugar2);
        final Button Lugar3 = (Button)findViewById(R.id.Lugar3);
        final Button Lugar4 = (Button)findViewById(R.id.Lugar4);

        final TextView texto = (TextView) findViewById(R.id.editText);

        //ENTRADA AL BLUETOOTH
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");
                    if(endOfLineIndex>0){
                        String dataInPrint = recDataString.substring(recDataString.indexOf("#")+1, endOfLineIndex);

                        if(estadoLugar1==2 && Integer.parseInt(dataInPrint.substring(0,1))==1)
                            Toast.makeText(getBaseContext(), "Tiempo límite de reserva alcanzado", Toast.LENGTH_SHORT).show();
                        else if(estadoLugar2==2 && Integer.parseInt(dataInPrint.substring(2,3))==1)
                            Toast.makeText(getBaseContext(), "Tiempo límite de reserva alcanzado", Toast.LENGTH_SHORT).show();
                        else if(estadoLugar3==2 && Integer.parseInt(dataInPrint.substring(4,5))==1)
                            Toast.makeText(getBaseContext(), "Tiempo límite de reserva alcanzado", Toast.LENGTH_SHORT).show();
                        else if(estadoLugar4==2 && Integer.parseInt(dataInPrint.substring(6,7))==1)
                            Toast.makeText(getBaseContext(), "Tiempo límite de reserva alcanzado", Toast.LENGTH_SHORT).show();

                        estadoLugar1=(Integer.parseInt(dataInPrint.substring(0,1)));
                        estadoLugar2=(Integer.parseInt(dataInPrint.substring(2,3)));
                        estadoLugar3=(Integer.parseInt(dataInPrint.substring(4,5)));
                        estadoLugar4=(Integer.parseInt(dataInPrint.substring(6,7)));

                        if(estadoLugar1!=2 && estadoLugar2!=2 && estadoLugar3!=2 && estadoLugar4!=2)
                            cantReservas=0;

                        if((estadoLugar1==3 && estadoLugar2==3 && estadoLugar3==3 && estadoLugar4==3) || (estadoLugar1+estadoLugar2+estadoLugar3+estadoLugar4)==11)
                            texto.setText("ESTACIONAMIENTO LLENO");
                        else
                            texto.setText("HAY LUGAR");

                        recDataString.delete(0, recDataString.length());
                        asignarColor(estadoLugar1, Lugar1);
                        asignarColor(estadoLugar2, Lugar2);
                        asignarColor(estadoLugar3, Lugar3);
                        asignarColor(estadoLugar4, Lugar4);
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    private void asignarColor(int estadoLugar, Button lugar) {
        if(estadoLugar==1)
            lugar.setBackgroundColor(getResources().getColor(R.color.verde));
        else if(estadoLugar==2)
            lugar.setBackgroundColor(getResources().getColor(R.color.azul));
        else if(estadoLugar==3)
            lugar.setBackgroundColor(getResources().getColor(R.color.rojo));
    }

    public void interactuar(final View view){

        final Button Lugar1 = (Button)findViewById(R.id.Lugar1);
        final Button Lugar2 = (Button)findViewById(R.id.Lugar2);
        final Button Lugar3 = (Button)findViewById(R.id.Lugar3);
        final Button Lugar4 = (Button)findViewById(R.id.Lugar4);

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Acción requerida");
        if(view.getId()==Lugar1.getId() && estadoLugar1==1){
            builder.setMessage("¿Desea reservar?");
        }
        else if(view.getId()==Lugar1.getId() && estadoLugar1==2)
            builder.setMessage("¿Desea cancelar reserva?");

//-----------------------------------------
        if(view.getId()==Lugar2.getId() && estadoLugar2==1){
            builder.setMessage("¿Desea reservar?");
        }
        else if(view.getId()==Lugar2.getId() && estadoLugar2==2)
            builder.setMessage("¿Desea cancelar reserva?");
//-----------------------------------------
        if(view.getId()==Lugar3.getId() && estadoLugar3==1){
            builder.setMessage("¿Desea reservar?");
        }
        else if(view.getId()==Lugar3.getId() && estadoLugar3==2)
            builder.setMessage("¿Desea cancelar reserva?");
//-----------------------------------------
        if(view.getId()==Lugar4.getId() && estadoLugar4==1){
            builder.setMessage("¿Desea reservar?");
        }
        else if(view.getId()==Lugar4.getId() && estadoLugar4==2)
            builder.setMessage("¿Desea cancelar reserva?");
//-----------------------------------------
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int witch){

                if(view.getId()==Lugar1.getId()) {
                    if(estadoLugar1==3){
                        Toast.makeText(getBaseContext(), "No puede reservar un lugar ocupado", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==1 && estadoLugar1!=2)  //si ya se realizó una reserva y este lugar no esta reservado, no puede reservar
                        Toast.makeText(getBaseContext(), "No puede reservar 2 lugares", Toast.LENGTH_SHORT).show();
                    else if(cantReservas==1 && estadoLugar1==2) { //si ya se realizó una reserva y este lugar esta reservado, cancelo la reserva
                        mConnectedThread.write("1");
                        cantReservas=0;
                        Toast.makeText(getBaseContext(), "La reserva fue cancelada", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==0 && estadoLugar1==1){
                        mConnectedThread.write("1");
                        cantReservas=1;
                        Toast.makeText(getBaseContext(), "La reserva fue realizada", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(view.getId()==Lugar2.getId()){
                    if(estadoLugar2==3){
                        Toast.makeText(getBaseContext(), "No puede reservar un lugar ocupado", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==1 && estadoLugar2!=2)  //si ya se realizó una reserva y este lugar no esta reservado, no puede reservar
                        Toast.makeText(getBaseContext(), "No puede reservar 2 lugares", Toast.LENGTH_SHORT).show();
                    else if(cantReservas==1 && estadoLugar2==2) { //si ya se realizó una reserva y este lugar esta reservado, cancelo la reserva
                        mConnectedThread.write("2");
                        cantReservas=0;
                        Toast.makeText(getBaseContext(), "La reserva fue cancelada", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==0 && estadoLugar2==1){
                        mConnectedThread.write("2");
                        cantReservas=1;
                        Toast.makeText(getBaseContext(), "La reserva fue realizada", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(view.getId()==Lugar3.getId()){
                    if(estadoLugar3==3){
                        Toast.makeText(getBaseContext(), "No puede reservar un lugar ocupado", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==1 && estadoLugar3!=2)  //si ya se realizó una reserva y este lugar no esta reservado, no puede reservar
                        Toast.makeText(getBaseContext(), "No puede reservar 2 lugares", Toast.LENGTH_SHORT).show();
                    else if(cantReservas==1 && estadoLugar3==2) { //si ya se realizó una reserva y este lugar esta reservado, cancelo la reserva
                        mConnectedThread.write("3");
                        cantReservas=0;
                        Toast.makeText(getBaseContext(), "La reserva fue cancelada", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==0 && estadoLugar3==1){
                        mConnectedThread.write("3");
                        cantReservas=1;
                        Toast.makeText(getBaseContext(), "La reserva fue realizada", Toast.LENGTH_SHORT).show();
                    }

                }
                else if(view.getId()==Lugar4.getId()){
                    if(estadoLugar4==3){
                        Toast.makeText(getBaseContext(), "No puede reservar un lugar ocupado", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==1 && estadoLugar4!=2)  //si ya se realizó una reserva y este lugar no esta reservado, no puede reservar
                        Toast.makeText(getBaseContext(), "No puede reservar 2 lugares", Toast.LENGTH_SHORT).show();
                    else if(cantReservas==1 && estadoLugar4==2) { //si ya se realizó una reserva y este lugar esta reservado, cancelo la reserva
                        mConnectedThread.write("4");
                        cantReservas=0;
                        Toast.makeText(getBaseContext(), "La reserva fue cancelada", Toast.LENGTH_SHORT).show();
                    }
                    else if(cantReservas==0 && estadoLugar4==1){
                        mConnectedThread.write("4");
                        cantReservas=1;
                        Toast.makeText(getBaseContext(), "La reserva fue realizada", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int witch){
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        TextView editText = (TextView) findViewById(R.id.editText);
    }

    @Override
    protected void onStop() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
        super.onStop();
    }

    public void onResume(){
        super.onResume();

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establecer la conexión con el socket
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //cierra la conexión
            btSocket.close();
        } catch (IOException e2) {
        }
    }

    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
       TextView editText = (TextView) findViewById(R.id.editText);
        synchronized (this) {
            long current_time = event.timestamp;

            curX = event.values[0];
            curY = event.values[1];
            curZ = event.values[2];

            if (prevX == 0 && prevY == 0 && prevZ == 0) {
                last_update = current_time;
                last_movement = current_time;
                prevX = curX;
                prevY = curY;
                prevZ = curZ;
            }

            long time_difference = current_time - last_update;
            if (time_difference > 0) {
                float movement = Math.abs((curX + curY + curZ) - (prevX - prevY - prevZ)) / time_difference;
                int limit = 1500;
                float min_movement = 3.6E-6f;
                if (movement > min_movement) {
                    if (current_time - last_movement >= limit) {
                        Toast.makeText(getApplicationContext(), "Hay movimiento de " + movement, Toast.LENGTH_SHORT).show();
                        if(estadoLugar3==2)
                            mConnectedThread.write("5");
                        if(estadoLugar4==2)
                            mConnectedThread.write("6");
                    }
                    last_movement = current_time;
                }
                prevX = curX;
                prevY = curY;
                prevZ = curZ;
                last_update = current_time;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }



}
