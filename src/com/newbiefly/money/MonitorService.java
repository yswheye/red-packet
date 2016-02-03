package com.newbiefly.money;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 */
public class MonitorService extends AccessibilityService {
    static final String TAG = "_hongbao";

    /** 微信的包名 */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /** 红包消息的关键字 */
    static final String HONGBAO_TEXT_KEY = "[微信红包]";

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.d(TAG, "事件---->" + event);

        // 通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY)) {
                        LuckyApplication.unlockScreen(getApplication());
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            Log.w(TAG, "事件autoGetMoney:" + LuckyApplication.autoGetMoney);
            openHongBao(event);
        }
    }

    /*
     * @Override protected boolean onKeyEvent(KeyEvent event) { //return super.onKeyEvent(event);
     * return true; }
     */

    @Override
    public void onInterrupt() {
        // Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /** 打开通知栏消息 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }

        // 以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            LuckyApplication.autoGetMoney = true;
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        Log.w(TAG, "event.getClassName():" + event.getClassName());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            LuckyApplication.autoGetMoney = false;
            checkKey1();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            LuckyApplication.autoGetMoney = false;
            // 拆完红包后看详细的纪录界面
            // nonething
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName()) || "android.widget.ListView".equals(event.getClassName())) {
            // 在聊天界面,去点中红包
            Log.w(TAG, "聊天界面autoGetMoney:" + LuckyApplication.autoGetMoney);
            if (LuckyApplication.autoGetMoney) {
                LuckyApplication.autoGetMoney = false;
                checkKey2();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LuckyApplication.autoGetMoney = false;
            Log.w(TAG, "rootWindow为空");
            return;
        }
        /*
         * List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包"); for
         * (AccessibilityNodeInfo n : list) { Log.i(TAG, "-->拆红包:" + n);
         * LuckyApplication.autoGetMoney = false;
         * n.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
         */
        int parent_count = nodeInfo.getChildCount();
        Log.i(TAG, "parent_count:" + parent_count);
        for (int i = 0; i < parent_count; i++) {
            AccessibilityNodeInfo info = nodeInfo.getChild(i);
            Log.i(TAG, "info isClick:" + info);
            if (info.isClickable()) {
                LuckyApplication.autoGetMoney = false;
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
        /*
         * while(parent != null){ Log.i(TAG, "parent isClick:"+parent); if(parent.isClickable()){
         * LuckyApplication.autoGetMoney = false;
         * parent.performAction(AccessibilityNodeInfo.ACTION_CLICK); break; } parent =
         * parent.getParent(); }
         */
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LuckyApplication.autoGetMoney = false;
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (!list.isEmpty()) {
            // 最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    LuckyApplication.autoGetMoney = false;
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }



}
