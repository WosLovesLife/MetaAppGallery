package com.example.metaappgallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import com.example.metaappgallery.pojo.PictureData
import com.example.metaappgallery.repository.GalleryRepository
import com.example.metaappgallery.ui.StatusLayout
import com.example.metaappgallery.ui.singleArgViewModelFactory
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {
    companion object {
        /**
         * 通过工厂创建 [GalleryViewModel]
         *
         * @param arg 将 repository 传给 [GalleryViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::GalleryViewModel)
    }

    private val _data =
        MutableLiveData<MutableList<PictureData>>().apply { arrayListOf<PictureData>() }

    val data: LiveData<MutableList<PictureData>>
        get() = _data

    private val _refreshLayoutShown = MutableLiveData<Boolean>(false)
    val refreshLayoutShown: LiveData<Boolean>
        get() = _refreshLayoutShown

    private val _layoutStatus =
        MutableLiveData<StatusLayout.Status>(StatusLayout.Status(StatusLayout.Status.STATUS_LOADING))
    val layoutStatus: LiveData<StatusLayout.Status>
        get() = _layoutStatus

    private val _loadMoreStatus = MutableLiveData<LoadMoreStatus>(LoadMoreStatus.Complete)
    val loadMoreStatus: LiveData<LoadMoreStatus>
        get() = _loadMoreStatus

    private val _isStaggeredGrid = MutableLiveData<Boolean>(false)
    val isStaggeredGrid: LiveData<Boolean>
        get() = _isStaggeredGrid

    private var currentKeywords: String = ""

    private var pageIndex = 1

    fun toggleLayout() {
        _isStaggeredGrid.value = !(isStaggeredGrid.value ?: true)
    }

    private suspend fun <T> updatePageStateWhileLoadData(
        isLoadMore: Boolean,
        pageLimit: Int,
        block: suspend () -> List<T>
    ) {
        if (data.value.isNullOrEmpty()) {
            _layoutStatus.value = StatusLayout.Status(StatusLayout.Status.STATUS_LOADING)
        } else if (!isLoadMore) {
            _refreshLayoutShown.value = true
            if (_loadMoreStatus.value == LoadMoreStatus.Loading) {
                _loadMoreStatus.value = LoadMoreStatus.Complete
            }
        }

        try {
            val newData = block()

            if (!isLoadMore) {
                pageIndex = 1
            } else if (newData.isNotEmpty()) {
                ++pageIndex
            }

            _refreshLayoutShown.value = false
            _layoutStatus.value = if (newData.isNullOrEmpty()) {
                StatusLayout.Status(StatusLayout.Status.STATUS_EMPTY)
            } else {
                StatusLayout.Status(StatusLayout.Status.STATUS_CONTENT)
            }
            _loadMoreStatus.value = if (newData.size < pageLimit) {
                LoadMoreStatus.End
            } else {
                LoadMoreStatus.Complete
            }
        } catch (cause: Throwable) {
            if (isLoadMore) {
                _loadMoreStatus.value = LoadMoreStatus.Fail
            } else {
                _refreshLayoutShown.value = false
                _layoutStatus.value = StatusLayout.Status(StatusLayout.Status.STATUS_ERROR, cause)
            }
        }
    }

    fun refresh(keywords: String) {
        viewModelScope.launch {
            updatePageStateWhileLoadData(false, GalleryRepository.PAGE_LIMIT) {
                val newData = repository.loadLandmarks(keywords, 1)
                _data.value = newData.toMutableList()
                currentKeywords = keywords
                newData
            }
        }
    }

    fun refresh() {
        refresh(currentKeywords)
    }

    fun loadMore() {
        viewModelScope.launch {
            updatePageStateWhileLoadData(true, GalleryRepository.PAGE_LIMIT) {
                val newData = repository.loadLandmarks(currentKeywords, pageIndex + 1)
                val currentData = _data.value ?: arrayListOf()
                currentData.addAll(newData)
                _data.value = currentData
                newData
            }
        }
    }
}