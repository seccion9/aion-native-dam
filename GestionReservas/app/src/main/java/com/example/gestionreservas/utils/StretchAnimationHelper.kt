// ui/helpers/StretchAnimationHelper.kt
package com.example.gestionreservas.ui.helpers

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object StretchAnimationHelper {

    @SuppressLint("ClickableViewAccessibility")
    fun activar(
        recyclerView: RecyclerView,
        swipeRefresh: SwipeRefreshLayout,
        onRefreshTriggered: () -> Unit
    ) {
        var startY = 0f
        var isDragging = false
        var isRefreshing = false
        val refreshThreshold = 120f

        recyclerView.setOnTouchListener { _, event ->
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val firstVisibleItem = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: -1

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isDragging = false
                    isRefreshing = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.y - startY
                    val canScrollUp = recyclerView.canScrollVertically(-1)

                    if (dy > 0 && !canScrollUp && firstVisibleItem == 0 && !swipeRefresh.isRefreshing) {
                        isDragging = true
                        for (i in 0 until recyclerView.childCount) {
                            val child = recyclerView.getChildAt(i)
                            child.translationY = dy / 2
                        }
                        if (dy > refreshThreshold) {
                            isRefreshing = true
                        }
                        return@setOnTouchListener true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        for (i in 0 until recyclerView.childCount) {
                            val child = recyclerView.getChildAt(i)
                            child.animate().translationY(0f).setDuration(300).start()
                        }
                        if (isRefreshing) {
                            onRefreshTriggered()
                        }
                        isDragging = false
                        isRefreshing = false
                    }
                }
            }
            false
        }
    }
}
