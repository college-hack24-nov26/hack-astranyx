package com.sktech.wastetrack.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.model.VehicleType

@Composable
fun UserRole.localizedDisplayName(): String = stringResource(this.nameRes)

@Composable
fun VehicleType.localizedDisplayName(): String = stringResource(this.nameRes)
