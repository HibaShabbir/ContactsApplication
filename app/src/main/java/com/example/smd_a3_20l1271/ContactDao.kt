package com.example.smd_a3_20l1271

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
//ContactDao
@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("UPDATE contacts SET imageUri = :imageUri WHERE id = :contactId")
    suspend fun updateContactImage(contactId: String, imageUri: String)

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): LiveData<List<Contact>>
}
