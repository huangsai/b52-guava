package com.mobile.guava.android.mvvm

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

class ActivityResultContracts(
    activityResultCaller: ActivityResultCaller,
    private val forActivityResultCallback: ForActivityResultCallback
) {
    val getContentLauncher: ActivityResultLauncher<String>
    val getMultipleContentsLauncher: ActivityResultLauncher<String>
    val createDocumentLauncher: ActivityResultLauncher<String>
    val openDocumentLauncher: ActivityResultLauncher<Array<String>>
    val openDocumentTreeLauncher: ActivityResultLauncher<Uri>
    val openMultipleDocumentsLauncher: ActivityResultLauncher<Array<String>>
    val requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
    val requestPermissionLauncher: ActivityResultLauncher<String>
    val pickContactLauncher: ActivityResultLauncher<Void>
    val startActivityForResultLauncher: ActivityResultLauncher<Intent>
    val startIntentSenderForResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    val takePictureLauncher: ActivityResultLauncher<Uri>
    val takePicturePreviewLauncher: ActivityResultLauncher<Void>
    val takeVideoLauncher: ActivityResultLauncher<Uri>

    init {
        getContentLauncher = activityResultCaller.registerForActivityResult(getContent) {
            forActivityResultCallback.onGetContent(it)
        }

        getMultipleContentsLauncher = activityResultCaller.registerForActivityResult(
            getMultipleContents
        ) {
            forActivityResultCallback.onGetMultipleContents(it)
        }

        createDocumentLauncher = activityResultCaller.registerForActivityResult(createDocument) {
            forActivityResultCallback.onCreateDocument(it)
        }

        openDocumentLauncher = activityResultCaller.registerForActivityResult(openDocument) {
            forActivityResultCallback.onOpenDocument(it)
        }

        openDocumentTreeLauncher = activityResultCaller.registerForActivityResult(
            openDocumentTree
        ) {
            forActivityResultCallback.onOpenDocumentTree(it)
        }

        openMultipleDocumentsLauncher = activityResultCaller.registerForActivityResult(
            openMultipleDocuments
        ) {
            forActivityResultCallback.onOpenMultipleDocuments(it)
        }

        requestMultiplePermissionsLauncher = activityResultCaller.registerForActivityResult(
            requestMultiplePermissions
        ) {
            forActivityResultCallback.onRequestMultiplePermissions(it)
        }

        requestPermissionLauncher = activityResultCaller.registerForActivityResult(
            requestPermission
        ) {
            forActivityResultCallback.onRequestPermission(it)
        }

        pickContactLauncher = activityResultCaller.registerForActivityResult(pickContact) {
            forActivityResultCallback.onPickContact(it)
        }

        startActivityForResultLauncher = activityResultCaller.registerForActivityResult(
            startActivityForResult
        ) {
            forActivityResultCallback.onStartActivityForResult(it)
        }

        startIntentSenderForResultLauncher = activityResultCaller.registerForActivityResult(
            startIntentSenderForResult
        ) {
            forActivityResultCallback.onStartIntentSenderForResult(it)
        }

        takePictureLauncher = activityResultCaller.registerForActivityResult(takePicture) {
            forActivityResultCallback.onTakePicture(it)
        }

        takePicturePreviewLauncher = activityResultCaller.registerForActivityResult(
            takePicturePreview
        ) {
            forActivityResultCallback.onTakePicturePreview(it)
        }

        takeVideoLauncher = activityResultCaller.registerForActivityResult(takeVideo) {
            forActivityResultCallback.onTakeVideo(it)
        }
    }

    fun unregister() {
        getContentLauncher.unregister()
        getMultipleContentsLauncher.unregister()
        createDocumentLauncher.unregister()
        openDocumentLauncher.unregister()
        openDocumentTreeLauncher.unregister()
        openMultipleDocumentsLauncher.unregister()
        requestMultiplePermissionsLauncher.unregister()
        requestPermissionLauncher.unregister()
        pickContactLauncher.unregister()
        startActivityForResultLauncher.unregister()
        startIntentSenderForResultLauncher.unregister()
        takePictureLauncher.unregister()
        takePicturePreviewLauncher.unregister()
        takeVideoLauncher.unregister()
    }
}