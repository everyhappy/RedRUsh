package com.example.hk.redrush;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

public class RedService extends AccessibilityService {

    private static final String TAG = "ResService";
    private static final String WECHAT_KEYNAME = "[微信红包]";
    private static final String LUCKMONEYRECEIVUI ="com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String LAUNCHERUI = "com.tencent.mm.ui.LauncherUI";
    private static final String LUCKMONEYDETAILUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private static final int OPENBUTTON = 0;
    private static final int CLOSEBUTTON = 1;
    private  boolean isopen = false;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event){
        final int eventType = event.getEventType();
        Log.d(TAG,"event = "+event);
        //notifycation
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            setOpenredbagState(true);
            List<CharSequence> texts = event.getText();
            for(CharSequence c : texts) {
                Log.d(TAG,"c = "+c);
            }
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(WECHAT_KEYNAME)) {
                            openNotification(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            for(CharSequence c : texts) {
                Log.d(TAG,"c = "+c);
            }
            openEnvelope(event);
        }

}
    private void openNotification(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //通过通知去打开微信
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    private void openEnvelope(AccessibilityEvent event) {
        if (LUCKMONEYRECEIVUI.equals(event.getClassName())) {
            getRedBag();
        } else if (LAUNCHERUI.equals(event.getClassName())) {
            //在聊天界面,去点中红包
            if(isopen==true)
            clickRedBag();
        } else if(LUCKMONEYDETAILUI.equals(event.getClassName())) {
            //红包详情页面

        }
    }
    private void getRedBag() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        AccessibilityNodeInfo openRedbagbtn = findFuctionButtonNode(nodeInfo,OPENBUTTON);
        if(openRedbagbtn==null) {
            AccessibilityNodeInfo closeButton = findFuctionButtonNode(nodeInfo,CLOSEBUTTON);
            return;
        }

        openRedbagbtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        setOpenredbagState(false);
    }

    private AccessibilityNodeInfo findFuctionButtonNode(AccessibilityNodeInfo nodeInfo,int fun) {
        AccessibilityNodeInfo FuctionButton = null;
        int chirdCount = nodeInfo.getChildCount();
        switch (fun) {
            case OPENBUTTON:
                for(int i=0;i<chirdCount;i++) {
                    if(nodeInfo.getChild(i).getClassName().equals("android.widget.Button"))
                          FuctionButton =nodeInfo.getChild(i);
                }
                break;
            case CLOSEBUTTON:
                for(int i=0;i<chirdCount;i++) {
                    if(nodeInfo.getChild(i).getViewIdResourceName().equals("com.tencent.mm:id/bga"))
                        FuctionButton =nodeInfo.getChild(i);
                }
                break;
            default:
            break;
        }
        return FuctionButton;
    }
    private void clickRedBag() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_KEYNAME);
            for (AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //点击红包
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->点击红包:" + parent);
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }
    private void setOpenredbagState(boolean state) {
        isopen = state;
    }
    @Override
    public void onInterrupt() {

    }
}
