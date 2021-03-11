package com.videogo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.constants.ReceiverKeys;
import com.videogo.download.DownLoadTaskRecordAbstract;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.util.LogUtil;
import com.videogo.widget.WaitDialog;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdkcommon.R;

@SuppressLint("Registered")
public class RootActivity extends Activity {

    public final static String TAG = RootActivity.class.getSimpleName();

    protected static Context mContext;

    private Toast mToast = null;

    private boolean mIsTip = true;

    protected int pageKey = -1;
    private WaitDialog mWaitDlg;

    protected static ArrayList<DownLoadTaskRecordAbstract> mDownloadTaskRecordListAbstract = new ArrayList<>();

    protected void showToast(int id) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }
        String text = getString(id);
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, int errCode) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
        if (errorId != 0) {
            text = getString(errorId);
        } else {
            text = text + " (" + errCode + ")";
        }
    }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, String msg) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (!TextUtils.isEmpty(msg)) {
            text = text + " (" + msg + ")";
        }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsTip) {
                    return;
                }

                if (isFinishing()) {
                    return;
                }
                if (text != null && !text.equals("")) {
                    if (mToast == null) {
                        mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
                        mToast.setGravity(Gravity.CENTER, 0, 0);
                    } else {
                        mToast.setText(text);
                    }
                    mToast.show();
                }
            }
        });
    }

    protected int getErrorId(int errorCode) {
        int errorId = this.getResources().getIdentifier("error_code_" + errorCode, "string", this.getPackageName());
        /*
         * Field fieldError; int errorId = 0; try { fieldError =
         * R.string.class.getDeclaredField("error_code_" + errorCode);
         * fieldError.setAccessible(true); R.string string = new R.string(); try { errorId =
         * fieldError.getInt(string); } catch (IllegalAccessException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); } catch (IllegalArgumentException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } } catch (NoSuchFieldException e) { //
         * TODO Auto-generated catch block e.printStackTrace(); }
         */

        return errorId;
    }

    protected void showToast(int res1, int res2, int errCode) {
        String text = res1 != 0 ? getString(res1) : "";
        if (res2 != 0) {
            text = text + ", " + getString(res2);
        }
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                text = getString(errorId);
            } else {
                text = text + " (" + errCode + ")";
            }
        }
        if (text != null) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void setPageKey(int argPageKey) {
        this.pageKey = argPageKey;
    }

    protected void showWaitDialog(String content) {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        if (content != null && !content.equals("")) {
            mWaitDlg.setWaitText(content);
        }
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    protected void showWaitDialog(int resId) {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setWaitText(getString(resId));
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    public void showWaitDialog() {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    public void showCancelableWaitDialog() {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(true);
        mWaitDlg.show();
    }

    public boolean isDialogShowing() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    public void dismissWaitDialog() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            mWaitDlg.dismiss();
        }
    }

    /**
     * @return the isResumed
     */
    // public static boolean isResumed() {
    // return isResumed;
    // }

    // public static void setResumed(boolean isResumed) {
    // RootActivity.isResumed = isResumed;
    // }

    protected String getErrorTip(int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();

        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                errorTip.append(getString(errorId));
            } else {
                errorTip.append(getString(id)).append(" (").append(errCode).append(")");
            }
        } else {
            errorTip.append(getString(id));
        }
        return errorTip.toString();
    }

    protected void hideInputMethod() {
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected void removeHandler(Handler handler) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private static List<Activity> mActivityList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivityList.add(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityList.remove(this);
    }

    public static void exitApp(){
        stopAllDownloadTasks();
        for (Activity activity: mActivityList){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
        System.exit(0);
    }

    private static void stopAllDownloadTasks(){
        for (DownLoadTaskRecordAbstract downloadRecord: mDownloadTaskRecordListAbstract){
            downloadRecord.stopDownloader();
        }
    }

    protected void toast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected static void toastMsg(final String msg){
        if (mContext == null){
            return;
        }
        if (mContext instanceof RootActivity){
            final RootActivity rootActivity = (RootActivity) mContext;
            rootActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rootActivity.toast(msg);
                }
            });
        }
    }

    private AlertDialog mLastDialog = null;
    protected void dialog(final String title, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLastDialog != null && mLastDialog.isShowing()){
                    mLastDialog.dismiss();
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RootActivity.this);
                dialogBuilder.setTitle(title);
                ViewGroup msgLayoutVg = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_dialog_tip, null);
                TextView msgTv = (TextView) msgLayoutVg.findViewById(R.id.tv_tip);
                if (msgTv != null){
                    msgTv.setText(msg);
                }
                dialogBuilder.setView(msgLayoutVg);
                dialogBuilder.setPositiveButton(getApplicationContext().getString(R.string.btn_ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setCancelable(false);
                mLastDialog = dialogBuilder.show();
            }
        });
    }

    private static TaskManager mTaskManager = null;
    protected synchronized static TaskManager getTaskManager(){
        if (mTaskManager == null){
            mTaskManager = new TaskManager();
        }
        return mTaskManager;
    }

    private static int mNotificationId = 1;
    protected static int getUniqueNotificationId(){
        return mNotificationId++;
    }

    /**
     * 显示指定内容的通知
     * @param title 通知标题
     * @param content 通知内容
     */
    protected void showSimpleNotification(int notificationId, String title, String content, boolean clickToCancel){
        LogUtil.d(TAG, "show notification " + notificationId);
        Intent intent = new Intent(mContext, NotificationReceiver.class)
                .putExtra(ReceiverKeys.NOTIFICATION_ID, notificationId);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.videogo_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.videogo_icon))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(clickToCancel ? PendingIntent.getBroadcast(mContext, notificationId, intent, 0) : null);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getIntExtra(ReceiverKeys.NOTIFICATION_ID, -1);
            LogUtil.d(TAG, "onClick, notificationId is " + notificationId);
            DownLoadTaskRecordAbstract downLoadTaskRecord = null;
            for (DownLoadTaskRecordAbstract downLoadTaskRecordAbstract : mDownloadTaskRecordListAbstract){
                if (downLoadTaskRecordAbstract.getNotificationId() == notificationId){
                    LogUtil.d(TAG, "stopped download task which related to notificationId " + notificationId);
                    downLoadTaskRecord = downLoadTaskRecordAbstract;
                    downLoadTaskRecord.stopDownloader();
                }
            }
            if (downLoadTaskRecord != null){
                mDownloadTaskRecordListAbstract.remove(downLoadTaskRecord);
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null){
                notificationManager.cancel(notificationId);
                RootActivity.toastMsg("canceled to downloaded!");
            }
        }
    }

    public static EZOpenSDK getOpenSDK(){
        return EzvizApplication.getOpenSDK();
    }

}
