package com.stefanlau.biometricdemo;

import android.os.CancellationSignal;
import androidx.annotation.NonNull;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
interface IBiometricPromptImpl {

    void authenticate(boolean loginFlg, @NonNull CancellationSignal cancel,
                      @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback);

}
