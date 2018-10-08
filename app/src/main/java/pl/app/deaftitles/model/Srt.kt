package pl.app.deaftitles.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Object class which represents .srt file element
 */
class Srt() : Parcelable {

    //Line number
    @SerializedName("line")
    var line: Int = 0

    @SerializedName("startTime")
    //Subtitle start time
    var startTime: Long = 0L

    @SerializedName("endTime")
    //Subtitle end time
    var endTime: Long = 0L

    @SerializedName("startTimeString")
    //Subtitle start time
    var startTimeString: String = ""

    @SerializedName("endTimeString")
    //Subtitle end time
    var endTimeString: String = ""

    @SerializedName("subtitle")
    //Subtitles
    var subtitle: String = ""

    constructor(parcel: Parcel) : this() {
        line = parcel.readInt()
        startTime = parcel.readLong()
        endTime = parcel.readLong()
        startTimeString = parcel.readString()
        endTimeString = parcel.readString()
        subtitle = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(line)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeString(startTimeString)
        parcel.writeString(endTimeString)
        parcel.writeString(subtitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Srt> {
        override fun createFromParcel(parcel: Parcel): Srt {
            return Srt(parcel)
        }

        override fun newArray(size: Int): Array<Srt?> {
            return arrayOfNulls(size)
        }
    }


}