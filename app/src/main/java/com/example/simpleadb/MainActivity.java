package com.example.simpleadb;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rosan.dhizuku.Dhizuku;
import com.rosan.dhizuku.api.IDhizukuDevicePolicyManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ListView list;
    private Button btnAdd, btnAdb;
    private List<EsimProfile> data = new ArrayList<>();
    private MyAdapter adapter;
    private IDhizukuDevicePolicyManager manager;
    private boolean isDhizukuReady = false; // 标记 Dhizuku 是否就绪

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.list);
        btnAdd = findViewById(R.id.btn_add);
        btnAdb = findViewById(R.id.btn_adb);

        adapter = new MyAdapter(this, data);
        list.setAdapter(adapter);

        // 1. 【核心修正】先初始化，再检查
        initDhizuku();

        btnAdb.setOnClickListener(v -> {
            if (isDhizukuReady && manager != null) {
                ShellUtils.enableAdb(manager, (r, e) -> runOnUiThread(() -> 
                    Toast.makeText(MainActivity.this, "ADB 5555 已开启", Toast.LENGTH_SHORT).show()
                ));
            } else {
                Toast.makeText(this, "Dhizuku 未就绪", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(v -> showAddDialog());

        list.setOnItemClickListener((parent, view, pos, id) -> {
            EsimProfile p = data.get(pos);
            Toast.makeText(this, "正在连接 ADB 执行...", Toast.LENGTH_SHORT).show();
            
            String cmd = "am broadcast -a com.heytap.wearable.lpa.action.INSTALL_ESIM " +
                         "--es activation_code \"" + p.getActivationCode() + "\" " +
                         "--es smdp_address \"" + p.getSmdpAddress() + "\" " +
                         "-n com.heytap.wearable.lpa/.receiver.EsimReceiver";

            ShellUtils.execLocalAdb(cmd, (r, e) -> runOnUiThread(() -> {
                if (e == null) {
                    Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "失败: " + e, Toast.LENGTH_LONG).show();
                }
            }));
        });
    }

    /**
     * 初始化 Dhizuku 并获取管理器
     * 必须在主线程调用 init()
     */
    private void initDhizuku() {
        // 1. 初始化
        boolean initResult = Dhizuku.init(this);
        if (!initResult) {
            Toast.makeText(this, "Dhizuku 初始化失败，请检查是否安装了 Dhizuku Server 并授权", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. 检查初始化成功状态
        if (Dhizuku.initSuccess()) {
            // 3. 获取 DevicePolicyManager
            Dhizuku.getDevicePolicyManager(new Dhizuku.IDevicePolicyManagerCallback() {
                @Override
                public void onSuccess(IDhizukuDevicePolicyManager m) {
                    manager = m;
                    isDhizukuReady = true;
                    runOnUiThread(() -> 
                        Toast.makeText(MainActivity.this, "Dhizuku 连接成功", Toast.LENGTH_SHORT).show()
                    );
                }
                @Override
                public void onFailed() {
                    runOnUiThread(() -> 
                        Toast.makeText(MainActivity.this, "获取 Dhizuku Manager 失败", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } else {
            Toast.makeText(this, "Dhizuku 初始化未成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_add, null);
        EditText etName = v.findViewById(R.id.et_name);
        EditTextetCode = v.findViewById(R.id.et_code);
        EditText etSmdp = v.findViewById(R.id.et_smdp);

        new AlertDialog.Builder(this).setView(v)
            .setPositiveButton("保存", (d, w) -> {
                String name = etName.getText().toString();
                String code = etCode.getText().toString();
                String smdp = etSmdp.getText().toString();
                if (!name.isEmpty() && !code.isEmpty() && !smdp.isEmpty()) {
                    data.add(new EsimProfile(name, code, smdp));
                    adapter.notifyDataSetChanged();
                }
            }).show();
    }

    private class MyAdapter extends ArrayAdapter<EsimProfile> {
        public MyAdapter(Context ctx, List<EsimProfile> d) { super(ctx, 0, d); }
        @Override
        public View getView(int pos, View c, View p) {
            if (c == null) c = getLayoutInflater().inflate(R.layout.item_esim, p, false);
            EsimProfile item = getItem(pos);
            ((TextView)c.findViewById(R.id.txt_name)).setText(item.getName());
            return c;
        }
    }
}
