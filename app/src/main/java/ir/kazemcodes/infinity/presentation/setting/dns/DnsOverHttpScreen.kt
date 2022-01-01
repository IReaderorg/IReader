package ir.kazemcodes.infinity.presentation.setting.dns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.data.network.models.dnsOverHttps
import ir.kazemcodes.infinity.data.network.utils.toast
import ir.kazemcodes.infinity.presentation.book_detail.Constants
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.presentation.setting.SettingViewModel

@Composable
fun DnsOverHttpScreen(viewModel: SettingViewModel) {
    val backStack = LocalBackstack.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(title = "DnsOverHttp")
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                navigationIcon = { TopAppBarBackButton(backStack = backStack) }
            )
        },
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)) {
            dnsOverHttps.forEach {
                RadioButtonWithTitleComposable(text = it.title,
                    selected = viewModel.state.value.doh == it.prefCode,
                    onClick = {
                        viewModel.setDohPrfUpdate(it.prefCode)
                        context.toast("relaunch the app to make this take effect")
                    })
            }
        }


    }

}