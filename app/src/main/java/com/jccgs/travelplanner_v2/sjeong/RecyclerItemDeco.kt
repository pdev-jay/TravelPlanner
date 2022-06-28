package com.jccgs.travelplanner_v2.sjeong

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemDeco(val context: Context): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val index = parent.getChildAdapterPosition(view)
        if(index % 1 == 0){
            outRect.set(10, 10, 10, 20)
        }
//        view.setBackgroundColor(Color.WHITE)
//        ViewCompat.setElevation(view, 10.0f)
    }
}