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

    /** ΢�ŵİ��� */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /** �����Ϣ�Ĺؼ��� */
    static final String HONGBAO_TEXT_KEY = "[΢�ź��]";

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.d(TAG, "�¼�---->" + event);

        // ֪ͨ���¼�
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
            Log.w(TAG, "�¼�autoGetMoney:" + LuckyApplication.autoGetMoney);
            openHongBao(event);
        }
    }

    /*
     * @Override protected boolean onKeyEvent(KeyEvent event) { //return super.onKeyEvent(event);
     * return true; }
     */

    @Override
    public void onInterrupt() {
        // Toast.makeText(this, "�ж����������", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Toast.makeText(this, "�������������", Toast.LENGTH_SHORT).show();
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

    /** ��֪ͨ����Ϣ */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }

        // �����Ǿ�������΢�ŵ�֪ͨ����Ϣ��
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
            // ����������ϸ�ļ�¼����
            // nonething
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName()) || "android.widget.ListView".equals(event.getClassName())) {
            // ���������,ȥ���к��
            Log.w(TAG, "�������autoGetMoney:" + LuckyApplication.autoGetMoney);
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
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        /*
         * List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("����"); for
         * (AccessibilityNodeInfo n : list) { Log.i(TAG, "-->����:" + n);
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
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("��ȡ���");
        if (!list.isEmpty()) {
            // ���µĺ������
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->��ȡ���:" + parent);
                if (parent != null) {
                    LuckyApplication.autoGetMoney = false;
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }



}
