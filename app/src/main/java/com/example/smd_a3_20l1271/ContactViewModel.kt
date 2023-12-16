package com.example.smd_a3_20l1271

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var repository: ContactRepository

    private val _insertedContact = MutableLiveData<Contact?>()
    val insertedContact: LiveData<Contact?> get() = _insertedContact

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> get() = _selectedContact

    private val READ_CONTACTS_PERMISSION_REQUEST_CODE = 123
    private val CONTACT_PICKER_REQUEST = 123

    init {
        // Initialize your repository here
        val database = ContactDatabase.getDatabase(application.applicationContext)
        repository = ContactRepository(database.contactDao())

        // Observe changes in the repository's LiveData and update the _contacts LiveData
        repository.allContacts.observeForever { contacts ->
            _contacts.value = contacts
        }
    }
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        val decodedUri = Uri.decode(uri.toString()).toUri()
        _selectedImageUri.value = decodedUri
        Log.d("image", decodedUri.toString() )
    }

    fun loadSelectedImageIntoImageView(imageView: ImageView) {
        _selectedImageUri.value?.let { imageUri ->
            // Use Coil to load the image into the provided ImageView
            imageView.load(imageUri)
        }
    }


    fun setRepository(contactRepository: ContactRepository) {
        repository = contactRepository
    }

    fun setSelectedContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.Main) {
            _selectedContact.value = contact
        }
    }

    fun importContacts(contentResolver: ContentResolver, activity: MainActivity) {
        // Check for READ_CONTACTS permission
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val contactsList = mutableListOf<Contact>()

                // Query the contacts provider
                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )

                cursor?.use {
                    while (it.moveToNext()) {
                        val contactIdColumnIndex =
                            it.getColumnIndex(ContactsContract.Contacts._ID)
                        val contactId = if (contactIdColumnIndex != -1) {
                            it.getString(contactIdColumnIndex)
                        } else {
                            // Handle the case where the column index is -1 (not found)
                            null
                        }

                        val contactNameColumnIndex =
                            it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                        val contactName = if (contactNameColumnIndex != -1) {
                            it.getString(contactNameColumnIndex)
                        } else {
                            // Handle the case where the column index is -1 (not found)
                            null
                        }

                        val phoneNumber = getPhoneNumber(contentResolver, contactId)

                        val contact = Contact(
                            name = contactName ?: "",
                            phone = phoneNumber ?: "",
                            imageUri = "android.resource://com.example.smd_a3_20l1271/drawable/baseline_person_24"
                        )
                        contactsList.add(contact)
                    }
                }

                // Insert the imported contacts into the database
                contactsList.forEach { contact ->
                    repository.insert(contact)
                }
            }
        } else {
            // Permission is not granted, show a Toast
            Toast.makeText(
                getApplication(),
                "Please grant READ_CONTACTS permission to import contacts.",
                Toast.LENGTH_SHORT
            ).show()

            // Request the permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun importSpecificContacts(contentResolver: ContentResolver, activity: MainActivity) {
        val contactPickerIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        activity.startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST)
    }

    fun getPhoneNumber(
        contentResolver: ContentResolver,
        contactId: String?
    ): String? {
        if (contactId.isNullOrBlank()) {
            return null
        }

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        cursor?.use {
            val phoneNumberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            if (it.moveToFirst() && phoneNumberIndex != -1) {
                val phoneNumber = it.getString(phoneNumberIndex)
                Log.d("ContactViewModel", "PhoneNumber for ContactId=$contactId: $phoneNumber")
                return phoneNumber
            }
        }

        Log.d("ContactViewModel", "PhoneNumber for ContactId=$contactId not found")
        return null
    }


    fun insertContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.Main) {
            repository.insert(contact)
        }
    }

    fun clearInsertedContact() {
        _insertedContact.value = null
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContact(contact)
        }
    }
}