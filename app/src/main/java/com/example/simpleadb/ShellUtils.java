package com.example.simpleadb;

import com.rosan.dhizuku.api.IDhizukuResultReceiver;
import android.util.Log;
import com.rosan.dhizuku.api.IDhizukuDevicePolicyManager; // 【已修正】根据 AAR 实际包名
import java.io.OutputStream;
import java.net.Socket;

public class ShellUtils {

    public interface ShellCallback {
        void onResult(String result);
        void onError(String error);
    }

    // 利用 Dhizuku 开启 ADB 端口
    public static void enableAdb(IDhizukuDevicePolicyManager manager, ShellCallback callback) {
        // 命令：设置端口并重启 adbd
        String cmd = "setprop service.adb.tcp.port 5555; stop adbd; start adbd";
        try {
            manager.executeShellCommand(cmd, new IDhizukuResultReceiver() {
                @Override
                public void onSuccess(String result) { callback.onResult(result); }
                @Override
                public void onFailed(String error) { callback.onError(error); }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Socket 连接本地 ADB 发送命令
    public static void execLocalAdb(String command, ShellCallback callback) {
        new Thread(() -> {
            Socket socket = null;
            OutputStream out = null;
            try {
                Log.d("ShellUtils", "正在连接 127.0.0.1:5555...");
                socket = new Socket("127.0.0.1", 5555);
                out = socket.getOutputStream();

                // ADB 协议握手
                byte[] header = hexToBin("0000");
                out.write(header);

                // 发送命令
                String cmdStr = "shell:" + command + "\n";
                byte[] cmdBytes = cmdStr.getBytes("UTF-8");
                String lenHex = String.format("%04x", cmdBytes.length);
                
                out.write(hexToBin(lenHex));
                out.write(cmdBytes);
                out.flush();

                Thread.sleep(1000);
                callback.onResult("命令已发送");

            } catch (Exception e) {
                Log.e("ShellUtils", "Socket执行失败", e);
                callback.onError("连接失败: " + e.getMessage());
            } finally {
                try { if (out != null) out.close(); } catch (Exception e) {}
                try { if (socket != null) socket.close(); } catch (Exception e) {}
            }
        }).start();
    }

    private static byte[] hexToBin(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
