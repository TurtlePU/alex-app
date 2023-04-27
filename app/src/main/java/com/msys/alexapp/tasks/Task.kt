package com.msys.alexapp.tasks

import kotlinx.coroutines.flow.Flow

data class Task(val progressFlow: Flow<Float>, val start: () -> Unit)