package com.example.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.databinding.FragmentGroupBinding
import com.example.receiver.DompetkuReceiver

class GroupFragment : Fragment() {

    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendBroadcast.setOnClickListener {
            sendNotificationBroadcast()
            Toast.makeText(
                requireContext(),
                "Broadcast dikirim! Periksa panel notifikasi Anda.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendNotificationBroadcast() {
        val intent = Intent(DompetkuReceiver.ACTION_TRIGGER_NOTIFICATION).apply {
            putExtra(DompetkuReceiver.EXTRA_TITLE, "Uji Broadcast Sukses!")
            putExtra(DompetkuReceiver.EXTRA_MESSAGE, "Halo Mahasiswa UPNVJ (NPM: 2410501125)! Broadcast Receiver Anda berfungsi dengan sempurna.")
            setPackage(requireContext().packageName)
        }
        requireContext().sendBroadcast(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
