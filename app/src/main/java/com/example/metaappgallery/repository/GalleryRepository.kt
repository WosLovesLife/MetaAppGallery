package com.example.metaappgallery.repository

import com.example.metaappgallery.api.GalleryApi
import com.example.metaappgallery.pojo.PictureData
import kotlinx.coroutines.withTimeout

class GalleryRepository(val network: GalleryApi) {
    companion object {
        const val PAGE_LIMIT = 20
    }

    suspend fun loadLandmarks(keyword: String, sn: Int): List<PictureData> {
        val result = withTimeout(10000) {
            network.getPictures(keyword, sn, PAGE_LIMIT)
        }
        return result.list
    }
}

class RefreshGalleryError(message: String, cause: Throwable?) : Throwable(message, cause)
