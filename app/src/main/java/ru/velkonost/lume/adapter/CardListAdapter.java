package ru.velkonost.lume.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.velkonost.lume.R;
import ru.velkonost.lume.activity.BoardCardActivity;
import ru.velkonost.lume.model.Card;

import static ru.velkonost.lume.Constants.CARD_ID;
import static ru.velkonost.lume.Constants.CARD_NAME;
import static ru.velkonost.lume.Constants.COLUMN_ORDER;
import static ru.velkonost.lume.Constants.MARQUEE_REPEAT_LIMIT;
import static ru.velkonost.lume.Managers.HtmlConverterManager.fromHtml;

/**
 * @author Velkonost
 *
 * Список карточек
 */
public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardListViewHolder> {

    /**
     * Свойство - данные, с которыми необходимо работать
     */
    private List<Card> data;

    private Context mContext;

    public CardListAdapter(List<Card> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @Override
    public CardListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_column_card, parent, false);

        return new CardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardListViewHolder holder, int position) {
        final Card item = data.get(position);

        final int id = item.getId();

        holder.title.setText(fromHtml(item.getName()));
        holder.title.setSelected(true);
        holder.title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.title.setHorizontallyScrolling(true);
        holder.title.setMarqueeRepeatLimit(MARQUEE_REPEAT_LIMIT);

        holder.amount.setText(String.valueOf(item.getAmountParticipants()));
        holder.amount.setVisibility(View.VISIBLE);

        if (item.isBelong()){
            holder.isBelong.setVisibility(View.VISIBLE);
        }

        int backgroundColor = item.getColor();
        holder.mRelativeLayout.setBackgroundColor(backgroundColor);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, BoardCardActivity.class);
                intent.putExtra(CARD_ID, id);
                intent.putExtra(CARD_NAME, item.getName());
                intent.putExtra(COLUMN_ORDER, item.getColumnOrder());
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            }
        });
    }

    public void setData(List<Card> data) {
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class CardListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cardView) CardView cardView;
        @BindView(R.id.rlCard) RelativeLayout mRelativeLayout;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.numberParticipants) TextView amount;
        @BindView(R.id.isYouParticipant) ImageView isBelong;

        CardListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
