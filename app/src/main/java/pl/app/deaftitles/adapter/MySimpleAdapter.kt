package pl.app.deaftitles.adapter

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class MySimpleAdapter<T>(private val item: Int,
                         private val itemListener: ((item: T) -> Unit)? = null,
                         private val onSimpleHolder: ((item: T, view: View) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = ArrayList<T>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? BindViewHolder<T>)?.onBind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            MySimpleHolder(LayoutInflater.from(parent.context).inflate(item, parent, false))

    inner class MySimpleHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindViewHolder<T> {

        override fun onBind(item: T) {
            itemView.apply {
                setOnClickListener {
                    itemListener?.invoke(items[position])
                }
                onSimpleHolder?.invoke(item, itemView)
            }
        }
    }
}

fun <T> RecyclerView.adapter(item: Int, items: ArrayList<T>, orientation: Int = RecyclerView.VERTICAL, decoration: Boolean = false, viewHolder: ((item: T, view: View) -> Unit)? = null, click: ((item: T) -> Unit)? = null) = this.let {
    itemAnimator = DefaultItemAnimator()
    layoutManager = LinearLayoutManager(this.context, orientation, false)
    MySimpleAdapter(item, click, viewHolder).apply {
        this@adapter.adapter = this
        this.items = items
    }
    if (decoration) {
        val dividerItemDecoration = DividerItemDecoration(this.context, orientation)
        addItemDecoration(dividerItemDecoration)
    }
}