package com.example.pelecarddemo.ui.receipt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.Transaction
import com.example.pelecarddemo.ui.components.SIGNATURE_ASPECT_RATIO
import com.example.pelecarddemo.ui.components.SignatureView
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
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.receipt),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            ReceiptRow(
                label = stringResource(R.string.amount),
                value = transaction.amount.toPlainString(),
            )
            transaction.installments?.let { installments ->
                ReceiptRow(label = stringResource(R.string.installments), value = installments.toString())
            }
            transaction.currency?.let { currency ->
                ReceiptRow(label = stringResource(R.string.currency), value = currency.code)
            }
            transaction.signature?.let { signature ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.signature),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(SIGNATURE_ASPECT_RATIO),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    SignatureView(signature = signature, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(onClick = onConvert, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.convert))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.finish))
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.titleLarge)
    }
}
