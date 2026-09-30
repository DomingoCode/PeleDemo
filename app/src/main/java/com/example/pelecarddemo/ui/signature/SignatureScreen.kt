package com.example.pelecarddemo.ui.signature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.SignaturePoint
import com.example.pelecarddemo.ui.components.SIGNATURE_ASPECT_RATIO
import com.example.pelecarddemo.ui.components.SignaturePad
import com.example.pelecarddemo.ui.transaction.SignatureUiState
import com.example.pelecarddemo.ui.transaction.TransactionViewModel

@Composable
fun SignatureRoute(viewModel: TransactionViewModel, onCancel: () -> Unit) {
    val state by viewModel.signatureState.collectAsStateWithLifecycle()
    SignatureScreen(
        state = state,
        onStrokeStart = viewModel::onStrokeStart,
        onStrokePoint = viewModel::onStrokePoint,
        onClear = viewModel::onSignatureClear,
        onSubmit = viewModel::onSignatureSubmit,
        onCancel = onCancel,
    )
}

@Composable
fun SignatureScreen(
    state: SignatureUiState,
    onStrokeStart: (SignaturePoint) -> Unit,
    onStrokePoint: (SignaturePoint) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.signature),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onClear, enabled = state.canSubmit) {
                    Text(stringResource(R.string.clear))
                }
            }

            // The pad takes the largest 2:1 rectangle that fits the free space (portrait or landscape).
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.aspectRatio(SIGNATURE_ASPECT_RATIO),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    SignaturePad(
                        strokes = state.strokes,
                        onStrokeStart = onStrokeStart,
                        onStrokePoint = onStrokePoint,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.submit))
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
