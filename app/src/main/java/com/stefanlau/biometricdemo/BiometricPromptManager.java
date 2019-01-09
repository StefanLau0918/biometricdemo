package com.stefanlau.biometricdemo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
public class BiometricPromptManager {

    private IBiometricPromptImpl mImpl;
    private Activity mActivity;

    public interface OnBiometricIdentifyCallback {
        void onUsePassword();

        void onSucceeded(FingerprintManager.AuthenticationResult result);

        void onSucceeded(BiometricPrompt.AuthenticationResult result);

        void onFailed();

        void onError(int code, String reason);

        void onCancel();

    }

    public static BiometricPromptManager from(Activity activity) {
        return new BiometricPromptManager(activity);
    }

    public BiometricPromptManager(Activity activity) {
        mActivity = activity;
        if (isAboveApi28()) {
            mImpl = new BiometricPromptApi28(activity);
        } else if (isAboveApi23()) {
            mImpl = new BiometricPromptApi23(activity);
        }
    }

    private boolean isAboveApi28() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    private boolean isAboveApi23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void authenticate(boolean loginFlg, @NonNull OnBiometricIdentifyCallback callback) {
        mImpl.authenticate(loginFlg, new CancellationSignal(), callback);
    }

    public void authenticate(@NonNull CancellationSignal cancel,
                             @NonNull OnBiometricIdentifyCallback callback) {
        // TODO: 2019/1/7
//        mImpl.authenticate(cancel, callback);
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    public boolean hasEnrolledFingerprints() {
        if (isAboveApi28()) {
            //TODO 这是Api23的判断方法，也许以后有针对Api28的判断方法
            final FingerprintManager manager = mActivity.getSystemService(FingerprintManager.class);
            return manager != null && manager.hasEnrolledFingerprints();
        } else if (isAboveApi23()) {
            return ((BiometricPromptApi23)mImpl).hasEnrolledFingerprints();
        } else {
            return false;
        }
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     * @return true if hardware is present and functional, false otherwise.
     */
    public boolean isHardwareDetected() {
        if (isAboveApi28()) {
            //TODO 这是Api23的判断方法，也许以后有针对Api28的判断方法
            final FingerprintManager fm = mActivity.getSystemService(FingerprintManager.class);
            return fm != null && fm.isHardwareDetected();
        } else if (isAboveApi23()) {
            return ((BiometricPromptApi23)mImpl).isHardwareDetected();
        } else {
            return false;
        }
    }

    public boolean isKeyguardSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            return true;
        }

        return false;
    }

    /**
     * Whether the device support biometric.
     * @return
     */
    public boolean isBiometricPromptEnable() {
        return isAboveApi23()
                && isHardwareDetected()
                && hasEnrolledFingerprints()
                && isKeyguardSecure();
    }

}
