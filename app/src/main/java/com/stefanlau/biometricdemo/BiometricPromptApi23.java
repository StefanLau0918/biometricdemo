package com.stefanlau.biometricdemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
@RequiresApi(Build.VERSION_CODES.M)
public class BiometricPromptApi23 implements IBiometricPromptImpl {

    private static final String TAG = "BiometricPromptApi23";
    private Activity mActivity;
    private BiometricPromptDialog mDialog;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private BiometricPromptManager.OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private FingerprintManager.AuthenticationCallback mFmAuthCallback = new FingerprintManageCallbackImpl();

    private ACache aCache;

    public BiometricPromptApi23(Activity activity) {
        mActivity = activity;

        aCache = ACache.get(App.getContext());

        mFingerprintManager = getFingerprintManager(activity);
    }

    @Override
    public void authenticate(boolean loginFlg, @Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback) {
        //指纹识别的回调
        mManagerIdentifyCallback = callback;

        mDialog = BiometricPromptDialog.newInstance();
        mDialog.setOnBiometricPromptDialogActionCallback(new BiometricPromptDialog.OnBiometricPromptDialogActionCallback() {
            @Override
            public void onDialogDismiss() {
                //当dialog消失的时候，包括点击userPassword、点击cancel、和识别成功之后
                if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                    mCancellationSignal.cancel();
                }
            }

            @Override
            public void onCancel() {
                //点击cancel键
                if (mManagerIdentifyCallback != null) {
                    mManagerIdentifyCallback.onCancel();
                }
            }
        });
        mDialog.show(mActivity.getFragmentManager(), "BiometricPromptApi23");

        mCancellationSignal = cancel;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mDialog.dismiss();
            }
        });

        KeyGenTool mKeyGenTool = new KeyGenTool(mActivity);
        FingerprintManager.CryptoObject object;
        if (loginFlg){
            //解密
            try {
                /**
                 * 可通过服务器保存iv,然后在使用之前从服务器获取
                 */
                String ivStr = aCache.getAsString("iv");
                byte[] iv = Base64.decode(ivStr, Base64.URL_SAFE);

                object = new FingerprintManager.CryptoObject(mKeyGenTool.getDecryptCipher(iv));
                getFingerprintManager(mActivity).authenticate(object, mCancellationSignal,
                        0, mFmAuthCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //加密
            try {
                object = new FingerprintManager.CryptoObject(mKeyGenTool.getEncryptCipher());
                getFingerprintManager(mActivity).authenticate(object, mCancellationSignal,
                        0, mFmAuthCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class FingerprintManageCallbackImpl extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Log.d(TAG, "onAuthenticationError() called with: errorCode = [" + errorCode + "], errString = [" + errString + "]");
            mDialog.setState(BiometricPromptDialog.STATE_ERROR);
            mManagerIdentifyCallback.onError(errorCode, errString.toString());
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.d(TAG, "onAuthenticationFailed() called");
            mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Log.d(TAG, "onAuthenticationHelp() called with: helpCode = [" + helpCode + "], helpString = [" + helpString + "]");
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Log.i(TAG, "onAuthenticationSucceeded: ");
            mDialog.setState(BiometricPromptDialog.STATE_SUCCEED);
            mManagerIdentifyCallback.onSucceeded(result);

        }
    }

    private FingerprintManager getFingerprintManager(Context context) {
        if (mFingerprintManager == null) {
            mFingerprintManager = context.getSystemService(FingerprintManager.class);
        }
        return mFingerprintManager;
    }

    public boolean isHardwareDetected() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.isHardwareDetected();
        }
        return false;
    }

    public boolean hasEnrolledFingerprints() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }
}
