package ru.velkonost.lume.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.velkonost.lume.Depository;
import ru.velkonost.lume.descriptions.BoardColumn;

public class BoardColumnsTabsFragmentAdapter extends FragmentPagerAdapter {

    private Map<Integer, BaseTabFragment> tabs;
    public static Map<Integer, Integer> tabsColumnOrder;
    private Context context;
    public static int last = 0;

    private String boardId;

    public BoardColumnsTabsFragmentAdapter(Context context, FragmentManager fm, String boardId) {
        super(fm);

        this.context = context;
        this.boardId = boardId;

        initTabsMap(context);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    private void initTabsMap(Context context) {
        tabs = new HashMap<>();
        tabsColumnOrder = new HashMap<>();

        List<BoardColumn> boardColumns = Depository.getBoardColumns();
        last = 0;

        for (int i = 0; i < boardColumns.size(); i++) {
            tabs.put(i, ColumnFragment.getInstance(context, boardColumns.get(i).getId(),
                    boardColumns.get(i).getName()));

            tabsColumnOrder.put(i, boardColumns.get(i).getId());

            last = i;
        }
        tabs.put(last + 1, AddColumnFragment.getInstance(context, boardId));
    }


}
