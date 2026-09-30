package com.example.pelecarddemo.ui.receipt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.Transaction
import com.example.pelecarddemo.domain.formatReceiptDate
import com.example.pelecarddemo.ui.components.ReceiptShape
import com.example.pelecarddemo.ui.components.SIGNATURE_ASPECT_RATIO
import com.example.pelecarddemo.ui.components.SignatureView
import com.example.pelecarddemo.ui.theme.BrandStampRed
import com.example.pelecarddemo.ui.theme.Fraunces
import com.example.pelecarddemo.ui.theme.SectionLabel
import com.example.pelecarddemo.ui.transaction.TransactionViewModel

@Composable
fun ReceiptRoute(viewModel: TransactionViewModel, onFinish: () -> Unit, onConvert: () -> Unit) {
    val transaction by viewModel.receipt.collectAsStateWithLifecycle()
    val finish = {
        viewModel.onFinish()
        onFinish()
    }

    // Back on the receipt behaves like Finish: the user cannot return into a completed transaction.
    BackHandler(onBack = finish)

    val current = transaction
    if (current == null) {
        // No receipt to show (e.g. the process was recreated): go back to the main screen.
        LaunchedEffect(Unit) { onFinish() }
    } else {
        ReceiptScreen(transaction = current, onFinish = finish, onConvert = onConvert)
    }
}

@Composable
fun ReceiptScreen(
    transaction: Transaction,
    onFinish: () -> Unit,
    onConvert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        onClick = onConvert,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_currency_exchange_24),
                                // Decorative: the button's own label ("Convert") already names the action.
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.convert), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    OutlinedButton(
                        onClick = onFinish,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Text(stringResource(R.string.finish), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_24),
                        // Decorative: the caption right below already says "Receipt printed".
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.receipt_printed_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = ReceiptShape(cornerRadius = 16.dp, toothWidth = 16.dp, toothDepth = 8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 40.dp)) {
                    Text(
                        text = stringResource(R.string.receipt_number_label, transaction.receiptNumber).uppercase(),
                        style = SectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        Text(
                            text = "${transaction.currency?.symbol ?: ""}${transaction.amount.toPlainString()}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = stringResource(R.string.demo_badge).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = BrandStampRed,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .rotate(-14f)
                                .border(BorderStroke(1.5.dp, BrandStampRed), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    transaction.installments?.let { installments ->
                        ReceiptRow(label = stringResource(R.string.installments), value = installments.toString())
                    }
                    transaction.currency?.let { currency ->
                        ReceiptRow(label = stringResource(R.string.currency), value = currency.code)
                    }
                    ReceiptRow(label = stringResource(R.string.date_label), value = formatReceiptDate(transaction.timestampMillis))

                    transaction.signature?.let { signature ->
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(SIGNATURE_ASPECT_RATIO),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            SignatureView(signature = signature, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.signature),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
