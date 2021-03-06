package ru.velkonost.lume.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;

import java.util.List;

import ru.velkonost.lume.Managers.HtmlConverterManager;
import ru.velkonost.lume.R;
import ru.velkonost.lume.activity.MessageActivity;
import ru.velkonost.lume.activity.ProfileActivity;
import ru.velkonost.lume.model.DialogContact;

import static android.provider.LiveFolders.NAME;
import static ru.velkonost.lume.Constants.DIALOG_ID;
import static ru.velkonost.lume.Constants.ID;
import static ru.velkonost.lume.Constants.JPG;
import static ru.velkonost.lume.Constants.SLASH;
import static ru.velkonost.lume.Constants.URL.SERVER_AVATAR;
import static ru.velkonost.lume.Constants.URL.SERVER_HOST;
import static ru.velkonost.lume.Constants.URL.SERVER_PROTOCOL;
import static ru.velkonost.lume.Constants.URL.SERVER_RESOURCE;
import static ru.velkonost.lume.Managers.SetImageManager.fetchImage;

/**
 * @author Velkonost
 *
 * Список диалогов пользователя
 */
public class DialogListAdapter extends ArrayAdapter {

    /**
     * Свойство - данные, с которыми необходимо работать
     */
    private List<DialogContact> data;

    private Context mContext;

    public DialogListAdapter(Context context, List<DialogContact> data) {
        super(context, R.layout.item_dialog, data);
        mContext = context;
        this.data = data;
    }

    public void setData(List<DialogContact> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull final ViewGroup parent) {
        final DialogContact dialogContact = data.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dialog, null);
        }

        /** Формирование адреса, по которому лежит аватар пользователя */
        String avatarURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_RESOURCE
                + SERVER_AVATAR + SLASH + dialogContact.getAvatar()
                + SLASH + dialogContact.getId() + JPG;

        ((TextView) convertView.findViewById(R.id.userId)).setText(dialogContact.getId());

        if (!dialogContact.isAvatar()){
            fetchImage(avatarURL, (ImageView) convertView.findViewById(R.id.avatar), false, false);
            Bitmap bitmap = ((BitmapDrawable) ((ImageView) convertView.findViewById(R.id.avatar))
                    .getDrawable()).getBitmap();
            ((ImageView) convertView.findViewById(R.id.avatar)).setImageBitmap(bitmap);

            dialogContact.setIsAvatar(true);
        }

        if (dialogContact.getUnreadMessages() != 0){
            ((TextView) convertView.findViewById(R.id.unreadMessages))
                    .setText(String.valueOf(dialogContact.getUnreadMessages()));
            (convertView.findViewById(R.id.unreadMessages)).setVisibility(View.VISIBLE);
        }

        final String collocutor = dialogContact.getName().length() == 0
                ? dialogContact.getLogin()
                : dialogContact.getSurname().length() == 0
                ? dialogContact.getLogin()
                : dialogContact.getName() + " " + dialogContact.getSurname();

        final View finalConvertView = convertView;
        (convertView.findViewById(R.id.lluser)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (finalConvertView.findViewById(R.id.unreadMessages)).setVisibility(View.INVISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(DIALOG_ID, Integer.parseInt(dialogContact.getDialogId()));
                        intent.putExtra(ID, Integer.parseInt(dialogContact.getId()));
                        intent.putExtra(NAME, collocutor);
                        mContext.startActivity(intent);
                    }
                }, 150);
            }
        });

        (convertView.findViewById(R.id.lluser)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                TSnackbar snackbar = TSnackbar.make(parent,
                        HtmlConverterManager.fromHtml(
                                dialogContact.getName().length() == 0
                                ? dialogContact.getLogin()
                                : dialogContact.getSurname().length() == 0
                                ? dialogContact.getLogin()
                                : dialogContact.getName() + " " + dialogContact.getSurname()
                        ),
                        TSnackbar.LENGTH_SHORT);

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) snackbarView
                        .findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(18);
                snackbar.show();

                snackbarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ID, Integer.parseInt(dialogContact.getId()));
                        mContext.startActivity(intent);
                    }
                });

                return true;
            }
        });

        return convertView;
    }

}
