package com.stefanlau.biometricdemo;

import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_hardware_status, tv_open_finger_login, tv_finger_login;

    private boolean fingerLoginEnable = false;

    private BiometricPromptManager mManager;

    private ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aCache = ACache.get(App.getContext());

        initView();

        checkHardware();
    }

    private void initView() {
        tv_hardware_status = findViewById(R.id.tv_hardware_status);
        tv_open_finger_login = findViewById(R.id.tv_open_finger_login);
        tv_open_finger_login.setOnClickListener(this);
        tv_finger_login = findViewById(R.id.tv_finger_login);
        tv_finger_login.setOnClickListener(this);
    }

    private void checkHardware() {
        if (Build.VERSION.SDK_INT >= 23 ) {
            mManager = BiometricPromptManager.from(this);
            if (mManager.isHardwareDetected() && mManager.hasEnrolledFingerprints() && mManager.isKeyguardSecure()){
                tv_hardware_status.setText("手机硬件支持指纹登录");
                tv_open_finger_login.setClickable(true);
                tv_finger_login.setClickable(true);
            }else {
                tv_hardware_status.setText("手机硬件不支持指纹登录");
                tv_open_finger_login.setClickable(false);
                tv_finger_login.setClickable(false);
            }
        }else {
            tv_hardware_status.setText("API 低于23,不支持指纹登录");
            tv_open_finger_login.setClickable(false);
            tv_finger_login.setClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_open_finger_login:
                /**
                 * 先要用户输入登录密码
                 * 自行处理获取用户输入的密码
                 * 另外自行做加密保护
                 */
                openFingerLogin("123546");
                break;
            case R.id.tv_finger_login:
                if (fingerLoginEnable){
                    fingerLogin();
                }else {
                    Toast.makeText(this, "请先开通指纹登录", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void fingerLogin() {
        if (mManager.isBiometricPromptEnable()) {
            mManager.authenticate(true, new BiometricPromptManager.OnBiometricIdentifyCallback() {
                @Override
                public void onUsePassword() {
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onSucceeded(FingerprintManager.AuthenticationResult result) {
                    try {
                        Cipher cipher = result.getCryptoObject().getCipher();
                        String text = aCache.getAsString("pwdEncode");
                        Log.i("test", "获取保存的加密密码: " + text);
                        byte[] input = Base64.decode(text, Base64.URL_SAFE);
                        byte[] bytes = cipher.doFinal(input);
                        /**
                         * 然后这里用原密码(当然是加密过的)调登录接口
                         */
                        Log.i("test", "解密得出的加密的登录密码: " + new String(bytes));
                        byte[] iv = cipher.getIV();
                        Log.i("test", "IV: " + Base64.encodeToString(iv,Base64.URL_SAFE));
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onSucceeded(BiometricPrompt.AuthenticationResult result) {
                    try {
                        Cipher cipher = result.getCryptoObject().getCipher();
                        String text = aCache.getAsString("pwdEncode");
                        Log.i("test", "获取保存的加密密码: " + text);
                        byte[] input = Base64.decode(text, Base64.URL_SAFE);
                        byte[] bytes = cipher.doFinal(input);
                        /**
                         * 然后这里用原密码(当然是加密过的)调登录接口
                         */
                        Log.i("test", "解密得出的原始密码: " + new String(bytes));
                        byte[] iv = cipher.getIV();
                        Log.i("test", "IV: " + Base64.encodeToString(iv,Base64.URL_SAFE));
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed() {
                }

                @Override
                public void onError(int code, String reason) {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void openFingerLogin(final String pwd) {
        if (mManager.isBiometricPromptEnable()) {
            mManager.authenticate(false, new BiometricPromptManager.OnBiometricIdentifyCallback() {
                @Override
                public void onUsePassword() {
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onSucceeded(FingerprintManager.AuthenticationResult result) {
                    try {
                        /**
                         * 加密后的密码和iv可保存在服务器,登录时通过接口根据账号获取
                         */
                        Log.i("test", "原密码: " + pwd);
                        Cipher cipher = result.getCryptoObject().getCipher();
                        byte[] bytes = cipher.doFinal(pwd.getBytes());
                        Log.i("test", "设置指纹时保存的加密密码: " + Base64.encodeToString(bytes,Base64.URL_SAFE));
                        aCache.put("pwdEncode", Base64.encodeToString(bytes,Base64.URL_SAFE));
                        byte[] iv = cipher.getIV();
                        Log.i("test", "设置指纹时保存的加密IV: " + Base64.encodeToString(iv,Base64.URL_SAFE));
                        aCache.put("iv", Base64.encodeToString(iv,Base64.URL_SAFE));
                        fingerLoginEnable = true;
                        Toast.makeText(MainActivity.this, "开通成功", Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onSucceeded(BiometricPrompt.AuthenticationResult result) {
                    try {
                        Cipher cipher = result.getCryptoObject().getCipher();
                        byte[] bytes = cipher.doFinal(pwd.getBytes());
                        //保存加密过后的字符串
                        Log.i("test", "设置指纹保存的加密密码: " + Base64.encodeToString(bytes,Base64.URL_SAFE));
                        aCache.put("pwdEncode", Base64.encodeToString(bytes,Base64.URL_SAFE));
                        byte[] iv = cipher.getIV();
                        Log.i("test", "设置指纹保存的加密IV: " + Base64.encodeToString(iv,Base64.URL_SAFE));
                        aCache.put("iv", Base64.encodeToString(iv,Base64.URL_SAFE));
                        fingerLoginEnable = true;
                        Toast.makeText(MainActivity.this, "开通成功", Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed() {
                }

                @Override
                public void onError(int code, String reason) {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }
}
