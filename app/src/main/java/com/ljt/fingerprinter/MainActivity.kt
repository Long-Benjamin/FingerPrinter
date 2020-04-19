package com.ljt.fingerprinter

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ljt.fingerprinter.finger.FingerprintDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


const val DEFAULT_KEY_NAME = "default_key"

class MainActivity : AppCompatActivity() {

    private lateinit var mBiometricPrompt: BiometricPrompt
    private var mKeyStore: KeyStore ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        tv_six.setOnClickListener {
            startFingerPrintCheck6()
        }

        tv_nine.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startFingerPrintCheck9()
            }
        }

    }

    private fun startFingerPrintCheck6() {
        if (FingerprintUtils.isSupportFingerprint(this)) {
            initKey()
            initCipher()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startFingerPrintCheck9() {
        if (FingerprintUtils.isSupportFingerprint(this)) {
            initKey()
            initCipher9()
        }
    }


    @TargetApi(23)
    private fun initKey() {
        if (mKeyStore != null)return

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore")
            mKeyStore?.load(null)
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @TargetApi(23)
    private fun initCipher() {
        try {
            val key = mKeyStore?.getKey(DEFAULT_KEY_NAME, null) as SecretKey
            val cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            showFingerPrintDialog(cipher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initCipher9() {
        try {
            val key = mKeyStore?.getKey(DEFAULT_KEY_NAME, null) as SecretKey
            val cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            showFingerPrintDialog9(cipher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showFingerPrintDialog(cipher: Cipher) {
        var fragment = FingerprintDialogFragment()
        fragment.setCipher(cipher)
        fragment.show(supportFragmentManager)
        fragment.setOnAuthenticatedListener(object : FingerprintDialogFragment.OnAuthenticatedListener{
            override fun onAuthenticated() {
                this@MainActivity.onAuthenticated()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showFingerPrintDialog9(cipher: Cipher) {

        var mCancellationSignal: CancellationSignal = CancellationSignal()

        mBiometricPrompt = BiometricPrompt.Builder(this.applicationContext)
                .setTitle("指纹识别")
                .setDescription("指纹识别这个功能相关描述！")
                .setSubtitle("指纹识别子标题")
                .setNegativeButton("取消", this.mainExecutor, DialogInterface.OnClickListener { _, _ ->
                    mCancellationSignal.cancel()
                })
                .build()

        mBiometricPrompt.authenticate(BiometricPrompt.CryptoObject(cipher),
                mCancellationSignal!!,
                this.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback(){
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(this@MainActivity, "识别失败!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(this@MainActivity, "识别异常：$errString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                        Toast.makeText(this@MainActivity, "帮助：$helpString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        onAuthenticated()
                    }
                }
        )
    }

    fun onAuthenticated() {
        Toast.makeText(this@MainActivity, "识别成功！", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this,VerifySuccessActivity::class.java))
    }

}
