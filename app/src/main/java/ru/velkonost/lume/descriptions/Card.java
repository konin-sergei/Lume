package ru.velkonost.lume.descriptions;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {

    private int id;
    private String name;
    private String description;

    private int[] userIds;

    public Card(int id, String name, int[] userIds) {
        this.id = id;
        this.name = name;
        this.userIds = userIds;
    }

    public Card(int id, String name, String description, int[] userIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userIds = userIds;
    }

    private Card(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();

    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(description);
    }
}
