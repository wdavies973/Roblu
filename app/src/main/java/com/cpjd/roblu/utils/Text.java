package com.cpjd.roblu.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cpjd.roblu.R;
import com.cpjd.roblu.cloud.api.CloudRequest;
import com.cpjd.roblu.forms.elements.EBoolean;
import com.cpjd.roblu.forms.elements.ECheckbox;
import com.cpjd.roblu.forms.elements.EChooser;
import com.cpjd.roblu.forms.elements.ECounter;
import com.cpjd.roblu.forms.elements.EGallery;
import com.cpjd.roblu.forms.elements.ESTextfield;
import com.cpjd.roblu.forms.elements.ESlider;
import com.cpjd.roblu.forms.elements.EStopwatch;
import com.cpjd.roblu.forms.elements.ETextfield;
import com.cpjd.roblu.forms.elements.Element;
import com.cpjd.roblu.models.Loader;
import com.cpjd.roblu.models.REvent;
import com.cpjd.roblu.models.RForm;
import com.cpjd.roblu.models.RSettings;
import com.cpjd.roblu.models.RTab;
import com.cpjd.roblu.models.RTeam;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.ACTIVITY_SERVICE;

/*******************************************************
 * Copyright (C) 2016 Will Davies wdavies973@gmail.com
 *
 * This file is part of Roblu
 *
 * Roblu cannot be distributed for a price or to people outside of your local robotics team.
 *******************************************************/

/**
 * Utility library for odds and ends functions.
 *
 * @since 1.0.0
 * @author Will Davies
 */
public class Text {

    private static int width;

    public static void initWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void setInputTextLayoutColor(final int accent, final int text, TextInputLayout textInputLayout, final AppCompatEditText edit) {
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AppCompatEditText edit2 = (AppCompatEditText)v;
                if(hasFocus) edit2.setSupportBackgroundTintList(ColorStateList.valueOf(accent));
                else edit2.setSupportBackgroundTintList(ColorStateList.valueOf(text));
            }
        });
        setCursorColor(edit, accent);

        try {
            if(textInputLayout == null) return;

            Field field = textInputLayout.getClass().getDeclaredField("mFocusedTextColor");
            field.setAccessible(true);
            int[][] states = new int[][]{
                    new int[]{}
            };
            int[] colors = new int[]{
                    accent
            };
            ColorStateList myList = new ColorStateList(states, colors);
            field.set(textInputLayout, myList);

            Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            fDefaultTextColor.setAccessible(true);
            fDefaultTextColor.set(textInputLayout, myList);

            Method method = textInputLayout.getClass().getDeclaredMethod("updateLabelState", boolean.class);
            method.setAccessible(true);
            method.invoke(textInputLayout, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCursorColor(AppCompatEditText view, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }
    }

    public static String concatenateArraylist(ArrayList<String> data) {
        if(data == null || data.size() == 0) return "";
        String temp = "";
        for(String s : data) temp += s +"\n";
        return temp;
    }

    public static int getWidth() {
        return width;
    }

    public static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static void showSnackbar(View layout, Context context, String text, boolean error, int primary) {
        Snackbar s = Snackbar.make(layout, text, Snackbar.LENGTH_LONG);
        if(error) s.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        else s.getView().setBackgroundColor(primary);
        s.show();
    }

    public static RForm createEmpty() {
        ArrayList<Element> pit = new ArrayList<>();
        ArrayList<Element> matches = new ArrayList<>();
        ESTextfield name = new ESTextfield("Team name", false);
        ESTextfield number = new ESTextfield("Team number", true);
        name.setID(0); number.setID(1);
        pit.add(name);
        pit.add(number);
        return new RForm(pit, matches);
    }

    public static String guessMatchKey(String matchName) {
        matchName = matchName.toLowerCase();
        if(matchName.startsWith("quals")) return "qm"+matchName.split("\\s+")[1];
        else if(matchName.startsWith("quarters")) return "qf"+matchName.split("\\s+")[1]+"m"+matchName.split("\\s+")[3];
        else if(matchName.startsWith("semis")) return "sf"+matchName.split("\\s+")[1] +"m"+matchName.split("\\s+")[3];
        else if(matchName.startsWith("finals")) return "f1"+matchName.split("\\s+")[1];
        return "";
    }

    /*
     * Used for sorting matches
     */
    public static long getMatchScore(String name) {
        long score = 0;
        String matchName = name.toLowerCase();
        String[] tokens = matchName.split("\\s+");

        // let's give the local match a score
        if(matchName.startsWith("pit")) score -= 100000;
        else if(matchName.startsWith("predictions")) score -= 1000;
        else if(matchName.startsWith("quals")) score = Integer.parseInt(matchName.split("\\s+")[1]);
        else if(matchName.startsWith("quarters")) {
            if(Integer.parseInt(tokens[1]) == 1) score += 1000;
            else if(Integer.parseInt(tokens[1]) == 2) score += 10000;
            else if(Integer.parseInt(tokens[1]) == 3) score += 100000;
            else if(Integer.parseInt(tokens[1]) == 4) score += 1000000;

            score += Integer.parseInt(tokens[3]);
        }
        else if(matchName.startsWith("semis")) {
            if(Integer.parseInt(tokens[1]) == 1) score += 10000000;
            else if(Integer.parseInt(tokens[1]) == 2) score += 100000000;

            score += Integer.parseInt(tokens[3]);
        }
        else if(matchName.startsWith("finals")) {
            score += 1000000000; // d a b, perfect coding right here
            score += Integer.parseInt(tokens[1]);
        }
        return score;
    }

    public static ArrayList<Element> createNew(ArrayList<Element> elements) {
        ArrayList<Element> newElements = new ArrayList<>();
        for(Element e : elements) {
            newElements.add(createNew(e));
        }
        return newElements;
    }

    public static Element createNew(Element e) {
        Element t = null;
        if(e instanceof EBoolean) {
            t = new EBoolean(e.getTitle(), ((EBoolean) e).getValue());
        }
        else if(e instanceof ECheckbox) {
            t = new ECheckbox(e.getTitle(), ((ECheckbox) e).getValues(), ((ECheckbox) e).getChecked());
        }
        else if(e instanceof ETextfield) {
            t = new ETextfield(e.getTitle(), ((ETextfield) e).getText());
        }
        else if(e instanceof EStopwatch) {
            t = new EStopwatch(e.getTitle(), ((EStopwatch) e).getTime());
        }
        else if(e instanceof ECounter) {
            t = new ECounter(e.getTitle(), ((ECounter) e).getMin(), ((ECounter) e).getMax(), ((ECounter) e).getIncrement(), ((ECounter) e).getCurrent());
        }
        else if(e instanceof EChooser) {
            t = new EChooser(e.getTitle(), ((EChooser) e).getValues(), ((EChooser) e).getSelected());
        }
        else if(e instanceof ESlider) {
            t = new ESlider(e.getTitle(), ((ESlider) e).getMax(), ((ESlider) e).getCurrent());
        } else if(e instanceof ESTextfield) {
            t = new ESTextfield(e.getTitle(), ((ESTextfield) e).isNumberOnly());
        }
        else if(e instanceof EGallery) {
            t = new EGallery(e.getTitle());
        }
        if(t != null) t.setID(e.getID());
        return t;
    }
    public static String concantenteTeams(ArrayList<RTeam> teams) {
        if(teams == null || teams.size() == 0) return "";

        String temp = "";
        for(int i = 0; i < teams.size(); i++) {
            if(i != teams.size() - 1) temp += "#"+teams.get(i).getNumber()+", ";
            else temp += "#"+teams.get(i).getNumber();
        }
        return temp;
    }
	public static boolean launchEventPicker(Context context, final EventSelectListener listener) {
        final Dialog d = new Dialog(context);
        d.setTitle("Pick event:");
        d.setContentView(R.layout.event_import_dialog);
        final Spinner spinner = (Spinner) d.findViewById(R.id.type);
        String[] values;
        final REvent[] events = new Loader(context).getEvents();
        if(events == null || events.length == 0) return false;
        values = new String[events.length];
        for(int i = 0; i < values.length; i++) {
            values[i] = events[i].getName();
        }
        ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, values);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);

        Button button = (Button) d.findViewById(R.id.button7);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.eventSelected(events[spinner.getSelectedItemPosition()].getID());
                d.dismiss();
            }
        });
        if(d.getWindow() != null) d.getWindow().getAttributes().windowAnimations = new Loader(context).loadSettings().getRui().getAnimation();
        d.show();
        return true;
    }

	// Converts unix millisecond time into a human readable time
	public static String convertTime(long timeMillis) {
        if(timeMillis == 0) return "Never";
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());    
		Date resultdate = new Date(timeMillis);
		return sdf.format(resultdate);

	}

	public static String convertTimeOnly(long timeMillis) {
        if(timeMillis == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Date resultdate = new Date(timeMillis);
        return sdf.format(resultdate);
    }

	public static ArrayList<RTab> createNewTabs(ArrayList<RTab> tabs) {
        if(tabs == null || tabs.size() == 0) return tabs;
        ArrayList<RTab> toReturn = new ArrayList<>();
        for(int i = 0; i < tabs.size(); i++) toReturn.add(tabs.get(i).duplicate());
        return toReturn;
    }

	public static String getDay(int dayOfWeek) {
		return Constants.daysOfWeek[dayOfWeek];
	}
	
	public static String getMonth(int month) {
		return Constants.monthsOfYear[month];
	}

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static int DPToPX(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    // with whitespace
    public static boolean contains(String string, String query) {
        if(string.equals(query)) return false;
        if(!string.contains(query)) return false;
        else if(string.indexOf(query) == 0 && string.length() > query.length()) return string.charAt(query.length()) == ' ';
        else if(string.indexOf(query) == string.length() - query.length() && string.length() > query.length() ) return string.charAt(string.length() - 1 - query.length()) == ' ';
        query = " " + query + " ";
        return string.contains(query);
    }

    public static void showTeamCode(final Context context, final String teamCode, final RegenTokenListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Your team code is: ");
        builder.setMessage(teamCode);

        builder.setPositiveButton("COPY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Roblu team code", teamCode);
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
                Toast.makeText(context, "Team code copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Regenerate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                confirmRegenerate(context, listener);
            }
        });
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().getAttributes().windowAnimations = new Loader(context).loadSettings().getRui().getAnimation();
        dialog.show();
    }
    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.cpjd.roblu.cloud.sync.Service".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private static void confirmRegenerate(final Context context, final RegenTokenListener listener) {
        final RSettings settings = new Loader(context).loadSettings();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Warning");
        builder.setMessage("If you regenerate your team code, all of your team members must rejoin this team with the new code.");

        builder.setPositiveButton("Regenerate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if(!Text.hasInternetConnection(context)) {
                        Toast.makeText(context, "You are not connected to the internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CloudRequest cr = new CloudRequest(settings.getAuth(), settings.getTeamCode());
                    // first, regenerate the token
                    JSONObject response = (JSONObject) cr.regenerateToken();
                    if(!response.get("status").toString().equalsIgnoreCase("success")) throw new Exception();
                    JSONArray updates = (JSONArray) response.get("data");
                    // get & set the new token
                    String token = ((JSONObject)updates.get(0)).get("code").toString();
                    settings.setTeamCode(token);
                    new Loader(context).saveSettings(settings);
                    showTeamCode(context, token, listener);
                    dialog.dismiss();
                    listener.tokenRegenerated(token);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("error: "+e.getMessage());
                    Toast.makeText(context, "Error occurred while contacting Roblu Server. Please try again later.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().getAttributes().windowAnimations = new Loader(context).loadSettings().getRui().getDialogDirection();
        dialog.show();
    }

}
