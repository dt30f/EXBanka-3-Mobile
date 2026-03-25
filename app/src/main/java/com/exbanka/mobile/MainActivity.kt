package com.exbanka.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exbanka.mobile.model.SessionUser
import com.exbanka.mobile.ui.screen.HomeScreen
import com.exbanka.mobile.ui.screen.LoginScreen
import com.exbanka.mobile.ui.screen.VerificationDetailsScreen
import com.exbanka.mobile.ui.screen.VerificationListScreen
import com.exbanka.mobile.ui.theme.ExBankaMobileTheme
import com.exbanka.mobile.ui.viewmodel.AuthViewModel
import com.exbanka.mobile.ui.viewmodel.HomeViewModel
import com.exbanka.mobile.ui.viewmodel.VerificationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AppContainer(applicationContext)

        setContent {
            ExBankaMobileTheme {
                ClientMobileApp(container)
            }
        }
    }
}

private enum class MainTab(val title: String) {
    HOME("Početna"),
    VERIFICATION("Verifikacija"),
}

@Composable
private fun ClientMobileApp(container: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(
        factory = simpleFactory { AuthViewModel(container.authRepository) },
    )
    val authState by authViewModel.uiState.collectAsState()

    if (authState.isBootstrapping) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val session = authState.session
    if (session == null) {
        LoginScreen(
            uiState = authState,
            onEmailChange = authViewModel::updateEmail,
            onPasswordChange = authViewModel::updatePassword,
            onLoginClick = authViewModel::login,
        )
        return
    }

    LoggedInShell(
        session = session,
        authViewModel = authViewModel,
        container = container,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoggedInShell(
    session: SessionUser,
    authViewModel: AuthViewModel,
    container: AppContainer,
) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = simpleFactory { HomeViewModel(container.bankingRepository) },
    )
    val verificationViewModel: VerificationViewModel = viewModel(
        factory = simpleFactory { VerificationViewModel(container.bankingRepository) },
    )

    val homeState by homeViewModel.uiState.collectAsState()
    val verificationState by verificationViewModel.uiState.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    LaunchedEffect(session.clientId) {
        homeViewModel.load(session)
        verificationViewModel.load(session)
    }

    val selectedVerification = verificationState.selectedItem
    val title = when {
        selectedVerification != null -> "Detalji verifikacije"
        currentTab == MainTab.HOME -> MainTab.HOME.title
        else -> MainTab.VERIFICATION.title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(),
                navigationIcon = {
                    if (selectedVerification != null) {
                        IconButton(onClick = verificationViewModel::closeDetails) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            homeViewModel.reset()
                            verificationViewModel.reset()
                            authViewModel.logout()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Odjava")
                    }
                },
            )
        },
        bottomBar = {
            if (selectedVerification == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == MainTab.HOME,
                        onClick = { currentTab = MainTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(MainTab.HOME.title) },
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.VERIFICATION,
                        onClick = { currentTab = MainTab.VERIFICATION },
                        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                        label = { Text(MainTab.VERIFICATION.title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            selectedVerification != null -> VerificationDetailsScreen(
                modifier = Modifier.padding(innerPadding),
                item = selectedVerification,
                uiState = verificationState,
                onShowCode = verificationViewModel::showVerificationCode,
                onConfirm = verificationViewModel::confirmSelected,
                onIgnore = verificationViewModel::rejectSelected,
                onBack = verificationViewModel::closeDetails,
            )

            currentTab == MainTab.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                session = session,
                uiState = homeState,
                onRefresh = { homeViewModel.load(session) },
                onSelectAccount = homeViewModel::selectAccount,
            )

            else -> VerificationListScreen(
                modifier = Modifier.padding(innerPadding),
                session = session,
                uiState = verificationState,
                onRefresh = { verificationViewModel.load(session) },
                onFilterChange = verificationViewModel::updateFilter,
                onItemClick = verificationViewModel::openDetails,
            )
        }
    }
}

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            @Suppress("UNCHECKED_CAST")
            return create() as VM
        }
    }
