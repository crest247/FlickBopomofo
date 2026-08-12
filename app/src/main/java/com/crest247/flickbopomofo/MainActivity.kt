package com.crest247.flickbopomofo

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import com.crest247.flickbopomofo.core.theme.KeyboardTheme
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyboardTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dimens = LocalKeyboardDimens.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.mainScreenPadding),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "步驟1：啟用輸入法",
            fontSize = dimens.previewTextSize
        )
        Text(
            text = "在鍵盤設定中，將「Flick注音輸入法」的開關打開"
        )
        Spacer(modifier = Modifier.height(dimens.mainScreenButtonSpacing))
        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("開啟鍵盤設定")
        }

        Spacer(modifier = Modifier.height(dimens.mainScreenBlockSpacing))

        Text(
            text = "步驟2：選擇輸入法",
            fontSize = dimens.previewTextSize
        )
        Text(
            text = "選擇「Flick注音輸入法」"
        )
        Spacer(modifier = Modifier.height(dimens.mainScreenButtonSpacing))
        Button(
            onClick = { context.getSystemService<InputMethodManager>()?.showInputMethodPicker() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("選擇輸入法")
        }
    }
}