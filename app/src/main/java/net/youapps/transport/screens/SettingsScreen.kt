package net.youapps.transport.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.schildbach.pte.NetworkId
import net.youapps.transport.R
import net.youapps.transport.components.TooltipIconButton
import net.youapps.transport.components.TransportNetworkRow
import net.youapps.transport.data.TransportNetworks
import net.youapps.transport.models.SettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsModel: SettingsModel) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.transport_network)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    TooltipIconButton(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    ) {
                        navController.popBackStack()
                    }
                },
            )
        }
    ) { pV ->
        Column(
            modifier = Modifier
                .padding(pV)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val savedNetwork by settingsModel.savedNetworkFlow.collectAsStateWithLifecycle(NetworkId.DB)
            val networkGroups = remember {
                TransportNetworks.networks.sortedBy { it.continent }
                    .groupBy { it.country to it.continent }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                for ((group, networks) in networkGroups) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "${group.first.name} (${group.second.name})",
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(networks) { network ->
                        TransportNetworkRow(
                            network = network,
                            isSelected = network.id == savedNetwork
                        ) {
                            // only update if the network would be changed
                            if (network.id != savedNetwork) settingsModel.setNetwork(network)
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}