package ru.velkonost.lume.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.velkonost.lume.Managers.InitializationsManager;
import ru.velkonost.lume.R;
import ru.velkonost.lume.Managers.TypefaceUtil;

import static ru.velkonost.lume.Constants.AMPERSAND;
import static ru.velkonost.lume.Constants.AVATAR;
import static ru.velkonost.lume.Constants.BIRTHDAY;
import static ru.velkonost.lume.Constants.CITY;
import static ru.velkonost.lume.Constants.COUNTRY;
import static ru.velkonost.lume.Constants.EMAIL;
import static ru.velkonost.lume.Constants.EQUALS;
import static ru.velkonost.lume.Constants.GET_DATA_SETTINGS;
import static ru.velkonost.lume.Constants.GET_EDIT_RESULT;
import static ru.velkonost.lume.Constants.ID;
import static ru.velkonost.lume.Constants.JPG;
import static ru.velkonost.lume.Constants.LOGIN;
import static ru.velkonost.lume.Constants.NAME;
import static ru.velkonost.lume.Constants.NEW_PASSWORD;
import static ru.velkonost.lume.Constants.PREV_PASSWORD;
import static ru.velkonost.lume.Constants.SLASH;
import static ru.velkonost.lume.Constants.STUDY;
import static ru.velkonost.lume.Constants.SURNAME;
import static ru.velkonost.lume.Constants.URL.SERVER_ACCOUNT_SCRIPT;
import static ru.velkonost.lume.Constants.URL.SERVER_AVATAR;
import static ru.velkonost.lume.Constants.URL.SERVER_EDIT_PARAMETERS_METHOD;
import static ru.velkonost.lume.Constants.URL.SERVER_GET_DATA_SETTINGS_METHOD;
import static ru.velkonost.lume.Constants.URL.SERVER_HOST;
import static ru.velkonost.lume.Constants.URL.SERVER_PROTOCOL;
import static ru.velkonost.lume.Constants.URL.SERVER_RESOURCE;
import static ru.velkonost.lume.Constants.USER_ID;
import static ru.velkonost.lume.Constants.WORK;
import static ru.velkonost.lume.Constants.WORK_EMAIL;
import static ru.velkonost.lume.Managers.DateConverterManager.formatDate;
import static ru.velkonost.lume.Managers.DateConverterManager.formatDateBack;
import static ru.velkonost.lume.Managers.SetImageManager.fetchImage;
import static ru.velkonost.lume.Managers.InitializationsManager.changeActivityCompat;
import static ru.velkonost.lume.Managers.InitializationsManager.initToolbar;
import static ru.velkonost.lume.Managers.InitializationsManager.inititializeAlertDialog;
import static ru.velkonost.lume.Managers.InitializationsManager.inititializeAlertDialogWithRefresh;
import static ru.velkonost.lume.Managers.PhoneDataStorageManager.deleteText;
import static ru.velkonost.lume.Managers.PhoneDataStorageManager.loadText;
import static ru.velkonost.lume.net.ServerConnection.getJSON;

/**
 * @author Velkonost
 *
 * Класс, предназначенный для самостоятельного изменения данных авторизованного пользователя.
 *
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int LAYOUT = R.layout.activity_settings;

    /**
     * Свойство - следующая активность.
     */
    private Intent nextIntent;

    /**
     * Свойство - описание верхней панели инструментов приложения.
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Свойство - описание {@link SearchActivity#LAYOUT}
     */
    @BindView(R.id.activity_settings)
    DrawerLayout drawerLayout;

    /**
     * Свойство - идентификатор пользователя, авторизованного на данном устройстве.
     */
    private String userId;

    /**
     * Свойство - экзмепляр класса {@link GetData}
     */
    protected GetData mGetData;

    /**
     * Свойство - экзмепляр класса {@link PostData}
     */
    protected PostData mPostData;

    /**
     * Свойство - элемент для выбора даты.
     */
    private DatePickerDialog dateBirdayDatePicker;

    /**
     * Свойство - отформатированная дата рождения, которую ввел пользователь.
     */
    protected String formattedBirthday;

    /**
     * Свойство - поля для ввода данных о пользователе, которые он желает изменить.
     */
    @BindView(R.id.editName)
    EditText editName; /** Имя пользователя */

    @BindView(R.id.editSurname)
    EditText editSurname; /** Фамилия пользователя */

    @BindView(R.id.editBirthday)
    TextView editBirthday; /** День рождения пользователя */

    @BindView(R.id.prevPassword)
    EditText prevPassword; /** Текущий пароль пользователя */

    @BindView(R.id.newPassword)
    EditText newPassword; /** Новый пароль пользователя */

    @BindView(R.id.editCity)
    EditText editCity; /** Город проживания пользователя */

    @BindView(R.id.editCountry)
    EditText editCountry; /** Страна проживания пользователя */

    @BindView(R.id.editStudy)
    EditText editStudy; /** Место учебы пользователя */

    @BindView(R.id.editWork)
    EditText editWork; /** Место работы пользователя */

    @BindView(R.id.editEmail)
    EditText editEmail; /** Основной email пользователя */

    @BindView(R.id.editWorkEmail)
    EditText editWorkEmail; /** email, который виден другим пользователям*/

    private Animation rotateArrowOpen, rotateArrowClose;

    @BindView(R.id.personalInfo)
    TextView personalInfo;

    @BindView(R.id.accountInfo)
    TextView accountInfo;

    @BindView(R.id.avatar)
    ImageView userAvatar;

    @BindView(R.id.login)
    TextView userLogin;

    @BindView(R.id.div_personal_header)
    LinearLayout divPersonalHeader;

    @BindView(R.id.div_account_header)
    LinearLayout divAccountHeader;

    @BindView(R.id.div_personal)
    LinearLayout divPersonal;

    @BindView(R.id.div_account)
    LinearLayout divAccount;

    private boolean personalOpen = false, accountOpen = false;

    @BindView(R.id.image_arrow_personal)
    ImageView imageArrowPersonal;

    @BindView(R.id.image_arrow_account)
    ImageView imageArrowAccount;

    @BindView(R.id.navigation)
    NavigationView navigationView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(LAYOUT);
        ButterKnife.bind(this);
        setTheme(R.style.AppTheme_Cursor);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/Roboto-Regular.ttf");

        userId = loadText(SettingsActivity.this, ID);

        /** Инициализация экземпляров классов */
        mGetData = new GetData();
        mPostData = new PostData();

        final LinearLayout.LayoutParams layoutParamsVisible
                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        final LinearLayout.LayoutParams layoutParamsInvisible
                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);

        divPersonal.setLayoutParams(layoutParamsInvisible);
        divAccount.setLayoutParams(layoutParamsInvisible);

        /** {@link InitializationsManager#initToolbar(Toolbar, int)}  */
        initToolbar(SettingsActivity.this, toolbar, getResources().getString(R.string.settings)); /** Инициализация */
        initNavigationView(); /** Инициализация */
        initDateBirthdayDatePicker(); /** Инициализация */

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mGetData.execute();

        rotateArrowOpen = AnimationUtils.loadAnimation(this, R.anim.arrow_rotation_open);
        rotateArrowClose = AnimationUtils.loadAnimation(this, R.anim.arrow_rotation_close);

        divPersonalHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (personalOpen) {
                    divPersonal.setLayoutParams(layoutParamsInvisible);
                    imageArrowPersonal.startAnimation(rotateArrowClose);

                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    getCurrentFocus().clearFocus();

                } else {
                    divPersonal.setLayoutParams(layoutParamsVisible);
                    imageArrowPersonal.startAnimation(rotateArrowOpen);

                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    getCurrentFocus().clearFocus();
                }

                personalOpen = !personalOpen;

            }
        });

        divAccountHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (accountOpen) {
                    divAccount.setLayoutParams(layoutParamsInvisible);
                    imageArrowAccount.startAnimation(rotateArrowClose);

                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    getCurrentFocus().clearFocus();
                } else {
                    divAccount.setLayoutParams(layoutParamsVisible);
                    imageArrowAccount.startAnimation(rotateArrowOpen);

                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    getCurrentFocus().clearFocus();
                }

                accountOpen = !accountOpen;

            }
        });



    }

    /**
     * Слушатель для даты рождения.
     **/
    public  void chooseDate(View w){
        switch (w.getId()){
            case R.id.editBirthday:
                /**
                 * Отображает календарь для выбора даты.
                 **/
                dateBirdayDatePicker.show();
                break;
        }
    }

    /**
     * Инициализация календаря.
     **/
    private void initDateBirthdayDatePicker(){
        /**
         * Использует для получения даты.
         */
        Calendar newCalendar = Calendar.getInstance();

        /**
         * Требуется для дальнейшего преобразования даты в строку.
         */
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat dateFormat
                = new SimpleDateFormat("dd-MM-yyyy");

        /**
         * Создает объект и инициализирует обработчиком события выбора даты и данными для даты по умолчанию.
         */
        dateBirdayDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            // функция onDateSet обрабатывает шаг 2: отображает выбранные нами данные в элементе EditText
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, monthOfYear, dayOfMonth);
                editBirthday.setText(dateFormat.format(newCal.getTime()));
            }
        },
                newCalendar.get(Calendar.YEAR),
                newCalendar.get(Calendar.MONTH),
                newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getGroupId();

        switch (id){
            case 0:
                /**
                 * Приводим дату к виду, в котором она хранится на сервере.
                 */
                formattedBirthday = formatDateBack(editBirthday.getText().toString());

                /**
                 * Отправляем данные на сервер.
                 */
                mPostData.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNavigationView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.view_navigation_open, R.string.view_navigation_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                getCurrentFocus().clearFocus();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                getCurrentFocus().clearFocus();
            }
        };

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View header = navigationView.getHeaderView(0);
        TextView navHeaderLogin = ButterKnife.findById(header, R.id.userNameHeader);
        ImageView askQuestion = ButterKnife.findById(header, R.id.askQuestion);

        navHeaderLogin.setText(loadText(SettingsActivity.this, LOGIN));


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
                        changeActivityCompat(SettingsActivity.this,
                                new Intent(SettingsActivity.this, FAQBotActivity.class));
                    }
                }, 350);

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

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
                        changeActivityCompat(SettingsActivity.this,
                                new Intent(SettingsActivity.this, ProfileActivity.class));
                    }
                }, 350);

                drawerLayout.closeDrawer(GravityCompat.START);

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                drawerLayout.closeDrawers();

                /** Инициализируем намерение на следующую активность */
                switch (menuItem.getItemId()) {

                    /** Переход на контакты данного пользователя */
                    case R.id.navigationContacts:
                        nextIntent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        break;

                    /** Переход на страницу сообщений данного пользователя */
                    case R.id.navigationMessages:
                        nextIntent = new Intent(SettingsActivity.this, DialogsActivity.class);
                        break;

                    /** Переход на страницу досок карточной версии канбан-системы */
                    case R.id.navigationBoards:
                        nextIntent = new Intent(SettingsActivity.this, BoardsListActivity.class);
                        break;

                    /** Переход на страницу индивидуальных настроек для данного пользователя */
                    case R.id.navigationSettings:
                        nextIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                        break;

                    /**
                     * Завершение сессии даного пользователя на данном устройстве.
                     * Удаляем всю информацию об авторизованном пользователе.
                     * Переход на страницу приветствия {@link WelcomeActivity}
                     **/
                    case R.id.navigationLogout:
                        deleteText(SettingsActivity.this, ID);
                        nextIntent = new Intent(SettingsActivity.this, WelcomeActivity.class);
                        break;
                }

                /**
                 * Переход на следующую активность.
                 * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                 * */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Обновляет страницу.
                         * {@link InitializationsManager#changeActivityCompat(Activity, Intent)}
                         * */
                        changeActivityCompat(SettingsActivity.this, nextIntent);
                    }
                }, 350);


                /** Если был осуществлен выход из аккаунта, то закрываем активность профиля */
                if (loadText(SettingsActivity.this, ID).equals(""))
                    finishAffinity();

                drawerLayout.closeDrawer(GravityCompat.START);

                return false;
            }
        });
    }

    /**
     * При нажатии на кнопку "Назад" поиск закрывется.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    /**
     * Класс для получения данных о пользователе с сервера.
     **/
    private class GetData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {

            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_ACCOUNT_SCRIPT
                    + SERVER_GET_DATA_SETTINGS_METHOD;

            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = USER_ID + EQUALS + userId;

            /** Свойство - код ответа, полученный от сервера */
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

            /**
             * Свойство - код ответа от методов сервера.
             *
             * ВНИМАНИЕ!
             *
             * Этот параметр не имеет никакого отношения к кодам состояния.
             * Он формируется на сервере в зависимости от результата проведения обработки данных.
             *
             **/
            int resultCode;

            /** Свойство - полученный JSON–объект*/
            final JSONObject dataJsonObj;

            try {

                /**
                 * Получение JSON-объекта по строке.
                 */
                dataJsonObj = new JSONObject(strJson);
                resultCode = Integer.parseInt(dataJsonObj.getString(GET_DATA_SETTINGS));

                /**
                 * Обработка полученного кода ответа.
                 */
                switch (resultCode) {
                    /** В случае успешного выполнения */
                    case 600:

                        /**
                         * Устанавливает в поля текущие данные пользователя.
                         */

                        editName.setText(dataJsonObj.getString(NAME));
                        editSurname.setText(dataJsonObj.getString(SURNAME));

                        editBirthday.setText(formatDate(dataJsonObj.getString(BIRTHDAY)));

                        editStudy.setText(dataJsonObj.getString(STUDY));
                        editWork.setText(dataJsonObj.getString(WORK));

                        editCity.setText(dataJsonObj.getString(CITY));
                        editCountry.setText(dataJsonObj.getString(COUNTRY));

                        editEmail.setText(dataJsonObj.getString(EMAIL));
                        editWorkEmail.setText(dataJsonObj.getString(WORK_EMAIL));

                        userLogin.setText(dataJsonObj.getString(LOGIN));

                        String avatarURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_RESOURCE
                                + SERVER_AVATAR + SLASH + dataJsonObj.getString(AVATAR)
                                + SLASH + userId + JPG;

                        fetchImage(avatarURL, userAvatar, true, false);

                        break;
                    /**
                     * Произошла неожиданная ошибка.
                     **/
                    case 601:
                        /**
                         * Формирование уведомления об ошибке.
                         */
                        inititializeAlertDialog(SettingsActivity.this,
                                getResources().getString(R.string.server_error),
                                getResources().getString(R.string.relogin),
                                getResources().getString(R.string.btn_ok));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Класс для отправки измененных данных о пользователе на сервер.
     */
    private class PostData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... strings) {


            /**
             * Формирование адреса, по которому необходимо обратиться.
             **/
            String dataURL = SERVER_PROTOCOL + SERVER_HOST + SERVER_ACCOUNT_SCRIPT
                    + SERVER_EDIT_PARAMETERS_METHOD;


            /**
             * Формирование отправных данных.
             */
            @SuppressWarnings("WrongThread") String params = USER_ID + EQUALS + userId
                    + AMPERSAND + NAME + EQUALS + editName.getText().toString()
                    + AMPERSAND + SURNAME + EQUALS + editSurname.getText().toString()
                    + AMPERSAND + CITY + EQUALS + editCity.getText().toString()
                    + AMPERSAND + COUNTRY + EQUALS + editCountry.getText().toString()
                    + AMPERSAND + STUDY + EQUALS + editStudy.getText().toString()
                    + AMPERSAND + WORK + EQUALS + editWork.getText().toString()
                    + AMPERSAND + EMAIL + EQUALS + editEmail.getText().toString()
                    + AMPERSAND + WORK_EMAIL + EQUALS + editWorkEmail.getText().toString()
                    + AMPERSAND + BIRTHDAY + EQUALS + formattedBirthday
                    + AMPERSAND + NEW_PASSWORD + EQUALS + newPassword.getText().toString()
                    + AMPERSAND + PREV_PASSWORD + EQUALS + prevPassword.getText().toString();

            /** Свойство - код ответа, полученный от сервера */
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

            /**
             * Свойство - код ответа от методов сервера.
             *
             * ВНИМАНИЕ!
             *
             * Этот параметр не имеет никакого отношения к кодам состояния.
             * Он формируется на сервере в зависимости от результата проведения обработки данных.
             *
             **/
            int resultCode;

            /** Свойство - полученный JSON–объект*/
            final JSONObject dataJsonObj;

            try {

                /**
                 * Получение JSON-объекта по строке.
                 */
                dataJsonObj = new JSONObject(strJson);
                resultCode = Integer.parseInt(dataJsonObj.getString(GET_EDIT_RESULT));

                /**
                 * Обработка полученного кода ответа.
                 */
                switch (resultCode) {
                    /** В случае успешного выполнения */
                    case 700:

                        /**
                         * Переходим в профиль.
                         * Изменения прошли успешно.
                         */
                        changeActivityCompat(SettingsActivity.this,
                                new Intent(SettingsActivity.this, ProfileActivity.class));

                        break;
                    /**
                     * При попытке сменить пароль, текущий пароль был указан неверно.
                     * Изменение не вступили в силу.
                     **/
                    case 701:
                        /**
                         * Формирование уведомления об ошибке.
                         */
                        inititializeAlertDialogWithRefresh(SettingsActivity.this,
                                getResources().getString(R.string.password_error),
                                getResources().getString(R.string.refill_password_field),
                                getResources().getString(R.string.btn_ok),
                                SettingsActivity.this);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
