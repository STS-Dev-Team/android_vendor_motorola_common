package com.motorola.motosimuihelper;

import android.app.Service;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccFileHandler;
import android.telephony.ServiceState;
import android.provider.Settings;
import java.lang.reflect.Field;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ShowSimStatusActivity  extends Service {
    static final String TAG = "MotoSimUiHelper";

    private static String ACTION_SIM_SHOW = "com.motorola.motosimuihelper.SIM_SHOW_INTENT";
    private static boolean DBG = false;

    static final int EF_HPLMNACT_ID = 0x6f62;
    static final int COMMAND_READ_BINARY = 0xb0;

    static final int DEFAULT_DELAY = 10000;

    private PhoneProxy mPhone = null;
    private CommandsInterface mCM = null;
    private Context mContext = null;
    private ShowSimStatusReceiver mShowSimStatusReceiver;
    private boolean mSimLoaded = false;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {

                case 1:
                    if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] ----- ACTION SIM_STATE_CHANGE :: EVENT_READ_RECORD_DONE Message");
                    AsyncResult localAsyncResult = (AsyncResult)paramMessage.obj;
                    IccIoResult ioResult = (IccIoResult)localAsyncResult.result;
                    if (localAsyncResult.exception == null) {
                        if (ioResult.getException() == null) {
                            if ((0x40 & ioResult.payload[3]) == 0) {
                                Log.e(TAG, "[SHOWSIMSTATUS] ERROR: EUTRAN is not avaliable");
                            }
                            else {
                                if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] MSG: EUTRAN is avaliable");
                                String lineNum = ShowSimStatusActivity.this.mPhone.getLine1Number();
                                if ((lineNum != null) && (!lineNum.startsWith("00000"))) {
                                    Log.d(TAG, "[SHOWSIMSTATUS] MSG: SIM is a valid activated Verizon 4G SIM");
                                    ShowSimStatusActivity.this.mSimLoaded = true;
                                }
                            }
                        }
                        else {
                            Log.e(TAG, "[SHOWSIMSTATUS] ERROR: EFHPLMNWACT not accessible.");
                        }
                    }
                    else {
                        Log.e(TAG, "[SHOWSIMSTATUS] ERROR: Read icc i/o exception");
                    }
                    break;
            }
        }

    };

    private int checkSimStatus() {
        TelephonyManager tm = (TelephonyManager)this.mPhone.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, "[SHOWSIMSTATUS] MSG: SIM operator " + tm.getSimOperator());
        String simOperator = tm.getSimOperator();
        int i;

        if (simOperator != null) {
                if ((simOperator.equals("311480")) || (simOperator.equals("20404"))) {
                    if (!this.mPhone.needsOtaServiceProvisioning()) {
                        if (this.mPhone.getIccCard().isApplicationOnIcc(AppType.APPTYPE_USIM)) {
                            this.mCM.iccIO(COMMAND_READ_BINARY, EF_HPLMNACT_ID, IccConstants.MF_SIM + IccConstants.DF_GSM, 0, 0, 5, null, null, this.mHandler.obtainMessage(1));
                            i = 3;
                        }
                        else {
                            Log.d(TAG, "[SHOWSIMSTATUS] ERROR: No usim application on ICC card");
                            i = 0;
                        }
                    }
                    else {
                        Log.d(TAG, "[SHOWSIMSTATUS] ERROR: The icc card needs to be provisioned");
                        i = 2;
                    }
                }
                else {
                    Log.d(TAG, "[SHOWSIMSTATUS] ERROR: Unkown SIM operator (" + simOperator + ")");
                    i = 1;
                }
        }
        else
             i = 2;
        
        return i;
    }

    private Runnable delayCheckSimStatus = new Runnable() {
        @Override
        public void run() {
            int i = -1;
            if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] ----- ACTION SIM_STATE_CHANGED START");
            i = checkSimStatus();
            if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] ----- ACTION SIM_STATE_CHANGED [CheckSimStatus == " + i + "]");
        }
    };

    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] onCreate");
        init();
    }

    private void init() {
        this.mContext = this;
        this.mPhone = (PhoneProxy)PhoneFactory.getDefaultPhone();
        try {
            mShowSimStatusReceiver = new ShowSimStatusReceiver();
            IntentFilter localIntentFilter = new IntentFilter();
            localIntentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            Intent localIntent = registerReceiver(mShowSimStatusReceiver, localIntentFilter);
            PhoneProxy localPhoneProxy = (PhoneProxy)ShowSimStatusActivity.this.mPhone;

            if (localPhoneProxy != null) {

                try {
                    Field localField = PhoneProxy.class.getDeclaredField("mCommandsInterface");
                    if (localField != null)
                        localField.setAccessible(true);

                    try {
                        ShowSimStatusActivity.this.mCM = ((CommandsInterface)localField.get(localPhoneProxy));
                    }
                    catch (IllegalAccessException localIllegalAccessException) {
                        Log.e(TAG, "[SHOWSIMSTATUS] ERROR: Cannot access CommandsInterface");
                        return;
                    }
                }
                catch (NoSuchFieldException localNoSuchFieldException) {
                    Log.e(TAG, "[SHOWSIMSTATUS] ERROR: No CommandsInterface found");
                    return;
                }
            }
        }
        catch (Exception ex2) {
            Log.e(TAG, "**** Exception in init(): " + ex2);
        }
    }

    private class ShowSimStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = -1;

            if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                String iccState = (String)intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                if (iccState != null) {
                    if (DBG) Log.d(TAG, "[SHOWSIMSTATUS] ----- ACTION SIM_STATE_CHANGED [iccCardState = " + iccState.toString() + "]");
                    if (!mSimLoaded && iccState.equals("LOADED")) {
	                mHandler.postDelayed(delayCheckSimStatus, DEFAULT_DELAY);
                    }
                }
            }
        }
    }

}

