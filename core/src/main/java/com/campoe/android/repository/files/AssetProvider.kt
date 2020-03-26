package com.campoe.android.repository.files

import android.content.Context
import java.io.InputStream
import java.io.OutputStream

class AssetProvider(private val context: Context, filename: String) : FileProvider(filename) {

    @Throws(UnsupportedOperationException::class)
    override fun output(): OutputStream {
        throw UnsupportedOperationException("Can't write to an asset file.")
    }

    override fun input(): InputStream = context.assets.open(filename)

}