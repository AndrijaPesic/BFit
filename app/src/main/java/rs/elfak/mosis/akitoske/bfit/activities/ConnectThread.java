package rs.elfak.mosis.akitoske.bfit.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;


public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private  InputStream mmInStream;
    private  OutputStream mmOutStream;
    private byte[] mmBuffer;
    FirebaseAuth mAuth;
    public android.os.Handler  mHandler;



    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }


    public ConnectThread(BluetoothDevice device, Handler cHandler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mHandler=cHandler;
        BluetoothSocket tmp = null;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mmDevice = device;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAuth=FirebaseAuth.getInstance();

        String aString="BFit";
        UUID uuid = UUID.nameUUIDFromBytes(aString.getBytes());


        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            Log.i("tag", "create client");
            tmp = device.createRfcommSocketToServiceRecord(uuid);

        } catch (IOException e) {
            Log.e("StringClient", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.i("tag", "connect client");
            mmSocket.connect();
            // OutputStream tmpOut= mmSocket.getOutputStream();
            //InputStream tmpIn = mmSocket.getInputStream();

            //mmInStream = tmpIn;
            // mmOutStream = tmpOut;

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("ClientString", "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {


        FirebaseUser user = mAuth.getCurrentUser();
        String myID = user.getUid();


        mmBuffer = new byte[28];
        mmBuffer = myID.getBytes();//.getBytes(Charset.forName("UTF-8"));

        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        //while (true) {
        try {
            // Read from the InputStream.
            Log.i("tag", "Output");

            OutputStream otp = mmSocket.getOutputStream();
            otp.write(mmBuffer);
            //numBytes = mmInStream.read(mmBuffer);
            // Send the obtained bytes to the UI activity.
            // Message readMsg = mHandler.obtainMessage(ConnectThread.MessageConstants.MESSAGE_WRITE, 28, -1, mmBuffer);
            // readMsg.sendToTarget();

            //byte[] buf=new byte[1];
            InputStream in = mmSocket.getInputStream();
            in.read(mmBuffer);
            Log.v("klijent", "unpair");
            unpairDevice(mmDevice);
            Message Msg = mHandler.obtainMessage(ConnectThread.MessageConstants.MESSAGE_WRITE, 28, -1, mmBuffer);
            Msg.sendToTarget();
        } catch (IOException e) {
            Log.v("tag", "Input stream was disconnected", e);
            Log.getStackTraceString(e);
            //break;
        }

        //Toast.makeText(ConnectThread.this,"cao",)
        //}
        //mmSocket.getOutputStream().write(Byte.parseByte("cao"));
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {

            mmSocket.close();
            unpairDevice(mmDevice);
        } catch (IOException e) {
            Log.e("StringClient1", "Could not close the client socket", e);
            Log.getStackTraceString(e);
        }
    }
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);



        } catch (Exception e) {
            Log.e("Remove bond", e.getMessage());
        }
    }

}
