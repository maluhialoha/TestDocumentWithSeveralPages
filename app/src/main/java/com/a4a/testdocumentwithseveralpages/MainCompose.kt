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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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

            println("xxxx = " + myViewModel.uiState)
            MainScreen(
                products.value.products,
                onAddValue = {
                    myViewModel.addProduct(
                        DocumentProductState(
                            id = null,
                            name = "another",
                            page = products.value.products.last().page
                        )
                    )
                },
                savePageNumbersInDatabase = {}
            )
        }
    }
}

@Composable
fun MainScreen(
    uiState: List<DocumentProductState>,
    onAddValue: () -> Unit,
    savePageNumbersInDatabase: () -> Unit,
) {
    println("uiState = " + uiState)

    Column {
        Pager(
            uiState = uiState,
            savePageNumbersInDatabase = savePageNumbersInDatabase
        )
        Spacer(Modifier.padding(42.dp))

        Button(onClick = onAddValue) {
            Text("Add a product to list")
        }
    }
}

fun calculateNumberOfPages(
    pagesContentHeight: SnapshotStateList<PageContentHeight>,
    availableSpace: Dp,
    numberOfPages: Int,
): Int {
    val totalContentHeight = pagesContentHeight.map { it.height.value }.sum()
    println("totalContentHeight" + totalContentHeight)
    println("availableSpace" + availableSpace)

    val quotient = (totalContentHeight / availableSpace.value).toInt()
    println("quotient" + quotient)

    val remainder = totalContentHeight % availableSpace.value
    println("remainder" + remainder)

    return if (remainder == 0F) quotient else quotient + 1
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pager(
    uiState: List<DocumentProductState>,
    savePageNumbersInDatabase: () -> Unit,
) {
    var numberOfPages by remember { mutableStateOf(1) }
    val pagerState = rememberPagerState { numberOfPages }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var productArray by mutableStateOf(uiState.map {
        ProductWithHeight(it)
    })

    val pagesContentHeight = remember { mutableStateListOf<PageContentHeight>() }

    Column {
        HorizontalPager(
            state = pagerState
        ) { pagerIndex ->
            println("index = " + pagerIndex)

            Column {
                /*      Text("page n°" + (pagerIndex + 1) + "/" + numberOfPages)
                      Text("triggerRecompose°" + (triggerRecompose + 1) )*/

                TemplateContent(
                    screenWidth = screenWidth,
                    productArray = productArray.filter { it.documentProduct.page == (pagerIndex + 1) },
                    //footerArray = ,
                    index = pagerIndex,
                    numberOfPages = numberOfPages,
                    onPageOverflow = { availableSpace, availableSpaceForDataTable ->
                        println("!!!!!!!!!!!!!!!!onpage overfloww")

                        numberOfPages = calculateNumberOfPages(
                            pagesContentHeight,
                            availableSpace,
                            numberOfPages

                        )
                        println("numberOfPages" + numberOfPages)

                        val limitIndexArray = mutableListOf(0)

                        println("productArray = " + productArray)

                        var indexOfTheFirstRowOnNextPage = 0


                        for (i in 0..(numberOfPages - 2)) {
                            val productsHeights = productArray.map { it.height.value }
                                .toMutableList() // ex: [1.6, 1.6, 2.4]
                            println("productsHeights = " + productsHeights)

                            /*val array =
                                productsHeights.slice(limitIndexArray.last()..productsHeights.lastIndex)
                            println("array = " + array)*/

                            val acc =
                                productsHeights.scan(0F) { acc, height -> acc + height }.drop(1)
                                    .toMutableList()

                            println("acc = " + acc)

                            println("availableSpaceForDataTable" + (availableSpaceForDataTable.value))

                            val limitHeight =
                                acc.lastOrNull() { it < availableSpaceForDataTable.value }
                            println("limitHeight = " + limitHeight)
                            indexOfTheFirstRowOnNextPage = acc.indexOf(limitHeight) + 1
                            println("indexOfTheFirstRowOnNextPage = " + indexOfTheFirstRowOnNextPage)

                            val idsOfProductToMoveToNextPage = productArray
                                .slice(indexOfTheFirstRowOnNextPage..productArray.lastIndex)
                                .map { it.documentProduct.id }

                            /* productArray = productArray.map { product ->
                               if (product.documentProduct.id in idsOfProductToMoveToNextPage) {
                                    product.copy(this = DocumentProductState(null, "k,", 1))
                                } else product
                            }*/

                            savePageNumbersInDatabase()

                            println("productArray = " + productArray)
                        }
                    },
                    onContentHeightCalculated = { height, pagerIndex ->
                        val heightsArrayIndex =
                            pagesContentHeight.indexOfFirst { it.pageNumber == pagerIndex + 1 }

                        if (heightsArrayIndex >= 0) {
                            pagesContentHeight[heightsArrayIndex].height = height
                        } else pagesContentHeight.add(
                            PageContentHeight(height, pagerIndex + 1)
                        )
                        println("pagesContentHeight1 =" + pagesContentHeight.toList())
                    },
                    onDataTableRowHeightCalculated = { height, product ->
                        println("onDataTableRowHeightCalculated productsHeight" + height + product)
                        println("productArray" + productArray)
                        productArray.first { it.documentProduct.id == product?.id }?.height =
                            height
                        println("productArray" + productArray)

                    },
                    onFooterRowHeightCalculated = { height, product ->
                        println("")
                        /*  footerArray.filter { it.rowName == product }.map {
                              it.height = height
                          }*/
                    },
                )
            }
        }
    }
}

data class ProductWithHeight(
    var documentProduct: DocumentProductState,
    var height: Dp = 0.dp,
)

@Composable
fun TemplateContent(
    screenWidth: Dp,
    productArray: List<ProductWithHeight>?,
    // footerArray: List<FooterRow>,
    index: Int,
    numberOfPages: Int,
    onPageOverflow: (Dp, Dp) -> Unit,
    onContentHeightCalculated: (Dp, Int) -> Unit,
    onDataTableRowHeightCalculated: (Dp, DocumentProductState?) -> Unit,
    onFooterRowHeightCalculated: (Dp, FooterRowName) -> Unit,
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
                    onContentHeightCalculated(pageContentHeight, index)
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
                onPageOverflow(availableSpace, availableSpace - pageHeaderHeight)
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
                productArray?.map { it.documentProduct }?.forEach { product ->
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
                                .onGloballyPositioned { coordinates ->
                                    onDataTableRowHeightCalculated.let {
                                        it(
                                            with(localDensity) { coordinates.size.height.toDp() + paddingTop },
                                            product
                                        )
                                    }
                                }
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
