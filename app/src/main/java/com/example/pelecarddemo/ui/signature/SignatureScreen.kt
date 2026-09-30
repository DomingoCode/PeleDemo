package com.example.pelecarddemo.ui.signature

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.SignaturePoint
import com.example.pelecarddemo.ui.components.SIGNATURE_ASPECT_RATIO
import com.example.pelecarddemo.ui.components.SignaturePad
import com.example.pelecarddemo.ui.theme.Fraunces
import com.example.pelecarddemo.ui.theme.SectionLabel
import com.example.pelecarddemo.ui.transaction.SignatureUiState
import com.example.pelecarddemo.ui.transaction.TransactionViewModel
import java.math.BigDecimal

@Composable
fun SignatureRoute(viewModel: TransactionViewModel, onCancel: () -> Unit) {
    val state by viewModel.signatureState.collectAsStateWithLifecycle()
    val mainState by viewModel.mainState.collectAsStateWithLifecycle()
    SignatureScreen(
        amount = mainState.amount,
        currency = mainState.currency,
        showCurrency = mainState.showCurrency,
        installments = if (mainState.showInstallments && mainState.installmentsOn) mainState.installments else null,
        installmentAmount = mainState.installmentAmount,
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
    amount: String,
    currency: Currency,
    showCurrency: Boolean,
    installments: Int?,
    installmentAmount: BigDecimal?,
    state: SignatureUiState,
    onStrokeStart: (SignaturePoint) -> Unit,
    onStrokePoint: (SignaturePoint) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = onClear,
                            enabled = state.canSubmit,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(stringResource(R.string.clear))
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSubmit,
                        enabled = state.canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(stringResource(R.string.submit), style = MaterialTheme.typography.titleMedium)
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
                    onClick = onCancel,
                    modifier = Modifier.border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        CircleShape,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = stringResource(R.string.back),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.signature_step_label).uppercase(),
                    style = SectionLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.signature_title, "${currency.symbol}$amount"),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (showCurrency) {
                SummaryRow(label = stringResource(R.string.currency), value = currency.code)
            }
            installments?.let {
                SummaryRow(label = stringResource(R.string.installments), value = it.toString())
                installmentAmount?.let { perInstallment ->
                    Text(
                        text = stringResource(
                            R.string.installment_breakdown,
                            it,
                            "${currency.symbol}${perInstallment.toPlainString()}",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The pad takes the largest 2:1 rectangle that fits the free space (portrait or landscape).
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .aspectRatio(SIGNATURE_ASPECT_RATIO)
                        .dashedBorder(MaterialTheme.colorScheme.outline, 16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SignaturePad(
                            strokes = state.strokes,
                            onStrokeStart = onStrokeStart,
                            onStrokePoint = onStrokePoint,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(R.string.signature_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.signature_pen_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** A dashed rounded-rect outline, drawn on top of the surface's own background. */
private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
    )
}
