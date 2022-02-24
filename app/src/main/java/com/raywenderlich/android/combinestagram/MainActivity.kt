/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.combinestagram

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    companion object {
        const val PHOTOSBOTTOMDIALOGFRAGMENT = "PhotosBottomDialogFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = getString(R.string.collage)

        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        addButton.setOnClickListener {
            actionAdd()
        }

        clearButton.setOnClickListener {
            actionClear()
        }

        saveButton.setOnClickListener {
            actionSave()
        }

        viewModel.getSelectedPhotos().observe(this, Observer { photos ->
            photos?.let {
                if (photos.isNotEmpty()) {
                    val bitmaps = photos.map {
                        BitmapFactory.decodeResource(resources, it.drawable)
                    }
                    // 3
                    val newBitmap = combineImages(bitmaps)
                    // 4
                    collageImage.setImageDrawable(
                        BitmapDrawable(resources, newBitmap)
                    )
                } else {
                    collageImage.setImageResource(android.R.color.transparent)
                }
                updateUi(photos)
            }
        })

    }

    private fun actionAdd() {
        val addPhotoBottomDialogFragment = PhotosBottomDialogFragment.newInstance()
        addPhotoBottomDialogFragment.show(supportFragmentManager, PHOTOSBOTTOMDIALOGFRAGMENT)
        viewModel.subscribeSelectedPhotos(
            addPhotoBottomDialogFragment.selectedPhotos
        )
        //viewModel.addPhoto(PhotoStore.photos[0])
    }

    private fun actionClear() {
        viewModel.clearPhotos()
    }

    private fun actionSave() {
        viewModel.saveBitmapFromImageView(collageImage, this)
                /*
                You may have noticed that, when you saved a collage, the Save button freezes in the tapped state and the UI stopped responding.
                Saving a photo to storage can take a long time, and is best done on a background thread.

                The subscribeOn() method instructs the Single to do its subscription work on the IO scheduler.
                The observeOn() method instructs the single to run the subscribeBy() code on the Android main thread.
                * */
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Log.e("MainActivity", "Se chay cai nay truoc, 1 runner")
                Toast.makeText(this, "Saving....",
                    Toast.LENGTH_SHORT).show()
            }
            .subscribeBy(
                onSuccess = { file ->
                    Log.e("MainActivity", "Save Complete")
                    Toast.makeText(this, "$file saved",
                        Toast.LENGTH_SHORT).show()
                },
                onError = { e ->
                    Log.e("MainActivity", "Save Error")
                    Toast.makeText(this,
                        "Error saving file :${e.localizedMessage}",
                        Toast.LENGTH_SHORT).show()
                }
            )
    }

    private fun updateUi(photos: List<Photo>) {
        saveButton.isEnabled =
            photos.isNotEmpty() && (photos.size % 2 == 0)
        clearButton.isEnabled = photos.isNotEmpty()
        addButton.isEnabled = photos.size < 6
        title = if (photos.isNotEmpty()) {
            resources.getQuantityString(
                R.plurals.photos_format,
                photos.size, photos.size
            )
        } else {
            getString(R.string.collage)
        }

        //Fix bug
        val photosBottomDialogFragment =
            supportFragmentManager.findFragmentByTag(PHOTOSBOTTOMDIALOGFRAGMENT) as PhotosBottomDialogFragment?
        if (photosBottomDialogFragment != null && photosBottomDialogFragment.isVisible) {
            if (photos.size >= 6)
                supportFragmentManager.beginTransaction().remove(photosBottomDialogFragment)
                    .commit()
        }
    }

    override fun onPause() {
        Log.e("MainActivity", "Se chay onPause")
        super.onPause()
    }

    override fun onResume() {
        Log.e("MainActivity", "Se chay onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.e("MainActivity", "Se chay onDestroy")
        super.onDestroy()

    }

}
