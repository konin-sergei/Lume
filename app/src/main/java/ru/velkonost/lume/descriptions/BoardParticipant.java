package ru.velkonost.lume.descriptions;

import android.os.Parcel;
import android.os.Parcelable;

public class BoardParticipant implements Parcelable {

    private int id;
    private int avatar;
    private String login;
    private boolean last;
    private int reminded;

    public BoardParticipant(int id, int avatar, String login, boolean last, int reminded) {
        this.id = id;
        this.avatar = avatar;
        this.login = login;
        this.last = last;
        this.reminded = reminded;
    }

    private BoardParticipant(Parcel in) {
        id = in.readInt();
        avatar = in.readInt();
        login = in.readString();
        reminded = in.readInt();
    }

    public static final Creator<BoardParticipant> CREATOR = new Creator<BoardParticipant>() {
        @Override
        public BoardParticipant createFromParcel(Parcel in) {
            return new BoardParticipant(in);
        }

        @Override
        public BoardParticipant[] newArray(int size) {
            return new BoardParticipant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(avatar);
        parcel.writeString(login);
        parcel.writeInt(reminded);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getReminded() {
        return reminded;
    }

    public void setReminded(int reminded) {
        this.reminded = reminded;
    }
}
