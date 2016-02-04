package com.android.red.packet;

import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

public class MonitorService extends AccessibilityService {
    static final String TAG = "_hongbao";
    /** ΢�ŵİ��� */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    // ֧����
    static final String ALIPAY_PACKAGENAME = "com.eg.android.AlipayGphone";
    /** �����Ϣ�Ĺؼ��� */
    static final String HONGBAO_TEXT_KEY = "[΢�ź��]";
    // ��һ��ҳ��
    public static final String ALIPAY_XIUXIU_CLASS = "com.alipay.android.wallet.newyear.activity.MonkeyYearActivity";
    private static final int MSG_NODE_CLICK = 0x110;
    private static final int XIU1XIU_CLICK_TIME = 3 * 1000;

    Handler mHandler = new Handler() {
        @SuppressLint("NewApi") public void handleMessage(Message msg) {
            if(msg.what == MSG_NODE_CLICK){
                Log.i(TAG, "--->��һ��, ��һ��");
                AccessibilityNodeInfo btnNode = (AccessibilityNodeInfo) msg.obj;
                btnNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                List<AccessibilityAction> actions = btnNode.getActionList();
                if (actions == null) {
                    Log.i(TAG, "--->��һ��, ��һ�� actions null");
                } else {
                    Log.i(TAG, "--->��һ��, ��һ�� actions " + actions.size());
                }
                AccessibilityNodeInfo parent = btnNode.getParent();
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    Log.i(TAG, "--->��һ��, ��һ�� parent null");
                }
                mHandler.postDelayed(xiu1XiuRunnable, XIU1XIU_CLICK_TIME);
            }
        };
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.d(TAG, "�¼�---->" + event);

        // ֪ͨ���¼�
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            /**
             * ͨ���������ܴ�֪ͨ�����ȶ�
             */
            // Log.d(TAG, "TYPE_NOTIFICATION_STATE_CHANGED");
            // List<CharSequence> texts = event.getText();
            // if (!texts.isEmpty()) {
            // for (CharSequence t : texts) {
            // String text = String.valueOf(t);
            // if (text.contains(HONGBAO_TEXT_KEY)) {
            // LuckyApplication.unlockScreen(getApplication());
            // openNotification(event);
            // break;
            // }
            // }
            // }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED | TYPE_VIEW_FOCUSED");
            Log.w(TAG, "�¼�autoGetMoney:" + LuckyApplication.autoGetMoney);
            if (event.getPackageName().toString().equals(WECHAT_PACKAGENAME)) {
                openHongBao(event);
            } else if (event.getPackageName().toString().equals(ALIPAY_PACKAGENAME)) {
                xiu1Xiu(event);
            }
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

    /**
     * ��֪ͨ����Ϣ
     * 
     * @param event
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotification(AccessibilityEvent event) {
        Log.d(TAG, "--->��΢��֪ͨ��openNotification");
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            Log.d(TAG, "--->��΢��֪ͨ��notification data null");
            return;
        }

        // �����Ǿ�������΢�ŵ�֪ͨ����Ϣ��
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            LuckyApplication.autoGetMoney = true;
            pendingIntent.send();
            Log.d(TAG, "--->��΢��֪ͨ��send");
        } catch (PendingIntent.CanceledException e) {
            Log.d(TAG, "--->��΢��֪ͨ��CanceledException");
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param event
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        Log.d(TAG, "openHongBao");
        Log.w(TAG, "event.getClassName():" + event.getClassName());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            LuckyApplication.autoGetMoney = false;
            openRedPocket();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            LuckyApplication.autoGetMoney = false;
            // ����������ϸ�ļ�¼����
            // nonething
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName()) || "android.widget.ListView".equals(event.getClassName())) {
            // ���������,ȥ���к��
            Log.w(TAG, "�������autoGetMoney:" + LuckyApplication.autoGetMoney);
            if (LuckyApplication.autoGetMoney) {
                LuckyApplication.autoGetMoney = false;
                getRedPocket();
            }
        }
    }

    /**
     * ���������
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openRedPocket() {
        Log.d(TAG, "--->����");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LuckyApplication.autoGetMoney = false;
            Log.w(TAG, "--->������rootWindowΪ��");
            return;
        }
        /*
         * List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("����"); for
         * (AccessibilityNodeInfo n : list) { Log.i(TAG, "-->����:" + n);
         * LuckyApplication.autoGetMoney = false;
         * n.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
         */
        int parent_count = nodeInfo.getChildCount();
        Log.i(TAG, "--->������parent_count:" + parent_count);
        for (int i = 0; i < parent_count; i++) {
            AccessibilityNodeInfo info = nodeInfo.getChild(i);
            Log.i(TAG, "--->������info isClick:" + info);
            if (info.isClickable()) {
                LuckyApplication.autoGetMoney = false;
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
        
        LuckyApplication.releaseScreen(getApplication());
        /*
         * while(parent != null){ Log.i(TAG, "parent isClick:"+parent); if(parent.isClickable()){
         * LuckyApplication.autoGetMoney = false;
         * parent.performAction(AccessibilityNodeInfo.ACTION_CLICK); break; } parent =
         * parent.getParent(); }
         */
    }

    /**
     * ���������
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getRedPocket() {
        Log.d(TAG, "--->����");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LuckyApplication.autoGetMoney = false;
            Log.w(TAG, "--->������rootWindowΪ��");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("��ȡ���");
        if (!list.isEmpty()) {
            // ���µĺ������
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                if (parent != null) {
                    LuckyApplication.autoGetMoney = false;
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i(TAG, "--->��ȡ����ɹ�:" + parent);
                    break;
                }
            }
        } else {
            Log.d(TAG, "--->������������view null��");
        }
    }

    /***********************************************/
    /**
     * ��һ��
     */
    private void xiu1Xiu(AccessibilityEvent event) {
        if (ALIPAY_XIUXIU_CLASS.equals(event.getClassName())) {
            Log.i(TAG, "--->��һ��");
            AccessibilityNodeInfo parent = getRootInActiveWindow();
            if (parent == null) {
                LuckyApplication.autoGetMoney = false;
                Log.w(TAG, "--->��һ�ݣ�rootWindowΪ��");
                return;
            }
            
            AccessibilityNodeInfo btn = getButtonInfo(parent);
            if (btn != null) {
                dontStopClick(btn);
            }
        } else {
            if (xiu1XiuRunnable != null) {
                mHandler.removeCallbacks(xiu1XiuRunnable);
            }
        }
    }
    
    /**
     * ɸѡ�����ݵ�button�����в�ͣ�ĵ��
     * 
     * @param parent
     * @return
     */
    private AccessibilityNodeInfo getButtonInfo(AccessibilityNodeInfo parent) {
        if (parent != null && parent.getChildCount() > 0) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AccessibilityNodeInfo node = parent.getChild(i);
                if ("android.widget.Button".equals(node.getClassName())) {
                    return node;
                }
            }
        }
        return null;
    }

    private Xiu1XiuRunnable xiu1XiuRunnable;

    private void dontStopClick(final AccessibilityNodeInfo btn) {
        if (xiu1XiuRunnable == null) {
            xiu1XiuRunnable = new Xiu1XiuRunnable(btn);
        }
        mHandler.postDelayed(xiu1XiuRunnable, XIU1XIU_CLICK_TIME);
    }
    
    private class Xiu1XiuRunnable implements Runnable {
        private AccessibilityNodeInfo btn;
        
        public Xiu1XiuRunnable(AccessibilityNodeInfo btn) {
            super();
            this.btn = btn;
        }

        @Override
        public void run() {
            Message m = mHandler.obtainMessage(MSG_NODE_CLICK, btn);
            mHandler.sendMessage(m);
        }
    }

}
