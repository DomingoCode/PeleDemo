package com.example.pelecarddemo.ui.conversion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pelecarddemo.R
import com.example.pelecarddemo.data.ExchangeRateRepository
import com.example.pelecarddemo.domain.ConversionRate
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.formatTime
import com.example.pelecarddemo.ui.theme.BrandErrorContainer
import com.example.pelecarddemo.ui.theme.BrandGreenContainer
import com.example.pelecarddemo.ui.theme.BrandStampRed
import com.example.pelecarddemo.ui.theme.Fraunces
import com.example.pelecarddemo.ui.theme.SectionLabel
import java.math.BigDecimal

private const val RATES_PROVIDER = "open.er-api.com"

@Composable
fun ConversionRoute(
    repository: ExchangeRateRepository,
    amount: BigDecimal,
    baseCurrencyCode: String,
    receiptNumber: Int,
    onBack: () -> Unit,
) {
    val viewModel: ConversionViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ConversionViewModel(repository, amount, baseCurrencyCode) }
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConversionScreen(
        state = state,
        amount = amount,
        baseCurrencyCode = baseCurrencyCode,
        receiptNumber = receiptNumber,
        onRetry = viewModel::retry,
        onBack = onBack,
    )
}

@Composable
fun ConversionScreen(
    state: ConversionUiState,
    amount: BigDecimal,
    baseCurrencyCode: String,
    receiptNumber: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseSymbol = Currency.entries.find { it.code == baseCurrencyCode }?.symbol.orEmpty()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (state is ConversionUiState.Error) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh_24),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.retry), style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(stringResource(R.string.back_to_receipt), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = stringResource(R.string.back_to_receipt),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.conversion_title).uppercase(),
                    style = SectionLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "$baseSymbol${amount.toPlainString()}",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.conversion_receipt_line, receiptNumber, baseCurrencyCode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (state) {
                is ConversionUiState.Loading -> LoadingContent(modifier = Modifier.weight(1f))
                is ConversionUiState.Error -> ErrorContent(modifier = Modifier.weight(1f))
                is ConversionUiState.Success -> ContentList(state = state, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.conversion_loading), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(40.dp))
        repeat(3) { index ->
            if (index > 0) Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {}
        }
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = BrandErrorContainer,
            modifier = Modifier.size(88.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_off_24),
                    contentDescription = null,
                    tint = BrandStampRed,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.conversion_error_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.conversion_error_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ContentList(state: ConversionUiState.Success, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.conversion_column_currency).uppercase(),
                    style = SectionLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.conversion_column_amount).uppercase(),
                    style = SectionLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(state.rates, key = { it.currencyCode }) { rate ->
            ConversionCard(rate = rate, baseCurrencyCode = state.baseCurrencyCode)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.conversion_footer, RATES_PROVIDER, formatTime(state.fetchedAtMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConversionCard(rate: ConversionRate, baseCurrencyCode: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = BrandGreenContainer, modifier = Modifier.size(40.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = rate.currencyCode.take(1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rate.currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.conversion_rate, baseCurrencyCode, rate.rate.toPlainString(), rate.currencyCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${rate.convertedAmount.toPlainString()} ${rate.currencyCode}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
