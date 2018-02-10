package ru.crew.motley.piideo.search.fragment

import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_useless.view.*
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.network.Member
import ru.crew.motley.piideo.network.neo.*
import ru.crew.motley.piideo.registration.fragments.PhoneFragment.*
import ru.crew.motley.piideo.search.SearchListener
import java.net.SocketTimeoutException
import java.util.*


/**
 * Created by vas on 1/16/18.
 */
class UselessFragment : Fragment() {

    companion object {
        val TAG = UselessFragment::class.java.simpleName

//        val FRENCH_LENGTH = 10
//        val FRENCH_PREFIX = "0"

        fun newInstance(listener: SearchListener) = UselessFragment().apply { this.callback = listener }
    }

    lateinit var callback: SearchListener
    lateinit var mMember: Member
    val mPhones = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMember = ChatLab.get(activity).member
        loadContactsPhones()
        createContacts()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_useless, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.next.setOnClickListener { callback.onNext() }
    }

    private fun knowsRequest(phone: String): Statement {
        val request = Statement()
        val filteredPhone = if (phone.length == FRENCH_LENGTH && phone.startsWith(FRENCH_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.length == MOROCCO_LENGTH && phone.startsWith(MOROCCO_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.length == NIGERIA_LENGTH && phone.startsWith(NIGERIA_PREFIX)) {
            phone.substring(1, phone.length)
        } else if (phone.startsWith("234")) {
            phone.substring(3, phone.length)
        } else if (phone.startsWith("212")) {
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

        parameters.props[Request.Var.PHONE + "From"] = mMember.phoneNumber
        parameters.props[Request.Var.PHONE + "To"] = filteredPhone
        request.parameters = parameters
        return request
    }

    private fun contactsRequests(): Statements {
        val statements = Statements()
        for (phone in mPhones) {
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
                    Toast.makeText(activity, R.string.ex_network, Toast.LENGTH_SHORT)
                            .show()
                    if (error1 !is SocketTimeoutException) {
                        throw RuntimeException(error1)
                    }
                }

    }

    private fun loadContactsPhones() {
        val managedCursor = activity!!.contentResolver
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        try {
            if (managedCursor != null && managedCursor.count > 0) {
                managedCursor.moveToFirst()
                while (!managedCursor.isAfterLast) {
                    var phone = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phone = phone.replace("\\D".toRegex(), "")
                    mPhones.add(phone)
                    managedCursor.moveToNext()
                }
            }
        } finally {
            managedCursor!!.close()
        }
    }

}