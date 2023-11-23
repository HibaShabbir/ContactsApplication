package com.example.smd_a3_20l1271

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smd_a3_20l1271.ui.theme.Khaki40
import com.example.smd_a3_20l1271.ui.theme.Pink40
import com.example.smd_a3_20l1271.ui.theme.SMD_A3_20L1271Theme
import com.google.accompanist.coil.rememberCoilPainter

class MainActivity : ComponentActivity() {
    private val viewModel: ContactViewModel by viewModels()
    private val CONTACT_PICKER_REQUEST = 123
    private val GALLERY_PICKER_REQUEST = 456

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.insertedContact.observe(this) { insertedContact ->
            insertedContact?.let {
                // Handle the inserted contact as needed
                viewModel.clearInsertedContact()
            }
        }
        setContent {
            SMD_A3_20L1271Theme {
                // Observe the contacts as state
                val contacts by viewModel.contacts.observeAsState(initial = emptyList())
                val selectedImageUri by viewModel.selectedImageUri.observeAsState()

                // UI content
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Contact List")

                        Spacer(modifier = Modifier.height(16.dp))
                        Row() {
                            // Button to trigger contact import
                            Button(onClick = { onImportContactsClick() }) {
                                Text("Import Contacts")
                            }

                            // Button to trigger contact import
                            Button(onClick = { onImportSpecificContactsClick() }) {
                                Text("Specific Contact")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display the contact list
                        val selectedContact by viewModel.selectedContact.observeAsState()
                        ContactListScreen(
                            contacts = contacts,
                            onContactClick = { contact ->
                                onContactClick(
                                    contact
                                )
                            },
                            onUpdateClick = { updatedContact ->
                                viewModel.updateContact(
                                    updatedContact
                                )
                            },
                            onDeleteClick = { selectedContact?.let { viewModel.deleteContact(it) } },
                            onCallClick = { contact -> onCallClick(contact.phone) },
                            onMessageClick = { contact -> onMessageClick(contact.phone) }
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK -> {
                data?.data?.let { contactUri ->
                    val cursor = contentResolver.query(contactUri, null, null, null, null)

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val contactIdColumnIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                            val contactId = if (contactIdColumnIndex != -1) {
                                it.getString(contactIdColumnIndex) ?: ""
                            } else {
                                // Handle the case where the column index is -1 (not found)
                                ""
                            }
                            Log.d("MainActivity", "Selected ContactId: $contactId")

                            val contactNameColumnIndex =
                                it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                            val contactName = if (contactNameColumnIndex != -1) {
                                it.getString(contactNameColumnIndex) ?: ""
                            } else {
                                // Handle the case where the column index is -1 (not found)
                                ""
                            }

                            Log.d("MainActivity", "Selected ContactName: $contactName")

                            val phoneNumber = viewModel.getPhoneNumber(contentResolver, contactId)

                            Log.d("MainActivity", "Selected PhoneNumber: $phoneNumber")

                            val contact = Contact(
                                name = contactName,
                                phone = phoneNumber ?: "",
                                imageUri = "android.resource://com.example.smd_a3_20l1271/drawable/baseline_person_24"
                            )

                            // Now you have the selected contact, you can do whatever you want with it
                            // For example, insert it into the database or display it in the list
                            viewModel.insertContact(contact)
                        }
                    }
                }
            }
            requestCode == GALLERY_PICKER_REQUEST && resultCode == RESULT_OK -> {
                data?.data?.let { imageUri ->
                    // Update the selected image URI in the view model
                    viewModel.setSelectedImageUri(imageUri)
                }
            }
        }
    }

    private fun openImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_PICKER_REQUEST)
    }

    private fun onImportContactsClick() {
        // Assuming you have obtained the ContentResolver appropriately
        viewModel.importContacts(contentResolver, this)
    }

    private fun onImportSpecificContactsClick() {
        // Assuming you have obtained the ContentResolver appropriately
        viewModel.importSpecificContacts(contentResolver, this)
    }

    private fun onContactClick(contact: Contact) {
        // Update the selected contact when a contact is clicked
        // Do any additional logic here if needed
        viewModel.setSelectedContact(contact)
    }

    private fun onCallClick(phoneNumber: String) {
        // Create an intent to dial the phone number
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(callIntent)
    }

    private fun onMessageClick(phoneNumber: String) {
        // Create an intent to open the messaging app
        val messageIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
        startActivity(messageIntent)
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactListItem(
        contact: Contact,
        onContactClick: (Contact) -> Unit,
        onUpdateClick: (Contact) -> Unit,
        onDeleteClick: () -> Unit,
        onCallClick: (Contact) -> Unit,
        onMessageClick: (Contact) -> Unit
    ) {
        var isEditing by remember { mutableStateOf(false) }
        var updatedName by remember { mutableStateOf(contact.name) }
        var updatedPhone by remember { mutableStateOf(contact.phone) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onContactClick(contact) }
        ) {

            // Display the contact image
            Image(
                painter = if (contact.imageUri != null) {
                    rememberCoilPainter(request = contact.imageUri)
                } else {
                    painterResource(id = R.drawable.baseline_person_24)
                },
                contentDescription = null,
                modifier = Modifier.size(30.dp).clickable {
                    // Launch image picker when the image is clicked
                    openImagePicker()
                }
            )

            Column(
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(text = "Contact ", fontWeight = FontWeight.Bold)
                Text(text = "Name: ${if (isEditing) updatedName else contact.name}")
                Text(text = "Phone: ${if (isEditing) updatedPhone else contact.phone}")
            }

            // Buttons for actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FloatingActionButton(
                    onClick = {
                        isEditing = !isEditing
                        if (!isEditing) {
                            onUpdateClick(Contact(contact.id, updatedName, updatedPhone,null))
                        }
                    },
                    content = {
                        if (isEditing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Done, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Edit")
                            }
                        }
                    },
                    containerColor = (Pink40),
                    contentColor = Color.White
                )

                FloatingActionButton(
                    onClick = { onDeleteClick() },
                    content = { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete") },
                    containerColor = (MaterialTheme.colorScheme.error)
                )

                FloatingActionButton(
                    onClick = { onCallClick(contact) },
                    content = { Icon(imageVector = Icons.Outlined.Call, contentDescription = "Call") },
                    containerColor = (MaterialTheme.colorScheme.primary)
                )

                FloatingActionButton(
                    onClick = { onMessageClick(contact) },
                    content = { Icon(imageVector = Icons.Outlined.Send, contentDescription = "Msg") },
                    containerColor = (Khaki40),
                    contentColor = Color.White
                )
            }

            // Editable text fields when editing is enabled
            if (isEditing) {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = { Text("Updated Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                OutlinedTextField(
                    value = updatedPhone,
                    onValueChange = { updatedPhone = it },
                    label = { Text("Updated Phone") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }

    @Composable
    fun ContactListScreen(
        contacts: List<Contact>,
        onContactClick: (Contact) -> Unit,
        onUpdateClick: (Contact) -> Unit,
        onDeleteClick: () -> Unit,
        onCallClick: (Contact) -> Unit,
        onMessageClick: (Contact) -> Unit
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(contacts) { contact ->
                    ContactListItem(
                        contact = contact,
                        onContactClick = onContactClick,
                        onUpdateClick = onUpdateClick,
                        onDeleteClick = onDeleteClick,
                        onCallClick = onCallClick,
                        onMessageClick = onMessageClick
                    )
                }
            }
        }
    }
}

