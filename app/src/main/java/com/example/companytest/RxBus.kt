package com.example.companytest

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File

class RxBus //RX java this in small work instead of this we can we multiple other things
private constructor() {
    private val UserImage: PublishSubject<File?>? = PublishSubject.create()
    fun sendUserImage(imageURI: File?) {
        imageURI?.let { UserImage?.onNext(it) }
    }

    fun observeUserImage(): Observable<File?>? {
        return UserImage
    }

    fun clearRx() {
        _instance = null
    }

    companion object {
        private var _instance: RxBus? = null
        fun getInstance(): RxBus? {
            if (_instance == null) {
                _instance = RxBus()
            }
            return _instance
        }
    }
}