package jp.shiita.astra.ui.waiting

import android.Manifest
import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentWaitingBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import jp.shiita.astra.extensions.showOnContactsDeniedDialog
import jp.shiita.astra.extensions.showOnContactsNeverAskAgainDialog
import jp.shiita.astra.extensions.showRationaleForContactsDialog
import jp.shiita.astra.ui.CallViewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject
import kotlin.random.Random

@RuntimePermissions
class WaitingFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: CallViewModel by activityViewModels { viewModelFactory }
    private val binding by dataBinding<FragmentWaitingBinding>(R.layout.fragment_waiting)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startTwinkleAnimation()
        observe()
    }

    override fun onResume() {
        super.onResume()
        gridObserveWithPermissionCheck()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopGridObserve()
    }

    private fun observe() {
        viewModel.startCallingEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(WaitingFragmentDirections.actionWaitingToCall())
        }
        viewModel.isOwnIdAvailable.observeNonNull(viewLifecycleOwner) {
            if (it) startLocalStreamWithPermissionCheck()
        }
    }

    // TODO: layoutも含めてCustomView化する
    private fun startTwinkleAnimation() {
        val starViews = listOf(
            binding.starView.image1,
            binding.starView.image2,
            binding.starView.image3,
            binding.starView.image4,
            binding.starView.image5,
            binding.starView.image6,
            binding.starView.image7,
            binding.starView.image8,
            binding.starView.image9,
            binding.starView.image10,
            binding.starView.image11,
            binding.starView.image12,
            binding.starView.image13
        )
        starViews.forEach {
            AnimatorInflater.loadAnimator(context, R.animator.twinkle).apply {
                setTarget(it)
                startDelay = Random.nextLong(1000)
                start()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
    fun startLocalStream() = viewModel.startLocalStream()

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
    fun gridObserve() = viewModel.startGridObserve()

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
    fun showRationaleForContacts(request: PermissionRequest) {
        showRationaleForContactsDialog(
            request,
            R.string.permission_record_location_title,
            R.string.permission_record_location_message
        )
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
    fun onContactsDenied() {
        showOnContactsDeniedDialog(R.string.permission_record_location_denied)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
    fun onContactsNeverAskAgain() {
        showOnContactsNeverAskAgainDialog(R.string.permission_record_location_never_ask)
    }

}