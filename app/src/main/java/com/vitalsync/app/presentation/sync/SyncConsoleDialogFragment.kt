package com.vitalsync.app.presentation.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.databinding.FragmentSyncConsoleBinding
import com.vitalsync.app.sync.SyncRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncConsoleDialogFragment : DialogFragment() {

    private var _binding: FragmentSyncConsoleBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var syncRepository: SyncRepository

    @Inject lateinit var vitalsRepository: VitalsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSyncConsoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getLong(ARG_UID)?.takeIf { it > 0 } ?: run {
            dismiss()
            return
        }

        binding.buttonSyncNow.setOnClickListener {
            syncRepository.requestImmediateSync()
            Toast.makeText(requireContext(), "Senkron isteği kuyruğa alındı", Toast.LENGTH_SHORT).show()
        }

        binding.buttonDeleteLatest.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = vitalsRepository.deleteLatestMeasurement(uid)
                val msg =
                    if (ok) "Son ölçüm silindi"
                    else "Silinecek kayıt yok"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonClose.setOnClickListener { dismiss() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    syncRepository.observePendingCount().collect { count ->
                        binding.pendingCount.text = count.toString()
                    }
                }
                launch {
                    syncRepository.observeRemoteSnippet().collect { snippet ->
                        binding.remoteSnippet.text =
                            snippet ?: "Henüz uzaktan özet alınamadı"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_UID = "uid"

        fun newInstance(userId: Long): SyncConsoleDialogFragment =
            SyncConsoleDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_UID, userId) }
            }
    }
}
