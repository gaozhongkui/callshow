package com.epiphany.callshow.function.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.epiphany.call.extensions.checkPermissionEnable
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.databinding.ActivityCheckPermissionBinding
import com.epiphany.callshow.dialog.FixPermissionDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * 权限检查
 * 1、运行时权限检查2个必须权限（存储和通讯录）  3个不必须的权限(电话、短信、通话记录)
 * 2、检查权限结果返回，检查两个必须权限。如果用户全部校验通过，进行下一步（3）的引导，如果用户全部选择禁止，弹框，底部文案是去开启，点击去开启 继续拉起这两个权限  。如果用户选择的是禁止不再提示，弹框，文案是去设置，点击去系统设置
 * 3、必要的2个权限通过验证，进行通知权限、系统设置、默认应用引导。否则卡主
 */
class CheckPermissionActivity :
    BaseActivity<CheckPermissionViewModel, ActivityCheckPermissionBinding>() {

    //返回后执行定时任务
    private val mHandler = Handler(Looper.getMainLooper())

    //当前遍历的 权限下标
    private var currCheckPermissionIndex = 0

    //是否已经进行过授权
    private var isAutoPermission = false

    //是否进行自动遍历
    private var autoIterator = true


    companion object {
        //请求权限
        const val REQUEST_PERMISSION = 1

        //请求必要权限
        const val REQUEST_NECESSARY_PERMISSION = 2

        //去应用详情
        const val REQUEST_TO_APP_DETAIL = 2

    }

    override fun getBindLayout(): Int {
        return R.layout.activity_check_permission
    }

    override fun getViewModelClass(): Class<CheckPermissionViewModel> {
        return CheckPermissionViewModel::class.java
    }

    private val adapter = PermissionAdapter()

    override fun initView() {
        binding.recyclerView.adapter = adapter
      /*  binding.title.setBackListener(View.OnClickListener {
            finishResult()
            finish()
        })*/
        initObserver()
        //沉浸式
        StatusBarUtil.setTranslucentStatus(this, true)

    }

    private fun initObserver() {
        viewModel.infoList.observe(this, Observer {
            adapter.setData(it)
        })
    }

    //去设置点击事件
    fun onSettingClick(view: View) {

        val needRequest = checkRuntimePermission()

        //有未授权的去授权 授权结束后去检测其他权限
        if (needRequest.size > 0) {
            ActivityCompat.requestPermissions(this, needRequest.toTypedArray(), REQUEST_PERMISSION)
        } else {
            //重新检测状态值赋值
            isAutoPermission = true
            autoIterator = true
            currCheckPermissionIndex = 0

            checkOther()
        }
    }

    private fun checkRuntimePermission(): MutableList<String> {
        //先弹出系统弹框（电话、通讯录、短信、通话记录） 在回调中处理后续逻辑
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECEIVE_SMS
        )
        val needRequest = mutableListOf<String>()
        //处理未拿到授权的权限
        for (permission in permissions) {
            if (!checkPermissionEnable(permission)) {
                needRequest.add(permission)
            }
        }
        return needRequest
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //请求权限回调
        if (requestCode == REQUEST_PERMISSION) {
            handleRequestPermissionResult(permissions, grantResults)
        } else if (requestCode == REQUEST_NECESSARY_PERMISSION) {
            handleNecessaryPermissionResult()
        }

    }

    private val mAutoStarDialog by lazy {
//        AutoStartDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //弹框请求回调
        if (requestCode == CheckPermissionViewModel.REQUEST_AUTO_START) {
//            mAutoStarDialog.show(supportFragmentManager, "auto_start")
        } else if (requestCode == REQUEST_TO_APP_DETAIL) {
            //去设置详情返回，
            handleNecessaryPermissionResult()
        }
    }

    private val mDispatchGuideRunnable = Runnable {
        val data = adapter.getData()!!
        if (autoIterator) {
            val size = data.size
            val start = currCheckPermissionIndex
            for (index in start until size) {
                val bean = data[index]
                if (!bean.isAuth) {
                    //执行页面跳转
                    bean.handle.invoke(this@CheckPermissionActivity)
                    currCheckPermissionIndex = index + 1
                    if (currCheckPermissionIndex == size) {
                        autoIterator = false
                    }
                    return@Runnable
                }
            }
        }
        //检验自动退出
        var finish = true
        for (bean in data) {
            if (!bean.isAuth) {
                finish = false
                break
            }
        }
        if (finish) {
            finishResult()
            finish()
        }
    }

    private val mReloadRunnable = Runnable {
        viewModel.loadInfoList(this)
    }

    //处理权限回调
    private fun handleRequestPermissionResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionToastMap = LinkedHashMap<String, String>().apply {
            this[Manifest.permission.WRITE_EXTERNAL_STORAGE] = "文件存储"
            this[Manifest.permission.READ_CONTACTS] = "联系人"

        }
        val unAuto = mutableListOf<String>()
        //刷新数据
        mHandler.postDelayed(mReloadRunnable, 50)
//        grantResults.forEachIndexed { index, result ->
//            run {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    unAuto.add(permissionToastMap[permissions[index]] ?: "")
//                }
//            }
//        }
        //检查两个必要权限是否授权
        for (entry in permissionToastMap) {
            if (!checkPermissionEnable(entry.key)) {
                unAuto.add(entry.value)
            }
        }

        //未全部授权提示
        if (unAuto.size > 0) {
//            Toast.makeText(
//                this,
//                String.format("请开启%s", unAuto.joinToString(separator = "、")),
//                Toast.LENGTH_LONG
//            ).show()
//            val intents = arrayOf(
//                    RomManager.get().getSettingDetailIntent(this),
//                    Intent(this, PermissionAuthGuideActivity::class.java)
//            )
//            startActivities(intents)
            //校验两个必要权限
            val hasPermanently = EasyPermissions.permissionPermanentlyDenied(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) ||
                    EasyPermissions.permissionPermanentlyDenied(
                        this,
                        Manifest.permission.READ_CONTACTS
                    )

            if (hasPermanently) {
                //有一个永久禁止就去系统设置
                showOpenSettingPermissionDialog()
            } else {
                //全部是禁止模式，提示开启
                showReRequestNecessaryPermissionDialog()
            }
        } else {
            isAutoPermission = true
        }

    }

    //处理必须权限单独
    private fun handleNecessaryPermissionResult() {
        val permissions =
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS)
        //校验全部权限，有一个没授权的就跳出
        for (permission in permissions) {
            if (!checkPermissionEnable(permission)) {
                return
            }
        }
        //全部授权后，继续三步引导
        isAutoPermission = true
//        checkOther()
    }

    //检测其他权限  读取来电通(通知)  保持来电秀正常启动(部分手机设置自启动) 替换来电页面(设置为系统应用) 修改手机铃声(系统设置修改权限)
    private fun checkOther() {
        mHandler.removeCallbacksAndMessages(mDispatchGuideRunnable)
        mHandler.postDelayed(mDispatchGuideRunnable, 200)
    }


    private val mRequirePermissionDialog = FixPermissionDialog(R.string.permission_to_open) {
        //继续请求 权限
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS),
            REQUEST_NECESSARY_PERMISSION
        )
    }

    private fun showReRequestNecessaryPermissionDialog() {
        mRequirePermissionDialog.show(supportFragmentManager, "open")
    }

    private val mSettingPermissionDialog = FixPermissionDialog(R.string.permission_to_setting) {
//        startActivityForResult(RomManager.get().getSettingDetailIntent(this), REQUEST_TO_APP_DETAIL)
    }

    private fun showOpenSettingPermissionDialog() {
        mSettingPermissionDialog.show(supportFragmentManager, "setting")
    }

    override fun onBackPressed() {
        finishResult()
        super.onBackPressed()
    }

    //必须要有的权限只要给了就RESULT_OK
    private fun finishResult() {
        //校验两个必须权限
        if (!checkPermissionEnable(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !checkPermissionEnable(
                Manifest.permission.READ_CONTACTS
            )
        ) {
            return
        }
        adapter.getData()?.let {
            var result = true
            for (bean in it) {
                //必须要给的权限 但是没有给
                if (bean.necessaryNeed && !bean.isAuth) {
                    result = false
                    break
                }
            }
            if (result) {
                setResult(Activity.RESULT_OK)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInfoList(this)

        //runtime permission全部授权后，进行必要权限引导
        if (isAutoPermission) {
            checkOther()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

}