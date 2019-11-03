package jp.shiita.astra.ui.waiting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.extensions.observeNonNull
import javax.inject.Inject

class WaitingFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: WaitingViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_waiting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
    }

    private fun observe() {
        viewModel.startCallingEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(WaitingFragmentDirections.actionWaitingToCall())
        }
    }
}