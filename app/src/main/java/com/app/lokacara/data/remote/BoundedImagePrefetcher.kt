package com.app.lokacara.data.remote

import android.content.Context
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Precision

class BoundedImagePrefetcher(
    private val context: Context,
    private val imageLoader: ImageLoader,
    private val maxRequests: Int
) {
    private val requests = linkedMapOf<String, Disposable>()

    @Synchronized
    fun replace(urls: Iterable<String>, sizePx: Int) {
        val desired = urls.asSequence()
            .filter(String::isNotBlank)
            .distinct()
            .take(maxRequests)
            .toSet()

        requests.keys.filterNot(desired::contains).forEach { url ->
            requests.remove(url)?.dispose()
        }

        desired.filterNot(requests::containsKey).forEach { url ->
            requests[url] = imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .size(sizePx)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .build()
            )
        }
    }

    @Synchronized
    fun clear() {
        requests.values.forEach(Disposable::dispose)
        requests.clear()
    }
}
