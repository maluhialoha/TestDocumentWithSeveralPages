package com.a4a.testdocumentwithseveralpages

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import testdocumentwithseveralpages.DocumentProduct
import java.math.BigDecimal

class DocumentProductDataSource(
    db: Database
) {
    private val documentProductQueries = db.documentProductQueries

    fun fetchAllProducts(): Flow<List<DocumentProductState>> {
        return documentProductQueries.getAllProducts()
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map { it.transformIntoEditableDocumentProduct() }
            }
    }

    suspend fun saveProduct(product: DocumentProductState) {
        return withContext(Dispatchers.IO) {
            try {
                documentProductQueries.saveProduct(
                    id = null,
                    name = product.name,
                    page = product.page.toLong(),
                )
            } catch (cause: Throwable) {
            }
        }
    }

    suspend fun updateLastItemPage() {
        return withContext(Dispatchers.IO) {
            try {
                documentProductQueries.updateLastProduct()
            } catch (cause: Throwable) {
            }
        }
    }
}

fun DocumentProduct.transformIntoEditableDocumentProduct(): DocumentProductState {
    return DocumentProductState(
        id = this.id.toInt(),
        name = this.name,
        page = this.page.toInt(),
    )
}
