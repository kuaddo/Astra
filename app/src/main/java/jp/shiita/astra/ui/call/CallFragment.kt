package jp.shiita.astra.ui.call

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentCallBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import jp.shiita.astra.ui.CallViewModel
import javax.inject.Inject
import kotlin.random.Random

class CallFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: CallViewModel by activityViewModels { viewModelFactory }
    private val binding by dataBinding<FragmentCallBinding>(R.layout.fragment_call)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.startCountDown()
        startTwinkleAnimation()
        binding.motionLayout.transitionToEnd()
        observe()
    }

    private fun observe() {
        viewModel.selectUploadImageEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(CallFragmentDirections.actionCallToSelectImages())
        }
        viewModel.viewImageEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(CallFragmentDirections.actionCallToViewImages())
        }
        viewModel.onStopConnectionEvent.observeNonNull(viewLifecycleOwner) { showFinishDialog() }
    }

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

    private fun showFinishDialog() = MaterialDialog(requireContext()).show {
        title(R.string.call_finish_dialog_title)
        message(R.string.call_finish_dialog_message)
        positiveButton(R.string.ok) { findNavController().popBackStack() }
        cancelable(false)
    }
}
