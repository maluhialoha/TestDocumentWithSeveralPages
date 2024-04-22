package com.a4a.testdocumentwithseveralpages
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*class MyyViewModel: ViewModel() {
    private val _counter = mutableStateOf(0)
    val counter: State<Int> get() = _counter

    private val _document = mutableStateOf(DeliveryNote(
        deliveryNoteId = 1,
        documentProducts = listOf(
            DocumentProductState(
                id = 1,
                name = "Chou",
            )
        )
    ))
    val document: State<DeliveryNote> get() = _document

    fun changeDocumentNumber() {
        _document.value = _document.value.copy(
            deliveryNoteId = 2
        )
    }

    fun addProductToDocument() {
        _document.value.deliveryNoteId = 4
        _uiState.value.documentProducts += DocumentProduct(
            id = 1,
            name = "Chou",
        )
        println("xxx = " + uiState)
    }

    fun incrementCounter()
    {
        _counter.value++
    }
    fun decrementCounter()
    {
        _counter.value--
    }
}*/

@Composable
fun MyyScreen(counterState: State<Int>,
              onIncrement: () -> Unit,
              onDecrement: () -> Unit,
              document: State<DeliveryNote>,
              onChangeNumber: () -> Unit,)
{
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "document: ${document.value}")
        Button(onClick = onChangeNumber) {
            Text("change number")
        }
        Button(onClick = onDecrement) {
            Text(text = "Decrement")
        }

        Spacer(modifier = Modifier.padding(40.dp))


        Text(text = "Counter: ${counterState.value}")
        Button(onClick = onIncrement) {
            Text("Increment")
        }
        Button(onClick = onDecrement) {
            Text(text = "Decrement")
        }
    }
}