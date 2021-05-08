package com.epiphany.callshow.function.callshow

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import com.epiphany.call.callshow.PhoneHelper
import com.epiphany.call.extensions.*
import com.epiphany.call.helpers.*
import com.epiphany.call.manager.CallManager
import com.epiphany.call.models.CallContact
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.SharedPreferenceUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.database.VideoRoomManager
import com.epiphany.callshow.databinding.ActivityPhoneCallBinding
import com.epiphany.callshow.function.video.VideoFragment
import com.epiphany.callshow.listener.DefaultButtonClickStyleListener
import com.epiphany.callshow.receivers.CallActionReceiver
import com.epiphany.callshow.widget.CallToolInfo
import com.epiphany.callshow.widget.CallToolType
import com.epiphany.callshow.widget.IOngoingCallLayoutLayoutListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.system.exitProcess


/**
 * 用于展示来电的页面
 */
class PhoneCallActivity : BaseActivity<BaseViewModel, ActivityPhoneCallBinding>() {
    //是否通话结束
    private var isCallEnded = false

    //通话时长
    private var callDuration = 0

    //通话联系人
    private var callContact: CallContact? = null

    private var mProximityWakeLock: PowerManager.WakeLock? = null

    //通话计时器
    private var mCallTimer = Timer()

    //来电状态
    private var mCallState = 0

    //视频Fragment
    private var mVideoFragment: VideoFragment? = null

    override fun getBindLayout(): Int = R.layout.activity_phone_call

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        initStatusBarLayout()
        initLayoutListener()
        audioManager.mode = AudioManager.MODE_IN_CALL
        CallManager.getCallContact(applicationContext) { contact ->
            callContact = contact
            runOnUiThread {
                updateOtherPersonsInfo()
                updateContentBgLayout()
                setupNotification()
            }
        }
        addLockScreenFlags()
        CallManager.registerCallback(callCallback)
        updateCallState(CallManager.getState())
        onCheckAudioState()
    }

    private fun initStatusBarLayout() {
        val layoutParams = binding.tvPhoneName.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topMargin += SystemInfo.getStatusBarHeight(this)
        binding.tvPhoneName.layoutParams = layoutParams
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initLayoutListener() {
        binding.callDecline.setOnTouchListener(DefaultButtonClickStyleListener())
        binding.callDecline.setOnClickListener {
            endCall()
        }
        binding.callAccept.setOnClickListener {
            acceptCall()
        }

        binding.flOngoingCall.setListener(object : IOngoingCallLayoutLayoutListener {
            override fun onItemClick(toolInfo: CallToolInfo) {
                when (toolInfo.type) {
                    //录音
                    CallToolType.SOUND_RECORDING -> {

                    }
                    //免提
                    CallToolType.ON_SPEAKERPHONE -> {
                        toolInfo.state = !toolInfo.state
                        val isSpeakerOn = toolInfo.state
                        audioManager.isSpeakerphoneOn = isSpeakerOn
                        val newRoute =
                            if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                        CallManager.inCallService?.setAudioRoute(newRoute)
                        toolInfo.iconRes =
                            if (toolInfo.state) R.drawable.ic_iphone_voice_choice else R.drawable.ic_iphone_voice
                        binding.flOngoingCall.notifyCallToolAdapter()
                    }
                    CallToolType.ADD_PHONE -> {

                    }
                    CallToolType.KEEP -> {
                        toolInfo.state = !toolInfo.state
                        val isKeep = toolInfo.state
                        if (isKeep) {
                            CallManager.hold()
                        } else {
                            CallManager.unHold()
                        }
                        toolInfo.iconRes =
                            if (toolInfo.state) R.drawable.ic_iphone_hold_choice else R.drawable.ic_iphone_hold
                        binding.flOngoingCall.notifyCallToolAdapter()
                    }
                    //静音
                    CallToolType.MUTE -> {
                        toolInfo.state = !toolInfo.state
                        val isMicrophoneOn = toolInfo.state
                        audioManager.isMicrophoneMute = isMicrophoneOn
                        CallManager.inCallService?.setMuted(isMicrophoneOn)
                        toolInfo.iconRes =
                            if (toolInfo.state) R.drawable.ic_iphone_mute_choice else R.drawable.ic_iphone_mute
                        binding.flOngoingCall.notifyCallToolAdapter()
                    }
                    CallToolType.DIAL_UP_KEYBOARD -> {
                        toolInfo.state = !toolInfo.state
                        binding.flOngoingCall.switchDigitalKeyboardVisible(toolInfo.state)
                        //判断是否展示数字键盘
                        if (toolInfo.state) {
                            binding.tvDialPadInput.beVisible()
                            binding.tvPhoneName.beInvisible()
                        } else {
                            binding.tvDialPadInput.beGone()
                            binding.tvPhoneName.beVisible()
                        }
                    }
                }
            }

            override fun onDigitalKeyboardItemClick(char: Char) {
                dialPadPressed(char)
            }
        })
    }

    /**
     * 检查声音的状态
     */
    private fun onCheckAudioState() {
        /*val currentRingTonePath = RingToneUtil.getCurrentRingTonePath(this)
        val lastRingTonePath = SharedPreferenceUtil.getString(SHARED_KEY_LAST_RING_TONE_PATH)

        val isRingChanged = TextUtils.equals(currentRingTonePath, lastRingTonePath)
        if (isRingChanged) {
            RingToneUtil.adjustMusicAudio(false)
            RingToneUtil.adjustRingAudio(true)
        } else {
            RingToneUtil.adjustMusicAudio(true)
            RingToneUtil.adjustRingAudio(false)
        }*/
    }

    @SuppressLint("NewApi")
    private fun setupNotification() {
        val callState = CallManager.getState()
        val channelId = "dialer_call"
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val name = "call_notification_channel"

            NotificationChannel(channelId, name, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val openAppIntent = Intent(this, PhoneCallActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)

        val acceptCallIntent = Intent(this, CallActionReceiver::class.java)
        acceptCallIntent.action = ACCEPT_CALL
        val acceptPendingIntent =
            PendingIntent.getBroadcast(this, 0, acceptCallIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val declineCallIntent = Intent(this, CallActionReceiver::class.java)
        declineCallIntent.action = DECLINE_CALL
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            declineCallIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val callerName =
            if (callContact != null && callContact!!.name.isNotEmpty()) callContact!!.name else getString(
                R.string.unknown_caller
            )
        val contentTextId = when (callState) {
            Call.STATE_RINGING -> R.string.is_calling
            Call.STATE_DIALING -> R.string.dialing
            Call.STATE_DISCONNECTED -> R.string.call_ended
            Call.STATE_DISCONNECTING -> R.string.call_ending
            else -> R.string.ongoing_call
        }

        val collapsedView = RemoteViews(packageName, R.layout.call_notification).apply {
            setText(R.id.notification_caller_name, callerName)
            setText(R.id.notification_call_status, getString(contentTextId))
            setVisibleIf(R.id.notification_accept_call, callState == Call.STATE_RINGING)

            setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
            setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_phone_vector)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setOngoing(true)
            .setSound(null)
            .setUsesChronometer(callState == Call.STATE_ACTIVE)
            .setChannelId(channelId)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val notification = builder.build()
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    /**
     * 输入字符
     */
    private fun dialPadPressed(char: Char) {
        CallManager.keypad(char)
        binding.tvDialPadInput.addCharacter(char)
    }

    private fun updateCallState(state: Int) {
        mCallState = state
        when (state) {
            Call.STATE_RINGING -> callRinging()
            Call.STATE_ACTIVE -> callStarted()
            Call.STATE_DISCONNECTED -> endCall()
            Call.STATE_CONNECTING, Call.STATE_DIALING -> initOutgoingCallUI()
        }
        if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
            mCallTimer.cancel()
        }


        val statusTextId = when (state) {
            Call.STATE_RINGING -> R.string.is_calling
            Call.STATE_DIALING -> R.string.dialing
            else -> 0
        }

        if (statusTextId != 0) {
            binding.tvCallStatusLabel.text = getString(statusTextId)
        }

        setupNotification()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    /**
     * 更新背景布局
     */
    private fun updateContentBgLayout() {
        //判断如果为来电状态时的处理
        if (mCallState == Call.STATE_RINGING) {
            callContact?.apply {
                loadIncomingCallVideo(number)
            }
        }
    }

    /**
     * 更新个人信息
     */
    private fun updateOtherPersonsInfo() {
        if (!SystemInfo.isValidActivity(this)) {
            return
        }
        callContact?.apply {
            binding.tvPhoneName.text =
                if (name.isNotEmpty()) name else getString(R.string.unknown_caller)
            if (number.isNotEmpty() && number != name) {
                binding.tvCallSimId.beGone()
                binding.tvPhoneNumber.text = String.format(
                    Locale.getDefault(),
                    "%s %s %s",
                    number,
                    PhoneHelper.getQCellCore(number),
                    PhoneHelper.getCarrier(number)
                )
            } else {
                binding.tvPhoneNumber.beGone()
                binding.tvCallSimId.beVisible()
                binding.tvCallSimId.text = String.format(
                    Locale.getDefault(),
                    "%s %s",
                    PhoneHelper.getQCellCore(number),
                    PhoneHelper.getCarrier(number)
                )
            }
        }
    }

    /**
     * 呼叫来电
     */
    private fun callRinging() {
        binding.flOngoingCall.beGone()
    }

    /**
     * 开始接听电话
     */
    private fun callStarted() {
        popAllStack()
        initProximitySensor()
        hideCallAcceptLayout()
        binding.flContent.removeAllViews()
        binding.flOngoingCall.beVisible()
        try {
            mCallTimer.scheduleAtFixedRate(getCallTimerUpdateTask(), 1000, 1000)
        } catch (ignored: Exception) {
        }
    }

    /**
     * 清空所有栈
     */
    private fun popAllStack() {
        mVideoFragment?.apply {
            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.remove(this)
            beginTransaction.commitAllowingStateLoss()
        }
    }

    private fun initOutgoingCallUI() {
        binding.flContent.removeAllViews()
        hideCallAcceptLayout()
        binding.flOngoingCall.visibility = View.VISIBLE
    }

    /**
     * 执行接听
     */
    private fun acceptCall() {
        CallManager.accept()
        hideCallAcceptLayout()
    }

    /**
     * 隐藏接听来电
     */
    private fun hideCallAcceptLayout() {
        //隐藏接听的布局
        val callAcceptParentView = binding.callAccept.parent as ViewGroup
        callAcceptParentView.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun endCall() {
        CallManager.reject()
        if (mProximityWakeLock?.isHeld == true) {
            mProximityWakeLock!!.release()
        }

        if (isCallEnded) {
            finish()
            return
        }

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
        }

        isCallEnded = true
        if (callDuration > 0) {
            runOnUiThread {
                binding.tvCallStatusLabel.text =
                    "${callDuration.getFormattedDuration()} (${getString(R.string.call_ended)})"
                binding.root.postDelayed({ finish() }, 3000)
            }
        } else {
            binding.tvCallStatusLabel.text =
                getString(R.string.call_ended)
            finish()
        }
    }

    private fun initProximitySensor() {
        if (mProximityWakeLock == null || mProximityWakeLock?.isHeld == false) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            mProximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "com.simplemobiletools.dialer.pro:wake_lock"
            )
            mProximityWakeLock!!.acquire(10 * MINUTE_SECONDS * 1000L)
        }
    }

    @SuppressLint("NewApi")
    private fun addLockScreenFlags() {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        if (isOreoPlus()) {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(
                this,
                null
            )
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    /**
     * 设置来电的视频背景资源
     */
    private fun loadIncomingCallVideo(phoneNumber: String) {
 /*       GlobalScope.launch {
            val videoInfo = VideoRoomManager.getLocalVideoByMobileNumber(phoneNumber)
            withContext(Dispatchers.Main) {
                val transaction = supportFragmentManager.beginTransaction()
                mVideoFragment = VideoFragment.newInstance(
                    videoInfo,
                    isShowControlView = false,
                    source = "phone_call"
                )
                transaction.replace(R.id.fl_content, mVideoFragment!!)
                transaction.commit()
            }
        }*/
    }


    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(state)
        }
    }

    private fun getCallTimerUpdateTask() = object : TimerTask() {
        override fun run() {
            callDuration++
            runOnUiThread {
                if (!isCallEnded) {
                    binding.tvCallStatusLabel.text = callDuration.getFormattedDuration()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (CallManager.getState() == Call.STATE_DIALING) {
            endCall()
        }
    }

    override fun finish() {
        if (isMIUI()) {
            exitProcess(0)
        } else {
            super.finish()
        }
    }

    private fun isMIUI(): Boolean {
        val manufacturer = Build.MANUFACTURER
        //这个字符串可以自己定义,例如判断华为就填写huawei,魅族就填写meizu
        return "xiaomi".equals(manufacturer, ignoreCase = true)
    }

    override fun onDestroy() {
        super.onDestroy()
 /*       //恢复默认的状态
        RingToneUtil.adjustMusicAudio(false)
        RingToneUtil.adjustRingAudio(false)*/

        CallManager.unregisterCallback(callCallback)
        mCallTimer.cancel()
        if (mProximityWakeLock?.isHeld == true) {
            mProximityWakeLock!!.release()
        }
        if (CallManager.getState() == Call.STATE_DISCONNECTED || CallManager.getState() == Call.STATE_DISCONNECTING) {
            notificationManager.cancel(CALL_NOTIFICATION_ID)
            endCall()
        }

    }

    companion object {
        private const val CALL_NOTIFICATION_ID = 1
        fun launchActivity(cxt: Context) {
            val intent = Intent(cxt, PhoneCallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            cxt.startActivity(intent)
        }

        fun getIntent(cxt: Context): Intent {
            val intent = Intent(cxt, PhoneCallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

    }
}