package com.campoe.android.repository.files

import java.io.*
import java.nio.file.Files
import java.nio.file.Paths

abstract class FileProvider(protected val filename: String) : IFileProvider {

    override fun output(): OutputStream {
        return Files.newOutputStream(Paths.get(filename))
    }

    override fun input(): InputStream {
        return Files.newInputStream(Paths.get(filename))
    }

    inline fun withOutput(f: (OutputStream) -> Unit) {
        var fos: OutputStream? = null
        try {
            fos = output()
            f.invoke(fos)
        } finally {
            fos?.flush()
            fos?.close()
        }
    }

    inline fun withInput(f: (InputStream) -> Unit) {
        var fis: InputStream? = null
        try {
            fis = input()
            f.invoke(fis)
        } finally {
            fis?.close()
        }
    }

    inline fun withBufferedWriter(f: (BufferedWriter) -> Unit) {
        var fos: OutputStream? = null
        var bw: BufferedWriter? = null
        try {
            fos = output()
            bw = BufferedWriter(OutputStreamWriter(fos))
            f.invoke(bw)
        } finally {
            fos?.close()
            bw?.flush()
            bw?.close()
        }
    }

    inline fun withBufferedReader(f: (BufferedReader) -> Unit) {
        var fis: InputStream? = null
        var br: BufferedReader? = null
        try {
            fis = input()
            br = BufferedReader(InputStreamReader(fis))
            f.invoke(br)
        } finally {
            fis?.close()
            br?.close()
        }
    }

}