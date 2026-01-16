package com.example.simpleadb;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import iam.efe.dhizuku.Dhizuku;
import iam.efe.dhizuku.api.DhizukuDevicePolicyManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Dhizuku.initSuccess()) {
            DhizukuDevicePolicyManager dpm = Dhizuku.getDevicePolicyManager();
            ComponentName admin = new ComponentName(this, getClass());
            try {
                dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "1");
                dpm.setGlobalSetting(admin, Settings.Global.ADB_TCP_PORT, "5555");
                Toast.makeText(this, "端口 5555 已开启！", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "未授权 Dhizuku", Toast.LENGTH_LONG).show();
        }
        finish(); // 自动关闭
    }
}
