package com.example.pelecarddemo.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.pelecarddemo.ui.theme.PeleDemoTheme
import com.example.pelecarddemo.ui.transaction.MainUiState
import com.example.pelecarddemo.ui.transaction.TransactionViewModel

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

    Scaffold(modifier = modifier) { innerPadding ->
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            }

            clock()

            Spacer(modifier = Modifier.height(32.dp))

            AmountField(
                value = state.amount,
                error = state.amountError,
                onValueChange = onAmountChange,
                onDone = { focusManager.clearFocus() },
            )

            if (state.showInstallments) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabeledSwitch(
                        label = stringResource(R.string.installments),
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
            }

            if (state.showCurrency) {
                Spacer(modifier = Modifier.height(12.dp))
                CurrencyToggle(
                    selected = state.currency,
                    onSelected = onCurrencyChange,
                )
            }

            if (state.showSignature) {
                Spacer(modifier = Modifier.height(12.dp))
                LabeledSwitch(
                    label = stringResource(R.string.signature),
                    checked = state.signatureOn,
                    onCheckedChange = onSignatureToggle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ActionButtons(
                focusManager = focusManager,
                onSubmit = onSubmit,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    error: AmountError?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.amount)) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(stringResource(error.messageRes())) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
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
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
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
            label = { Text(stringResource(R.string.installments_count)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
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
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.submit))
        }
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onCancel()
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.cancel))
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
                amount = "200",
                amountError = null,
                showInstallments = true,
                installmentsOn = true,
                installments = 12,
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
