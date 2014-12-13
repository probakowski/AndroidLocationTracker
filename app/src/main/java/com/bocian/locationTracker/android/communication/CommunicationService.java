package com.bocian.locationTracker.android.communication;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.bocian.locationTracker.android.location.TrackerLocationService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class CommunicationService extends IntentService {

    public static final String LOCK_NAME_STATIC = "com.bocian.locationTracker.android.STATIC_LOCK";
    private static PowerManager.WakeLock lockStatic = null;
    private TrackerLocationServiceConnection conn;

    private TrackerLocationService trackerLocationService;


    public CommunicationService() {
        super("CommunicationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationTracker", "CommunicationService.onCreate");

        bindToTrackerLocationService();

        Log.d("LocationTracker", "CommunicationService.onCreate end");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    Semaphore s = new Semaphore(1);

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("LocationTracker", "CommunicationService: onHandleIntent");



        Log.d("LocationTracker", "CommunicationService: onHandleIntent end");

//        lockStatic.release();
    }

    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    private void bindToTrackerLocationService() {


        Log.d("LocationTracker", "CommunicationService.bindToTrackerLocationService");
        conn = new TrackerLocationServiceConnection();
        Intent intent = new Intent(this, TrackerLocationService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.d("LocationTracker", "CommunicationService.bindToTrackerLocationService end");
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }


    class TrackerLocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("LocationTracker", "TrackerLocationServiceConnection: onServiceConnected");
            trackerLocationService = ((TrackerLocationService.LocalBinder) iBinder).getService();

            LinkedBlockingQueue<Location> queue = trackerLocationService.getQueue();
            while (!queue.isEmpty()) {
                Location poll = queue.poll();
                Log.d("LocationTracker", "CommunicationService: onHandleIntent polled:" + poll);
            }

            Log.d("LocationTracker", "TrackerLocationServiceConnection: onServiceConnected end");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("LocationTracker", "TrackerLocationServiceConnection: onServiceDisconnected");
            trackerLocationService = null;
        }
    }
}