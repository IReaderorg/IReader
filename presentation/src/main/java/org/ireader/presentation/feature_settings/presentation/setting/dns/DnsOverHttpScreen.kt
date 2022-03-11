package org.ireader.presentation.feature_settings.presentation.setting.dns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.ireader.core.utils.Constants
import org.ireader.domain.utils.toast
import org.ireader.domain.view_models.settings.SettingViewModel
import org.ireader.infinity.core.data.network.models.dnsOverHttps
import org.ireader.presentation.feature_library.presentation.components.RadioButtonWithTitleComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle

@Composable
fun DnsOverHttpScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: SettingViewModel = hiltViewModel(),
) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.systemBarsPadding(),
                title = {
                    TopAppBarTitle(title = "DnsOverHttp")
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                navigationIcon = { TopAppBarBackButton(navController = navController) }
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