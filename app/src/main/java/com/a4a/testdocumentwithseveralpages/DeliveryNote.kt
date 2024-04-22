package com.a4a.testdocumentwithseveralpages

data class DeliveryNote(
    var deliveryNoteId: Int = 0,
    var documentProducts: List<DocumentProductState>,
)

data class DocumentProductState(
    var id: Int? = null,
    var name: String = "",
    var page: Int = 1
)

data class ProductsUiState(
    val products: List<DocumentProductState> = listOf(),
    val isFetchingproducts: Boolean = false,
)
