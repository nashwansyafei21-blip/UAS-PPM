package com.example.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.data.Transaction
import com.example.databinding.DialogAddTransactionBinding
import com.example.databinding.FragmentTransactionsBinding
import com.example.receiver.DompetkuReceiver
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by activityViewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    private val categories = arrayOf(
        "Makanan & Minuman",
        "Transportasi",
        "Belanja",
        "Hiburan & Rekreasi",
        "Gaji",
        "Lain-lain"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeTransactions()

        binding.fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.btnLayoutToggle.setOnClickListener {
            transactionAdapter.isGridView = !transactionAdapter.isGridView
            updateLayoutManager()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            showAddEditDialog(transaction)
        }
        binding.rvTransactions.adapter = transactionAdapter
        updateLayoutManager()
    }

    private fun updateLayoutManager() {
        if (transactionAdapter.isGridView) {
            binding.rvTransactions.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.btnLayoutToggle.setImageResource(R.drawable.ic_list)
        } else {
            binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
            binding.btnLayoutToggle.setImageResource(R.drawable.ic_grid)
        }
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTransactions.collectLatest { transactions ->
                transactionAdapter.submitList(transactions)

                if (transactions.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvTransactions.visibility = View.VISIBLE
                }

                calculateSummaries(transactions)
            }
        }
    }

    private fun calculateSummaries(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in transactions) {
            if (transaction.isExpense) {
                totalExpense += transaction.amount
            } else {
                totalIncome += transaction.amount
            }
        }

        val totalBalance = totalIncome - totalExpense

        binding.tvTotalBalance.text = formatRupiah(totalBalance)
        binding.tvTotalIncome.text = formatRupiah(totalIncome)
        binding.tvTotalExpense.text = formatRupiah(totalExpense)
    }

    private fun showAddEditDialog(transaction: Transaction?) {
        val dialogBinding = DialogAddTransactionBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)

        val alertDialog = builder.create()

        // Set up spinner adapter
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        dialogBinding.spinnerCategory.adapter = spinnerAdapter

        // Pre-fill fields if in Edit Mode
        val isEditMode = transaction != null
        if (isEditMode && transaction != null) {
            dialogBinding.tvDialogTitle.setText(R.string.edit_transaction)
            dialogBinding.etTitle.setText(transaction.title)
            dialogBinding.etAmount.setText(transaction.amount.toString())
            dialogBinding.etNotes.setText(transaction.notes)

            if (transaction.isExpense) {
                dialogBinding.rbExpense.isChecked = true
            } else {
                dialogBinding.rbIncome.isChecked = true
            }

            val categoryIndex = categories.indexOf(transaction.category)
            if (categoryIndex >= 0) {
                dialogBinding.spinnerCategory.setSelection(categoryIndex)
            }

            dialogBinding.btnDelete.visibility = View.VISIBLE
            dialogBinding.btnDelete.setOnClickListener {
                viewModel.delete(transaction)
                sendActivityBroadcast("Transaksi Dihapus", "Transaksi \'${transaction.title}\' telah berhasil dihapus dari dompet.")
                alertDialog.dismiss()
                Toast.makeText(requireContext(), "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val amountStr = dialogBinding.etAmount.text.toString().trim()
            val notes = dialogBinding.etNotes.text.toString().trim()
            val isExpense = dialogBinding.rbExpense.isChecked
            val category = dialogBinding.spinnerCategory.selectedItem.toString()

            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Judul tidak boleh kosong"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                dialogBinding.etAmount.error = "Masukkan nominal yang valid"
                return@setOnClickListener
            }

            if (isEditMode && transaction != null) {
                val updatedTransaction = transaction.copy(
                    title = title,
                    amount = amount,
                    isExpense = isExpense,
                    category = category,
                    notes = notes
                )
                viewModel.update(updatedTransaction)
                sendActivityBroadcast("Transaksi Diperbarui", "Transaksi \'${title}\' sebesar ${formatRupiah(amount)} berhasil diperbarui.")
                Toast.makeText(requireContext(), "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                val newTransaction = Transaction(
                    title = title,
                    amount = amount,
                    isExpense = isExpense,
                    category = category,
                    notes = notes
                )
                viewModel.insert(newTransaction)
                sendActivityBroadcast("Transaksi Baru Ditambahkan", "Berhasil mencatat transaksi \'${title}\' sebesar ${formatRupiah(amount)}.")
                Toast.makeText(requireContext(), "Transaksi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun sendActivityBroadcast(title: String, message: String) {
        val intent = Intent(DompetkuReceiver.ACTION_TRIGGER_NOTIFICATION).apply {
            putExtra(DompetkuReceiver.EXTRA_TITLE, title)
            putExtra(DompetkuReceiver.EXTRA_MESSAGE, message)
            setPackage(requireContext().packageName)
        }
        requireContext().sendBroadcast(intent)
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
