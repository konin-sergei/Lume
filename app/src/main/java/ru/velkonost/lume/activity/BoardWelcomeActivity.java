package ru.velkonost.lume.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.velkonost.lume.Depository;
import ru.velkonost.lume.Managers.Initializations;
import ru.velkonost.lume.Managers.PhoneDataStorage;
import ru.velkonost.lume.Managers.ValueComparator;
import ru.velkonost.lume.R;
import ru.velkonost.lume.adapter.BoardInviteListAdapter;
import ru.velkonost.lume.descriptions.BoardColumn;
import ru.velkonost.lume.descriptions.BoardParticipant;
import ru.velkonost.lume.descriptions.Contact;
import ru.velkonost.lume.fragments.BoardDescriptionFragment;
import ru.velkonost.lume.fragments.BoardParticipantsFragment;
import ru.velkonost.lume.fragments.BoardWelcomeColumnFragment;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static ru.velkonost.lume.Constants.AMPERSAND;
import static ru.velkonost.lume.Constants.AVATAR;
import static ru.velkonost.lume.Constants.BOARD_DESCRIPTION;
import static ru.velkonost.lume.Constants.BOARD_ID;
import static ru.velkonost.lume.Constants.BOARD_LAST_CONTRIBUTED_USER;
import static ru.velkonost.lume.Constants.BOARD_NAME;
import static ru.velkonost.lume.Constants.COLUMN_IDS;
import static ru.velkonost.lume.Constants.EQUALS;
import static ru.velkonost.lume.Constants.ID;
import static ru.velkonost.lume.Constants.IDS;
import static ru.velkonost.lume.Constants.LOGIN;
import static ru.velkonost.lume.Constants.NAME;
import static ru.velkonost.lume.Constants.SURNAME;
import static ru.velkonost.lume.Constants.URL.SERVER_ACCOUNT_SCRIPT;
import static ru.velkonost.lume.Constants.URL.SERVER_GET_BOARD_INFO_METHOD;
import static ru.velkonost.lume.Constants.URL.SERVER_GET_CONTACTS_METHOD;
import static ru.velkonost.lume.Constants.URL.SERVER_HOST;
import static ru.velkonost.lume.Constants.URL.SERVER_KANBAN_SCRIPT;
import static ru.velkonost.lume.Constants.URL.SERVER_LEAVE_BOARD_METHOD;
import static ru.velkonost.lume.Constants.URL.SERVER_PROTOCOL;
import static ru.velkonost.lume.Constants.USER_IDS;
import static ru.velkonost.lume.Managers.Initializations.changeActivityCompat;
import static ru.velkonost.lume.Managers.Initializations.initToolbar;
import static ru.velkonost.lume.Managers.PhoneDataStorage.deleteText;
import static ru.velkonost.lume.Managers.PhoneDataStorage.loadText;
import static ru.velkonost.lume.Managers.PhoneDataStorage.saveText;
import static ru.velkonost.lume.R.layout.popup_board_invite_list;
import static ru.velkonost.lume.net.ServerConnection.getJSON;

public class BoardWelcomeActivity extends AppCompatActivity {

    private static final int LAYOUT = R.layout.activity_board_welcome;

    /**
     * Свойство - следующая активность.
     */
    private Intent nextIntent;

    /**
     * Свойство - описание верхней панели инструментов приложения.
     */
    private Toolbar toolbar;

    /**
     * Свойство - описание {@link SearchActivity#LAYOUT}
     */
    private DrawerLayout drawerLayout;

    /**
     * Свойство - идентификатор пользователя, авторизованного на данном устройстве.
     */
    private String userId;


    private int boardId;

    /**
     * Идентификаторы досок, к которым принадлежит авторизованный пользователь.
     **/
//    private ArrayList<String> bids;

    /**
     * Свойство - экзмепляр класса {@link GetBoardInfo}
     */
    protected GetBoardInfo mGetBoardInfo;

    private GetContacts mGetContacts;


    /**
     * Свойство - список контактов.
     * {@link ru.velkonost.lume.descriptions.BoardParticipant}
     */
    private List<BoardParticipant> mBoardParticipants;

    private List<BoardColumn> mBoardColumns;

    private List<Contact> mContacts;

    /**
     * Идентификаторы пользователей, некоторые данные которых соответствуют искомой информации.
     **/
    private ArrayList<String> ids;

    /**
     * Контакты авторизованного пользователя.
     *
     * Ключ - идентификатор пользователя.
     * Значение - его полное имя или логин.
     **/
    private Map<String, String> contacts;


    private RecyclerView recyclerView;
    private View popupView;
    public static PopupWindow popupWindowInvite;


    private EditText editBoardName;

    private String boardName;
    private String boardDescription;

    private BoardDescriptionFragment descriptionFragment;

    private Menu menu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(LAYOUT);

        mGetBoardInfo = new GetBoardInfo();
        mGetContacts = new GetContacts();

        mBoardParticipants = new ArrayList<>();
        mBoardColumns = new ArrayList<>();
        mContacts = new ArrayList<>();
        ids = new ArrayList<>();
        contacts = new HashMap<>();


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_board_welcome);

        editBoardName = (EditText) findViewById(R.id.editBoardName);

        /** {@link Initializations#initToolbar(Toolbar, int)}  */
        initToolbar(BoardWelcomeActivity.this, toolbar, R.string.menu_item_boards); /** Инициализация */
        initNavigationView(); /** Инициализация */

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /**
         * Получение id пользователя.
         * {@link PhoneDataStorage#loadText(Context, String)}
         **/
        userId = loadText(BoardWelcomeActivity.this, ID);

        Intent intent = getIntent();
        boardId = intent.getIntExtra(BOARD_ID, 0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        popupView = layoutInflater.inflate(popup_board_invite_list, null);

        popupWindowInvite = new PopupWindow(popupView,
                WRAP_CONTENT, height - dp2px(120));


        recyclerView = (RecyclerView) popupView
                .findViewById(R.id.recyclerViewBoardInvite);


        mGetBoardInfo.execute();
        mGetContacts.execute();


    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                BoardWelcomeActivity.this.getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_board_welcome);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_board_welcome, menu);

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                toolbar.setTitle("");
                editBoardName.setVisibility(View.VISIBLE);
                editBoardName.setText(boardName);

                descriptionFragment.showNext();

                menu.findItem(R.id.action_settings).setVisible(false);
                menu.findItem(R.id.action_invite).setVisible(false);
                menu.findItem(R.id.action_leave).setVisible(false);

                menu.findItem(R.id.action_agree).setVisible(true);

                menu.findItem(R.id.action_agree).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        toolbar.setTitle(editBoardName.getText().toString());
                        descriptionFragment.changeText();
                        descriptionFragment.showNext();

                        editBoardName.setVisibility(View.INVISIBLE);

                        menu.findItem(R.id.action_settings).setVisible(true);
                        menu.findItem(R.id.action_invite).setVisible(true);
                        menu.findItem(R.id.action_leave).setVisible(true);

                        menu.findItem(R.id.action_agree).setVisible(false);

                        return false;
                    }
                });

                break;
            case R.id.action_invite:

                popupWindowInvite.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        changeActivityCompat(BoardWelcomeActivity.this);
                    }
                });

                popupWindowInvite.setTouchable(true);
                popupWindowInvite.setFocusable(true);
                popupWindowInvite.setBackgroundDrawable(new ColorDrawable(getResources()
                        .getColor(android.R.color.transparent)));
                popupWindowInvite.setOutsideTouchable(true);

                popupWindowInvite.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                break;
            case R.id.action_leave:

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.leave_board))
                        .setMessage(getResources().getString(R.string.ask_confirmation))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        LeaveBoard leaveBoard = new LeaveBoard();
                                        leaveBoard.execute();

                                        changeActivityCompat(BoardWelcomeActivity.this,
                                                new Intent(BoardWelcomeActivity.this,
                                                        BoardsListActivity.class));
                                        finishAffinity();
                                    }
                                })
                        .create().show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Рисует боковую панель навигации.
     **/
    private void initNavigationView() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.view_navigation_open, R.string.view_navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();

                /** Инициализируем намерение на следующую активность */
                switch (menuItem.getItemId()) {

                    /** Переход на профиль данного пользователя */
                    case R.id.navigationProfile:
                        nextIntent = new Intent(BoardWelcomeActivity.this, ProfileActivity.class);
                        break;

                    /** Переход на контакты данного пользователя */
                    case R.id.navigationContacts:
                        nextIntent = new Intent(BoardWelcomeActivity.this, ContactsActivity.class);
                        break;

                    /** Переход на страницу напоминаний, созданных данным пользователем */
                    case R.id.navigationReminder:
                        break;

                    /** Переход на страницу сообщений данного пользователя */
                    case R.id.navigationMessages:
                        nextIntent = new Intent(BoardWelcomeActivity.this, DialogsActivity.class);
                        break;

                    /** Переход на страницу досок карточной версии канбан-системы */
                    case R.id.navigationBoards:
                        nextIntent = new Intent(BoardWelcomeActivity.this, BoardsListActivity.class);
                        break;

                    /** Переход на страницу индивидуальных настроек для данного пользователя */
                    case R.id.navigationSettings:
                        nextIntent = new Intent(BoardWelcomeActivity.this, SettingsActivity.class);
                        break;

                    /**
                     * Завершение сессии даного пользователя на данном устройстве.
                     * Удаляем всю информацию об авторизованном пользователе.
                     * Переход на страницу приветствия {@link WelcomeActivity}
                     **/
                    case R.id.navigationLogout:
                        deleteText(BoardWelcomeActivity.this, ID);
                        nextIntent = new Intent(BoardWelcomeActivity.this, WelcomeActivity.class);
                        break;
                }

                /**
                 * Переход на следующую активность.
                 * {@link Initializations#changeActivityCompat(Activity, Intent)}
                 * */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Обновляет страницу.
                         * {@link Initializations#changeActivityCompat(Activity, Intent)}
                         * */
                        changeActivityCompat(BoardWelcomeActivity.this, nextIntent);
                    }
                }, 350);


                /** Если был осуществлен выход из аккаунта, то закрываем активность профиля */
                if (loadText(BoardWelcomeActivity.this, ID).equals("")) finishAffinity();

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_board_welcome);
                drawer.closeDrawer(GravityCompat.START);

                return true;
            }
        });
    }

    private class LeaveBoard extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_KANBAN_SCRIPT
                    + SERVER_LEAVE_BOARD_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = BOARD_ID + EQUALS + boardId
                    + AMPERSAND + ID + EQUALS + userId;

            /** Свойство - код ответа, полученных от сервера */
            String resultJson = "";

            /**
             * Соединяется с сервером, отправляет данные, получает ответ.
             * {@link ru.velkonost.lume.net.ServerConnection#getJSON(String, String)}
             **/
            try {
                resultJson = getJSON(dataURL, params);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultJson;
        }
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
        }
    }
    private class GetBoardInfo extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_KANBAN_SCRIPT
                    + SERVER_GET_BOARD_INFO_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = BOARD_ID + EQUALS + boardId;

            /** Свойство - код ответа, полученных от сервера */
            String resultJson = "";

            /**
             * Соединяется с сервером, отправляет данные, получает ответ.
             * {@link ru.velkonost.lume.net.ServerConnection#getJSON(String, String)}
             **/
            try {
                resultJson = getJSON(dataURL, params);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultJson;
        }
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            /** Свойство - полученный JSON–объект*/
            JSONObject dataJsonObj;

            try {

                /**
                 * Получение JSON-объекта по строке.
                 */
                dataJsonObj = new JSONObject(strJson);

                /**
                 * Получение идентификаторов найденных пользователей.
                 */
                JSONArray idsJSON = dataJsonObj.getJSONArray(USER_IDS);
                JSONArray cidsJSON = dataJsonObj.getJSONArray(COLUMN_IDS);

                boardName = dataJsonObj.getString(BOARD_NAME);
                boardDescription = dataJsonObj.getString(BOARD_DESCRIPTION);

                toolbar.setTitle(boardName);
                saveText(BoardWelcomeActivity.this, BOARD_NAME, boardName);

                ArrayList<String> uids = new ArrayList<>();
                ArrayList<String> cids = new ArrayList<>();

                for (int i = 0; i < idsJSON.length(); i++) {
                    uids.add(idsJSON.getString(i));
                }

                for (int i = 0; i < cidsJSON.length(); i++) {
                    cids.add(cidsJSON.getString(i));
                }


                for (int i = 0; i < uids.size(); i++) {
                    String participantId = uids.get(i);

                    JSONObject userInfo = dataJsonObj.getJSONObject(participantId);

                    mBoardParticipants.add(new BoardParticipant(
                            Integer.parseInt(participantId.substring(0, uids.get(i).length() - 4)),
                            Integer.parseInt(userInfo.getString(AVATAR)),
                            userInfo.getString(LOGIN),
                            BOARD_LAST_CONTRIBUTED_USER == i + 1, uids.size() - i, boardId
                    ));

                    if (BOARD_LAST_CONTRIBUTED_USER == i) break;

                }

                for (int i = 0; i < cids.size(); i++) {
                    JSONObject columnInfo = dataJsonObj.getJSONObject(cids.get(i));

                    mBoardColumns.add(new BoardColumn(
                            Integer.parseInt(columnInfo.getString(ID)),
                            columnInfo.getString(NAME),  i)
                    );

                }

                Depository.setBoardColumns(mBoardColumns);

                saveText(BoardWelcomeActivity.this, BOARD_DESCRIPTION, boardDescription);

                descriptionFragment = new BoardDescriptionFragment();
                BoardParticipantsFragment boardParticipantsFragment
                        = BoardParticipantsFragment.getInstance(BoardWelcomeActivity.this, mBoardParticipants);
                BoardWelcomeColumnFragment boardWelcomeColumnFragment
                        = BoardWelcomeColumnFragment.getInstance(BoardWelcomeActivity.this, mBoardColumns);

                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.add(R.id.descriptionContainer, descriptionFragment);
                transaction.add(R.id.participantsContainer, boardParticipantsFragment);
                transaction.add(R.id.columnsContainer, boardWelcomeColumnFragment);
                transaction.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class GetContacts extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_ACCOUNT_SCRIPT
                    + SERVER_GET_CONTACTS_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = ID + EQUALS + userId;

            /** Свойство - код ответа, полученных от сервера */
            String resultJson = "";

            /**
             * Соединяется с сервером, отправляет данные, получает ответ.
             * {@link ru.velkonost.lume.net.ServerConnection#getJSON(String, String)}
             **/
            try {
                resultJson = getJSON(dataURL, params);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultJson;
        }
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            /** Свойство - полученный JSON–объект*/
            JSONObject dataJsonObj;

            try {

                /**
                 * Получение JSON-объекта по строке.
                 */
                dataJsonObj = new JSONObject(strJson);

                /**
                 * Получение идентификаторов найденных пользователей.
                 */
                JSONArray idsJSON = dataJsonObj.getJSONArray(IDS);

                for (int i = 0; i < idsJSON.length(); i++){
                    ids.add(idsJSON.getString(i));
                }

                /**
                 * Заполнение Map{@link contacts} для последующей сортировки контактов.
                 *
                 * По умолчанию идентификатору контакта соответствует его полное имя.
                 *
                 * Если такогого не имеется, то устанавливает взамен логин.
                 **/
                for (int i = 0; i < ids.size(); i++){
                    JSONObject userInfo = dataJsonObj.getJSONObject(ids.get(i));

                    contacts.put(
                            ids.get(i),
                            userInfo.getString(NAME).length() != 0
                                    ? userInfo.getString(SURNAME).length() != 0
                                    ? userInfo.getString(NAME) + " " + userInfo.getString(SURNAME)
                                    : userInfo.getString(LOGIN) : userInfo.getString(LOGIN)
                    );
                }

                /** Создание и инициализация Comparator{@link ValueComparator} */
                Comparator<String> comparator = new ValueComparator<>((HashMap<String, String>) contacts);

                /** Помещает отсортированную Map */
                TreeMap<String, String> sortedContacts = new TreeMap<>(comparator);
                sortedContacts.putAll(contacts);

                /** "Обнуляет" хранилище идентификаторов */
                ids = new ArrayList<>();

                /** Заполняет хранилище идентификаторов */
                for (String key : sortedContacts.keySet()) {
                    ids.add(key);
                }

                /** "Поворачивает" хранилище идентификаторов */
                Collections.reverse(ids);

                /**
                 * Составление view-элементов с краткой информацией о пользователях
                 */
                for (int i = 0; i < ids.size(); i++) {

                    /**
                     * Получение JSON-объекта с информацией о конкретном пользователе по его идентификатору.
                     */
                    JSONObject userInfo = dataJsonObj.getJSONObject(ids.get(i));

                    mContacts.add(new Contact(userInfo.getString(ID), userInfo.getString(NAME),
                            userInfo.getString(SURNAME), userInfo.getString(LOGIN),
                            Integer.parseInt(userInfo.getString(AVATAR))));
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(BoardWelcomeActivity.this));
                recyclerView.setAdapter(new BoardInviteListAdapter(BoardWelcomeActivity.this, mContacts, boardId));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
