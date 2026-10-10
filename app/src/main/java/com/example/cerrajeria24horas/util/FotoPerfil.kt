package com.example.cerrajeria24horas.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import com.example.cerrajeria24horas.R
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

/** Guarda y muestra la imagen de perfil del usuario como base64 dentro de su documento. */
object FotoPerfil {

    private const val COL_USUARIOS = "usuarios"
    private const val CAMPO_FOTO = "fotoPerfil"
    private const val LADO_MAXIMO_PX = 384

    fun guardar(contexto: Context, uid: String, uri: Uri): Task<Void> {
        val base64 = generarBase64(contexto, uri)
        return FirebaseFirestore.getInstance().collection(COL_USUARIOS).document(uid)
            .update(CAMPO_FOTO, base64)
    }

    fun obtener(uid: String, callback: (String?) -> Unit) {
        FirebaseFirestore.getInstance().collection(COL_USUARIOS).document(uid)
            .get()
            .addOnSuccessListener { doc -> callback(doc.getString(CAMPO_FOTO)) }
            .addOnFailureListener { callback(null) }
    }

    fun eliminar(uid: String): Task<Void> =
        FirebaseFirestore.getInstance().collection(COL_USUARIOS).document(uid)
            .update(CAMPO_FOTO, FieldValue.delete())

    fun mostrar(imageView: ImageView, base64: String?) {
        if (base64.isNullOrBlank()) {
            mostrarIconoPorDefecto(imageView)
            return
        }
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val mapa = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (mapa == null) {
            mostrarIconoPorDefecto(imageView)
        } else {
            imageView.setImageBitmap(mapa)
            imageView.imageTintList = null
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(0, 0, 0, 0)
        }
    }

    fun mostrar(imageView: ImageView, uri: Uri) {
        val base64 = generarBase64(imageView.context, uri)
        if (base64.isNotEmpty()) {
            mostrar(imageView, base64)
        } else {
            mostrarIconoPorDefecto(imageView)
        }
    }

    private fun generarBase64(contexto: Context, uri: Uri): String {
        val dimensiones = leerDimensiones(contexto, uri)
        var factor = 1
        while (dimensiones.first / factor > LADO_MAXIMO_PX || dimensiones.second / factor > LADO_MAXIMO_PX) {
            factor *= 2
        }
        val entrada = contexto.contentResolver.openInputStream(uri) ?: return ""
        try {
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = factor
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            var mapa = BitmapFactory.decodeStream(entrada, null, opciones) ?: return ""
            mapa = aplicarOrientacion(mapa, leerOrientacion(contexto, uri))
            val salida = ByteArrayOutputStream()
            mapa.compress(Bitmap.CompressFormat.JPEG, 82, salida)
            return Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
        } finally {
            entrada.close()
        }
    }

    private fun leerOrientacion(contexto: Context, uri: Uri): Int {
        return try {
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                val exif = ExifInterface(entrada)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun aplicarOrientacion(mapa: Bitmap, orientacion: Int): Bitmap {
        val rotacion = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return mapa
        }
        val matriz = Matrix().apply { postRotate(rotacion) }
        val rotada = Bitmap.createBitmap(mapa, 0, 0, mapa.width, mapa.height, matriz, true)
        if (rotada != mapa) {
            mapa.recycle()
        }
        return rotada
    }

    private fun leerDimensiones(contexto: Context, uri: Uri): Pair<Int, Int> {
        val entrada = contexto.contentResolver.openInputStream(uri) ?: return 0 to 0
        try {
            val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(entrada, null, opciones)
            return opciones.outWidth to opciones.outHeight
        } catch (_: Exception) {
            return 0 to 0
        } finally {
            entrada.close()
        }
    }

    private fun mostrarIconoPorDefecto(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_person)
        imageView.imageTintList = ColorStateList.valueOf(Color.WHITE)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val margen = imageView.resources.getDimensionPixelSize(R.dimen.margen_dialogo)
        imageView.setPadding(margen, margen, margen, margen)
    }
}