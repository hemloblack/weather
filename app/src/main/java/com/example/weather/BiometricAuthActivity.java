package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class BiometricAuthActivity extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);

        // چک کردن وجود سنسور
        checkBiometricSupport();

        // راه‌اندازی احراز هویت
        setupBiometricAuth();

        // شروع احراز هویت
        showBiometricPrompt();
    }

    private void checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // سنسور موجود است و کار می‌کند
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "دستگاه شما سنسور اثر انگشت ندارد",
                        Toast.LENGTH_LONG).show();
                // برو به صفحه اصلی بدون احراز هویت
                goToMainActivity();
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "سنسور در حال حاضر در دسترس نیست",
                        Toast.LENGTH_LONG).show();
                goToMainActivity();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "لطفاً ابتدا اثر انگشت خود را در تنظیمات ثبت کنید",
                        Toast.LENGTH_LONG).show();
                goToMainActivity();
                break;
        }
    }

    private void setupBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            // کاربر لغو کرد
                            Toast.makeText(BiometricAuthActivity.this,
                                    "احراز هویت لغو شد",
                                    Toast.LENGTH_SHORT).show();
                            finish(); // برنامه را ببند
                        } else {
                            Toast.makeText(BiometricAuthActivity.this,
                                    "خطا: " + errString,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        Toast.makeText(BiometricAuthActivity.this,
                                "✅ احراز هویت موفق!",
                                Toast.LENGTH_SHORT).show();

                        // برو به صفحه اصلی
                        goToMainActivity();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        Toast.makeText(BiometricAuthActivity.this,
                                "❌ احراز هویت ناموفق، دوباره تلاش کنید",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // تنظیمات پنجره احراز هویت
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("🔐 ورود به برنامه هواشناسی")
                .setSubtitle("برای ورود اثر انگشت خود را اسکن کنید")
                .setDescription("از سنسور اثر انگشت برای باز کردن برنامه استفاده کنید")
                .setNegativeButtonText("لغو")
                .build();
    }

    private void showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(BiometricAuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // این Activity را ببند
    }

    @Override
    protected void onResume() {
        super.onResume();
        // اگر از برنامه خارج شد و برگشت، دوباره احراز هویت بگیر
        // (اختیاری - می‌توانید کامنت کنید)
    }

    @Override
    public void onBackPressed() {
        // جلوگیری از برگشت به عقب
        // کاربر باید احراز هویت کند یا برنامه را ببندد
        finishAffinity(); // تمام Activity ها را ببند
    }
}