package com.alomran.tactics;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.InputType;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int DARK = Color.rgb(6, 18, 13);
    private final int GOLD = Color.rgb(216, 180, 90);
    private final int PANEL = Color.rgb(10, 30, 35);

    private LinearLayout root;
    private FormationView formationView;
    private SharedPreferences sp;

    private String[] names = {
            "ابو الليث", "حسونة", "عبدالله", "عبدالخالق",
            "سلوم", "جليبيب", "ابو احمد", "يوسف"
    };
    private String substitute = "مصطفى عثمان";
    private boolean showArrows = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("alomran_data", MODE_PRIVATE);
        loadData();
        showMainScreen();
    }

    private void loadData() {
        for (int i = 0; i < names.length; i++) {
            names[i] = sp.getString("p" + i, names[i]);
        }
        substitute = sp.getString("substitute", substitute);
        showArrows = sp.getBoolean("showArrows", false);
    }

    private void saveData() {
        SharedPreferences.Editor e = sp.edit();
        for (int i = 0; i < names.length; i++) e.putString("p" + i, names[i]);
        e.putString("substitute", substitute);
        e.putBoolean("showArrows", showArrows);
        e.apply();
    }

    private TextView tv(String text, int size, int color, int style) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT_BOLD, style);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }

    private Button btn(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(12, 55, 58));
        b.setPadding(dp(4), dp(6), dp(4), dp(6));
        return b;
    }

    private void baseRoot() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(DARK);
        root.setPadding(dp(8), dp(8), dp(8), dp(8));
        setContentView(root);
    }

    private void addTabs(String active) {
        TextView title = tv("العمران", 24, Color.WHITE, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        String[] labels = {"الخطة", "الديربي", "تعديل"};
        for (String s : labels) {
            Button b = btn(s.equals(active) ? "● " + s : s);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1);
            lp.setMargins(dp(3), dp(3), dp(3), dp(6));
            tabs.addView(b, lp);
            if (s.equals("الخطة")) b.setOnClickListener(v -> showMainScreen());
            if (s.equals("الديربي")) b.setOnClickListener(v -> showDerbyScreen());
            if (s.equals("تعديل")) b.setOnClickListener(v -> showEditScreen());
        }
        root.addView(tabs, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showMainScreen() {
        loadData();
        baseRoot();
        addTabs("الخطة");

        formationView = new FormationView(this);
        formationView.setData(names, substitute, showArrows, true);
        root.addView(formationView, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button arrows = btn(showArrows ? "إخفاء الأسهم" : "إظهار الأسهم");
        arrows.setOnClickListener(v -> {
            showArrows = !showArrows;
            saveData();
            formationView.setData(names, substitute, showArrows, true);
            formationView.invalidate();
            showMainScreen();
        });

        Button save = btn("حفظ كصورة");
        save.setOnClickListener(v -> savePoster());

        actions.addView(arrows, new LinearLayout.LayoutParams(0, dp(50), 1));
        actions.addView(save, new LinearLayout.LayoutParams(0, dp(50), 1));
        root.addView(actions, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showEditScreen() {
        loadData();
        baseRoot();
        addTabs("تعديل");

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(8), dp(8), dp(24));
        sv.addView(box);

        box.addView(tv("تعديل أسماء اللاعبين", 20, Color.WHITE, Typeface.BOLD));

        String[] labels = {
                "الحارس", "مدافع يسار", "مدافع وسط", "مدافع يمين",
                "وسط متقدم يسار", "أمام المدافعين", "وسط متقدم يمين", "رأس حربة"
        };

        EditText[] inputs = new EditText[8];
        for (int i = 0; i < 8; i++) {
            TextView label = tv(labels[i], 15, GOLD, Typeface.BOLD);
            label.setGravity(Gravity.RIGHT);
            box.addView(label);
            EditText in = input(names[i]);
            inputs[i] = in;
            box.addView(in, new LinearLayout.LayoutParams(-1, dp(52)));
        }

        TextView subLabel = tv("البدلاء", 15, GOLD, Typeface.BOLD);
        subLabel.setGravity(Gravity.RIGHT);
        box.addView(subLabel);
        EditText subInput = input(substitute);
        box.addView(subInput, new LinearLayout.LayoutParams(-1, dp(52)));

        CheckBox cb = new CheckBox(this);
        cb.setText("إظهار أسهم التحركات");
        cb.setTextColor(Color.WHITE);
        cb.setTextSize(16);
        cb.setChecked(showArrows);
        box.addView(cb);

        Button save = btn("حفظ التعديلات");
        save.setOnClickListener(v -> {
            for (int i = 0; i < 8; i++) {
                names[i] = inputs[i].getText().toString().trim();
                if (names[i].isEmpty()) names[i] = labels[i];
            }
            substitute = subInput.getText().toString().trim();
            if (substitute.isEmpty()) substitute = "مصطفى عثمان";
            showArrows = cb.isChecked();
            saveData();
            hideKeyboard();
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show();
            showMainScreen();
        });
        box.addView(save, new LinearLayout.LayoutParams(-1, dp(56)));

        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private EditText input(String value) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setTextColor(Color.WHITE);
        e.setTextSize(17);
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setPadding(dp(12), 0, dp(12), 0);
        e.setBackgroundColor(Color.rgb(15, 45, 44));
        return e;
    }

    private void showDerbyScreen() {
        loadData();
        baseRoot();
        addTabs("الديربي");

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(8), dp(8), dp(28));
        sv.addView(box);

        box.addView(tv("قسم الديربي", 22, Color.WHITE, Typeface.BOLD));
        box.addView(tv("أضف نتيجة مباراة، وستظهر مع التشكيلة الحالية.", 14, Color.LTGRAY, Typeface.NORMAL));

        EditText opponent = input("اسم الفريق المنافس");
        EditText our = input("أهداف العمران");
        EditText their = input("أهداف المنافس");
        EditText date = input(new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()));

        our.setInputType(InputType.TYPE_CLASS_NUMBER);
        their.setInputType(InputType.TYPE_CLASS_NUMBER);

        box.addView(opponent, new LinearLayout.LayoutParams(-1, dp(52)));
        box.addView(our, new LinearLayout.LayoutParams(-1, dp(52)));
        box.addView(their, new LinearLayout.LayoutParams(-1, dp(52)));
        box.addView(date, new LinearLayout.LayoutParams(-1, dp(52)));

        Button add = btn("إضافة نتيجة الديربي");
        add.setOnClickListener(v -> {
            String op = opponent.getText().toString().trim();
            String o = our.getText().toString().trim();
            String t = their.getText().toString().trim();
            String d = date.getText().toString().trim();
            if (op.isEmpty() || o.isEmpty() || t.isEmpty()) {
                Toast.makeText(this, "أدخل اسم المنافس والنتيجة", Toast.LENGTH_SHORT).show();
                return;
            }
            String record = d + " | العمران " + o + " - " + t + " " + op + " | التشكيلة: " +
                    names[0] + "، " + names[1] + "، " + names[2] + "، " + names[3] + "، " +
                    names[5] + "، " + names[4] + "، " + names[6] + "، " + names[7] +
                    " | البديل: " + substitute;
            String old = sp.getString("matches", "");
            sp.edit().putString("matches", record + "\n" + old).apply();
            Toast.makeText(this, "تمت إضافة النتيجة", Toast.LENGTH_SHORT).show();
            hideKeyboard();
            showDerbyScreen();
        });
        box.addView(add, new LinearLayout.LayoutParams(-1, dp(56)));

        FormationView small = new FormationView(this);
        small.setData(names, substitute, false, false);
        box.addView(small, new LinearLayout.LayoutParams(-1, dp(620)));

        box.addView(tv("نتائج الديربي", 20, GOLD, Typeface.BOLD));
        String matches = sp.getString("matches", "").trim();
        if (matches.isEmpty()) {
            box.addView(tv("لا توجد نتائج مضافة بعد", 15, Color.LTGRAY, Typeface.NORMAL));
        } else {
            String[] arr = matches.split("\\n");
            for (String m : arr) {
                TextView card = tv(m, 15, Color.WHITE, Typeface.BOLD);
                card.setGravity(Gravity.RIGHT);
                card.setBackgroundColor(PANEL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.setMargins(0, dp(5), 0, dp(5));
                box.addView(card, lp);
            }
        }

        Button clear = btn("مسح نتائج الديربي");
        clear.setOnClickListener(v -> {
            sp.edit().remove("matches").apply();
            showDerbyScreen();
        });
        box.addView(clear, new LinearLayout.LayoutParams(-1, dp(52)));

        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void savePoster() {
        try {
            Bitmap bitmap = Bitmap.createBitmap(1080, 1600, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            FormationView.Drawer.drawPoster(canvas, 1080, 1600, names, substitute, showArrows, true);

            String filename = "AlOmran_Tactics_" + System.currentTimeMillis() + ".png";

            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AlOmran");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("Cannot create image");
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                if (out != null) out.close();
            } else {
                File dir = new File(getExternalFilesDir(null), "AlOmran");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }

            Toast.makeText(this, "تم حفظ الصورة في الاستديو / Pictures/AlOmran", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, "تعذر حفظ الصورة: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (imm != null && view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class FormationView extends View {
        private String[] names = new String[8];
        private String substitute = "مصطفى عثمان";
        private boolean showArrows = false;
        private boolean showFooter = true;

        public FormationView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        public void setData(String[] n, String sub, boolean arrows, boolean footer) {
            for (int i = 0; i < 8; i++) names[i] = n[i];
            substitute = sub;
            showArrows = arrows;
            showFooter = footer;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Drawer.drawPoster(canvas, getWidth(), getHeight(), names, substitute, showArrows, showFooter);
        }

        public static class Drawer {
            static int dark = Color.rgb(6, 18, 13);
            static int gold = Color.rgb(216, 180, 90);
            static int blue1 = Color.rgb(9, 78, 190);
            static int blue2 = Color.rgb(0, 34, 110);

            static void drawPoster(Canvas c, int w, int h, String[] names, String sub, boolean arrows, boolean footer) {
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.FILL);
                p.setColor(dark);
                c.drawRect(0, 0, w, h, p);

                RectF pitch = new RectF(w * 0.08f, h * 0.16f, w * 0.92f, footer ? h * 0.83f : h * 0.94f);
                drawTitle(c, w, h, p);
                drawStadium(c, pitch, p);
                drawPitch(c, pitch, p);
                drawLogo(c, w * 0.50f, h * 0.115f, Math.min(w, h) * 0.050f, p, false);

                Pos[] pos = positions(pitch);
                if (arrows) drawMovementArrows(c, pos, p);

                int[] nums = {1,2,3,4,5,6,7,8};
                for (int i = 0; i < 8; i++) {
                    drawPlayer(c, pos[i].x, pos[i].y, nums[i], names[i], p);
                }

                if (footer) {
                    drawFooter(c, w, h, sub, p);
                }
            }

            static Pos[] positions(RectF pitch) {
                return new Pos[] {
                        P(pitch, .50f, .90f),
                        P(pitch, .23f, .72f),
                        P(pitch, .50f, .72f),
                        P(pitch, .77f, .72f),
                        P(pitch, .30f, .49f),
                        P(pitch, .50f, .58f),
                        P(pitch, .70f, .49f),
                        P(pitch, .50f, .27f)
                };
            }

            static Pos P(RectF r, float x, float y) {
                return new Pos(r.left + r.width() * x, r.top + r.height() * y);
            }

            static void drawTitle(Canvas c, int w, int h, Paint p) {
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(h * 0.040f);
                p.setColor(Color.WHITE);
                c.drawText("خطة 3-3-1", w / 2f, h * 0.072f, p);

                p.setStrokeWidth(3);
                p.setColor(gold);
                c.drawLine(w * .18f, h * .055f, w * .36f, h * .055f, p);
                c.drawLine(w * .64f, h * .055f, w * .82f, h * .055f, p);
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(w * .36f, h * .055f, 4, p);
                c.drawCircle(w * .64f, h * .055f, 4, p);
            }

            static void drawStadium(Canvas c, RectF pitch, Paint p) {
                RectF outer = new RectF(pitch.left - pitch.width()*0.07f, pitch.top - pitch.height()*0.07f,
                        pitch.right + pitch.width()*0.07f, pitch.bottom + pitch.height()*0.04f);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(235, 229, 216));
                c.drawRoundRect(outer, 8, 8, p);

                p.setColor(Color.rgb(120, 24, 22));
                c.drawRoundRect(new RectF(pitch.left - 12, pitch.top - 12, pitch.right + 12, pitch.bottom + 12), 5, 5, p);

                // top stand
                RectF stand = new RectF(pitch.left, outer.top + 15, pitch.right, pitch.top - 18);
                p.setColor(Color.rgb(45, 25, 36));
                c.drawRoundRect(stand, 6, 6, p);
                p.setColor(Color.rgb(150, 90, 110));
                for (int i=0; i<4; i++) c.drawRect(stand.left, stand.top + i*stand.height()/5f, stand.right, stand.top + i*stand.height()/5f + 5, p);

                // small buildings around stadium
                p.setColor(Color.rgb(215, 210, 202));
                c.drawRoundRect(new RectF(pitch.left + pitch.width()*0.44f, outer.top, pitch.left + pitch.width()*0.56f, pitch.top - 5), 6,6,p);
                c.drawRoundRect(new RectF(pitch.right + 18, pitch.top + pitch.height()*0.40f, pitch.right + 62, pitch.top + pitch.height()*0.48f), 6,6,p);
                p.setColor(Color.rgb(80, 32, 40));
                c.drawRoundRect(new RectF(pitch.right - pitch.width()*0.06f, outer.top + 22, pitch.right + 45, pitch.top - 12), 5,5,p);

                // tiled texture outside
                p.setColor(Color.argb(55, 70,70,70));
                p.setStrokeWidth(1);
                for (float x=outer.left; x<outer.right; x+=24) c.drawLine(x, outer.top, x, outer.bottom, p);
                for (float y=outer.top; y<outer.bottom; y+=24) c.drawLine(outer.left, y, outer.right, y, p);
                p.setStyle(Paint.Style.FILL);
            }

            static void drawPitch(Canvas c, RectF r, Paint p) {
                // grass base, realistic stripes and checker patches
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(22, 128, 24));
                c.drawRect(r, p);

                int cols = 12;
                for (int i = 0; i < cols; i++) {
                    p.setColor(i % 2 == 0 ? Color.rgb(26, 143, 27) : Color.rgb(18, 115, 22));
                    c.drawRect(r.left + i * r.width()/cols, r.top, r.left + (i+1)*r.width()/cols, r.bottom, p);
                }

                int rows = 11;
                for (int i = 0; i < cols; i++) {
                    for (int j = 0; j < rows; j++) {
                        boolean light = (i + j) % 2 == 0;
                        p.setColor(light ? Color.argb(24, 75, 190, 55) : Color.argb(20, 0, 55, 0));
                        c.drawRect(r.left + i*r.width()/cols, r.top + j*r.height()/rows,
                                r.left + (i+1)*r.width()/cols, r.top + (j+1)*r.height()/rows, p);
                    }
                }

                // pitch lines
                p.setColor(Color.WHITE);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(3, r.width()*0.004f));
                c.drawRect(r, p);
                c.drawLine(r.left, r.centerY(), r.right, r.centerY(), p);
                c.drawCircle(r.centerX(), r.centerY(), r.width()*0.105f, p);

                // boxes
                float boxW = r.width()*0.34f, boxH = r.height()*0.12f;
                RectF topBox = new RectF(r.centerX()-boxW/2, r.top, r.centerX()+boxW/2, r.top+boxH);
                RectF botBox = new RectF(r.centerX()-boxW/2, r.bottom-boxH, r.centerX()+boxW/2, r.bottom);
                c.drawRect(topBox, p);
                c.drawRect(botBox, p);

                float smallW = r.width()*0.18f, smallH = r.height()*0.055f;
                c.drawRect(r.centerX()-smallW/2, r.top, r.centerX()+smallW/2, r.top+smallH, p);
                c.drawRect(r.centerX()-smallW/2, r.bottom-smallH, r.centerX()+smallW/2, r.bottom, p);

                // goals
                p.setStrokeWidth(2);
                c.drawRect(r.centerX()-r.width()*0.055f, r.top-14, r.centerX()+r.width()*0.055f, r.top, p);
                c.drawRect(r.centerX()-r.width()*0.055f, r.bottom, r.centerX()+r.width()*0.055f, r.bottom+14, p);

                // corner arcs
                float cr = r.width()*0.035f;
                c.drawArc(new RectF(r.left-cr, r.top-cr, r.left+cr, r.top+cr), 0, 90, false, p);
                c.drawArc(new RectF(r.right-cr, r.top-cr, r.right+cr, r.top+cr), 90, 90, false, p);
                c.drawArc(new RectF(r.left-cr, r.bottom-cr, r.left+cr, r.bottom+cr), 270, 90, false, p);
                c.drawArc(new RectF(r.right-cr, r.bottom-cr, r.right+cr, r.bottom+cr), 180, 90, false, p);

                p.setStyle(Paint.Style.FILL);
            }

            static void drawLogo(Canvas c, float cx, float cy, float rad, Paint p, boolean bigText) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(5, 22, 28));
                c.drawCircle(cx, cy, rad, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(rad*0.08f);
                p.setColor(gold);
                c.drawCircle(cx, cy, rad*0.88f, p);
                p.setStrokeWidth(rad*0.06f);
                c.drawLine(cx-rad*.45f, cy-rad*.25f, cx+rad*.42f, cy-rad*.25f, p);
                c.drawLine(cx-rad*.48f, cy, cx+rad*.35f, cy, p);
                c.drawLine(cx-rad*.30f, cy+rad*.25f, cx+rad*.45f, cy+rad*.25f, p);
                p.setStyle(Paint.Style.FILL);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(rad*.38f);
                p.setColor(Color.WHITE);
                c.drawText("العمران", cx, cy + rad*1.25f, p);
            }

            static void drawPlayer(Canvas c, float x, float y, int num, String name, Paint p) {
                float r = 26;
                p.setShader(new RadialGradient(x-r/3, y-r/3, r*1.4f, blue1, blue2, Shader.TileMode.CLAMP));
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(x, y, r, p);
                p.setShader(null);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(3);
                p.setColor(Color.WHITE);
                c.drawCircle(x, y, r, p);

                p.setStyle(Paint.Style.FILL);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setColor(Color.WHITE);
                p.setTextSize(24);
                c.drawText(String.valueOf(num), x, y + 9, p);

                // name card
                RectF card = new RectF(x - 62, y + 30, x + 62, y + 64);
                p.setColor(Color.rgb(6, 24, 30));
                c.drawRoundRect(card, 8, 8, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                p.setColor(gold);
                c.drawRoundRect(card, 8, 8, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                p.setTextSize(18);
                c.drawText(name, x, y + 54, p);
            }

            static void drawFooter(Canvas c, int w, int h, String sub, Paint p) {
                float y = h * .865f;

                RectF subBox = new RectF(w * .60f, y, w * .88f, h * .965f);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(5, 24, 30));
                c.drawRoundRect(subBox, 10, 10, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                p.setColor(gold);
                c.drawRoundRect(subBox, 10, 10, p);
                p.setStyle(Paint.Style.FILL);
                p.setTextAlign(Paint.Align.CENTER);
                p.setColor(Color.WHITE);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(22);
                c.drawText("البدلاء", subBox.centerX(), subBox.top + 32, p);
                drawMiniNumber(c, subBox.left + 48, subBox.top + 68, 12, p);
                p.setTextSize(19);
                c.drawText(sub, subBox.centerX() + 20, subBox.top + 75, p);

                RectF key = new RectF(w * .08f, y, w * .38f, h * .965f);
                p.setColor(Color.rgb(5, 24, 30));
                c.drawRoundRect(key, 10, 10, p);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(gold);
                c.drawRoundRect(key, 10, 10, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                p.setTextSize(20);
                c.drawText("مفتاح المراكز", key.centerX(), key.top + 32, p);
                p.setTextSize(16);
                p.setTextAlign(Paint.Align.RIGHT);
                c.drawText("حارس مرمى     1", key.right - 25, key.top + 62, p);
                c.drawText("مدافعون         3", key.right - 25, key.top + 88, p);
                c.drawText("وسط            3", key.right - 25, key.top + 114, p);
                c.drawText("مهاجم           1", key.right - 25, key.top + 140, p);

                drawLogo(c, w*.50f, h*.915f, Math.min(w,h)*.04f, p, true);
            }

            static void drawMiniNumber(Canvas c, float x, float y, int num, Paint p) {
                p.setStyle(Paint.Style.FILL);
                p.setShader(new RadialGradient(x, y, 22, blue1, blue2, Shader.TileMode.CLAMP));
                c.drawCircle(x, y, 22, p);
                p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.WHITE);
                p.setStrokeWidth(2);
                c.drawCircle(x, y, 22, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(18);
                c.drawText(String.valueOf(num), x, y + 7, p);
            }

            static void drawMovementArrows(Canvas c, Pos[] pos, Paint p) {
                // Yellow = attack movement, Red dashed = defensive return
                int yellow = Color.rgb(255, 215, 32);
                int red = Color.rgb(255, 70, 45);

                // GK support
                arrow(c, pos[0].x, pos[0].y-10, pos[0].x-70, pos[0].y-90, yellow, false, false, p);
                arrow(c, pos[0].x, pos[0].y-10, pos[0].x+70, pos[0].y-90, yellow, false, false, p);

                // defenders
                arrow(c, pos[1].x-35, pos[1].y-20, pos[1].x-40, pos[1].y-170, yellow, false, false, p);
                curve(c, pos[1].x+15, pos[1].y-25, pos[1].x+80, pos[1].y-120, yellow, false, p);
                curve(c, pos[1].x+15, pos[1].y+30, pos[1].x+85, pos[1].y+105, red, true, p);

                arrow(c, pos[2].x, pos[2].y-40, pos[2].x, pos[5].y+42, yellow, false, false, p);
                arrow(c, pos[2].x, pos[2].y-5, pos[2].x, pos[0].y-58, red, true, false, p);

                arrow(c, pos[3].x+35, pos[3].y-20, pos[3].x+40, pos[3].y-170, yellow, false, false, p);
                curve(c, pos[3].x-15, pos[3].y-25, pos[3].x-80, pos[3].y-120, yellow, false, p);
                curve(c, pos[3].x-15, pos[3].y+30, pos[3].x-85, pos[3].y+105, red, true, p);

                // CDM and mids
                arrow(c, pos[5].x, pos[5].y-35, pos[5].x, pos[5].y-170, yellow, false, false, p);
                curve(c, pos[5].x-25, pos[5].y-10, pos[5].x-100, pos[5].y-60, yellow, false, p);
                curve(c, pos[5].x+25, pos[5].y-10, pos[5].x+100, pos[5].y-60, yellow, false, p);
                arrow(c, pos[5].x, pos[5].y+35, pos[5].x, pos[2].y-45, red, true, false, p);

                arrow(c, pos[4].x, pos[4].y-35, pos[4].x-15, pos[4].y-165, yellow, false, false, p);
                curve(c, pos[4].x-45, pos[4].y-10, pos[4].x-95, pos[4].y-115, yellow, false, p);
                curve(c, pos[4].x+30, pos[4].y-10, pos[4].x+95, pos[4].y-90, yellow, false, p);
                arrow(c, pos[4].x, pos[4].y+30, pos[4].x+10, pos[4].y+95, red, true, false, p);

                arrow(c, pos[6].x, pos[6].y-35, pos[6].x+15, pos[6].y-165, yellow, false, false, p);
                curve(c, pos[6].x+45, pos[6].y-10, pos[6].x+95, pos[6].y-115, yellow, false, p);
                curve(c, pos[6].x-30, pos[6].y-10, pos[6].x-95, pos[6].y-90, yellow, false, p);
                arrow(c, pos[6].x, pos[6].y+30, pos[6].x-10, pos[6].y+95, red, true, false, p);

                // striker
                arrow(c, pos[7].x, pos[7].y-40, pos[7].x, pos[7].y-150, yellow, false, false, p);
                curve(c, pos[7].x-40, pos[7].y-12, pos[7].x-140, pos[7].y-75, yellow, false, p);
                curve(c, pos[7].x+40, pos[7].y-12, pos[7].x+140, pos[7].y-75, yellow, false, p);
                arrow(c, pos[7].x, pos[7].y+32, pos[7].x, pos[7].y+100, red, true, false, p);
            }

            static void arrow(Canvas c, float x1, float y1, float x2, float y2, int color, boolean dashed, boolean thin, Paint p) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(thin ? 3 : 5);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setColor(color);
                p.setPathEffect(dashed ? new DashPathEffect(new float[]{12,10}, 0) : null);
                c.drawLine(x1,y1,x2,y2,p);
                p.setPathEffect(null);
                drawHead(c, x1,y1,x2,y2,color,p);
                p.setStyle(Paint.Style.FILL);
            }

            static void curve(Canvas c, float x1, float y1, float x2, float y2, int color, boolean dashed, Paint p) {
                Path path = new Path();
                path.moveTo(x1, y1);
                float cx = (x1 + x2) / 2f + (x2 > x1 ? 25 : -25);
                float cy = (y1 + y2) / 2f;
                path.quadTo(cx, cy, x2, y2);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(5);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setColor(color);
                p.setPathEffect(dashed ? new DashPathEffect(new float[]{12,10}, 0) : null);
                c.drawPath(path, p);
                p.setPathEffect(null);
                drawHead(c, cx,cy,x2,y2,color,p);
                p.setStyle(Paint.Style.FILL);
            }

            static void drawHead(Canvas c, float x1, float y1, float x2, float y2, int color, Paint p) {
                double angle = Math.atan2(y2-y1, x2-x1);
                float len = 18;
                Path head = new Path();
                head.moveTo(x2, y2);
                head.lineTo((float)(x2 - len*Math.cos(angle - Math.PI/6)), (float)(y2 - len*Math.sin(angle - Math.PI/6)));
                head.lineTo((float)(x2 - len*Math.cos(angle + Math.PI/6)), (float)(y2 - len*Math.sin(angle + Math.PI/6)));
                head.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(color);
                c.drawPath(head, p);
            }
        }

        static class Pos {
            float x, y;
            Pos(float xx, float yy) { x = xx; y = yy; }
        }
    }
}
