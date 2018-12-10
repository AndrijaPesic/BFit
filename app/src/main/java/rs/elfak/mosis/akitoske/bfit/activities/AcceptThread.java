package rs.elfak.mosis.akitoske.bfit.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class AcceptThread extends Thread {


    boolean start;
    String userID;
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private  InputStream mmInStream;
    private  OutputStream mmOutStream;
    private byte[] mmBuffer;
    public SettingsActivity mSettingsActivity;
    // public boolean stop;
    public java.util.concurrent.Semaphore semaphore;
    public boolean accept;
    FirebaseAuth mAuth;

    public Handler mHandler;

    public void setActivity(SettingsActivity settingsActivity)
    {
        this.mSettingsActivity = settingsActivity;
    }




    public AcceptThread(Handler handler,String userID) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        this.start=true;
        this.userID = userID;
        semaphore=new java.util.concurrent.Semaphore(0b0);

        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler=handler;
        mAuth=FirebaseAuth.getInstance();
        String aString="BFit";
        UUID uuid = UUID.nameUUIDFromBytes(aString.getBytes());

        try {
            Log.i("tag", "create server");
            // MY_UUID is the app's UUID string, also used by the client code.

            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BFit", uuid);
        }
        catch (IOException e)
        {
            Log.e("String1", "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;

        Log.i("tag", "run server");

        // Keep listening until exception occurs or a socket is returned.
        while (start) {
            try {

                socket = mmServerSocket.accept();

                Log.d("run","Socket's close()accept");
            } catch (IOException e) {
                Log.e("String2", "Socket's accept() method failed", e);
                //break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.


                // InputStream tmpIn = socket.getInputStream();
                // OutputStream tmpOut= null;
                //tmpOut = socket.getOutputStream();
                // mmInStream = tmpIn;
                // mmOutStream = tmpOut;

                manageMyConnectedSocket(socket);
                try {

                    //????
                    Log.d("close","Socket's close()method");
                    mmServerSocket.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {


        mmBuffer = new byte[28];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        //  while (true) {
        try {
            // Read from the InputStream.
            Log.i("tag", "input");
            InputStream in = socket.getInputStream();
            in.read(mmBuffer);
            String s=new String(mmBuffer);


            // String doc = ....
            // byte[] bytes = doc.getBytes("UTF-8");
            // String doc2 = new String(bytes, "UTF-8");

            Log.i("podaci",s);
            // Send the obtained bytes to the UI activity.
            Message readMsg = mHandler.obtainMessage(ConnectThread.MessageConstants.MESSAGE_READ, mmBuffer.length, -1, mmBuffer);

            readMsg.sendToTarget();

            semaphore.acquire();
            Log.d("server", "odblokiran");

            //saljem ID



            // byte[] buf = new byte[28];
            if(accept)
            {
                mmBuffer = userID.getBytes();
            }
            else {
                String i ="0000000000000000000000000000";
                mmBuffer =i.getBytes();
            }

            OutputStream otp = socket.getOutputStream();
            otp.write(mmBuffer);
            Log.d("server", "poslao");
            // mSettingsActivity.ShowMessage(s);

        } catch (IOException e) {
            Log.d("tag", "Input stream was disconnected", e);
            // break;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Toast.makeText(ConnectThread.this,"cao",)
        // }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            start=false;
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e("string3", "Could not close the connect socket", e);
        }
    }
}
