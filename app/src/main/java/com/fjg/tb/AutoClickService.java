package com.fjg.tb;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.fjg.tb.util.ThreadPoolUtils;

import java.util.List;
import java.util.Random;

public class AutoClickService extends AccessibilityService {

    private static AutoClickService mInstance;

    public static AutoClickService getInstance() {
        return mInstance;
    }

    private AccessibilityNodeInfo mNodeInfo = null;

    private String mCurClassName = "";

    private boolean isExecuting = false;

    private String activityPage = "com.taobao.browser.BrowserActivity";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mInstance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String classname = event.getClassName().toString();
            mCurClassName = classname;
            start(classname);
        }
    }

    public void resume() {
        if (mCurClassName.equals(activityPage)) {
            start(mCurClassName);
        } else {
            ThreadPoolUtils.runTaskInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AutoClickService.this, "请打开淘宝2020双11活动页", Toast.LENGTH_SHORT).show();
                }
            }, 50);
        }
    }

    private void start(String classname) {
        if (isExecuting) {
            return;
        }
        if (classname.equals(activityPage)) {
            if (MainActivity.ENABLE) {
                isExecuting = true;
                AccessibilityNodeInfo nodeInfo = findNodeByText(getRootInActiveWindow(), "我的猫，点击撸猫");
                if (nodeInfo != null) {
                    mNodeInfo = nodeInfo;
                    startClickCat();
                } else {
                    startClickCat();
                }
            }
        }
    }

    /**
     * 开始撸猫
     */
    private void startClickCat() {
        if (MainActivity.COUNT >= 1500) {
            stop();
            return;
        }
        if (!mCurClassName.equals(activityPage)) {
            stop();
            return;
        }
        if (MainActivity.ENABLE) {
            int randomSleepTime = 222 + new Random().nextInt(150);
            ThreadPoolUtils.runTaskInUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mNodeInfo != null) {
                        performClick(mNodeInfo);
                    } else {
                        clickScreen();
                    }
                    MainActivity.COUNT++;
                    startClickCat();
                }
            }, randomSleepTime);
        } else {
            stop();
        }
    }

    private GestureDescription gestureDescription;
    private int mWidth = 0;
    private int mHeight = 0;

    /**
     * 点击屏幕
     */
    private void clickScreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (gestureDescription == null) {
                mWidth = getScreenWidth(this) / 2;
                mHeight = getScreenHeight(this) / 2;

                GestureDescription.Builder builder = new GestureDescription.Builder();
                Path path = new Path();
                path.moveTo(mWidth, mHeight);
                path.lineTo(mWidth, mHeight);
                gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 20, 80)).build();
            }
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    MainActivity.COUNT = Integer.MAX_VALUE;
                }
            }, null);
        }
    }

    @Override
    public void onInterrupt() {
        stop();
    }

    private void stop() {
        MainActivity.ENABLE = false;
        mNodeInfo = null;
        isExecuting = false;
    }

    private boolean performClick(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo clickNode = node;
        if (clickNode == null) {
            return false;
        }
        while (clickNode != null
                && !clickNode.isClickable()) {
            clickNode = clickNode.getParent();
        }
        if (clickNode != null) {
            return clickNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        return false;
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo root, String text) {
        if (root == null || TextUtils.isEmpty(text)) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeList = root.findAccessibilityNodeInfosByText(text);

        if (nodeList == null || nodeList.isEmpty()) {
            return null;
        }
        AccessibilityNodeInfo clickNode = null;
        for (AccessibilityNodeInfo nodeInfo : nodeList) {
            boolean eqText = nodeInfo.getText() != null && nodeInfo.getText().toString().equals(text);
            boolean eqDesc = nodeInfo.getContentDescription() != null && nodeInfo.getContentDescription().toString().equals(text);
            if (eqText || eqDesc) {
                clickNode = nodeInfo;
                break;
            }
        }
        return clickNode;
    }

    private int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

}
