package com.example.smd_a3_20l1271

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
//Contact.kt
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "imageUri")
    val imageUri: String?,

)

/*
@Composable
fun ContactDetailScreen(
    viewModel: ContactViewModel,
    onUpdateClick: (Contact) -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val contact by viewModel.selectedContact.observeAsState()

    contact?.let { contact ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Contact Details", fontWeight = FontWeight.Bold)
                    Text(text = "Name: ${contact.name}")
                    Text(text = "Phone: ${contact.phone}")

                }
                // Buttons for actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { onUpdateClick(contact) }) {
                            Text("Update")
                        }

                        Button(onClick = { onDeleteClick() }) {
                            Text("Delete")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { onCallClick() }) {
                            Text("Call")
                        }

                        Button(onClick = { onMessageClick() }) {
                            Text("Message")
                        }
                    }
                }
            }

        }
    }
}

 */
