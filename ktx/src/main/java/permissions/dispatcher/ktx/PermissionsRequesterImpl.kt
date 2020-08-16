package permissions.dispatcher.ktx

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

internal class PermissionsRequesterImpl<T>(
    private val permissions: Array<out String>,
    private val onShowRationale: ShowRationaleFun?,
    private val onPermissionDenied: Fun?,
    private val requiresPermission: Fun,
    onNeverAskAgain: Fun?,
    private val permissionRequestType: PermissionRequestType,
    viewModelStoreOwner: ViewModelStoreOwner,
    lifecycleOwner: LifecycleOwner,
    private val fragmentManager: FragmentManager,
    private val context: Context,
    private val target: T,
    private val shouldShowRequestPermissionRationale: (T, Array<out String>) -> Boolean
) : PermissionsRequester {
    init {
        val viewModel =
            ViewModelProvider(viewModelStoreOwner).get(PermissionRequestViewModel::class.java)
        viewModel.observe(
            lifecycleOwner,
            requiresPermission,
            onPermissionDenied,
            onNeverAskAgain
        )
    }

    override fun launch() {
        if (permissionRequestType.checkPermissions(context, permissions)) {
            requiresPermission()
        } else {
            val requestFun = {
                fragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, permissionRequestType.fragment(permissions))
                    .commitNowAllowingStateLoss()
            }
            if (shouldShowRequestPermissionRationale(target, permissions)) {
                onShowRationale?.invoke(KtxPermissionRequest.create(onPermissionDenied, requestFun))
            } else {
                requestFun.invoke()
            }
        }
    }
}
