package ru.crew.motley.piideo.search.fragment

import android.Manifest.permission.*
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_search_subject_input.view.*
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.network.Member
import ru.crew.motley.piideo.network.NetworkErrorCallback
import ru.crew.motley.piideo.network.neo.*
import ru.crew.motley.piideo.registration.fragments.PhoneFragment.*
import ru.crew.motley.piideo.search.SearchListener
import java.util.*

/**
 * Created by vas on 1/16/18.
 */
class UselessFragment : Fragment() {

    companion object {
        val TAG = UselessFragment::class.java.simpleName

        const val REQUEST_SD_CARD = 666

        fun newInstance(listener: SearchListener, errorCallback: NetworkErrorCallback) = UselessFragment().apply {
            this.callback = listener
            this.errorCallback = errorCallback
        }
    }

    lateinit var callback: SearchListener
    lateinit var errorCallback: NetworkErrorCallback
    var mMember: Member? = null
    val mPhones = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMember = ChatLab.get(activity).member
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_subject_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.next.setOnClickListener {
            val permissions = requiredPermissions()
            if (permissions.isNotEmpty()) {
                requestPermissions(permissions.toTypedArray(), REQUEST_SD_CARD)
            } else {
                Log.d(TAG, " on next ")
                callback.onNext()
            }
        }
    }

    private fun requiredPermissions(): List<String> {
        val writePermission = ContextCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(context!!, READ_EXTERNAL_STORAGE)
        val contactsPermission = ContextCompat.checkSelfPermission(context!!, READ_CONTACTS)
        val forRequest = mutableListOf<String>()
        if (writePermission != PERMISSION_GRANTED)
            forRequest.add(WRITE_EXTERNAL_STORAGE)
        if (readPermission != PERMISSION_GRANTED)
            forRequest.add(READ_EXTERNAL_STORAGE)
        if (contactsPermission != PERMISSION_GRANTED)
            forRequest.add(READ_CONTACTS)
        return forRequest
    }

    private fun knowsRequest(phone: String): Statement {
        val request = Statement()
        val filteredPhone = if (phone.length == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.length == MOROCCO_LENGTH && phone.startsWith(MOROCCO_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.length == NIGERIA_LENGTH && phone.startsWith(NIGERIA_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.startsWith("234") || phone.startsWith("212") || phone.startsWith("213")) {
            phone.substring(3, phone.length)
        } else if (phone.startsWith("33")) {
            phone.substring(2, phone.length)
        } else if (phone.length > 10) {
            phone.substring(phone.length - 10, phone.length)
        } else {
            phone
        }
        request.statement = Request.KNOWS
        val parameters = Parameters()

        parameters.props[Request.Var.PHONE + "From"] = mMember!!.phoneNumber
        parameters.props[Request.Var.PHONE + "To"] = filteredPhone
        request.parameters = parameters
        return request
    }

    private fun contactsRequests(): Statements {
        val statements = Statements()
        for (phone in mPhones) {
            statements.values.add(contactRequest(phone))
            statements.values.add(knowsRequest(phone))
        }
        return statements
    }

    private fun createContacts() {
        val api = NeoApiSingleton.getInstance()
        val statements1 = contactsRequests()
        api.executeStatement(statements1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ transaction1 ->
                    Log.d(TAG, "" + Arrays.toString(transaction1.errors.toTypedArray()))
                }
                ) { error1 ->
                    Log.e(TAG, "Error!: " + error1.localizedMessage)
                    errorCallback.onError()
                }

    }

    private fun syncContactPhones() {
        val managedCursor = activity!!.contentResolver
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        val phones = ArrayList<String>()
        try {
            if (managedCursor != null && managedCursor.count > 0) {
                managedCursor.moveToFirst()
                while (!managedCursor.isAfterLast) {
                    val phone = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phones.add(phone)
                    managedCursor.moveToNext()
                }
            }
        } finally {
            managedCursor!!.close()
        }
        for (phone in phones) {
            mPhones.add(phone.replace("\\D".toRegex(), ""))
        }
        createContacts()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_SD_CARD ->
                if (grantResults.isNotEmpty() && !grantResults.contains(PERMISSION_DENIED)) {
                    callback.onNext()
                    syncContactPhones()
                    Log.d(TAG, " on next ")
                } else {
                    Toast.makeText(activity,
                            "This is a key permission. " + "You can't use this app without it.",
                            Toast.LENGTH_SHORT)
                            .show()
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun contactRequest(phone: String): Statement {
        val request = Statement()
        if (phone.length == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            request.statement = Request.NEW_CONTACT_WITH_PHONE_PREFIX
        } else if (phone.length == MOROCCO_LENGTH && phone.startsWith(MOROCCO_PREFIX)) {
            request.statement = Request.NEW_CONTACT_WITH_PHONE_PREFIX
        } else if (phone.length == NIGERIA_LENGTH && phone.startsWith(NIGERIA_PREFIX)) {
            request.statement = Request.NEW_CONTACT_WITH_PHONE_PREFIX
        } else if (phone.length > 10) {
            request.statement = Request.NEW_CONTACT_WITH_COUNTRY_CODE
        } else {
            request.statement = Request.NEW_CONTACT
        }
        val parameters = Parameters()
        if (phone.length == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            parameters.props[Request.Var.PH_PREFIX] = phone.substring(0, 1)
            parameters.props[Request.Var.PHONE] = phone.substring(1, phone.length)
        } else if (phone.length == MOROCCO_LENGTH && phone.startsWith(MOROCCO_PREFIX)) {
            parameters.props[Request.Var.PH_PREFIX] = phone.substring(0, 1)
            parameters.props[Request.Var.PHONE] = phone.substring(1, phone.length)
        } else if (phone.length == NIGERIA_LENGTH && phone.startsWith(NIGERIA_PREFIX)) {
            parameters.props[Request.Var.PH_PREFIX] = phone.substring(0, 1)
            parameters.props[Request.Var.PHONE] = phone.substring(1, phone.length)
        } else if (phone.startsWith("33")) {
            parameters.props[Request.Var.C_CODE] = phone.substring(0, 2)
            parameters.props[Request.Var.PHONE] = phone.substring(2, phone.length)
        } else if (phone.startsWith("212") || phone.startsWith("213") || phone.startsWith("234")) {
            parameters.props[Request.Var.C_CODE] = phone.substring(0, 3)
            parameters.props[Request.Var.PHONE] = phone.substring(3, phone.length)
        } else if (phone.length > 10) {
            parameters.props[Request.Var.C_CODE] = phone.substring(0, phone.length - 10)
            parameters.props[Request.Var.PHONE] = phone.substring(phone.length - 10, phone.length)
        } else {
            parameters.props[Request.Var.PHONE] = phone
        }
        request.parameters = parameters
        return request
    }

}