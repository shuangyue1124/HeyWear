package com.m16a4666.heywear.interact

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.m16a4666.heywear.utils.ImageSaver
import kotlinx.coroutines.launch

@Composable
internal fun rememberImageSaveAction(): (String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingUrl by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val url = pendingUrl
        pendingUrl = null
        if (isGranted && url != null) {
            scope.launch { ImageSaver.saveImage(context, url) }
        } else {
            Toast.makeText(context, "需要存储权限才能保存", Toast.LENGTH_SHORT).show()
        }
    }

    return { url ->
        val needsPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            pendingUrl = url
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            scope.launch { ImageSaver.saveImage(context, url) }
        }
    }
}
