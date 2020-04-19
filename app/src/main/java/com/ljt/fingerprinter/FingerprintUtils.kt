package com.ljt.fingerprinter

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

object FingerprintUtils {

    fun isSupportFingerprint(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(context, "您的系统版本过低，暂不支持指纹功能!", Toast.LENGTH_SHORT).show()
            return false
        } else {
            val keyguardManager = context.getSystemService(KeyguardManager::class.java)
            val fingerprintManager = FingerprintManagerCompat.from(context)
            if (!fingerprintManager!!.isHardwareDetected) {
                Toast.makeText(context, "您的手机不支持指纹功能！", Toast.LENGTH_SHORT).show()
                return false
            } else if (!keyguardManager!!.isKeyguardSecure) {
                Toast.makeText(context, "请开启您手机的指纹验证功能！", Toast.LENGTH_SHORT).show()
                return false
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(context, "请您在系统设置中至少添加一个指纹!", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }
}