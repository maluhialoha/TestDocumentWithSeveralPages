package com.a4a.testdocumentwithseveralpages

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val productDataSource: DocumentProductDataSource,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null

/*    private val _uiState = mutableStateOf(DeliveryNote(
        deliveryNoteId = 1,
        documentProducts =
    ))
    val uiState: State<DeliveryNote> get() = _uiState*/

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                productDataSource.fetchAllProducts().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products.sortedBy { it.name })
                    }
                }
            } catch (e: Exception) {
                println("Fetching products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun addProduct(product: DocumentProductState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                productDataSource.saveProduct(product)
            } catch (e: Exception) {
                println("Fetching products failed with exception: ${e.localizedMessage}")
            }
        }
    }

}