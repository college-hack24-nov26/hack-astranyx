package com.sktech.wastetrack.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sktech.wastetrack.domain.model.UserRole

@Composable
fun UserRole.localizedDisplayName(): String = stringResource(this.nameRes)
