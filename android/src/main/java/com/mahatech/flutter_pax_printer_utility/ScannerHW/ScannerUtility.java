package com.mahatech.flutter_pax_printer_utility.ScannerHW;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mahatech.flutter_pax_printer_utility.R;
import com.pax.dal.IDAL;
import com.pax.dal.IScannerHw;
import com.pax.dal.entity.ScanResult;
import com.pax.dal.exceptions.ScannerHwDevException;
import com.pax.neptunelite.api.NeptuneLiteUser;

public class ScannerUtility {
    private IScannerHw mIScannerHw;
    public static final int INT_WHAT = 0;
    private static Context _context;
    private static IDAL dal;
    private String code;

    public ScannerUtility(Context context){
        this._context = context;
    }

    public String onCreate(){
        this._context = _context.getApplicationContext();
        dal = getDal();
        mIScannerHw = getDal().getScannerHw();
        //Thread scannThread = new Thread(runnable);
        //scannThread.start();
        if (null != mIScannerHw) {
            try {
                mIScannerHw.open();
                ScanResult scanResult = mIScannerHw.read(10000);

                    if (null != scanResult) {
                        return scanResult.getContent();
                    }
                return "Error";
            } catch (ScannerHwDevException e) {
                e.printStackTrace();
                return e.toString();
            }
        }
        return "Errorrrrrrrrr";
    }

    public static IDAL getDal(){
        if(dal == null){
            try {
                long start = System.currentTimeMillis();
                dal = NeptuneLiteUser.getInstance().getDal(_context);
                Log.i("Test","get dal cost:"+(System.currentTimeMillis() - start)+" ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dal;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != mIScannerHw) {
                try {
                    mIScannerHw.open();
                    ScanResult scanResult = mIScannerHw.read(10000);
                    if (!Thread.interrupted()) {
                        if (null != scanResult) {
                            Message message = Message.obtain(handler, INT_WHAT, scanResult);
                            message.sendToTarget();
                        }
                    }
                } catch (ScannerHwDevException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INT_WHAT:
                    ScanResult scanResult = (ScanResult) msg.obj;
                    if (null != scanResult) {
                       code = scanResult.getContent();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
