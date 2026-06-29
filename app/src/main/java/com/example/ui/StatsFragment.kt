package com.example.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.databinding.FragmentStatsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStats()
    }

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTransactions.collectLatest { transactions ->
                val expenses = transactions.filter { it.isExpense }
                val totalExpense = expenses.sumOf { it.amount }

                // Group by categories
                var makananAmount = 0.0
                var transportasiAmount = 0.0
                var belanjaAmount = 0.0
                var hiburanAmount = 0.0
                var lainAmount = 0.0

                for (expense in expenses) {
                    when (expense.category) {
                        "Makanan & Minuman" -> makananAmount += expense.amount
                        "Transportasi" -> transportasiAmount += expense.amount
                        "Belanja" -> belanjaAmount += expense.amount
                        "Hiburan & Rekreasi" -> hiburanAmount += expense.amount
                        else -> lainAmount += expense.amount
                    }
                }

                // Bind text values
                binding.tvStatMakanan.text = formatRupiah(makananAmount)
                binding.tvStatTransportasi.text = formatRupiah(transportasiAmount)
                binding.tvStatBelanja.text = formatRupiah(belanjaAmount)
                binding.tvStatHiburan.text = formatRupiah(hiburanAmount)
                binding.tvStatLain.text = formatRupiah(lainAmount)

                // Bind progress indicators
                if (totalExpense > 0) {
                    binding.progressMakanan.progress = ((makananAmount / totalExpense) * 100).toInt()
                    binding.progressTransportasi.progress = ((transportasiAmount / totalExpense) * 100).toInt()
                    binding.progressBelanja.progress = ((belanjaAmount / totalExpense) * 100).toInt()
                    binding.progressHiburan.progress = ((hiburanAmount / totalExpense) * 100).toInt()
                    binding.progressLain.progress = ((lainAmount / totalExpense) * 100).toInt()
                } else {
                    binding.progressMakanan.progress = 0
                    binding.progressTransportasi.progress = 0
                    binding.progressBelanja.progress = 0
                    binding.progressHiburan.progress = 0
                    binding.progressLain.progress = 0
                }
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
