package com.fjg.tb;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean ENABLE = false;
    public static int COUNT = 0;

    private TextView tv_tip;

    private boolean canTurnToTaoBao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button tv_start = findViewById(R.id.btn_start);
        Button tv_stop = findViewById(R.id.btn_stop);

        tv_tip = findViewById(R.id.tv_tip);

        tv_start.setOnClickListener(this);
        tv_stop.setOnClickListener(this);
        tv_tip.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (checkAlertWindowsPermission(MainActivity.this)) {
                    enable(true);
                } else {
                    showPermissionDialog();
                }
                break;
            case R.id.btn_stop:
                enable(false);
                break;
            case R.id.tv_tip:
                if (canTurnToTaoBao) {
                    turnToTaoBao();
                }
                break;
        }
    }

    /**
     * 显示权限对话框
     */
    private void showPermissionDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("请先打开悬浮窗权限");
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("前往开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                turnToPermission();
            }
        });
        dialog.show();
    }

    private void enable(boolean enable) {
        COUNT = 0;
        if (enable) {
            if (isStartAccessibilityService(this)) {
                showAlertView();
            } else {
                showAccessibilityDialog();
                return;
            }
        } else {
            ENABLE = false;
            hideAlertView();
        }
        canTurnToTaoBao = enable;
        if (enable) {
            Toast.makeText(this, "辅助功能已开启", Toast.LENGTH_SHORT).show();
            tv_tip.setText("辅助功能已开启，请打开淘宝2020双11活动页\n点击打开淘宝");
        } else {
            Toast.makeText(this, "辅助功能已关闭", Toast.LENGTH_SHORT).show();
            tv_tip.setText("辅助功能已关闭");
        }
    }

    /**
     * 显示辅助功能对话框
     */
    private void showAccessibilityDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("请先开启应用的辅助功能");
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("前往开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                turnToAccessibility();
            }
        });
        dialog.show();
    }

    private WindowManager windowManager;
    private ControlView controlView;
    private boolean isAddView = false;

    /**
     * 显示悬浮窗
     */
    private void showAlertView() {
        if (isAddView) {
            return;
        }
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //8.0新特性
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置窗口坐标参考系
        params.gravity = Gravity.LEFT | Gravity.TOP;
        //设置原点
        params.x = 0;
        params.y = 240;
        //设置悬浮窗口长宽数据.
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        controlView = new ControlView(this, mCallback);

        windowManager.addView(controlView, params);

        isAddView = true;
    }

    /**
     * 隐藏悬浮窗
     */
    private void hideAlertView() {
        if (windowManager != null && controlView != null) {
            windowManager.removeView(controlView);
            controlView = null;
            isAddView = false;
        }
    }

    private ControlView.ControlCallback mCallback = new ControlView.ControlCallback() {
        @Override
        public void start() {
            if (!ENABLE) {
                ENABLE = true;
                AutoClickService.getInstance().resume();
            }
        }

        @Override
        public void stop() {
            ENABLE = false;
        }
    };

    @Override
    protected void onStop() {
        if (isFinishing()) {
            hideAlertView();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        backToHome();
    }

    private void backToHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }

    private void turnToTaoBao() {
        Intent intent = new Intent();
        intent.setAction("Android.intent.action.VIEW");
        intent.setClassName("com.taobao.taobao", "com.taobao.tao.welcome.Welcome");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "启动淘宝失败，请手动打开淘宝", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnToPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            if (getPackageManager().resolveActivity(intent, 0) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "请前往设置中打开应用的悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请前往设置中打开应用的悬浮窗权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnToAccessibility() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "请在手机设置中开启辅助功能", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断AccessibilityService服务是否已经启动
     *
     * @param context
     * @return boolean
     */
    private boolean isStartAccessibilityService(Context context) {
        String service = context.getApplicationContext().getPackageName() + "/" + AutoClickService.class.getCanonicalName();
        int ok = 0;
        try {
            ok = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
        if (ok == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    private boolean checkAlertWindowsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
            return true;
        }
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

}