package com.example.pelecarddemo.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.AmountError
import com.example.pelecarddemo.domain.ClockTime
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.PaymentRules
import com.example.pelecarddemo.ui.clock.ClockViewModel
import com.example.pelecarddemo.ui.components.AnalogClock
import com.example.pelecarddemo.ui.theme.BrandInk
import com.example.pelecarddemo.ui.theme.BrandInkMuted
import com.example.pelecarddemo.ui.theme.BrandPaper
import com.example.pelecarddemo.ui.theme.Fraunces
import com.example.pelecarddemo.ui.theme.PeleDemoTheme
import com.example.pelecarddemo.ui.theme.SectionLabel
import com.example.pelecarddemo.ui.transaction.MainUiState
import com.example.pelecarddemo.ui.transaction.TransactionViewModel
import java.math.BigDecimal

/** Connects the ViewModels to the stateless [MainScreen]. */
@Composable
fun MainRoute(
    transactionViewModel: TransactionViewModel,
    clockViewModel: ClockViewModel,
    onOpenSettings: () -> Unit,
) {
    val state by transactionViewModel.mainState.collectAsStateWithLifecycle()
    MainScreen(
        state = state,
        clock = { ClockView(clockViewModel) },
        onSettingsClick = onOpenSettings,
        onAmountChange = transactionViewModel::onAmountChange,
        onInstallmentsToggle = transactionViewModel::onInstallmentsToggle,
        onInstallmentsChange = transactionViewModel::onInstallmentsChange,
        onCurrencyChange = transactionViewModel::onCurrencyChange,
        onSignatureToggle = transactionViewModel::onSignatureToggle,
        onSubmit = transactionViewModel::onSubmit,
        onCancel = transactionViewModel::onCancel,
    )
}

/** Collects the time here so only the clock recomposes every second, not the whole screen. */
@Composable
private fun ClockView(viewModel: ClockViewModel) {
    val time by viewModel.time.collectAsStateWithLifecycle()
    AnalogClock(time = time)
}

@Composable
fun MainScreen(
    state: MainUiState,
    clock: @Composable () -> Unit,
    onSettingsClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onInstallmentsToggle: (Boolean) -> Unit,
    onInstallmentsChange: (Int) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onSignatureToggle: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                ActionButtons(
                    focusManager = focusManager,
                    onSubmit = onSubmit,
                    onCancel = onCancel,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.new_payment_label).uppercase(),
                    style = SectionLabel,
                    color = BrandInkMuted,
                    modifier = Modifier.weight(1f),
                )
                DemoBadge()
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                clock()
            }

            Spacer(modifier = Modifier.height(24.dp))

            AmountField(
                value = state.amount,
                currencySymbol = state.currency.symbol,
                error = state.amountError,
                onValueChange = onAmountChange,
                onDone = { focusManager.clearFocus() },
            )

            if (state.showInstallments) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LabeledSwitch(
                                label = stringResource(R.string.installments_count),
                                checked = state.installmentsOn,
                                onCheckedChange = onInstallmentsToggle,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            InstallmentsPicker(
                                selected = state.installments,
                                enabled = state.installmentsOn,
                                onSelected = onInstallmentsChange,
                                modifier = Modifier.width(120.dp),
                            )
                        }
                        state.installmentAmount?.let { perInstallment ->
                            Text(
                                text = stringResource(
                                    R.string.installment_breakdown,
                                    state.installments,
                                    formatMoney(perInstallment, state.currency),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                    }
                }
            }

            if (state.showCurrency) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CurrencyToggle(
                        selected = state.currency,
                        onSelected = onCurrencyChange,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            if (state.showSignature) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LabeledSwitch(
                        label = stringResource(R.string.signature),
                        checked = state.signatureOn,
                        onCheckedChange = onSignatureToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatMoney(amount: BigDecimal, currency: Currency): String =
    "${currency.symbol}${amount.toPlainString()}"

@Composable
private fun DemoBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 28.dp),
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.error,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = stringResource(R.string.demo_badge).uppercase(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    currencySymbol: String,
    error: AmountError?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.amount)) },
        leadingIcon = {
            Text(
                text = currencySymbol,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Fraunces),
            )
        },
        textStyle = MaterialTheme.typography.displaySmall.copy(
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        ),
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(stringResource(error.messageRes())) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

/** A whole row toggles the switch, which makes a much larger touch target and reads well in TalkBack. */
@Composable
private fun LabeledSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstallmentsPicker(
    selected: Int,
    enabled: Boolean,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val isExpanded = expanded && enabled

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            shape = RoundedCornerShape(14.dp),
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { expanded = false },
        ) {
            PaymentRules.INSTALLMENT_OPTIONS.forEach { count ->
                DropdownMenuItem(
                    text = { Text(count.toString()) },
                    onClick = {
                        onSelected(count)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyToggle(
    selected: Currency,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.currency), modifier = Modifier.weight(1f))
        SingleChoiceSegmentedButtonRow {
            Currency.entries.forEachIndexed { index, currency ->
                SegmentedButton(
                    selected = currency == selected,
                    onClick = { onSelected(currency) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Currency.entries.size),
                    label = { Text(currency.code) },
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    focusManager: FocusManager,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = BrandInk, contentColor = BrandPaper),
        ) {
            Text(stringResource(R.string.submit), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onCancel()
            },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@StringRes
private fun AmountError.messageRes(): Int = when (this) {
    AmountError.REQUIRED -> R.string.error_amount_required
    AmountError.INVALID -> R.string.error_amount_invalid
    AmountError.MUST_BE_POSITIVE -> R.string.error_amount_positive
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    PeleDemoTheme {
        MainScreen(
            state = MainUiState(
                amount = "1240",
                amountError = null,
                showInstallments = true,
                installmentsOn = true,
                installments = 3,
                installmentAmount = BigDecimal("413.33"),
                showCurrency = true,
                currency = Currency.ILS,
                showSignature = true,
                signatureOn = true,
            ),
            clock = { AnalogClock(time = ClockTime(10, 10, 30)) },
            onSettingsClick = {},
            onAmountChange = {},
            onInstallmentsToggle = {},
            onInstallmentsChange = {},
            onCurrencyChange = {},
            onSignatureToggle = {},
            onSubmit = {},
            onCancel = {},
        )
    }
}
