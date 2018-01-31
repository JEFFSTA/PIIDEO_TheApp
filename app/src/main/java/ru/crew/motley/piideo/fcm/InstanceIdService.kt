package ru.crew.motley.piideo.fcm

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.network.Member

/**
 * Created by vas on 1/11/18.
 */

class InstanceIdService : FirebaseInstanceIdService() {

    companion object {
        val TAG = InstanceIdService::class.java.simpleName
    }

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val instanceId = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "refresh token " + instanceId)
        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(it.uid)
                    .child("instanceId")
                    .setValue(instanceId)
        }
//        val lab = ChatLab.get(this)
//        val member = lab.member
//        if (member != null) {
//            member.chatId = instanceId
//            lab.updateMemberToken(member)
//        }
    }
}