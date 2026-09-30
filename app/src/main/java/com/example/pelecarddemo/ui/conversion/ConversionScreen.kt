package com.example.pelecarddemo.ui.conversion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pelecarddemo.R
import com.example.pelecarddemo.data.ExchangeRateRepository
import com.example.pelecarddemo.domain.ConversionRate
import java.math.BigDecimal

@Composable
fun ConversionRoute(
    repository: ExchangeRateRepository,
    amount: BigDecimal,
    baseCurrencyCode: String,
    onBack: () -> Unit,
) {
    val viewModel: ConversionViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ConversionViewModel(repository, amount, baseCurrencyCode) }
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConversionScreen(state = state, onRetry = viewModel::retry, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    state: ConversionUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.conversion_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is ConversionUiState.Loading -> CircularProgressIndicator()
                is ConversionUiState.Error -> ConversionErrorContent(onRetry = onRetry)
                is ConversionUiState.Success -> ConversionRatesList(state)
            }
        }
    }
}

@Composable
private fun ConversionErrorContent(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.conversion_error),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun ConversionRatesList(state: ConversionUiState.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
    ) {
        item {
            Text(
                text = stringResource(
                    R.string.conversion_header,
                    state.amount.toPlainString(),
                    state.baseCurrencyCode,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(state.rates, key = { it.currencyCode }) { rate ->
            ConversionRow(rate = rate, baseCurrencyCode = state.baseCurrencyCode)
            HorizontalDivider()
        }
    }
}

@Composable
private fun ConversionRow(rate: ConversionRate, baseCurrencyCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(text = rate.currencyCode, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.conversion_rate, baseCurrencyCode, rate.rate.toPlainString(), rate.currencyCode),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${rate.convertedAmount.toPlainString()} ${rate.currencyCode}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
