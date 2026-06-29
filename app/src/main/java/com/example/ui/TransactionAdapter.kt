package com.example.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.Transaction
import com.example.databinding.ItemTransactionGridBinding
import com.example.databinding.ItemTransactionListBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClicked: (Transaction) -> Unit
) : ListAdapter<Transaction, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem == newItem
            }
        }
    }

    var isGridView: Boolean = false

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_GRID) {
            val binding = ItemTransactionGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemTransactionListBinding.inflate(inflater, parent, false)
            ListViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transaction = getItem(position)
        if (holder is GridViewHolder) {
            holder.bind(transaction)
        } else if (holder is ListViewHolder) {
            holder.bind(transaction)
        }
    }

    inner class ListViewHolder(private val binding: ItemTransactionListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title

            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                .format(Date(transaction.date))
            binding.tvCategoryDate.text = "${transaction.category} • $formattedDate"

            val formattedAmount = formatRupiah(transaction.amount)
            if (transaction.isExpense) {
                binding.tvAmount.text = "- $formattedAmount"
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F"))
            } else {
                binding.tvAmount.text = "+ $formattedAmount"
                binding.tvAmount.setTextColor(Color.parseColor("#388E3C"))
            }

            binding.tvCategoryIcon.text = transaction.category.firstOrNull()?.toString()?.uppercase() ?: "T"

            val color = getCategoryColor(transaction.category)
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            binding.iconContainer.background = drawable
        }
    }

    inner class GridViewHolder(private val binding: ItemTransactionGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.tvGridTitle.text = transaction.title
            binding.tvGridCategory.text = "Kategori: ${transaction.category}"

            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                .format(Date(transaction.date))
            binding.tvGridDate.text = formattedDate

            val formattedAmount = formatRupiah(transaction.amount)
            if (transaction.isExpense) {
                binding.tvGridAmount.text = "- $formattedAmount"
                binding.tvGridAmount.setTextColor(Color.parseColor("#D32F2F"))
                binding.tvGridTypeBadge.text = "PENGELUARAN"
                binding.tvGridTypeBadge.setBackgroundColor(Color.parseColor("#D32F2F"))
            } else {
                binding.tvGridAmount.text = "+ $formattedAmount"
                binding.tvGridAmount.setTextColor(Color.parseColor("#388E3C"))
                binding.tvGridTypeBadge.text = "PEMASUKAN"
                binding.tvGridTypeBadge.setBackgroundColor(Color.parseColor("#388E3C"))
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    private fun getCategoryColor(category: String): Int {
        return when (category.lowercase(Locale.getDefault())) {
            "makanan", "makanan & minuman" -> Color.parseColor("#FF9800")
            "transportasi" -> Color.parseColor("#2196F3")
            "belanja" -> Color.parseColor("#9C27B0")
            "hiburan", "hiburan & rekreasi" -> Color.parseColor("#E91E63")
            "gaji" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#607D8B")
        }
    }
}
