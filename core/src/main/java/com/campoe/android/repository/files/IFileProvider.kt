package com.campoe.android.repository.files

import java.io.InputStream
import java.io.OutputStream

internal interface IFileProvider {

    fun output(): OutputStream

    fun input(): InputStream

}