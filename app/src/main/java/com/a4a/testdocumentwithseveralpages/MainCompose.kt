package com.a4a.testdocumentwithseveralpages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a4a.testdocumentwithseveralpages.ui.theme.TestDocumentWithSeveralPagesTheme


@Composable
fun MainCompose() {
    TestDocumentWithSeveralPagesTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val myViewModel: MyViewModel = hiltViewModel()
            var products = myViewModel.uiState
                .collectAsStateWithLifecycle()
            var numberOfPages = products.value.products.last().page

            println("xxxx = " + myViewModel.uiState)
            MainScreen(
                numberOfPages = numberOfPages,
                products.value.products,
                onIncrementPages = {
                    numberOfPages++
                },
                onAddValue = {
                    myViewModel.addProduct(
                        DocumentProductState(
                            id = null,
                            name = "another",
                            page = numberOfPages
                        )
                    )
                },
                updateItemPage = { myViewModel.updateLastItemPage() }
            )
        }
    }
}

@Composable
fun MainScreen(
    numberOfPages: Int,
    uiState: List<DocumentProductState>,
    onAddValue: () -> Unit,
    onIncrementPages: () -> Unit,
    updateItemPage: () -> Unit,
) {
    println("uiState = " + uiState)

    Column {
        Pager(
            numberOfPages = numberOfPages,
            incrementPages = onIncrementPages,
            products = uiState,
            updatePageNumberInDatabase = updateItemPage
        )
        Spacer(Modifier.padding(42.dp))

        Button(onClick = onAddValue) {
            Text("Add a product to list")
        }
    }
}

fun calculateNumberOfPages(
    pageContentHeight: Dp,
    availableSpace: Dp,
): Int {
    println("totalContentHeight" + pageContentHeight)
    println("availableSpace" + availableSpace)

    val quotient = (pageContentHeight.value / availableSpace.value).toInt()
    println("quotient" + quotient)

    val remainder = (pageContentHeight.value % availableSpace.value)
    println("remainder" + remainder)

    return if (remainder == 0F) quotient else quotient + 1
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pager(
    numberOfPages: Int,
    incrementPages: () -> Unit,
    products: List<DocumentProductState>,
    updatePageNumberInDatabase: () -> Unit,
) {
    val pagerState = rememberPagerState { numberOfPages }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var pageContentHeight = remember { 0.dp }

    Column {
        HorizontalPager(
            state = pagerState
        ) { pagerIndex ->
            println("index = " + pagerIndex)

            Column {
                Text("page n°" + (pagerIndex + 1) + "/" + numberOfPages)

                TemplateContent(
                    screenWidth = screenWidth,
                    productArray = products.filter { it.page == (pagerIndex + 1) },
                    //footerArray = ,
                    index = pagerIndex,
                    numberOfPages = numberOfPages,
                    onPageOverflow = {
                        println("!!!!!!!!!!!!!!!!onpage overfloww")
                        incrementPages()
                        println("numberOfPages" + numberOfPages)
                        products.last().page += 1
                        println("!!!! lastproduct page = " + products.last())
                        updatePageNumberInDatabase()

                    }
                )
            }
        }
    }
}

@Composable
fun TemplateContent(
    screenWidth: Dp,
    productArray: List<DocumentProductState>?,
    // footerArray: List<FooterRow>,
    index: Int,
    numberOfPages: Int,
    onPageOverflow: () -> Unit,

    ) {
    println("_______IN CONTENT-- = " + index)
    println("index = " + index)
    println("productArray = " + productArray?.toList())

    val pagePadding = 20.dp
    var pageHeight by remember {
        mutableStateOf(0.dp)
    }
    var pageContentHeight by remember {
        mutableStateOf(0.dp)
    }
    var pageHeaderHeight by remember {
        mutableStateOf(0.dp)
    }
    var productRowHeight by remember {
        mutableStateOf(0.dp)
    }

    val localDensity = LocalDensity.current

    Box(
        modifier = Modifier
            .background(Color.Red)
            .width(screenWidth)
            .padding(
                start = pagePadding,
                top = pagePadding,
                bottom = pagePadding,
                end = pagePadding
            )
            .background(Color.White)
            .aspectRatio(1f / 1f)
            .onGloballyPositioned { coordinates ->
                pageHeight = with(localDensity) { coordinates.size.height.toDp() }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp,
                    end = 20.dp
                )

                .onGloballyPositioned { coordinates ->
                    pageContentHeight = with(localDensity) { coordinates.size.height.toDp() }
                    println("pageContentHeight = " + pageContentHeight)
                }
        ) {
            val paddingTopAndBottom = 42.dp
            val availableSpace = pageHeight - paddingTopAndBottom
            var overflowingHeight = pageContentHeight - availableSpace

            println("pageHeight =" + pageHeight)
            println("availableSpace =" + availableSpace)
            println("------overflowingHeight = " + overflowingHeight)

            if (pageContentHeight != 0.dp && (overflowingHeight > 0.dp)) {
                onPageOverflow()
                pageContentHeight = 0.dp
            }

            Column(
                modifier = Modifier
                    // .background(Color.Green)
                    .onGloballyPositioned { coordinates ->
                        pageHeaderHeight = with(localDensity) { coordinates.size.height.toDp() }
                        println("PAGE HEAD = " + pageHeaderHeight)

                    }) {
                if (index == 0) {
                    Text(
                        fontSize = 60.sp,
                        text = "HEADER"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                )
            }

            Column(
                Modifier
                    //.background(Color.Black)
                    .fillMaxWidth()
            ) {
                productArray?.forEach { product ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    ) {
                        var paddingTop = 4.dp

                        Column(
                            modifier = Modifier
                                .padding(start = 2.dp, end = 4.dp, top = paddingTop)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = product.id.toString() + " " + product.name + " page =" + product.page,
                            )
                        }
                    }

                }
            }

            /*     DeliveryNoteBasicTemplateFooter(uiState= uiState,
                     footerArray = footerArray,
                     onHeightCalculated = onFooterRowHeightCalculated
                 )*/
        }
    }
}

data class PageAndHeight(
    var page: Int = 1,
    var height: Dp = 0.dp,
)

data class FooterRow(
    var rowName: FooterRowName,
    var page: Int = 1,
    var height: Dp = 0.dp,
)

enum class FooterRowName {
    TOTAL_WITHOUT_TAX, TAXES, TOTAL_WITH_TAX
}

data class PageContentHeight(
    var height: Dp,
    var pageNumber: Int,
)
