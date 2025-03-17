package com.example.naver

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng

class SearchAdapter(
    private val results: List<Pair<String, LatLng>>,
    private val onItemClick: (LatLng) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.search_result_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, location) = results[position]
        holder.textView.text = name
        holder.itemView.setOnClickListener {
            Log.d("SEARCH_LOCATION", "사용자가 선택한 위치 - 위도: ${location.latitude}, 경도: ${location.longitude}") // ✅ 로그 출력
            onItemClick(location) // ✅ MainActivity에서 전달받은 클릭 이벤트 실행
        }
    }



    override fun getItemCount() = results.size
}
