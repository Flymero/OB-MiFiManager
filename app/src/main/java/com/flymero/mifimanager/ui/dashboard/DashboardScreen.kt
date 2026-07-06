package com.flymero.mifimanager.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.OrderItem
import com.flymero.mifimanager.data.model.PlanInfo
import com.flymero.mifimanager.data.model.PlanSimCard
import com.flymero.mifimanager.data.model.StatisticsInfo
import com.flymero.mifimanager.data.model.StatusInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider
import com.flymero.mifimanager.ui.components.StatusChip
import com.flymero.mifimanager.ui.theme.BatteryLow
import com.flymero.mifimanager.ui.theme.BatteryMedium
import com.flymero.mifimanager.ui.theme.SignalBad
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.SignalFair
import com.flymero.mifimanager.ui.theme.SignalGood
import com.flymero.mifimanager.ui.theme.SignalPoor
import com.flymero.mifimanager.ui.theme.SpeedDownload
import com.flymero.mifimanager.ui.theme.SpeedUpload
import com.flymero.mifimanager.ui.theme.Success
import com.flymero.mifimanager.ui.theme.AppColors
import com.flymero.mifimanager.ui.theme.Warning
import com.flymero.mifimanager.ui.theme.mifiDefaultSpatialSpec
import com.flymero.mifimanager.ui.theme.mifiFastEffectsSpec
import com.flymero.mifimanager.ui.util.carrierLogoRes
import com.flymero.mifimanager.ui.util.formatCarrierName
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private data class DashboardDragOverlay(
    val card: DashboardCardType,
    val pointerWindowY: Float,
    val grabOffsetY: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onOpenPlanDetail: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val status = state.statusInfo
    val homepage = state.homepageInfo
    val stats = state.statisticsInfo
    val plan = state.planInfo
    val context = LocalContext.current
    val dashboardScrollState = rememberScrollState()

    var isEditingCards by rememberSaveable { mutableStateOf(false) }
    var showAddCards by rememberSaveable { mutableStateOf(false) }
    var showEditDragHint by rememberSaveable { mutableStateOf(false) }
    var dashboardScrollViewportBounds by remember { mutableStateOf<Rect?>(null) }
    var dashboardDragOverlay by remember { mutableStateOf<DashboardDragOverlay?>(null) }

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val signalQuality = status.signalQuality.toIntOrNull() ?: 0
    val signalText = signalLabel(signalQuality)
    val signalColor = when (signalQuality) {
        5 -> SignalExcellent
        4 -> SignalGood
        3 -> SignalFair
        2 -> SignalPoor
        else -> SignalBad
    }
    val batteryPercent = status.batteryPercent.toIntOrNull() ?: 0
    val batteryColor = when {
        batteryPercent <= 20 -> BatteryLow
        batteryPercent <= 50 -> BatteryMedium
        else -> Success
    }
    val connectionChipText = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> "不可达"
        homepage.connectDisconnect == "cellular" -> "已连接"
        else -> "未连接"
    }
    val connectionChipColor = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> Warning
        homepage.connectDisconnect == "cellular" -> Success
        else -> Warning
    }
    val connectionChipContainer = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> AppColors.warningContainer()
        homepage.connectDisconnect == "cellular" -> AppColors.successContainer()
        else -> AppColors.warningContainer()
    }
    val animatedChipColor by animateColorAsState(connectionChipColor, tween(300), label = "chipColor")
    val animatedChipContainer by animateColorAsState(connectionChipContainer, tween(300), label = "chipContainer")
    val animatedBattery by animateIntAsState(batteryPercent, tween(500), label = "battery")
    val animatedSignal by animateIntAsState(signalQuality, tween(400), label = "signal")
    val carrierName = formatCarrierName(homepage.networkName).ifEmpty { "未知" }
    val carrierLogo = carrierLogoRes(homepage.networkName)
    val dashboardCardContent: @Composable (DashboardCardType) -> Unit = { card ->
        DashboardCardContent(
            card = card,
            editMode = isEditingCards,
            status = status,
            homepage = homepage,
            stats = stats,
            speedSamples = state.speedSamples,
            plan = plan,
            signalText = signalText,
            signalColor = signalColor,
            batteryPercent = batteryPercent,
            batteryColor = batteryColor,
            animatedBattery = animatedBattery,
            animatedSignal = animatedSignal,
            carrierName = carrierName,
            carrierLogo = carrierLogo,
            bandSummary = state.bandSummary,
            localUptimeSeconds = state.localUptimeSeconds,
            cellularConnecting = state.cellularConnecting,
            routerReachable = state.routerReachable,
            lastReachableAtLeastOnce = state.lastReachableAtLeastOnce,
            showPlanHint = state.showPlanHint,
            onOpenPlan = {
                viewModel.markPlanHintSeen()
                onOpenPlanDetail()
            },
            onToggleCellular = viewModel::toggleCellular
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.localToWindow(Offset.Zero)
                    dashboardScrollViewportBounds = Rect(
                        offset = position,
                        size = Size(
                            width = coordinates.size.width.toFloat(),
                            height = coordinates.size.height.toFloat()
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(dashboardScrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = homepage.decodedSsid().ifEmpty { homepage.deviceName.ifEmpty { "MiFi" } },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$carrierName · ${status.networkType()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusChip(
                                text = connectionChipText,
                                color = animatedChipColor,
                                containerColor = animatedChipContainer
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isEditingCards) {
                            IconButton(onClick = { showAddCards = true }) {
                                Icon(Icons.Default.Add, contentDescription = "添加首页卡片")
                            }
                            IconButton(onClick = viewModel::resetDashboardCards) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "恢复默认首页卡片")
                            }
                        }
                        IconButton(onClick = {
                            val enteringEdit = !isEditingCards
                            isEditingCards = enteringEdit
                            showEditDragHint = enteringEdit && viewModel.consumeDashboardEditHint()
                        }) {
                            Icon(
                                imageVector = if (isEditingCards) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = if (isEditingCards) "完成编辑" else "编辑首页卡片"
                            )
                        }
                    }
                }

                DashboardCardsList(
                    cards = state.dashboardCards,
                    editMode = isEditingCards,
                    hasPlan = plan != null,
                    scrollState = dashboardScrollState,
                    scrollViewportBounds = dashboardScrollViewportBounds,
                    showDragHint = showEditDragHint,
                    onDragOverlayChange = { dashboardDragOverlay = it },
                    onReorder = viewModel::saveDashboardCardOrder,
                    onRemove = viewModel::removeDashboardCard,
                    content = dashboardCardContent
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val overlay = dashboardDragOverlay
            val viewportBounds = dashboardScrollViewportBounds
            if (overlay != null && viewportBounds != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                x = 0,
                                y = (overlay.pointerWindowY - overlay.grabOffsetY - viewportBounds.top).roundToInt()
                            )
                        }
                        .zIndex(10f)
                        .graphicsLayer {
                            scaleX = 1.015f
                            scaleY = 1.015f
                            alpha = 0.98f
                        }
                ) {
                    dashboardCardContent(overlay.card)
                }
            }
        }
    }

    if (showAddCards) {
        val missingCards = DashboardCardType.availableCards.filterNot { it in state.dashboardCards }
        ModalBottomSheet(
            onDismissRequest = { showAddCards = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddDashboardCardsSheet(
                missingCards = missingCards,
                onAdd = viewModel::addDashboardCard,
                onClose = { showAddCards = false }
            )
        }
    }
}

@Composable
fun PackageDetailScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val plan = state.planInfo
    var showOrders by rememberSaveable { mutableStateOf(false) }
    var displayCount by rememberSaveable { mutableStateOf(10) }
    val planDetailListState = rememberLazyListState()
    val orderListState = rememberLazyListState()
    val spatialSpec = mifiDefaultSpatialSpec<IntOffset>()
    val effectsSpec = mifiFastEffectsSpec<Float>()

    BackHandler(enabled = showOrders) {
        showOrders = false
        displayCount = 10
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = showOrders,
            label = "planDetailContent",
            transitionSpec = {
                val direction = if (targetState) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                (slideIntoContainer(direction, animationSpec = spatialSpec) + fadeIn(effectsSpec)) togetherWith
                    (slideOutOfContainer(direction, animationSpec = spatialSpec) + fadeOut(effectsSpec))
            }
        ) { ordersVisible ->
            if (ordersVisible) {
                OrderHistorySheet(
                    orders = state.orderList,
                    isLoading = state.isOrderLoading,
                    error = state.orderError,
                    listState = orderListState,
                    displayCount = displayCount,
                    onLoadMore = { displayCount += 10 },
                    onClose = {
                        showOrders = false
                        displayCount = 10
                    }
                )
            } else if (plan != null) {
                PackageDetailSheet(
                    plan = plan,
                    context = context,
                    isRefreshing = state.isPlanRefreshing,
                    listState = planDetailListState,
                    onRefresh = viewModel::refreshPlanManually,
                    onClose = onBack,
                    onShowOrders = {
                        viewModel.fetchOrders()
                        showOrders = true
                    }
                )
            } else {
                MissingPlanDetail(onBack = onBack)
            }
        }
    }
}

@Composable
private fun MissingPlanDetail(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailHeader(title = "套餐详情", onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无套餐数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardCardsList(
    cards: List<DashboardCardType>,
    editMode: Boolean,
    hasPlan: Boolean,
    scrollState: ScrollState,
    scrollViewportBounds: Rect?,
    showDragHint: Boolean,
    onDragOverlayChange: (DashboardDragOverlay?) -> Unit,
    onReorder: (List<DashboardCardType>) -> Unit,
    onRemove: (DashboardCardType) -> Unit,
    content: @Composable (DashboardCardType) -> Unit
) {
    val cardSpacing = 12.dp
    val autoScrollEdge = 120.dp
    val autoScrollMaxStep = 32.dp
    var previewOrder by remember { mutableStateOf(cards) }
    var lastDragOrder by remember { mutableStateOf(cards) }
    var draggedCard by remember { mutableStateOf<DashboardCardType?>(null) }
    var dragCenterY by remember { mutableStateOf<Float?>(null) }
    var dragPointerY by remember { mutableStateOf<Float?>(null) }
    var dragGrabOffsetY by remember { mutableStateOf(0f) }
    var draggedBounds by remember { mutableStateOf<Rect?>(null) }
    var dragBaseOrder by remember { mutableStateOf<List<DashboardCardType>>(emptyList()) }
    var dragBaseBounds by remember { mutableStateOf<Map<DashboardCardType, Rect>>(emptyMap()) }

    LaunchedEffect(cards, editMode) {
        if (!editMode || draggedCard == null) {
            previewOrder = cards
            lastDragOrder = cards
            onDragOverlayChange(null)
        }
    }

    val displayCards = if (editMode) {
        cards
    } else {
        cards.filter { it != DashboardCardType.PlanUsage || hasPlan }
    }
    val latestDisplayCards by rememberUpdatedState(displayCards)
    val latestPreviewOrder by rememberUpdatedState(previewOrder)
    val latestDragPointerY by rememberUpdatedState(dragPointerY)
    val latestScrollViewportBounds by rememberUpdatedState(scrollViewportBounds)
    val itemBounds = remember { mutableStateMapOf<DashboardCardType, Rect>() }
    val density = LocalDensity.current
    val spacingPx = with(density) { cardSpacing.toPx() }
    val autoScrollEdgePx = with(density) { autoScrollEdge.toPx() }
    val autoScrollMaxStepPx = with(density) { autoScrollMaxStep.toPx() }
    val editWobbleTransition = rememberInfiniteTransition(label = "dashboard-edit-wobble")
    val editWobbleRotation by editWobbleTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 460
                -0.35f at 0
                0.35f at 230
                -0.35f at 460
            }
        ),
        label = "dashboard-edit-card-rotation"
    )
    val activeBounds = if (draggedCard != null && dragBaseBounds.isNotEmpty()) {
        dragBaseBounds
    } else {
        itemBounds
    }
    val activeOrder = if (draggedCard != null && dragBaseOrder.isNotEmpty()) {
        dragBaseOrder
    } else {
        displayCards
    }
    val projectedTops = remember(previewOrder, activeOrder, activeBounds, spacingPx, draggedCard) {
        if (draggedCard == null) {
            emptyMap()
        } else {
            calculateDashboardSlotTops(previewOrder, activeOrder, activeBounds, spacingPx)
        }
    }
    fun updatePreviewOrderForDrag(card: DashboardCardType, pointerWindowY: Float) {
        val viewportBounds = latestScrollViewportBounds ?: return
        val draggedHeight = draggedBounds?.height ?: dragBaseBounds[card]?.height ?: return
        val centerContentY = pointerWindowY -
            viewportBounds.top +
            scrollState.value +
            (draggedHeight / 2f - dragGrabOffsetY)
        val baseOrder = dragBaseOrder.ifEmpty { latestDisplayCards }
        val baseBounds = dragBaseBounds.ifEmpty { itemBounds.toMap() }
        val nextOrder = calculateDashboardDragOrder(
            baseOrder = baseOrder,
            bounds = baseBounds,
            draggedCard = card,
            dragCenterY = centerContentY,
            spacingPx = spacingPx
        )
        dragCenterY = centerContentY
        if (nextOrder != latestPreviewOrder) {
            previewOrder = nextOrder
            lastDragOrder = nextOrder
        }
    }

    LaunchedEffect(draggedCard, editMode) {
        while (editMode && draggedCard != null) {
            val pointerY = latestDragPointerY
            val viewportBounds = latestScrollViewportBounds
            val scrollDelta = if (pointerY != null && viewportBounds != null) {
                calculateDashboardAutoScrollDelta(
                    pointerY = pointerY,
                    viewportBounds = viewportBounds,
                    edgePx = autoScrollEdgePx,
                    maxStepPx = autoScrollMaxStepPx
                )
            } else {
                0f
            }
            val consumedScroll = if (scrollDelta != 0f) {
                scrollState.dispatchRawDelta(scrollDelta)
            } else {
                0f
            }
            if (consumedScroll != 0f) {
                val card = draggedCard
                if (card != null && pointerY != null) {
                    updatePreviewOrderForDrag(card, pointerY)
                }
            }
            withFrameMillis { }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(cardSpacing)) {
            displayCards.forEachIndexed { index, card ->
                key(card) {
                    val isDragging = draggedCard == card
                    val editRotation = if (editMode && !isDragging && draggedCard == null) {
                        editWobbleRotation * if (index % 2 == 0) 1f else -1f
                    } else {
                        0f
                    }
                    val targetTranslationY = if (draggedCard != null) {
                        val top = projectedTops[card]
                        val bounds = activeBounds[card]
                        if (top != null && bounds != null) top - bounds.top else 0f
                    } else {
                        0f
                    }
                    val animatedTranslationY by animateFloatAsState(
                        targetValue = targetTranslationY,
                        animationSpec = tween(durationMillis = 150),
                        label = "dashboard-card-translation"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = animatedTranslationY
                                rotationZ = editRotation
                                if (isDragging) alpha = 0.22f
                            }
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.localToWindow(Offset.Zero)
                                itemBounds[card] = Rect(
                                    offset = position,
                                    size = Size(
                                        width = coordinates.size.width.toFloat(),
                                        height = coordinates.size.height.toFloat()
                                    )
                                )
                            }
                            .then(
                                if (editMode) {
                                    Modifier.pointerInput(card, editMode) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { localOffset ->
                                                val viewportBounds = latestScrollViewportBounds
                                                val currentWindowBounds = latestDisplayCards.mapNotNull { item ->
                                                    itemBounds[item]?.let { item to it }
                                                }.toMap()
                                                val bounds = currentWindowBounds[card] ?: itemBounds[card]
                                                val contentBounds = if (viewportBounds != null) {
                                                    currentWindowBounds.mapValues { (_, rect) ->
                                                        rect.toDashboardContentRect(
                                                            viewportTop = viewportBounds.top,
                                                            scrollValue = scrollState.value
                                                        )
                                                    }
                                                } else {
                                                    currentWindowBounds
                                                }
                                                dragBaseOrder = latestDisplayCards
                                                dragBaseBounds = contentBounds
                                                previewOrder = latestDisplayCards
                                                lastDragOrder = latestDisplayCards
                                                draggedCard = card
                                                draggedBounds = bounds
                                                dragGrabOffsetY = localOffset.y
                                                dragPointerY = bounds?.let { it.top + localOffset.y }
                                                dragPointerY?.let { pointerY ->
                                                    onDragOverlayChange(
                                                        DashboardDragOverlay(
                                                            card = card,
                                                            pointerWindowY = pointerY,
                                                            grabOffsetY = localOffset.y
                                                        )
                                                    )
                                                    updatePreviewOrderForDrag(card, pointerY)
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val currentPointerY = dragPointerY ?: itemBounds[card]?.center?.y
                                                    ?: return@detectDragGesturesAfterLongPress
                                                val nextPointerY = currentPointerY + dragAmount.y
                                                dragPointerY = nextPointerY
                                                onDragOverlayChange(
                                                    DashboardDragOverlay(
                                                        card = card,
                                                        pointerWindowY = nextPointerY,
                                                        grabOffsetY = dragGrabOffsetY
                                                    )
                                                )
                                                updatePreviewOrderForDrag(card, nextPointerY)
                                            },
                                            onDragEnd = {
                                                val finalOrder = lastDragOrder
                                                previewOrder = finalOrder
                                                onReorder(finalOrder)
                                                onDragOverlayChange(null)
                                                draggedCard = null
                                                dragCenterY = null
                                                dragPointerY = null
                                                draggedBounds = null
                                                dragBaseOrder = emptyList()
                                                dragBaseBounds = emptyMap()
                                            },
                                            onDragCancel = {
                                                previewOrder = cards
                                                lastDragOrder = cards
                                                onDragOverlayChange(null)
                                                draggedCard = null
                                                dragCenterY = null
                                                dragPointerY = null
                                                draggedBounds = null
                                                dragBaseOrder = emptyList()
                                                dragBaseBounds = emptyMap()
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        content(card)
                        if (editMode) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showDragHint && index == 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.DragHandle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "长按拖动",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { onRemove(card) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "隐藏${card.title}",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Rect.toDashboardContentRect(viewportTop: Float, scrollValue: Int): Rect {
    return Rect(
        offset = Offset(left, top - viewportTop + scrollValue),
        size = size
    )
}

private fun calculateDashboardAutoScrollDelta(
    pointerY: Float,
    viewportBounds: Rect,
    edgePx: Float,
    maxStepPx: Float
): Float {
    val topEdge = viewportBounds.top + edgePx
    val bottomEdge = viewportBounds.bottom - edgePx
    return when {
        pointerY < topEdge -> {
            val intensity = ((topEdge - pointerY) / edgePx).coerceIn(0f, 1f)
            -maxStepPx * intensity
        }
        pointerY > bottomEdge -> {
            val intensity = ((pointerY - bottomEdge) / edgePx).coerceIn(0f, 1f)
            maxStepPx * intensity
        }
        else -> 0f
    }
}

private fun calculateDashboardDragOrder(
    baseOrder: List<DashboardCardType>,
    bounds: Map<DashboardCardType, Rect>,
    draggedCard: DashboardCardType,
    dragCenterY: Float,
    spacingPx: Float
): List<DashboardCardType> {
    if (draggedCard !in baseOrder) return baseOrder
    val remainingCards = baseOrder.filterNot { it == draggedCard }
    if (remainingCards.isEmpty()) return listOf(draggedCard)

    val firstTop = baseOrder.mapNotNull { bounds[it]?.top }.minOrNull() ?: return baseOrder
    var projectedTop = firstTop
    var insertIndex = remainingCards.size

    for ((index, card) in remainingCards.withIndex()) {
        val height = bounds[card]?.height ?: bounds[draggedCard]?.height ?: 0f
        val centerY = projectedTop + height / 2f
        if (dragCenterY < centerY) {
            insertIndex = index
            break
        }
        projectedTop += height + spacingPx
    }

    return remainingCards.toMutableList().apply {
        add(insertIndex.coerceIn(0, size), draggedCard)
    }
}

private fun calculateDashboardSlotTops(
    order: List<DashboardCardType>,
    baseOrder: List<DashboardCardType>,
    bounds: Map<DashboardCardType, Rect>,
    spacingPx: Float
): Map<DashboardCardType, Float> {
    if (order.isEmpty()) return emptyMap()
    val firstTop = baseOrder.mapNotNull { bounds[it]?.top }.minOrNull() ?: return emptyMap()
    var nextTop = firstTop
    return buildMap {
        order.forEach { card ->
            put(card, nextTop)
            nextTop += (bounds[card]?.height ?: 0f) + spacingPx
        }
    }
}

@Composable
private fun DashboardCardContent(
    card: DashboardCardType,
    editMode: Boolean,
    status: StatusInfo,
    homepage: HomepageInfo,
    stats: StatisticsInfo,
    speedSamples: List<SpeedSample>,
    plan: PlanInfo?,
    signalText: String,
    signalColor: Color,
    batteryPercent: Int,
    batteryColor: Color,
    animatedBattery: Int,
    animatedSignal: Int,
    carrierName: String,
    carrierLogo: Int?,
    bandSummary: String,
    localUptimeSeconds: Long,
    cellularConnecting: Boolean,
    routerReachable: Boolean,
    lastReachableAtLeastOnce: Boolean,
    showPlanHint: Boolean,
    onOpenPlan: () -> Unit,
    onToggleCellular: (Boolean) -> Unit
) {
    when (card) {
        DashboardCardType.DeviceStatus -> DeviceStatusDashboardCard(
            status = status,
            signalText = signalText,
            signalColor = signalColor,
            batteryPercent = batteryPercent,
            batteryColor = batteryColor,
            animatedBattery = animatedBattery,
            animatedSignal = animatedSignal,
            carrierName = carrierName,
            carrierLogo = carrierLogo
        )
        DashboardCardType.NetworkSpeed -> NetworkSpeedDashboardCard(
            status = status,
            speedSamples = speedSamples
        )
        DashboardCardType.PlanUsage -> PlanUsageDashboardCard(
            plan = plan,
            editMode = editMode,
            showPlanHint = showPlanHint,
            onOpenPlan = onOpenPlan
        )
        DashboardCardType.TrafficStats -> TrafficStatsDashboardCard(stats = stats)
        DashboardCardType.NetworkConnection -> NetworkConnectionDashboardCard(
            editMode = editMode,
            homepage = homepage,
            bandSummary = bandSummary,
            localUptimeSeconds = localUptimeSeconds,
            cellularConnecting = cellularConnecting,
            routerReachable = routerReachable,
            lastReachableAtLeastOnce = lastReachableAtLeastOnce,
            onToggleCellular = onToggleCellular
        )
    }
}

@Composable
private fun DeviceStatusDashboardCard(
    status: StatusInfo,
    signalText: String,
    signalColor: Color,
    batteryPercent: Int,
    batteryColor: Color,
    animatedBattery: Int,
    animatedSignal: Int,
    carrierName: String,
    carrierLogo: Int?
) {
    SectionCard {
        CardTitle("设备状态")
        val batteryIcon = when {
            status.batteryCharging == "1" -> Icons.Default.BatteryChargingFull
            batteryPercent <= 20 -> Icons.Default.BatteryAlert
            else -> Icons.Default.BatteryFull
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard(
                    title = "信号强度",
                    primary = signalText,
                    secondary = "${status.rssi} dBm",
                    footnote = "$animatedSignal 格",
                    icon = Icons.Default.SignalCellularAlt,
                    accentColor = signalColor,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "电量",
                    primary = "$animatedBattery%",
                    secondary = if (status.batteryCharging == "1") "充电中" else if (batteryPercent <= 20) "低电量" else "状态正常",
                    icon = batteryIcon,
                    accentColor = batteryColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard(
                    title = "运营商",
                    primary = carrierName,
                    icon = if (carrierLogo == null) Icons.Default.Language else null,
                    iconContent = carrierLogo?.let { logo ->
                        {
                            Image(
                                painter = painterResource(logo),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "在线设备",
                    primary = "${status.wifiClientsNum} 台",
                    icon = Icons.Default.Devices,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard(
                    title = "下载速率",
                    primary = status.formattedSpeed(status.rxSpeed),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = SpeedDownload,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "上传速率",
                    primary = status.formattedSpeed(status.txSpeed),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = SpeedUpload,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NetworkSpeedDashboardCard(
    status: StatusInfo,
    speedSamples: List<SpeedSample>
) {
    val currentTotalSpeed = (status.rxSpeed.toLongOrNull() ?: 0L) + (status.txSpeed.toLongOrNull() ?: 0L)
    SectionCard {
        CardTitle(
            title = "网络速度",
            trailing = {
                Text(
                    text = "总 ${formatSpeedValue(currentTotalSpeed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        SpeedLineChart(
            samples = speedSamples,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardStatCard(
                title = "下载",
                value = status.formattedSpeed(status.rxSpeed),
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "上传",
                value = status.formattedSpeed(status.txSpeed),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpeedLineChart(
    samples: List<SpeedSample>,
    modifier: Modifier = Modifier
) {
    val downloadColor = SpeedDownload
    val uploadColor = SpeedUpload
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val displaySamples = remember(samples) {
        when {
            samples.isEmpty() -> listOf(
                SpeedSample(0L, 0L, 0L),
                SpeedSample(1L, 0L, 0L)
            )
            samples.size == 1 -> listOf(
                samples.first(),
                samples.first()
            )
            else -> samples
        }
    }

    Canvas(modifier = modifier) {
        val maxSpeed = max(
            1L,
            displaySamples.maxOf { max(it.rxBytesPerSec, it.txBytesPerSec) }
        ).toFloat()
        val topPadding = 8.dp.toPx()
        val bottomPadding = 14.dp.toPx()
        val chartHeight = size.height - topPadding - bottomPadding
        val pointCount = displaySamples.lastIndex.coerceAtLeast(1)

        repeat(3) { index ->
            val y = topPadding + chartHeight * index / 2f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        fun pointFor(index: Int, value: Long): Offset {
            val x = size.width * index / pointCount
            val ratio = (value / maxSpeed).coerceIn(0f, 1f)
            val y = topPadding + chartHeight * (1f - ratio)
            return Offset(x, y)
        }

        val downloadPoints = displaySamples.mapIndexed { index, sample ->
            pointFor(index, sample.rxBytesPerSec)
        }
        val uploadPoints = displaySamples.mapIndexed { index, sample ->
            pointFor(index, sample.txBytesPerSec)
        }
        val downloadPath = buildSmoothPath(downloadPoints)
        val uploadPath = buildSmoothPath(uploadPoints)

        if (downloadPoints.isNotEmpty()) {
            val fillPath = Path().apply {
                addPath(downloadPath)
                lineTo(downloadPoints.last().x, size.height)
                lineTo(downloadPoints.first().x, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        downloadColor.copy(alpha = 0.18f),
                        downloadColor.copy(alpha = 0.02f)
                    )
                )
            )
        }

        drawPath(
            path = downloadPath,
            color = downloadColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = uploadPath,
            color = uploadColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawLine(
            color = baselineColor,
            start = Offset(0f, size.height - bottomPadding),
            end = Offset(size.width, size.height - bottomPadding),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return path

    for (index in 1 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val midX = (current.x + next.x) / 2f
        val midY = (current.y + next.y) / 2f
        path.quadraticTo(current.x, current.y, midX, midY)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

private fun formatSpeedValue(bytesPerSec: Long): String {
    return when {
        bytesPerSec >= 1048576L -> "%.1f MB/s".format(bytesPerSec / 1048576.0)
        bytesPerSec >= 1024L -> "%.1f KB/s".format(bytesPerSec / 1024.0)
        else -> "$bytesPerSec B/s"
    }
}

@Composable
private fun PlanUsageDashboardCard(
    plan: PlanInfo?,
    editMode: Boolean,
    showPlanHint: Boolean,
    onOpenPlan: () -> Unit
) {
    if (plan == null) {
        if (editMode) {
            SectionCard {
                CardTitle("套餐信息")
                Text(
                    text = "暂无套餐数据。登录页填写充值号后，这张卡片会显示剩余流量、到期时间和订单入口。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    SectionCard(onClick = if (editMode) null else onOpenPlan) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = plan.packageName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "到期时间：${plan.expiretime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!editMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "查看详情",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = { plan.usagePercent() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已用 ${plan.usedFormatted()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "剩余 ${plan.remainFormatted()} / ${plan.totalFormatted()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showPlanHint && !editMode) {
            Text(
                text = "点击套餐卡查看详细用量",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TrafficStatsDashboardCard(stats: StatisticsInfo) {
    SectionCard {
        CardTitle("用量统计")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    title = "本次下载",
                    value = stats.formattedCurrentTraffic().first,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = "本次上传",
                    value = stats.formattedCurrentTraffic().second,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    title = "累计下载",
                    value = stats.formattedTotalTraffic().first,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = "累计上传",
                    value = stats.formattedTotalTraffic().second,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NetworkConnectionDashboardCard(
    editMode: Boolean,
    homepage: HomepageInfo,
    bandSummary: String,
    localUptimeSeconds: Long,
    cellularConnecting: Boolean,
    routerReachable: Boolean,
    lastReachableAtLeastOnce: Boolean,
    onToggleCellular: (Boolean) -> Unit
) {
    SectionCard {
        CardTitle("网络连接")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "蜂窝网络",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when {
                        cellularConnecting -> "切换中..."
                        !routerReachable && lastReachableAtLeastOnce -> "路由器不可达"
                        homepage.connectDisconnect == "cellular" -> "已连接"
                        else -> "已断开"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = routerReachable && homepage.connectDisconnect == "cellular",
                onCheckedChange = onToggleCellular,
                enabled = !editMode && !cellularConnecting && routerReachable
            )
        }
        SectionDivider()
        KeyValueRow(label = "频段", value = bandSummary)
        SectionDivider()
        KeyValueRow(label = "WAN IP", value = homepage.wanIp.ifEmpty { "--" })
        SectionDivider()
        KeyValueRow(label = "LAN IP", value = homepage.lanIp.ifEmpty { "--" })
        SectionDivider()
        KeyValueRow(label = "运行时间", value = formatUptime(localUptimeSeconds), valueColor = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddDashboardCardsSheet(
    missingCards: List<DashboardCardType>,
    onAdd: (DashboardCardType) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "添加首页卡片",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (missingCards.isEmpty()) {
            Text(
                text = "所有卡片都已经在首页中。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            missingCards.forEach { card ->
                OutlinedButton(
                    onClick = { onAdd(card) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(card.title)
                }
            }
        }
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("完成")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    primary: String,
    modifier: Modifier = Modifier,
    secondary: String? = null,
    footnote: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconContent: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.height(122.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null || iconContent != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconContent != null) {
                            iconContent()
                        } else if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                secondary?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!footnote.isNullOrBlank()) {
                Text(
                    text = footnote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回"
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        action?.invoke()
    }
}

@Composable
private fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.97f else 1f,
        animationSpec = mifiFastEffectsSpec(),
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun CopyableKeyValueRow(
    label: String,
    value: String,
    clipLabel: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.34f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.weight(0.66f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(clipLabel, value))
                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SimStatusRow(sim: PlanSimCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatCarrierName(sim.operatorText).ifBlank { "SIM 卡" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (sim.isInUse()) {
                Text(
                    text = "当前",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = sim.realnameStatusText.ifBlank { "--" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun PackageDetailSheet(
    plan: PlanInfo,
    context: Context,
    isRefreshing: Boolean,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
    onShowOrders: () -> Unit
) {
    val daysLeft = plan.daysUntilExpire()
    val dailyBudget = plan.dailyBudget()
    val dailyAverage = plan.dailyAverageUsage()
    val planRows = buildList {
        plan.balance.takeIf { it.isNotBlank() }?.let { add("账户余额" to "¥$it") }
        plan.operator.takeIf { it.isNotBlank() }?.let { add("运营商" to formatCarrierName(it)) }
        plan.realnameStatus.takeIf { it.isNotBlank() }?.let { add("实名状态" to it) }
        plan.paymentTypeText.takeIf { it.isNotBlank() }?.let { add("支付方式" to it) }
    }
    val equipment = plan.equipment
    val equipmentRows = equipment?.let {
        buildList {
            add("设备状态" to if (it.deviceStatus == 1) "在线" else "离线")
            it.hotspotName.takeIf { name -> name.isNotBlank() }?.let { name -> add("热点名称" to name) }
        }
    }.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            DetailHeader(title = "套餐详情", onBack = onClose) {
                val refreshInteraction = remember { MutableInteractionSource() }
                TextButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.pressScale(refreshInteraction, enabled = !isRefreshing),
                    interactionSource = refreshInteraction
                ) {
                    Text(if (isRefreshing) "刷新中..." else "刷新")
                }
            }
        }

        item(key = "package-title") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = plan.packageName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "到期时间：${plan.expiretime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "usage-summary") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${"%.2f".format(plan.usagePercent())}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "已使用",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "剩余 ${plan.remainFormatted()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "总流量 ${plan.totalFormatted()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { plan.usagePercent() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "已用 ${plan.usedFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "剩余 ${plan.remainFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "usage-details") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    KeyValueRow(label = "总流量", value = plan.totalFormatted())
                    SectionDivider()
                    KeyValueRow(label = "已使用", value = plan.usedFormatted())
                    SectionDivider()
                    KeyValueRow(label = "剩余流量", value = plan.remainFormatted())
                    SectionDivider()
                    KeyValueRow(label = "日均使用", value = dailyAverage ?: "--")
                    daysLeft?.let {
                        SectionDivider()
                        KeyValueRow(label = "距离到期", value = "$it 天")
                    }
                    dailyBudget?.let {
                        SectionDivider()
                        KeyValueRow(label = "预计每日可用", value = it)
                    }
                    SectionDivider()
                    KeyValueRow(label = "使用进度", value = "${"%.2f".format(plan.usagePercent())}%")
                    SectionDivider()
                    KeyValueRow(label = "到期时间", value = plan.expiretime)
                    equipment?.takeIf { it.devNo.isNotBlank() }?.let {
                        SectionDivider()
                        CopyableKeyValueRow(
                            label = "充值号",
                            value = it.devNo,
                            clipLabel = "recharge_no",
                            context = context
                        )
                    }
                }
            }
        }

        if (planRows.isNotEmpty()) {
            item(key = "plan-info") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CardTitle("套餐信息")
                        planRows.forEachIndexed { index, (label, value) ->
                            if (index > 0) SectionDivider()
                            KeyValueRow(label = label, value = value)
                        }
                    }
                }
            }
        }

        if (equipment != null) {
            item(key = "equipment-info") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CardTitle("设备与 SIM")
                        equipmentRows.forEachIndexed { index, (label, value) ->
                            if (index > 0) SectionDivider()
                            KeyValueRow(label = label, value = value)
                        }
                        equipment.hotspotPassword.takeIf { it.isNotBlank() }?.let { password ->
                            if (equipmentRows.isNotEmpty()) SectionDivider()
                            CopyableKeyValueRow(
                                label = "热点密码",
                                value = password,
                                clipLabel = "hotspot_password",
                                context = context
                            )
                        }
                        equipment.cardList.forEach { sim ->
                            SectionDivider()
                            SimStatusRow(sim = sim)
                        }
                    }
                }
            }
        }

        item(key = "actions") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val ordersInteraction = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onShowOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(ordersInteraction),
                    shape = MaterialTheme.shapes.large,
                    interactionSource = ordersInteraction
                ) {
                    Text("历史订单")
                }
                val closeInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(closeInteraction),
                    shape = MaterialTheme.shapes.large,
                    interactionSource = closeInteraction
                ) {
                    Text("知道了")
                }
            }
        }
    }
}

private fun signalLabel(level: Int): String = when (level) {
    5 -> "优秀"
    4 -> "良好"
    3 -> "一般"
    2 -> "较弱"
    else -> "较差"
}
private fun PlanInfo.daysUntilExpire(): Int? {
    val parts = expiretime.split("-")
    if (parts.size != 3) return null
    return runCatching {
        val target = java.time.LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        val now = java.time.LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, target).toInt()
        days.coerceAtLeast(0)
    }.getOrNull()
}

private fun PlanInfo.dailyBudget(): String? {
    val days = daysUntilExpire() ?: return null
    if (days == 0) return remainFormatted()
    val remainPerDayMb = remainAmountSafe() / days
    return when {
        remainPerDayMb >= 1024 -> "%.2f GB/天".format(remainPerDayMb / 1024)
        else -> "%.0f MB/天".format(ceil(remainPerDayMb))
    }
}

private fun PlanInfo.dailyAverageUsage(): String? {
    val daysLeft = daysUntilExpire() ?: return null
    val totalDays = packageDurationDays() ?: return null
    val usedDays = (totalDays - daysLeft).coerceAtLeast(1)
    return formatDailyMb(usedAmount() / usedDays)
}

private fun PlanInfo.packageDurationDays(): Int? {
    val name = packageName
    Regex("""(\d+)\s*(天|日)""").find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
        return it
    }
    Regex("""(\d+)\s*(个月|月)""").find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
        return it * 30
    }
    Regex("""(\d+)\s*年""").find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
        return it * 365
    }
    return when {
        name.contains("月") -> 30
        name.contains("年") -> 365
        else -> null
    }
}

private fun formatDailyMb(mb: Double): String = when {
    mb >= 1024 -> "%.2f GB/天".format(mb / 1024)
    else -> "%.0f MB/天".format(ceil(mb))
}

private fun formatUptime(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return buildString {
        if (days > 0) append("${days}天 ")
        if (hours > 0) append("${hours}时 ")
        append("${minutes}分 ${secs}秒")
    }
}

@Composable
private fun OrderHistorySheet(
    orders: List<OrderItem>,
    isLoading: Boolean,
    error: String?,
    listState: LazyListState,
    displayCount: Int,
    onLoadMore: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailHeader(title = "历史订单", onBack = onClose)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                orders.isEmpty() -> Text(
                    text = "暂无订单记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    val visibleOrders = orders.take(displayCount)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleOrders, key = { it.orderNo }) { order ->
                            OrderItemCard(order)
                        }
                        if (displayCount < orders.size) {
                            item {
                                val loadMoreInteraction = remember { MutableInteractionSource() }
                                TextButton(
                                    onClick = onLoadMore,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pressScale(loadMoreInteraction),
                                    interactionSource = loadMoreInteraction
                                ) {
                                    Text("查看更多（剩余 ${orders.size - displayCount} 条）")
                                }
                            }
                        }
                    }
                }
            }
        }

        val closeInteraction = remember { MutableInteractionSource() }
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(closeInteraction),
            shape = MaterialTheme.shapes.large,
            interactionSource = closeInteraction
        ) {
            Text("返回套餐详情")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OrderItemCard(order: OrderItem) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = order.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = order.displayTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "¥${order.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = order.statusText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
