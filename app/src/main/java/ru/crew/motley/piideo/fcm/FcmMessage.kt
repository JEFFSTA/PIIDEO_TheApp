package ru.crew.motley.piideo.fcm

import org.parceler.Parcel
import org.parceler.ParcelConstructor

/**
 * Created by vas on 1/12/18.
 */
@Parcel(Parcel.Serialization.BEAN)
class FcmMessage @ParcelConstructor constructor(
        val timestamp: Long? = null,
        val negatedTimestamp: Long? = null,
        val dayTimestamp: Long? = null,
        val from: String? = null,
        val to: String? = null,
        val content: String? = null,
        val type: String? = null,
        val ownerReceiver: String? = null,
        val visible: Boolean = false) {

    constructor(): this(null)

}
