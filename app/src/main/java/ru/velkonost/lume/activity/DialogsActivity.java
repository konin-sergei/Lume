package ru.velkonost.lume.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyalbira.loadingdots.LoadingDots;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.velkonost.lume.Managers.InitializationsManager;
import ru.velkonost.lume.Managers.TypefaceUtil;
import ru.velkonost.lume.R;
import ru.velkonost.lume.model.DialogContact;
import ru.velkonost.lume.fragments.DialogsFragment;

import static ru.velkonost.lume.Constants.AVATAR;
import static ru.velkonost.lume.Constants.DIALOG_ID;
import static ru.velkonost.lume.Constants.EQUALS;
import static ru.velkonost.lume.Constants.ID;
import static ru.velkonost.lume.Constants.IDS;
import static ru.velkonost.lume.Constants.LOGIN;
import static ru.velkonost.lume.Constants.NAME;
import static ru.velkonost.lume.Constants.STATUS;
import static ru.velkonost.lume.Constants.SURNAME;
import static ru.velkonost.lume.Constants.UNREAD_MESSAGES;
import static ru.velkonost.lume.Constants.URL.SERVER_DIALOG_SCRIPT;
import static ru.velkonost.lume.Constants.URL.SERVER_HOST;
import static ru.velkonost.lume.Constants.URL.SERVER_PROTOCOL;
import static ru.velkonost.lume.Constants.URL.SERVER_SHOW_DIALOGS_METHOD;
import static ru.velkonost.lume.Constants.USER_ID;
import static ru.velkonost.lume.Constants.USER_PLACE_LIVING;
import static ru.velkonost.lume.Constants.USER_PLACE_STUDY;
import static ru.velkonost.lume.Constants.USER_PLACE_WORK;
import static ru.velkonost.lume.Constants.USER_WORKING_EMAIL;
import static ru.velkonost.lume.Managers.InitializationsManager.changeActivityCompat;
import static ru.velkonost.lume.Managers.InitializationsManager.initToolbar;
import static ru.velkonost.lume.Managers.PhoneDataStorageManager.deleteText;
import static ru.velkonost.lume.Managers.PhoneDataStorageManager.loadText;
import static ru.velkonost.lume.net.ServerConnection.getJSON;

/**
 * @author Velkonost
 *
 * Класс, описывающий активность существующих диалогов пользователя.
 *
 */
public class DialogsActivity extends AppCompatActivity {

    private static final int LAYOUT = R.layout.activity_dialogs;

    /**
     * Свойство - описание верхней панели инструментов приложения.
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Свойство - описание {@link SearchActivity#LAYOUT}
     */
    @BindView(R.id.activity_dialogs)
    DrawerLayout drawerLayout;

    /**
     * Свойство - строка поиска.
     * {@link MaterialSearchView}
     */
    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    /**
     * Свойство - боковая панель
     */
    @BindView(R.id.navigation)
    NavigationView navigationView;

    /**
     * Свойство - элемент, символизирующий загрузку данных
     */
    @BindView(R.id.loadingDots)
    LoadingDots loadingDots;

    /**
     * Свойство - идентификатор пользователя, авторизованного на данном устройстве.
     */
    private String userId;

    /**
     * Свойство - следующая активность.
     */
    private Intent nextIntent;

    private ArrayList<String> ids;

    /**
     * Свойство - экзмепляр класса {@link GetDialogs}
     */
    protected GetDialogs mGetDialogs;

    /**
     * Свойство - список контактов.
     * {@link DialogContact}
     */
    private List<DialogContact> mDialogs;

    private DialogsFragment dialogsFragment;

    private TimerCheckDialogsState timer;

    /**
     * Свойство - определяет, можно ли обновлять состояние списка диалогов
     */
    private boolean letRefresh = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBase();
        getData();
        initialize();

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        executeTasks();
        startTimer();

    }

    /**
     * Установка первоначальных настроек активности
     */
    private void setBase() {
        setContentView(LAYOUT);
        ButterKnife.bind(this);
        setTheme(R.style.AppTheme_Cursor);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/Roboto-Regular.ttf");
    }

    /**
     * Инитиализация основных элементов
     */
    private void initialize() {

        mGetDialogs = new GetDialogs();
        ids = new ArrayList<>();
        mDialogs = new ArrayList<>();

        /** {@link InitializationsManager#initToolbar(Toolbar, int)}  */
        initToolbar(DialogsActivity.this, toolbar, R.string.menu_item_messages); /** Инициализация */
        initNavigationView(); /** Инициализация */

        /**
         * Инициализируем строку поиска.
         * {@link MaterialSearchView}
         * {@link InitializationsManager#initSearch(Activity, MaterialSearchView)}
         **/
        initSearchDialog(searchView);
        searchView.setCursorDrawable(R.drawable.cursor_drawable);

    }

    /**
     * Получение данных (отсутствует получение с интернета)
     */
    private void getData() {
        getFromFile();
    }

    /**
     * Получение данных из специального файла приложения
     */
    private void getFromFile() {
        /**
         * Получение id пользователя.
         * {@link PhoneDataStorageManager#loadText(Context, String)}
         **/
        userId = loadText(DialogsActivity.this, ID);
    }

    /**
     * Вызов процессов, происходящих в параллельных потоках
     */
    private void executeTasks() {
        mGetDialogs.execute();
    }

    /**
     * Запуск таймера
     */
    private void startTimer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timer = new TimerCheckDialogsState(100000000, 5000);
                timer.start();
            }
        }, 5000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null)
            timer.cancel();
    }

    /**
     * Инициализация поиска по диалогам
     * @param searchView
     */
    private void initSearchDialog(final MaterialSearchView searchView) {

        searchView.setEllipsize(true);
        final boolean[] check = {false, true};



        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!mDialogs.isEmpty()) {
                    dialogsFragment.search(query, check[0], check[1]);
                    check[1] = false;
                    searchView.clearFocus();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                letRefresh = newText.isEmpty();

                if (!mDialogs.isEmpty()) {
                    dialogsFragment.search(newText, check[0], check[1]);
                    check[0] = true;
                    check[1] = true;
                }

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        /**
         * Устанавливает меню для строки поиска.
         */
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        /**
         * Вешает слушателя для открытия строки по нажатию.
         */
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown () {
                searchView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onSearchViewClosed() {
                letRefresh = true;
            }
        });
        return true;
    }

    /**
     * При нажатии на кнопку "Назад" поиск закрывется.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (searchView.isSearchOpen())
            searchView.closeSearch();
        else
            super.onBackPressed();
    }

    /**
     * Скрытие клавиатуры
     */
    private void hideKeyBoard() {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        getCurrentFocus().clearFocus();

    }

    private ActionBarDrawerToggle initializeToggle() {
        return new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.view_navigation_open, R.string.view_navigation_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                hideKeyBoard();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                hideKeyBoard();
            }
        };
    }

    /**
     * Инициализация заголовка боковой панели
     */
    private void initializeNavHeader() {
        View header = navigationView.getHeaderView(0);
        initializeNavHeaderLogin(header);
        initializeNavHeaderAskQuestion(header);
    }

    /**
     * Инициализация элемента в заголовке боковой панели
     * @param header - заголовок боковой панели
     */
    private void initializeNavHeaderAskQuestion(View header) {

        ImageView askQuestion = ButterKnife.findById(header, R.id.askQuestion);

        askQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Обновляет страницу.
                         * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                         * */
                        changeActivityCompat(DialogsActivity.this,
                                new Intent(DialogsActivity.this, FAQBotActivity.class));
                    }
                }, 350);

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

    }

    /**
     * Инициализация элемента в заголовке боковой панели
     * @param header - заголовок боковой панели
     */
    private void initializeNavHeaderLogin(View header) {

        TextView navHeaderLogin = ButterKnife.findById(header, R.id.userNameHeader);
        navHeaderLogin.setText(loadText(DialogsActivity.this, LOGIN));

        navHeaderLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Обновляет страницу.
                         * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                         * */
                        changeActivityCompat(DialogsActivity.this,
                                new Intent(DialogsActivity.this, ProfileActivity.class));
                    }
                }, 350);

                drawerLayout.closeDrawer(GravityCompat.START);

            }
        });

    }

    /**
     * Установка слушателя на боковую панель
     */
    private void setNavigationViewListener() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();

                /** Инициализируем намерение на следующую активность */
                switch (menuItem.getItemId()) {

                    /** Переход на контакты данного пользователя */
                    case R.id.navigationContacts:
                        nextIntent = new Intent(DialogsActivity.this, ContactsActivity.class);
                        break;

                    /** Переход на страницу сообщений данного пользователя */
                    case R.id.navigationMessages:
                        nextIntent = new Intent(DialogsActivity.this, DialogsActivity.class);
                        break;

                    /** Переход на страницу досок карточной версии канбан-системы */
                    case R.id.navigationBoards:
                        nextIntent = new Intent(DialogsActivity.this, BoardsListActivity.class);
                        break;

                    /** Переход на страницу индивидуальных настроек для данного пользователя */
                    case R.id.navigationSettings:
                        nextIntent = new Intent(DialogsActivity.this, SettingsActivity.class);
                        break;

                    /**
                     * Завершение сессии даного пользователя на данном устройстве.
                     * Удаляем всю информацию об авторизованном пользователе.
                     * Переход на страницу приветствия {@link WelcomeActivity}
                     **/
                    case R.id.navigationLogout:
                        deleteText(DialogsActivity.this, ID);
                        nextIntent = new Intent(DialogsActivity.this, WelcomeActivity.class);
                        break;
                }

                /**
                 * Переход на следующую активность.
                 * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                 * */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (loadText(DialogsActivity.this, ID).equals("")) {
                            deleteText(DialogsActivity.this, USER_PLACE_LIVING);
                            deleteText(DialogsActivity.this, USER_PLACE_STUDY);
                            deleteText(DialogsActivity.this, USER_PLACE_WORK);
                            deleteText(DialogsActivity.this, USER_WORKING_EMAIL);
                        }

                        /**
                         * Обновляет страницу.
                         * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                         * */
                        changeActivityCompat(DialogsActivity.this, nextIntent);
                    }
                }, 350);


                /** Если был осуществлен выход из аккаунта, то закрываем активность профиля */
                if (loadText(DialogsActivity.this, ID).equals("")) finishAffinity();

                drawerLayout.closeDrawer(GravityCompat.START);

                return false;
            }
        });
    }

    /**
     * Рисует боковую панель навигации.
     **/
    private void initNavigationView() {

        ActionBarDrawerToggle toggle = initializeToggle();
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        initializeNavHeader();
        setNavigationViewListener();
    }

    /**
     * Таймер для обновления состояния списка диалогов
     */
    private class TimerCheckDialogsState extends CountDownTimer {

        TimerCheckDialogsState(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            RefreshDialogs mRefreshDialogs = new RefreshDialogs();
            mRefreshDialogs.execute();
        }

        @Override
        public void onFinish() {
        }
    }

    /**
     * Получения данных о диалогах
     */
    private class GetDialogs extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_DIALOG_SCRIPT
                    + SERVER_SHOW_DIALOGS_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = USER_ID + EQUALS + userId;

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
                 * Составление view-элементов с краткой информацией о пользователях
                 */
                for (int i = 0; i < ids.size(); i++) {

                    /**
                     * Получение JSON-объекта с информацией о конкретном пользователе по его идентификатору.
                     */
                    JSONObject userInfo = dataJsonObj.getJSONObject(ids.get(i));

                    mDialogs.add(new DialogContact(userInfo.getString(ID),
                            userInfo.getString(DIALOG_ID), userInfo.getString(NAME),
                            userInfo.getString(SURNAME), userInfo.getString(LOGIN),
                            Integer.parseInt(userInfo.getString(UNREAD_MESSAGES)),
                            Integer.parseInt(userInfo.getString(AVATAR)),
                            Integer.parseInt(userInfo.getString(STATUS)) == 0));
                }

                /**
                 * Добавляем фрагмент на экран.
                 * {@link DialogsFragment}
                 */
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                dialogsFragment
                        = DialogsFragment.getInstance(DialogsActivity.this, mDialogs);
                ft.add(R.id.lldialog, dialogsFragment);
                ft.commit();

                loadingDots.setVisibility(View.INVISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Обновление состояния списка диалогов
     */
    private class RefreshDialogs extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_DIALOG_SCRIPT
                    + SERVER_SHOW_DIALOGS_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = USER_ID + EQUALS + userId;

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
                    if (!ids.contains(idsJSON.getString(i)))
                        ids.add(idsJSON.getString(i));
                }

                /**
                 * Составление view-элементов с краткой информацией о пользователях
                 */
                for (int i = 0; i < ids.size(); i++) {
                    boolean exist = false;
                    /**
                     * Получение JSON-объекта с информацией о конкретном пользователе по его идентификатору.
                     */
                    JSONObject userInfo = dataJsonObj.getJSONObject(ids.get(i));

                    for (int j = 0; j < mDialogs.size(); j++){
                        if (mDialogs.get(j).getId().equals(userInfo.getString(ID))) {

                            mDialogs.get(j).setUnreadMessages(Integer.parseInt(userInfo
                                    .getString(UNREAD_MESSAGES)));
                            mDialogs.get(j).setIsAvatar(true);

                            exist = true;
                            break;
                        }
                    }

                    if (!exist){
                        mDialogs.add(new DialogContact(userInfo.getString(ID),
                                userInfo.getString(DIALOG_ID), userInfo.getString(NAME),
                                userInfo.getString(SURNAME), userInfo.getString(LOGIN),
                                Integer.parseInt(userInfo.getString(UNREAD_MESSAGES)),
                                Integer.parseInt(userInfo.getString(AVATAR)),
                                Integer.parseInt(userInfo.getString(STATUS)) == 0));
                    }
                }

                /**
                 * Добавляем фрагмент на экран.
                 * {@link DialogsFragment}
                 */
                if(!isFinishing() && letRefresh) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    dialogsFragment.refreshContacts(mDialogs);
                    ft.replace(R.id.lldialog, dialogsFragment);
                    ft.commitAllowingStateLoss();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
