package com.campoe.android.repository.files

import android.content.Context
import java.io.FileInputStream
import java.io.FileOutputStream

class ContextFileProvider(private val context: Context, filename: String) : FileProvider(filename) {

    override fun output(): FileOutputStream =
        context.openFileOutput(filename, Context.MODE_PRIVATE)

    override fun input(): FileInputStream = context.openFileInput(filename)

}